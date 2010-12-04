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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Franz-Josef Elmer
 */
public class SampleWithPropertiesAndAbundance implements ISerializable,
        IEntityInformationHolderWithPermId, IEntityPropertiesHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    private String permId;

    private String identifier;

    private String code;

    private SampleType sampleType;

    private List<IEntityProperty> properties;

    private double abundance;

    public final Long getId()
    {
        return id;
    }

    public final void setId(Long id)
    {
        this.id = id;
    }

    public final String getIdentifier()
    {
        return identifier;
    }

    public final void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public final String getCode()
    {
        return code;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    public EntityType getEntityType()
    {
        return getSampleType();
    }

    public EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(final SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    public final void setProperties(List<IEntityProperty> properties)
    {
        this.properties = properties;
    }

    public final double getAbundance()
    {
        return abundance;
    }

    public final void setAbundance(double abundance)
    {
        this.abundance = abundance;
    }

    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

}
