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

import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.IDataStoreStrategy;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.NullStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.ConversionUtils;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * An implementation of the storage algorithm that registers container data sets. Container data
 * sets are a little different since they have no files; this requires a different registration
 * process.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ContainerDataSetStorageAlgorithm<T extends DataSetInformation> extends
        DataSetStorageAlgorithm<T>
{
    // We use our own states, not the ones of the parent.
    private DataSetStorageAlgorithmState<T> state;

    /**
     * @param incomingDataSetFile
     * @param registrationDetails
     * @param dataStoreStrategy
     * @param storageProcessor
     * @param dataSetValidator
     * @param dataStoreCode
     * @param fileOperations
     * @param mailClient
     * @param stagingDirectory
     */
    public ContainerDataSetStorageAlgorithm(File incomingDataSetFile,
            DataSetRegistrationDetails<? extends T> registrationDetails,
            IDataStoreStrategy dataStoreStrategy, IStorageProcessorTransactional storageProcessor,
            IDataSetValidator dataSetValidator, String dataStoreCode,
            IFileOperations fileOperations, IMailClient mailClient, File stagingDirectory,
            File precommitDirectory)
    {
        super(incomingDataSetFile, registrationDetails, dataStoreStrategy, storageProcessor,
                dataSetValidator, dataStoreCode, fileOperations, mailClient, stagingDirectory,
                precommitDirectory);

        state = new InitializedState<T>(this);
    }

    @Override
    public IStorageProcessorTransaction prepare(IRollbackStack rollbackStack)
    {
        InitializedState<T> initializedState = (InitializedState<T>) state;
        initializedState.prepare();

        state = new PreparedState<T>(initializedState);
        return new NullStorageProcessorTransaction();
    }

    @Override
    public void runStorageProcessor() throws Throwable
    {
        PreparedState<T> preparedState = (PreparedState<T>) state;
        preparedState.storeData();

        state = new StoredState<T>(preparedState);
    }

    @Override
    public void transitionToRolledbackState(Throwable throwable)
    {
        // Rollback may be called on in the stored state or in the prepared state.
        if (state instanceof PreparedState)
        {
            // Container data sets do not use the storage processer -- there is nothing to do
            return;
        }

        StoredState<T> storedState = (StoredState<T>) state;

        state = new RolledbackState<T>(storedState, UnstoreDataAction.LEAVE_UNTOUCHED, throwable);
    }

    @Override
    public void transitionToUndoneState()
    {
        // Rollback may be called on in the stored state or in the prepared state. In the prepared
        // state, there is nothing to do.
        if (state instanceof PreparedState)
        {
            state = new UndoneState<T>((PreparedState<T>) state);
            return;
        }

        RolledbackState<T> rolledbackState = (RolledbackState<T>) state;

        state = new UndoneState<T>(rolledbackState);
    }

    @Override
    public void commitStorageProcessor()
    {
        StoredState<T> storedState = (StoredState<T>) state;
        storedState.commitStorageProcessor();

        state = new CommittedState<T>(storedState);
    }

    @Override
    public NewExternalData createExternalData()
    {
        return ConversionUtils.convertToNewContainerDataSet(getRegistrationDetails(),
                getDataStoreCode());
    }

    @Override
    public String getSuccessRegistrationMessage()
    {
        // The success registration message is the same as the superclass, but for clarity, make
        // that explicit.
        return super.getSuccessRegistrationMessage();
    }

    @Override
    public String getFailureRegistrationMessage()
    {
        return "Error trying to register container data set '" + getDataSetInformation().toString()
                + "'.";
    }

    private static abstract class DataSetStorageAlgorithmState<T extends DataSetInformation>
    {
        protected final DataSetStorageAlgorithm<T> storageAlgorithm;

        protected DataSetStorageAlgorithmState(DataSetStorageAlgorithm<T> storageAlgorithm)
        {
            this.storageAlgorithm = storageAlgorithm;
        }
    }

    private static class InitializedState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {
        public InitializedState(DataSetStorageAlgorithm<T> storageAlgorithm)
        {
            super(storageAlgorithm);
        }

        /**
         * Prepare registration of a data set.
         */
        public void prepare()
        {
        }
    }

    private static class PreparedState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {
        public PreparedState(InitializedState<T> oldState)
        {
            super(oldState.storageAlgorithm);
        }

        public void storeData()
        {

        }
    }

    private static class StoredState<T extends DataSetInformation> extends
            DataSetStorageAlgorithmState<T>
    {

        public StoredState(PreparedState<T> oldState)
        {
            super(oldState.storageAlgorithm);
        }

        /**
         * Committed data sets don't use a storage process -- there is nothing to do.
         */
        public void commitStorageProcessor()
        {
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
