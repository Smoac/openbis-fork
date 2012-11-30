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

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.jython.PythonInterpreter;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A top-level data set handler that runs a python (jython) script to register data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetHandlerV2<T extends DataSetInformation> extends
        ch.systemsx.cisd.etlserver.registrator.v1.JythonTopLevelDataSetHandler<T>
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
            ch.systemsx.cisd.etlserver.registrator.v1.JythonTopLevelDataSetHandler.JythonDataSetRegistrationService<T> service,
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

        IJavaDataSetRegistrationDropboxV2 v2Programm =
                new JythonAsJavaDataSetRegistrationDropboxV2Wrapper(interpreter);

        // Invoke the evaluator
        interpreter.exec(scriptString);

        verifyEvaluatorHookFunctions(interpreter);

        if (false == v2Programm.isRetryFunctionDefined())
        {
            // in case when there is no retry function defined we just call the process and don't
            // try to catch any kind of exceptions
            v2Programm.process(wrapTransaction(service.transaction()));
        } else
        {
            executeProcessFunctionWithRetries(v2Programm,
                    (JythonDataSetRegistrationServiceV2<T>) service, dataSetFile);
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
    protected IJavaDataSetRegistrationDropboxV2 getV2DropboxProgram(
            DataSetRegistrationService<T> service)
    {
        return new JythonAsJavaDataSetRegistrationDropboxV2Wrapper(
                getInterpreterFromService(service));
    }

    @Override
    protected ch.systemsx.cisd.etlserver.registrator.v1.AbstractProgrammableTopLevelDataSetHandler<T>.RecoveryHookAdaptor getRecoveryHookAdaptor(
            File incoming)
    {
        return new RecoveryHookAdaptor(incoming)
            {
                IJavaDataSetRegistrationDropboxV2 v2ProgramInternal;

                @Override
                protected IJavaDataSetRegistrationDropboxV2 getV2DropboxProgramInternal()
                {
                    if (v2ProgramInternal == null)
                    {
                        PythonInterpreter internalInterpreter =
                                PythonInterpreter.createIsolatedPythonInterpreter();
                        // interpreter.execute script

                        configureEvaluator(incoming, null, internalInterpreter);

                        // Load the script
                        String scriptString = FileUtilities.loadToString(scriptFile);

                        // Invoke the evaluator
                        internalInterpreter.exec(scriptString);

                        verifyEvaluatorHookFunctions(internalInterpreter);

                        v2ProgramInternal =
                                new JythonAsJavaDataSetRegistrationDropboxV2Wrapper(
                                        internalInterpreter);
                    }
                    return v2ProgramInternal;
                }
            };
    }
}
