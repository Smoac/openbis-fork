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

import java.util.Set;

/**
 * A <code>IPropertyMapper</code> extension that allows you to define an alias for a given property name.
 * 
 * @author Christian Ribeaud
 */
public interface IAliasPropertyMapper extends IPropertyMapper
{
    /**
     * Sets an alias for given <var>propertyName</var>. Note that it does not check whether a property with the name
     * <var>propertyName</var> could be found because, for optional properties, this is not sure that we will find them
     * in the header.
     * <p>
     * Only one alias for a given property name can be specified and it should not be possible to set the same alias for
     * two different properties.
     * </p>
     * <p>
     * Alias replaces the original property name in <code>Set</code> returned by
     * {@link IPropertyMapper#getAllPropertyNames()}.
     * </p>
     * 
     * @param aliasName can not be <code>null</code>. The is the property name found in the bean.
     * @param propertyName can not be <code>null</code>. This is the property name found in the parsed file.
     */
    public void setAliasForPropertyName(final String aliasName, final String propertyName)
            throws IllegalArgumentException;

    /**
     * Returns the property name (in parsed file) that has been specified for given alias (<i>Bean</i> properties).
     * 
     * @throws IllegalArgumentException if given <var>alias</var> does not exist.
     */
    public String getPropertyNameForAlias(final String alias) throws IllegalArgumentException;

    /**
     * Returns all aliases that have been set.
     * 
     * @return never <code>null</code> but could return an empty set.
     */
    public Set<String> getAllAliases();
}
