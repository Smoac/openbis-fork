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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The unique {@link IMaterialBO} implementation.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialBO extends AbstractBusinessObject implements IMaterialBO
{

    private final IEntityPropertiesConverter propertiesConverter;

    private MaterialPE material;

    private boolean dataChanged;

    public MaterialBO(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.MATERIAL, daoFactory));
    }

    @Private
    MaterialBO(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session);
        propertiesConverter = entityPropertiesConverter;
    }

    public void loadDataByTechId(TechId materialId)
    {
        material = getMaterialById(materialId);
        dataChanged = false;
    }

    private static final String PROPERTY_TYPES = "materialType.materialTypePropertyTypesInternal";

    private MaterialPE getMaterialById(final TechId materialId)
    {
        assert materialId != null : "Material technical id unspecified.";
        String[] connections =
            { PROPERTY_TYPES };
        final MaterialPE result = getMaterialDAO().tryGetByTechId(materialId, connections);
        if (result == null)
        {
            throw new UserFailureException(String.format("Material with ID '%s' does not exist.",
                    materialId));
        }
        return result;
    }

    public final void loadByMaterialIdentifier(final MaterialIdentifier identifier)
    {
        material = getMaterialByIdentifier(identifier);
        dataChanged = false;
    }

    private MaterialPE getMaterialByIdentifier(final MaterialIdentifier identifier)
    {
        assert identifier != null : "Material identifier unspecified.";
        final MaterialPE mat = getMaterialDAO().tryFindMaterial(identifier);
        if (mat == null)
        {
            throw UserFailureException.fromTemplate(
                    "No material could be found for identifier '%s'.", identifier);
        }
        return mat;
    }

    public final void enrichWithProperties()
    {
        if (material != null)
        {
            HibernateUtils.initialize(material.getProperties());
        }
    }

    public void save() throws UserFailureException
    {
        assert dataChanged : "Data not changed";
        try
        {
            final ArrayList<MaterialPE> materials = new ArrayList<MaterialPE>();
            materials.add(material);
            getMaterialDAO().createMaterials(materials);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Material '%s'", material.getCode()));
        }
        checkBusinessRules();
        dataChanged = false;
    }

    private void checkBusinessRules()
    {
        propertiesConverter.checkMandatoryProperties(material.getProperties(), material
                .getMaterialType());
    }

    public void update(TechId materialId, List<IEntityProperty> properties, Date version)
    {
        loadDataByTechId(materialId);
        if (version.equals(material.getModificationDate()) == false)
        {
            throwModifiedEntityException("Material");
        }
        updateProperties(properties);
        dataChanged = true;
    }

    private void updateProperties(List<IEntityProperty> properties)
    {
        final Set<MaterialPropertyPE> existingProperties = material.getProperties();
        final EntityTypePE type = material.getMaterialType();
        final PersonPE registrator = findRegistrator();
        material.setProperties(propertiesConverter.updateProperties(existingProperties, type,
                properties, registrator));
    }

    public MaterialPE getMaterial()
    {
        return material;
    }

    public void deleteByTechId(TechId materialId, String reason)
    {
        loadDataByTechId(materialId);
        try
        {
            getMaterialDAO().delete(material);
            getEventDAO().persist(createDeletionEvent(material, session.tryGetPerson(), reason));
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Material '%s' (%s)", material.getCode(), material
                    .getMaterialType().getCode()), EntityKind.MATERIAL);
        }
    }

    public static EventPE createDeletionEvent(MaterialPE material, PersonPE registrator,
            String reason)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.MATERIAL);
        event.setIdentifier(material.getCode());
        event.setDescription(getDeletionDescription(material));
        event.setReason(reason);
        event.setRegistrator(registrator);
        return event;
    }

    private static String getDeletionDescription(MaterialPE material)
    {
        return String.format("%s (%s)", material.getCode(), material.getMaterialType().getCode());
    }
}
