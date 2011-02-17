/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.BaseDirectoryHolder;
import ch.systemsx.cisd.etlserver.DataStoreStrategyKey;
import ch.systemsx.cisd.etlserver.FileRenamer;
import ch.systemsx.cisd.etlserver.IDataStoreStrategy;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.etlserver.IStorageProcessor.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.TransferredDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.ConversionUtils;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * DataSetStorageAlgorithm is a state machine that executes steps to store a data set in the store
 * and transitions between states.
 * <p>
 * The states and transitions are as follows: <br>
 * 
 * <pre>
 *                                             /-> (Committed*)
 * (Initialized) -> (Prepared) -> (Stored) -<
 *                     \                       \-> (Rolledback) -> (UndoneState*)
 *                      \ -------------------------------------/
 * </pre>
 * 
 * States marked with a "*" are terminal.
 * <p>
 * N.b. Methods invoked on states for which the method is not valid will yield a class cast
 * exception since the state object will not be castable to the desired class.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetStorageAlgorithm<T extends DataSetInformation>
{
    static private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetStorageAlgorithm.class);

    private final File incomingDataSetFile;

    private final DataSetRegistrationDetails<T> registrationDetails;

    private final T dataSetInformation;

    private final IDataStoreStrategy dataStoreStrategy;

    private final IStorageProcessor storageProcessor;

    private final String dataStoreCode;

    private final DataSetType dataSetType;

    private final File storeRoot;

    private final IFileOperations fileOperations;

    // Used by the storage processor
    private final IMailClient mailClient;

    // State that changes during execution
    private DataSetStorageAlgorithmState<T> state;

    /**
     * Utility method for creating base directories
     */
    public static File createBaseDirectory(final IDataStoreStrategy strategy, final File baseDir,
            IFileOperations fileOperations, final DataSetInformation dataSetInfo,
            DataSetType dataSetType, File incomingDataSetFile)
    {
        final File baseDirectory = strategy.getBaseDirectory(baseDir, dataSetInfo, dataSetType);
        baseDirectory.mkdirs();
        if (fileOperations.isDirectory(baseDirectory) == false)
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Creating data set base directory '%s' for data set '%s' failed.",
                    baseDirectory.getAbsolutePath(), incomingDataSetFile);
        }
        return baseDirectory;
    }

    public DataSetStorageAlgorithm(File incomingDataSetFile,
            DataSetRegistrationDetails<T> registrationDetails,
            IDataStoreStrategy dataStoreStrategy, IStorageProcessor storageProcessor,
            IDataSetValidator dataSetValidator, String dataStoreCode,
            IFileOperations fileOperations, IMailClient mailClient)
    {
        this.incomingDataSetFile = incomingDataSetFile;
        this.registrationDetails = registrationDetails;
        this.dataSetInformation = registrationDetails.getDataSetInformation();
        this.dataStoreStrategy = dataStoreStrategy;
        this.storageProcessor = storageProcessor;
        this.dataStoreCode = dataStoreCode;
        this.fileOperations = fileOperations;
        this.mailClient = mailClient;

        this.storeRoot = storageProcessor.getStoreRootDirectory();
        this.dataSetType = registrationDetails.getDataSetType();

        assert dataStoreStrategy.getKey() == DataStoreStrategyKey.IDENTIFIED : "Data set must be associated with an experiment ";

        dataSetValidator.assertValidDataSet(dataSetType, incomingDataSetFile);

        state = new InitializedState<T>(this);
    }

    /**
     * Prepare registration of a data set.
     */
    public void prepare()
    {
        InitializedState<T> initializedState = (InitializedState<T>) state;
        initializedState.prepare();

        state = new PreparedState<T>(initializedState);
    }

    /**
     * Run the storage processor.
     */
    public void runStorageProcessor() throws Throwable
    {
        PreparedState<T> preparedState = (PreparedState<T>) state;
        preparedState.storeData();

        state = new StoredState<T>(preparedState);
    }

    /**
     * Ask the storage processor to rollback. Used by clients of the algorithm.
     */
    public void rollbackStorageProcessor(Throwable throwable)
    {
        // Rollback may be called on in the stored state or in the prepared state. In the prepared
        // state, there is nothing to do.
        if (state instanceof PreparedState)
        {
            return;
        }

        StoredState<T> storedState = (StoredState<T>) state;
        UnstoreDataAction action = storedState.rollbackStorageProcessor(throwable);

        state = new RolledbackState<T>(storedState, action, throwable);
    }

    public void executeUndoStoreAction()
    {
        // Rollback may be called on in the stored state or in the prepared state. In the prepared
        // state, there is nothing to do.
        if (state instanceof PreparedState)
        {
            state = new UndoneState<T>((PreparedState<T>) state);
            return;
        }

        RolledbackState<T> rolledbackState = (RolledbackState<T>) state;
        rolledbackState.executeUndoAction();

        state = new UndoneState<T>(rolledbackState);
    }

    /**
     * Ask the storage processor to commit. Used by clients of the algorithm.
     */
    public void commitStorageProcessor()
    {
        StoredState<T> storedState = (StoredState<T>) state;
        storedState.commitStorageProcessor();

        state = new CommittedState<T>(storedState);
    }

    public File getIncomingDataSetFile()
    {
        return incomingDataSetFile;
    }

    public T getDataSetInformation()
    {
        return dataSetInformation;
    }

    protected Logger getOperationLog()
    {
        return operationLog;
    }

    public NewExternalData createExternalData()
    {
        File dataFile = ((StoredState<T>) state).getDataFile();
        String relativePath = FileUtilities.getRelativeFile(storeRoot, dataFile);
        String absolutePath = dataFile.getAbsolutePath();
        assert relativePath != null : String.format(
                TransferredDataSetHandler.TARGET_NOT_RELATIVE_TO_STORE_ROOT, absolutePath,
                storeRoot.getAbsolutePath());
        StorageFormat storageFormat = storageProcessor.getStorageFormat();

        return ConversionUtils.convertToNewExternalData(registrationDetails, dataStoreCode,
                storageFormat, relativePath);
    }

    public String getSuccessRegistrationMessage()
    {
        return dataSetInformation.toString();
    }

    public String getFailureRegistrationMessage()
    {
        return "Error when trying to register data set '" + incomingDataSetFile.getName() + "'.";
    }

    protected DataSetType getDataSetType()
    {
        return dataSetType;
    }

    protected IDataStoreStrategy getDataStoreStrategy()
    {
        return dataStoreStrategy;
    }

    protected File getStoreRoot()
    {
        return storeRoot;
    }

    protected IStorageProcessor getStorageProcessor()
    {
        return storageProcessor;
    }

    protected DataSetRegistrationDetails<T> getRegistrationDetails()
    {
        return registrationDetails;
    }

    private static abstract class DataSetStorageAlgorithmState<T extends DataSetInformation>
    {
        protected final DataSetStorageAlgorithm<T> storageAlgorithm;

        protected final File incomingDataSetFile;

        protected DataSetStorageAlgorithmState(DataSetStorageAlgorithm<T> storageAlgorithm)
        {
            this.storageAlgorithm = storageAlgorithm;
            this.incomingDataSetFile = storageAlgorithm.getIncomingDataSetFile();
        }

        protected Logger getOperationLog()
        {
            return storageAlgorithm.getOperationLog();
        }

        protected IFileOperations getFileOperations()
        {
            return storageAlgorithm.fileOperations;
        }

        protected final File createBaseDirectory(final IDataStoreStrategy strategy,
                final File baseDir, final DataSetInformation dataSetInfo)
        {

            return DataSetStorageAlgorithm.createBaseDirectory(strategy, baseDir,
                    getFileOperations(), dataSetInfo, storageAlgorithm.getDataSetType(),
                    incomingDataSetFile);
        }
    }

    private static class InitializedState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {
        protected BaseDirectoryHolder baseDirectoryHolder;

        public InitializedState(DataSetStorageAlgorithm<T> storageAlgorithm)
        {
            super(storageAlgorithm);
        }

        /**
         * Prepare registration of a data set.
         */
        public void prepare()
        {
            IDataStoreStrategy dataStoreStrategy = storageAlgorithm.getDataStoreStrategy();
            final File baseDirectory =
                    createBaseDirectory(dataStoreStrategy, storageAlgorithm.getStoreRoot(),
                            storageAlgorithm.getDataSetInformation());
            baseDirectoryHolder =
                    new BaseDirectoryHolder(dataStoreStrategy, baseDirectory, incomingDataSetFile);
        }
    }

    private static class PreparedState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {
        protected final BaseDirectoryHolder baseDirectoryHolder;

        protected final IStorageProcessor storageProcessor;

        protected final DataSetInformation dataSetInformation;

        protected File markerFile;

        protected File dataFile;

        public PreparedState(InitializedState<T> oldState)
        {
            super(oldState.storageAlgorithm);
            this.storageProcessor = storageAlgorithm.getStorageProcessor();
            this.baseDirectoryHolder = oldState.baseDirectoryHolder;
            this.dataSetInformation = storageAlgorithm.getDataSetInformation();
        }

        public void storeData()
        {
            markerFile = createProcessingMarkerFile();
            String entityDescription = createEntityDescription();
            if (getOperationLog().isInfoEnabled())
            {
                getOperationLog().info("Start storing data set for " + entityDescription + ".");
            }
            final StopWatch watch = new StopWatch();
            watch.start();

            dataFile =
                    storageProcessor.storeData(storageAlgorithm.getDataSetInformation(),
                            storageAlgorithm.getRegistrationDetails(), getMailClient(),
                            incomingDataSetFile, baseDirectoryHolder.getBaseDirectory());
            if (getOperationLog().isInfoEnabled())
            {
                getOperationLog().info(
                        "Finished storing data set for " + entityDescription + ", took " + watch);
            }
            assert dataFile != null : "The folder that contains the stored data should not be null.";
        }

        private IMailClient getMailClient()
        {
            return storageAlgorithm.mailClient;
        }

        private final File createProcessingMarkerFile()
        {
            final File baseDirectory = baseDirectoryHolder.getBaseDirectory();
            final File baseParentDirectory = baseDirectory.getParentFile();
            final String processingDirName = baseDirectory.getName();
            markerFile =
                    new File(baseParentDirectory, Constants.PROCESSING_PREFIX + processingDirName);
            try
            {
                getFileOperations().createNewFile(markerFile);
            } catch (final IOExceptionUnchecked ex)
            {
                throw EnvironmentFailureException.fromTemplate(ex,
                        "Cannot create marker file '%s'.", markerFile.getPath());
            }
            return markerFile;
        }

        private String createEntityDescription()
        {
            SampleIdentifier sampleIdentifier = dataSetInformation.getSampleIdentifier();
            if (sampleIdentifier != null)
            {
                return "sample '" + sampleIdentifier + "'";
            }
            return "experiment '" + dataSetInformation.getExperimentIdentifier() + "'";
        }
    }

    private static class StoredState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {
        protected final IStorageProcessor storageProcessor;

        protected final BaseDirectoryHolder baseDirectoryHolder;

        protected final File markerFile;

        protected final File dataFile;

        public StoredState(PreparedState<T> oldState)
        {
            super(oldState.storageAlgorithm);
            this.storageProcessor = storageAlgorithm.getStorageProcessor();
            this.baseDirectoryHolder = oldState.baseDirectoryHolder;

            this.markerFile = oldState.markerFile;
            this.dataFile = oldState.dataFile;
        }

        protected File getDataFile()
        {
            return dataFile;
        }

        /**
         * Ask the storage processor to rollback. Used by clients of the algorithm.
         */
        public UnstoreDataAction rollbackStorageProcessor(final Throwable throwable)
        {
            UnstoreDataAction action =
                    storageProcessor.rollback(incomingDataSetFile,
                            baseDirectoryHolder.getBaseDirectory(), throwable);
            cleanUp();
            return action;
        }

        /**
         * Ask the storage processor to commit. Used by clients of the algorithm.
         */
        public void commitStorageProcessor()
        {
            storageProcessor.commit(incomingDataSetFile, baseDirectoryHolder.getBaseDirectory());
            cleanUp();
        }

        /**
         * Cleanup from the processing -- done after a commit or rollback
         */
        private void cleanUp()
        {
            getFileOperations().delete(markerFile);
        }
    }

    private static class CommittedState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {

        CommittedState(StoredState<T> oldState)
        {
            super(oldState.storageAlgorithm);
        }

    }

    private static class RolledbackState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {
        private final UnstoreDataAction action;

        private final Throwable throwable;

        private final File storeRoot;

        private BaseDirectoryHolder baseDirectoryHolder;

        public RolledbackState(StoredState<T> oldState, UnstoreDataAction action,
                Throwable throwable)
        {
            super(oldState.storageAlgorithm);
            this.action = action;
            this.throwable = throwable;

            this.storeRoot = oldState.storageProcessor.getStoreRootDirectory();

            this.baseDirectoryHolder = oldState.baseDirectoryHolder;
        }

        public void executeUndoAction()
        {
            if (action == UnstoreDataAction.MOVE_TO_ERROR)
            {
                final File baseDirectory =
                        createBaseDirectory(TransferredDataSetHandler.ERROR_DATA_STRATEGY,
                                storeRoot, storageAlgorithm.getDataSetInformation());
                baseDirectoryHolder =
                        new BaseDirectoryHolder(TransferredDataSetHandler.ERROR_DATA_STRATEGY,
                                baseDirectory, incomingDataSetFile);
                FileRenamer.renameAndLog(incomingDataSetFile, baseDirectoryHolder.getTargetFile());
                writeThrowable();
            } else if (action == UnstoreDataAction.DELETE)
            {
                FileUtilities.deleteRecursively(incomingDataSetFile, new Log4jSimpleLogger(
                        getOperationLog()));
            }
        }

        private void writeThrowable()
        {
            final String fileName = incomingDataSetFile.getName() + ".exception";
            final File file =
                    new File(baseDirectoryHolder.getTargetFile().getParentFile(), fileName);
            FileWriter writer = null;
            try
            {
                writer = new FileWriter(file);
                throwable.printStackTrace(new PrintWriter(writer));
            } catch (final IOException e)
            {
                getOperationLog().warn(
                        String.format("Could not write out the exception '%s' in file '%s'.",
                                fileName, file.getAbsolutePath()), e);
            } finally
            {
                IOUtils.closeQuietly(writer);
            }
        }
    }

    private static class UndoneState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {

        UndoneState(PreparedState<T> oldState)
        {
            super(oldState.storageAlgorithm);
        }

        UndoneState(RolledbackState<T> oldState)
        {
            super(oldState.storageAlgorithm);
        }

    }
}
