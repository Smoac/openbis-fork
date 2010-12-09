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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;

/**
 * Utility functions around data types.
 * 
 * @author Franz-Josef Elmer
 */
public class DataTypeUtils
{
    private static final Map<DataTypeCode, Converter> map = new HashMap<DataTypeCode, Converter>();

    private enum Converter implements IsSerializable
    {

        INTEGER(DataTypeCode.INTEGER)
        {
            @Override
            public ISerializableComparable doConversion(String value)
            {
                return new IntegerTableCell(doSimpleConversion(value));
            }

            @Override
            public Long doSimpleConversion(String value)
            {
                try
                {
                    return new Long(value);
                } catch (NumberFormatException ex)
                {
                    throw new IllegalArgumentException("Is not an integer number: " + value);
                }
            }
        },
        DOUBLE(DataTypeCode.REAL)
        {
            @Override
            public ISerializableComparable doConversion(String value)
            {
                return new DoubleTableCell(doSimpleConversion(value));
            }

            @Override
            public Double doSimpleConversion(String value)
            {
                try
                {
                    return new Double(value);
                } catch (NumberFormatException ex)
                {
                    throw new IllegalArgumentException("Is not a floating point number: " + value);
                }
            }
        },
        DATE(DataTypeCode.TIMESTAMP)
        {
            @Override
            public ISerializableComparable doConversion(String value)
            {
                return new StringTableCell(value);
            }

            @Override
            public Serializable doSimpleConversion(String value)
            {
                return value;
            }
        },
        STRING(DataTypeCode.VARCHAR, DataTypeCode.MULTILINE_VARCHAR, DataTypeCode.BOOLEAN, DataTypeCode.XML,
                DataTypeCode.CONTROLLEDVOCABULARY, DataTypeCode.MATERIAL, DataTypeCode.HYPERLINK)
        {
            @Override
            public ISerializableComparable doConversion(String value)
            {
                return new StringTableCell(value);
            }
            
            @Override
            public Serializable doSimpleConversion(String value)
            {
                return value;
            }
        },
        ;

        private static final StringTableCell EMPTY_CELL = new StringTableCell("");

        private Converter(DataTypeCode... codes)
        {
            for (DataTypeCode dataTypeCode : codes)
            {
                map.put(dataTypeCode, this);
            }
        }

        public static Converter resolve(DataTypeCode dataTypeCode)
        {
            return map.get(dataTypeCode);
        }

        public ISerializableComparable convert(String value)
        {
            if (StringUtils.isBlank(value))
            {
                return EMPTY_CELL;
            }
            if (value.startsWith(BasicConstant.ERROR_PROPERTY_PREFIX))
            {
                return new StringTableCell(value.substring(1));
            }
            return doConversion(value);
        }
        
        public Serializable convertValue(String value)
        {
            return StringUtils.isBlank(value) ? null : doSimpleConversion(value);
        }

        protected abstract ISerializableComparable doConversion(String value);
        protected abstract Serializable doSimpleConversion(String value);
    }

    /**
     * Converts the specified string value into a data value in accordance with specified data type.
     */
    public static ISerializableComparable convertTo(DataTypeCode dataTypeCode, String value)
    {
        return Converter.resolve(dataTypeCode).convert(value);
    }

    
    /**
     * Converts the specified string value into a data value in accordance with specified data type.
     */
    public static Serializable convertValueTo(DataTypeCode dataTypeCode, String value)
    {
        return Converter.resolve(dataTypeCode).convertValue(value);
    }
    
    /**
     * Returns a data type which is compatible with the previous data type and the new data type.
     */
    public static DataTypeCode getCompatibleDataType(DataTypeCode previousDataTypeOrNull,
            DataTypeCode dataType)
    {
        if (previousDataTypeOrNull == null)
        {
            return dataType;
        }
        if (dataType == null)
        {
            return previousDataTypeOrNull;
        }
        if (previousDataTypeOrNull == DataTypeCode.REAL)
        {
            if (dataType == DataTypeCode.REAL || dataType == DataTypeCode.INTEGER)
            {
                return DataTypeCode.REAL;
            }
            return DataTypeCode.VARCHAR;
        }
        if (previousDataTypeOrNull == DataTypeCode.INTEGER)
        {
            if (dataType == DataTypeCode.REAL)
            {
                return DataTypeCode.REAL;
            }
            if (dataType == DataTypeCode.INTEGER)
            {
                return DataTypeCode.INTEGER;
            }
            return DataTypeCode.VARCHAR;
        }
        if (previousDataTypeOrNull == DataTypeCode.TIMESTAMP && dataType == DataTypeCode.TIMESTAMP)
        {
            return DataTypeCode.TIMESTAMP;
        }
        return DataTypeCode.VARCHAR;
    }

    private DataTypeUtils()
    {
    }
}
