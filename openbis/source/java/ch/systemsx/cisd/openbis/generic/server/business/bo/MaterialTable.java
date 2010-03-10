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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The only productive implementation of {@link IMaterialTable}.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialTable extends AbstractBusinessObject implements IMaterialTable
{
    private List<MaterialPE> materials;

    private boolean dataChanged;

    private final IEntityPropertiesConverter entityPropertiesConverter;

    public MaterialTable(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.MATERIAL, daoFactory),
                null, false);
    }

    @Private
    MaterialTable(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter, List<MaterialPE> materials,
            boolean dataChanged)
    {
        super(daoFactory, session);
        this.entityPropertiesConverter = entityPropertiesConverter;
        this.materials = materials;
        this.dataChanged = dataChanged;
    }

    public final List<MaterialPE> getMaterials()
    {
        assert materials != null : "Materials have not been loaded.";
        return materials;
    }

    public void add(List<NewMaterial> newMaterials, MaterialTypePE materialTypePE)
    {
        assert newMaterials != null : "New materials undefined.";
        assert materialTypePE != null : "Material type undefined.";
        if (materials == null)
        {
            materials = new ArrayList<MaterialPE>();
        }
        setBatchUpdateMode(true);
        for (NewMaterial newMaterial : newMaterials)
        {
            materials.add(createMaterial(newMaterial, materialTypePE));
        }
        setBatchUpdateMode(false);
        dataChanged = true;
    }

    public void save()
    {
        assert materials != null : "Materials have not been loaded.";
        assert dataChanged == true : "Data not changed";
        try
        {
            getMaterialDAO().createMaterials(materials);
            checkBusinessRules();
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("One of materials"));
        }
        dataChanged = false;
    }

    private void checkBusinessRules()
    {
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();
        for (MaterialPE m : materials)
        {
            entityPropertiesConverter.checkMandatoryProperties(m.getProperties(), m
                    .getMaterialType(), cache);
        }
    }

    private MaterialPE createMaterial(NewMaterial newMaterial, MaterialTypePE materialTypePE)
    {
        final MaterialPE material = new MaterialPE();
        material.setCode(newMaterial.getCode());
        material.setRegistrator(findRegistrator());
        material.setMaterialType(materialTypePE);
        material.setDatabaseInstance(getHomeDatabaseInstance());
        if (newMaterial.getProperties().length > 0)
        {
            defineMaterialProperties(material, newMaterial.getProperties());
        }
        return material;
    }

    private final void defineMaterialProperties(final MaterialPE material,
            final IEntityProperty[] materialProperties)
    {
        final String materialTypeCode = material.getMaterialType().getCode();
        final List<MaterialPropertyPE> properties =
                entityPropertiesConverter.convertProperties(materialProperties, materialTypeCode,
                        material.getRegistrator());
        for (final MaterialPropertyPE materialProperty : properties)
        {
            material.addProperty(materialProperty);
        }
    }
}
