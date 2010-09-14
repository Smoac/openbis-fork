/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.dss;

import java.text.MessageFormat;
import java.util.Properties;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * @author Franz-Josef Elmer
 */
class TimeSeriesDataSetUploaderParameters
{
    static final String DATA_SET_TYPE_PATTERN_FOR_DEFAULT_HANDLING = "data-set-type-pattern-for-default-handling";

    static final String EXPERIMENT_CODE_TEMPLATE_KEY = "experiment-code-template";

    static final String DEFAULT_EXPERIMENT_CODE_TEMPLATE = "{0}_{1}_{2}";

    static final String IGNORE_EMPTY_LINES_KEY = "ignore-empty-lines";

    private final MessageFormat experimentCodeFormat;

    private final boolean ignoreEmptyLines;

    private final Pattern patternForDefaultHandling;

    TimeSeriesDataSetUploaderParameters(Properties properties)
    {
        patternForDefaultHandling =
                Pattern.compile(PropertyUtils.getMandatoryProperty(properties,
                        DATA_SET_TYPE_PATTERN_FOR_DEFAULT_HANDLING));
        ignoreEmptyLines = PropertyUtils.getBoolean(properties, IGNORE_EMPTY_LINES_KEY, false);
        experimentCodeFormat =
                new MessageFormat(properties.getProperty(EXPERIMENT_CODE_TEMPLATE_KEY,
                        DEFAULT_EXPERIMENT_CODE_TEMPLATE));
    }
    
    MessageFormat getExperimentCodeFormat()
    {
        return experimentCodeFormat;
    }

    boolean isIgnoreEmptyLines()
    {
        return ignoreEmptyLines;
    }

    Pattern getPatternForDefaultHandling()
    {
        return patternForDefaultHandling;
    }

}
