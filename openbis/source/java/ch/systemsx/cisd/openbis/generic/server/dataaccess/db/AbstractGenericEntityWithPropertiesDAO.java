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

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.StatelessSession;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IFullTextIndexUpdateScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexUpdateOperation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Abstract super class of DAOs for entities that are indexed and contain properties.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractGenericEntityWithPropertiesDAO<T extends IEntityInformationWithPropertiesHolder>
        extends AbstractGenericEntityDAO<T>
{
    private final PersistencyResources persistencyResources;

    protected AbstractGenericEntityWithPropertiesDAO(
            final PersistencyResources persistencyResources,
            final DatabaseInstancePE databaseInstance, final Class<T> entityClass)
    {
        super(persistencyResources.getSessionFactoryOrNull(), databaseInstance, entityClass);
        this.persistencyResources = persistencyResources;
    }

    protected IFullTextIndexUpdateScheduler getIndexUpdateScheduler()
    {
        return persistencyResources.getIndexUpdateScheduler();
    }

    protected IDynamicPropertyEvaluationScheduler getDynamicPropertyEvaluatorScheduler()
    {
        return persistencyResources.getDynamicPropertyEvaluationScheduler();
    }

    @Override
    public void delete(T entity) throws DataAccessException
    {
        super.delete(entity);
        scheduleRemoveFromFullTextIndex(Collections.singletonList(entity.getId()));
    }

    protected void scheduleRemoveFromFullTextIndex(List<Long> ids)
    {
        getIndexUpdateScheduler()
                .scheduleUpdate(IndexUpdateOperation.remove(getEntityClass(), ids));
    }

    protected void scheduleDynamicPropertiesEvaluation(List<T> entities)
    {
        scheduleDynamicPropertiesEvaluation(getDynamicPropertyEvaluatorScheduler(),
                getEntityClass(), entities);
    }

    protected void scheduleDynamicPropertiesEvaluationByIds(List<Long> ids)
    {
        scheduleDynamicPropertiesEvaluationForIds(getDynamicPropertyEvaluatorScheduler(),
                getEntityClass(), ids);
    }

    protected void executeDeleteAction(final EntityKind entityKind, final List<TechId> entityIds,
            final PersonPE registrator, final String reason, final String sqlPermId,
            final String sqlDeleteProperties, final String sqlAttachmentContentIds,
            final String sqlDeleteAttachmentContents, final String sqlDeleteAttachments,
            final String sqlDeleteEntity, final String sqlInsertEvent)
    {
        executeStatelessAction(new StatelessHibernateCallback()
            {
                public Object doInStatelessSession(StatelessSession session)
                {
                    final SQLQuery sqlQueryPermId = session.createSQLQuery(sqlPermId);
                    final SQLQuery sqlQueryDeleteProperties =
                            session.createSQLQuery(sqlDeleteProperties);
                    final SQLQuery sqlQueryDeleteEntity = session.createSQLQuery(sqlDeleteEntity);
                    final SQLQuery sqlQueryInsertEvent = session.createSQLQuery(sqlInsertEvent);
                    final SQLQuery sqlQueryAttachmentContentIds =
                            session.createSQLQuery(sqlAttachmentContentIds);
                    final SQLQuery sqlQueryDeleteAttachments =
                            session.createSQLQuery(sqlDeleteAttachments);
                    final SQLQuery sqlQueryDeleteAttachmentContents =
                            session.createSQLQuery(sqlDeleteAttachmentContents);
                    sqlQueryInsertEvent.setParameter("eventType", EventType.DELETION.name());
                    sqlQueryInsertEvent.setParameter("reason", reason);
                    sqlQueryInsertEvent.setParameter("registratorId", registrator.getId());
                    sqlQueryInsertEvent.setParameter("entityType", entityKind.name());
                    int counter = 0;
                    for (TechId techId : entityIds)
                    {
                        sqlQueryPermId.setParameter("entityId", techId.getId());
                        final String permIdOrNull = tryGetEntity(sqlQueryPermId.uniqueResult());
                        if (permIdOrNull != null)
                        {
                            // delete properties
                            sqlQueryDeleteProperties.setParameter("entityId", techId.getId());
                            sqlQueryDeleteProperties.executeUpdate();
                            // delete attachments
                            sqlQueryAttachmentContentIds.setParameter("entityId", techId.getId());
                            List<Long> attachmentContentIds =
                                    cast(sqlQueryAttachmentContentIds.list());
                            if (attachmentContentIds.size() > 0)
                            {
                                sqlQueryDeleteAttachments.setParameter("entityId", techId.getId());
                                sqlQueryDeleteAttachments.executeUpdate();
                                sqlQueryDeleteAttachmentContents.setParameterList("aIds",
                                        attachmentContentIds);
                                sqlQueryDeleteAttachmentContents.executeUpdate();
                            }
                            // delete mainEntity
                            sqlQueryDeleteEntity.setParameter("entityId", techId.getId());
                            sqlQueryDeleteEntity.executeUpdate();
                            // create event
                            sqlQueryInsertEvent.setParameter("description", permIdOrNull);
                            sqlQueryInsertEvent.setParameter("identifier", permIdOrNull);
                            sqlQueryInsertEvent.executeUpdate();
                            if (++counter % 1000 == 0)
                            {
                                getLogger().info(
                                        String.format("%d %ss have been deleted...", counter,
                                                entityKind.name()));
                            }
                        }
                    }
                    return null;
                }
            });

        // FIXME remove this when we remove the switch to disable trash
        List<Long> ids = TechId.asLongs(entityIds);
        scheduleRemoveFromFullTextIndex(ids);
    }

    abstract Logger getLogger();

}
