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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescriptionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Implementation of {@link IExternalDataDAO} for databases.
 * 
 * @author Christian Ribeaud
 */
final class ExternalDataDAO extends AbstractGenericEntityDAO<ExternalDataPE> implements
        IExternalDataDAO
{
    private final static Class<ExternalDataPE> ENTITY_CLASS = ExternalDataPE.class;

    private final static Class<DataPE> ENTITY_SUPER_CLASS = DataPE.class;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ExternalDataDAO.class);

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    ExternalDataDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, ENTITY_CLASS);
    }

    //
    // IExternalDataDAO
    //

    public boolean hasExternalData(SamplePE sample) throws DataAccessException
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(ExternalDataPE.class);
        criteria.add(Restrictions.eq("sampleInternal", sample));
        criteria.setProjection(Projections.rowCount());
        Integer count = (Integer) getHibernateTemplate().findByCriteria(criteria).get(0);
        return count > 0;
    }

    public final List<ExternalDataPE> listRelatedExternalData(final IEntityInformationHolder entity)
            throws DataAccessException
    {
        assert entity != null : "Unspecified entity.";

        final String entityName = entity.getEntityKind().toString().toLowerCase();
        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.sampleInternal " + "left join fetch e.parents "
                        + "left join fetch e.dataSetProperties " + "where e.%sInternal.id = ?",
                        TABLE_NAME, entityName);
        final List<ExternalDataPE> list =
                cast(getHibernateTemplate().find(query, toArray(entity.getId())));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d external data have been found for [entity=%s].",
                    list.size(), entity));
        }
        return list;
    }

    public final List<ExternalDataPE> listExternalData(final SamplePE sample)
            throws DataAccessException
    {
        assert sample != null : "Unspecified sample.";

        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.parents " + "left join fetch e.dataSetProperties "
                        + "where e.sampleInternal = ?", TABLE_NAME);
        final List<ExternalDataPE> list = cast(getHibernateTemplate().find(query, toArray(sample)));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d external data have been found for [sample=%s].",
                    list.size(), sample));
        }
        return list;
    }

    public final List<ExternalDataPE> listExternalData(final DataStorePE dataStore)
            throws DataAccessException
    {
        assert dataStore != null : "Unspecified data store.";
        final DetachedCriteria criteria = DetachedCriteria.forClass(ExternalDataPE.class);
        criteria.add(Restrictions.eq("dataStore", dataStore));
        final List<ExternalDataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d data set(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), dataStore, list.size()));
        }
        return list;
    }

    public final List<ExternalDataPE> listExternalData(final ExperimentPE experiment)
            throws DataAccessException
    {
        assert experiment != null : "Unspecified experiment.";

        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.parents " + "left join fetch e.dataSetProperties "
                        + "where e.experimentInternal = ?", TABLE_NAME);
        final List<ExternalDataPE> list =
                cast(getHibernateTemplate().find(query, toArray(experiment)));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d external data have been found for [experiment=%e].", list.size(),
                    experiment));
        }
        return list;
    }

    private void distinct(List<ExternalDataPE> list)
    {
        Set<ExternalDataPE> set = new TreeSet<ExternalDataPE>(list);
        list.clear();
        list.addAll(set);
    }

    public DataPE tryToFindDataSetByCode(String dataSetCode)
    {
        assert dataSetCode != null : "Unspecified data set code.";

        final String mangledCode = CodeConverter.tryToDatabase(dataSetCode);
        final Criterion codeEq = Restrictions.eq("code", mangledCode);

        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_SUPER_CLASS);
        criteria.add(codeEq);
        criteria.setFetchMode("dataSetType", FetchMode.JOIN);
        criteria.setFetchMode("dataStore", FetchMode.JOIN);
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        final DataPE entity = tryFindEntity(list, "data set");

        if (operationLog.isDebugEnabled())
        {
            String methodName = MethodUtils.getCurrentMethod().getName();
            operationLog.debug(String.format("%s(%s): '%s'.", methodName, dataSetCode, entity));
        }
        return entity;
    }

    public List<DatasetDescription> tryToFindDatasetDescriptionsByCodes(List<String> dataSetCodes)
    {
        assert dataSetCodes != null : "Unspecified data set code";

        // final Criterion codeEq = Restrictions.eq("code", mangledCode);

        // TODO
        // Hibernate bug (HHH-2676) - can't take a lock in this kind of query.
        String queryString =
                "select new ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription(d.code, d.location, d.sampleInternal.code, "
                        + "d.experimentInternal.projectInternal.group.code, "
                        + "d.experimentInternal.projectInternal.code, "
                        + "d.experimentInternal.code, "
                        + "d.dataSetType.mainDataSetPath, d.dataSetType.mainDataSetPattern, "
                        + "d.experimentInternal.projectInternal.group.databaseInstance.code) "
                        + "from " + TABLE_NAME + " d where d.code IN (:codes)";

        Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
        final List<DatasetDescription> list =
                cast(session.createQuery(queryString).setParameterList("codes", dataSetCodes)
                        .list());
 
        System.err.println(list.size());
        for (DatasetDescription desc : list)
        {
            System.err.println(ReflectionToStringBuilder.toString(desc));
        }
        // if (operationLog.isDebugEnabled())
        // {
        // operationLog.debug(String.format("External data '%s' found for data set code '%s'.",
        // entity, dataSetCode));
        // }
        return list;
    }

    public List<DatasetDescriptionPE> tryToFindDatasetDescriptionsByCodes(List<String> dataSetCodes)
    {
        assert dataSetCodes != null : "Unspecified data set code";

        // final Criterion codeEq = Restrictions.eq("code", mangledCode);

        // TODO
        // Hibernate bug (HHH-2676) - can't take a lock in this kind of query.
        String queryString =
                "select new DatasetDescriptionPE(d.code, d.location, d.sampleInternal.code, "
                        + "d.experimentInternal.projectInternal.group.code, "
                        + "d.experimentInternal.projectInternal.code, "
                        + "d.experimentInternal.code, "
                        + "d.dataSetType.mainDataSetPath, d.dataSetType.mainDataSetPattern, "
                        + "d.experimentInternal.projectInternal.group.databaseInstance.code) "
                        + "from " + TABLE_NAME + " d where d.code IN (:codes)";

        Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
        final List<DatasetDescriptionPE> list =
                cast(session.createQuery(queryString).setParameterList("codes", dataSetCodes)
                        .list());

        System.err.println(list.size());
        for (DatasetDescriptionPE desc : list)
        {
            System.err.println(ReflectionToStringBuilder.toString(desc));
        }
        // if (operationLog.isDebugEnabled())
        // {
        // operationLog.debug(String.format("External data '%s' found for data set code '%s'.",
        // entity, dataSetCode));
        // }
        return list;
    }

    public List<ExternalDataPE> tryToFindFullDataSetsByCodes(Collection<String> dataSetCodes,
            boolean withPropertyTypes, boolean lockForUpdate)
    {
        assert dataSetCodes != null : "Unspecified collection";

        if (dataSetCodes.size() == 0)
        {
            return Collections.emptyList();
        }

        final Criterion codeIn = Restrictions.in("code", dataSetCodes);

        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_CLASS);
        criteria.add(codeIn);
        criteria.setFetchMode("dataSetType", FetchMode.SELECT);
        criteria.setFetchMode("dataStore", FetchMode.SELECT);
        criteria.setFetchMode("experimentInternal", FetchMode.SELECT);
        criteria.setFetchMode("sampleInternal", FetchMode.SELECT);
        criteria.setFetchMode("fileFormat", FetchMode.SELECT);
        if (withPropertyTypes)
        {
            criteria.setFetchMode("dataSetType.dataSetTypePropertyTypesInternal", FetchMode.JOIN);
        }
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (lockForUpdate)
        {
            criteria.setLockMode(LockMode.UPGRADE);
        }
        final List<ExternalDataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Found '%s' data sets for codes '%s'.", list.size(),
                    dataSetCodes));
        }
        return list;
    }

    public ExternalDataPE tryToFindFullDataSetByCode(String dataSetCode, boolean withPropertyTypes,
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
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (lockForUpdate)
        {
            criteria.setLockMode(LockMode.UPGRADE);
        }
        final List<ExternalDataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        final ExternalDataPE entity = tryFindEntity(list, "data set");

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("External data '%s' found for data set code '%s'.",
                    entity, dataSetCode));
        }
        return entity;
    }

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
        int updatedRows = (Integer) hibernateTemplate.execute(new HibernateCallback()
            {

                //
                // HibernateCallback
                //

                public final Object doInHibernate(final Session session) throws HibernateException,
                        SQLException
                {
                    // NOTE: 'VERSIONED' makes modification time modified too
                    return session.createQuery(
                            "UPDATE VERSIONED " + TABLE_NAME
                                    + " SET status = :status WHERE code IN (:codes) ")
                            .setParameter("status", status).setParameterList("codes", dataSetCodes)
                            .executeUpdate();
                }
            });
        hibernateTemplate.flush();
        if (updatedRows != dataSetCodes.size())
        {
            throw UserFailureException.fromTemplate("Update of %s data set statuses to %s failed.",
                    dataSetCodes.size(), status);
        } else if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATED: %s data set statuses to '%s'.", dataSetCodes
                    .size(), status));
        }
    }

    public void createDataSet(DataPE dataset)
    {
        assert dataset != null : "Unspecified data set.";

        dataset.setCode(CodeConverter.tryToDatabase(dataset.getCode()));
        final HibernateTemplate template = getHibernateTemplate();
        template.save(dataset);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: data set '%s'.", dataset));
        }
    }

    public void updateDataSet(ExternalDataPE externalData)
    {
        assert externalData != null : "Given external data can not be null.";
        validatePE(externalData);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        externalData.setCode(CodeConverter.tryToDatabase(externalData.getCode()));
        Long id = HibernateUtils.getId(externalData);
        final DataPE loaded = (DataPE) hibernateTemplate.load(ENTITY_CLASS, id);
        // This just means that we do not have any entry in 'EXTERNAL_DATA' table for this id. It
        // might happen when we work with placeholder data.
        if (loaded instanceof ExternalDataPE == false)
        {
            String location = externalData.getLocation();
            Long locatorTypeID = externalData.getLocatorType().getId();
            Long fileFormatTypeID = externalData.getFileFormatType().getId();
            char complete = externalData.getComplete().name().charAt(0);
            Long storageFormatTermID = externalData.getStorageFormatVocabularyTerm().getId();
            executeUpdate("insert into " + TableNames.EXTERNAL_DATA_TABLE
                    + " (data_id, location, loty_id, ffty_id, is_complete, cvte_id_stor_fmt) "
                    + "values (?, ?, ?, ?, ?, ?)", id, location, locatorTypeID, fileFormatTypeID,
                    complete, storageFormatTermID);
            hibernateTemplate.evict(loaded);
        }
        hibernateTemplate.update(externalData);
        hibernateTemplate.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: external data '%s'.", externalData));
        }
    }

    @Override
    public void delete(ExternalDataPE entity) throws DataAccessException
    {
        assert entity != null : "entity unspecified";
        if (entity.getChildren().size() > 0)
        {
            throw new DataIntegrityViolationException(
                    String
                            .format(
                                    "External Data '%s' cannot be deleted because children datasets are connected.",
                                    entity.getCode()));
        }
        super.delete(entity);
    }

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

                        public final Object doInHibernate(final Session session)
                        {
                            // we could remove this transformation if we choose to pass Long values
                            final List<Long> longIds = transformTechIds2Longs(dataSetIds);
                            return session.createSQLQuery(query).setParameterList("ids", longIds)
                                    .list();
                        }
                    });
        return transformNumbers2TechIds(results);
    }

    // data set relationship helper methods

    private List<Long> transformTechIds2Longs(Collection<TechId> techIds)
    {
        final List<Long> result = new ArrayList<Long>(techIds.size());
        for (TechId techId : techIds)
        {
            result.add(techId.getId());
        }
        return result;
    }

    private Set<TechId> transformNumbers2TechIds(Collection<? extends Number> numbers)
    {
        final Set<TechId> result = new HashSet<TechId>();
        for (Number number : numbers)
        {
            result.add(new TechId(number));
        }
        return result;
    }

}
