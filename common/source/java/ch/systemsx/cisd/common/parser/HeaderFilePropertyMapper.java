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

package ch.systemsx.cisd.common.parser;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * 
 *
 * @author Christian Ribeaud
 */
public class HeaderFilePropertyMapper implements IPropertyMapper
{
    public final static char NAME_FORMAT_SEPARATOR = ',';
    
    private final Map<String, Property> properties;
    
    HeaderFilePropertyMapper(String[] headerTokens) {
        this.properties = tokensToMap(headerTokens);
    }
    
    private final static Map<String, Property> tokensToMap(String[] tokens)
    {
        Map<String, Property> map = new HashMap<String, Property>(tokens.length);
        for (int i = 0; i < tokens.length; i++)
        {
            String token = tokens[i];
            String[] split = StringUtils.split(token, NAME_FORMAT_SEPARATOR);
            String format = null;
            if (split.length > 1)
            {
                format = split[1];
            }
            map.put(token, new Property(i, split[0], format));
        }
        return map;
    }
    
    
    ///////////////////////////////////////////////////////
    // IPropertyMapper
    ///////////////////////////////////////////////////////

    public Property getProperty(String name)
    {
        return properties.get(name);
    }

}
