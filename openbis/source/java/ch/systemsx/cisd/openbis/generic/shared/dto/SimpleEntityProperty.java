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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityPropertyValue;

/**
 * Simple version of EntityProperty which can be expressed in web service definition language<br>
 * Deprecated methods should be used only during bean conversion.
 * 
 * @author Tomasz Pylak
 */
public final class SimpleEntityProperty extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public final static SimpleEntityProperty[] EMPTY_ARRAY = new SimpleEntityProperty[0];

    /** Label of the property, which is shown to the user. */
    private String label;

    private String code;

    private EntityDataType dataType;

    private Serializable value;

    public SimpleEntityProperty()
    {
    }

    public SimpleEntityProperty(final String name, final String label,
            final EntityDataType dataType, final Serializable valueOrNull)
    {
        this.code = name;
        this.label = label;
        this.dataType = dataType;
        this.value = valueOrNull;
    }

    public final String getCode()
    {
        return code;
    }

    public final EntityDataType getDataType()
    {
        return dataType;
    }

    public final Serializable getValue()
    {
        return value;
    }

    // can be null
    public String getUntypedValue()
    {
        return EntityPropertyValue.createFromSimple(this).tryGetUntypedValue();
    }

    public final String getLabel()
    {
        return label;
    }

    public final void setLabel(final String label)
    {
        this.label = label;
    }

    public final void setCode(final String code)
    {
        this.code = code;
    }

    public final void setDataType(final EntityDataType dataType)
    {
        this.dataType = dataType;
    }

    public final void setValue(final Serializable value)
    {
        this.value = value;
    }

    public final void setValue(final String value)
    {
        this.value = value;
    }

}
