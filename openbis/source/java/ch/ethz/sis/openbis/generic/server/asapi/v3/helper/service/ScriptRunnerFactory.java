/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.service;

import java.io.Serializable;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.context.ServiceContext;
import ch.systemsx.cisd.common.jython.JythonUtils;
import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ScriptRunnerFactory implements IScriptRunnerFactory
{
    private final String scriptPath;
    private final IApplicationServerApi applicationService;

    public ScriptRunnerFactory(String scriptPath, IApplicationServerApi applicationService)
    {
        this.scriptPath = scriptPath;
        this.applicationService = applicationService;
        Evaluator.initialize();
    }

    @Override
    public String getScriptPath()
    {
        return scriptPath;
    }

    @Override
    public IServiceScriptRunner createServiceRunner(ServiceContext context)
    {
        String scriptString = JythonUtils.extractScriptFromPath(scriptPath);
        String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPath);

        try
        {
            Evaluator evaluator = new Evaluator("", pythonPath, null, scriptString, false);
            evaluator.set("sessionToken", context.getSessionToken());
            evaluator.set("applicationService", applicationService);
            return new ServiceScriptRunner(evaluator);
        } catch (EvaluatorException ex)
        {
            throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
        }
    }
    
    private static final class ServiceScriptRunner implements IServiceScriptRunner
    {
        private static final String PROCESS_FUNCTION_NAME = "process";
        
        private Evaluator evaluator;

        ServiceScriptRunner(Evaluator evaluator)
        {
            this.evaluator = evaluator;
            if (evaluator.hasFunction(PROCESS_FUNCTION_NAME) == false)
            {
                throw new EvaluatorException("Function '" + PROCESS_FUNCTION_NAME
                        + "' was not defined in the processing plugin script");
            }
            
        }

        @Override
        public Serializable process(ExecutionOptions options)
        {
            Object result = evaluator.evalFunction(PROCESS_FUNCTION_NAME, options.getParameters());
            if (result == null || result instanceof Serializable)
            {
                return (Serializable) result;
            }
            throw new EvaluatorException("Function '" + PROCESS_FUNCTION_NAME 
                    + "' dosn't return a serializable object. Object type: " + result.getClass());
        }
    }

}
