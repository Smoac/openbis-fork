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

package ch.systemsx.cisd.openbis.generic.shared;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Definition of an internal service through which entity relationships can be changed and deleted.
 * All the methods of this service require authorization,
 * 
 * @author anttil
 */
public interface IRelationshipService
{
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    @Capability("ASSIGN_EXPERIMENT_TO_PROJECT")
    public void assignExperimentToProject(IAuthSession session,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            ExperimentIdentifier experiment,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            ProjectIdentifier project);

}
