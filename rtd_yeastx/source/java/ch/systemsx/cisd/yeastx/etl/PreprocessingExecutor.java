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

package ch.systemsx.cisd.yeastx.etl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;

/**
 * A class which is configured from properties and is able to execute a script from the command line
 * using the configured path {@link #PREPROCESSING_SCRIPT_PATH}.
 * 
 * @author Tomasz Pylak
 */
public class PreprocessingExecutor
{
    /**
     * A path to a script which should be called from command line for every dataset batch before it
     * is processed. Can be used e.g. to change file permissions. The script gets one parameter, the
     * path to the dataset file, relative to the incoming directory.
     */
    public final static String PREPROCESSING_SCRIPT_PATH = "preprocessing-script";

    public static PreprocessingExecutor create(Properties properties)
    {
        String preprocessingScriptPath = properties.getProperty(PREPROCESSING_SCRIPT_PATH);
        if (preprocessingScriptPath != null)
        {
            return new PreprocessingExecutor(preprocessingScriptPath);
        } else
        {
            throw EnvironmentFailureException.fromTemplate("Property '%s' is not set!",
                    PREPROCESSING_SCRIPT_PATH);
        }
    }

    private final String preprocessingScriptPath;

    private PreprocessingExecutor(String preprocessingScriptPath)
    {
        this.preprocessingScriptPath = preprocessingScriptPath;
    }

    public boolean execute(String filePath)
    {
        return callScript(preprocessingScriptPath, getClass(), filePath);
    }

    private static boolean callScript(String scriptPath, Class<?> logClass, String... args)
    {
        List<String> cmd = new ArrayList<String>();
        cmd.add(scriptPath);
        cmd.addAll(Arrays.asList(args));
        Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, logClass);
        Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, logClass);
        return ProcessExecutionHelper.runAndLog(cmd, operationLog, machineLog);
    }

}