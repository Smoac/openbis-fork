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
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.etlserver.DataStrategyStore;
import ch.systemsx.cisd.etlserver.ETLServerPluginFactory;
import ch.systemsx.cisd.etlserver.IETLServerPlugin;
import ch.systemsx.cisd.etlserver.Parameters;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.validation.DataSetValidator;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
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

    // These are all initialized only once, but it is not possible to initialize them at
    // construction time, since this causes a dependency loop that causes problems in Spring.
    private DataSetTypeToPluginMapper pluginMap;

    private DataStrategyStore dataStrategyStore;

    private File storeDirectory;

    private String dataStoreCode;

    private boolean isInitialized = false;

    private IMailClient mailClient;

    private File incomingDir;

    private IDataSetValidator dataSetValidator;

    private DatabaseInstance homeDatabaseInstance;

    /**
     * The designated constructor.
     * 
     * @param openBisService
     * @param operationLog
     */
    public PutDataSetService(IEncapsulatedOpenBISService openBisService, Logger operationLog)
    {
        this.openBisService = openBisService;
        this.operationLog = operationLog;

        this.registrationLock = new ReentrantLock();
    }

    /**
     * A constructor for testing purposes. Not useful outside of testing.
     * 
     * @param openBisService
     * @param operationLog
     * @param store
     * @param incoming
     * @param map
     * @param mail
     * @param dsCode
     */
    public PutDataSetService(IEncapsulatedOpenBISService openBisService, Logger operationLog,
            File store, File incoming, DataSetTypeToPluginMapper map, IMailClient mail,
            String dsCode, IDataSetValidator validator)
    {
        this(openBisService, operationLog);

        incomingDir = incoming;
        incomingDir.mkdir();

        pluginMap = map;
        storeDirectory = store;
        pluginMap.initializeStoreRootDirectory(storeDirectory);

        mailClient = mail;
        dataStrategyStore = new DataStrategyStore(openBisService, mailClient);

        this.dataStoreCode = dsCode;

        homeDatabaseInstance = openBisService.getHomeDatabaseInstance();

        dataSetValidator = validator;

        isInitialized = true;

    }

    public String putDataSet(String sessionToken, NewDataSetDTO newDataSet, InputStream inputStream)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        if (false == isInitialized)
        {
            doInitialization();
        }

        try
        {
            String dataSetTypeOrNull = newDataSet.tryDataSetType();
            IETLServerPlugin thePlugin = pluginMap.getPluginForType(dataSetTypeOrNull);
            List<DataSetInformation> infos =
                    new PutDataSetExecutor(this, thePlugin, sessionToken, newDataSet, inputStream)
                            .execute();
            StringBuilder sb = new StringBuilder();
            for (DataSetInformation info : infos)
            {
                sb.append(info.getDataSetCode());
                sb.append(",");
            }

            // Remove the trailing comma
            if (sb.length() > 0)
            {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
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

        incomingDir.mkdirs();

        pluginMap = initializer.getPluginMap();
        pluginMap.initializeStoreRootDirectory(storeDirectory);

        // plugin = initializer.getPlugin();
        // plugin.getStorageProcessor().setStoreRootDirectory(storeDirectory);

        mailClient = new MailClient(initializer.getMailProperties());
        dataStrategyStore = new DataStrategyStore(openBisService, mailClient);

        this.dataStoreCode = initializer.getDataStoreCode();

        homeDatabaseInstance = openBisService.getHomeDatabaseInstance();

        dataSetValidator = initializer.getDataSetValidator();

        isInitialized = true;
    }

    IEncapsulatedOpenBISService getOpenBisService()
    {
        return openBisService;
    }

    IMailClient getMailClient()
    {
        return mailClient;
    }

    File getIncomingDir()
    {
        return incomingDir;
    }

    public void setIncomingDir(File aDir)
    {
        incomingDir = aDir;
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

    public DataSetTypeToPluginMapper getPluginMap()
    {
        return new DataSetTypeToPluginMapper(params);
    }

    Properties getMailProperties()
    {
        return Parameters.createMailProperties(params.getProperties());
    }

    IETLServerPlugin getPlugin()
    {
        ThreadParameters[] threadParams = params.getThreads();
        return ETLServerPluginFactory.getPluginForThread(threadParams[0]);
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
