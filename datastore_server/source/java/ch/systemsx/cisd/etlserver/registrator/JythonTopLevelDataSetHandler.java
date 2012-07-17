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
import java.util.List;

import org.python.core.Py;
import org.python.core.PyBaseCode;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.interpreter.PythonInterpreter;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.api.IJavaDataSetRegistrationDropboxV1;
import ch.systemsx.cisd.etlserver.registrator.api.v1.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IJavaDataSetRegistrationDropboxV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.JythonAsJavaDataSetRegistrationDropboxV2Wrapper;
import ch.systemsx.cisd.etlserver.registrator.monitor.DssRegistrationHealthMonitor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A top-level data set handler that runs a python (jython) script to register data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetHandler<T extends DataSetInformation> extends
        AbstractProgrammableTopLevelDataSetHandler<T>
{
    public enum JythonHookFunction
    {
        /**
         * The name of the v2 process function, that is executed during the registration. A
         * replacement for a top-level file body code in v1.
         */
        PROCESS_FUNCTION("process", 1),

        /**
         * The name of the function to define to hook into the service rollback mechanism. V1 only.
         */
        ROLLBACK_SERVICE_FUNCTION_NAME("rollback_service", 2),

        /**
         * The name of the function to define to hook into the transaction rollback mechanism. V1
         * only.
         */
        ROLLBACK_TRANSACTION_FUNCTION_NAME("rollback_transaction", 4),

        /**
         * The name of the function called after successful transaction commit. V1 only.
         */
        COMMIT_TRANSACTION_FUNCTION_NAME("commit_transaction", 2),

        /**
         * The name of the function called after successful transaction commit.
         */
        POST_STORAGE_FUNCTION_NAME("post_storage", 1),

        /**
         * The name of the function called just before registration of datasets in application
         * server.
         */
        PRE_REGISTRATION_FUNCTION_NAME("pre_metadata_registration", 1),

        /**
         * The name of the function called just after successful registration of datasets in
         * application server.
         */
        POST_REGISTRATION_FUNCTION_NAME("post_metadata_registration", 1),

        /**
         * The name of the function to define to hook into the transaction rollback mechanism.
         */
        ROLLBACK_PRE_REGISTRATION_FUNCTION_NAME("rollback_pre_registration", 2),

        /**
         * The name of the function to define to hook into the transaction rollback mechanism.
         */
        SHOULD_RETRY_PROCESS_FUNCTION_NAME("should_retry_processing", 2),

        /**
         * The name of the function called when secondary transactions, DynamicTransactionQuery
         * objects, fail.
         */
        DID_ENCOUNTER_SECONDARY_TRANSACTION_ERRORS_FUNCTION_NAME(
                "did_encounter_secondary_transaction_errors", 3);

        public final String name;

        int argCount;

        private JythonHookFunction(String name, int argCount)
        {
            this.name = name;
            this.argCount = argCount;

        }
    }

    protected static final String FACTORY_VARIABLE_NAME = "factory";

    /**
     * The name of the local variable under which the service is made available to the script.
     */
    protected static final String SERVICE_VARIABLE_NAME = "service";

    /**
     * The name of the local variable under which the global state
     */
    protected static final String STATE_VARIABLE_NAME = "state";

    /**
     * The name of the local variable under which the incoming directory is made available to the
     * script.
     */
    protected static final String INCOMING_DATA_SET_VARIABLE_NAME = "incoming";

    /**
     * The name of the local variable under which the transaction is made available to the script.
     */
    protected static final String TRANSACTION_VARIABLE_NAME = "transaction";

    // The key for the script in the properties file
    public static final String SCRIPT_PATH_KEY = "script-path";

    private IJavaDataSetRegistrationDropboxV1<T> v1 = new IJavaDataSetRegistrationDropboxV1<T>()
        {
            @Override
            public void rollbackTransaction(DataSetRegistrationService<T> service,
                    DataSetRegistrationTransaction<T> transaction,
                    DataSetStorageAlgorithmRunner<T> algorithmRunner, Throwable ex)
            {
                PythonInterpreter interpreter = getInterpreterFromService(service);

                PyFunction function =
                        tryJythonFunction(interpreter,
                                JythonHookFunction.ROLLBACK_TRANSACTION_FUNCTION_NAME);
                if (null != function)
                {
                    invokeRollbackTransactionFunction(function, service, transaction,
                            algorithmRunner, ex);
                } else
                {
                    throw new NotImplementedException();
                }
            }

            @Override
            public void rollbackService(DataSetRegistrationService<T> service, Throwable ex)
            {
                PythonInterpreter interpreter = getInterpreterFromService(service);
                PyFunction function =
                        tryJythonFunction(interpreter,
                                JythonHookFunction.ROLLBACK_SERVICE_FUNCTION_NAME);
                if (null != function)
                {
                    invokeRollbackServiceFunction(function, service, ex);
                } else
                {
                    throw new NotImplementedException();
                }
            }

            @Override
            public void commitTransaction(DataSetRegistrationService<T> service,
                    DataSetRegistrationTransaction<T> transaction)
            {
                PythonInterpreter interpreter = getInterpreterFromService(service);
                PyFunction function =
                        tryJythonFunction(interpreter,
                                JythonHookFunction.COMMIT_TRANSACTION_FUNCTION_NAME);
                if (null != function)
                {
                    invokeServiceTransactionFunction(function, service, transaction);
                } else
                {
                    throw new NotImplementedException();
                }
            }

            @Override
            public void didEncounterSecondaryTransactionErrors(
                    DataSetRegistrationService<T> service,
                    DataSetRegistrationTransaction<T> transaction,
                    List<SecondaryTransactionFailure> secondaryErrors)
            {
                PythonInterpreter interpreter = getInterpreterFromService(service);
                PyFunction function =
                        tryJythonFunction(
                                interpreter,
                                JythonHookFunction.DID_ENCOUNTER_SECONDARY_TRANSACTION_ERRORS_FUNCTION_NAME);
                if (null != function)
                {
                    invokeDidEncounterSecondaryTransactionErrorsFunction(function, service,
                            transaction, secondaryErrors);
                } else
                {
                    throw new NotImplementedException();
                }
            }
        };

    protected final File scriptFile;

    /**
     * Constructor.
     * 
     * @param globalState
     */
    public JythonTopLevelDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);

        String path =
                PropertyUtils.getMandatoryProperty(globalState.getThreadParameters()
                        .getThreadProperties(), SCRIPT_PATH_KEY);
        scriptFile = new File(path);
        if (scriptFile.isFile() == false)
        {
            throw ConfigurationFailureException.fromTemplate("Script file '%s' does not exist!",
                    path);
        }

        DssRegistrationHealthMonitor.getInstance(globalState.getOpenBisService(),
                globalState.getRecoveryStateDir());

    }

    @Override
    public void handleDataSet(DataSetFile dataSetFile, DataSetRegistrationService<T> genericService)
            throws Throwable
    {
        // Load the script
        String scriptString = FileUtilities.loadToString(scriptFile);

        JythonDataSetRegistrationService<T> service =
                (JythonDataSetRegistrationService<T>) genericService;

        waitUntilApplicationIsReady(dataSetFile);

        executeJythonScript(dataSetFile, scriptString, service);
    }

    protected void executeJythonScript(DataSetFile dataSetFile, String scriptString,
            JythonDataSetRegistrationService<T> service)
    {
        // Configure the evaluator
        PythonInterpreter interpreter = service.interpreter;
        configureEvaluator(dataSetFile.getLogicalIncomingFile(), service, interpreter);

        // Invoke the evaluator
        interpreter.exec(scriptString);

        verifyEvaluatorHookFunctions(interpreter);
    }

    protected void verifyEvaluatorHookFunctions(PythonInterpreter interpreter)
    {
        for (JythonHookFunction function : JythonHookFunction.values())
        {
            PyFunction py = tryJythonFunction(interpreter, function);
            if (py != null)
            {
                if (py.func_code instanceof PyBaseCode)
                {
                    int co_argcount = ((PyBaseCode) py.func_code).co_argcount;
                    if (co_argcount != function.argCount)
                    {
                        throw new IllegalArgumentException(
                                String.format(
                                        "The function %s in %s has wrong number of arguments(%s instead of %s).",
                                        function.name, scriptFile.getName(), co_argcount,
                                        function.argCount));
                    }
                } else
                {
                    System.err
                            .println("Possibly incorrect python code. Can't verify script correctness.");
                }
            }
        }
    }

    private void configureEvaluator(File dataSetFile, JythonDataSetRegistrationService<T> service,
            PythonInterpreter interpreter)
    {
        interpreter.set(SERVICE_VARIABLE_NAME, service);
        interpreter.set(INCOMING_DATA_SET_VARIABLE_NAME, dataSetFile);
        interpreter.set(STATE_VARIABLE_NAME, getGlobalState());
        interpreter.set(FACTORY_VARIABLE_NAME, service.getDataSetRegistrationDetailsFactory());
        interpreter.set(TRANSACTION_VARIABLE_NAME, Py.None);
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
        return createJythonDataSetRegistrationService(incomingDataSetFile,
                callerDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                PythonInterpreter.createIsolatedPythonInterpreter(), getGlobalState());
    }

    /**
     * Create a Jython registration service that includes access to the interpreter.
     * 
     * @param pythonInterpreter
     */
    protected DataSetRegistrationService<T> createJythonDataSetRegistrationService(
            DataSetFile incomingDataSetFile,
            DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate, PythonInterpreter pythonInterpreter,
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        return new JythonDataSetRegistrationService<T>(this, incomingDataSetFile,
                userProvidedDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                pythonInterpreter, globalState);
    }

    @Override
    protected void rollback(DataSetRegistrationService<T> service, Throwable throwable)
    {
        try
        {
            v1.rollbackService(service, throwable);
        } catch (NotImplementedException ex)
        {
            // ignore
        }

        super.rollback(service, throwable);
    }

    // getters for v2 hook functions required for auto-recovery
    public PyFunction tryGetPostRegistrationFunction(DataSetRegistrationService<T> service)
    {
        PythonInterpreter interpreter = getInterpreterFromService(service);
        PyFunction function =
                tryJythonFunction(interpreter, JythonHookFunction.POST_REGISTRATION_FUNCTION_NAME);
        return function;
    }

    public PyFunction tryGetPostStorageFunction(DataSetRegistrationService<T> service)
    {
        PythonInterpreter interpreter = getInterpreterFromService(service);
        PyFunction function =
                tryJythonFunction(interpreter, JythonHookFunction.POST_STORAGE_FUNCTION_NAME);
        return function;
    }

    public PyFunction getRollbackPreRegistrationFunction(DataSetRegistrationService<T> service)
    {
        PythonInterpreter interpreter = getInterpreterFromService(service);
        PyFunction function =
                tryJythonFunction(interpreter,
                        JythonHookFunction.ROLLBACK_PRE_REGISTRATION_FUNCTION_NAME);
        return function;
    }

    public PyFunction getShouldRetryProcessFunction(DataSetRegistrationService<T> service)
    {
        PythonInterpreter interpreter = getInterpreterFromService(service);
        PyFunction function =
                tryJythonFunction(interpreter,
                        JythonHookFunction.SHOULD_RETRY_PROCESS_FUNCTION_NAME);
        return function;
    }

    /**
     * If true than the old methods of jython hook functions will also be used (as a fallbacks in
     * case of the new methods or missing, or normally)
     */
    @Override
    protected boolean shouldUseOldJythonHookFunctions()
    {
        return true;
    }

    protected PyFunction tryJythonFunction(PythonInterpreter interpreter,
            JythonHookFunction functionDefinition)
    {
        try
        {
            PyFunction function = interpreter.get(functionDefinition.name, PyFunction.class);
            return function;
        } catch (Exception e)
        {
            return null;
        }
    }

    private void invokeRollbackServiceFunction(PyFunction function,
            DataSetRegistrationService<T> service, Throwable throwable)
    {
        invokeFunction(function, service, throwable);
    }

    private void invokeRollbackTransactionFunction(PyFunction function,
            DataSetRegistrationService<T> service, DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithmRunner, Throwable throwable)
    {
        invokeFunction(function, service, transaction, algorithmRunner, throwable);
    }

    private void invokeServiceTransactionFunction(PyFunction function,
            DataSetRegistrationService<T> service, DataSetRegistrationTransaction<T> transaction)
    {
        invokeFunction(function, service, transaction);
    }

    private void invokeDidEncounterSecondaryTransactionErrorsFunction(PyFunction function,
            DataSetRegistrationService<T> service, DataSetRegistrationTransaction<T> transaction,
            List<SecondaryTransactionFailure> secondaryErrors)
    {
        invokeFunction(function, service, transaction, secondaryErrors);
    }

    /**
     * Turn all arguments into a python objects, and calls the specified function.
     */
    protected PyObject invokeFunction(PyFunction function, Object... args)
    {
        PyObject[] pyArgs = new PyObject[args.length];
        for (int i = 0; i < args.length; i++)
        {
            pyArgs[i] = Py.java2py(args[i]);
        }
        return function.__call__(pyArgs);
    }

    public abstract static class ProgrammableDropboxObjectFactory<T extends DataSetInformation>
            extends AbstractDataSetRegistrationDetailsFactory<T>
    {
        public ProgrammableDropboxObjectFactory(
                OmniscientTopLevelDataSetRegistratorState registratorState,
                DataSetInformation userProvidedDataSetInformationOrNull)
        {
            super(registratorState, userProvidedDataSetInformationOrNull);
        }

        /**
         * Factory method that creates a new registration details object.
         */
        public DataSetRegistrationDetails<T> createRegistrationDetails()
        {
            return createDataSetRegistrationDetails();
        }

        /**
         * Returns the Java class for the given class name.
         */
        public Class<?> getClass(String className)
        {
            try
            {
                return Class.forName(className);
            } catch (ClassNotFoundException ex)
            {
                return null;
            }
        }
    }

    public static class JythonDataSetRegistrationService<T extends DataSetInformation> extends
            DataSetRegistrationService<T>
    {
        private final PythonInterpreter interpreter;

        public JythonDataSetRegistrationService(
                AbstractProgrammableTopLevelDataSetHandler<T> registrator,
                DataSetFile incomingDataSetFile,
                DataSetInformation userProvidedDataSetInformationOrNull,
                IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
                ITopLevelDataSetRegistratorDelegate delegate, PythonInterpreter interpreter,
                TopLevelDataSetRegistratorGlobalState globalState)
        {
            super(registrator, incomingDataSetFile, registrator
                    .createObjectFactory(userProvidedDataSetInformationOrNull),
                    globalCleanAfterwardsAction, delegate);
            interpreter.set(STATE_VARIABLE_NAME, globalState);
            this.interpreter = interpreter;
        }

        public PythonInterpreter getInterpreter()
        {
            return interpreter;
        }

        @Override
        public void cleanAfterRegistrationIfNecessary()
        {
            super.cleanAfterRegistrationIfNecessary();

            if (interpreter != null)
            {
                interpreter.releaseResources();
            }
        }
    }

    protected PythonInterpreter getInterpreterFromService(DataSetRegistrationService<T> service)
    {
        PythonInterpreter interpreter = ((JythonDataSetRegistrationService<T>) service).interpreter;
        return interpreter;
    }

    @Override
    protected Throwable asSerializableException(Throwable throwable)
    {
        if (throwable instanceof PyException)
        {
            return new RuntimeException(throwable.toString());
        }

        return super.asSerializableException(throwable);
    }

    /**
     * V1 registration framework -- any file can go into faulty paths.
     */
    @Override
    public boolean shouldNotAddToFaultyPathsOrNull(File storeItem)
    {
        return false;
    }

    @Override
    protected IJavaDataSetRegistrationDropboxV2<T> getV2DropboxProgram(
            DataSetRegistrationService<T> service)
    {
        return new JythonAsJavaDataSetRegistrationDropboxV2Wrapper<T>(
                getInterpreterFromService(service));
    }

    @Override
    protected IJavaDataSetRegistrationDropboxV1<T> getV1DropboxProgram()
    {
        return v1;
    }

    @Override
    protected ch.systemsx.cisd.etlserver.registrator.AbstractProgrammableTopLevelDataSetHandler<T>.RecoveryHookAdaptor getRecoveryHookAdaptor(
            File incoming)
    {
        throw new NotImplementedException();
    }
}
