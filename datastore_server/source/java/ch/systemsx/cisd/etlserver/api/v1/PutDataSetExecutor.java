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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.IDataStrategyStore;
import ch.systemsx.cisd.etlserver.IETLServerPlugin;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A helper class for carrying out the put command for creating data sets.
 * <p>
 * It is a data set handler that allows overriding information obtained from the extractors.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PutDataSetExecutor implements IDataSetHandlerRpc
{
    // General State
    private final PutDataSetService service;

    // Command Context State
    private final IETLServerPlugin plugin;

    private final String sessionToken;

    private final NewDataSetDTO newDataSet;

    private final InputStream inputStream;

    private final File dataSetDir;

    private final OverridingTypeExtractor overridingTypeExtractor;

    private final IDataSetHandler handler;

    private final IImmutableCopier copier;

    private DataSetInformation override;

    PutDataSetExecutor(PutDataSetService service, IETLServerPlugin plugin, String sessionToken,
            NewDataSetDTO newDataSet, InputStream inputStream)
    {
        this.service = service;
        this.plugin = plugin;
        this.sessionToken = sessionToken;
        this.newDataSet = newDataSet;
        this.inputStream = inputStream;
        this.copier = FastRecursiveHardLinkMaker.tryCreate();
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

        overridingTypeExtractor = new OverridingTypeExtractor();
        handler = plugin.getDataSetHandler(this, service.getOpenBisService());
    }

    /**
     * Run the put command; does *not* close the input stream &mdash; clients of the executor are
     * expected to close the input stream when appropriate.
     * 
     * @throws IOException
     */
    public String execute() throws UserFailureException, IOException
    {
        // Check that the session owner has at least user access to the space the new data
        // set should belongs to
        SpaceIdentifier spaceId = getSpaceIdentifierForNewDataSet();
        getOpenBisService().checkSpaceAccess(sessionToken, spaceId);

        writeDataSetToTempDirectory();
        createDefaultOverride();

        // Register the data set
        try
        {
            List<DataSetInformation> infos = handler.handleDataSet(dataSetDir);
            if (infos.isEmpty())
            {
                return "";
            } else
            {
                return infos.get(0).getDataSetCode();
            }
        } finally
        {
            deleteDataSetDir();
        }

    }

    public List<DataSetInformation> handleDataSet(File dataSet)
    {
        return handleDataSet(dataSet, null);
    }

    public List<DataSetInformation> handleDataSet(File dataSet, DataSetInformation newOverride)
    {
        // Remember the old override, replace it with the override for the execution, then restore
        // it
        DataSetInformation oldOverride = override;
        if (newOverride != null)
        {
            override = newOverride;
        }

        RegistrationHelper helper = new RegistrationHelper(service, plugin, dataSet);
        helper.prepare();
        if (helper.hasDataSetBeenIdentified())
        {
            helper.registerDataSet();
        } else
        {
            helper.dealWithUnidentifiedDataSet();
            throw new UserFailureException("Could not find owner:\n\t" + newOverride
                    + "\nfor data set:\n\t" + dataSet);
        }

        override = oldOverride;

        return Collections.singletonList(helper.getDataSetInformation());
    }

    public List<DataSetInformation> linkAndHandleDataSet(File dataSetComponent,
            DataSetInformation newOverride)
    {
        File incomingDir = service.getIncomingDir();
        // Make a hard link to the file within the data set
        boolean success;
        success = copier.copyImmutably(dataSetComponent, incomingDir, null);
        if (success == false)
        {
            throw new EnvironmentFailureException("Couldn't create a hard-link copy of '"
                    + dataSetComponent.getAbsolutePath() + "' in folder '"
                    + service.getIncomingDir().getAbsolutePath() + "'.");
        }

        File linkedFile = new File(incomingDir, dataSetComponent.getName());

        // Register the component of the data set
        try
        {
            return handleDataSet(linkedFile, newOverride);
        } finally
        {
            deleteDir(linkedFile);
        }
    }

    public SessionContextDTO getSessionContext()
    {
        return getOpenBisService().tryGetSession(sessionToken);
    }

    public File getFileForExternalData(ExternalData externalData)
    {
        File dataSetFile = new File(service.getStoreRootDirectory(), externalData.getLocation());
        return DefaultStorageProcessor.getOriginalDirectory(dataSetFile);
    }

    public String getDataStoreCode()
    {
        return service.getDataStoreCode();
    }

    public DataSetOwner getDataSetOwner()
    {
        DataSetOwner owner = newDataSet.getDataSetOwner();
        return owner;
    }

    public DataSetInformation getCallerDataSetInformation()
    {
        return override;
    }

    private void createDefaultOverride()
    {
        override = new DataSetInformation();
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
                override.setExperimentIdentifier(tryExperimentIdentifier());
                break;
            case SAMPLE:
                SampleIdentifier sampleId = trySampleIdentifier();

                override.setSampleCode(sampleId.getSampleCode());
                override.setSpaceCode(sampleId.getSpaceLevel().getSpaceCode());
                override.setInstanceCode(sampleId.getSpaceLevel().getDatabaseInstanceCode());
                break;
        }
        String typeCode = newDataSet.tryDataSetType();
        if (null != typeCode)
        {
            override.setDataSetType(new DataSetType(typeCode));
        }

        Map<String, String> primitiveProps = newDataSet.getProperties();
        if (false == primitiveProps.isEmpty())
        {
            ArrayList<NewProperty> properties = new ArrayList<NewProperty>();
            for (String key : primitiveProps.keySet())
            {
                properties.add(new NewProperty(key, primitiveProps.get(key)));
            }
            override.setDataSetProperties(properties);
        }
    }

    private void writeDataSetToTempDirectory() throws IOException
    {
        ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(inputStream);
        for (FileInfoDssDTO fileInfo : newDataSet.getFileInfos())
        {
            if (fileInfo.isDirectory())
            {
                // Just make the directory
                File file = new File(dataSetDir, fileInfo.getPathInDataSet());
                file.mkdir();
            } else
            {
                // Download the file -- the directory should have already been made
                OutputStream output = getOutputStream(fileInfo);
                imagesWriter.writeNextBlock(output);
                output.flush();
                output.close();
            }
        }
    }

    private OutputStream getOutputStream(FileInfoDssDTO fileInfo)
    {
        File file = new File(dataSetDir, fileInfo.getPathInDataSet());

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
        deleteDir(dataSetDir);
    }

    private void deleteDir(File dirToDelete)
    {
        try
        {
            FileUtils.deleteDirectory(dirToDelete);
        } catch (IOException ex)
        {
            getOperationLog().error("Could not delete data set directory " + dataSetDir, ex);
            ex.printStackTrace();
        }
    }

    private SpaceIdentifier getSpaceIdentifierForNewDataSet()
    {
        SpaceIdentifier spaceId = null;
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
            {
                ExperimentIdentifier experimentId = tryExperimentIdentifier();
                spaceId =
                        new SpaceIdentifier(experimentId.getDatabaseInstanceCode(),
                                experimentId.getSpaceCode());
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
        DataSetOwner owner = getDataSetOwner();
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
        DataSetOwner owner = getDataSetOwner();
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

    public OverridingTypeExtractor getOverridingTypeExtractor()
    {
        return overridingTypeExtractor;
    }

    private static class CleanAfterwardsAction implements IDelegatedActionWithResult<Boolean>
    {
        public Boolean execute()
        {
            return true; // do nothing
        }
    }

    /**
     * Implementation of ITypeExtractor that overrides the plugin's type extractor only if the
     * caller has provided an override in the {@link NewDataSetDTO}.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private class OverridingTypeExtractor implements ITypeExtractor
    {
        private final ITypeExtractor pluginTypeExtractor;

        OverridingTypeExtractor()
        {
            pluginTypeExtractor = plugin.getTypeExtractor();
        }

        public DataSetType getDataSetType(File incomingDataSetPath)
        {
            DataSetType dataSetType = override.getDataSetType();
            if (null != dataSetType)
            {
                return dataSetType;
            }
            return pluginTypeExtractor.getDataSetType(incomingDataSetPath);
        }

        public FileFormatType getFileFormatType(File incomingDataSetPath)
        {
            return pluginTypeExtractor.getFileFormatType(incomingDataSetPath);
        }

        public LocatorType getLocatorType(File incomingDataSetPath)
        {
            return pluginTypeExtractor.getLocatorType(incomingDataSetPath);
        }

        public String getProcessorType(File incomingDataSetPath)
        {
            return pluginTypeExtractor.getProcessorType(incomingDataSetPath);
        }

        public boolean isMeasuredData(File incomingDataSetPath)
        {
            return pluginTypeExtractor.isMeasuredData(incomingDataSetPath);
        }

    }

    /**
     * An implementation of the registration algorithm that gets its state from the executor.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private class RegistrationHelper extends DataSetRegistrationAlgorithm
    {
        /**
         * @param service The provider of global state for the data set registration algorithm
         * @param plugin The provider of the storage processor
         * @param incomingDataSetFile The data set to register
         */
        public RegistrationHelper(PutDataSetService service, IETLServerPlugin plugin,
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
            return DataSetRegistrationAlgorithm.EMAIL_SUBJECT_TEMPLATE;
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
            return getOverridingTypeExtractor();
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

            // Override / extend information extracted with our override
            dataSetInfo.setExperimentIdentifier(override.getExperimentIdentifier());
            SampleIdentifier sampleIdOrNull = override.getSampleIdentifier();
            if (sampleIdOrNull != null)
            {
                dataSetInfo.setSampleCode(sampleIdOrNull.getSampleCode());
                dataSetInfo.setSpaceCode(sampleIdOrNull.getSpaceLevel().getSpaceCode());
                dataSetInfo.setInstanceCode(sampleIdOrNull.getSpaceLevel()
                        .getDatabaseInstanceCode());
            }

            // Override the properties if some have been specified
            if (false == override.getDataSetProperties().isEmpty())
            {
                dataSetInfo.setDataSetProperties(override.getDataSetProperties());
            }

            final SessionContextDTO session =
                    service.getOpenBisService().tryGetSession(sessionToken);
            dataSetInfo.setUploadingUserId(session.getUserName());
            return dataSetInfo;
        }
    }
}
