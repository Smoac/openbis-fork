/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.shared.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * The unique {@link IRelationshipService} implementation.
 * 
 * @author anttil
 */
public class RelationshipService implements IRelationshipService
{
    private static final String ERR_PROJECT_NOT_FOUND =
            "No project for experiment '%s' could be found in the database.";

    private org.hibernate.SessionFactory sessionFactory;

    @Override
    public void reassignProject(IAuthSession session, ProjectIdentifier projectId,
            ExperimentIdentifier experimentId)
    {
        ProjectPE project = findProject(projectId);
        ExperimentPE experiment = findExperiment(experimentId);
        ProjectPE previousProject = experiment.getProject();
        if (project.equals(previousProject))
        {
            return;
        }
        // if the group has changes, move all samples to that group
        if (project.getSpace().equals(previousProject.getSpace()) == false)
        {
            SampleUtils.setSamplesGroup(experiment, project.getSpace());
        }
        experiment.setProject(project);
    }

    private ProjectPE findProject(ProjectIdentifier id)
    {

        final Criteria criteria =
                this.sessionFactory.getCurrentSession().createCriteria(ProjectPE.class);
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(id.getProjectCode())));
        final Criteria spaceCriteria = criteria.createCriteria("space");
        spaceCriteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(id.getSpaceCode())));

        ProjectPE project = (ProjectPE) criteria.uniqueResult();
        if (project == null)
        {
            throw UserFailureException.fromTemplate(ERR_PROJECT_NOT_FOUND, id);
        }
        return project;
    }

    private ExperimentPE findExperiment(ExperimentIdentifier id)
    {
        final Criteria criteria =
                this.sessionFactory.getCurrentSession().createCriteria(ExperimentPE.class);
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(id.getExperimentCode())));
        return (ExperimentPE) criteria.uniqueResult();
    }

    public void setSessionFactory(org.hibernate.SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
}
