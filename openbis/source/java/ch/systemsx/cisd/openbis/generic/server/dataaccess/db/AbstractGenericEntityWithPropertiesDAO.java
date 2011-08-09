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
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

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

    protected void executePermanentDeleteAction(final EntityKind entityKind,
            final List<TechId> entityTechIds, final PersonPE registrator, final String reason,
            final String sqlSelectPermIds, final String sqlDeleteProperties,
            final String sqlSelectAttachmentContentIds, final String sqlDeleteAttachmentContents,
            final String sqlDeleteAttachments, final String sqlDeleteEntities,
            final String sqlInsertEvent, final String... additionalQueries)
    {
        List<Long> entityIds = TechId.asLongs(entityTechIds);
        DeletePermanentlyBatchOperation deleteOperation =
                new DeletePermanentlyBatchOperation(entityKind, entityIds, registrator, reason,
                        sqlSelectPermIds, sqlDeleteProperties, sqlSelectAttachmentContentIds,
                        sqlDeleteAttachmentContents, sqlDeleteAttachments, sqlDeleteEntities,
                        sqlInsertEvent, additionalQueries);
        BatchOperationExecutor.executeInBatches(deleteOperation);

        // FIXME remove this when we remove the switch to disable trash
        scheduleRemoveFromFullTextIndex(entityIds);
    }

    abstract Logger getLogger();

    class DeletePermanentlyBatchOperation implements IBatchOperation<Long>
    {

        private final EntityKind entityKind;

        private final List<Long> allEntityIds;

        private final PersonPE registrator;

        private final String reason;

        private final String sqlSelectPermIds;

        private final String sqlDeleteProperties;

        private final String sqlSelectAttachmentContentIds;

        private final String sqlDeleteAttachmentContents;

        private final String sqlDeleteAttachments;

        private final String sqlDeleteEntities;

        private final String sqlInsertEvent;

        private final String[] additionalQueries;

        DeletePermanentlyBatchOperation(EntityKind entityKind, List<Long> allEntityIds,
                PersonPE registrator, String reason, String sqlSelectPermIds,
                String sqlDeleteProperties, String sqlSelectAttachmentContentIds,
                String sqlDeleteAttachmentContents, String sqlDeleteAttachments,
                String sqlDeleteEntities, String sqlInsertEvent, String... additionalQueries)
        {
            this.entityKind = entityKind;
            this.allEntityIds = allEntityIds;
            this.registrator = registrator;
            this.reason = reason;
            this.sqlSelectPermIds = sqlSelectPermIds;
            this.sqlDeleteProperties = sqlDeleteProperties;
            this.sqlSelectAttachmentContentIds = sqlSelectAttachmentContentIds;
            this.sqlDeleteAttachmentContents = sqlDeleteAttachmentContents;
            this.sqlDeleteAttachments = sqlDeleteAttachments;
            this.sqlDeleteEntities = sqlDeleteEntities;
            this.sqlInsertEvent = sqlInsertEvent;
            this.additionalQueries = additionalQueries;
        }

        public void execute(final List<Long> batchEntityIds)
        {
            executeStatelessAction(new StatelessHibernateCallback()
                {
                    public Object doInStatelessSession(StatelessSession session)
                    {
                        final SQLQuery sqlQuerySelectPermIds =
                                session.createSQLQuery(sqlSelectPermIds);
                        final SQLQuery sqlQueryDeleteProperties =
                                session.createSQLQuery(sqlDeleteProperties);
                        final SQLQuery sqlQueryDeleteEntities =
                                session.createSQLQuery(sqlDeleteEntities);
                        final SQLQuery sqlQueryInsertEvent = session.createSQLQuery(sqlInsertEvent);
                        final SQLQuery sqlQuerySelectAttachmentContentIds =
                                session.createSQLQuery(sqlSelectAttachmentContentIds);
                        final SQLQuery sqlQueryDeleteAttachments =
                                session.createSQLQuery(sqlDeleteAttachments);
                        final SQLQuery sqlQueryDeleteAttachmentContents =
                                session.createSQLQuery(sqlDeleteAttachmentContents);
                        final List<SQLQuery> additionalSqlQueries = new ArrayList<SQLQuery>();
                        for (String queryString : additionalQueries)
                        {
                            additionalSqlQueries.add(session.createSQLQuery(queryString));
                        }

                        final List<String> permIds =
                                selectPermIds(batchEntityIds, sqlQuerySelectPermIds);
                        if (permIds.isEmpty())
                        {
                            return null;
                        }
                        deleteProperties(sqlQueryDeleteProperties, batchEntityIds);
                        deleteAttachmentsWithContents(sqlQuerySelectAttachmentContentIds,
                                sqlQueryDeleteAttachments, sqlQueryDeleteAttachmentContents,
                                batchEntityIds);
                        executeAdditionalQueries(additionalSqlQueries, batchEntityIds);
                        deleteMainEntities(sqlQueryDeleteEntities, batchEntityIds);
                        insertEvent(sqlQueryInsertEvent, permIds);
                        return null;
                    }

                    private List<String> selectPermIds(final List<Long> entityIds,
                            final SQLQuery sqlQuerySelectPermIds)
                    {
                        sqlQuerySelectPermIds.setParameterList(ENTITY_IDS_PARAM, entityIds);
                        final List<String> permIdsOrNull = cast(sqlQuerySelectPermIds.list());
                        return permIdsOrNull == null ? Collections.<String> emptyList()
                                : permIdsOrNull;
                    }

                    private void deleteProperties(final SQLQuery sqlQueryDeleteProperties,
                            List<Long> entityIds)
                    {
                        sqlQueryDeleteProperties.setParameterList(ENTITY_IDS_PARAM, entityIds);
                        sqlQueryDeleteProperties.executeUpdate();
                    }

                    private void deleteAttachmentsWithContents(
                            final SQLQuery sqlQueryAttachmentContentIds,
                            final SQLQuery sqlQueryDeleteAttachments,
                            final SQLQuery sqlQueryDeleteAttachmentContents, List<Long> entityIds)
                    {
                        sqlQueryAttachmentContentIds.setParameterList(ENTITY_IDS_PARAM, entityIds);
                        List<Long> attachmentContentIds = cast(sqlQueryAttachmentContentIds.list());
                        if (attachmentContentIds.size() > 0)
                        {
                            deleteProperties(sqlQueryDeleteAttachments, entityIds);
                            sqlQueryDeleteAttachmentContents.setParameterList(ATTACHMENT_IDS_PARAM,
                                    attachmentContentIds);
                            sqlQueryDeleteAttachmentContents.executeUpdate();
                        }
                    }

                    private void executeAdditionalQueries(
                            final List<SQLQuery> additionalSqlQueries, List<Long> entityIds)
                    {
                        for (SQLQuery query : additionalSqlQueries)
                        {
                            query.setParameter(ENTITY_IDS_PARAM, entityIds);
                            query.executeUpdate();
                        }
                    }

                    private void deleteMainEntities(final SQLQuery sqlQueryDeleteEntities,
                            List<Long> entityIds)
                    {
                        sqlQueryDeleteEntities.setParameterList(ENTITY_IDS_PARAM, entityIds);
                        sqlQueryDeleteEntities.executeUpdate();
                    }

                    private void insertEvent(final SQLQuery sqlQueryInsertEvent,
                            final List<String> permIds)
                    {
                        final String description =
                                CollectionUtils.abbreviate(permIds, 3, CollectionStyle.NO_BOUNDARY);
                        sqlQueryInsertEvent.setParameter(DESCRIPTION_PARAM, description);
                        sqlQueryInsertEvent.setParameter(EVENT_TYPE_PARAM,
                                EventType.DELETION.name());
                        sqlQueryInsertEvent.setParameter(REASON_PARAM, reason);
                        sqlQueryInsertEvent.setParameter(REGISTRATOR_ID_PARAM, registrator.getId());
                        sqlQueryInsertEvent.setParameter(ENTITY_TYPE_PARAM, entityKind.name());

                        final String allPermIdsAsString =
                                CollectionUtils
                                        .abbreviate(permIds, -1, CollectionStyle.NO_BOUNDARY);
                        sqlQueryInsertEvent.setParameter(IDENTIFIERS_PARAM, allPermIdsAsString);
                        sqlQueryInsertEvent.executeUpdate();
                    }
                });
        }

        public List<Long> getAllEntities()
        {
            return allEntityIds;
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

    static final String IDENTIFIERS_PARAM = "identifiers";

    static final String DESCRIPTION_PARAM = "description";

    static final String ENTITY_TYPE_PARAM = "entityType";

    static final String REGISTRATOR_ID_PARAM = "registratorId";

    static final String REASON_PARAM = "reason";

    static final String EVENT_TYPE_PARAM = "eventType";

    static final String ENTITY_IDS_PARAM = "entityIds";

    static final String ATTACHMENT_IDS_PARAM = "aIds";

    protected static class SQLBuilder
    {

        protected static String createSelectPermIdsSQL(final String entitiesTable)
        {
            return "SELECT perm_id FROM " + entitiesTable + " WHERE id " + inEntityIds();
        }

        protected static String createDeleteProperties(final String propertiesTable,
                String ownerColumnName)
        {
            return "DELETE FROM " + propertiesTable + " WHERE " + ownerColumnName + " "
                    + inEntityIds();
        }

        protected static String createSelectAttachmentContentIdsSQL(final String ownerColumnName)
        {
            return "SELECT exac_id FROM " + TableNames.ATTACHMENTS_TABLE + " WHERE "
                    + ownerColumnName + " " + inEntityIds();
        }

        protected static String createDeleteAttachmentContentsSQL()
        {
            return "DELETE FROM " + TableNames.ATTACHMENT_CONTENT_TABLE + " WHERE id "
                    + in(ATTACHMENT_IDS_PARAM);
        }

        protected static String createDeleteAttachmentsSQL()
        {
            return "DELETE FROM " + TableNames.ATTACHMENTS_TABLE + " WHERE samp_id "
                    + inEntityIds();
        }

        protected static String createDeleteEnitiesSQL(final String entitiesTable)
        {
            return "DELETE FROM " + entitiesTable + " WHERE id " + inEntityIds();
        }

        protected static String createInsertEventSQL()
        {
            return String
                    .format("INSERT INTO %s (id, event_type, description, reason, pers_id_registerer, entity_type, identifiers) "
                            + "VALUES (nextval('%s'), :%s, :%s, :%s, :%s, :%s, :%s)",
                            TableNames.EVENTS_TABLE, SequenceNames.EVENT_SEQUENCE,
                            EVENT_TYPE_PARAM, DESCRIPTION_PARAM, REASON_PARAM,
                            REGISTRATOR_ID_PARAM, ENTITY_TYPE_PARAM, IDENTIFIERS_PARAM);
        }

        protected static String inEntityIds()
        {
            return in(ENTITY_IDS_PARAM);
        }

        protected static String in(String paramName)
        {
            return "IN (:" + paramName + ")";
        }

    }
}
