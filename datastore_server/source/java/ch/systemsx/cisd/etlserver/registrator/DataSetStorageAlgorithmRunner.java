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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.registrator.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;

/**
 * An algorithm that implements the logic running many data set storage algorithms in one logical
 * transaction.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetStorageAlgorithmRunner<T extends DataSetInformation>
{

    public static interface IRollbackDelegate<T extends DataSetInformation>
    {
        /**
         * @param algorithm The algorithm that is rolling back
         * @param ex The throwable that forced the rollback
         * @param errorType The point in the execution of the algorithm that rollback happened
         */
        public void didRollbackStorageAlgorithmRunner(DataSetStorageAlgorithmRunner<T> algorithm,
                Throwable ex, ErrorType errorType);
    }

    /**
     * Interface for code that is run to register a new data set.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IDataSetInApplicationServerRegistrator<T extends DataSetInformation>
    {
        public void registerDataSetsInApplicationServer(List<DataSetRegistrationInformation<T>> data)
                throws Throwable;
    }

    public static interface IPrePostRegistrationHook<T extends DataSetInformation>
    {
        public void executePreRegistration(DataSetRegistrationTransaction<T> transaction);

        public void executePostRegistration(DataSetRegistrationTransaction<T> transaction);
    }

    static private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetStorageAlgorithmRunner.class);

    public static final String DATA_SET_REGISTRATION_FAILURE_TEMPLATE =
            "Registration of data set '%s' failed.";

    public static final String DATA_SET_STORAGE_FAILURE_TEMPLATE = "Storing data set '%s' failed.";

    public static final String SUCCESSFULLY_REGISTERED = "Successfully registered data set: [";

    private final ArrayList<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms;

    private final IDataSetInApplicationServerRegistrator<T> applicationServerRegistrator;

    private final IRollbackDelegate<T> rollbackDelegate;

    private final IRollbackStack rollbackStack;

    private final DataSetRegistrationTransaction<T> transaction;

    private final IPrePostRegistrationHook<T> postPreRegistrationHooks;

    private final DssRegistrationLogger dssRegistrationLog;

    private final IEncapsulatedOpenBISService openBISService;

    // this is unused because it is only here as the prerequisite for the auto-recovery
    private final AutoRecoverySettings shouldUseAutomaticRecovery;

    private final IDataSetStorageRecoveryManager storageRecoveryManager;

    public DataSetStorageAlgorithmRunner(List<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms,
            DataSetRegistrationTransaction<T> transaction, IRollbackStack rollbackStack,
            DssRegistrationLogger dssRegistrationLog, IEncapsulatedOpenBISService openBISService,
            IPrePostRegistrationHook<T> postPreRegistrationHooks)
    {
        this.dataSetStorageAlgorithms =
                new ArrayList<DataSetStorageAlgorithm<T>>(dataSetStorageAlgorithms);
        this.rollbackDelegate = transaction;
        this.applicationServerRegistrator = transaction;
        this.transaction = transaction;
        this.rollbackStack = rollbackStack;
        this.dssRegistrationLog = dssRegistrationLog;
        this.openBISService = openBISService;
        this.postPreRegistrationHooks = postPreRegistrationHooks;
        this.shouldUseAutomaticRecovery = transaction.getAutoRecoverySettings();
        this.storageRecoveryManager = transaction.getStorageRecoveryManager();
    }

    /**
     * Prepare registration of a data set.
     */
    public final void prepare()
    {
        // Log information about the prepare
        StringBuilder registrationSummary = new StringBuilder();
        registrationSummary.append("Prepared registration of ");
        registrationSummary.append(dataSetStorageAlgorithms.size());
        if (1 == dataSetStorageAlgorithms.size())
        {
            registrationSummary.append(" data set:");
        } else
        {
            registrationSummary.append(" data sets:");
        }
        registrationSummary.append("\n\t");

        // Do the prepare
        for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
        {
            IStorageProcessorTransaction transaction1 = storageAlgorithm.prepare(rollbackStack);
            ITransactionalCommand command = new StorageProcessorTransactionCommand(transaction1);
            rollbackStack.pushAndExecuteCommand(command);

            // Collect logging information
            registrationSummary.append(storageAlgorithm.getDataSetInformation().getDataSetCode());
            registrationSummary.append(",");
        }

        registrationSummary.deleteCharAt(registrationSummary.length() - 1);

        dssRegistrationLog.logTruncatingIfNecessary(registrationSummary.toString());
    }

    /**
     * This object will live in the persistent stack of the transaction. In case the server process
     * is killed in the middle of a transaction, the {@link #rollback()} will attempt to rollback
     * the storage processor transaction after restart.
     */
    public static class StorageProcessorTransactionCommand implements ITransactionalCommand
    {

        private static final long serialVersionUID = 1L;

        final IStorageProcessorTransaction transaction;

        StorageProcessorTransactionCommand(IStorageProcessorTransaction transaction)
        {
            this.transaction = transaction;
        }

        public void execute()
        {

        }

        public void rollback()
        {
            transaction.rollback(null);
        }

    }

    private void confirmStorageInApplicationServer()
    {
        try
        {
            for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
            {
                String dataSetCode = storageAlgorithm.getDataSetInformation().getDataSetCode();
                openBISService.setStorageConfirmed(dataSetCode);
            }
            dssRegistrationLog.log("Storage has been confirmed in openBIS Application Server.");
        } catch (final Exception ex)
        {
            // as this case doesn't allow rollbacking, we don't have to catch aggresively
            // (throwables).
            // There is nothing we can do about this at the moment,
            // Graceful recovery should (and will) take care of this case

        }
    }

    private void logPreCommitMessage()
    {
        // Use the precommit folder to create an informative message
        if (dataSetStorageAlgorithms.size() > 0)
        {
            DataSetStorageAlgorithm<T> anAlgorithm = dataSetStorageAlgorithms.get(0);
            File precommitDirectory = anAlgorithm.getPreCommitDirectory();
            dssRegistrationLog.log("Data has been moved to the pre-commit directory: "
                    + precommitDirectory.getAbsolutePath());
        } else
        {
            dssRegistrationLog.log("In pre-commit state; no data needed to be moved.");
        }
    }

    /**
     * @returns true if some datasets have been registered
     */
    public <T extends DataSetInformation> boolean prepareAndRunStorageAlgorithms()
    {
        prepare();
        // all algorithms are now in
        // PREPARED STATE

        if (preCommitStorageAlgorithms() == false)
        {
            return false;
        }
        // PRECOMMITED STATE
        storageRecoveryManager.checkpointPrecomittedState();
        if (registerDataSetsInApplicationServer() == false)
        {
            return false;
        }

        executePostRegistrationHooks();

        if (commitStorageProcessors() == false)
        {
            return false;
        }

        // COMMITED

        if (storeCommitedDatasets() == false)
        {
            return false;
        }

        // move files to the store

        confirmStorageInApplicationServer();

        storageRecoveryManager.registrationCompleted();

        return !dataSetStorageAlgorithms.isEmpty();

        // confirm storage in AS

        // STORAGECONFIRMED
    }

    private void rollbackDuringStorageProcessorRun(Throwable ex)
    {
        operationLog.error("Failed to run storage processor");
        rollbackStorageProcessors(ex);
        rollbackDelegate.didRollbackStorageAlgorithmRunner(this, ex,
                ErrorType.STORAGE_PROCESSOR_ERROR);
    }

    private void rollbackDuringMetadataRegistration(Throwable ex)
    {
        operationLog.error("Failed to register metadata", ex);
        rollbackStorageProcessors(ex);
        rollbackDelegate.didRollbackStorageAlgorithmRunner(this, ex,
                ErrorType.OPENBIS_REGISTRATION_FAILURE);
    }

    private void rollbackAfterStorageProcessorAndMetadataRegistration(Throwable ex)
    {
        operationLog.error("Failed to complete transaction", ex);
        rollbackStorageProcessors(ex);
        rollbackDelegate.didRollbackStorageAlgorithmRunner(this, ex,
                ErrorType.POST_REGISTRATION_ERROR);
    }

    /**
     * Committed => Stored
     */
    private boolean storeCommitedDatasets()
    {

        try
        {
            for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
            {
                storageAlgorithm.moveToTheStore();
            }
            logSuccessfulRegistration();
            dssRegistrationLog.log("Data has been moved to the final store.");
        } catch (final Throwable throwable)
        {
            // Something has gone really wrong
            return false;
        }
        return true;
    }

    /**
     * Precommitted => Committed
     */
    private boolean commitStorageProcessors()
    {
        try
        {
            // Should always succeed
            for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
            {
                storageAlgorithm.commitStorageProcessor();
            }
            dssRegistrationLog.log("Storage processors have committed.");

        } catch (final Throwable throwable)
        {
            // Something has gone really wrong
            rollbackAfterStorageProcessorAndMetadataRegistration(throwable);
            return false;
        }
        return true;
    }

    private boolean registerDataSetsInApplicationServer()
    {
        try
        {
            postPreRegistrationHooks.executePreRegistration(transaction);

            // registers data set with yet non-existing store path.
            // Runs or throw a throwable
            ArrayList<DataSetRegistrationInformation<T>> registrationData =
                    new ArrayList<DataSetRegistrationInformation<T>>();
            for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
            {
                registrationData.add(new DataSetRegistrationInformation<T>(storageAlgorithm
                        .getDataSetInformation(), storageAlgorithm.createExternalData()));

            }
            applicationServerRegistrator.registerDataSetsInApplicationServer(registrationData);

        } catch (final Throwable throwable)
        {
            rollbackDuringMetadataRegistration(throwable);
            return false;
        }

        dssRegistrationLog.log("Data has been registered with the openBIS Application Server.");
        return true;
    }

    /**
     * Prepared => Precommit
     */
    private boolean preCommitStorageAlgorithms()
    {
        try
        {
            // move data to precommited directory

            // Runs or throws a throwable
            for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
            {
                storageAlgorithm.preCommit();
            }
        } catch (final Throwable throwable)
        {
            rollbackDuringStorageProcessorRun(throwable);
            return false;
        }

        logPreCommitMessage();

        return true;
    }

    private void executePostRegistrationHooks()
    {
        try
        {
            postPreRegistrationHooks.executePostRegistration(transaction);
        } catch (final Throwable throwable)
        {
            dssRegistrationLog.log("Post-registration action failed:");
            dssRegistrationLog.log(throwable.toString());

            operationLog.warn("Post-registration action failed", throwable);
        }
    }

    private void rollbackStorageProcessors(Throwable ex)
    {
        operationLog.error(
                "Error during dataset registration: " + ExceptionUtils.getRootCauseMessage(ex), ex);

        // Errors which are not AssertionErrors leave the system in a state that we don't
        // know and can't trust. Thus we will not perform any operations any more in this
        // case.
        if (ex instanceof Error && ex instanceof AssertionError == false)
        {
            return;
        }

        // Rollback in the reverse order
        for (int i = dataSetStorageAlgorithms.size() - 1; i >= 0; --i)
        {
            DataSetStorageAlgorithm<T> storageAlgorithm = dataSetStorageAlgorithms.get(i);
            storageAlgorithm.transitionToRolledbackState(ex);
            storageAlgorithm.transitionToUndoneState();
        }
    }

    private void logSuccessfulRegistration()
    {
        if (getOperationLog().isInfoEnabled())
        {
            String msg = getSuccessRegistrationMessage();
            getOperationLog().info(msg);
        }
    }

    private final String getSuccessRegistrationMessage()
    {
        final StringBuilder buffer = new StringBuilder();

        for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
        {
            buffer.append(SUCCESSFULLY_REGISTERED);
            buffer.append(storageAlgorithm.getSuccessRegistrationMessage());
            buffer.append(']');
        }
        return buffer.toString();
    }

    private Logger getOperationLog()
    {
        return operationLog;
    }
}
