/*
 * Copyright 2008 ETH Zuerich, CISD
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.ResultTransformer;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.collection.CollectionStyle;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.collection.IToStringConverter;
import ch.systemsx.cisd.common.collection.ToStringDefaultConverter;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Implementation of {@link IDataDAO} for databases.
 * 
 * @author Christian Ribeaud
 */
final class DataDAO extends AbstractGenericEntityWithPropertiesDAO<DataPE> implements IDataDAO
{
    private final int MAX_BATCH_SIZE = 999;

    private final static Class<DataPE> ENTITY_CLASS = DataPE.class;

    private final static Class<ExternalDataPE> EXTERNAL_DATA_ENTITY_CLASS = ExternalDataPE.class;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataDAO.class);

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    private static final String EXTERNAL_DATA_TABLE_NAME = EXTERNAL_DATA_ENTITY_CLASS
            .getSimpleName();

    DataDAO(final PersistencyResources persistencyResources,
            final DatabaseInstancePE databaseInstance)
    {
        super(persistencyResources, databaseInstance, ENTITY_CLASS);
    }

    //
    // IExternalDataDAO
    //

    @Override
    public boolean hasDataSet(SamplePE sample) throws DataAccessException
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(ExternalDataPE.class);
        criteria.add(Restrictions.eq("sampleInternal", sample));
        criteria.setProjection(Projections.rowCount());
        Integer count =
                ((Number) getHibernateTemplate().findByCriteria(criteria).get(0)).intValue();
        return count > 0;
    }

    @Override
    public final List<DataPE> listRelatedDataSets(final List<IEntityInformationHolder> entities,
            EntityKind entityKind) throws DataAccessException
    {
        assert entities != null : "Unspecified entities.";
        assert entities.size() > 0 : "Empty entities.";
        assert entityKind != null : "Unspecified entity kind.";

        final String entityName = entityKind.toString().toLowerCase();
        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.sampleInternal "
                        + "left join fetch e.dataSetParentRelationships "
                        + "left join fetch e.containedDataSets "
                        + "left join fetch e.dataSetProperties "
                        + "where e.%sInternal.id IN (:ids)", TABLE_NAME, entityName);

        final List<Long> ids = new ArrayList<Long>();
        for (IEntityInformationHolder entity : entities)
        {
            ids.add(entity.getId());
        }
        final List<DataPE> results = new ArrayList<DataPE>();
        BatchOperationExecutor.executeInBatches(new IBatchOperation<Long>()
            {
                @Override
                public void execute(List<Long> entityIds)
                {

                    final List<DataPE> list =
                            cast(getHibernateTemplate().findByNamedParam(query, "ids", entityIds));
                    results.addAll(list);
                }

                @Override
                public List<Long> getAllEntities()
                {
                    return ids;
                }

                @Override
                public String getEntityName()
                {
                    return "dataSet";
                }

                @Override
                public String getOperationName()
                {
                    return "listRelatedDataSets";
                }
            });

        distinct(results);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d external data have been found for %d entities.",
                    results.size(), entities.size()));
        }
        return results;
    }

    @Override
    public final List<DataPE> listDataSets(final SamplePE sample) throws DataAccessException
    {
        assert sample != null : "Unspecified sample.";

        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.dataSetParentRelationships "
                        + "left join fetch e.dataSetProperties " + "where e.sampleInternal = ?",
                        TABLE_NAME);
        final List<DataPE> list = cast(getHibernateTemplate().find(query, toArray(sample)));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d external data have been found for [sample=%s].",
                    list.size(), sample));
        }
        return list;
    }

    @Override
    public final List<DataPE> listExternalData(final DataStorePE dataStore)
            throws DataAccessException
    {
        assert dataStore != null : "Unspecified data store.";
        final DetachedCriteria criteria = DetachedCriteria.forClass(ExternalDataPE.class);
        criteria.add(Restrictions.eq("dataStore", dataStore));
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d data set(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), dataStore, list.size()));
        }
        return list;
    }

    @Override
    public final List<DataPE> listDataSets(final ExperimentPE experiment)
            throws DataAccessException
    {
        assert experiment != null : "Unspecified experiment.";

        final String query =
                String.format(
                        "from %s e " + "left join fetch e.experimentInternal "
                                + "left join fetch e.dataSetParentRelationships "
                                + "left join fetch e.dataSetProperties "
                                + "where e.experimentInternal = ?", TABLE_NAME);
        final List<DataPE> list = cast(getHibernateTemplate().find(query, toArray(experiment)));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d external data have been found for [experiment=%s].", list.size(),
                    experiment));
        }
        return list;
    }

    private void distinct(List<DataPE> list)
    {
        Set<DataPE> set = new TreeSet<DataPE>(list);
        list.clear();
        list.addAll(set);
    }

    @Override
    public DataPE tryToFindDataSetByCode(String dataSetCode)
    {
        assert dataSetCode != null : "Unspecified data set code.";

        final String mangledCode = CodeConverter.tryToDatabase(dataSetCode);
        final Criterion codeEq = Restrictions.eq("code", mangledCode);

        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_CLASS);
        criteria.add(codeEq);
        criteria.setFetchMode("dataSetType", FetchMode.JOIN);
        criteria.setFetchMode("dataStore", FetchMode.JOIN);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        final DataPE entity = tryFindEntity(list, "data set");

        if (operationLog.isDebugEnabled())
        {
            String methodName = MethodUtils.getCurrentMethod().getName();
            operationLog.debug(String.format("%s(%s): '%s'.", methodName, dataSetCode, entity));
        }
        return entity;
    }

    @Override
    public List<DeletedDataPE> tryToFindDeletedDataSetsByCodes(Collection<String> dataSetCodes)
    {
        assert dataSetCodes != null : "Unspecified collection";

        if (dataSetCodes.size() == 0)
        {
            return Collections.emptyList();
        }

        final List<DeletedDataPE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            final DetachedCriteria criteria =
                                    DetachedCriteria.forClass(DeletedDataPE.class);
                            criteria.setFetchMode("dataStore", FetchMode.SELECT);
                            criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
                            return criteria;
                        }
                    }, "code", dataSetCodes);

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Found '%s' data sets for codes '%s'.", list.size(),
                    dataSetCodes));
        }
        return list;
    }

    @Override
    public List<DataPE> tryToFindFullDataSetsByIds(Collection<Long> ids, boolean withPropertyTypes,
            boolean lockForUpdate)
    {
        return tryToFindFullDataSets("id", ids, withPropertyTypes, lockForUpdate);
    }

    @Override
    public List<DataPE> tryToFindFullDataSetsByCodes(Collection<String> dataSetCodes,
            boolean withPropertyTypes, boolean lockForUpdate)
    {
        return tryToFindFullDataSets("code", dataSetCodes, withPropertyTypes, lockForUpdate);
    }

    private List<DataPE> tryToFindFullDataSets(String identifierColumn, Collection<?> identifiers,
            boolean withPropertyTypes, boolean lockForUpdate)
    {
        assert identifiers != null : "Unspecified collection";

        // PostgreSQL has a limit of 2^15-1 values in an IN clause, so we need to split the data set
        // codes into chunks to avoid a
        // java.io.IOException: Tried to send an out-of-range integer as a 2-byte value: nn
        // However, we know from experience that using chunk sizes larger than 999 we get bad
        // performance, so we split the query in chunks of 999 rather than 2^15-1.
        final int len = identifiers.size();
        if (len > MAX_BATCH_SIZE)
        {
            final List<DataPE> result = new ArrayList<DataPE>(identifiers.size());
            final List<?> dataSetCodesList = new ArrayList<Object>(identifiers);
            int startIndex = 0;
            int endIndex = MAX_BATCH_SIZE;
            while (endIndex > startIndex)
            {
                result.addAll(primFindFullDataSetsByCode(identifierColumn,
                        dataSetCodesList.subList(startIndex, endIndex), withPropertyTypes,
                        lockForUpdate));
                startIndex = endIndex;
                endIndex = Math.min(endIndex + MAX_BATCH_SIZE, len);
            }
            return result;
        } else
        {
            return primFindFullDataSetsByCode(identifierColumn, identifiers, withPropertyTypes,
                    lockForUpdate);
        }
    }

    private List<DataPE> primFindFullDataSetsByCode(String identifierColumn,
            Collection<?> identifiers, final boolean withPropertyTypes, final boolean lockForUpdate)
    {

        if (identifiers.size() == 0)
        {
            return Collections.emptyList();
        }

        final List<DataPE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            final DetachedCriteria criteria =
                                    DetachedCriteria.forClass(ENTITY_CLASS);
                            criteria.setFetchMode("dataSetType", FetchMode.SELECT);
                            criteria.setFetchMode("dataStore", FetchMode.SELECT);
                            criteria.setFetchMode("experimentInternal", FetchMode.SELECT);
                            criteria.setFetchMode("sampleInternal", FetchMode.SELECT);
                            criteria.setFetchMode("fileFormat", FetchMode.SELECT);
                            if (withPropertyTypes)
                            {
                                criteria.setFetchMode(
                                        "dataSetType.dataSetTypePropertyTypesInternal",
                                        FetchMode.JOIN);
                            }
                            criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

                            /**
                             * lockForUpdate parameter is ignored. See LMS-2882 details
                             */
                            /*
                             * if (lockForUpdate) { criteria.setLockMode(LockMode.UPGRADE); }
                             */
                            return criteria;
                        }
                    }, identifierColumn, identifiers);

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Found '%s' data sets for codes '%s'.", list.size(),
                    identifiers));
        }
        return list;
    }

    @Override
    public DataPE tryToFindFullDataSetByCode(String dataSetCode, boolean withPropertyTypes,
            boolean lockForUpdate)
    {
        assert dataSetCode != null : "Unspecified data set code";

        final String mangledCode = CodeConverter.tryToDatabase(dataSetCode);
        final Criterion codeEq = Restrictions.eq("code", mangledCode);

        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_CLASS);
        criteria.add(codeEq);
        criteria.setFetchMode("dataSetType", FetchMode.SELECT);
        criteria.setFetchMode("dataStore", FetchMode.SELECT);
        criteria.setFetchMode("experimentInternal", FetchMode.SELECT);
        criteria.setFetchMode("sampleInternal", FetchMode.SELECT);
        criteria.setFetchMode("fileFormat", FetchMode.SELECT);
        if (withPropertyTypes)
        {
            criteria.setFetchMode("dataSetType.dataSetTypePropertyTypesInternal", FetchMode.JOIN);
        }
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        /**
         * lockForUpdate parameter is ignored. See LMS-2882 details
         */
        /*
         * if (lockForUpdate) { criteria.setLockMode(LockMode.UPGRADE); }
         */
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        final DataPE entity = tryFindEntity(list, "data set");

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Data Set '%s' found for data set code '%s'.", entity,
                    dataSetCode));
        }
        return entity;
    }

    @Override
    public void updateDataSetStatuses(final List<String> dataSetCodes,
            final DataSetArchivingStatus status)
    {
        assert dataSetCodes != null : "Unspecified data set codes";
        assert status != null : "Unspecified code";

        if (dataSetCodes.size() == 0)
        {
            return;
        }

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        // PostgreSQL has a limit of 2^15-1 values in an IN clause, so we need to split the data set
        // codes into chunks to avoid a
        // java.io.IOException: Tried to send an out-of-range integer as a 2-byte value: nn
        // However, we know from experience that using chunk sizes larger than 999 we get bad
        // performance, so we split the query in chunks of 999 rather than 2^15-1.
        final int len = dataSetCodes.size();
        int updatedRows = 0;
        if (len > MAX_BATCH_SIZE)
        {
            int startIndex = 0;
            int endIndex = MAX_BATCH_SIZE;
            while (endIndex > startIndex)
            {
                final int startIndexFinal = startIndex;
                final int endIndexFinal = endIndex;
                updatedRows += (Integer) hibernateTemplate.execute(new HibernateCallback()
                    {

                        //
                        // HibernateCallback
                        //

                        @Override
                        public final Object doInHibernate(final Session session)
                                throws HibernateException, SQLException
                        {
                            // NOTE: 'VERSIONED' makes modification time modified too
                            return session
                                    .createQuery(
                                            "UPDATE VERSIONED "
                                                    + EXTERNAL_DATA_TABLE_NAME
                                                    + " SET status = :status WHERE code IN (:codes) ")
                                    .setParameter("status", status)
                                    .setParameterList("codes",
                                            dataSetCodes.subList(startIndexFinal, endIndexFinal))
                                    .executeUpdate();
                        }
                    });
                startIndex = endIndex;
                endIndex = Math.min(endIndex + MAX_BATCH_SIZE, len);
            }
        } else
        {
            updatedRows = (Integer) hibernateTemplate.execute(new HibernateCallback()
                {

                    //
                    // HibernateCallback
                    //

                    @Override
                    public final Object doInHibernate(final Session session)
                            throws HibernateException, SQLException
                    {
                        // NOTE: 'VERSIONED' makes modification time modified too
                        return session
                                .createQuery(
                                        "UPDATE VERSIONED " + EXTERNAL_DATA_TABLE_NAME
                                                + " SET status = :status WHERE code IN (:codes) ")
                                .setParameter("status", status)
                                .setParameterList("codes", dataSetCodes).executeUpdate();
                    }
                });
        }
        hibernateTemplate.flush();
        if (updatedRows != dataSetCodes.size())
        {
            throw UserFailureException.fromTemplate("Update of %s data set statuses to %s failed.",
                    dataSetCodes.size(), status);
        } else if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATED: %s data set statuses to '%s'.",
                    dataSetCodes.size(), status));
        }
    }

    @Override
    public void updateDataSetStatuses(final List<String> dataSetCodes,
            final DataSetArchivingStatus status, final boolean presentInArchive)
    {
        assert dataSetCodes != null : "Unspecified data set codes";
        assert status != null : "Unspecified code";

        if (dataSetCodes.size() == 0)
        {
            return;
        }

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        // PostgreSQL has a limit of 2^15-1 values in an IN clause, so we need to split the data set
        // codes into chunks to avoid a
        // java.io.IOException: Tried to send an out-of-range integer as a 2-byte value: nn
        // However, we know from experience that using chunk sizes larger than 999 we get bad
        // performance, so we split the query in chunks of 999 rather than 2^15-1.
        final int len = dataSetCodes.size();
        int updatedRows = 0;
        if (len > MAX_BATCH_SIZE)
        {
            int startIndex = 0;
            int endIndex = MAX_BATCH_SIZE;
            while (endIndex > startIndex)
            {
                final int startIndexFinal = startIndex;
                final int endIndexFinal = endIndex;
                updatedRows += (Integer) hibernateTemplate.execute(new HibernateCallback()
                    {

                        //
                        // HibernateCallback
                        //

                        @Override
                        public final Object doInHibernate(final Session session)
                                throws HibernateException, SQLException
                        {
                            // NOTE: 'VERSIONED' makes modification time modified too
                            return session
                                    .createQuery(
                                            "UPDATE VERSIONED "
                                                    + EXTERNAL_DATA_TABLE_NAME
                                                    + " SET status = :status, presentInArchive = :presentInArchive"
                                                    + " WHERE code IN (:codes) ")
                                    .setParameter("status", status)
                                    .setParameter("presentInArchive", presentInArchive)
                                    .setParameterList("codes",
                                            dataSetCodes.subList(startIndexFinal, endIndexFinal))
                                    .executeUpdate();
                        }
                    });
                startIndex = endIndex;
                endIndex = Math.min(endIndex + MAX_BATCH_SIZE, len);
            }
        } else
        {
            updatedRows = (Integer) hibernateTemplate.execute(new HibernateCallback()
                {

                    //
                    // HibernateCallback
                    //

                    @Override
                    public final Object doInHibernate(final Session session)
                            throws HibernateException, SQLException
                    {
                        // NOTE: 'VERSIONED' makes modification time modified too
                        return session
                                .createQuery(
                                        "UPDATE VERSIONED "
                                                + EXTERNAL_DATA_TABLE_NAME
                                                + " SET status = :status, presentInArchive = :presentInArchive"
                                                + " WHERE code IN (:codes) ")
                                .setParameter("status", status)
                                .setParameter("presentInArchive", presentInArchive)
                                .setParameterList("codes", dataSetCodes).executeUpdate();
                    }
                });
        }
        hibernateTemplate.flush();
        if (updatedRows != dataSetCodes.size())
        {
            throw UserFailureException.fromTemplate(
                    "Update of %s data set statuses to '%s' and presentInArchive to '%s' failed.",
                    dataSetCodes.size(), status, presentInArchive);
        } else if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "UPDATED: %s data set statuses to '%s' and presentInArchive flag to '%s'.",
                    dataSetCodes.size(), status, presentInArchive));
        }
    }

    @Override
    public void createDataSet(DataPE dataset, PersonPE modifier)
    {
        assert dataset != null : "Unspecified data set.";

        dataset.setCode(CodeConverter.tryToDatabase(dataset.getCode()));
        dataset.setModifier(modifier);
        if (false == dataset.isPlaceholder())
        {
            validatePE(dataset);
        }

        final HibernateTemplate template = getHibernateTemplate();
        lockRelatedEntities(dataset);
        template.save(dataset);
        template.flush();
        scheduleDynamicPropertiesEvaluation(Collections.singletonList(dataset));

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: data set '%s'.", dataset));
        }
    }

    private void lockRelatedEntities(DataPE data)
    {
        lockEntity(data.getExperiment());
        lockEntity(data.tryGetSample());
        lockEntity(data.getContainer());
        lockEntities(data.getParents());
        lockEntities(data.getChildren());
    }

    @Override
    public void updateDataSet(DataPE data, PersonPE modifier)
    {
        assert data != null : "Given external data can not be null.";
        validatePE(data);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        data.setCode(CodeConverter.tryToDatabase(data.getCode()));
        data.setModifier(modifier);
        Long id = HibernateUtils.getId(data);
        final DataPE loaded = (DataPE) hibernateTemplate.load(ENTITY_CLASS, id);
        // This just means that we do not have any entry in 'EXTERNAL_DATA' table for this id. It
        // might happen when we work with placeholder data.
        if (loaded.isPlaceholder() && data instanceof ExternalDataPE)
        {
            ExternalDataPE externalData = (ExternalDataPE) data;
            String shareId = externalData.getShareId();
            String location = externalData.getLocation();
            Long size = externalData.getSize();
            Long locatorTypeID = externalData.getLocatorType().getId();
            Long fileFormatTypeID = externalData.getFileFormatType().getId();
            char complete = externalData.getComplete().name().charAt(0);
            Long storageFormatTermID = externalData.getStorageFormatVocabularyTerm().getId();
            if (size == null)
            {
                executeUpdate(
                        "insert into "
                                + TableNames.EXTERNAL_DATA_TABLE
                                + " (data_id, share_id, location, loty_id, ffty_id, is_complete, cvte_id_stor_fmt) "
                                + "values (?, ?, ?, ?, ?, ?, ?)", id, shareId, location,
                        locatorTypeID, fileFormatTypeID, complete, storageFormatTermID);
            } else
            {
                executeUpdate(
                        "insert into "
                                + TableNames.EXTERNAL_DATA_TABLE
                                + " (data_id, share_id, location, size, loty_id, ffty_id, is_complete, cvte_id_stor_fmt) "
                                + "values (?, ?, ?, ?, ?, ?, ?, ?)", id, shareId, location, size,
                        locatorTypeID, fileFormatTypeID, complete, storageFormatTermID);
            }
            hibernateTemplate.evict(loaded);
        }
        lockRelatedEntities(data);
        hibernateTemplate.update(data);
        hibernateTemplate.flush();
        scheduleDynamicPropertiesEvaluation(Collections.singletonList(data));

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: external data '%s'.", data));
        }
    }

    @Override
    public void delete(DataPE entity) throws DataAccessException
    {
        assert entity != null : "entity unspecified";

        // FIXME: Pawel Glyzewski shouldn't be deleted by cascade?
        // Remove children & components and flush changes before deletion.
        // Otherwise constraint violation exception will be thrown.
        // List<DataPE> children = new ArrayList<DataPE>(entity.getChildren());
        // for (DataPE child : children)
        // {
        // child.removeParent(entity);
        // }
        if (entity.isContainer())
        {
            List<DataPE> components = new ArrayList<DataPE>(entity.getContainedDataSets());
            for (DataPE component : components)
            {
                entity.removeComponent(component);
            }
        }
        flush();
        super.delete(entity);
    }

    @Override
    public void delete(final List<TechId> dataIds, final PersonPE registrator, final String reason)
            throws DataAccessException
    {
        // NOTE: we use DATA_ALL_TABLE, not DELETED_DATA_VIEW because we still want to be
        // able to directly delete data without going to trash (trash may be disabled)
        final String dataTable = TableNames.DATA_ALL_TABLE;

        // For data sets we load codes, which are logically the same as perm ids.
        // We load them ordered by ID because locations need to be loaded in the same order.
        final String sqlSelectPermIds = createSelectCodesOrderByIdSQL(dataTable);
        final String sqlSelectLocations = createSelectLocationsOrderByIdSQL(dataTable);

        final String sqlDeleteProperties =
                SQLBuilder.createDeletePropertiesSQL(TableNames.DATA_SET_PROPERTIES_TABLE,
                        ColumnNames.DATA_SET_COLUMN);
        final String sqlDeleteDataSets = SQLBuilder.createDeleteEnitiesSQL(dataTable);
        final String sqlInsertEvent = SQLBuilder.createInsertEventSQL();
        // data set specific queries
        final String sqlDeleteExternalData = createDeleteExternalDataSQL();
        final String sqlDeleteChildrenConnections = createDeleteChildrenConnectionsSQL();
        final String sqlDeleteParentConnections = createDeleteParentConnectionsSQL();
        final String sqlDeleteComponentConnections = createDeleteComponentConnectionsSQL();

        executePermanentDeleteOfDataSets(EntityType.DATASET, dataIds, registrator, reason,
                sqlSelectPermIds, sqlSelectLocations, sqlDeleteProperties, sqlDeleteDataSets,
                sqlInsertEvent, sqlDeleteExternalData, sqlDeleteChildrenConnections,
                sqlDeleteParentConnections, sqlDeleteComponentConnections);
    }

    protected void executePermanentDeleteOfDataSets(final EntityType entityType,
            final List<TechId> entityTechIds, final PersonPE registrator, final String reason,
            final String sqlSelectPermIds, final String sqlSelectLocations,
            final String sqlDeleteProperties, final String sqlDeleteEntities,
            final String sqlInsertEvent, final String... additionalQueries)
    {
        List<Long> entityIds = TechId.asLongs(entityTechIds);
        DeleteDataSetsPermanentlyBatchOperation deleteOperation =
                new DeleteDataSetsPermanentlyBatchOperation(entityType, entityIds, registrator,
                        reason, sqlSelectPermIds, sqlSelectLocations, sqlDeleteProperties,
                        sqlDeleteEntities, sqlInsertEvent, additionalQueries);
        BatchOperationExecutor.executeInBatches(deleteOperation);

        // FIXME remove this when we remove the switch to disable trash
        scheduleRemoveFromFullTextIndex(entityIds);
    }

    private static String createSelectCodesOrderByIdSQL(final String dataSetsTable)
    {
        return "SELECT code FROM " + dataSetsTable + " WHERE id " + SQLBuilder.inEntityIds()
                + " ORDER BY id";
    }

    private static String createSelectLocationsOrderByIdSQL(final String dataSetsTable)
    {
        return "SELECT ed.location, ed.share_id, ds.code FROM " + dataSetsTable + " d "
                + "JOIN data_stores ds ON (d.dast_id = ds.id) "
                + "LEFT OUTER JOIN external_data ed ON (d.id = ed.data_id) WHERE d.id "
                + SQLBuilder.inEntityIds() + " ORDER BY d.id";
    }

    private static String createDeleteExternalDataSQL()
    {
        return "DELETE FROM " + TableNames.EXTERNAL_DATA_TABLE + " WHERE data_id "
                + SQLBuilder.inEntityIds();
    }

    private static String createDeleteChildrenConnectionsSQL()
    {
        return "DELETE FROM " + TableNames.DATA_SET_RELATIONSHIPS_ALL_TABLE
                + " WHERE data_id_parent " + SQLBuilder.inEntityIds();
    }

    private static String createDeleteParentConnectionsSQL()
    {
        return "DELETE FROM " + TableNames.DATA_SET_RELATIONSHIPS_ALL_TABLE
                + " WHERE data_id_child " + SQLBuilder.inEntityIds();
    }

    private static String createDeleteComponentConnectionsSQL()
    {
        return "UPDATE " + TableNames.DATA_ALL_TABLE + " SET ctnr_id = NULL WHERE ctnr_id "
                + SQLBuilder.inEntityIds();
    }

    // TODO refactor - it is very similar code to the one in AbstractGenericEntityWithPropertiesDAO
    protected class DeleteDataSetsPermanentlyBatchOperation implements IBatchOperation<Long>
    {

        private final EntityType entityType;

        private final List<Long> allEntityIds;

        private final PersonPE registrator;

        private final String reason;

        private final String sqlSelectPermIds;

        private final String sqlSelectLocations;

        private final String sqlDeleteProperties;

        private final String sqlDeleteEntities;

        private final String sqlInsertEvent;

        private final String[] additionalQueries;

        public DeleteDataSetsPermanentlyBatchOperation(EntityType entityType,
                List<Long> allEntityIds, PersonPE registrator, String reason,
                String sqlSelectPermIds, String sqlSelectLocations, String sqlDeleteProperties,
                String sqlDeleteEntities, String sqlInsertEvent, String... additionalQueries)
        {
            this.entityType = entityType;
            this.allEntityIds = allEntityIds;
            this.registrator = registrator;
            this.reason = reason;
            this.sqlSelectPermIds = sqlSelectPermIds;
            this.sqlSelectLocations = sqlSelectLocations;
            this.sqlDeleteProperties = sqlDeleteProperties;
            this.sqlDeleteEntities = sqlDeleteEntities;
            this.sqlInsertEvent = sqlInsertEvent;
            this.additionalQueries = additionalQueries;
        }

        @Override
        public List<Long> getAllEntities()
        {
            return allEntityIds;
        }

        @Override
        public String getEntityName()
        {
            return entityType.name();
        }

        @Override
        public String getOperationName()
        {
            return "permanently deleting";
        }

        @Override
        public void execute(final List<Long> batchEntityIds)
        {
            executeStatelessAction(createPermanentDeleteAction(batchEntityIds));
        }

        private StatelessHibernateCallback createPermanentDeleteAction(
                final List<Long> entityIdsToDelete)
        {
            return new PermanentDeletionAction(entityIdsToDelete);
        }

        private class PermanentDeletionAction implements StatelessHibernateCallback
        {
            private final List<Long> entityIdsToDelete;

            public PermanentDeletionAction(List<Long> entityIdsToDelete)
            {
                this.entityIdsToDelete = entityIdsToDelete;
            }

            @Override
            public Object doInStatelessSession(StatelessSession session)
            {
                final SQLQuery sqlQuerySelectPermIds = session.createSQLQuery(sqlSelectPermIds);
                final SQLQuery sqlQuerySelectLocations = session.createSQLQuery(sqlSelectLocations);
                final SQLQuery sqlQueryDeleteProperties =
                        session.createSQLQuery(sqlDeleteProperties);
                final SQLQuery sqlQueryDeleteEntities = session.createSQLQuery(sqlDeleteEntities);
                final SQLQuery sqlQueryInsertEvent = session.createSQLQuery(sqlInsertEvent);
                final List<SQLQuery> additionalSqlQueries = new ArrayList<SQLQuery>();
                for (String queryString : additionalQueries)
                {
                    additionalSqlQueries.add(session.createSQLQuery(queryString));
                }

                final List<String> permIds =
                        selectPermIds(sqlQuerySelectPermIds, entityIdsToDelete);
                if (permIds.isEmpty())
                {
                    return null;
                }
                final List<DeletedDataSetLocation> locations =
                        selectLocations(sqlQuerySelectLocations, entityIdsToDelete);

                deleteProperties(sqlQueryDeleteProperties, entityIdsToDelete);
                executeAdditionalQueries(additionalSqlQueries, entityIdsToDelete);
                deleteMainEntities(sqlQueryDeleteEntities, entityIdsToDelete);
                insertEvent(sqlQueryInsertEvent, permIds, locations);
                return null;
            }

            private List<String> selectPermIds(final SQLQuery sqlQuerySelectPermIds,
                    final List<Long> entityIds)
            {
                sqlQuerySelectPermIds.setParameterList(ENTITY_IDS_PARAM, entityIds);
                final List<String> permIdsOrNull = cast(sqlQuerySelectPermIds.list());
                return permIdsOrNull == null ? Collections.<String> emptyList() : permIdsOrNull;
            }

            private List<DeletedDataSetLocation> selectLocations(
                    final SQLQuery sqlQuerySelectLocations, final List<Long> entityIds)
            {
                sqlQuerySelectLocations.setParameterList(ENTITY_IDS_PARAM, entityIds);
                sqlQuerySelectLocations.setResultTransformer(new ResultTransformer()
                    {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public Object transformTuple(Object[] values, String[] aliases)
                        {
                            DeletedDataSetLocation location = new DeletedDataSetLocation();
                            location.setLocation((String) values[0]);
                            location.setShareId((String) values[1]);
                            location.setDatastoreCode((String) values[2]);
                            return location;
                        }

                        @SuppressWarnings("rawtypes")
                        @Override
                        public List transformList(List list)
                        {
                            return list;
                        }
                    });
                return cast(sqlQuerySelectLocations.list());
            }

            private void deleteProperties(final SQLQuery sqlQueryDeleteProperties,
                    List<Long> entityIds)
            {
                sqlQueryDeleteProperties.setParameterList(ENTITY_IDS_PARAM, entityIds);
                sqlQueryDeleteProperties.executeUpdate();
            }

            private void executeAdditionalQueries(final List<SQLQuery> additionalSqlQueries,
                    List<Long> entityIds)
            {
                for (SQLQuery query : additionalSqlQueries)
                {
                    query.setParameterList(ENTITY_IDS_PARAM, entityIds);
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
                    final List<String> permIds, final List<DeletedDataSetLocation> locations)
            {
                sqlQueryInsertEvent.setParameter(EVENT_TYPE_PARAM, EventType.DELETION.name());
                sqlQueryInsertEvent.setParameter(REASON_PARAM, reason);
                sqlQueryInsertEvent.setParameter(REGISTRATOR_ID_PARAM, registrator.getId());
                sqlQueryInsertEvent.setParameter(ENTITY_TYPE_PARAM, entityType.name());

                IToStringConverter<Object> toStringConverter = new IToStringConverter<Object>()
                    {
                        IToStringConverter<Object> delegatee = ToStringDefaultConverter
                                .getInstance();

                        @Override
                        public String toString(Object value)
                        {
                            return value == null ? "" : delegatee.toString(value);
                        }
                    };

                final String allPermIdsAsString =
                        CollectionUtils.abbreviate(permIds, -1, toStringConverter,
                                CollectionStyle.NO_BOUNDARY);
                sqlQueryInsertEvent.setParameter(IDENTIFIERS_PARAM, allPermIdsAsString);

                sqlQueryInsertEvent.setParameter(DESCRIPTION_PARAM,
                        DeletedDataSetLocation.format(locations));

                sqlQueryInsertEvent.executeUpdate();
            }
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<TechId> findParentIds(final Collection<TechId> dataSetIds)
    {
        // Native SQL query is used to be able to query on 'many-to-many association table -
        // - 'data_set_relationships' without join with 'data' table involved in this association.
        // Drawback of this solution is that instead of list of Long values we get list of
        // BigIntegers and so a transformation is needed.

        final String query =
                "select data_id_parent from data_set_relationships where data_id_child in (:ids)";
        final List<? extends Number> results =
                (List<? extends Number>) getHibernateTemplate().execute(new HibernateCallback()
                    {

                        //
                        // HibernateCallback
                        //

                        @Override
                        public final Object doInHibernate(final Session session)
                        {
                            // we could remove this transformation if we choose to pass Long values
                            final List<Long> longIds = TechId.asLongs(dataSetIds);
                            return session.createSQLQuery(query).setParameterList("ids", longIds)
                                    .list();
                        }
                    });
        return transformNumbers2TechIdSet(results);
    }

    // data set relationship helper methods

    @Override
    public List<DataPE> listByCode(Set<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return new ArrayList<DataPE>();
        }
        final List<DataPE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), DataPE.class, "code", values);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d data set(s) have been found.", list.size()));
        }
        return list;
    }

    @Override
    public List<SpacePE> listSpacesByDataSetIds(Collection<Long> values)
    {
        final List<Long> allIds = new ArrayList<Long>(values);
        final String query =
                "from " + SpacePE.class.getSimpleName()
                        + " as s where s.id in (select p.space.id from "
                        + ProjectPE.class.getSimpleName()
                        + " as p where p.id in (select e.projectInternal.id from "
                        + ExperimentPE.class.getSimpleName()
                        + " as e where e.id in (select d.experimentInternal.id from "
                        + DataPE.class.getSimpleName() + " as d where d.id in (:ids))))";
        final List<SpacePE> result = new ArrayList<SpacePE>();
        BatchOperationExecutor.executeInBatches(new IBatchOperation<Long>()
            {
                @Override
                public void execute(List<Long> ids)
                {
                    List<SpacePE> spaces =
                            cast(getHibernateTemplate().findByNamedParam(query, "ids", ids));
                    result.addAll(spaces);
                }

                @Override
                public List<Long> getAllEntities()
                {
                    return allIds;
                }

                @Override
                public String getEntityName()
                {
                    return "space";
                }

                @Override
                public String getOperationName()
                {
                    return "listSpacesByDataSetIds";
                }
            });

        return result;
    }

    @Override
    public void updateDataSets(List<DataPE> dataSets, PersonPE modifier)
    {
        assert dataSets != null : "Data sets not defined";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        for (DataPE data : dataSets)
        {
            validatePE(data);
            data.setCode(CodeConverter.tryToDatabase(data.getCode()));
            hibernateTemplate.saveOrUpdate(data);
        }

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: %d data sets.", dataSets.size()));
        }

        flushWithSqlExceptionHandling(getHibernateTemplate());
        scheduleDynamicPropertiesEvaluation(dataSets);

        // if session is not cleared registration of many samples slows down after each batch
        hibernateTemplate.clear();
    }

    @Override
    public final void validateAndSaveUpdatedEntity(DataPE entity) throws DataAccessException
    {
        super.validateAndSaveUpdatedEntity(entity);
        scheduleDynamicPropertiesEvaluation(Arrays.asList(entity));
    }

    @Override
    public List<TechId> listDataSetIdsBySampleIds(final Collection<TechId> samples)
    {
        final List<Long> longIds = TechId.asLongs(samples);
        final List<Long> results =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            final DetachedCriteria criteria =
                                    DetachedCriteria.forClass(DataPE.class);
                            criteria.setProjection(Projections.id());
                            return criteria;
                        }
                    }, "sampleInternal.id", longIds);

        if (operationLog.isDebugEnabled())
        {
            operationLog
                    .info(String.format("found %s data sets for given samples", results.size()));
        }
        return transformNumbers2TechIdList(results);
    }

    @Override
    public List<TechId> listDataSetIdsByExperimentIds(final Collection<TechId> experiments)
    {
        final List<Long> longIds = TechId.asLongs(experiments);
        final List<Long> results =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            final DetachedCriteria criteria =
                                    DetachedCriteria.forClass(DataPE.class);
                            criteria.setProjection(Projections.id());
                            return criteria;
                        }
                    }, "experimentInternal.id", longIds);
        if (operationLog.isDebugEnabled())
        {
            operationLog.info(String.format("found %s data sets for given experiments",
                    results.size()));
        }
        return transformNumbers2TechIdList(results);
    }

    @Override
    Logger getLogger()
    {
        return operationLog;
    }

    @Override
    public List<TechId> listContainedDataSets(Collection<TechId> containerIds)
    {

        final List<Long> longIds = TechId.asLongs(containerIds);
        final List<Long> totalResults = new ArrayList<Long>();

        BatchOperationExecutor.executeInBatches(new IBatchOperation<Long>()
            {

                @Override
                public void execute(List<Long> batchIds)
                {
                    List<Long> result =
                            DAOUtils.listByCollection(getHibernateTemplate(),
                                    new IDetachedCriteriaFactory()
                                        {
                                            @Override
                                            public DetachedCriteria createCriteria()
                                            {
                                                final DetachedCriteria criteria =
                                                        DetachedCriteria.forClass(DataPE.class);
                                                criteria.setProjection(Projections.id());
                                                return criteria;
                                            }
                                        }, "containerInternal.id", batchIds);
                    totalResults.addAll(result);
                }

                @Override
                public List<Long> getAllEntities()
                {
                    return longIds;
                }

                @Override
                public String getEntityName()
                {
                    return "dataSet";
                }

                @Override
                public String getOperationName()
                {
                    return "listContainedDataSets";
                }
            }, MAX_BATCH_SIZE);

        if (operationLog.isDebugEnabled())
        {
            operationLog.info(String.format("found %s data sets for given containers",
                    totalResults.size()));
        }

        return transformNumbers2TechIdList(totalResults);
    }

    @Override
    public List<TechId> listContainedDataSetsRecursively(Collection<TechId> containersIds)
    {
        LinkedHashSet<TechId> allIds = new LinkedHashSet<TechId>();
        // cascade deletion of contained datasets
        List<TechId> containedDataSetIds = new LinkedList<TechId>();

        containedDataSetIds.addAll(containersIds);

        while (allIds.addAll(containedDataSetIds))
        {
            containedDataSetIds = listContainedDataSets(containedDataSetIds);
        }

        return new ArrayList<TechId>(allIds);

    }

    @Override
    public boolean confirmStorage(String dataSetCode)
    {
        SQLQuery query =
                getSession()
                        .createSQLQuery(
                                "update external_data set storage_confirmation = true where storage_confirmation = false and data_id in (select id from data_all where code = :code)");
        query.setString("code", dataSetCode);
        return query.executeUpdate() > 0;
    }
}
