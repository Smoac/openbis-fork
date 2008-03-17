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

package ch.systemsx.cisd.common.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A default <code>IAliasPropertyMapper</code> implementation.
 * 
 * @author Christian Ribeaud
 */
// TODO 2008-02-17, Tomasz Pylak: this class should be removed (we do not need or want field aliases)
public class DefaultAliasPropertyMapper extends DefaultPropertyMapper implements IAliasPropertyMapper
{
    private final Map<String, String> aliasToPropertyMappings;

    public DefaultAliasPropertyMapper(final String[] properties)
    {
        super(properties);
        aliasToPropertyMappings = new LinkedHashMap<String, String>();
    }

    private final void checkOneToOneRelation(final String aliasName, final String propertyName)
            throws IllegalArgumentException
    {
        // No two aliases for a certain property name.
        final String alias = tryGetAliasForProperty(propertyName);
        if (alias != null)
        {
            throw new IllegalArgumentException("Following property name '" + propertyName + "' has already alias '"
                    + alias + "' registered.");
        }
        // No alias for two different properties.
        final String property = aliasToPropertyMappings.get(aliasName);
        if (aliasToPropertyMappings.containsKey(aliasName))
        {
            throw new IllegalArgumentException("Alias name '" + aliasName + "' already specified for property '"
                    + property + "'.");
        }
    }

    /**
     * For given <var>propertyName</var> returns the corresponding alias.
     * 
     * @return <code>null</code> if no alias could be found.
     */
    private final String tryGetAliasForProperty(final String propertyName)
    {
        for (final Map.Entry<String, String> entry : aliasToPropertyMappings.entrySet())
        {
            if (entry.getValue().equals(propertyName))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * @param propertyName could be an alias. If so returns corresponding property name.
     */
    private final String getRealPropertyName(final String propertyName)
    {
        String propertyNameInLowerCase = propertyName.toLowerCase();
        final String realPropertyName = aliasToPropertyMappings.get(propertyNameInLowerCase);
        return realPropertyName == null ? propertyNameInLowerCase : realPropertyName;
    }

    //
    // IAliasPropertyMapper
    //

    public final String getPropertyNameForAlias(final String alias) throws IllegalArgumentException
    {
        assert alias != null : "Unspecified alias.";
        String aliasInLowerCase = alias.toLowerCase();
        if (aliasToPropertyMappings.containsKey(aliasInLowerCase) == false)
        {
            throw new IllegalArgumentException(String.format("Unknown alias '%s'.", alias));
        }
        return aliasToPropertyMappings.get(aliasInLowerCase);
    }

    public final void setAliasForPropertyName(final String aliasName, final String propertyName)
            throws IllegalArgumentException
    {
        assert aliasName != null : "Given alias name can not be null.";
        assert propertyName != null : "Given property name can not be null.";
        String aliasNameInLowerCase = aliasName.toLowerCase();
        String propertyNameInLowerCase = propertyName.toLowerCase();
        checkOneToOneRelation(aliasNameInLowerCase, propertyNameInLowerCase);
        aliasToPropertyMappings.put(aliasNameInLowerCase, propertyNameInLowerCase);
    }

    public final Set<String> getAllAliases()
    {
        return new TreeSet<String>(aliasToPropertyMappings.keySet());
    }

    //
    // DefaultPropertyMapper
    //

    @Override
    public final boolean containsPropertyName(final String propertyName)
    {
        return super.containsPropertyName(getRealPropertyName(propertyName));
    }

    /**
     * Returns property names replaced with aliases that are really backed up by property names (alias can be specified
     * for a non-existing property name).
     */
    @Override
    public final Set<String> getAllPropertyNames()
    {
        final Set<String> propertyNames = new TreeSet<String>();
        for (final String propertyName : super.getAllPropertyNames())
        {
            final String alias = tryGetAliasForProperty(propertyName);
            if (alias != null)
            {
                propertyNames.add(alias);
            } else
            {
                propertyNames.add(propertyName);
            }
        }
        return propertyNames;
    }

    @Override
    public final IPropertyModel getPropertyModel(final String propertyName) throws IllegalArgumentException
    {
        return super.getPropertyModel(getRealPropertyName(propertyName));
    }
}
