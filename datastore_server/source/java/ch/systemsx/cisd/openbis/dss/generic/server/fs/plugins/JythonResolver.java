/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.fs.plugins;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.jython.JythonUtils;
import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.IResolverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.FtpNonExistingFile;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.IFtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;

public class JythonResolver implements IResolverPlugin
{

    private static Map<String, IJythonEvaluator> interpreters = new HashMap<>();

    private static final String RESOLVE_FUNCTION_NAME = "resolve";

    private IJythonEvaluator interpreter;

    private String code;

    private String pluginName;

    @Override
    public IFtpFile resolve(String fullPath, String[] pathItems, FtpPathResolverContext resolverContext)
    {
        if (fullPath.startsWith("/" + code))
        {
            String path = fullPath.substring(code.length() + 1);
            if (path.startsWith("/"))
            {
                path = path.substring(1);
            }
            Object result = interpreter.evalFunction(RESOLVE_FUNCTION_NAME, path, fullPath, resolverContext);
            return (IFtpFile) result;
        } else
        {
            return new FtpNonExistingFile(fullPath, "invalid request to plugin " + pluginName + ": " + fullPath);
        }
    }

    @Override
    public void initialize(String pluginName, String code)
    {
        this.pluginName = pluginName;
        this.code = code;

        if (interpreters.containsKey(pluginName) == false)
        {
            String scriptPath = PropertyUtils.getMandatoryProperty(DssPropertyParametersUtil.loadServiceProperties(), pluginName + ".script-file");
            String scriptString = JythonUtils.extractScriptFromPath(scriptPath);
            String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPath);
            interpreters.put(pluginName, Evaluator.getFactory().create("", pythonPath, scriptPath, null, scriptString, false));
        }
        interpreter = interpreters.get(pluginName);
    }

}
