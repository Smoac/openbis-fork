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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The unique {@link IEntityPropertyTypeDAO} implementation.
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

    //
    // IEntityPropertyTypeDAO
    //

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

    public EntityTypePropertyTypePE tryFindAssignment(EntityTypePE entityType,
            PropertyTypePE propertyType)
    {
        assert entityType != null : "Unspecified entity type.";
        assert propertyType != null : "Unspecified property type.";

        final Criteria criteria =
                getSession().createCriteria(getEntityTypePropertyTypeAssignmentClass());
        criteria.add(Restrictions.eq("propertyTypeInternal", propertyType));
        criteria.add(Restrictions.eq("entityTypeInternal", entityType));
        final EntityTypePropertyTypePE etpt = (EntityTypePropertyTypePE) criteria.uniqueResult();
        return etpt;
    }

    public final void createEntityPropertyTypeAssignment(
            final EntityTypePropertyTypePE entityPropertyTypeAssignement)
            throws DataAccessException
    {
        assert entityPropertyTypeAssignement != null : "Unspecified EntityTypePropertyType";
        validatePE(entityPropertyTypeAssignement);

        final HibernateTemplate template = getHibernateTemplate();
        // FIXME 2009-10-26, Piotr Buczek: remove when ordinal will be set in BO
        if (entityPropertyTypeAssignement.getOrdinal() == null)
        {
            entityPropertyTypeAssignement.setOrdinal(1L);
        }
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

    public List<IEntityPropertiesHolder> listEntities(final EntityTypePE entityType)
            throws DataAccessException
    {
        assert entityType != null : "Unspecified entity type.";

        final DetachedCriteria criteria = DetachedCriteria.forClass(entityKind.getEntityClass());
        criteria.add(Restrictions.eq(entityKind.getEntityTypeFieldName(), entityType));
        final List<IEntityPropertiesHolder> list =
                cast(getHibernateTemplate().findByCriteria(criteria));
        return list;
    }

    public List<IEntityPropertiesHolder> listEntitiesWithoutPropertyValue(
            final EntityTypePE entityType, final PropertyTypePE propertyType)
            throws DataAccessException
    {
        assert entityType != null : "Unspecified entity type.";
        assert propertyType != null : "Unspecified property type.";

        final DetachedCriteria criteria = DetachedCriteria.forClass(entityKind.getEntityClass());
        criteria.add(Restrictions.eq(entityKind.getEntityTypeFieldName(), entityType));
        //
        final List<IEntityPropertiesHolder> list =
                cast(getHibernateTemplate().findByCriteria(criteria));

        // TODO 2009-07-01, Piotr Buczek: filter results with criteria
        // final DetachedCriteria propertyTypesCriteria =
        // DetachedCriteria.forClass(entityKind.getEntityPropertyClass());
        // propertyTypesCriteria.add(Restrictions.eq("entityTypePropertyType", propertyType));
        // criteria.add(Subqueries.notExists(propertyTypesCriteria.setProjection(Projections
        // .property(...))));
        final List<IEntityPropertiesHolder> result =
                new ArrayList<IEntityPropertiesHolder>(list.size());
        for (IEntityPropertiesHolder entity : list)
        {
            if (isEntityWithoutPropertyValue(entity, propertyType))
            {
                result.add(entity);
            }
        }
        return result;
    }

    private final boolean isEntityWithoutPropertyValue(final IEntityPropertiesHolder entity,
            final PropertyTypePE propertyType)
    {
        for (EntityPropertyPE property : entity.getProperties())
        {
            if (propertyType.equals(property.getEntityTypePropertyType()))
            {
                return false;
            }
        }
        return true;
    }

    public void fillTermUsageStatistics(List<VocabularyTermWithStats> termsWithStats,
            VocabularyPE vocabulary)
    {
        assert termsWithStats != null : "Unspecified terms.";
        assert vocabulary != null : "Unspecified vocabulary.";
        assert termsWithStats.size() == vocabulary.getTerms().size() : "Sizes of terms to be filled and vocabulary terms don't match.";

        Map<Long, VocabularyTermWithStats> termsById =
                new HashMap<Long, VocabularyTermWithStats>(termsWithStats.size());
        for (VocabularyTermWithStats termWithStats : termsWithStats)
        {
            Long id = termWithStats.getTerm().getId();
            termsById.put(id, termWithStats);
        }

        final DetachedCriteria criteria =
                DetachedCriteria.forClass(entityKind.getEntityPropertyClass());
        // alias is the easiest way to restrict on association using criteria
        criteria.createAlias("vocabularyTerm", "term");
        criteria.add(Restrictions.eq("term.vocabularyInternal", vocabulary));
        ProjectionList projectionList = Projections.projectionList();
        projectionList.add(Projections.rowCount());
        projectionList.add(Projections.groupProperty("term.id"));
        criteria.setProjection(projectionList);

        final List<Object[]> results = cast(getHibernateTemplate().findByCriteria(criteria));

        for (Object[] result : results)
        {
            Integer numberOfUsages = (Integer) result[0];
            Long termId = (Long) result[1];
            termsById.get(termId).registerUsage(entityKind, numberOfUsages);
        }
    }

    public List<EntityPropertyPE> listPropertiesByVocabularyTerm(String vocabularyTermCode)
    {
        // we have to fetch props.entity, because hibernate search has some problems with reindexing
        // otherwise
        String query =
                String
                        .format(
                                "from %s props join fetch props.entity where props.vocabularyTerm.code = ?",
                                entityKind.getEntityPropertyClass().getSimpleName());
        List<EntityPropertyPE> properties =
                cast(getHibernateTemplate().find(query, toArray(vocabularyTermCode)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Term '%s' is used in %d properties of kind %s.",
                    vocabularyTermCode, properties.size(), entityKind));
        }
        return properties;
    }

    public void updateProperties(List<EntityPropertyPE> properties)
    {
        final HibernateTemplate template = getHibernateTemplate();
        for (EntityPropertyPE entityProperty : properties)
        {
            template.save(entityProperty);
        }
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("UPDATE: " + properties.size() + " of kind " + entityKind
                    + " updated.");
        }
    }

    public final void validateAndSaveUpdatedEntity(EntityTypePropertyTypePE entity)
    {
        assert entity != null : "entity is null";

        validatePE(entity);
        getHibernateTemplate().flush();
    }

    public void delete(EntityTypePropertyTypePE assignment)
    {
        HibernateTemplate template = getHibernateTemplate();
        unassignFromEntity(assignment.getPropertyValues());
        template.delete(assignment);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("DELETE: assignment between " + entityKind + " of type "
                    + assignment.getEntityType().getCode() + " and property type "
                    + assignment.getPropertyType().getCode());
        }
    }

    // If we do not do this, we get inconsistent hibernate cache. The reason is that entities would
    // still reference deleted property values.
    private static void unassignFromEntity(Set<? extends EntityPropertyPE> propertyValues)
    {
        for (EntityPropertyPE propertyValue : propertyValues)
        {
            propertyValue.getEntity().removeProperty(propertyValue);
        }
    }

}
