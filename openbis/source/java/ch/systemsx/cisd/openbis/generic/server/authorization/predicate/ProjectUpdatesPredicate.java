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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ProjectTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * An <code>IPredicate</code> implementation based on {@link ProjectUpdatesDTO}. Checks that: 1) the
 * user has rights to update the project 2) if project is moved to a different group the user has
 * access to this group.
 * 
 * @author Tomasz Pylak
 */
public class ProjectUpdatesPredicate extends AbstractPredicate<ProjectUpdatesDTO>
{
    private final SpaceIdentifierPredicate spacePredicate;

    private final ProjectTechIdPredicate projectTechIdPredicate;

    private final ProjectPermIdPredicate projectPermIdPredicate;

    private final ProjectIdentifierPredicate projectIdentifierPredicate;

    public ProjectUpdatesPredicate()
    {
        this.spacePredicate = new SpaceIdentifierPredicate();
        this.projectTechIdPredicate = new ProjectTechIdPredicate();
        this.projectPermIdPredicate = new ProjectPermIdPredicate();
        this.projectIdentifierPredicate = new ProjectIdentifierPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        spacePredicate.init(provider);
        projectTechIdPredicate.init(provider);
        projectPermIdPredicate.init(provider);
        projectIdentifierPredicate.init(provider);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "project updates";
    }

    @Override
    protected Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles,
            final ProjectUpdatesDTO updates)
    {
        assert spacePredicate.initialized : "Predicate has not been initialized";
        assert projectTechIdPredicate.initialized : "Predicate has not been initialized";
        Status status;
        if (updates.getTechId() != null)
        {
            status = projectTechIdPredicate.doEvaluation(person,
                    allowedRoles, updates.getTechId());
        } else if (updates.getPermId() != null)
        {
            status = projectPermIdPredicate.doEvaluation(person,
                    allowedRoles, updates.getPermId());
        } else
        {
            status = projectIdentifierPredicate.doEvaluation(person,
                    allowedRoles, updates.getIdentifier());
        }
        if (status.equals(Status.OK) == false)
        {
            return status;
        }
        String newSpaceCode = updates.getSpaceCode();
        if (newSpaceCode != null)
        {
            SpaceIdentifier newSpaceIdentifier =
                    new SpaceIdentifier(DatabaseInstanceIdentifier.HOME, newSpaceCode);
            status = spacePredicate.doEvaluation(person, allowedRoles, newSpaceIdentifier);
        }
        return status;
    }
}
