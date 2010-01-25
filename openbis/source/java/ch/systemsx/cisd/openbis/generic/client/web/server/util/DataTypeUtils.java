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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

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
        
        INTEGER(DataTypeCode.INTEGER){
            @Override
            public ISerializableComparable doConvertion(String value)
            {
                long number;
                try
                {
                    number = Long.parseLong(value);
                } catch (NumberFormatException ex)
                {
                    throw new IllegalArgumentException("Is not an integer number: " + value);
                }
                return new IntegerTableCell(number);
            }
        },
        DOUBLE(DataTypeCode.REAL){
            @Override
            public ISerializableComparable doConvertion(String value)
            {
                double number;
                try
                {
                    number = Double.parseDouble(value);
                } catch (NumberFormatException ex)
                {
                    throw new IllegalArgumentException("Is not a floating point number: " + value);
                }
                return new DoubleTableCell(number);
            }
        },
        DATE(DataTypeCode.TIMESTAMP){
            @Override
            public ISerializableComparable doConvertion(String value)
            {
                return new StringTableCell(value);
            }
        },
        STRING(DataTypeCode.VARCHAR, DataTypeCode.MULTILINE_VARCHAR, DataTypeCode.BOOLEAN,
                DataTypeCode.CONTROLLEDVOCABULARY, DataTypeCode.MATERIAL, DataTypeCode.HYPERLINK)
        {
            @Override
            public ISerializableComparable doConvertion(String value)
            {
                return new StringTableCell(value);
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
                return EMPTY_CELL ;
            }
            return doConvertion(value);
        }
        public abstract ISerializableComparable doConvertion(String value);
    }
    
    /**
     * Converts the specified string value into a data value in accordance with specified
     * data type.
     */
    public static ISerializableComparable convertTo(DataTypeCode dataTypeCode, String value)
    {
        return Converter.resolve(dataTypeCode).convert(value);
    }
    
    private DataTypeUtils()
    {
    }
}
