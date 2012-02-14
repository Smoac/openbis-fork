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

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IFullTextIndexUpdateScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexUpdateOperation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IDeletablePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * <i>Data Access Object</i> implementation for {@link IDeletionDAO}.
 * 
 * @author Christian Ribeaud
 */
final class DeletionDAO extends AbstractGenericEntityDAO<DeletionPE> implements IDeletionDAO
{

    private static final String ID = "id";

    private static final String DELETION_ID = "deletion.id";

    private static final String CONTAINER_ID = "containerId";

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}. </p>
     */
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DeletionDAO.class);

    private final PersistencyResources persistencyResources;

    DeletionDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance,
            final PersistencyResources persistencyResources)
    {
        super(sessionFactory, databaseInstance, DeletionPE.class);

        this.persistencyResources = persistencyResources;
    }

    //
    // IDeletionDAO
    //

    public final void create(final DeletionPE deletion) throws DataAccessException
    {
        assert deletion != null : "Unspecified deletion";
        validatePE(deletion);

        final HibernateTemplate template = getHibernateTemplate();
        template.save(deletion);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: deletion '%s'.", deletion));
        }
    }

    public void revert(DeletionPE deletion) throws DataAccessException
    {
        operationLog.info(String.format("REVERT: deletion %s.", deletion));
        for (EntityKind entityKind : EntityKind.values())
        {
            // NOTE: material deletion are always permanent and therefore can't be reverted
            if (entityKind != EntityKind.MATERIAL)
            {
                revertDeletionOfEntities(deletion, entityKind);
            }
        }
        super.delete(deletion);
    }

    @SuppressWarnings("unused")
    private void revertDeletionOfEntitiesOld(final DeletionPE deletion, final EntityKind entityKind)
    {
        assert deletion != null : "Unspecified deletion";
        assert entityKind != null : "Unspecified entity kind";

        List<TechId> ids =
                findTrashedEntityIds(Collections.singletonList(TechId.create(deletion)), entityKind);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        String query =
                String.format("UPDATE VERSIONED %s SET deletion = NULL WHERE deletion = ?",
                        entityKind.getDeletedEntityClass().getSimpleName());
        int updatedRows = hibernateTemplate.bulkUpdate(query, deletion);
        hibernateTemplate.flush();
        hibernateTemplate.clear();

        scheduleDynamicPropertiesEvaluationByIds(TechId.asLongs(ids), entityKind);

        operationLog.info(String.format("%s %s(s) reverted", updatedRows, entityKind.name()));
    }

    private void revertDeletionOfEntities(final DeletionPE deletion, final EntityKind entityKind)
    {
        assert deletion != null : "Unspecified deletion";
        assert entityKind != null : "Unspecified entity kind";

        List<TechId> ids =
                findTrashedEntityIds(Collections.singletonList(TechId.create(deletion)), entityKind);

        int updatedRows = (Integer) executeStatelessAction(new StatelessHibernateCallback()
            {

                public Object doInStatelessSession(StatelessSession session)
                {
                    String query =
                            String.format("UPDATE %s SET modification_timestamp = now(), "
                                    + "del_id = NULL WHERE del_id = :deletionId",
                                    entityKind.getAllTableName());
                    final SQLQuery sqlQuery = session.createSQLQuery(query);
                    sqlQuery.setParameter("deletionId", HibernateUtils.getId(deletion));
                    return sqlQuery.executeUpdate();
                }

            });

        switch (entityKind)
        {
            case SAMPLE:
                revertDeletionOfRelationships(deletion, TableNames.SAMPLE_RELATIONSHIPS_ALL_TABLE);
                break;
            case DATA_SET:
                revertDeletionOfRelationships(deletion, TableNames.DATA_SET_RELATIONSHIPS_ALL_TABLE);
                break;
            case EXPERIMENT:
                break;
            case MATERIAL:
                break;
        }

        scheduleDynamicPropertiesEvaluationByIds(TechId.asLongs(ids), entityKind);

        operationLog.info(String.format("%s %s(s) reverted", updatedRows, entityKind.name()));
    }

    private void revertDeletionOfRelationships(final DeletionPE deletion, final String tableName)
    {
        assert deletion != null : "Unspecified deletion";

        int updatedRows = (Integer) executeStatelessAction(new StatelessHibernateCallback()
            {

                public Object doInStatelessSession(StatelessSession session)
                {
                    String query =
                            String.format("UPDATE %s SET del_id = NULL WHERE del_id = :deletionId",
                                    tableName);
                    final SQLQuery sqlQuery = session.createSQLQuery(query);
                    sqlQuery.setParameter("deletionId", HibernateUtils.getId(deletion));
                    return sqlQuery.executeUpdate();
                }
            });

        operationLog.info(String.format("%s %s(s) reverted", updatedRows, tableName));
    }

    public List<TechId> findTrashedSampleIds(final List<TechId> deletionIds)
    {
        return findTrashedEntityIds(deletionIds, EntityKind.SAMPLE);
    }

    public List<TechId> findTrashedNonComponentSampleIds(final List<TechId> deletionIds)
    {
        return findTrashedEntityIds(deletionIds, EntityKind.SAMPLE,
                Restrictions.isNull(CONTAINER_ID));
    }

    public List<TechId> findTrashedComponentSampleIds(final List<TechId> deletionIds)
    {
        return findTrashedEntityIds(deletionIds, EntityKind.SAMPLE,
                Restrictions.isNotNull(CONTAINER_ID));
    }

    public List<TechId> findTrashedExperimentIds(final List<TechId> deletionIds)
    {
        return findTrashedEntityIds(deletionIds, EntityKind.EXPERIMENT);
    }

    public List<String> findTrashedDataSetCodes(final List<TechId> deletionIds)
    {
        if (deletionIds.isEmpty())
        {
            return Collections.emptyList();
        }
        final DetachedCriteria criteria =
                DetachedCriteria.forClass(EntityKind.DATA_SET.getDeletedEntityClass());
        final List<Long> longIds = TechId.asLongs(deletionIds);
        criteria.setProjection(Projections.property("code"));
        criteria.add(Restrictions.in(DELETION_ID, longIds));
        final List<String> results = cast(getHibernateTemplate().findByCriteria(criteria));
        operationLog.info(String.format("found %s trashed %s(s)", results.size(),
                EntityKind.DATA_SET.name()));
        return results;
    }

    private List<TechId> findTrashedEntityIds(final List<TechId> deletionIds,
            final EntityKind entityKind, Criterion... additionalCriteria)
    {
        if (deletionIds.isEmpty())
        {
            return Collections.emptyList();
        }
        final DetachedCriteria criteria =
                DetachedCriteria.forClass(entityKind.getDeletedEntityClass());
        final List<Long> longIds = TechId.asLongs(deletionIds);
        criteria.setProjection(Projections.id());
        criteria.add(Restrictions.in(DELETION_ID, longIds));
        for (Criterion criterion : additionalCriteria)
        {
            criteria.add(criterion);
        }
        final List<Long> results = cast(getHibernateTemplate().findByCriteria(criteria));
        operationLog
                .info(String.format("found %s trashed %s(s)", results.size(), entityKind.name()));
        return transformNumbers2TechIdList(results);
    }

    public int trash(final EntityKind entityKind, final List<TechId> entityIds,
            final DeletionPE deletion) throws DataAccessException
    {
        if (entityIds.isEmpty())
        {
            return 0;
        }
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        int updatedRows = (Integer) hibernateTemplate.execute(new HibernateCallback()
            {

                //
                // HibernateCallback
                //

                public final Object doInHibernate(final Session session) throws HibernateException,
                        SQLException
                {
                    // NOTE: 'VERSIONED' makes modification time modified too
                    return session
                            .createQuery(
                                    "UPDATE VERSIONED "
                                            + entityKind.getEntityClass().getSimpleName()
                                            + " SET deletion = :deletion"
                                            + " WHERE deletion IS NULL AND id IN (:ids) ")
                            .setParameter("deletion", deletion)
                            .setParameterList("ids", TechId.asLongs(entityIds)).executeUpdate();
                }
            });

        switch (entityKind)
        {
            case SAMPLE:
                trashSampleRelationships(entityIds, deletion);
                break;
            case DATA_SET:
                trashDataSetRelationships(entityIds, deletion);
                break;
            case EXPERIMENT:
                break;
            case MATERIAL:
                break;
        }

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("trashing %d %ss", updatedRows, entityKind.getLabel()));
        }
        hibernateTemplate.flush();

        List<Long> ids = TechId.asLongs(entityIds);
        scheduleRemoveFromFullTextIndex(ids, entityKind);

        return updatedRows;
    }

    public int trashSampleRelationships(final List<TechId> samplesIds, final DeletionPE deletion)
            throws DataAccessException
    {
        if (samplesIds.isEmpty())
        {
            return 0;
        }
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        int updatedRows = (Integer) hibernateTemplate.execute(new HibernateCallback()
            {
                //
                // HibernateCallback
                //
                public final Object doInHibernate(final Session session) throws HibernateException,
                        SQLException
                {
                    // NOTE: 'VERSIONED' makes modification time modified too
                    return session
                            .createQuery(
                                    "UPDATE "
                                            + SampleRelationshipPE.class.getSimpleName()
                                            + " SET deletion = :deletion"
                                            + " WHERE deletion IS NULL"
                                            + " AND (parentSample.id IN (:ids) OR childSample.id in (:ids))")
                            .setParameter("deletion", deletion)
                            .setParameterList("ids", TechId.asLongs(samplesIds)).executeUpdate();
                }
            });
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String
                    .format("trashing %d %ss", updatedRows, "sample relationships."));
        }
        hibernateTemplate.flush();

        return updatedRows;
    }

    public int trashDataSetRelationships(final List<TechId> dataSetIds, final DeletionPE deletion)
            throws DataAccessException
    {
        if (dataSetIds.isEmpty())
        {
            return 0;
        }
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        int updatedRows = (Integer) hibernateTemplate.execute(new HibernateCallback()
            {
                //
                // HibernateCallback
                //
                public final Object doInHibernate(final Session session) throws HibernateException,
                        SQLException
                {
                    // NOTE: 'VERSIONED' makes modification time modified too
                    return session
                            .createQuery(
                                    "UPDATE "
                                            + SampleRelationshipPE.class.getSimpleName()
                                            + " SET deletion = :deletion"
                                            + " WHERE deletion IS NULL"
                                            + " AND (parentDataSet.id IN (:ids) OR childDataSet.id in (:ids))")
                            .setParameter("deletion", deletion)
                            .setParameterList("ids", TechId.asLongs(dataSetIds)).executeUpdate();
                }
            });
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("trashing %d %ss", updatedRows,
                    "data set relationships."));
        }
        hibernateTemplate.flush();

        return updatedRows;
    }

    public List<DeletionPE> findAllById(List<Long> ids)
    {
        if (ids.isEmpty())
        {
            return Collections.emptyList();
        }
        final Criteria criteria = getSession().createCriteria(DeletionPE.class);
        criteria.add(Restrictions.in("id", ids));

        @SuppressWarnings("unchecked")
        List<DeletionPE> result = criteria.list();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s deletions has been found", result.size()));
        }

        return result;
    }

    protected IFullTextIndexUpdateScheduler getIndexUpdateScheduler()
    {
        return persistencyResources.getIndexUpdateScheduler();
    }

    protected IDynamicPropertyEvaluationScheduler getDynamicPropertyEvaluatorScheduler()
    {
        return persistencyResources.getDynamicPropertyEvaluationScheduler();
    }

    protected void scheduleRemoveFromFullTextIndex(List<Long> ids, EntityKind entityKind)
    {
        getIndexUpdateScheduler().scheduleUpdate(
                IndexUpdateOperation.remove(entityKind.getEntityClass(), ids));
    }

    protected void scheduleDynamicPropertiesEvaluationByIds(List<Long> ids, EntityKind entityKind)
    {
        scheduleDynamicPropertiesEvaluationForIds(getDynamicPropertyEvaluatorScheduler(),
                entityKind.getEntityClass(), ids);
    }

    public List<TechId> findTrashedDataSetIds(List<TechId> deletionIds)
    {
        return findTrashedEntityIds(deletionIds, EntityKind.DATA_SET);
    }

    public List<? extends IDeletablePE> listDeletedEntities(EntityKind entityKind,
            List<TechId> entityIds)
    {
        if (entityIds.isEmpty())
        {
            return Collections.emptyList();
        }
        DetachedCriteria criteria = DetachedCriteria.forClass(entityKind.getDeletedEntityClass());
        List<Long> ids = TechId.asLongs(entityIds);
        criteria.add(Restrictions.in(ID, ids));
        return cast(getHibernateTemplate().findByCriteria(criteria));
    }

    public List<TechId> listDeletedEntitiesForType(EntityKind entityKind, TechId entityTypeId)
    {
        String typeId = null;
        switch (entityKind)
        {
            case EXPERIMENT:
                typeId = "experimentType.id";
                break;

            case SAMPLE:
                typeId = "sampleType.id";
                break;

            case DATA_SET:
                typeId = "dataSetType.id";
                break;

            default:
                // entities of these types cannot be in the trash can
                return Collections.emptyList();
        }

        DetachedCriteria criteria = DetachedCriteria.forClass(entityKind.getDeletedEntityClass());
        criteria.setProjection(Projections.id());
        criteria.add(Restrictions.eq(typeId, entityTypeId.getId()));

        List<Long> result = cast(getHibernateTemplate().findByCriteria(criteria));
        return TechId.createList(result);
    }
}
