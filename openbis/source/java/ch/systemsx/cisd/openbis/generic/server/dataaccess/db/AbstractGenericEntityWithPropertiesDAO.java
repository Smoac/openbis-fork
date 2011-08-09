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
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.StatelessSession;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.collections.CollectionStyle;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
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
            final String sqlDeleteEntity, final String sqlInsertEvent,
            final String... additionalQueries)
    {

        DeletePermanentlyBatchOperation deleteOperation =
                new DeletePermanentlyBatchOperation(entityKind, entityIds, registrator, reason,
                        sqlPermId, sqlDeleteProperties, sqlAttachmentContentIds,
                        sqlDeleteAttachmentContents, sqlDeleteAttachments, sqlDeleteEntity,
                        sqlInsertEvent, additionalQueries);
        BatchOperationExecutor.executeInBatches(deleteOperation);

        // FIXME remove this when we remove the switch to disable trash
        List<Long> ids = TechId.asLongs(entityIds);
        scheduleRemoveFromFullTextIndex(ids);
    }

    abstract Logger getLogger();

    class DeletePermanentlyBatchOperation implements IBatchOperation<TechId>
    {

        static final String IDENTIFIERS_PARAM = "identifiers";

        static final String DESCRIPTION_PARAM = "description";

        static final String ENTITY_TYPE_PARAM = "entityType";

        static final String REGISTRATOR_ID_PARAM = "registratorId";

        static final String REASON_PARAM = "reason";

        static final String EVENT_TYPE_PARAM = "eventType";

        static final String ENTITY_IDS_PARAM = "entityIds";

        static final String ATTACHMENT_IDS_PARAM = "aIds";

        private final EntityKind entityKind;

        private final List<TechId> entityTechIds;

        private final PersonPE registrator;

        private final String reason;

        private final String sqlPermId;

        private final String sqlDeleteProperties;

        private final String sqlAttachmentContentIds;

        private final String sqlDeleteAttachmentContents;

        private final String sqlDeleteAttachments;

        private final String sqlDeleteEntity;

        private final String sqlInsertEvent;

        private final String[] additionalQueries;

        DeletePermanentlyBatchOperation(EntityKind entityKind, List<TechId> entityTechIds,
                PersonPE registrator, String reason, String sqlPermId, String sqlDeleteProperties,
                String sqlAttachmentContentIds, String sqlDeleteAttachmentContents,
                String sqlDeleteAttachments, String sqlDeleteEntity, String sqlInsertEvent,
                String... additionalQueries)
        {
            this.entityKind = entityKind;
            this.entityTechIds = entityTechIds;
            this.registrator = registrator;
            this.reason = reason;
            this.sqlPermId = sqlPermId;
            this.sqlDeleteProperties = sqlDeleteProperties;
            this.sqlAttachmentContentIds = sqlAttachmentContentIds;
            this.sqlDeleteAttachmentContents = sqlDeleteAttachmentContents;
            this.sqlDeleteAttachments = sqlDeleteAttachments;
            this.sqlDeleteEntity = sqlDeleteEntity;
            this.sqlInsertEvent = sqlInsertEvent;
            this.additionalQueries = additionalQueries;
        }

        public void execute(final List<TechId> batchEntityTechIds)
        {
            executeStatelessAction(new StatelessHibernateCallback()
                {
                    public Object doInStatelessSession(StatelessSession session)
                    {
                        final SQLQuery sqlQueryPermId = session.createSQLQuery(sqlPermId);
                        final SQLQuery sqlQueryDeleteProperties =
                                session.createSQLQuery(sqlDeleteProperties);
                        final SQLQuery sqlQueryDeleteEntity =
                                session.createSQLQuery(sqlDeleteEntity);
                        final SQLQuery sqlQueryInsertEvent = session.createSQLQuery(sqlInsertEvent);
                        final SQLQuery sqlQueryAttachmentContentIds =
                                session.createSQLQuery(sqlAttachmentContentIds);
                        final SQLQuery sqlQueryDeleteAttachments =
                                session.createSQLQuery(sqlDeleteAttachments);
                        final SQLQuery sqlQueryDeleteAttachmentContents =
                                session.createSQLQuery(sqlDeleteAttachmentContents);

                        final List<SQLQuery> additionalSqlQueries = new ArrayList<SQLQuery>();
                        for (String queryString : additionalQueries)
                        {
                            additionalSqlQueries.add(session.createSQLQuery(queryString));
                        }

                        List<Long> entityIds = TechId.asLongs(batchEntityTechIds);

                        sqlQueryPermId.setParameterList(ENTITY_IDS_PARAM, entityIds);
                        final List<String> permIdsOrNull = cast(sqlQueryPermId.list());
                        if (permIdsOrNull == null || permIdsOrNull.isEmpty())
                        {
                            return null;
                        }

                        // delete properties
                        sqlQueryDeleteProperties.setParameterList(ENTITY_IDS_PARAM, entityIds);
                        sqlQueryDeleteProperties.executeUpdate();

                        // delete attachments
                        sqlQueryAttachmentContentIds.setParameterList(ENTITY_IDS_PARAM, entityIds);
                        List<Long> attachmentContentIds = cast(sqlQueryAttachmentContentIds.list());
                        if (attachmentContentIds.size() > 0)
                        {
                            sqlQueryDeleteAttachments.setParameterList(ENTITY_IDS_PARAM, entityIds);
                            sqlQueryDeleteAttachments.executeUpdate();
                            sqlQueryDeleteAttachmentContents.setParameterList(ATTACHMENT_IDS_PARAM,
                                    attachmentContentIds);
                            sqlQueryDeleteAttachmentContents.executeUpdate();
                        }

                        // additional queries (optional)
                        for (SQLQuery query : additionalSqlQueries)
                        {
                            query.setParameter(ENTITY_IDS_PARAM, entityIds);
                            query.executeUpdate();
                        }

                        // delete mainEntity
                        sqlQueryDeleteEntity.setParameterList(ENTITY_IDS_PARAM, entityIds);
                        sqlQueryDeleteEntity.executeUpdate();

                        // create event
                        sqlQueryInsertEvent.setParameter(EVENT_TYPE_PARAM,
                                EventType.DELETION.name());
                        sqlQueryInsertEvent.setParameter(REASON_PARAM, reason);
                        sqlQueryInsertEvent.setParameter(REGISTRATOR_ID_PARAM, registrator.getId());
                        sqlQueryInsertEvent.setParameter(ENTITY_TYPE_PARAM, entityKind.name());
                        final String description =
                                CollectionUtils.abbreviate(permIdsOrNull, 3,
                                        CollectionStyle.NO_BOUNDARY);
                        sqlQueryInsertEvent.setParameter(DESCRIPTION_PARAM, description);

                        final String allPermIdsAsString =
                                CollectionUtils.abbreviate(permIdsOrNull, -1,
                                        CollectionStyle.NO_BOUNDARY);
                        sqlQueryInsertEvent.setParameter(IDENTIFIERS_PARAM, allPermIdsAsString);
                        sqlQueryInsertEvent.executeUpdate();
                        return null;
                    }
                });
        }

        public List<TechId> getAllEntities()
        {
            return entityTechIds;
        }

        public String getEntityName()
        {
            return entityKind.getDescription();
        }

        public String getOperationName()
        {
            return "permanently deleting";
        }

    }

}
