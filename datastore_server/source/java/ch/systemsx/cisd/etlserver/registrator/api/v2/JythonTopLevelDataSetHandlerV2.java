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
import java.util.Calendar;
import java.util.Date;

import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.utilities.PythonUtils;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPersistentMap;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner.IPrePostRegistrationHook;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner.IRollbackDelegate;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageRollbacker;
import ch.systemsx.cisd.etlserver.registrator.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.etlserver.registrator.MarkerFileUtility;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.RollbackStack.IRollbackStackDelegate;
import ch.systemsx.cisd.etlserver.registrator.recovery.AbstractRecoveryState;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStoragePrecommitRecoveryState;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryInfo;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryInfo.RecoveryStage;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * A top-level data set handler that runs a python (jython) script to register data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetHandlerV2<T extends DataSetInformation> extends
        ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler<T>
{

    /**
     * Constructor.
     * 
     * @param globalState
     */
    public JythonTopLevelDataSetHandlerV2(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
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
                PythonUtils.createIsolatedPythonInterpreter(), getGlobalState());
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

    @Override
    protected void configureEvaluator(
            File dataSetFile,
            ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler.JythonDataSetRegistrationService<T> service,
            PythonInterpreter interpreter)
    {
        interpreter.set(INCOMING_DATA_SET_VARIABLE_NAME, dataSetFile);
        interpreter.set(STATE_VARIABLE_NAME, getGlobalState());

        if (service != null)
        {
            interpreter.set(TRANSACTION_VARIABLE_NAME, service.transaction());
            interpreter.set(FACTORY_VARIABLE_NAME, service.getDataSetRegistrationDetailsFactory());
        }
    }

    @Override
    protected void executeJythonProcessFunction(PythonInterpreter interpreter)
    {
        String PROCESS_FUNCTION_NAME = "process";
        try
        {
            PyFunction function = interpreter.get(PROCESS_FUNCTION_NAME, PyFunction.class);
            if (function == null)
            {
                throw new IllegalStateException("Undefined process() function");
            }
            function.__call__();
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
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

        IDelegatedActionWithResult<Boolean> recoveryMarkerFileCleanupAction =
                new IDelegatedActionWithResult<Boolean>()
                    {
                        public Boolean execute(boolean didOperationSucceed)
                        {
                            if (!didOperationSucceed
                                    && recoveryInfo.getTryCount() >= state.getGlobalState()
                                            .getStorageRecoveryManager().getMaximumRertyCount())
                            {
                                notificationLog.error("The dataset "
                                        + recoveryState.getIncomingDataSetFile()
                                                .getRealIncomingFile()
                                        + " has failed to register. Giving up.");
                                deleteMarkerFile();

                                System.err.println("This is happening " + recoveryMarkerFile);
                                File errorRecoveryMarkerFile =
                                        new File(recoveryMarkerFile.getParent(),
                                                recoveryMarkerFile.getName() + ".ERROR");
                                state.getFileOperations().move(recoveryMarkerFile,
                                        errorRecoveryMarkerFile);

                                logger.log("Recovery failed. Giving up.");
                                logger.registerFailure();

                            } else
                            {
                                if (didOperationSucceed)
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
                                    rInfo.increaseTryCount();
                                    rInfo.setLastTry(new Date());
                                    rInfo.writeToFile(recoveryMarkerFile);
                                }
                            }
                            return true;
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
            final IDelegatedActionWithResult<Boolean> recoveryMarkerCleanup)
    {

        final DssRegistrationLogger logger = recoveryState.getRegistrationLogger(state);

        // keeps track of whether we should keep or delete the recovery files.
        // we can delete if succesfully recovered, or rolledback.
        // This code is not executed at all in case of a recovery give-up
        boolean shouldStopRecovery = false;

        IRollbackDelegate<T> rollbackDelegate = new IRollbackDelegate<T>()
            {
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

                        public DataSetRegistrationPersistentMap getPersistentMap()
                        {
                            return recoveryState.getPersistentMap();
                        }
                    };

        DataSetStorageAlgorithmRunner<T> runner =
                new DataSetStorageAlgorithmRunner<T>(recoveryState.getIncomingDataSetFile(), // incoming
                        recoveryState.getDataSetStorageAlgorithms(state), // algorithms
                        rollbackDelegate, // rollback delegate,
                        recoveryState.getRollbackStack(), // rollbackstack
                        logger, // registrationLogger
                        state.getGlobalState().getOpenBisService(), // openBisService
                        hookAdaptor, // the hooks
                        state.getGlobalState().getStorageRecoveryManager(), persistentMapHolder);

        boolean registrationSuccessful = false;

        operationLog.info("Recovery succesfully deserialized the state of the registration");
        try
        {
            boolean entityOperationsSucceeded;

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
                entityOperationsSucceeded =
                        state.getGlobalState().getOpenBisService()
                                .didEntityOperationsSucceed(registrationId);
            } else
            {
                // if we are at the later stage than precommit - it means that the entity operations
                // have succeeded
                entityOperationsSucceeded = true;
            }

            if (false == entityOperationsSucceeded)
            {
                operationLog
                        .info("Recovery hasn't found registration artifacts in the application server. Registration of metadata was not successful.");

                IRollbackStackDelegate rollbackStackDelegate =
                        new AbstractTransactionState.LiveTransactionRollbackDelegate(state
                                .getGlobalState().getStagingDir());

                recoveryState.getRollbackStack().setLockedState(false);

                recoveryState.getRollbackStack().rollbackAll(rollbackStackDelegate);
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

            // in this case we should ignore, and run the recovery again after some time
            logger.log(error, "Uncaught error during recovery");
        }

        cleanAfterwardsAction.execute(registrationSuccessful);

        recoveryMarkerCleanup.execute(shouldStopRecovery);

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
                internalInterpreter = PythonUtils.createIsolatedPythonInterpreter();
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

        public void executePreRegistration(
                DataSetRegistrationPersistentMap.IHolder persistentMapHolder)
        {
            throw new NotImplementedException("Recovery cannot execute pre-registration hook.");
        }

        public void executePostRegistration(
                DataSetRegistrationPersistentMap.IHolder persistentMapHolder)
        {
            PyFunction function =
                    tryJythonFunction(getInterpreter(),
                            JythonHookFunction.POST_REGISTRATION_FUNCTION_NAME);
            if (function != null)
            {
                invokeFunction(function, persistentMapHolder.getPersistentMap());
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
                invokeFunction(function, persistentMapHolder.getPersistentMap());
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
                invokeFunction(function, persistentMapHolder.getPersistentMap(), t);
            }
        }

    }
}
