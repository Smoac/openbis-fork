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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IRelationshipService;
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

    private DAOFactory daoFactory;

    @Override
    public void reassignProject(IAuthSession session, ProjectIdentifier projectId,
            ExperimentIdentifier experimentId)
    {

        ProjectPE previousProject =
                findProject(new ProjectIdentifier(experimentId.getDatabaseInstanceCode(),
                        experimentId.getSpaceCode(), experimentId.getProjectCode()));

        ProjectPE project = findProject(projectId);

        ExperimentPE experiment =
                daoFactory.getExperimentDAO().tryFindByCodeAndProject(previousProject,
                        experimentId.getExperimentCode());

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

    private ProjectPE findProject(ProjectIdentifier projectId)
    {
        ProjectPE project =
                daoFactory.getProjectDAO().tryFindProject(projectId.getDatabaseInstanceCode(),
                        projectId.getSpaceCode(), projectId.getProjectCode());

        if (project == null)
        {
            throw UserFailureException.fromTemplate(ERR_PROJECT_NOT_FOUND, projectId);
        }
        return project;
    }

    public void setDaoFactory(DAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }
}
