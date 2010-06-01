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
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.IDataStrategyStore;
import ch.systemsx.cisd.etlserver.IETLServerPlugin;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class DataSetRegistrationHelper extends DataSetRegistrationAlgorithm
{

    private final PutDataSetService service;

    private final IETLServerPlugin plugin;

    /**
     * @param incomingDataSetFile
     * @param cleanAftrewardsAction
     */
    public DataSetRegistrationHelper(PutDataSetService service, IETLServerPlugin plugin,
            File incomingDataSetFile, IDelegatedActionWithResult<Boolean> cleanAftrewardsAction)
    {
        super(incomingDataSetFile, cleanAftrewardsAction);
        this.service = service;
        this.plugin = plugin;
    }

    @Override
    protected IDataSetInfoExtractor getDataSetInfoExtractor()
    {
        return plugin.getDataSetInfoExtractor();
    }

    @Override
    protected IDataSetValidator getDataSetValidator()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getDataStoreCode()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IDataStrategyStore getDataStrategyStore()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getEmailSubjectTemplate()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IFileOperations getFileOperations()
    {
        return FileOperations.getMonitoredInstanceForCurrentThread();
    }

    @Override
    protected DatabaseInstance getHomeDatabaseInstance()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IMailClient getMailClient()
    {
        return service.getMailClient();
    }

    @Override
    protected Logger getNotificationLog()
    {
        return service.getOperationLog();
    }

    @Override
    protected IEncapsulatedOpenBISService getOpenBisService()
    {
        return service.getOpenBisService();
    }

    @Override
    protected Logger getOperationLog()
    {
        return service.getOperationLog();
    }

    @Override
    protected Lock getRegistrationLock()
    {
        return service.getRegistrationLock();
    }

    @Override
    protected IStorageProcessor getStorageProcessor()
    {
        return plugin.getStorageProcessor();
    }

    @Override
    protected ITypeExtractor getTypeExtractor()
    {
        return plugin.getTypeExtractor();
    }

    @Override
    protected void rollback(Throwable ex)
    {
        rollbackStorageProcessor(ex);
    }

    @Override
    protected boolean shouldDeleteUnidentified()
    {
        return true;
    }

    @Override
    protected boolean shouldNotifySuccessfulRegistration()
    {
        return false;
    }

}
