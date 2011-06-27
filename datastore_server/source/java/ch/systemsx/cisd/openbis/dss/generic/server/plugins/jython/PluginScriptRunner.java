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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IDataSet;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * @author Piotr Buczek
 */
public class PluginScriptRunner
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PluginScriptRunner.class);

    private final static String PROCESS_FUNCTION_NAME = "process";

    private final static String DESCRIBE_FUNCTION_NAME = "describe";

    private final static String DESCRIBE_ALL_FUNCTION_NAME = "describeAll";

    enum PluginScriptType
    {
        REPORTING, PROCESSING
    }

    /**
     * @return script string from file with given path
     * @throws EvaluatorException if the file doesn't exist or is empty
     */
    static String extractScriptFromPath(String scriptPath) throws EvaluatorException
    {
        File scriptFile = new File(scriptPath);
        if (false == scriptFile.exists())
        {
            throw new EvaluatorException("Plugin script [" + scriptPath
                    + "] specified in the configuration doesn't exist.");
        } else
        {
            String scriptString = FileUtilities.loadToString(scriptFile);
            if (StringUtils.isBlank(scriptString))
            {
                throw new EvaluatorException("Plugin script [" + scriptPath
                        + "] specified in the configuration is empty.");
            } else
            {
                try
                {
                    return scriptString + "\n";
                } catch (EvaluatorException ex)
                {
                    throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
                }
            }
        }
    }

    /**
     * Factory method for creating an IReportingPluginScriptRunner given a path to a script.
     */
    public static IReportingPluginScriptRunner createReportingPluginFromScriptPath(String scriptPath)
    {
        String scriptString = extractScriptFromPath(scriptPath);
        try
        {
            return createReportingPluginFromScriptString(scriptString);
        } catch (EvaluatorException ex)
        {
            throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
        }
    }

    /**
     * Factory method for creating an IProcessingPluginScriptRunner given a path to a script.
     */
    public static IProcessingPluginScriptRunner createProcessingPluginFromScriptPath(
            String scriptPath)
    {
        String scriptString = extractScriptFromPath(scriptPath);
        try
        {
            return createProcessingPluginFromScriptString(scriptString);
        } catch (EvaluatorException ex)
        {
            throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
        }
    }

    /**
     * Factory method for creating an IReportingPluginScriptRunner given the script as a string.
     */
    static IReportingPluginScriptRunner createReportingPluginFromScriptString(String scriptString)
    {
        return new ReportingPluginScriptRunner(createEvaluator(scriptString));
    }

    /**
     * Factory method for creating an IProcessingPluginScriptRunner given the script as a string.
     */
    static IProcessingPluginScriptRunner createProcessingPluginFromScriptString(String scriptString)
    {
        return new ProcessingPluginScriptRunner(createEvaluator(scriptString));
    }

    private static Evaluator createEvaluator(String scriptString)
    {
        return new Evaluator("", null, scriptString, false);
    }

    protected final Evaluator evaluator;

    PluginScriptRunner(Evaluator evaluator)
    {
        this.evaluator = evaluator;
    }

    static class ReportingPluginScriptRunner extends PluginScriptRunner implements
            IReportingPluginScriptRunner
    {

        private final boolean isDescribeAllDefined;

        private final boolean isDescribeDefined;

        ReportingPluginScriptRunner(Evaluator evaluator)
        {
            super(evaluator);
            this.isDescribeAllDefined = evaluator.hasFunction(DESCRIBE_ALL_FUNCTION_NAME);
            this.isDescribeDefined = evaluator.hasFunction(DESCRIBE_FUNCTION_NAME);
            String failMessagePrefix =
                    "Either '" + DESCRIBE_FUNCTION_NAME + "' or '" + DESCRIBE_ALL_FUNCTION_NAME
                            + "' " + "' funciton should be defined in the reporting plugin script";
            if (isDescribeAllDefined && isDescribeDefined)
            {
                throw new EvaluatorException(failMessagePrefix + " but both were defined.");
            }
            if (false == (isDescribeAllDefined || isDescribeDefined))
            {
                throw new EvaluatorException(failMessagePrefix + " but neither was defined.");
            }
        }

        public void describe(List<IDataSet> dataSets, ISimpleTableModelBuilderAdaptor tableBuilder)
        {
            if (isDescribeAllDefined)
            {
                evaluator.evalFunction(DESCRIBE_ALL_FUNCTION_NAME, tableBuilder, dataSets);
            } else
            {
                assert isDescribeDefined;
                for (IDataSet dataSet : dataSets)
                {
                    evaluator.evalFunction(DESCRIBE_FUNCTION_NAME, tableBuilder, dataSet);
                }
            }
        }
    }

    static class ProcessingPluginScriptRunner extends PluginScriptRunner implements
            IProcessingPluginScriptRunner
    {

        ProcessingPluginScriptRunner(Evaluator evaluator)
        {
            super(evaluator);
            if (false == evaluator.hasFunction(PROCESS_FUNCTION_NAME))
            {
                throw new EvaluatorException("Function '" + PROCESS_FUNCTION_NAME
                        + "' was not defined in the script reporting script");
            }
        }

        public Status process(IDataSet dataSet)
        {
            try
            {
                evaluator.evalFunction(PROCESS_FUNCTION_NAME, dataSet);
            } catch (EvaluatorException ex)
            {
                operationLog.error(ex.getMessage());
                return Status.createError(ex.getMessage());
            }
            return Status.OK;
        }

    }

}
