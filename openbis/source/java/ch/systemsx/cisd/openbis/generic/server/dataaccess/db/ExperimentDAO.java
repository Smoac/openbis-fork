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

import java.util.List;

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
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * Data access object for {@link ExperimentPE}.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentDAO extends AbstractGenericEntityDAO<ExperimentPE> implements IExperimentDAO
{

    @Override
    Class<ExperimentPE> getEntityClass()
    {
        return ExperimentPE.class;
    }

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ExperimentDAO.class);

    protected ExperimentDAO(final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    public List<ExperimentPE> listExperimentsWithProperties(final ExperimentTypePE experimentType,
            final ProjectPE project) throws DataAccessException
    {
        assert experimentType != null : "Unspecified experiment type.";
        assert project != null : "Unspecified project.";

        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.eq("experimentType", experimentType));
        criteria.add(Restrictions.eq("projectInternal", project));
        criteria.setFetchMode("experimentProperties", FetchMode.JOIN);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        final List<ExperimentPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d experiments have been found for experiment type '%s' and project '%s'.",
                    list.size(), experimentType, project));
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

}
