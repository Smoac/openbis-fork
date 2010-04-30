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
import org.apache.commons.lang.time.StopWatch;
import org.springframework.dao.DataAccessException;

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
        StopWatch watch = new StopWatch();
        watch.start();
        EntityTypePE entityType = findEntityType(entityTypeCode);
        PropertyTypePE propertyType = findPropertyType(propertyTypeCode);
        IEntityPropertyTypeDAO entityPropertyTypeDAO = getEntityPropertyTypeDAO(entityKind);
        assignment = entityPropertyTypeDAO.tryFindAssignment(entityType, propertyType);
    }

    public int countAssignmentValues(String propertyTypeCode, String entityTypeCode)
    {
        IEntityPropertyTypeDAO entityPropertyTypeDAO = getEntityPropertyTypeDAO(entityKind);
        return entityPropertyTypeDAO.countAssignmentValues(entityTypeCode, propertyTypeCode);
    }

    public void createAssignment(String propertyTypeCode, String entityTypeCode,
            boolean isMandatory, String defaultValue, String section, Long previousETPTOrdinal)
    {
        EntityTypePE entityType = findEntityType(entityTypeCode);
        PropertyTypePE propertyType = findPropertyType(propertyTypeCode);
        assignment =
                createAssignment(isMandatory, section, previousETPTOrdinal, entityType,
                        propertyType);
        // fill default property values
        if (isMandatory)
        {
            String errorMsgTemplate =
                    "Cannot create mandatory assignment. "
                            + "Please specify 'Initial Value', which will be used for %s %s%s "
                            + "of type '%s' already existing in the database.";
            List<IEntityPropertiesHolder> entities = getAllEntities(entityType);
            addPropertyWithDefaultValue(entityType, propertyType, defaultValue, entities,
                    errorMsgTemplate);
        } else if (StringUtils.isEmpty(defaultValue) == false)
        {
            List<IEntityPropertiesHolder> entities = getAllEntities(entityType);
            addPropertyWithDefaultValue(entityType, propertyType, defaultValue, entities, null);
        }
    }

    private List<IEntityPropertiesHolder> getAllEntities(EntityTypePE entityType)
    {
        return getEntityPropertyTypeDAO(entityKind).listEntities(entityType);
    }

    private void addPropertyWithDefaultValue(EntityTypePE entityType, PropertyTypePE propertyType,
            String defaultValue, List<IEntityPropertiesHolder> entities, String errorMsgTemplate)
    {
        final int size = entities.size();
        if (size > 0)
        {
            if (StringUtils.isEmpty(defaultValue))
            {
                throw new UserFailureException(String.format(errorMsgTemplate, size, entityKind
                        .getLabel(), createPlural(size), entityType.getCode()));
            }
        }
        for (IEntityPropertiesHolder entity : entities)
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

    public void updateLoadedAssignment(final boolean isMandatory, final String defaultValue,
            final String section, final Long previousETPTOrdinal)
    {
        // if ordinal was changed some etpts need to be shifted by 1
        final Long currentOrdinal = previousETPTOrdinal + 1;
        if (assignment.getOrdinal().equals(currentOrdinal) == false)
        {
            increaseOrdinals(assignment.getEntityType(), currentOrdinal, 1);
        }
        assignment.setOrdinal(currentOrdinal);
        assignment.setSection(section);
        // fill missing property values if we change from optional to mandatory
        if (isMandatory && (assignment.isMandatory() == false))
        {
            final EntityTypePE entityType = assignment.getEntityType();
            final PropertyTypePE propertyType = assignment.getPropertyType();
            List<IEntityPropertiesHolder> entities =
                    getEntityPropertyTypeDAO(entityKind).listEntitiesWithoutPropertyValue(
                            entityType, propertyType);
            String errorMsgTemplate =
                    "Cannot change assignment to mandatory. "
                            + "Please specify 'Update Value', which will be used for %s %s%s "
                            + "of type '%s' already existing in the database "
                            + "without any value for this property.";
            addPropertyWithDefaultValue(entityType, propertyType, defaultValue, entities,
                    errorMsgTemplate);
        }
        assignment.setMandatory(isMandatory);
        validateAndSave();
    }

    private void validateAndSave()
    {
        getEntityPropertyTypeDAO(entityKind).validateAndSaveUpdatedEntity(assignment);
    }

    private String createPlural(int size)
    {
        return size == 1 ? "" : "s";
    }

    private EntityTypePropertyTypePE createAssignment(final boolean mandatory,
            final String section, final Long previousETPTOrdinal, final EntityTypePE entityType,
            final PropertyTypePE propertyType)
    {
        checkAssignmentDoesNotExist(entityType, propertyType);
        // need to shift existing etpts to create space for new one
        final Long currentOrdinal = previousETPTOrdinal + 1;
        increaseOrdinals(entityType, currentOrdinal, 1);

        final EntityTypePropertyTypePE etpt =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        etpt.setPropertyType(propertyType);
        etpt.setRegistrator(findRegistrator());
        etpt.setEntityType(entityType);
        etpt.setMandatory(mandatory);
        etpt.setSection(section);
        etpt.setOrdinal(currentOrdinal);

        try
        {
            getEntityPropertyTypeDAO(entityKind).createEntityPropertyTypeAssignment(etpt);
        } catch (DataAccessException e)
        {
            throwException(e, createExceptionMessage(entityType, propertyType));
        }
        return etpt;
    }

    /**
     * shift specified entity type etpts by specified increment starting from etpt with specified
     * ordinal
     * 
     * @param entityType
     */
    private void increaseOrdinals(EntityTypePE entityType, Long startOrdinal, int increment)
    {
        getEntityPropertyTypeDAO(entityKind).increaseOrdinals(entityType, startOrdinal, increment);
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
            throw new UserFailureException(createExceptionMessage(entityType, propertyType));
        }
    }

    private String createExceptionMessage(EntityTypePE entityType, PropertyTypePE propertyType)
    {
        return String.format("Property type '%s' is already assigned to %s type '%s'.",
                propertyType.getCode(), entityKind.getLabel(), entityType.getCode());
    }
}
