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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.Select;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * Data access object for {@link ExperimentPE}.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentDAO extends AbstractGenericEntityWithPropertiesDAO<ExperimentPE> implements
        IExperimentDAO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ExperimentDAO.class);

    /**
     * A query for fast access to ids and codes of samples of an experiment.
     */
    public static interface IExperimentSampleQuery extends BaseQuery
    {
        @Select(sql = "select id from samples s where expe_id = ?{1}")
        List<Long> getExperimentSampleIds(long experimentId);

        @Select(sql = "select code from samples s where expe_id = ?{1}")
        List<String> getExperimentSampleCodes(long experimentId);
    }

    private final IExperimentSampleQuery experimentSampleQuery;

    protected ExperimentDAO(final PersistencyResources persistencyResources,
            final DatabaseInstancePE databaseInstance)
    {
        super(persistencyResources, databaseInstance, ExperimentPE.class);
        this.experimentSampleQuery = QueryTool.getManagedQuery(IExperimentSampleQuery.class);
    }

    @Override
    public List<ExperimentPE> listExperimentsWithProperties(final ProjectPE project,
            boolean onlyHavingSamples, boolean onlyHavingDataSets) throws DataAccessException
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project wasn't set");
        }
        return listExperimentsWithProperties(null, project, null, onlyHavingSamples,
                onlyHavingDataSets);
    }

    @Override
    public List<ExperimentPE> listExperimentsWithProperties(final SpacePE space)
            throws DataAccessException
    {
        if (space == null)
        {
            throw new IllegalArgumentException("Space wasn't set");
        }
        return listExperimentsWithProperties(null, null, space);
    }

    @Override
    public List<ExperimentPE> listExperimentsWithProperties(
            final ExperimentTypePE experimentTypeOrNull, final ProjectPE projectOrNull,
            final SpacePE spaceOrNull) throws DataAccessException
    {
        return listExperimentsWithProperties(experimentTypeOrNull, projectOrNull, spaceOrNull,
                false, false);
    }

    @Override
    public List<ExperimentPE> listExperimentsWithProperties(
            final ExperimentTypePE experimentTypeOrNull, final ProjectPE projectOrNull,
            final SpacePE spaceOrNull, final boolean onlyHavingSamples,
            final boolean onlyHavingDataSets) throws DataAccessException
    {
        final DetachedCriteria criteria = createCriteriaForUndeleted();
        if (experimentTypeOrNull != null)
        {
            criteria.add(Restrictions.eq("experimentType", experimentTypeOrNull));
        }
        if (projectOrNull != null)
        {
            criteria.add(Restrictions.eq("projectInternal", projectOrNull));
        }
        if (spaceOrNull != null)
        {
            // alias is the easiest way to restrict on association using criteria
            criteria.createAlias("projectInternal", "project");
            criteria.add(Restrictions.eq("project.space", spaceOrNull));
        }
        if (onlyHavingSamples)
        {
            criteria.add(Restrictions.isNotEmpty("experimentSamples"));
        }
        if (onlyHavingDataSets)
        {
            criteria.add(Restrictions.isNotEmpty("experimentDataSets"));
        }

        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        final List<ExperimentPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d experiments have been found for project '%s'%s.",
                    list.size(), projectOrNull, (experimentTypeOrNull == null) ? ""
                            : " and experiment type '" + experimentTypeOrNull + "'"));
        }
        return list;
    }

    @Override
    public List<ExperimentPE> listExperimentsWithProperties(Collection<Long> experimentIDs)
            throws DataAccessException
    {
        if (experimentIDs == null || experimentIDs.isEmpty())
        {
            return new ArrayList<ExperimentPE>();
        }
        final List<ExperimentPE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
                            criteria.setFetchMode("experimentProperties", FetchMode.JOIN);
                            criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
                            return criteria;
                        }
                    }, "id", experimentIDs);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d experiments have been found for specified IDs.",
                    list.size()));
        }
        return list;
    }

    @Override
    public List<ExperimentPE> listExperiments() throws DataAccessException
    {
        final DetachedCriteria criteria = createCriteriaForUndeleted();
        final List<ExperimentPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d experiment(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    private DetachedCriteria createCriteriaForUndeleted()
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.isNull("deletion"));
        return criteria;
    }

    @Override
    public ExperimentPE tryFindByCodeAndProject(final ProjectPE project, final String experimentCode)
    {
        assert experimentCode != null : "Unspecified experiment code.";
        assert project != null : "Unspecified project.";

        final Criteria criteria = getSession().createCriteria(getEntityClass());
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(experimentCode)));
        criteria.add(Restrictions.eq("projectInternal", project));
        criteria.setFetchMode("experimentType.experimentTypePropertyTypesInternal", FetchMode.JOIN);
        final ExperimentPE experiment = (ExperimentPE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following experiment '%s' has been found for code '%s' and project '%s'.",
                    experiment, experimentCode, project));
        }
        return experiment;
    }

    @Override
    public List<ExperimentPE> listExperimentsByProjectAndProperty(String propertyCode,
            String propertyValue, ProjectPE project) throws DataAccessException
    {
        assert project != null : "Unspecified space.";
        assert propertyCode != null : "Unspecified property code";
        assert propertyValue != null : "Unspecified property value";

        String queryFormat =
                "from " + ExperimentPropertyPE.class.getSimpleName()
                        + " where %s = ? and entity.projectInternal = ? "
                        + " and entityTypePropertyType.propertyTypeInternal.simpleCode = ?"
                        + " and entityTypePropertyType.propertyTypeInternal.internalNamespace = ?";

        List<ExperimentPE> entities =
                listByPropertyValue(queryFormat, propertyCode, propertyValue, project);
        if (operationLog.isDebugEnabled())
        {
            operationLog
                    .debug(String
                            .format("%d experiments have been found for project '%s' and property '%s' equal to '%s'.",
                                    entities.size(), project, propertyCode, propertyValue));
        }
        return entities;
    }

    private List<ExperimentPE> listByPropertyValue(String queryFormat, String propertyCode,
            String propertyValue, ProjectPE project)
    {
        String simplePropertyCode = CodeConverter.tryToDatabase(propertyCode);
        boolean isInternalNamespace = CodeConverter.isInternalNamespace(propertyCode);
        Object[] arguments =
                toArray(propertyValue, project, simplePropertyCode, isInternalNamespace);

        String queryPropertySimpleValue = String.format(queryFormat, "value");
        List<ExperimentPropertyPE> properties1 =
                cast(getHibernateTemplate().find(queryPropertySimpleValue, arguments));

        String queryPropertyVocabularyTerm = String.format(queryFormat, "vocabularyTerm.code");
        List<ExperimentPropertyPE> properties2 =
                cast(getHibernateTemplate().find(queryPropertyVocabularyTerm, arguments));

        properties1.addAll(properties2);
        List<ExperimentPE> entities = extractEntities(properties1);
        return entities;
    }

    private static List<ExperimentPE> extractEntities(List<ExperimentPropertyPE> properties)
    {
        List<ExperimentPE> samples = new ArrayList<ExperimentPE>();
        for (ExperimentPropertyPE prop : properties)
        {
            samples.add(prop.getEntity());
        }
        return samples;
    }

    @Override
    public ExperimentPE tryGetByPermID(String permId)
    {
        final Criteria criteria = getSession().createCriteria(getEntityClass());
        criteria.add(Restrictions.eq("permId", permId));
        final ExperimentPE experimentOrNull = (ExperimentPE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following experiment '%s' has been found for permId '%s'.", experimentOrNull,
                    permId));
        }
        return experimentOrNull;
    }

    @Override
    public List<SpacePE> listSpacesByExperimentIds(Collection<Long> experimentIds)
    {
        final List<Long> allIds = new ArrayList<Long>(experimentIds);
        final String query =
                "from " + SpacePE.class.getSimpleName()
                        + " as s where s.id in (select p.space.id from "
                        + ProjectPE.class.getSimpleName()
                        + " as p where p.id in (select e.projectInternal.id from "
                        + ExperimentPE.class.getSimpleName() + " as e where e.id in (:ids))))";
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
    public List<ExperimentPE> listByPermID(Set<String> permIds)
    {
        return listByIDsOfName("permId", permIds);
    }

    @Override
    public List<ExperimentPE> listByIDs(Collection<Long> ids)
    {
        return listByIDsOfName("id", ids);
    }

    private List<ExperimentPE> listByIDsOfName(String idName, Collection<?> ids)
    {
        if (ids == null || ids.isEmpty())
        {
            return new ArrayList<ExperimentPE>();
        }
        final List<ExperimentPE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), ExperimentPE.class, idName, ids);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d experiment(s) have been found.", list.size()));
        }
        return list;
    }

    private List<Long> getSampleIds(ExperimentPE experiment)
    {
        return experimentSampleQuery.getExperimentSampleIds(experiment.getId());
    }

    @Override
    public List<String> getSampleCodes(ExperimentPE experiment)
    {
        return experimentSampleQuery.getExperimentSampleCodes(experiment.getId());
    }

    @Override
    public void createOrUpdateExperiment(ExperimentPE experiment, PersonPE modifier)
    {
        HibernateTemplate template = getHibernateTemplate();
        internalCreateOrUpdateExperiment(experiment, modifier, template);
        template.flush();

        scheduleDynamicPropertiesEvaluation(Collections.singletonList(experiment));

        // Moving the experiment to other space affects the index data of attached samples, and also
        // might affect dynamic properties of attached samples.
        // Thus we trigger here dynamic properties evaluation, that in turn triggers reindexing.
        scheduleDynamicPropertiesEvaluationWithIds(getDynamicPropertyEvaluatorScheduler(),
                SamplePE.class, getSampleIds(experiment));
    }

    @Override
    public void createOrUpdateExperiments(List<ExperimentPE> experiments, PersonPE modifier)
    {
        assert experiments != null && experiments.size() > 0 : "Unspecified or empty experiments.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        for (final ExperimentPE experiment : experiments)
        {
            internalCreateOrUpdateExperiment(experiment, modifier, hibernateTemplate);
        }
        hibernateTemplate.flush();

        // if session is not cleared registration of many experiments slows down after each batch
        hibernateTemplate.clear();
        scheduleDynamicPropertiesEvaluation(experiments);
    }

    private void internalCreateOrUpdateExperiment(ExperimentPE experiment, PersonPE modifier,
            HibernateTemplate hibernateTemplate)
    {
        assert experiment != null : "Missing experiment.";
        experiment.setCode(CodeConverter.tryToDatabase(experiment.getCode()));
        validatePE(experiment);
        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(experiment);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("ADD: experiment '%s'.", experiment));
        }
    }

    @Override
    public void delete(final List<TechId> experimentIds, final PersonPE registrator,
            final String reason) throws DataAccessException
    {
        // NOTE: we use EXPERIMENT_ALL_TABLE, not DELETED_EXPERIMENTS_VIEW because we still want to
        // be able to directly delete samples without going to trash (trash may be disabled)
        final String experimentsTable = TableNames.EXPERIMENTS_ALL_TABLE;

        final String sqlSelectPermIds = SQLBuilder.createSelectPermIdsSQL(experimentsTable);
        final String sqlDeleteProperties =
                SQLBuilder.createDeletePropertiesSQL(TableNames.EXPERIMENT_PROPERTIES_TABLE,
                        ColumnNames.EXPERIMENT_COLUMN);
        final String sqlSelectAttachmentContentIds =
                SQLBuilder.createSelectAttachmentContentIdsSQL(ColumnNames.EXPERIMENT_COLUMN);
        final String sqlDeleteAttachmentContents = SQLBuilder.createDeleteAttachmentContentsSQL();
        final String sqlDeleteAttachments =
                SQLBuilder.createDeleteAttachmentsSQL(ColumnNames.EXPERIMENT_COLUMN);
        final String sqlDeleteExperiments = SQLBuilder.createDeleteEnitiesSQL(experimentsTable);
        final String sqlInsertEvent = SQLBuilder.createInsertEventSQL();

        executePermanentDeleteAction(EntityType.EXPERIMENT, experimentIds, registrator, reason,
                sqlSelectPermIds, sqlDeleteProperties, sqlSelectAttachmentContentIds,
                sqlDeleteAttachmentContents, sqlDeleteAttachments, sqlDeleteExperiments,
                sqlInsertEvent);
    }

    @Override
    Logger getLogger()
    {
        return operationLog;
    }

}
