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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.interpreter.PythonInterpreter;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPersistentMap;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner.IPrePostRegistrationHook;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner.IRollbackDelegate;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageRollbacker;
import ch.systemsx.cisd.etlserver.registrator.DistinctExceptionsCollection;
import ch.systemsx.cisd.etlserver.registrator.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.etlserver.registrator.MarkerFileUtility;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.RollbackStack;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.RollbackStack.IRollbackStackDelegate;
import ch.systemsx.cisd.etlserver.registrator.recovery.AbstractRecoveryState;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStoragePrecommitRecoveryState;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryInfo;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryInfo.RecoveryStage;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * A top-level data set handler that runs a python (jython) script to register data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetHandlerV2<T extends DataSetInformation> extends
        ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler<T>
{

    private final int processMaxRetryCount;

    private final int processRetryPauseInSec;

    /**
     * Constructor.
     * 
     * @param globalState
     */
    public JythonTopLevelDataSetHandlerV2(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
        this.processMaxRetryCount = globalState.getThreadParameters().getProcessMaxRetryCount();
        this.processRetryPauseInSec = globalState.getThreadParameters().getProcessRetryPauseInSec();
    }

    /**
     * Create a registration service that includes a python interpreter (we need the interpreter in
     * the service so we can use it in error handling).
     */
    @Override
    protected DataSetRegistrationService<T> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return createJythonDataSetRegistrationServiceV2(incomingDataSetFile,
                callerDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                PythonInterpreter.createIsolatedPythonInterpreter(), getGlobalState());
    }

    /**
     * Create a Jython registration service that includes access to the interpreter.
     * 
     * @param pythonInterpreter
     */
    protected DataSetRegistrationService<T> createJythonDataSetRegistrationServiceV2(
            DataSetFile incomingDataSetFile,
            DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate, PythonInterpreter pythonInterpreter,
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        return new JythonDataSetRegistrationServiceV2<T>(this, incomingDataSetFile,
                userProvidedDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                pythonInterpreter, globalState);
    }

    private void configureEvaluator(
            File dataSetFile,
            ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler.JythonDataSetRegistrationService<T> service,
            PythonInterpreter interpreter)
    {
        interpreter.set(INCOMING_DATA_SET_VARIABLE_NAME, dataSetFile);
        interpreter.set(STATE_VARIABLE_NAME, getGlobalState());

        if (service != null)
        {
            interpreter.set(FACTORY_VARIABLE_NAME, service.getDataSetRegistrationDetailsFactory());
        }
    }

    @Override
    protected void executeJythonScript(DataSetFile dataSetFile, String scriptString,
            JythonDataSetRegistrationService<T> service)
    {

        // Configure the evaluator
        PythonInterpreter interpreter = service.getInterpreter();

        // Invoke the evaluator
        interpreter.exec(scriptString);

        verifyEvaluatorHookFunctions(interpreter);

        PyFunction retryFunction = getShouldRetryProcessFunction(service);

        if (retryFunction == null)
        {

            // in case when there is no retry function defined we just call the process and don't
            // try to catch any kind of exceptions
            executeJythonProcessFunction(service.getInterpreter(), service.transaction());
        } else
        {
            executeJythonProcessFunctionWithRetries(interpreter,
                    (JythonDataSetRegistrationServiceV2<T>) service, retryFunction, dataSetFile);
        }
    }

    private void executeJythonProcessFunctionWithRetries(PythonInterpreter interpreter,
            JythonDataSetRegistrationServiceV2<T> service, PyFunction retryFunction,
            DataSetFile incomingDataSetFile)
    {
        DistinctExceptionsCollection errors = new DistinctExceptionsCollection();

        // create initial transaction
        service.transaction();

        while (true)
        {
            waitUntilApplicationIsReady(incomingDataSetFile);

            Exception problem;
            try
            {
                executeJythonProcessFunction(interpreter, service.getTransaction());
                // if function succeeded - than we are happy
                return;
            } catch (Exception ex)
            {
                problem = ex;
                operationLog
                        .info("Exception occured during jython script processing. Will check if can retry.",
                                ex);
            }

            int errorCount = errors.add(problem);

            if (errorCount > processMaxRetryCount)
            {
                operationLog
                        .error("The jython script processing has failed too many times. Rolling back.");
                throw CheckedExceptionTunnel.wrapIfNecessary(problem);
            } else
            {
                operationLog.debug("The same error happened for the " + errorCount
                        + " time (max allowed is " + processMaxRetryCount + ")");
            }

            DataSetRegistrationPersistentMap persistentMap =
                    service.getTransaction().getRegistrationContext();

            PyObject retryFunctionResult = null;
            try
            {
                retryFunctionResult = invokeFunction(retryFunction, persistentMap, problem);
            } catch (Exception ex)
            {
                operationLog.error("The retry function has failed. Rolling back.", ex);
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }

            if (retryFunctionResult == null)
            {
                operationLog
                        .error("The should_retry_processing function did not return anything. Will not retry.");
                throw CheckedExceptionTunnel.wrapIfNecessary(problem);
            }

            if (retryFunctionResult instanceof PyInteger == false)
            { // the python booleans are returned as PyIntegers
                operationLog
                        .error("The should_retry_processing function returned object of non-boolean type "
                                + retryFunctionResult.getClass() + ". Will not retry.");
                throw CheckedExceptionTunnel.wrapIfNecessary(problem);
            }

            if (((PyInteger) retryFunctionResult).asInt() == 0)
            {
                operationLog
                        .error("The should_retry_processing function returned false. Will not retry.");
                throw CheckedExceptionTunnel.wrapIfNecessary(problem);
            }

            service.rollbackAndForgetTransaction();
            // TODO: now the transaction is rolled back and everything should be in place again.
            // should we catch some exceptions here? can we recover if whatever went wrong in here

            // creates the new transaction and propagates the values in the persistent map
            service.transaction().getRegistrationContext().putAll(persistentMap);

            waitTheRetryPeriod(processRetryPauseInSec);
        }
    }

    protected void executeJythonProcessFunction(PythonInterpreter interpreter,
            IDataSetRegistrationTransaction transaction)
    {
        try
        {
            PyFunction function =
                    tryJythonFunction(interpreter, JythonHookFunction.PROCESS_FUNCTION);
            if (function == null)
            {
                throw new IllegalStateException("Undefined process() function");
            }
            IDataSetRegistrationTransactionV2 v2transaction = wrapTransaction(transaction);
            invokeFunction(function, v2transaction);
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    /**
     * Wraps the transaction - to hide methods which we don't want to expose in the api.
     */
    protected IDataSetRegistrationTransactionV2 wrapTransaction(
            IDataSetRegistrationTransaction transaction)
    {
        IDataSetRegistrationTransactionV2 v2transaction =
                new DataSetRegistrationTransactionV2Delegate(transaction);
        return v2transaction;
    }

    @Override
    protected boolean shouldUseOldJythonHookFunctions()
    {
        return false;
    }

    /**
     * V2 registration framework -- do not put files that are scheduled for recovery into the faulty
     * paths.
     */
    @Override
    public boolean shouldNotAddToFaultyPathsOrNull(File file)
    {
        // If there is a recovery marker file, do not add the file to faulty paths.
        return hasRecoveryMarkerFile(file);
    }

    @Override
    protected boolean hasRecoveryMarkerFile(File incoming)
    {
        return getGlobalState().getStorageRecoveryManager().getProcessingMarkerFile(incoming)
                .exists();
    }

    @Override
    protected void handleRecovery(final File incomingFileOriginal)
    {
        // get the marker file
        final File recoveryMarkerFile =
                state.getGlobalState().getStorageRecoveryManager()
                        .getProcessingMarkerFile(incomingFileOriginal);

        // deserialize recovery state
        final AbstractRecoveryState<T> recoveryState =
                state.getGlobalState().getStorageRecoveryManager()
                        .extractRecoveryCheckpoint(recoveryMarkerFile);

        // then we should ensure that the recovery will actually take place itself!
        final DataSetStorageRecoveryInfo recoveryInfo =
                state.getGlobalState().getStorageRecoveryManager()
                        .getRecoveryFileFromMarker(recoveryMarkerFile);

        final File recoveryFile = recoveryInfo.getRecoveryStateFile();

        if (false == recoveryFile.exists())
        {
            operationLog.error("Recovery file does not exist. " + recoveryFile);

            throw new IllegalStateException("Recovery file " + recoveryFile + " doesn't exist");
        }

        if (false == retryPeriodHasPassed(recoveryInfo))
        {
            return;
        }

        operationLog.info("Will recover from broken registration. Found marker file "
                + recoveryMarkerFile + " and " + recoveryFile);

        final DssRegistrationLogger logger = recoveryState.getRegistrationLogger(state);

        logger.log("Starting recovery at checkpoint " + recoveryInfo.getRecoveryStage());

        IRecoveryCleanupDelegate recoveryMarkerFileCleanupAction = new IRecoveryCleanupDelegate()
            {
                @Override
                public void execute(boolean shouldStopRecovery, boolean shouldIncreaseTryCount)
                {
                    if (false == shouldStopRecovery
                            && recoveryInfo.getTryCount() >= state.getGlobalState()
                                    .getStorageRecoveryManager().getMaximumRertyCount())
                    {
                        notificationLog.error("The dataset "
                                + recoveryState.getIncomingDataSetFile().getRealIncomingFile()
                                + " has failed to register. Giving up.");
                        deleteMarkerFile();

                        File errorRecoveryMarkerFile =
                                new File(recoveryMarkerFile.getParent(),
                                        recoveryMarkerFile.getName() + ".ERROR");
                        state.getFileOperations().move(recoveryMarkerFile, errorRecoveryMarkerFile);

                        logger.log("Recovery failed. Giving up.");
                        logger.registerFailure();

                    } else
                    {
                        if (shouldStopRecovery)
                        {
                            deleteMarkerFile();

                            recoveryMarkerFile.delete();
                            recoveryFile.delete();
                        } else
                        {
                            // this replaces the recovery file with a new one with increased
                            // count
                            // FIXME: is this safe operation (how to assure, that it won't
                            // corrupt the recoveryMarkerFile?)
                            DataSetStorageRecoveryInfo rInfo =
                                    state.getGlobalState().getStorageRecoveryManager()
                                            .getRecoveryFileFromMarker(recoveryMarkerFile);
                            if (shouldIncreaseTryCount)
                            {
                                rInfo.increaseTryCount();
                            }
                            rInfo.setLastTry(new Date());
                            rInfo.writeToFile(recoveryMarkerFile);
                        }
                    }
                }

                private void deleteMarkerFile()
                {
                    File incomingMarkerFile =
                            MarkerFileUtility.getMarkerFileFromIncoming(recoveryState
                                    .getIncomingDataSetFile().getRealIncomingFile());
                    if (incomingMarkerFile.exists())
                    {

                        incomingMarkerFile.delete();
                    }
                }
            };

        PostRegistrationCleanUpAction cleanupAction =
                new PostRegistrationCleanUpAction(recoveryState.getIncomingDataSetFile(),
                        new DoNothingDelegatedAction());

        handleRecoveryState(recoveryInfo.getRecoveryStage(), recoveryState, cleanupAction,
                recoveryMarkerFileCleanupAction);
    }

    interface IRecoveryCleanupDelegate
    {
        void execute(boolean shouldStopRecovery, boolean shouldIncreaseTryCount);
    }

    /**
     * Check wheter the last retry + retry period < date.now
     */
    private boolean retryPeriodHasPassed(final DataSetStorageRecoveryInfo recoveryInfo)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(recoveryInfo.getLastTry());
        c.add(Calendar.SECOND, state.getGlobalState().getStorageRecoveryManager()
                .getRetryPeriodInSeconds());
        return c.getTime().before(new Date());
    }

    private void handleRecoveryState(RecoveryStage recoveryStage,
            final AbstractRecoveryState<T> recoveryState,
            final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            final IRecoveryCleanupDelegate recoveryMarkerCleanup)
    {

        final DssRegistrationLogger logger = recoveryState.getRegistrationLogger(state);

        // keeps track of whether we should keep or delete the recovery files.
        // we can delete if succesfully recovered, or rolledback.
        // This code is not executed at all in case of a recovery give-up
        boolean shouldStopRecovery = false;

        // by default in case of failure we increase try count
        boolean shouldIncreaseTryCount = true;

        IRollbackDelegate<T> rollbackDelegate = new IRollbackDelegate<T>()
            {
                @Override
                public void didRollbackStorageAlgorithmRunner(
                        DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex,
                        ErrorType errorType)
                {
                    // do nothing. recovery takes care of everything
                }

                @Override
                public void markReadyForRecovery(DataSetStorageAlgorithmRunner<T> algorithm,
                        Throwable ex)
                {
                    // don't have to do nothing.
                }
            };

        // hookAdaptor
        RecoveryHookAdaptor hookAdaptor =
                new RecoveryHookAdaptor(recoveryState.getIncomingDataSetFile()
                        .getLogicalIncomingFile());

        DataSetRegistrationPersistentMap.IHolder persistentMapHolder =
                new DataSetRegistrationPersistentMap.IHolder()
                    {

                        @Override
                        public DataSetRegistrationPersistentMap getRegistrationContext()
                        {
                            return recoveryState.getPersistentMap();
                        }
                    };

        ArrayList<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms =
                recoveryState.getDataSetStorageAlgorithms(state);

        RollbackStack rollbackStack = recoveryState.getRollbackStack();

        DataSetStorageAlgorithmRunner<T> runner =
                new DataSetStorageAlgorithmRunner<T>(
                        recoveryState.getIncomingDataSetFile(), // incoming
                        dataSetStorageAlgorithms, // algorithms
                        rollbackDelegate, // rollback delegate,
                        rollbackStack, // rollbackstack
                        logger, // registrationLogger
                        state.getGlobalState().getOpenBisService(), // openBisService
                        hookAdaptor, // the hooks
                        state.getGlobalState().getStorageRecoveryManager(), persistentMapHolder,
                        state.getGlobalState());

        boolean registrationSuccessful = false;

        operationLog.info("Recovery succesfully deserialized the state of the registration");
        try
        {
            EntityOperationsState entityOperationsState;

            if (recoveryStage.beforeOrEqual(RecoveryStage.PRECOMMIT))
            {
                TechId registrationId =
                        ((DataSetStoragePrecommitRecoveryState<T>) recoveryState)
                                .getRegistrationId();
                if (registrationId == null)
                {
                    throw new IllegalStateException(
                            "Recovery state cannot have null registrationId at the precommit phase");
                }
                entityOperationsState =
                        state.getGlobalState().getOpenBisService()
                                .didEntityOperationsSucceed(registrationId);
            } else
            {
                // if we are at the later stage than precommit - it means that the entity operations
                // have succeeded
                entityOperationsState = EntityOperationsState.OPERATION_SUCCEEDED;
            }

            if (EntityOperationsState.IN_PROGRESS == entityOperationsState)
            {
                shouldIncreaseTryCount = false;
            } else if (EntityOperationsState.NO_OPERATION == entityOperationsState)
            {
                operationLog
                        .info("Recovery hasn't found registration artifacts in the application server. Registration of metadata was not successful.");

                IRollbackStackDelegate rollbackStackDelegate =
                        new AbstractTransactionState.LiveTransactionRollbackDelegate(state
                                .getGlobalState().getStagingDir());

                rollbackStack.setLockedState(false);

                rollbackStack.rollbackAll(rollbackStackDelegate);
                UnstoreDataAction action =
                        state.getOnErrorActionDecision().computeUndoAction(
                                ErrorType.OPENBIS_REGISTRATION_FAILURE, null);
                DataSetStorageRollbacker rollbacker =
                        new DataSetStorageRollbacker(state, operationLog, action, recoveryState
                                .getIncomingDataSetFile().getRealIncomingFile(), null, null,
                                ErrorType.OPENBIS_REGISTRATION_FAILURE);
                operationLog.info(rollbacker.getErrorMessageForLog());
                rollbacker.doRollback(logger);

                logger.log("Operations haven't been registered in AS - recovery rollback");
                logger.registerFailure();

                shouldStopRecovery = true;

                hookAdaptor.executePreRegistrationRollback(persistentMapHolder, null);

                finishRegistration(dataSetStorageAlgorithms, rollbackStack);
            } else
            {

                operationLog
                        .info("Recovery has found datasets in the AS. The registration of metadata was successful.");

                if (recoveryStage.before(RecoveryStage.POST_REGISTRATION_HOOK_EXECUTED))
                {
                    runner.postRegistration();
                }

                boolean success = true;
                if (recoveryStage.before(RecoveryStage.STORAGE_COMPLETED))
                {
                    success = runner.commitAndStore();
                }

                if (success)
                {
                    success = runner.cleanPrecommitAndConfirmStorage();
                }
                if (success)
                {
                    hookAdaptor.executePostStorage(persistentMapHolder);

                    registrationSuccessful = true;
                    shouldStopRecovery = true;

                    logger.registerSuccess();

                    // do the actions performed when the registration comes into terminal state.
                    finishRegistration(dataSetStorageAlgorithms, rollbackStack);

                }
            }
        } catch (Throwable error)
        {
            if ("org.jmock.api.ExpectationError".equals(error.getClass().getCanonicalName()))
            {
                // this exception can by only thrown by tests.
                // propagation of the exception is essential to test some functionalities
                // implemented like this to avoid dependency to jmock in production
                throw (Error) error;
            }
            operationLog.error("Uncaught error during recovery", error);
            // in this case we should ignore, and run the recovery again after some time
            logger.log(error, "Uncaught error during recovery");
        }

        cleanAfterwardsAction.execute(registrationSuccessful);

        recoveryMarkerCleanup.execute(shouldStopRecovery, shouldIncreaseTryCount);

    }

    private void finishRegistration(ArrayList<DataSetStorageAlgorithm<T>> dataSetStorageAlgorithms,
            RollbackStack rollbackStack)
    {
        for (DataSetStorageAlgorithm<T> algorithm : dataSetStorageAlgorithms)
        {
            algorithm.getStagingFile().delete();
        }
        rollbackStack.discard();
    }

    /**
     * Create an adaptor that offers access to the recovery hook functions.
     */
    protected class RecoveryHookAdaptor implements IPrePostRegistrationHook<T>
    {
        /**
         * internally use only with getInterpreter
         */
        private PythonInterpreter internalInterpreter;

        private final File incoming;

        public RecoveryHookAdaptor(File incoming)
        {
            this.incoming = incoming;
        }

        private PythonInterpreter getInterpreter()
        {
            if (internalInterpreter == null)
            {
                internalInterpreter = PythonInterpreter.createIsolatedPythonInterpreter();
                // interpreter.execute script

                configureEvaluator(incoming, null, internalInterpreter);

                // Load the script
                String scriptString = FileUtilities.loadToString(scriptFile);

                // Invoke the evaluator
                internalInterpreter.exec(scriptString);

                verifyEvaluatorHookFunctions(internalInterpreter);
            }
            return internalInterpreter;
        }

        @Override
        public void executePreRegistration(
                DataSetRegistrationPersistentMap.IHolder persistentMapHolder)
        {
            throw new NotImplementedException("Recovery cannot execute pre-registration hook.");
        }

        @Override
        public void executePostRegistration(
                DataSetRegistrationPersistentMap.IHolder persistentMapHolder)
        {
            PyFunction function =
                    tryJythonFunction(getInterpreter(),
                            JythonHookFunction.POST_REGISTRATION_FUNCTION_NAME);
            if (function != null)
            {
                invokeFunction(function, persistentMapHolder.getRegistrationContext());
            }
        }

        /**
         * This method does not belong to the IPrePostRegistrationHook interface. Is called directly
         * by recovery.
         */
        public void executePostStorage(DataSetRegistrationPersistentMap.IHolder persistentMapHolder)
        {
            PyFunction function =
                    tryJythonFunction(getInterpreter(),
                            JythonHookFunction.POST_STORAGE_FUNCTION_NAME);
            if (function != null)
            {
                invokeFunction(function, persistentMapHolder.getRegistrationContext());
            }
        }

        public void executePreRegistrationRollback(
                DataSetRegistrationPersistentMap.IHolder persistentMapHolder, Throwable t)
        {
            PyFunction function =
                    tryJythonFunction(getInterpreter(),
                            JythonHookFunction.ROLLBACK_PRE_REGISTRATION_FUNCTION_NAME);
            if (function != null)
            {
                invokeFunction(function, persistentMapHolder.getRegistrationContext(), t);
            }
        }

    }
}
