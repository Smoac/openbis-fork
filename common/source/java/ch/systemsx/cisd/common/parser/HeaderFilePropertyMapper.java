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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * A <code>IPropertyMapper</code> implementation for mapping informations being in the header of a file.
 * 
 * @author Christian Ribeaud
 */
public final class HeaderFilePropertyMapper implements IAliasPropertyMapper
{
    private final Map<String, IPropertyModel> properties;

    private final Map<String, String> aliases;

    public HeaderFilePropertyMapper(final String[] headerTokens) throws IllegalArgumentException
    {
        assert headerTokens != null;
        aliases = new LinkedHashMap<String, String>();
        this.properties = tokensToMap(headerTokens);
    }

    private final static Map<String, IPropertyModel> tokensToMap(final String[] tokens) throws IllegalArgumentException
    {
        final int len = tokens.length;
        final Map<String, IPropertyModel> map = new LinkedHashMap<String, IPropertyModel>(len);
        for (int i = 0; i < len; i++)
        {
            final String token = tokens[i];
            if (StringUtils.isBlank(token))
            {
                throw new IllegalArgumentException(String.format("%s token of %s is blank.", StringUtilities
                        .getOrdinal(i), Arrays.asList(tokens)));
            }
            map.put(token.toLowerCase(), new MappedProperty(i, token));
        }
        return map;
    }

    private final void checkOneToOneRelation(final String aliasName, final String propertyName)
            throws IllegalArgumentException
    {
        // No more than one alias for a given property
        final String propertyLowerCase = propertyName.toLowerCase();
        if (aliases.containsValue(propertyLowerCase))
        {
            throw new IllegalArgumentException("Following alias '" + getPropertyAlias(propertyLowerCase)
                    + "' already exists for property '" + propertyName + "'.");
        }
        final String aliasLowerCase = aliasName.toLowerCase();
        // No alias for two different properties.
        if (aliases.containsKey(aliasLowerCase))
        {
            throw new IllegalArgumentException("Alias name '" + aliasName + "' already specified for property '"
                    + aliases.get(aliasLowerCase) + "'.");
        }
    }

    /**
     * For given <var>property</var> returns the corresponding alias.
     * 
     * @return <code>null</code> if no alias could be found.
     */
    private final String getPropertyAlias(final String propertyName)
    {
        for (Map.Entry<String, String> entry : aliases.entrySet())
        {
            if (entry.getValue().equals(propertyName))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    //
    // IAliasPropertyMapper
    //

    public final void setAlias(final String aliasName, final String propertyName) throws IllegalArgumentException
    {
        assert aliasName != null : "Given alias name can not be null.";
        assert propertyName != null : "Given property name can not be null.";
        final String lowerCase = propertyName.toLowerCase();
        checkOneToOneRelation(aliasName, propertyName);
        aliases.put(aliasName.toLowerCase(), lowerCase);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that the searching is case-insensitive. 'Name' or 'name' as <var>propertName</var> returns the same
     * <code>IPropertyModel</code>.
     * </p>
     */
    public final IPropertyModel getProperty(final String propertyName)
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
        final Set<String> set = new LinkedHashSet<String>();
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

    public final String tryGetPropertyName(final String alias)
    {
        assert alias != null : "Given property name can not be null.";
        return aliases.get(alias.toLowerCase());
    }

}
