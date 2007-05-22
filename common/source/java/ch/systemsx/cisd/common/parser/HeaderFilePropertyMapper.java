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

/**
 * A <code>IPropertyMapper</code> implementation for mapping informations being in the header of a given file.
 * <p>
 * The parser already parsed the header and give us a <code>String</code> array.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class HeaderFilePropertyMapper implements IPropertyMapper
{
    private final Map<String, IPropertyModel> properties;
    
    HeaderFilePropertyMapper(String[] headerTokens) {
        this.properties = tokensToMap(headerTokens);
    }
    
    private final static Map<String, IPropertyModel> tokensToMap(String[] tokens)
    {
        Map<String, IPropertyModel> map = new HashMap<String, IPropertyModel>(tokens.length);
        for (int i = 0; i < tokens.length; i++)
        {
            String token = tokens[i];
            map.put(token, new MappedProperty(i, token));
        }
        return map;
    }
    
    ///////////////////////////////////////////////////////
    // IPropertyMapper
    ///////////////////////////////////////////////////////

    public final IPropertyModel getProperty(String name)
    {
        return properties.get(name);
    }

}
