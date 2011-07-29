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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * <i>Data Access Object</i> implementation for {@link IDeletionDAO}.
 * 
 * @author Christian Ribeaud
 */
final class DeletionDAO extends AbstractGenericEntityDAO<DeletionPE> implements IDeletionDAO
{

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

    private void revertDeletionOfEntities(final DeletionPE deletion, final EntityKind entityKind)
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

    public List<TechId> findTrashedSampleIds(final List<TechId> deletionIds)
    {
        return findTrashedEntityIds(deletionIds, EntityKind.SAMPLE);
    }

    public List<TechId> findTrashedExperimentIds(final List<TechId> deletionIds)
    {
        return findTrashedEntityIds(deletionIds, EntityKind.EXPERIMENT);
    }

    public List<String> findTrashedDataSetCodes(final List<TechId> deletionIds)
    {
        final DetachedCriteria criteria =
                DetachedCriteria.forClass(EntityKind.DATA_SET.getDeletedEntityClass());
        final List<Long> longIds = TechId.asLongs(deletionIds);
        criteria.setProjection(Projections.property("code"));
        criteria.add(Restrictions.in("deletion.id", longIds));
        final List<String> results = cast(getHibernateTemplate().findByCriteria(criteria));
        operationLog.info(String.format("found %s trashed %s(s)", results.size(),
                EntityKind.DATA_SET.name()));
        return results;
    }

    private List<TechId> findTrashedEntityIds(final List<TechId> deletionIds,
            final EntityKind entityKind)
    {
        final DetachedCriteria criteria =
                DetachedCriteria.forClass(entityKind.getDeletedEntityClass());
        final List<Long> longIds = TechId.asLongs(deletionIds);
        criteria.setProjection(Projections.id());
        criteria.add(Restrictions.in("deletion.id", longIds));
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
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("trashing %d %ss", updatedRows, entityKind.getLabel()));
        }
        hibernateTemplate.flush();

        List<Long> ids = TechId.asLongs(entityIds);
        scheduleRemoveFromFullTextIndex(ids, entityKind);

        return updatedRows;
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
}
