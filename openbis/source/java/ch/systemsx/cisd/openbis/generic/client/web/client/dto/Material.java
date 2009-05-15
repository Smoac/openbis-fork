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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * The <i>GWT</i> equivalent to {@link MaterialPE}.
 * 
 * @author Izabela Adamczyk
 */
public class Material extends CodeWithRegistration<Material> implements IEntityInformationHolder,
        IEntityPropertiesHolder, IIdentifiable
{
    private DatabaseInstance databaseInstance;

    private MaterialType MaterialType;

    private Material inhibitorOf;

    private Long id;

    private Date modificationDate;

    private List<MaterialProperty> properties;

    public MaterialType getMaterialType()
    {
        return MaterialType;
    }

    public void setMaterialType(MaterialType experimentType)
    {
        this.MaterialType = experimentType;
    }

    public List<MaterialProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<MaterialProperty> properties)
    {
        this.properties = properties;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public Material getInhibitorOf()
    {
        return inhibitorOf;
    }

    public void setInhibitorOf(Material inhibitorOf)
    {
        this.inhibitorOf = inhibitorOf;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    public EntityType getEntityType()
    {
        return getMaterialType();
    }

    public EntityKind getEntityKind()
    {
        return EntityKind.MATERIAL;
    }

    //
    // IIdentifierHolder
    //

    public String getIdentifier()
    {
        return new MaterialIdentifier(getCode(), getMaterialType().getCode()).print();
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final Material o)
    {
        return getIdentifier().compareTo(o.getIdentifier());
    }
}
