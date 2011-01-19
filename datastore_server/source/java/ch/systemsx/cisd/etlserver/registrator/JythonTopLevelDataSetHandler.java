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

import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A top-level data set handler that runs a python (jython) script to register data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetHandler extends AbstractOmniscientTopLevelDataSetRegistrator
{
    /**
     * The name of the function to define to hook into the data set registration rollback mechanism.
     */
    private static final String ROLLBACK_DATA_SET_REGISTRATION_FUNCTION_NAME =
            "rollback_data_set_registration";

    /**
     * The name of the function to define to hook into the service rollback mechanism.
     */
    private static final String ROLLBACK_SERVICE_FUNCTION_NAME = "rollback_service";

    /**
     * The name of the local variable under which the service is made available to the script.
     */
    private static final String SERVICE_VARIABLE_NAME = "service";

    /**
     * The name of the local variable under which the global state
     */
    private static final String STATE_VARIABLE_NAME = "state";

    /**
     * The name of the local variable under which the incoming directory is made available to the
     * script.
     */
    private static final String INCOMING_DATA_SET_VARIABLE_NAME = "incoming";

    // The key for the script in the properties file
    public static final String SCRIPT_PATH_KEY = "script-path";

    private final File scriptFile;

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

    }

    @Override
    protected void handleDataSet(File dataSetFile, DataSetRegistrationService genericService)
            throws Throwable
    {
        // Load the script
        String scriptString = FileUtilities.loadToString(scriptFile);

        JythonDataSetRegistrationService service =
                (JythonDataSetRegistrationService) genericService;

        // Configure the evaluator
        PythonInterpreter interpreter = service.interpreter;
        interpreter.set(SERVICE_VARIABLE_NAME, service);
        interpreter.set(INCOMING_DATA_SET_VARIABLE_NAME, dataSetFile);
        interpreter.set(STATE_VARIABLE_NAME, getGlobalState());
        setObjectFactory(interpreter);

        try
        {
            // Invoke the evaluator
            interpreter.exec(scriptString);
        } catch (Throwable ex)
        {
            operationLog
                    .error(String
                            .format("Cannot register dataset from a file '%s'. Error in jython dropbox has occured:\n%s",
                                    dataSetFile.getPath(), ex.toString()));
            throw ex;
        }
    }

    /**
     * Create a registration service that includes a python interpreter (we need the interpreter in
     * the service so we can use it in error handling).
     */
    @Override
    protected DataSetRegistrationService createDataSetRegistrationService(
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction)
    {
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.set(STATE_VARIABLE_NAME, getGlobalState());
        JythonDataSetRegistrationService service =
                new JythonDataSetRegistrationService(this, cleanAfterwardsAction, interpreter);
        return service;
    }

    @Override
    public void rollback(DataSetRegistrationService service,
            DataSetRegistrationAlgorithm registrationAlgorithm, Throwable throwable)
    {
        PythonInterpreter interpreter = ((JythonDataSetRegistrationService) service).interpreter;
        PyFunction function =
                tryJythonFunction(interpreter, ROLLBACK_DATA_SET_REGISTRATION_FUNCTION_NAME);
        if (null != function)
        {
            invokeRollbackDataSetRegistrationFunction(function, service, registrationAlgorithm,
                    throwable);
        }

        super.rollback(service, registrationAlgorithm, throwable);
    }

    @Override
    protected void rollback(DataSetRegistrationService service, Throwable throwable)
    {
        PythonInterpreter interpreter = ((JythonDataSetRegistrationService) service).interpreter;
        PyFunction function = tryJythonFunction(interpreter, ROLLBACK_SERVICE_FUNCTION_NAME);
        if (null != function)
        {
            invokeRollbackServiceFunction(function, service, throwable);
        }

        super.rollback(service, throwable);
    }

    private PyFunction tryJythonFunction(PythonInterpreter interpreter, String functionName)
    {
        try
        {
            PyFunction function = (PyFunction) interpreter.get(functionName, PyFunction.class);
            return function;
        } catch (Exception e)
        {
            return null;
        }

    }

    /**
     * Pulled out as a separate method so tests can hook in.
     */
    protected void invokeRollbackServiceFunction(PyFunction function,
            DataSetRegistrationService service, Throwable throwable)
    {
        function.__call__(Py.java2py(service), Py.java2py(throwable));
    }

    /**
     * Pulled out as a separate method so tests can hook in.
     */
    protected void invokeRollbackDataSetRegistrationFunction(PyFunction function,
            DataSetRegistrationService service, DataSetRegistrationAlgorithm registrationAlgorithm,
            Throwable throwable)
    {
        function.__call__(Py.java2py(service), Py.java2py(registrationAlgorithm),
                Py.java2py(throwable));
    }

    /**
     * Set the factory available to the python script. Subclasses may want to override.
     */
    protected void setObjectFactory(PythonInterpreter interpreter)
    {
        interpreter.set("factory", new JythonObjectFactory(getRegistratorState()));
    }

    public static class JythonObjectFactory
    {
        protected final OmniscientTopLevelDataSetRegistratorState registratorState;

        public JythonObjectFactory(OmniscientTopLevelDataSetRegistratorState registratorState)
        {
            this.registratorState = registratorState;
        }

        /**
         * Factory method that creates a new registration details object.
         */
        public DataSetRegistrationDetails createRegistrationDetails()
        {
            DataSetRegistrationDetails registrationDetails = new DataSetRegistrationDetails();
            registrationDetails.setDataSetInformation(createDataSetInformation());
            return registrationDetails;
        }

        /**
         * Factory method that creates a new data set information object.
         */
        public DataSetInformation createDataSetInformation()
        {
            DataSetInformation dataSetInfo = new DataSetInformation();
            dataSetInfo.setInstanceCode(registratorState.getHomeDatabaseInstance().getCode());
            dataSetInfo.setInstanceUUID(registratorState.getHomeDatabaseInstance().getUuid());

            return dataSetInfo;
        }
    }

    protected static class JythonDataSetRegistrationService extends DataSetRegistrationService
    {
        private final PythonInterpreter interpreter;

        /**
         * @param registrator
         * @param globalCleanAfterwardsAction
         */
        public JythonDataSetRegistrationService(
                AbstractOmniscientTopLevelDataSetRegistrator registrator,
                IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
                PythonInterpreter interpreter)
        {
            super(registrator, globalCleanAfterwardsAction);
            this.interpreter = interpreter;
        }

        public PythonInterpreter getInterpreter()
        {
            return interpreter;
        }

    }
}
