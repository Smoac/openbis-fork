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

package ch.systemsx.cisd.etlserver;

import java.util.Properties;

import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * @author Izabela Adamczyk
 */
public class MaintenanceTaskParameters
{
    private static final int ONE_DAY_IN_SEC = 60 * 60 * 24;

    private static final String CLASS_KEY = "class";

    private static final String INTERVAL_KEY = "interval";

    private final String pluginName;

    private final long interval;

    private final String className;

    private final Properties properties;

    public MaintenanceTaskParameters(Properties properties, String pluginName)
    {
        this.properties = properties;
        this.pluginName = pluginName;
        interval = PropertyUtils.getLong(properties, INTERVAL_KEY, ONE_DAY_IN_SEC);
        className = PropertyUtils.getMandatoryProperty(properties, CLASS_KEY);
    }

    public long getIntervalSeconds()
    {
        return interval;
    }

    public String getClassName()
    {
        return className;
    }

    public String getPluginName()
    {
        return pluginName;
    }

    public Properties getProperties()
    {
        return properties;
    }
}
