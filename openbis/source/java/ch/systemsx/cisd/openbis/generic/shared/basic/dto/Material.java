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

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;

/**
 * The <i>GWT</i> equivalent to MaterialPE.
 * 
 * @author Izabela Adamczyk
 */
public class Material extends CodeWithRegistration<Material> implements
        IEntityInformationHolderWithIdentifier, IEntityPropertiesHolder, IIdAndCodeHolder
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private DatabaseInstance databaseInstance;

    private MaterialType MaterialType;

    private Long id;

    private Date modificationDate;

    private List<IEntityProperty> properties;

    public MaterialType getMaterialType()
    {
        return MaterialType;
    }

    public void setMaterialType(MaterialType experimentType)
    {
        this.MaterialType = experimentType;
    }

    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<IEntityProperty> properties)
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

    public String getPermId()
    {
        return getIdentifier();
    }

    @Override
    public String toString()
    {
        return getIdentifier();
    }

}
