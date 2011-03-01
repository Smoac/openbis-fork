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
import ch.systemsx.cisd.etlserver.BaseDirectoryHolder;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.DataSetRegistrationAlgorithmState;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithmRunner;
import ch.systemsx.cisd.etlserver.FileRenamer;
import ch.systemsx.cisd.etlserver.IDataStoreStrategy;
import ch.systemsx.cisd.etlserver.IdentifiedDataStrategy;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.TransferredDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * A service that registers many files as individual data sets in one transaction.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationService<T extends DataSetInformation> implements
        DataSetRegistrationAlgorithm.IRollbackDelegate, IDataSetRegistrationService
{
    static final String STAGING_DIR = "staging-dir";

    private final AbstractOmniscientTopLevelDataSetRegistrator<T> registrator;

    private final OmniscientTopLevelDataSetRegistratorState registratorContext;

    private final IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction;

    private final ArrayList<DataSetRegistrationAlgorithm> dataSetRegistrations =
            new ArrayList<DataSetRegistrationAlgorithm>();

    private final IDataSetRegistrationDetailsFactory<T> dataSetRegistrationDetailsFactory;

    private final File stagingDirectory;

    private final File incomingDataSetFile;

    /**
     * All transactions ever created on this service.
     */
    private final ArrayList<DataSetRegistrationTransaction<T>> transactions;

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
    public DataSetRegistrationService(AbstractOmniscientTopLevelDataSetRegistrator<T> registrator,
            File incomingDataSetFile,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory,
            IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction)
    {
        this.registrator = registrator;
        this.registratorContext = registrator.getRegistratorState();
        this.incomingDataSetFile = incomingDataSetFile;
        this.globalCleanAfterwardsAction = globalCleanAfterwardsAction;
        this.dataSetRegistrationDetailsFactory = registrationDetailsFactory;

        Properties properties =
                registratorContext.getGlobalState().getThreadParameters().getThreadProperties();
        String stagingDirString = PropertyUtils.getProperty(properties, STAGING_DIR);
        if (null == stagingDirString)
        {
            stagingDirectory = registratorContext.getGlobalState().getStoreRootDir();
        } else
        {
            stagingDirectory = new File(stagingDirString);
        }

        transactions = new ArrayList<DataSetRegistrationTransaction<T>>();
    }

    public OmniscientTopLevelDataSetRegistratorState getRegistratorContext()
    {
        return registratorContext;
    }

    /**
     * Queue registration a data set and return a future for the data set that will be created.
     */
    public FutureDataSet queueDataSetRegistration(File dataSetFile,
            final DataSetRegistrationDetails<T> details)
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
    public IDataSetRegistrationTransaction transaction()
    {
        return transaction(incomingDataSetFile, getDataSetRegistrationDetailsFactory());
    }

    /**
     * Create a new transaction that atomically performs file operations and registers entities.
     */
    public IDataSetRegistrationTransaction transaction(File dataSetFile)
    {
        return transaction(dataSetFile, getDataSetRegistrationDetailsFactory());
    }

    /**
     * Create a new transaction that atomically performs file operations and registers entities.
     */
    public DataSetRegistrationTransaction<T> transaction(File dataSetFile,
            IDataSetRegistrationDetailsFactory<T> detailsFactory)
    {
        File workingDirectory = dataSetFile.getParentFile();

        // Clone this service for the transaction to keep them independent
        DataSetRegistrationTransaction<T> transaction =
                new DataSetRegistrationTransaction<T>(registrator.getGlobalState()
                        .getStoreRootDir(), workingDirectory, stagingDirectory, this,
                        detailsFactory);

        transactions.add(transaction);
        return transaction;
    }

    /**
     * Commit any scheduled changes.
     */
    public void commit()
    {
        // If a transaction is hanging around, commit it
        commitExtantTransactions();

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
        rollbackExtantTransactions();
        dataSetRegistrations.clear();
    }

    public File moveIncomingToError(String dataSetTypeCodeOrNull)
    {
        // Make sure the data set information is valid
        DataSetInformation dataSetInfo = new DataSetInformation();
        dataSetInfo.setShareId(registratorContext.getGlobalState().getShareId());
        if (null == dataSetTypeCodeOrNull)
        {
            dataSetInfo.setDataSetType(new DataSetType(DataSetTypeCode.UNKNOWN.getCode()));
        } else
        {
            dataSetInfo.setDataSetType(new DataSetType(dataSetTypeCodeOrNull));
        }

        // Create the error directory
        File baseDirectory =
                DataSetStorageAlgorithm.createBaseDirectory(
                        TransferredDataSetHandler.ERROR_DATA_STRATEGY, registratorContext
                                .getStorageProcessor().getStoreRootDirectory(), registratorContext
                                .getFileOperations(), dataSetInfo, dataSetInfo.getDataSetType(),
                        incomingDataSetFile);
        BaseDirectoryHolder baseDirectoryHolder =
                new BaseDirectoryHolder(TransferredDataSetHandler.ERROR_DATA_STRATEGY,
                        baseDirectory, incomingDataSetFile);

        // Move the incoming there
        FileRenamer.renameAndLog(incomingDataSetFile, baseDirectoryHolder.getTargetFile());
        return baseDirectoryHolder.getTargetFile();
    }

    public void rollbackTransaction(DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex)
    {
        registrator.rollbackTransaction(this, transaction, algorithm, ex);
    }

    /**
     * Create a storage algorithm for storing an individual data set. This is internally used by
     * transactions. Other clients may find it useful as well.
     */
    public DataSetStorageAlgorithm<T> createStorageAlgorithm(File dataSetFile,
            DataSetRegistrationDetails<T> dataSetDetails)
    {
        TopLevelDataSetRegistratorGlobalState globalContext = registratorContext.getGlobalState();
        T dataSetInformation = dataSetDetails.getDataSetInformation();
        dataSetInformation.setShareId(globalContext.getShareId());
        IDataStoreStrategy strategy =
                registratorContext.getDataStrategyStore().getDataStoreStrategy(dataSetInformation,
                        dataSetFile);

        DataSetStorageAlgorithm<T> algorithm =
                new DataSetStorageAlgorithm<T>(dataSetFile, dataSetDetails, strategy,
                        registratorContext.getStorageProcessor(),
                        globalContext.getDataSetValidator(), globalContext.getDssCode(),
                        registratorContext.getFileOperations(), globalContext.getMailClient());
        return algorithm;
    }

    /**
     * Create a storage algorithm for storing an individual data set, bypassing the detection of
     * whether the data set's owner is in the db. This is used if the owner will be registered in
     * the same transaction. This is internally used by transactions. Other clients may find it
     * useful as well.
     */
    public DataSetStorageAlgorithm<T> createStorageAlgorithmWithIdentifiedStrategy(
            File dataSetFile, DataSetRegistrationDetails<T> dataSetDetails)
    {
        TopLevelDataSetRegistratorGlobalState globalContext = registratorContext.getGlobalState();
        T dataSetInformation = dataSetDetails.getDataSetInformation();
        dataSetInformation.setShareId(globalContext.getShareId());
        IDataStoreStrategy strategy = new IdentifiedDataStrategy();

        DataSetStorageAlgorithm<T> algorithm =
                new DataSetStorageAlgorithm<T>(dataSetFile, dataSetDetails, strategy,
                        registratorContext.getStorageProcessor(),
                        globalContext.getDataSetValidator(), globalContext.getDssCode(),
                        registratorContext.getFileOperations(), globalContext.getMailClient());
        return algorithm;
    }

    public IEntityOperationService<T> getEntityRegistrationService()
    {
        return new DefaultEntityOperationService<T>(registrator);
    }

    protected IDataSetRegistrationDetailsFactory<T> getDataSetRegistrationDetailsFactory()
    {
        return dataSetRegistrationDetailsFactory;
    }

    /**
     * If a transaction is hanging around, commit it
     */
    private void commitExtantTransactions()
    {
        for (DataSetRegistrationTransaction<T> transaction : transactions)
        {
            if (false == transaction.isCommittedOrRolledback())
            {
                // Commit the existing transaction
                transaction.commit();
            }
        }
    }

    private void rollbackExtantTransactions()
    {
        for (DataSetRegistrationTransaction<T> transaction : transactions)
        {
            if (false == transaction.isCommittedOrRolledback())
            {
                // Rollback the existing transaction
                transaction.rollback();
            }
        }
    }

    private DataSetRegistrationAlgorithm createRegistrationAlgorithm(File aDataSetFile,
            DataSetRegistrationDetails<T> details)
    {
        final TopLevelDataSetRegistratorGlobalState globalState =
                registratorContext.getGlobalState();
        details.getDataSetInformation().setShareId(globalState.getShareId());
        final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction =
                new IDelegatedActionWithResult<Boolean>()
                    {
                        public Boolean execute()
                        {
                            return true; // do nothing
                        }
                    };

        IDataStoreStrategy dataStoreStrategy =
                registratorContext.getDataStrategyStore().getDataStoreStrategy(
                        details.getDataSetInformation(), aDataSetFile);

        DataSetRegistrationAlgorithmState state =
                new DataSetRegistrationAlgorithmState(aDataSetFile,
                        globalState.getOpenBisService(), cleanAfterwardsAction,
                        registratorContext.getPreRegistrationAction(),
                        registratorContext.getPostRegistrationAction(),
                        details.getDataSetInformation(), dataStoreStrategy, details,
                        registratorContext.getStorageProcessor(),
                        registratorContext.getFileOperations(), globalState.getDataSetValidator(),
                        globalState.getMailClient(), globalState.isDeleteUnidentified(),
                        registratorContext.getRegistrationLock(), globalState.getDssCode(),
                        globalState.isNotifySuccessfulRegistration());
        return new DataSetRegistrationAlgorithm(state, this,
                new DefaultApplicationServerRegistrator(registrator,
                        details.getDataSetInformation()));
    }

    private static class DefaultApplicationServerRegistrator implements
            DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator
    {
        private final AbstractOmniscientTopLevelDataSetRegistrator<?> registrator;

        private final DataSetInformation dataSetInformation;

        DefaultApplicationServerRegistrator(
                AbstractOmniscientTopLevelDataSetRegistrator<?> registrator,
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

    public void rollback(DataSetRegistrationAlgorithm algorithm, Throwable ex)
    {
        registrator.rollback(this, algorithm, ex);
    }

    protected AbstractOmniscientTopLevelDataSetRegistrator<T> getRegistrator()
    {
        return registrator;
    }
}
