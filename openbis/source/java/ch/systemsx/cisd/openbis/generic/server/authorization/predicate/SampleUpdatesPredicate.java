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
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * An <code>IPredicate</code> implementation based on {@link SampleUpdatesDTO}. Checks that: 1) the user has rights to update the sample 2) if sample
 * is moved to a different group the user has access to this group 3) if sample is attached to experiment, user has access to this experiment.
 * 
 * @author Izabela Adamczyk
 */
public class SampleUpdatesPredicate extends AbstractSamplePredicate<SampleUpdatesDTO>
{
    @Override
    public final String getCandidateDescription()
    {
        return "sample updates";
    }

    // TODO 2009-07-27, IA: tests needed
    @Override
    protected Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles,
            final SampleUpdatesDTO updates)
    {
        assert sampleTechIdPredicate.initialized : "Predicate has not been initialized";
        assert spacePredicate.initialized : "Predicate has not been initialized";
        assert sampleOwnerPredicate.initialized : "Predicate has not been initialized";
        Status status;
        status =
                sampleTechIdPredicate.doEvaluation(person, allowedRoles,
                        updates.getSampleIdOrNull());
        if (status.equals(Status.OK) == false)
        {
            return status;
        }
        status = evaluateBasedOnExperimentOrProject(spacePredicate, person, allowedRoles, updates);
        if (status.isOK() == false)
        {
            return status;
        }
        status =
                sampleOwnerPredicate.doEvaluation(person, allowedRoles,
                        updates.getSampleIdentifier());
        return status;
    }
    
    static Status evaluateBasedOnExperimentOrProject(SpaceIdentifierPredicate spacePredicate,
            PersonPE person, List<RoleWithIdentifier> allowedRoles, SampleUpdatesDTO sampleUpdates)
    {
        ExperimentIdentifier expId = sampleUpdates.getExperimentIdentifierOrNull();
        if (expId != null)
        {
            Status result = spacePredicate.doEvaluation(person, allowedRoles, expId);
            if (result.isOK() == false)
            {
                return result;
            }
        }
        ProjectIdentifier projId = sampleUpdates.getProjectIdentifier();
        if (projId != null)
        {
            return spacePredicate.doEvaluation(person, allowedRoles, projId);
        }
        return Status.OK;
    }

}
