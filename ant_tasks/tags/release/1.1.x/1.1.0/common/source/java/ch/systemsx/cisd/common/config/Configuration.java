/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.config;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Provider of configuration data.
 *
 * @author Franz-Josef Elmer
 */
public class Configuration
{
    private final IConfigurationDataProvider configuration;

    public Configuration(IConfigurationDataProvider configuration)
    {
        this.configuration = configuration;
    }
    
    /**
     * Returns the specified string property.
     * 
     * @param key The key of the requested property. Has to be a non-empty string.
     * @throws EnvironmentFailureException if property not found.
     */
    public String getStringProperty(String key)
    {
        String property = configuration.getProperty(key);
        if (property == null)
        {
            throw new EnvironmentFailureException("Undefined property '" + key + "'.");
        }
        return property;
    }
    
    /**
     * Returns the specified string property or the specified default value if not found.
     * 
     * @param key The key of the requested property. Has to be a non-empty string.
     */
    public String getStringProperty(String key, String defaultValue)
    {
        String property = configuration.getProperty(key);
        return property == null ? defaultValue : property;
    }
    
    
    
}
