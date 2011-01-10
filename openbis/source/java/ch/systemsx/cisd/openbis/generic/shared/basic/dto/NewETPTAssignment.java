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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * Stores data needed to create new entity type-property type assignment.
 * 
 * @author Izabela Adamczyk
 */
public class NewETPTAssignment implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private EntityKind entityKind;

    private String propertyTypeCode;

    private String entityTypeCode;

    private boolean mandatory;

    private String defaultValue;

    private String section;

    private Long ordinal;

    // TODO 2011-01-07, Piotr Buczek: use Script DTO here instead of 3 fields
    private String scriptName;

    private boolean dynamic;

    private boolean managed;

    public NewETPTAssignment()
    {
    }

    public NewETPTAssignment(EntityKind entityKind, String propertyTypeCode, String entityTypeCode,
            boolean mandatory, String defaultValue, String section, Long ordinal, boolean dynamic,
            boolean managed, String scriptOrNull)
    {
        this.entityKind = entityKind;
        this.propertyTypeCode = propertyTypeCode;
        this.entityTypeCode = entityTypeCode;
        this.mandatory = mandatory;
        this.defaultValue = defaultValue;
        this.section = section;
        this.ordinal = ordinal;
        this.dynamic = dynamic;
        this.managed = managed;
        this.scriptName = scriptOrNull;
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public void setEntityKind(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public String getPropertyTypeCode()
    {
        return propertyTypeCode;
    }

    public void setPropertyTypeCode(String propertyTypeCode)
    {
        this.propertyTypeCode = propertyTypeCode;
    }

    public String getEntityTypeCode()
    {
        return entityTypeCode;
    }

    public void setEntityTypeCode(String entityTypeCode)
    {
        this.entityTypeCode = entityTypeCode;
    }

    public boolean isMandatory()
    {
        return mandatory;
    }

    public void setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public String getSection()
    {
        return section;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    public Long getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(Long ordinal)
    {
        this.ordinal = ordinal;
    }

    public String getScriptName()
    {
        return scriptName;
    }

    public void setScriptName(String scriptName)
    {
        this.scriptName = scriptName;
    }

    public boolean isDynamic()
    {
        return dynamic;
    }

    public void setDynamic(boolean dynamic)
    {
        this.dynamic = dynamic;
    }

    public boolean isManaged()
    {
        return managed;
    }

    public void setManaged(boolean managed)
    {
        this.managed = managed;
    }

}