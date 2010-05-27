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

import java.io.File;
import java.text.MessageFormat;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * @author Franz-Josef Elmer
 */
class TimeSeriesDataSetUploaderParameters
{
    static final String TIME_SERIES_DATA_SET_DROP_BOX_PATH = "time-series-data-set-drop-box-path";

    static final String EXPERIMENT_CODE_TEMPLATE_KEY = "experiment-code-template";

    static final String DEFAULT_EXPERIMENT_CODE_TEMPLATE = "{0}_{1}_{2}";

    static final String SAMPLE_CODE_TEMPLATE_KEY = "sample-code-template";

    static final String DEFAULT_SAMPLE_CODE_TEMPLATE = "{0}_{1}_{2}";

    static final String SAMPLE_TYPE_CODE_KEY = "sample-type-code";

    static final String DEFAULT_SAMPLE_TYPE_CODE = "TIME_POINT";

    static final String IGNORE_EMPTY_LINES_KEY = "ignore-empty-lines";

    private final MessageFormat experimentCodeFormat;

    private final MessageFormat sampleCodeFormat;

    private final String sampleTypeCode;

    private final boolean ignoreEmptyLines;

    private final File timeSeriesDropBox;

    TimeSeriesDataSetUploaderParameters(Properties properties)
    {
        String timeSeriesDataSetDropBoxPath =
                PropertyUtils.getMandatoryProperty(properties, TIME_SERIES_DATA_SET_DROP_BOX_PATH);
        timeSeriesDropBox = new File(timeSeriesDataSetDropBoxPath);
        if (timeSeriesDropBox.exists() == false)
        {
            throw new ConfigurationFailureException(
                    "Time series data set drop box does not exist: " + timeSeriesDropBox);
        }
        if (timeSeriesDropBox.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Time series data set drop box is not a folder: " + timeSeriesDropBox);
        }
        sampleTypeCode = properties.getProperty(SAMPLE_TYPE_CODE_KEY, DEFAULT_SAMPLE_TYPE_CODE);
        ignoreEmptyLines = PropertyUtils.getBoolean(properties, IGNORE_EMPTY_LINES_KEY, true);
        experimentCodeFormat =
                new MessageFormat(properties.getProperty(EXPERIMENT_CODE_TEMPLATE_KEY,
                        DEFAULT_EXPERIMENT_CODE_TEMPLATE));
        sampleCodeFormat =
                new MessageFormat(properties.getProperty(SAMPLE_CODE_TEMPLATE_KEY,
                        DEFAULT_SAMPLE_CODE_TEMPLATE));
    }
    
    File getTimeSeriesDropBox()
    {
        return timeSeriesDropBox;
    }

    MessageFormat getExperimentCodeFormat()
    {
        return experimentCodeFormat;
    }

    MessageFormat getSampleCodeFormat()
    {
        return sampleCodeFormat;
    }

    String getSampleTypeCode()
    {
        return sampleTypeCode;
    }

    boolean isIgnoreEmptyLines()
    {
        return ignoreEmptyLines;
    }

}
