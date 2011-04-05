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
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.BaseDirectoryHolder;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithmRunner;
import ch.systemsx.cisd.etlserver.FileRenamer;
import ch.systemsx.cisd.etlserver.IDataStoreStrategy;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.IdentifiedDataStrategy;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.TransferredDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * A service that registers many files as individual data sets in one transaction.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationService<T extends DataSetInformation> implements
        IDataSetRegistrationService
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

    private final ITopLevelDataSetRegistratorDelegate delegate;

    /**
     * Keep track of errors we encounter while processing. Clients may want this information.
     */
    private final ArrayList<Throwable> encounteredErrors = new ArrayList<Throwable>();

    /**
     * All transactions ever created on this service.
     */
    private final ArrayList<DataSetRegistrationTransaction<T>> transactions;

    /**
     * Create a new DataSetRegistrationService.
     * 
     * @param registrator The top level data set registrator
     * @param globalCleanAfterwardsAction An action to execute when the service has finished
     */
    public DataSetRegistrationService(AbstractOmniscientTopLevelDataSetRegistrator<T> registrator,
            File incomingDataSetFile,
            IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory,
            IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        this.registrator = registrator;
        this.registratorContext = registrator.getRegistratorState();
        this.incomingDataSetFile = incomingDataSetFile;
        this.globalCleanAfterwardsAction = globalCleanAfterwardsAction;
        this.dataSetRegistrationDetailsFactory = registrationDetailsFactory;
        this.delegate = delegate;

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
        encounteredErrors.add(ex);
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
        return new DefaultEntityOperationService<T>(registrator, delegate);
    }

    /**
     * Return true if errors happend while processing the service.
     */
    public boolean didErrorsArise()
    {
        return encounteredErrors.isEmpty() == false;
    }

    /**
     * Return the list of errors that were encountered. If didErrorsArise is false, this list will
     * be empty, otherwise there will be at least one element.
     */
    public List<Throwable> getEncounteredErrors()
    {
        return encounteredErrors;
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

    protected AbstractOmniscientTopLevelDataSetRegistrator<T> getRegistrator()
    {
        return registrator;
    }
}
