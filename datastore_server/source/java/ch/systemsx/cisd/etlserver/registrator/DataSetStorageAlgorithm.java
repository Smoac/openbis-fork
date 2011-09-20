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

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.BaseDirectoryHolder;
import ch.systemsx.cisd.etlserver.DataStoreStrategyKey;
import ch.systemsx.cisd.etlserver.IDataStoreStrategy;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.StorageProcessorTransactionParameters;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
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

    private final IStorageProcessorTransactional storageProcessor;

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
            IDataStoreStrategy dataStoreStrategy, IStorageProcessorTransactional storageProcessor,
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
        // Rollback may be called on in the stored state or in the prepared state.
        if (state instanceof PreparedState)
        {
            PreparedState<T> preparedState = (PreparedState<T>) state;
            if (preparedState.wasStoreDataAttempted())
            {
                // If a storeData() was attempted and failed, then we need to move to the stored
                // state in order to rollback
                state = new StoredState<T>((PreparedState<T>) state);
            } else
            {
                // If no storeData() was invoked, there is nothing to do
                return;
            }
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

        // Do not undo the individual data sets -- the entire transaction needs to be undone as a
        // group
        // rolledbackState.executeUndoAction();

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
        File dataFile = ((StoredState<T>) state).getStoredDirectory();
        String relativePath = FileUtilities.getRelativeFilePath(storeRoot, dataFile);
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
        return "Error trying to register data set '" + incomingDataSetFile.getName() + "'.";
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

    protected IStorageProcessorTransactional getStorageProcessor()
    {
        return storageProcessor;
    }

    protected String getDataStoreCode()
    {
        return dataStoreCode;
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

        protected final IStorageProcessorTransactional storageProcessor;

        protected final DataSetInformation dataSetInformation;

        protected File markerFile;

        protected IStorageProcessorTransaction transaction;

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

            StorageProcessorTransactionParameters transactionParameters =
                    new StorageProcessorTransactionParameters(
                            storageAlgorithm.getDataSetInformation(), incomingDataSetFile,
                            baseDirectoryHolder.getBaseDirectory());
            transaction = storageProcessor.createTransaction(transactionParameters);
            transaction.storeData(storageAlgorithm.getRegistrationDetails(), getMailClient(),
                    incomingDataSetFile);
            if (getOperationLog().isInfoEnabled())
            {
                getOperationLog().info(
                        "Finished storing data set for " + entityDescription + ", took " + watch);
            }
            assert transaction.getStoredDataDirectory() != null : "The folder that contains the stored data should not be null.";
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

        private boolean wasStoreDataAttempted()
        {
            return transaction != null;
        }
    }

    private static class StoredState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {
        protected final IStorageProcessorTransaction transaction;

        protected final File markerFile;

        public StoredState(PreparedState<T> oldState)
        {
            super(oldState.storageAlgorithm);
            this.transaction = oldState.transaction;
            this.markerFile = oldState.markerFile;
        }

        /**
         * Ask the storage processor to rollback. Used by clients of the algorithm.
         */
        public UnstoreDataAction rollbackStorageProcessor(final Throwable throwable)
        {
            UnstoreDataAction action = transaction.rollback(throwable);
            cleanUp();
            return action;
        }

        /**
         * Ask the storage processor to commit. Used by clients of the algorithm.
         */
        public void commitStorageProcessor()
        {
            transaction.commit();
            cleanUp();
        }

        /**
         * The directory with the stored dataset.
         */
        public File getStoredDirectory()
        {
            return transaction.getStoredDataDirectory();
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

        public RolledbackState(StoredState<T> oldState, UnstoreDataAction action,
                Throwable throwable)
        {
            super(oldState.storageAlgorithm);
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
