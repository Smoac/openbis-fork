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
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IMailService;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISearchService;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * @author Piotr Buczek
 */
class PluginScriptRunner
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PluginScriptRunner.class);

    private final static String SEARCH_SERVICE_VARIABLE_NAME = "searchService";

    private final static String MAIL_SERVICE_VARIABLE_NAME = "mailService";

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
    public static IReportingPluginScriptRunner createReportingPluginRunnerFromScriptPath(
            String scriptPath, DataSetProcessingContext context)
    {
        String scriptString = extractScriptFromPath(scriptPath);
        try
        {
            return createReportingPluginRunnerFromScriptString(scriptString, context);
        } catch (EvaluatorException ex)
        {
            throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
        }
    }

    /**
     * Factory method for creating an IProcessingPluginScriptRunner given a path to a script.
     */
    public static IProcessingPluginScriptRunner createProcessingPluginRunnerFromScriptPath(
            String scriptPath, DataSetProcessingContext context)
    {
        String scriptString = extractScriptFromPath(scriptPath);
        try
        {
            return createProcessingPluginRunnerFromScriptString(scriptString, context);
        } catch (EvaluatorException ex)
        {
            throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
        }
    }

    /**
     * Factory method for creating an IReportingPluginScriptRunner given the script as a string.
     */
    static IReportingPluginScriptRunner createReportingPluginRunnerFromScriptString(
            String scriptString, DataSetProcessingContext context)
    {
        return new ReportingPluginScriptRunner(createEvaluator(scriptString, context));
    }

    /**
     * Factory method for creating an IProcessingPluginScriptRunner given the script as a string.
     */
    static IProcessingPluginScriptRunner createProcessingPluginRunnerFromScriptString(
            String scriptString, DataSetProcessingContext context)
    {
        return new ProcessingPluginScriptRunner(createEvaluator(scriptString, context));
    }

    private static Evaluator createEvaluator(String scriptString, DataSetProcessingContext context)
    {
        final Evaluator evaluator = new Evaluator("", null, scriptString, false);
        evaluator.set(SEARCH_SERVICE_VARIABLE_NAME, createSearchService());
        evaluator.set(MAIL_SERVICE_VARIABLE_NAME, createMailService(context));
        return evaluator;
    }

    private static ISearchService createSearchService()
    {
        return ServiceProvider.getSearchServiceProvider();
    }

    private static IMailService createMailService(DataSetProcessingContext context)
    {
        return new MailService(context.getMailClient(), context.getUserEmailOrNull());
    }

    protected final Evaluator evaluator;

    PluginScriptRunner(Evaluator evaluator)
    {
        this.evaluator = evaluator;
    }

    static class ReportingPluginScriptRunner extends PluginScriptRunner implements
            IReportingPluginScriptRunner
    {
        private final static String DESCRIBE_FUNCTION_NAME = "describe";

        private static final long serialVersionUID = 1L;

        ReportingPluginScriptRunner(Evaluator evaluator)
        {
            super(evaluator);
            if (false == evaluator.hasFunction(DESCRIBE_FUNCTION_NAME))
            {
                throw new EvaluatorException("Function '" + DESCRIBE_FUNCTION_NAME
                        + "' was not defined in the reporting plugin script");
            }
        }

        public void describe(List<IDataSet> dataSets, ISimpleTableModelBuilderAdaptor tableBuilder)
        {
            evaluator.evalFunction(DESCRIBE_FUNCTION_NAME, dataSets, tableBuilder);
        }
    }

    static class ProcessingPluginScriptRunner extends PluginScriptRunner implements
            IProcessingPluginScriptRunner
    {
        private final static String PROCESS_FUNCTION_NAME = "process";

        ProcessingPluginScriptRunner(Evaluator evaluator)
        {
            super(evaluator);
            if (false == evaluator.hasFunction(PROCESS_FUNCTION_NAME))
            {
                throw new EvaluatorException("Function '" + PROCESS_FUNCTION_NAME
                        + "' was not defined in the processing plugin script");
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
