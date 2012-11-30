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

package ch.systemsx.cisd.etlserver.registrator.v2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.DistinctExceptionsCollection;
import ch.systemsx.cisd.etlserver.registrator.IRollbackStack;
import ch.systemsx.cisd.etlserver.registrator.ITransactionalCommand;
import ch.systemsx.cisd.etlserver.registrator.IncomingFileDeletedBeforeRegistrationException;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.monitor.DssRegistrationHealthMonitor;
import ch.systemsx.cisd.etlserver.registrator.recovery.IDataSetStorageRecoveryManager;
import ch.systemsx.cisd.etlserver.registrator.v2.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

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

        public void markReadyForRecovery(DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex);
    }

    /**
     * Interface for code that is run to register a new data set.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IDataSetInApplicationServerRegistrator<T extends DataSetInformation>
    {
        public void registerDataSetsInApplicationServer(TechId registrationId,
                List<DataSetRegistrationInformation<T>> data) throws Throwable;

        public EntityOperationsState didEntityOperationsSucceeded(TechId registrationId);
    }

    public static interface IPrePostRegistrationHook<T extends DataSetInformation>
    {
        public void executePreRegistration(
                DataSetRegistrationContext.IHolder registrationContextHolder);

        public void executePostRegistration(
                DataSetRegistrationContext.IHolder registrationContextHolder);
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

    private final DataSetRegistrationContext.IHolder registrationContextHolder;

    private final IPrePostRegistrationHook<T> postPreRegistrationHooks;

    private final DssRegistrationLogger dssRegistrationLog;

    private final IEncapsulatedOpenBISService openBISService;

    private final IDataSetStorageRecoveryManager storageRecoveryManager;

    private final DataSetFile incomingDataSetFile;

    private final int registrationMaxRetryCount;

    private final int registrationRetryPauseInSec;

    public DataSetStorageAlgorithmRunner(List<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms,
            DataSetRegistrationTransaction<T> transaction, IRollbackStack rollbackStack,
            DssRegistrationLogger dssRegistrationLog, IEncapsulatedOpenBISService openBISService,
            IPrePostRegistrationHook<T> postPreRegistrationHooks,
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        this.dataSetStorageAlgorithms =
                new ArrayList<DataSetStorageAlgorithm<T>>(dataSetStorageAlgorithms);
        this.rollbackDelegate = transaction;
        this.applicationServerRegistrator = transaction;
        this.registrationContextHolder = transaction;
        this.rollbackStack = rollbackStack;
        this.dssRegistrationLog = dssRegistrationLog;
        this.openBISService = openBISService;
        this.postPreRegistrationHooks = postPreRegistrationHooks;
        this.storageRecoveryManager = transaction.getStorageRecoveryManager();
        this.incomingDataSetFile = transaction.getIncomingDataSetFile();

        this.registrationMaxRetryCount =
                globalState.getThreadParameters().getDataSetRegistrationMaxRetryCount();
        this.registrationRetryPauseInSec =
                globalState.getThreadParameters().getDataSetRegistrationPauseInSec();
    }

    /**
     * Constructor used by the autorecovery infrastructure.
     */
    public DataSetStorageAlgorithmRunner(DataSetFile incomingDataSetFile,
            List<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms,
            IRollbackDelegate<T> rollbackDelegate, IRollbackStack rollbackStack,
            DssRegistrationLogger dssRegistrationLog, IEncapsulatedOpenBISService openBISService,
            IPrePostRegistrationHook<T> postPreRegistrationHooks,
            IDataSetStorageRecoveryManager storageRecoveryManager,
            DataSetRegistrationContext.IHolder registrationContextHolder,
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        this.dataSetStorageAlgorithms =
                new ArrayList<DataSetStorageAlgorithm<T>>(dataSetStorageAlgorithms);
        this.rollbackDelegate = rollbackDelegate;
        this.applicationServerRegistrator = null;
        this.registrationContextHolder = registrationContextHolder;
        this.rollbackStack = rollbackStack;
        this.dssRegistrationLog = dssRegistrationLog;
        this.openBISService = openBISService;
        this.postPreRegistrationHooks = postPreRegistrationHooks;
        this.storageRecoveryManager = storageRecoveryManager;
        this.incomingDataSetFile = incomingDataSetFile;

        this.registrationMaxRetryCount =
                globalState.getThreadParameters().getDataSetRegistrationMaxRetryCount();
        this.registrationRetryPauseInSec =
                globalState.getThreadParameters().getDataSetRegistrationPauseInSec();
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

        @Override
        public void execute()
        {

        }

        @Override
        public void rollback()
        {
            transaction.rollback(null);
        }

    }

    private boolean confirmStorageInApplicationServer()
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
            rollbackDelegate.markReadyForRecovery(this, ex);
            operationLog.error("Error during storage confirmation", ex);
            dssRegistrationLog.log(ex, "Error during storage confirmation");
            return false;
            // There is nothing we can do without recovery
        }
        return true;
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

    private boolean executePreRegistrationHooks()
    {
        try
        {
            postPreRegistrationHooks.executePreRegistration(registrationContextHolder);
        } catch (Throwable throwable)
        {
            operationLog.error("Error in execution of pre registration hooks", throwable);
            dssRegistrationLog.log(throwable, "Error in execution of pre registration hooks");

            rollbackDuringPreRegistration(throwable);
            return false;
        }
        return true;
    }

    /**
     * @returns true if some datasets have been registered
     */
    public boolean prepareAndRunStorageAlgorithms()
    {
        prepare();
        // all algorithms are now in
        // PREPARED STATE

        if (preCommitStorageAlgorithms() == false)
        {
            return false;
        }

        if (executePreRegistrationHooks() == false)
        {
            return false;
        }

        ArrayList<DataSetRegistrationInformation<T>> registrationData =
                tryPrepareRegistrationData();

        TechId registrationId = new TechId(openBISService.drawANewUniqueID());
        logMetadataRegistration(registrationId);

        if (registrationData == null)
        {
            return false;
        }

        // PRECOMMITED STATE
        storageRecoveryManager.checkpointPrecommittedState(registrationId, this);

        waitUntilApplicationIsReady();

        if (registerDataSetsInApplicationServer(registrationId, registrationData) == false)
        {
            return false;
        }

        postRegistration();

        if (false == commitAndStore())
        {
            return false;
        }

        return cleanPrecommitAndConfirmStorage();

        // confirm storage in AS

        // STORAGECONFIRMED
    }

    public void postRegistration()
    {
        executeJythonScriptsForPostRegistration();

        storageRecoveryManager.checkpointPrecommittedStateAfterPostRegistrationHook(this);

        waitUntilApplicationIsReady();

    }

    private void logMetadataRegistration(TechId registrationId)
    {
        dssRegistrationLog.log("About to register metadata with AS: registrationId("
                + registrationId.toString() + ")");
    }

    public boolean commitAndStore()
    {
        if (commitStorageProcessors() == false)
        {
            return false;
        }

        // COMMITED

        if (storeCommitedDatasets() == false)
        {
            return false;
        }

        storageRecoveryManager.checkpointStoredStateBeforeStorageConfirmation(this);

        waitUntilApplicationIsReady();

        return true;
    }

    private void waitUntilApplicationIsReady()
    {
        while (false == DssRegistrationHealthMonitor.getInstance().isApplicationReady(
                incomingDataSetFile.getRealIncomingFile().getParentFile()))
        {
            waitTheRetryPeriod();
            // do nothing. just repeat until the application is ready
        }
    }

    /**
     * Execute the post-registration part of the storage process
     */
    public boolean cleanPrecommitAndConfirmStorage()
    {

        cleanPrecommitDirectory();

        boolean confirmStorageSucceeded = confirmStorageInApplicationServer();

        if (!confirmStorageSucceeded)
        {
            return false;
        }

        storageRecoveryManager.registrationCompleted(this);

        return true;
    }

    private ArrayList<DataSetRegistrationInformation<T>> tryPrepareRegistrationData()
    {
        try
        {
            // registers data set with yet non-existing store path.
            // Runs or throw a throwable
            ArrayList<DataSetRegistrationInformation<T>> registrationData =
                    new ArrayList<DataSetRegistrationInformation<T>>();
            for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
            {
                registrationData.add(new DataSetRegistrationInformation<T>(storageAlgorithm
                        .getDataSetInformation(), storageAlgorithm.createExternalData()));
            }
            return registrationData;
        } catch (Throwable t)
        {
            rollbackDuringMetadataRegistration(t);
            return null;
        }
    }

    private void rollbackDuringStorageProcessorRun(Throwable ex)
    {
        operationLog.error("Failed to run storage processor");
        rollbackStorageProcessors(ex);
        rollbackDelegate.didRollbackStorageAlgorithmRunner(this, ex,
                ErrorType.STORAGE_PROCESSOR_ERROR);
    }

    private void rollbackDuringPreRegistration(Throwable ex)
    {
        operationLog.error("Failed to pre-register", ex);
        rollbackStorageProcessors(ex);
        rollbackDelegate.didRollbackStorageAlgorithmRunner(this, ex,
                ErrorType.PRE_REGISTRATION_ERROR);
    }

    private void rollbackDuringMetadataRegistration(Throwable ex)
    {
        if (false == ex instanceof IncomingFileDeletedBeforeRegistrationException)
        {
            // Don't log if the file was deleted before registration, we already know.
            operationLog.error("Failed to register metadata", ex);
        }
        rollbackStorageProcessors(ex);
        rollbackDelegate.didRollbackStorageAlgorithmRunner(this, ex,
                ErrorType.OPENBIS_REGISTRATION_FAILURE);
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
            rollbackDelegate.markReadyForRecovery(this, throwable);

            dssRegistrationLog.log(throwable, "Error while storing committed datasets.");
            // Something has gone really wrong
            operationLog.error("Error while storing committed datasets", throwable);
            return false;
        }
        return true;
    }

    /**
     * Stored => Stored. Idempotent operation of cleanup. Can fail.
     */
    private void cleanPrecommitDirectory()
    {
        try
        {
            for (DataSetStorageAlgorithm<T> storageAlgorithm : dataSetStorageAlgorithms)
            {
                storageAlgorithm.cleanPrecommitDirectory();
            }
        } catch (final Throwable throwable)
        {
            // failed to delete precommit directory? oh well...
            operationLog.warn("Failed to delete precommit directory", throwable);
        }
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
            dssRegistrationLog.log(throwable, "Error in commit of storage processors.");
            // Something has gone really wrong
            operationLog.error("Error while committing storage processors", throwable);

            rollbackDelegate.markReadyForRecovery(this, throwable);
            return false;
        }
        return true;
    }

    private boolean registerDataSetsInApplicationServer(TechId registrationId,
            List<DataSetRegistrationInformation<T>> registrationData)
    {
        boolean result = registerDataWithRecovery(registrationId, registrationData);
        if (result)
        {
            dssRegistrationLog.log("Data has been registered with the openBIS Application Server.");
        }
        return result;
    }

    private boolean registerDataWithRecovery(TechId registrationId,
            List<DataSetRegistrationInformation<T>> registrationData)
    {
        DistinctExceptionsCollection exceptionCollection = new DistinctExceptionsCollection();

        EntityOperationsState result = EntityOperationsState.NO_OPERATION;

        Throwable problem = null;
        int errorCount = 0;

        while (true)
        {
            waitUntilApplicationIsReady();

            if (result == EntityOperationsState.NO_OPERATION)
            {
                try
                {
                    applicationServerRegistrator.registerDataSetsInApplicationServer(
                            registrationId, registrationData);
                    return true;
                } catch (IncomingFileDeletedBeforeRegistrationException e)
                {
                    operationLog
                            .warn("The incoming file was deleted before registration. Nothing was registered in openBIS.");
                    dssRegistrationLog
                            .log("The incoming file was deleted before registration. Nothing was registered in openBIS.");
                    rollbackDuringMetadataRegistration(e);
                    return false;
                } catch (final Throwable exception)
                {
                    operationLog.error("Error in registrating data in application server",
                            exception);
                    dssRegistrationLog.log("Error in registrating data in application server");

                    problem = exception;
                }

                // how many times has this error already happened?
                errorCount = exceptionCollection.add(problem);

                if (!storageRecoveryManager.canRecoverFromError(problem))
                {
                    rollbackDuringMetadataRegistration(problem);
                    return false;
                }
            }
            operationLog.debug("Will check the status of registration");

            // check in openbis.registration succeeded
            result = checkOperationsSucceededNoGiveUp(registrationId);

            operationLog.debug("The registration is in state: " + result);

            switch (result)
            {
                case IN_PROGRESS:
                    operationLog
                            .debug("The registration is in progress. Will wait until it's done.");

                    waitTheRetryPeriod();
                    break;

                case NO_OPERATION:
                    if (errorCount > registrationMaxRetryCount)
                    {
                        operationLog.debug("The same error happened " + errorCount
                                + " times. Will stop registration.");
                        dssRegistrationLog.log("The same error happened " + errorCount
                                + " times. Will stop registration.");

                        rollbackDelegate.markReadyForRecovery(this, problem);
                        return false;
                    } else
                    {
                        operationLog.debug("The same error happened for the " + errorCount
                                + " time. Will continue retrying after "
                                + registrationRetryPauseInSec + " seconds");
                    }
                    waitTheRetryPeriod();
                    break;

                case OPERATION_SUCCEEDED:
                    operationLog
                            .debug("The registration is in progress. Will wait until it's done.");

                    // the operation has succeeded so we return
                    return true;
            }

        }
    }

    /**
     * Checks if the operations have succeeded in AS. Never give up if can't connect.
     */
    private EntityOperationsState checkOperationsSucceededNoGiveUp(TechId registrationId)
    {
        while (true)
        {
            try
            {
                EntityOperationsState result =
                        applicationServerRegistrator.didEntityOperationsSucceeded(registrationId);
                return result;
            } catch (Exception exception)
            {
                operationLog
                        .debug("Error in checking status of registration. Probably AS is down. Will wait.",
                                exception);
                waitTheRetryPeriod();
            }
        }
    }

    private void waitTheRetryPeriod()
    {
        ConcurrencyUtilities.sleep(registrationRetryPauseInSec * 1000);
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

    private void executeJythonScriptsForPostRegistration()
    {
        try
        {
            postPreRegistrationHooks.executePostRegistration(registrationContextHolder);
        } catch (final Throwable throwable)
        {
            dssRegistrationLog.log("Post-registration action failed:");
            dssRegistrationLog.log(throwable.toString());

            operationLog.warn("Post-registration action failed", throwable);
        }
    }

    private void rollbackStorageProcessors(Throwable ex)
    {
        if (false == ex instanceof IncomingFileDeletedBeforeRegistrationException)
        {
            operationLog.error(
                    "Error during dataset registration: " + ExceptionUtils.getRootCauseMessage(ex),
                    ex);
        }

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

    public List<DataSetStorageAlgorithm<T>> getDataSetStorageAlgorithms()
    {
        return dataSetStorageAlgorithms;
    }

    public IRollbackStack getRollbackStack()
    {
        return rollbackStack;
    }

    public DssRegistrationLogger getDssRegistrationLogger()
    {
        return dssRegistrationLog;
    }

    private Logger getOperationLog()
    {
        return operationLog;
    }

    public DataSetFile getIncomingDataSetFile()
    {
        return incomingDataSetFile;
    }

    public DataSetRegistrationContext getRegistrationContext()
    {
        return registrationContextHolder.getRegistrationContext();
    }
}
