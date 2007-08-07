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

import java.util.Properties;
import java.util.Set;

/**
 * Useful utility functions concerning configuration data. 
 *
 * @author Franz-Josef Elmer
 */
public class ConfigUtilities
{
    /**
     * Extracts from the specified properties object all properties starting with the specified key prefix.
     */
    public static Properties extractPropertiesStartingWith(String prefix, Properties properties)
    {
        assert prefix != null : "Missing prefix";
        assert properties != null : "Missing properties";
        
        Properties result = new Properties();
        int prefixLength = prefix.length();
        Set<Object> keys = properties.keySet();
        for (Object object : keys)
        {
            String key = object.toString();
            if (key.startsWith(prefix))
            {
                result.setProperty(key.substring(prefixLength), properties.getProperty(key));
            }
        }
        return result;
    }
    
    private ConfigUtilities()
    {
    }
    
}
