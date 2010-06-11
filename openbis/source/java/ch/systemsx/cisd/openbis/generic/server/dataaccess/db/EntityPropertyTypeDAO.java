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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.EntitiesToUpdate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IFullTextIndexUpdateScheduler;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
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

    private final IFullTextIndexUpdateScheduler fullTextIndexUpdateScheduler;

    public EntityPropertyTypeDAO(final EntityKind entityKind, final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance,
            final IFullTextIndexUpdateScheduler fullTextIndexUpdateScheduler)
    {
        super(sessionFactory, databaseInstance);
        this.entityKind = entityKind;
        this.fullTextIndexUpdateScheduler = fullTextIndexUpdateScheduler;
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

    public int countAssignmentValues(String entityTypeCode, String propertyTypeCode)
    {
        assert entityTypeCode != null : "Unspecified entity type.";
        assert propertyTypeCode != null : "Unspecified property type.";

        String query =
                String.format("select count(pv) from %s pa join pa.propertyValues pv "
                        + "where pa.propertyTypeInternal.simpleCode = ? "
                        + "and pa.entityTypeInternal.code = ?", entityKind
                        .getEntityTypePropertyTypeAssignmentClass().getSimpleName(), entityKind
                        .getEntityPropertyClass().getSimpleName());
        return ((Long) (getHibernateTemplate().find(query,
                toArray(propertyTypeCode, entityTypeCode)).get(0))).intValue();
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

    public List<Long> listEntityIds(final EntityTypePE entityType) throws DataAccessException
    {
        assert entityType != null : "Unspecified entity type.";

        final DetachedCriteria criteria = DetachedCriteria.forClass(entityKind.getEntityClass());
        criteria.add(Restrictions.eq(entityKind.getEntityTypeFieldName(), entityType));
        criteria.setProjection(Projections.id());
        final List<Long> list = cast(getHibernateTemplate().findByCriteria(criteria));
        return list;
    }

    public void createProperties(final EntityPropertyPE property, final List<Long> entityIds)
    {
        assert property != null : "Given property data can not be null.";

        final Long etptId = property.getEntityTypePropertyType().getId();
        final Long registratorId = property.getRegistrator().getId();

        final EntityKindPropertyTableNames propertyTableNames =
                getEntityKindPropertyTableNames(entityKind);
        final String tableName = propertyTableNames.getPropertiesTable();
        final String sequenceName = propertyTableNames.getPropertiesSequence();
        final String entityColumn = propertyTableNames.getEntityColumn();
        final String propertyTypeColumn = propertyTableNames.getPropertyTypeColumn();

        final String valueColumn;
        final Serializable valueObject;
        if (property.getVocabularyTerm() != null)
        {
            valueColumn = "cvte_id";
            valueObject = property.getVocabularyTerm().getId();
        } else if (property.getMaterialValue() != null)
        {
            valueColumn = "mate_prop_id";
            valueObject = property.getMaterialValue().getId();
        } else
        {
            assert property.getValue() != null;
            valueColumn = "value";
            valueObject = property.getValue();
        }

        final String sql =
                String.format("INSERT INTO %s (id, pers_id_registerer, %s, %s, %s) "
                        + "VALUES (nextval('%s'), :registratorId, :entityId, :etptId, :value)",
                        tableName, entityColumn, propertyTypeColumn, valueColumn, sequenceName);

        getHibernateTemplate().execute(new HibernateCallback()
            {

                //
                // HibernateCallback
                //

                public final Object doInHibernate(final Session session) throws HibernateException,
                        SQLException
                {
                    final SQLQuery sqlQuery = session.createSQLQuery(sql);
                    sqlQuery.setParameter("registratorId", registratorId);
                    sqlQuery.setParameter("etptId", etptId);
                    sqlQuery.setParameter("value", valueObject);
                    int counter = 0;
                    for (Long entityId : entityIds)
                    {
                        sqlQuery.setParameter("entityId", entityId);
                        sqlQuery.executeUpdate();
                        if (operationLog.isDebugEnabled())
                        {
                            operationLog.debug(String.format(
                                    "Created property '%s' for %s with id %s", property, entityKind
                                            .getLabel(), entityId));
                        }
                        if (operationLog.isInfoEnabled() && (++counter % 1000 == 0))
                        {
                            operationLog.info(String.format(
                                    "%d %s properties have been created...", counter, entityKind
                                            .getLabel()));

                        }
                    }
                    return null;
                }
            });
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Created %s %s properties : %s", entityIds.size(),
                    entityKind.getLabel(), property));
        }
        // index will not be updated automatically by Hibernate because we use native SQL queries
        scheduleFullTextIndexUpdate(entityIds);
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
        //
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

    public void increaseOrdinals(EntityTypePE entityType, Long fromOrdinal, int increment)
    {
        assert entityType != null : "Unspecified entity type.";
        assert fromOrdinal != null : "Unspecified ordinal.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();

        String query =
                String.format("UPDATE %s etpt SET etpt.ordinal = etpt.ordinal + ? "
                        + "WHERE etpt.entityTypeInternal = ? AND etpt.ordinal >= ?", entityKind
                        .getEntityTypePropertyTypeAssignmentClass().getSimpleName());
        final int updatedRows =
                hibernateTemplate.bulkUpdate(query, toArray(new Long(increment), entityType,
                        fromOrdinal));
        hibernateTemplate.flush();

        if (operationLog.isInfoEnabled())
        {
            operationLog.debug(String.format(
                    "%d etpt(s) updated for entity type '%s' with ordinal increased by %d.",
                    updatedRows, entityType.getCode(), increment));
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
            propertyValue.getEntity().removeProperty(propertyValue); // FIXME loads entities?
        }
    }

    // helpers

    private void scheduleFullTextIndexUpdate(List<Long> entityIds)
    {
        fullTextIndexUpdateScheduler.scheduleUpdate(new EntitiesToUpdate(
                getIndexedEntityClass(entityKind), entityIds));
    }

    private static Class<?> getIndexedEntityClass(EntityKind entityKind)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return ExternalDataPE.class;
            default:
                return entityKind.getEntityClass();
        }
    }

    private static EntityKindPropertyTableNames getEntityKindPropertyTableNames(
            EntityKind entityKind)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return new EntityKindPropertyTableNames(TableNames.DATA_SET_PROPERTIES_TABLE,
                        SequenceNames.DATA_SET_PROPERTY_SEQUENCE,
                        ColumnNames.DATA_SET_TYPE_PROPERTY_TYPE_COLUMN, ColumnNames.DATA_SET_COLUMN);
            case EXPERIMENT:
                return new EntityKindPropertyTableNames(TableNames.EXPERIMENT_PROPERTIES_TABLE,
                        SequenceNames.EXPERIMENT_PROPERTY_SEQUENCE,
                        ColumnNames.EXPERIMENT_TYPE_PROPERTY_TYPE_COLUMN,
                        ColumnNames.EXPERIMENT_COLUMN);
            case MATERIAL:
                return new EntityKindPropertyTableNames(TableNames.MATERIAL_PROPERTIES_TABLE,
                        SequenceNames.MATERIAL_PROPERTY_SEQUENCE,
                        ColumnNames.MATERIAL_TYPE_PROPERTY_TYPE_COLUMN, ColumnNames.MATERIAL_COLUMN);
            case SAMPLE:
                return new EntityKindPropertyTableNames(TableNames.SAMPLE_PROPERTIES_TABLE,
                        SequenceNames.SAMPLE_PROPERTY_SEQUENCE,
                        ColumnNames.SAMPLE_TYPE_PROPERTY_TYPE_COLUMN, ColumnNames.SAMPLE_COLUMN);
        }
        return null; // can't happen
    }

    private static class EntityKindPropertyTableNames
    {

        private final String propertiesTable;

        private final String propertiesSequence;

        private final String propertyTypeColumn;

        private final String entityColumn;

        public EntityKindPropertyTableNames(String propertiesTable, String propertiesSequence,
                String propertyTypeColumn, String entityColumn)
        {
            this.propertiesTable = propertiesTable;
            this.propertiesSequence = propertiesSequence;
            this.propertyTypeColumn = propertyTypeColumn;
            this.entityColumn = entityColumn;
        }

        public String getPropertiesTable()
        {
            return propertiesTable;
        }

        public String getPropertiesSequence()
        {
            return propertiesSequence;
        }

        public String getPropertyTypeColumn()
        {
            return propertyTypeColumn;
        }

        public String getEntityColumn()
        {
            return entityColumn;
        }

    }

}
