/*
 * Copyright 2013 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <code>IPredicate</code> implementation based on {@link ProjectIdentifier}.
 * 
 * @author Bernd Rinn
 */
public class ProjectIdentifierPredicate extends AbstractProjectPredicate<ProjectIdentifier>
{
    @Override
    public final String getCandidateDescription()
    {
        return "project identifier";
    }

    @Override
    protected Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles,
            final ProjectIdentifier identifier)
    {
        assert spacePredicate.initialized : "Predicate has not been initialized";
        assert projectTechIdPredicate.initialized : "Predicate has not been initialized";
        assert projectPermIdPredicate.initialized : "Predicate has not been initialized";
        assert projectAugmentedCodePredicate.initialized : "Predicate has not been initialized";
        Status status = null;
        if (identifier.getDatabaseId() != null)
        {
            status = projectTechIdPredicate.doEvaluation(person,
                    allowedRoles, new TechId(identifier.getDatabaseId()));
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        if (identifier.getPermId() != null)
        {
            status = projectPermIdPredicate.doEvaluation(person,
                    allowedRoles, identifier.getPermId());
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        if (identifier.getAugmentedCode() != null)
        {
            status = projectAugmentedCodePredicate.doEvaluation(person,
                    allowedRoles, identifier.getAugmentedCode());
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        if (status == null)
        {
            return Status.createError("No identifier given");
        }
        return Status.OK;
    }
}
