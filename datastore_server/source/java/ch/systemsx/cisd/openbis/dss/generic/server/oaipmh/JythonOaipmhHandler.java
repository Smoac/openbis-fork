/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.dss.generic.server.oaipmh;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.cisd.common.jython.JythonUtils;
import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.SearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

import de.schlichtherle.io.File;

/**
 * @author pkupczyk
 */
public class JythonOaipmhHandler implements IOaipmhHandler
{

    private static final String SCRIPT_PATH_PARAMETER_NAME = "script-path";

    private static final String SCRIPT_FUNCTION_NAME = "handle";

    private static final String SEARCH_SERVICE_VARIABLE_NAME = "searchService";

    private String scriptPath;

    @Override
    public void init(Properties properties)
    {
        this.scriptPath = initScriptPath(properties);
    }

    private String initScriptPath(Properties properties)
    {
        String path = properties.getProperty(SCRIPT_PATH_PARAMETER_NAME);

        if (path == null || path.trim().length() == 0)
        {
            throw new IllegalArgumentException("Script path is null or empty");
        }

        File file = new File(path);

        if (false == file.exists())
        {
            throw new IllegalArgumentException("Script path: '" + file.getAbsolutePath() + "' does not exist");
        }

        if (file.isDirectory())
        {
            throw new IllegalArgumentException("Script path: '" + file.getAbsolutePath() + "' represents a directory");
        }

        return path;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
    {
        String scriptString = JythonUtils.extractScriptFromPath(scriptPath);
        String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPath);

        Evaluator evaluator = new Evaluator("", pythonPath, null, scriptString, false);
        evaluator.set(SEARCH_SERVICE_VARIABLE_NAME, new SearchService(ServiceProvider.getOpenBISService()));
        evaluator.evalFunction(SCRIPT_FUNCTION_NAME, req, resp);
    }

}
