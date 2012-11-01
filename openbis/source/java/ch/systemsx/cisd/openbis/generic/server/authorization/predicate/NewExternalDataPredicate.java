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
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Predicate for {@link NewExternalData} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class NewExternalDataPredicate extends AbstractPredicate<NewExternalData>
{
    private final SampleOwnerIdentifierPredicate sampleOwnerIdentifierPredicate;

    private final ExistingSpaceIdentifierPredicate experimentOwnerIdentifierPredicate;

    public NewExternalDataPredicate()
    {
        sampleOwnerIdentifierPredicate = new SampleOwnerIdentifierPredicate(true, true);
        experimentOwnerIdentifierPredicate = new ExistingSpaceIdentifierPredicate();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        sampleOwnerIdentifierPredicate.init(provider);
        experimentOwnerIdentifierPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "new data set";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            NewExternalData value)
    {
        // Skip all further checks if the person has instance-wide write permissions.
        if (hasInstanceWritePermissions(person, allowedRoles).isOK())
        {
            return Status.OK;
        }

        SampleIdentifier sampleIdentifier = value.getSampleIdentifierOrNull();
        if (sampleIdentifier != null)
        {
            return sampleOwnerIdentifierPredicate.evaluate(person, allowedRoles, sampleIdentifier);
        }
        return experimentOwnerIdentifierPredicate.evaluate(person, allowedRoles,
                value.getExperimentIdentifierOrNull());
    }

}
