/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.console.server;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ConfigParameters
{
    private static final String DATAMOVERS = "datamovers";
    private static final String WORKING_DIRECTORY = ".working-directory";
    
    private final Map<String, String> datamoversWorkingDirectories;

    public ConfigParameters(Properties properties)
    {
        String datamovers = PropertyUtils.getMandatoryProperty(properties, DATAMOVERS);
        StringTokenizer tokenizer = new StringTokenizer(datamovers);
        datamoversWorkingDirectories = new LinkedHashMap<String, String>();
        while (tokenizer.hasMoreElements())
        {
            String datamover = tokenizer.nextToken();
            String key = datamover + WORKING_DIRECTORY;
            String workingDirectory = PropertyUtils.getMandatoryProperty(properties, key);
            datamoversWorkingDirectories.put(datamover, workingDirectory);
        }
        if (datamoversWorkingDirectories.isEmpty())
        {
            throw new ConfigurationFailureException(
                    "Working directory of at least one datamover should be specified.");
        }
    }
    
    public Map<String, String> getDatamoversWorkingDirectories()
    {
        return Collections.unmodifiableMap(datamoversWorkingDirectories);
    }
}
