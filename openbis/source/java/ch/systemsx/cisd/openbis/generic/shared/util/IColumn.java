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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;

/**
 *  Interface of a fluent API for building columns in a {@link TypedTableModel}.
 *
 * @author Franz-Josef Elmer
 */
public interface IColumn
{
    /**
     * Sets the column title.
     */
    public IColumn withTitle(String title);
    
    /**
     * Sets the default column width.
     */
    public IColumn withDefaultWidth(int width);

    /**
     * Sets the data type.
     */
    public IColumn withDataType(DataTypeCode dataType);
    
    /**
     * Adds a value.
     */
    public void addValue(ISerializableComparable valueOrNull);
    
    /**
     * Adds a string value to the column. 
     */
    public void addString(String valueOrNull);
    
    /**
     * Adds an integer value to the column.
     */
    public void addInteger(Long valueOrNull);
    
    /**
     * Adds a double value to the column.
     */
    public void addDouble(Double valueOrNull);
    
    /**
     * Adds a date value to the column.
     */
    public void addDate(Date valueOrNull);
    
    /**
     * Adds a person to the column as a string.
     */
    public void addPerson(Person personOrNull);
}
