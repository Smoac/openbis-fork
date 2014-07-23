/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnsupportedObjectIdException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * @author pkupczyk
 */
@Component
public class TryGetProjectByIdExecutor implements ITryGetProjectByIdExecutor
{

    private IProjectDAO projectDAO;

    @SuppressWarnings("unused")
    private TryGetProjectByIdExecutor()
    {
    }

    public TryGetProjectByIdExecutor(IProjectDAO projectDAO)
    {
        this.projectDAO = projectDAO;
    }

    @Override
    public ProjectPE tryGet(IOperationContext context, IProjectId projectId)
    {
        if (projectId instanceof ProjectPermId)
        {
            ProjectPermId projectPermId = (ProjectPermId) projectId;
            return projectDAO.tryGetByPermID(projectPermId.getPermId());
        }
        if (projectId instanceof ProjectIdentifier)
        {
            ProjectIdentifier projectIdentifier = (ProjectIdentifier) projectId;
            ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier pid =
                    ProjectIdentifierFactory.parse(projectIdentifier.getIdentifier());
            return projectDAO.tryFindProject(pid.getSpaceCode(), pid.getProjectCode());
        }
        throw new UnsupportedObjectIdException(projectId);
    }

    @Autowired
    private void setDAOFactory(IDAOFactory daoFactory)
    {
        projectDAO = daoFactory.getProjectDAO();
    }

}
