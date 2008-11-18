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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The unique <code>EntityPropertyTypeDAO</code> implementation.
 * 
 * @author Christian Ribeaud
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
final class EntityPropertyTypeDAO extends AbstractDAO implements IEntityPropertyTypeDAO
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, EntityPropertyTypeDAO.class);

    private final EntityKind entityKind;

    public EntityPropertyTypeDAO(final EntityKind entityKind, final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
        this.entityKind = entityKind;

    }

    private final <T extends EntityTypePropertyTypePE> Class<T> getEntityTypePropertyTypeAssignmentClass()
    {
        return entityKind.getEntityTypePropertyTypeAssignmentClass();
    }

    private final String getTypeFieldName()
    {
        return entityKind.getLabel() + "Type";
    }

    //
    // IEntityPropertyTypeDAO
    //

    public final long countEntitiesWithProperty(final long entityTypeId, final long propertyTypeId)
            throws DataAccessException
    {
        final List<Long> sizes =
                cast(getHibernateTemplate().find(
                        String.format("select count(ep) from %s ep "
                                + "where ep.entityTypePropertyType.entityTypeInternal.id = ? "
                                + "and ep.entityTypePropertyType.propertyType.id = ?", entityKind
                                .getEntityPropertyClass().getSimpleName()),
                        toArray(entityTypeId, propertyTypeId)));
        final Long count = getEntity(sizes);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d entity(ies) found for entity type id %d and property type id %d.", count,
                    entityTypeId, propertyTypeId));
        }
        return count;
    }

    public final void createEntityPropertyTypeAssignment(
            final EntityTypePropertyTypePE entityPropertyTypeAssignement)
            throws DataAccessException
    {
        assert entityPropertyTypeAssignement != null : "Unspecified EntityTypePropertyType";
        validatePE(entityPropertyTypeAssignement);

        final HibernateTemplate template = getHibernateTemplate();
        template.save(entityPropertyTypeAssignement);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("ADD: assignment of property '"
                    + entityPropertyTypeAssignement.getPropertyType().getCode()
                    + "' with entity type '"
                    + entityPropertyTypeAssignement.getEntityType().getCode() + "'.");
        }
    }

    public final List<IIdAndCodeHolder> listEntitiesWithMissingPropertyValues(
            final EntityTypePropertyTypePE entityPropertyType) throws DataAccessException
    {
        assert entityPropertyType != null : "Unspecified EntityTypePropertyTypePE";
        assert entityPropertyType.getEntityType() != null : "Unspecified EntityType";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        final String hql =
                String.format("select e from %s e where e.%s = ? "
                        + "and (select count(*) from %s ep where ep.entity = e "
                        + "and ep.entityTypePropertyType = ?) = 0", entityKind.getEntityClass()
                        .getSimpleName(), getTypeFieldName(), entityKind.getEntityPropertyClass()
                        .getSimpleName());
        final List<IIdAndCodeHolder> list =
                cast(hibernateTemplate.find(hql, toArray(entityPropertyType.getEntityType(),
                        entityPropertyType)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String
                    .format("%d entity(ies) of kind '%s' with missing value for "
                            + "property '%s' have been found.", list.size(), entityKind,
                            entityPropertyType));
        }
        return list;
    }

    public final List<EntityTypePropertyTypePE> listEntityPropertyTypeRelations()
            throws DataAccessException
    {
        final String hql =
                String.format("from %s etpt where etpt.propertyType.managedInternally = false"
                        + " and etpt.propertyType.databaseInstance = ?",
                        getEntityTypePropertyTypeAssignmentClass().getSimpleName());
        final List<EntityTypePropertyTypePE> assignments =
                cast(getHibernateTemplate().find(hql, toArray(getDatabaseInstance())));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d entity property type relations have been found for entity kind '%s'",
                    assignments.size(), entityKind.getLabel()));
        }
        return assignments;
    }

    public final List<EntityTypePropertyTypePE> listEntityPropertyTypes(
            final EntityTypePE entityType) throws DataAccessException
    {
        assert entityType != null : "Unspecified EntityType";

        final List<EntityTypePropertyTypePE> assignments =
                cast(getHibernateTemplate().find(
                        String.format("from %s etpt where etpt.entityTypeInternal = ?",
                                getEntityTypePropertyTypeAssignmentClass().getSimpleName()),
                        toArray(entityType)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d assignments have been found for entity type '%s'.", assignments.size(),
                    entityType));
        }
        return assignments;
    }

    public final EntityTypePropertyTypePE tryFindAssignment(final EntityTypePE entityType,
            final PropertyTypePE propertyType) throws DataAccessException
    {
        final List<EntityTypePropertyTypePE> assignments =
                cast(getHibernateTemplate()
                        .find(
                                String
                                        .format(
                                                "from %s etpt where etpt.entityTypeInternal = ? and etpt.propertyType = ?",
                                                getEntityTypePropertyTypeAssignmentClass()
                                                        .getSimpleName()),
                                toArray(entityType, propertyType)));
        final EntityTypePropertyTypePE result =
                tryFindEntity(assignments, "assignments", entityType, propertyType);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("tryFindAssignment(" + entityType + "," + propertyType + "): '"
                    + result + "'.");
        }
        return result;
    }

    public final void unassignEntityPropertyType(final EntityTypePE entityType,
            final PropertyTypePE propertyType) throws DataAccessException
    {
        assert entityType != null : "Unspecified entity type.";
        assert propertyType != null : "Unspecified property type.";

        final EntityTypePropertyTypePE assignment = tryFindAssignment(entityType, propertyType);
        if (assignment != null)
        {
            getHibernateTemplate().delete(assignment);
            getHibernateTemplate().flush();

            if (operationLog.isInfoEnabled())
            {
                operationLog.info("REMOVE: " + " property " + propertyType.getCode()
                        + ",entityType = " + entityType.getCode() + " assignment.");
            }
        }

    }

    public final void updateEntityTypePropertyType(final EntityTypePropertyTypePE entityPropertyType)
            throws DataAccessException
    {
        assert entityPropertyType != null : "Unspecified EntityTypePropertyTypePE.";
        validatePE(entityPropertyType);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.update(entityPropertyType);
        hibernateTemplate.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: Entity type property type '%s'.",
                    entityPropertyType));
        }

    }

    public final void createPropertyValue(final EntityPropertyPE property)
    {
        String value = property.tryGetUntypedValue();
        if (value != null)
        {
            final VocabularyTermPE vocabularyTerm = property.getVocabularyTerm();
            if (vocabularyTerm != null)
            {
                value = null;
            }
            validatePE(property);

            final HibernateTemplate hibernateTemplate = getHibernateTemplate();
            hibernateTemplate.save(property);
            hibernateTemplate.flush();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("ADD: Entity property '%s' for entity kind '%s'.",
                        property, entityKind));
            }
        }
    }
}