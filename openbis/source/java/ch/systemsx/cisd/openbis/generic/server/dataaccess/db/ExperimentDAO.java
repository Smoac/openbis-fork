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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * Data access object for {@link ExperimentPE}.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentDAO extends AbstractGenericEntityDAO<ExperimentPE> implements IExperimentDAO
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ExperimentDAO.class);

    protected ExperimentDAO(final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, ExperimentPE.class);
    }

    public List<ExperimentPE> listExperimentsWithProperties(final ProjectPE project)
            throws DataAccessException
    {
        return listExperimentsWithProperties(null, project);
    }

    public List<ExperimentPE> listExperimentsWithProperties(
            final ExperimentTypePE experimentTypeOrNull, final ProjectPE project)
            throws DataAccessException
    {
        assert project != null : "Unspecified project.";

        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        if (experimentTypeOrNull != null)
        {
            criteria.add(Restrictions.eq("experimentType", experimentTypeOrNull));
        }
        criteria.add(Restrictions.eq("projectInternal", project));
        criteria.setFetchMode("experimentProperties", FetchMode.JOIN);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        final List<ExperimentPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d experiments have been found for project '%s'%s.",
                    list.size(), project, (experimentTypeOrNull == null) ? ""
                            : " and experiment type '" + experimentTypeOrNull + "'"));
        }
        return list;
    }

    public List<ExperimentPE> listExperiments() throws DataAccessException
    {
        final List<ExperimentPE> list = cast(getHibernateTemplate().loadAll(getEntityClass()));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d experiment(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

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

    public void createExperiment(ExperimentPE experiment)
    {
        assert experiment != null : "Missing experiment.";
        experiment.setCode(CodeConverter.tryToDatabase(experiment.getCode()));

        validatePE(experiment);

        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(experiment);
        template.flush();
    }

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
                            .format(
                                    "%d experiments have been found for project '%s' and property '%s' equal to '%s'.",
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

    public void deleteZombiePlaceholders(ExperimentPE experiment) throws DataAccessException
    {
        // it will happen very rarely so it doesn't have to be optimized in terms of performance
        getHibernateTemplate().deleteAll(experiment.getDataSets());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Zombie placeholders connected with experiment '%s' have been deleted.",
                    experiment.getCode()));
        }
    }

    public List<ExperimentPE> listByPermID(Set<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return new ArrayList<ExperimentPE>();
        }
        final DetachedCriteria criteria = DetachedCriteria.forClass(ExperimentPE.class);
        criteria.add(Restrictions.in("permId", values));
        final List<ExperimentPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d experiment(s) have been found.", list.size()));
        }
        return list;
    }
}
