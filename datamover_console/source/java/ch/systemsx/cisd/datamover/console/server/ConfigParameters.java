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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * Confuguration parameters.
 *
 * @author Franz-Josef Elmer
 */
public class ConfigParameters
{
    @Private static final String REFRESH_TIME_INTERVAL = "refresh-time-interval";
    @Private static final String TARGETS = "targets";
    @Private static final String LOCATION = "location";
    @Private static final String DATAMOVERS = "datamovers";
    @Private static final String WORKING_DIRECTORY = "working-directory";
    
    private final Map<String, String> targets;
    private final Map<String, String> datamoversWorkingDirectories;
    private final int refreshTimeInterval;

    /**
     * Creates an instance based on the specified properties.
     */
    public ConfigParameters(Properties properties)
    {
        refreshTimeInterval = PropertyUtils.getInt(properties, REFRESH_TIME_INTERVAL, 60) * 1000;
        targets = obtainMapFrom(properties, TARGETS, LOCATION);
        if (targets.isEmpty())
        {
            throw new ConfigurationFailureException("At least one target should be specified.");
        }
        datamoversWorkingDirectories = obtainMapFrom(properties, DATAMOVERS, WORKING_DIRECTORY);
        if (datamoversWorkingDirectories.isEmpty())
        {
            throw new ConfigurationFailureException("At least one datamover should be specified.");
        }
    }
    
    public final int getRefreshTimeInterval()
    {
        return refreshTimeInterval;
    }

    private Map<String, String> obtainMapFrom(Properties properties, String name, String valueName)
    {
        String keys = PropertyUtils.getMandatoryProperty(properties, name);
        StringTokenizer tokenizer = new StringTokenizer(keys);
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        while (tokenizer.hasMoreTokens())
        {
            String key = tokenizer.nextToken();
            map.put(key, PropertyUtils.getMandatoryProperty(properties, key + "." + valueName));
        }
        return map;
    }

    /**
     * Returns the map of symbolic target names to target locations.
     */
    public final Map<String, String> getTargets()
    {
        return targets;
    }

    /**
     * Returns the map of datamover names to datamover working directories.
     */
    public Map<String, String> getDatamoversWorkingDirectories()
    {
        return datamoversWorkingDirectories;
    }
}
