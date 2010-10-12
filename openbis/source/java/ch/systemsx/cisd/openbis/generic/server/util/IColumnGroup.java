/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.util;

import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;

/**
 *  Interface of a fluent API for building groups of columns in a {@link TypedTableModel}.
 *
 * @author Franz-Josef Elmer
 */
public interface IColumnGroup
{
    /**
     * Gets or creates a column with specified identifier.
     */
    public IColumn column(String id);
    
    /**
     * Adds all specified properties. The group name is used as a prefix which combined with the property
     * type code to determine the column id. The property type label is used as column title.
     */
    public void addProperties(Collection<IEntityProperty> properties);
    
    /**
     * Adds all specified properties. The specified identifier prefix is combined with the property
     * type code to determine the column id. The property type label is used as column title.
     */
    public void addProperties(String idPrefix, Collection<IEntityProperty> properties);
}
