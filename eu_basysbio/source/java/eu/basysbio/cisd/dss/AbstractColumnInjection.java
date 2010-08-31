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

package eu.basysbio.cisd.dss;

import ch.systemsx.cisd.etlserver.utils.Column;

abstract class AbstractColumnInjection<T extends AbstractDataValue> implements IColumnInjection<T>
{
    protected final Column column;
    AbstractColumnInjection(Column column)
    {
        this.column = column;
    }
    
    public void inject(T dataValue, int rowIndex)
    {
        String value = column.getValues().get(rowIndex);
        try
        {
            inject(dataValue, value);
        } catch (Exception ex)
        {
            throw new IllegalArgumentException("Column '" + column.getHeader()
                    + "' has an invalid value in row " + (rowIndex + 2) + ": " + value, ex);
        }
    }
    
    abstract void inject(T dataValue, String value);
}