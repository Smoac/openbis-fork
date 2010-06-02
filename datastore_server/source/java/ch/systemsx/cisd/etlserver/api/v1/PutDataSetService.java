/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.etlserver.api.v1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.etlserver.DataStrategyStore;
import ch.systemsx.cisd.etlserver.IETLServerPlugin;
import ch.systemsx.cisd.etlserver.Parameters;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.validation.DataSetValidator;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * Helper class that maintains the state for handling put requests. The requests themselves are
 * serviced by the {@link PutDataSetExecutor}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class PutDataSetService
{
    private final IEncapsulatedOpenBISService openBisService;

    private final Logger operationLog;

    private final Lock registrationLock;

    private final DataStrategyStore dataStrategyStore;

    // These are all initialized only once, but it is not possible to initialize them at
    // construction time, since this causes a dependency loop that causes problems in Spring.
    private File storeDirectory;

    private String dataStoreCode;

    private boolean isInitialized = false;

    private MailClient mailClient;

    private IETLServerPlugin plugin;

    private File incomingDir;

    private IDataSetValidator dataSetValidator;

    private DatabaseInstance homeDatabaseInstance;

    public PutDataSetService(IEncapsulatedOpenBISService openBisService, Logger operationLog)
    {
        this.openBisService = openBisService;
        this.operationLog = operationLog;

        this.registrationLock = new ReentrantLock();

        this.dataStrategyStore = new DataStrategyStore(this.openBisService, mailClient);
    }

    public void putDataSet(String sessionToken, NewDataSetDTO newDataSet, InputStream inputStream)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        if (false == isInitialized)
        {
            doInitialization();
        }

        try
        {
            new PutDataSetExecutor(this, plugin, sessionToken, newDataSet, inputStream).execute();
        } catch (UserFailureException e)
        {
            throw new IllegalArgumentException(e);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        } finally
        {
            // Close the input stream now that we are done with it
            try
            {
                inputStream.close();
            } catch (IOException ex)
            {

            }
        }
    }

    private void doInitialization()
    {
        PutDataSetServiceInitializer initializer = new PutDataSetServiceInitializer();

        incomingDir = initializer.getIncomingDir();
        incomingDir.mkdir();

        plugin = initializer.getPlugin();
        plugin.getStorageProcessor().setStoreRootDirectory(storeDirectory);

        mailClient = new MailClient(initializer.getMailProperties());

        this.dataStoreCode = initializer.getDataStoreCode();

        homeDatabaseInstance = openBisService.getHomeDatabaseInstance();

        dataSetValidator = initializer.getDataSetValidator();

        isInitialized = true;
    }

    IEncapsulatedOpenBISService getOpenBisService()
    {
        return openBisService;
    }

    MailClient getMailClient()
    {
        return mailClient;
    }

    File getIncomingDir()
    {
        return incomingDir;
    }

    Logger getOperationLog()
    {
        return operationLog;
    }

    Lock getRegistrationLock()
    {
        return registrationLock;
    }

    DataStrategyStore getDataStrategyStore()
    {
        return dataStrategyStore;
    }

    String getDataStoreCode()
    {
        return dataStoreCode;
    }

    IDataSetValidator getDataSetValidator()
    {
        return dataSetValidator;
    }

    DatabaseInstance getHomeDatabaseInstance()
    {
        return homeDatabaseInstance;
    }

    public File getStoreRootDirectory()
    {
        return storeDirectory;
    }

    public void setStoreDirectory(File storeDirectory)
    {
        this.storeDirectory = storeDirectory;
    }

}

/**
 * Helper class to simplify initializing the final fields of the {@link PutDataSetService}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PutDataSetServiceInitializer
{
    private final Parameters params;

    PutDataSetServiceInitializer()
    {
        params = Parameters.createParametersForApiUse();
    }

    File getIncomingDir()
    {
        return new File(System.getProperty("java.io.tmpdir"), "dss_rpc_incoming");
    }

    Properties getMailProperties()
    {
        return Parameters.createMailProperties(params.getProperties());
    }

    IETLServerPlugin getPlugin()
    {
        ThreadParameters[] threadParams = params.getThreads();
        return threadParams[0].getPlugin();
    }

    String getDataStoreCode()
    {
        return DssPropertyParametersUtil.getDataStoreCode(params.getProperties());
    }

    DataSetValidator getDataSetValidator()
    {
        return new DataSetValidator(params.getProperties());
    }
}
