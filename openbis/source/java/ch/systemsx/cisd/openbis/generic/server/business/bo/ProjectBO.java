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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * The only productive implementation of {@link IProjectBO}. We are using an interface here to keep
 * the system testable.
 * 
 * @author Christian Ribeaud
 */
public final class ProjectBO extends AbstractBusinessObject implements IProjectBO
{

    /**
     * The business object held by this implementation.
     * <p>
     * Package protected so that <i>Unit Test</i> can access it.
     * </p>
     */
    private ProjectPE project;

    public ProjectBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    private ProjectPE createProject(final ProjectIdentifier projectIdentifier, String description,
            String leaderId)
    {
        final ProjectPE result = new ProjectPE();
        final GroupPE group =
                GroupIdentifierHelper.tryGetGroup(projectIdentifier, session.tryGetPerson(), this);
        result.setGroup(group);
        result.setRegistrator(findRegistrator());
        result.setCode(projectIdentifier.getProjectCode());
        result.setDescription(description);
        if (leaderId != null)
        {
            PersonPE leader = getPersonDAO().tryFindPersonByUserId(leaderId);
            if (leader == null)
            {
                throw new UserFailureException("Person '%s' not found in the database.");
            }
            result.setProjectLeader(leader);
        }
        return result;
    }

    public final void save()
    {
        assert project != null : "Can not save an undefined project.";
        try
        {
            getProjectDAO().createProject(project);
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Project '" + project.getCode() + "'");
        }
    }

    public final ProjectPE getProject()
    {
        return project;
    }

    public void define(ProjectIdentifier projectIdentifier, String description, String leaderId)
            throws UserFailureException
    {
        assert projectIdentifier != null : "Unspecified project identifier.";
        this.project = createProject(projectIdentifier, description, leaderId);
    }
}
