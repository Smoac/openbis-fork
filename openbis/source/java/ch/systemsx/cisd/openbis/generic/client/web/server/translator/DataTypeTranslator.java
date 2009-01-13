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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import ch.systemsx.cisd.openbis.generic.client.shared.DataType;
import ch.systemsx.cisd.openbis.generic.client.shared.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;

/**
 * A {@link DataType} &lt;---&gt; {@link DataTypePE} translator.
 * 
 * @author Izabela Adamczyk
 */
public class DataTypeTranslator
{
    private DataTypeTranslator()
    {
        // Can not be instantiated.
    }

    public static DataType translate(final DataTypePE dataTypePE)
    {
        final DataType result = new DataType();
        result.setCode(translate(dataTypePE.getCode()));
        result.setDescription(dataTypePE.getDescription());
        return result;
    }

    public static EntityDataType translate(final DataType dataType)
    {
        switch (dataType.getCode())
        {
            case BOOLEAN:
                return EntityDataType.BOOLEAN;
            case CONTROLLEDVOCABULARY:
                return EntityDataType.CONTROLLEDVOCABULARY;
            case INTEGER:
                return EntityDataType.INTEGER;
            case REAL:
                return EntityDataType.REAL;
            case TIMESTAMP:
                return EntityDataType.TIMESTAMP;
            default:
                return EntityDataType.VARCHAR;
        }
    }

    private static DataTypeCode translate(EntityDataType edt)
    {
        switch (edt)
        {
            case BOOLEAN:
                return DataTypeCode.BOOLEAN;
            case CONTROLLEDVOCABULARY:
                return DataTypeCode.CONTROLLEDVOCABULARY;
            case INTEGER:
                return DataTypeCode.INTEGER;
            case REAL:
                return DataTypeCode.REAL;
            case TIMESTAMP:
                return DataTypeCode.TIMESTAMP;
            default:
                return DataTypeCode.VARCHAR;

        }
    }
}
