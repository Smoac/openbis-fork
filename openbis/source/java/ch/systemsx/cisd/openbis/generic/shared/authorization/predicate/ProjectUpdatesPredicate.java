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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

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

    public ProjectUpdatesPredicate()
    {
        this.spacePredicate = new SpaceIdentifierPredicate();
    }

    public final void init(IAuthorizationDataProvider provider)
    {
        spacePredicate.init(provider);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "project updates";
    }

    @Override
    Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final ProjectUpdatesDTO updates)
    {
        assert spacePredicate.initialized : "Predicate has not been initialized";
        Status status;
        status = spacePredicate.doEvaluation(person, allowedRoles, updates.getIdentifier());
        if (status.equals(Status.OK) == false)
        {
            return status;
        }
        String newGroupCode = updates.getGroupCode();
        if (newGroupCode != null)
        {
            GroupIdentifier newGroupIdentifier =
                    new GroupIdentifier(updates.getIdentifier().getDatabaseInstanceCode(),
                            newGroupCode);
            status = spacePredicate.doEvaluation(person, allowedRoles, newGroupIdentifier);
        }
        return status;
    }
}
