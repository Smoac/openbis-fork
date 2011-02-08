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
import java.util.ArrayList;
import java.util.Properties;

import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.DataSetRegistrationAlgorithmState;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.IRollbackDelegate;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithmRunner;
import ch.systemsx.cisd.etlserver.IDataStoreStrategy;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * A service that registers many files as individual data sets in one transaction.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationService implements IRollbackDelegate
{
    static final String STAGING_DIR = "staging-dir";

    private final AbstractOmniscientTopLevelDataSetRegistrator registrator;

    private final OmniscientTopLevelDataSetRegistratorState registratorState;

    private final IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction;

    private final ArrayList<DataSetRegistrationAlgorithm> dataSetRegistrations =
            new ArrayList<DataSetRegistrationAlgorithm>();

    /**
     * The currently live child transaction.
     */
    private DataSetRegistrationTransaction<DataSetInformation> liveTransactionOrNull;

    /**
     * A data set that will be created but might not yet exist.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public class FutureDataSet
    {
        private final String code;

        public FutureDataSet(String code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }
    }

    /**
     * Create a new DataSetRegistrationService.
     * 
     * @param registrator The top level data set registrator
     * @param globalCleanAfterwardsAction An action to execute when the service has finished
     */
    public DataSetRegistrationService(AbstractOmniscientTopLevelDataSetRegistrator registrator,
            IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction)
    {
        this.registrator = registrator;
        this.registratorState = registrator.getRegistratorState();
        this.globalCleanAfterwardsAction = globalCleanAfterwardsAction;
    }

    public OmniscientTopLevelDataSetRegistratorState getRegistratorState()
    {
        return registratorState;
    }

    /**
     * Queue registration a data set and return a future for the data set that will be created.
     */
    public FutureDataSet queueDataSetRegistration(File dataSetFile,
            DataSetRegistrationDetails<?> details)
    {
        DataSetRegistrationAlgorithm registration =
                createRegistrationAlgorithm(dataSetFile, details);
        dataSetRegistrations.add(registration);

        FutureDataSet future =
                new FutureDataSet(registration.getDataSetInformation().getDataSetCode());
        return future;
    }

    /**
     * Create a new transaction that atomically performs file operations and registers entities.
     */
    public IDataSetRegistrationTransaction transaction(File dataSetFile,
            IDataSetRegistrationDetailsFactory<DataSetInformation> detailsFactory)
    {
        // If a transaction is hanging around, commit it before starting a new one
        commitExtantTransaction();

        File workingDirectory = dataSetFile.getParentFile();
        Properties properties =
                registratorState.getGlobalState().getThreadParameters().getThreadProperties();
        File stagingDirectory =
                new File(PropertyUtils.getMandatoryProperty(properties, STAGING_DIR));

        // Clone this service for the transaction to keep them independent
        liveTransactionOrNull =
                new DataSetRegistrationTransaction<DataSetInformation>(registrator.getGlobalState()
                        .getStoreRootDir(), workingDirectory, stagingDirectory, this,
                        detailsFactory);

        return liveTransactionOrNull;
    }

    /**
     * Commit any scheduled changes.
     */
    public void commit()
    {
        // If a transaction is hanging around, commit it
        commitExtantTransaction();

        for (DataSetRegistrationAlgorithm registrationAlgorithm : dataSetRegistrations)
        {
            new DataSetRegistrationAlgorithmRunner(registrationAlgorithm).runAlgorithm();
        }
        globalCleanAfterwardsAction.execute();
    }

    /**
     * Abort any scheduled changes.
     */
    public void abort()
    {
        rollbackExtantTransaction();
        dataSetRegistrations.clear();
    }

    public void rollback(DataSetRegistrationAlgorithm algorithm, Throwable ex)
    {
        registrator.rollback(this, algorithm, ex);
    }

    public <T extends DataSetInformation> void rollbackTransaction(
            DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex)
    {
        registrator.rollbackTransaction(this, transaction, algorithm, ex);
    }

    /**
     * Create a storage algorithm for storing an individual data set. This is internally used by
     * transactions. Other clients may find it useful as well.
     */
    public <T extends DataSetInformation> DataSetStorageAlgorithm<T> createStorageAlgorithm(
            File dataSetFile, DataSetRegistrationDetails<T> dataSetDetails)
    {
        IDataStoreStrategy strategy =
                registratorState.getDataStrategyStore().getDataStoreStrategy(
                        dataSetDetails.getDataSetInformation(), dataSetFile);

        TopLevelDataSetRegistratorGlobalState globalContext = registratorState.getGlobalState();
        DataSetStorageAlgorithm<T> algorithm =
                new DataSetStorageAlgorithm<T>(dataSetFile, dataSetDetails, strategy,
                        registratorState.getStorageProcessor(),
                        globalContext.getDataSetValidator(), globalContext.getDssCode(),
                        registratorState.getFileOperations(), globalContext.getMailClient());
        return algorithm;
    }

    public <T extends DataSetInformation> IEntityRegistrationService<T> getEntityRegistrationService()
    {
        return new DefaultEntityRegistrationService<T>(registrator);
    }

    private DataSetRegistrationAlgorithm createRegistrationAlgorithm(File incomingDataSetFile,
            DataSetRegistrationDetails<?> details)
    {
        final TopLevelDataSetRegistratorGlobalState globalState = registratorState.getGlobalState();
        final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction =
                new IDelegatedActionWithResult<Boolean>()
                    {
                        public Boolean execute()
                        {
                            return true; // do nothing
                        }
                    };

        IDataStoreStrategy dataStoreStrategy =
                registratorState.getDataStrategyStore().getDataStoreStrategy(
                        details.getDataSetInformation(), incomingDataSetFile);

        DataSetRegistrationAlgorithmState state =
                new DataSetRegistrationAlgorithmState(incomingDataSetFile,
                        globalState.getOpenBisService(), cleanAfterwardsAction,
                        registratorState.getPreRegistrationAction(),
                        registratorState.getPostRegistrationAction(),
                        details.getDataSetInformation(), dataStoreStrategy, details,
                        registratorState.getStorageProcessor(),
                        registratorState.getFileOperations(), globalState.getDataSetValidator(),
                        globalState.getMailClient(), globalState.isDeleteUnidentified(),
                        registratorState.getRegistrationLock(), globalState.getDssCode(),
                        globalState.isNotifySuccessfulRegistration());
        return new DataSetRegistrationAlgorithm(state, this,
                new DefaultApplicationServerRegistrator(registrator,
                        details.getDataSetInformation()));
    }

    /**
     * If a transaction is hanging around, commit it
     */
    private void commitExtantTransaction()
    {
        if (null != liveTransactionOrNull
                && false == liveTransactionOrNull.isCommittedOrRolledback())
        {
            // Commit the existing transaction
            liveTransactionOrNull.commit();
        }
    }

    private void rollbackExtantTransaction()
    {
        if (null != liveTransactionOrNull)
        {
            liveTransactionOrNull.rollback();
        }
    }

    private static class DefaultApplicationServerRegistrator implements
            IDataSetInApplicationServerRegistrator
    {
        private final AbstractOmniscientTopLevelDataSetRegistrator registrator;

        private final DataSetInformation dataSetInformation;

        DefaultApplicationServerRegistrator(
                AbstractOmniscientTopLevelDataSetRegistrator registrator,
                DataSetInformation dataSetInformation)
        {
            this.dataSetInformation = dataSetInformation;
            this.registrator = registrator;
        }

        public void registerDataSetInApplicationServer(NewExternalData data) throws Throwable
        {
            registrator.registerDataSetInApplicationServer(dataSetInformation, data);
        }
    }

}
