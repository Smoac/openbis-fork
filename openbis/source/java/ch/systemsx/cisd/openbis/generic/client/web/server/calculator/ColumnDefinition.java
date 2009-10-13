/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * Definition of column used in jython. 
 * <p>
 * All public methods of this class are part of the Filter/Calculated Column API.
 * 
 * @author Franz-Josef Elmer
 */
class ColumnDefinition
{
    private final IColumnDefinition<?> definition;

    ColumnDefinition(IColumnDefinition<?> definition)
    {
        this.definition = definition;
    }
    
    /**
     * Returns the ID of the column.
     */
    public String id()
    {
        return definition.getIdentifier();
    }
    
    /**
     * Returns the value of property specified by the key.
     * 
     * @return <code>null</code> if no property found.
     */
    public String property(String key)
    {
        return definition.tryToGetProperty(key);
    }

}
