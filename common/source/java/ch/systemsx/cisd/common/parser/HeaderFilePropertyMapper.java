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
 * A <code>IPropertyMapper</code> implementation for mapping informations being in the header of a file.
 * 
 * @author Christian Ribeaud
 */
public final class HeaderFilePropertyMapper implements IAliasPropertyMapper
{
    private final Map<String, IPropertyModel> properties;

    private final Map<String, String> aliases;

    public HeaderFilePropertyMapper(String[] headerTokens)
    {
        assert headerTokens != null;
        aliases = new HashMap<String, String>();
        this.properties = tokensToMap(headerTokens);
    }

    private final static Map<String, IPropertyModel> tokensToMap(String[] tokens)
    {
        final Map<String, IPropertyModel> map = new HashMap<String, IPropertyModel>(tokens.length);
        for (int i = 0; i < tokens.length; i++)
        {
            final String token = tokens[i];
            if (token != null)
            {
                map.put(token, new MappedProperty(i, token));
            }
        }
        return map;
    }

    //
    // IAliasPropertyMapper
    //

    public final void setAlias(String aliasName, String propertyName)
    {
        aliases.put(aliasName, propertyName);
    }

    public final IPropertyModel getProperty(String propertyName)
    {
        // <code>propertyName</code> could be an alias.
        IPropertyModel propertyModel = properties.get(propertyName);
        if (propertyModel == null)
        {
            String realPropertyName = aliases.get(propertyName);
            if (realPropertyName != null)
            {
                propertyModel = properties.get(realPropertyName);
            }
            
        }
        return propertyModel;
    }

}
