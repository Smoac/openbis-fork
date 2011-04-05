/*
 * Copyright 2011 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Sample related to a protein. Contains information about abundance and amino-acid modification.
 *
 * @author Franz-Josef Elmer
 */
public class ProteinRelatedSample implements ISerializable, IEntityInformationHolderWithPermId
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;
    
    private Long id;
    
    private String permId;
    
    private String code;
    
    private String identifier;
    
    private BasicEntityType entityType;
    
    private Double abundance;
    
    private Long modificationPosition;
    
    private Double modificationMass;
    
    private Double modificationFraction;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public BasicEntityType getEntityType()
    {
        return entityType;
    }

    public void setEntityType(BasicEntityType entityType)
    {
        this.entityType = entityType;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public Double getAbundance()
    {
        return abundance;
    }

    public void setAbundance(Double abundance)
    {
        this.abundance = abundance;
    }

    public Long getModificationPosition()
    {
        return modificationPosition;
    }

    public void setModificationPosition(Long modificationPosition)
    {
        this.modificationPosition = modificationPosition;
    }

    public Double getModificationMass()
    {
        return modificationMass;
    }

    public void setModificationMass(Double modificationMass)
    {
        this.modificationMass = modificationMass;
    }

    public Double getModificationFraction()
    {
        return modificationFraction;
    }

    public void setModificationFraction(Double modificationFraction)
    {
        this.modificationFraction = modificationFraction;
    }

    public EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

}
