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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Franz-Josef Elmer
 */
public class Treatment implements ISerializable, Comparable<Treatment>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String value;

    private String type;

    private String typeCode;

    private String valueType;

    public final String getValue()
    {
        return value;
    }

    public final void setValue(String value)
    {
        this.value = value;
    }

    public final String getValueType()
    {
        return valueType;
    }

    public final void setValueType(String valueType)
    {
        this.valueType = valueType;
    }

    public final String getType()
    {
        return type;
    }

    public final void setType(String type)
    {
        this.type = type;
    }

    public final String getTypeCode()
    {
        return typeCode;
    }

    public final void setTypeCode(String typeCode)
    {
        this.typeCode = typeCode;
    }

    public final String getLabel()
    {
        return value + " " + type;
    }

    public int compareTo(Treatment that)
    {
        int typeComparisonResult = this.type.toLowerCase().compareTo(that.type.toLowerCase());
        if (typeComparisonResult != 0)
        {
            return typeComparisonResult;
        }
        double thisNumber = convert(this.value);
        double thatNumber = convert(that.value);
        if (Double.isNaN(thisNumber) || Double.isNaN(thatNumber))
        {
            return this.value.compareTo(that.value);
        }
        return Double.compare(thisNumber, thatNumber);
    }

    private double convert(String numberOrString)
    {
        try
        {
            return Double.parseDouble(numberOrString);
        } catch (NumberFormatException e)
        {
            return Double.NaN;
        }
    }

    @Override
    public String toString()
    {
        return getLabel();
    }

}
