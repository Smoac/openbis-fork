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
import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.PropertiesHelper;

/**
 * Specification of one entity property (name, type, is mandatory).
 * 
 * @author Tomasz Pylak
 */
public final class EntityPropertyTypeDTO extends Id implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private boolean mandatory;

    private Date registrationDate;

    private PersonPE registrator;

    private PropertyTypePE propertyType;

    public EntityPropertyTypeDTO()
    {
    }

    public EntityPropertyTypeDTO(final long id, final boolean mandatory,
            final Date registrationDate, final PersonPE registrator,
            final PropertyTypePE propertyType)
    {
        setId(id);
        this.mandatory = mandatory;
        this.registrationDate = registrationDate;
        this.registrator = registrator;
        this.propertyType = propertyType;
    }

    public final EntityDataType getDataTypeCode()
    {
        return EntityDataType.valueOf(PropertiesHelper.getSimpleType(propertyType));
    }

    public final boolean isMandatory()
    {
        return mandatory;
    }

    public final void setMandatory(final boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    public final Date getRegistrationDate()
    {
        return registrationDate;
    }

    public final void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public final PersonPE getRegistrator()
    {
        return registrator;
    }

    public final void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
    }

    public final PropertyTypePE getPropertyType()
    {
        return propertyType;
    }

    public final void setPropertyType(final PropertyTypePE propertyType)
    {
        this.propertyType = propertyType;
    }

}