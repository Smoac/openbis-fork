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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * A {@link IPredicate} for a project permanent id.
 * 
 * @author Bernd Rinn
 */
public class ProjectPermIdPredicate extends AbstractSpacePredicate<PermId>
{

    @Override
    public String getCandidateDescription()
    {
        return "project perm id";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            PermId permId)
    {
        final ProjectPE project =
                authorizationDataProvider.tryGetProjectByPermId(permId);
        if (project == null)
        {
            return Status.createError(String.format("There is no project with perm id '%s'.", permId));
        }
        return evaluateSpace(person, allowedRoles, project.getSpace());
    }

}
