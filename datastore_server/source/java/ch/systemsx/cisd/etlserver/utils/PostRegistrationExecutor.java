/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.etlserver.IPostRegistrationAction;

/**
 * A class which is configured from properties and is able to execute a script from the command line
 * using the configured path.
 * 
 * @author Izabela Adamczyk
 */
public class PostRegistrationExecutor implements IPostRegistrationAction
{

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PostRegistrationExecutor.class);

    private final static Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            PostRegistrationExecutor.class);

    static class EmptyScriptExecutor implements IPostRegistrationAction
    {

        public boolean execute(String dataSetCode, String dataSetAbsolutePathInStore)
        {
            return true;
        }
    }

    public static IPostRegistrationAction create(String scriptPath)
    {
        if (scriptPath != null)
        {
            return new PostRegistrationExecutor(scriptPath);
        } else
        {
            operationLog.debug("No postregistration script found, skipping execution.");
            return new EmptyScriptExecutor();
        }
    }

    private final String scriptPath;

    private PostRegistrationExecutor(String script)
    {
        this.scriptPath = script;
        File file = new File(script);
        IFileOperations fileOperations = FileOperations.getInstance();
        if (fileOperations.isFile(file) == false || fileOperations.canRead(file) == false)
        {
            throw new ConfigurationFailureException(String.format(
                    "Cannot access postregistration script '%s'.", script));
        }
        operationLog.info("Postregistration script: " + script);
    }

    public boolean execute(final String dataSetCode, final String dataSetAbsolutePathInStore)
    {
        return callScript(scriptPath, dataSetCode, dataSetAbsolutePathInStore);
    }

    private static boolean callScript(String scriptPath, String... args)
    {
        List<String> cmd = new ArrayList<String>();
        cmd.add(scriptPath);
        cmd.addAll(Arrays.asList(args));
        boolean result =
                ProcessExecutionHelper.runAndLog(cmd, operationLog, machineLog,
                        ConcurrencyUtilities.NO_TIMEOUT);
        if (result == false)
        {
            operationLog.warn(String.format("Script execution failed: SCRIPT(%s) PARAMETERS(%s)",
                    scriptPath, StringUtils.join(args, ",")));
        } else
        {
            operationLog.info(String.format(
                    "Successful script execution: SCRIPT(%s) PARAMETERS(%s)", scriptPath,
                    StringUtils.join(args, ",")));
        }
        return result;
    }

}