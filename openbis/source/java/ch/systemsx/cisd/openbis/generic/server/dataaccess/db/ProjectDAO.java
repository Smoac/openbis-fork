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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Implementation of {@link IProjectDAO}.
 * 
 * @author Izabela Adamczyk
 */
public class ProjectDAO extends AbstractGenericEntityDAO<ProjectPE> implements IProjectDAO
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ProjectDAO.class);

    protected ProjectDAO(final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, ProjectPE.class);
    }

    @Override
    public List<ProjectPE> listProjects()
    {
        final List<ProjectPE> list = cast(getHibernateTemplate().loadAll(ProjectPE.class));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d projects(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public List<ProjectPE> listProjects(final SpacePE space)
    {
        assert space != null : "Unspecified space.";

        final DetachedCriteria criteria = DetachedCriteria.forClass(ProjectPE.class);
        criteria.add(Restrictions.eq("space", space));
        final List<ProjectPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d project(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), space, list.size()));
        }
        return list;
    }

    @Override
    public ProjectPE tryFindProject(final String databaseInstanceCode, final String spaceCode,
            final String projectCode)
    {
        assert projectCode != null : "Unspecified project code.";
        assert spaceCode != null : "Unspecified space code.";

        final Criteria criteria = getSession().createCriteria(ProjectPE.class);
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(projectCode)));
        final Criteria spaceCriteria = criteria.createCriteria("space");
        spaceCriteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(spaceCode)));
        if (StringUtils.isBlank(databaseInstanceCode))
        {
            spaceCriteria.add(Restrictions.eq("databaseInstance", getDatabaseInstance()));
        } else
        {
            spaceCriteria.createCriteria("databaseInstance").add(
                    Restrictions.eq("code", CodeConverter.tryToDatabase(databaseInstanceCode)));
        }
        return (ProjectPE) criteria.uniqueResult();
    }

    @Override
    public void createProject(ProjectPE project, PersonPE modifier)
    {
        assert project != null : "Missing project.";
        validatePE(project);

        project.setCode(CodeConverter.tryToDatabase(project.getCode()));
        project.setModifier(modifier);
        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(project);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("SAVE: project '%s'.", project));
        }
    }

}
