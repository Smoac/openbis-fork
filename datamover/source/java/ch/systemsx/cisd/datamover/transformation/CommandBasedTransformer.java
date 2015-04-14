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

package ch.systemsx.cisd.datamover.transformation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessIOStrategy;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.string.Template;

/**
 * Implementation of {@link ITransformator} which executes a shell script.
 *
 * @author Franz-Josef Elmer
 */
public class CommandBasedTransformer implements ITransformator
{
    static final String COMMAND_TEMPLATE_PROP = "command-template";
    
    static final String REPLACE_ENVIRONMENT_PROP = "replace-environment";
    
    static final String ENV_PROP_PREFIX = "env.";
    
    private static final String ABSOLUTE_FILE_PATH_PLACE_HOLDER = "absolute-file-path";
    private static final String ABSOLUTE_PARENT_PATH_PLACE_HOLDER = "absolute-parent-path";
    private static final String FILE_NAME_PLACE_HOLDER = "file-name";
    private static final List<String> PLACE_HOLDERS 
            = Arrays.asList(ABSOLUTE_FILE_PATH_PLACE_HOLDER, ABSOLUTE_PARENT_PATH_PLACE_HOLDER, 
                    FILE_NAME_PLACE_HOLDER);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CommandBasedTransformer.class);
    
    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, CommandBasedTransformer.class);
    
    private Template commandTemplate;

    private boolean replaceEnvironment;
    
    private Map<String, String> environment;
    
    public CommandBasedTransformer(Properties properties)
    {
        commandTemplate = new Template(PropertyUtils.getMandatoryProperty(properties, COMMAND_TEMPLATE_PROP));
        Set<String> placeholderNames = commandTemplate.getPlaceholderNames();
        placeholderNames.retainAll(PLACE_HOLDERS);
        if (placeholderNames.isEmpty())
        {
            throw new ConfigurationFailureException("The property '" + COMMAND_TEMPLATE_PROP 
                    + "' should have at least one of the following place holders: " + PLACE_HOLDERS);
        }
        replaceEnvironment = PropertyUtils.getBoolean(properties, REPLACE_ENVIRONMENT_PROP, false);
        Enumeration<?> propertyNames = properties.propertyNames();
        environment = new HashMap<String, String>();
        while (propertyNames.hasMoreElements())
        {
            Object element = propertyNames.nextElement();
            if (element instanceof String)
            {
                String key = (String) element;
                if (key.startsWith(ENV_PROP_PREFIX))
                {
                    environment.put(key.substring(ENV_PROP_PREFIX.length()), properties.getProperty(key));
                }
            }
        }
    }

    @Override
    public Status transform(File path)
    {
        List<String> command = createCommand(path);
        operationLog.info("Execute: " + command);
        ProcessResult processResult = ProcessExecutionHelper.run(command, environment, replaceEnvironment, 
                operationLog, machineLog, ConcurrencyUtilities.NO_TIMEOUT,
                ProcessIOStrategy.DEFAULT_IO_STRATEGY, false);
        processResult.log();
        return processResult.toStatus();
    }
    
    List<String> createCommand(File path)
    {
        Template template = commandTemplate.createFreshCopy();
        template.attemptToBind(ABSOLUTE_FILE_PATH_PLACE_HOLDER, path.getAbsolutePath());
        template.attemptToBind(ABSOLUTE_PARENT_PATH_PLACE_HOLDER, path.getParentFile().getAbsolutePath());
        template.attemptToBind(FILE_NAME_PLACE_HOLDER, path.getName());
        return split(template.createText());
    }
    
    private List<String> split(String command)
    {
        StringBuilder builder = new StringBuilder();
        List<String> result = new ArrayList<String>();
        char quoteChar = 0;
        for (int i = 0, n = command.length(); i < n; i++)
        {
            char c = command.charAt(i);
            if (quoteChar != 0)
            {
                quoteChar = handleInQuotedString(c, result, builder, quoteChar);
            } else if (c == ' ')
            {
                addTo(result, builder);
            } else if ("'\"".contains(Character.toString(c)))
            {
                quoteChar = c;
            } else
            {
                builder.append(c);
            }
        }
        addTo(result, builder);
        return result;
    }

    private char handleInQuotedString(char c, List<String> result, StringBuilder builder, char quoteChar)
    {
        if (c == quoteChar)
        {
            addTo(result, builder);
            return 0;
        } 
        builder.append(c);
        return quoteChar;
    }

    private void addTo(List<String> result, StringBuilder builder)
    {
        if (builder.length() > 0)
        {
            result.add(builder.toString());
            builder.setLength(0);
        }
    }

}
