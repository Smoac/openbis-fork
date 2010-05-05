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

package ch.systemsx.cisd.openbis.knime.query;

import java.io.Serializable;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;

enum ColumnType
{
    DOUBLE(DoubleCell.TYPE)
    {
        @Override
        public DataCell createCell(Serializable valueOrNull)
        {
            if (valueOrNull instanceof Double)
            {
                return new DoubleCell((Double) valueOrNull);
            }
            return DataType.getMissingCell();
        }
    },
    LONG(LongCell.TYPE)
    {
        @Override
        public DataCell createCell(Serializable valueOrNull)
        {
            if (valueOrNull instanceof Long)
            {
                return new LongCell((Long) valueOrNull);
            }
            return DataType.getMissingCell();
        }
    },
    STRING(StringCell.TYPE)
    {

        @Override
        public DataCell createCell(Serializable valueOrNull)
        {
            return new StringCell(valueOrNull == null ? "" : valueOrNull.toString());
        }
    };

    private final DataType dataType;

    private ColumnType(DataType dataType)
    {
        this.dataType = dataType;
    }
    
    public DataType getDataType()
    {
        return dataType;
    }
    
    public abstract DataCell createCell(Serializable valueOrNull);
}