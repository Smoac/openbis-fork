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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                map.put(token.toLowerCase(), new MappedProperty(i, token));
            }
        }
        return map;
    }

    private final void checkOneToOneRelation(String aliasName, String propertyName)
    {
        // No more than one alias for a given property
        if (aliases.containsValue(propertyName))
        {
            throw new IllegalArgumentException("Following alias '" + getPropertyAlias(propertyName)
                    + "' already exists for property '" + propertyName + "'.");
        }
        String alias = aliasName.toLowerCase();
        // No alias for two different properties.
        if (aliases.containsKey(alias))
        {
            throw new IllegalArgumentException("Alias name '" + aliasName + "' already specified for property '"
                    + aliases.get(alias) + "'.");
        }
    }

    /**
     * For given <var>property</var> returns the corresponding alias.
     * 
     * @return <code>null</code> if no alias could be found.
     */
    private final String getPropertyAlias(String property)
    {
        for (Map.Entry<String, String> entry : aliases.entrySet())
        {
            if (entry.getValue().equals(property))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    //
    // IAliasPropertyMapper
    //

    public final void setAlias(String aliasName, String propertyName)
    {
        assert aliasName != null;
        assert propertyName != null;
        checkOneToOneRelation(aliasName, propertyName);
        aliases.put(aliasName.toLowerCase(), propertyName.toLowerCase());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that the searching is case-insensitive. 'Name' or 'name' as <var>propertName</var> returns the same
     * <code>IPropertyModel</code>.
     * </p>
     */
    public final IPropertyModel getProperty(String propertyName)
    {
        String property = propertyName.toLowerCase();
        // Given <code>propertyName</code> could be an alias.
        IPropertyModel propertyModel = properties.get(property);
        if (propertyModel == null)
        {
            property = aliases.get(property);
            if (property != null)
            {
                propertyModel = properties.get(property);
            }

        }
        return propertyModel;
    }

    /**
     * {@inheritDoc}
     * 
     * @return a <code>Set</code> of all properties in <b>lower case</b>. If an alias has been found for a given
     *         property, then this alias will be added to the returned <code>Set</code> instead of the original
     *         property.
     */
    public final Set<String> getAllPropertyNames()
    {
        Set<String> set = new HashSet<String>();
        for (String property : properties.keySet())
        {
            if (aliases.containsValue(property))
            {
                set.add(getPropertyAlias(property));
            } else
            {
                set.add(property);
            }
        }
        return set;
    }
}
