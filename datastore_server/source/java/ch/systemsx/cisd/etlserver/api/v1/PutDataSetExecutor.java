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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.IDataStrategyStore;
import ch.systemsx.cisd.etlserver.IETLServerPlugin;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.TransferredDataSetHandler;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A helper class for carrying out the put command for creating data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PutDataSetExecutor
{
    // General State
    private final PutDataSetService service;

    // Command Context State
    private final IETLServerPlugin plugin;

    private final String sessionToken;

    private final NewDataSetDTO newDataSet;

    private final InputStream inputStream;

    private final File dataSetDir;

    PutDataSetExecutor(PutDataSetService service, IETLServerPlugin plugin, String sessionToken,
            NewDataSetDTO newDataSet, InputStream inputStream)
    {
        this.service = service;
        this.plugin = plugin;
        this.sessionToken = sessionToken;
        this.newDataSet = newDataSet;
        this.inputStream = inputStream;
        this.dataSetDir = new File(service.getIncomingDir(), newDataSet.getDataSetFolderName());
        if (dataSetDir.exists())
        {
            deleteDataSetDir();
        }
        if (false == this.dataSetDir.mkdir())
        {
            throw new EnvironmentFailureException("Could not create directory for data set "
                    + newDataSet.getDataSetFolderName());
        }
    }

    /**
     * Run the put command; does *not* close the input stream &mdash; clients are expected to close
     * the input stream when appropriate.
     * 
     * @throws IOException
     */
    public void execute() throws UserFailureException, IOException
    {
        // Check that the session owner has at least user access to the space the new data
        // set should belongs to
        SpaceIdentifier spaceId = getSpaceIdentifierForNewDataSet();
        getOpenBisService().checkSpaceAccess(sessionToken, spaceId);

        writeDataSetToTempDirectory();

        // Register the data set
        try
        {
            DataSetRegistrationHelper helper =
                    new DataSetRegistrationHelper(service, plugin, dataSetDir);
            helper.prepare();
            if (helper.hasDataSetBeenIdentified())
            {
                helper.registerDataSet();
            } else
            {
                helper.dealWithUnidentifiedDataSet();
                throw new UserFailureException("Could not find owner:\n\t"
                        + newDataSet.getDataSetOwner() + "\nfor data set:\n\t" + newDataSet);
            }
        } finally
        {
            deleteDataSetDir();
        }
    }

    private void writeDataSetToTempDirectory() throws IOException
    {
        ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(inputStream);
        for (FileInfoDssDTO fileInfo : newDataSet.getFileInfos())
        {
            OutputStream output = getOutputStream(fileInfo);
            imagesWriter.writeNextBlock(output);
            output.flush();
            output.close();
        }
    }

    private OutputStream getOutputStream(FileInfoDssDTO fileInfo)
    {
        File file = new File(dataSetDir, fileInfo.getPathInDataSet());
        System.out.println(file);

        FileOutputStream fos;
        try
        {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException ex)
        {
            // This should not happen since we just created the directory to store the new file in
            throw new EnvironmentFailureException("Could not save file " + file, ex);
        }
        return new BufferedOutputStream(fos);
    }

    private void deleteDataSetDir()
    {
        try
        {
            FileUtils.deleteDirectory(dataSetDir);
        } catch (IOException ex)
        {
            getOperationLog().error("Could not delete data set directory " + dataSetDir, ex);
            ex.printStackTrace();
        }
    }

    private SpaceIdentifier getSpaceIdentifierForNewDataSet()
    {
        SpaceIdentifier spaceId = null;
        DataSetOwner owner = newDataSet.getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
            {
                ExperimentIdentifier experimentId = tryExperimentIdentifier();
                spaceId =
                        new SpaceIdentifier(experimentId.getDatabaseInstanceCode(), experimentId
                                .getSpaceCode());
                break;
            }
            case SAMPLE:
            {
                SampleIdentifier sampleId = trySampleIdentifier();
                spaceId = sampleId.getSpaceLevel();
                break;
            }
        }
        return spaceId;
    }

    private ExperimentIdentifier tryExperimentIdentifier()
    {
        DataSetOwner owner = newDataSet.getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
            {
                return new ExperimentIdentifierFactory(owner.getIdentifier()).createIdentifier();
            }
            case SAMPLE:
            {
                return null;
            }
        }

        return null;
    }

    private SampleIdentifier trySampleIdentifier()
    {
        DataSetOwner owner = newDataSet.getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
            {
                return null;
            }
            case SAMPLE:
            {
                return new SampleIdentifierFactory(owner.getIdentifier()).createIdentifier();

            }
        }

        return null;
    }

    private IEncapsulatedOpenBISService getOpenBisService()
    {
        return service.getOpenBisService();
    }

    private Logger getOperationLog()
    {
        return service.getOperationLog();
    }

    private static class CleanAfterwardsAction implements IDelegatedActionWithResult<Boolean>
    {
        public Boolean execute()
        {
            return true; // do nothing
        }
    }

    /**
     * An implementation of the registration algorithm that gets its state from the executor.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private class DataSetRegistrationHelper extends DataSetRegistrationAlgorithm
    {
        /**
         * @param service The provider of global state for the data set registration algorithm
         * @param plugin The provider of the storage processor
         * @param incomingDataSetFile The data set to register
         */
        public DataSetRegistrationHelper(PutDataSetService service, IETLServerPlugin plugin,
                File incomingDataSetFile)
        {
            super(incomingDataSetFile, new CleanAfterwardsAction());
        }

        @Override
        protected IDataSetInfoExtractor getDataSetInfoExtractor()
        {
            return plugin.getDataSetInfoExtractor();
        }

        @Override
        protected IDataSetValidator getDataSetValidator()
        {
            return service.getDataSetValidator();
        }

        @Override
        protected String getDataStoreCode()
        {
            return service.getDataStoreCode();
        }

        @Override
        protected IDataStrategyStore getDataStrategyStore()
        {
            return service.getDataStrategyStore();
        }

        @Override
        protected String getEmailSubjectTemplate()
        {
            return TransferredDataSetHandler.EMAIL_SUBJECT_TEMPLATE;
        }

        @Override
        protected IFileOperations getFileOperations()
        {
            return FileOperations.getMonitoredInstanceForCurrentThread();
        }

        @Override
        protected DatabaseInstance getHomeDatabaseInstance()
        {
            return service.getHomeDatabaseInstance();
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
            if (ex instanceof UserFailureException)
            {
                throw (UserFailureException) ex;
            }
            throw new EnvironmentFailureException("Could not register data set " + newDataSet, ex);
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

        @Override
        protected DataSetInformation extractDataSetInformation(final File incomingDataSetPath)
        {
            DataSetInformation dataSetInfo = super.extractDataSetInformation(incomingDataSetPath);
            if (null == dataSetInfo)
            {
                return dataSetInfo;
            }
            DataSetOwner owner = newDataSet.getDataSetOwner();
            switch (owner.getType())
            {
                case EXPERIMENT:
                    dataSetInfo.setExperimentIdentifier(tryExperimentIdentifier());
                    break;
                case SAMPLE:
                    SampleIdentifier sampleId = trySampleIdentifier();

                    dataSetInfo.setSampleCode(sampleId.getSampleCode());
                    dataSetInfo.setSpaceCode(sampleId.getSpaceLevel().getSpaceCode());
                    dataSetInfo.setInstanceCode(sampleId.getSpaceLevel().getDatabaseInstanceCode());
                    break;
            }

            // TODO: Get the session owner's email address from OpenBIS
            // dataSetInfo.setUploadingUserEmail()

            // TODO: When registering, set the registrator to the session owner; only an admin on
            // the space or an ETL server can override.
            return dataSetInfo;
        }

        @Override
        protected DataSetType extractDataSetType()
        {
            return new DataSetType(newDataSet.getDataSetType());
        }
    }
}
