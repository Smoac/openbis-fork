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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The unique {@link IEntityTypePropertyTypeBO} implementation.
 * 
 * @author Izabela Adamczyk
 */
public class EntityTypePropertyTypeBO extends AbstractBusinessObject implements
        IEntityTypePropertyTypeBO
{
    private EntityKind entityKind;

    private IEntityPropertiesConverter propertiesConverter;

    private EntityTypePropertyTypePE assignment;

    @Private
    EntityTypePropertyTypeBO(IDAOFactory daoFactory, Session session, EntityKind entityKind,
            IEntityPropertiesConverter converter)
    {
        super(daoFactory, session);
        propertiesConverter = converter;
        this.entityKind = entityKind;
    }

    public EntityTypePropertyTypeBO(IDAOFactory daoFactory, Session session, EntityKind entityKind)
    {
        super(daoFactory, session);
        propertiesConverter = new EntityPropertiesConverter(entityKind, daoFactory);
        this.entityKind = entityKind;
    }

    public EntityTypePropertyTypePE getLoadedAssignment()
    {
        if (assignment == null)
        {
            throw new IllegalStateException("No assignment loaded.");
        }
        return assignment;
    }

    public void deleteLoadedAssignment()
    {
        if (assignment == null)
        {
            return;
        }
        getEntityPropertyTypeDAO(entityKind).delete(assignment);
        assignment = null;
    }

    public void loadAssignment(String propertyTypeCode, String entityTypeCode)
    {
        EntityTypePE entityType = findEntityType(entityTypeCode);
        PropertyTypePE propertyType = findPropertyType(propertyTypeCode);
        IEntityPropertyTypeDAO entityPropertyTypeDAO = getEntityPropertyTypeDAO(entityKind);
        assignment = entityPropertyTypeDAO.tryFindAssignment(entityType, propertyType);
    }

    public void createAssignment(String propertyTypeCode, String entityTypeCode,
            boolean isMandatory, String defaultValue)
    {
        EntityTypePE entityType = findEntityType(entityTypeCode);
        PropertyTypePE propertyType = findPropertyType(propertyTypeCode);
        assignment = createAssignment(isMandatory, entityType, propertyType);
        List<IEntityPropertiesHolder<EntityPropertyPE>> entities =
                getEntityPropertyTypeDAO(entityKind).listEntities(entityType);
        final int size = entities.size();
        if (size > 0)
        {
            if (isMandatory && StringUtils.isEmpty(defaultValue))
            {
                throw new UserFailureException(String.format(
                        "Cannot create mandatory assignment. Please specify "
                                + "'Initial Value', which will be used for %s %s%s "
                                + "of type '%s' already existing in the database.", size,
                        entityKind.getLabel(), createPlural(size), entityType.getCode()));
            }
        }
        for (IEntityPropertiesHolder<EntityPropertyPE> entity : entities)
        {
            final EntityPropertyPE property =
                    propertiesConverter.createProperty(propertyType, assignment, findRegistrator(),
                            defaultValue);
            if (property != null)
            {
                entity.addProperty(property);
            }
        }
    }

    private String createPlural(int size)
    {
        return size == 1 ? "" : "s";
    }

    private EntityTypePropertyTypePE createAssignment(final boolean mandatory,
            final EntityTypePE entityType, final PropertyTypePE propertyType)
    {
        checkAssignmentDoesNotExist(entityType, propertyType);
        final EntityTypePropertyTypePE etpt =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        etpt.setPropertyType(propertyType);
        etpt.setRegistrator(findRegistrator());
        etpt.setEntityType(entityType);
        etpt.setMandatory(mandatory);
        getEntityPropertyTypeDAO(entityKind).createEntityPropertyTypeAssignment(etpt);
        return etpt;
    }

    private PropertyTypePE findPropertyType(String propertyTypeCode)
    {
        PropertyTypePE propertyType =
                getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyTypeCode);
        if (propertyType == null)
        {
            throw new UserFailureException(String.format("Property type '%s' does not exist.",
                    propertyTypeCode));
        }
        if (propertyType.isManagedInternally())
        {
            throw new UserFailureException(String.format(
                    "Property type '%s' is managed internally.", propertyTypeCode));
        }
        return propertyType;
    }

    private EntityTypePE findEntityType(String entityTypeCode)
    {
        EntityTypePE entityType =
                getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(entityTypeCode);
        if (entityType == null)
        {
            throw new UserFailureException(String.format("%s type '%s' does not exist.",
                    StringUtils.capitalize(entityKind.getLabel()), entityTypeCode));
        }
        return entityType;
    }

    private void checkAssignmentDoesNotExist(EntityTypePE entityType, PropertyTypePE propertyType)
    {
        if (getEntityPropertyTypeDAO(entityKind).tryFindAssignment(entityType, propertyType) != null)
        {
            throw new UserFailureException(String.format(
                    "Property type '%s' is already assigned to %s type '%s'.", propertyType
                            .getCode(), entityKind.getLabel(), entityType.getCode()));
        }
    }
}
