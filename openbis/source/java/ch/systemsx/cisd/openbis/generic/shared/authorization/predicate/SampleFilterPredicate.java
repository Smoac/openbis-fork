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
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleFilter;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierPattern;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * An <code>IPredicate</code> implementation for {@link SampleFilter}.
 * 
 * @author Christian Ribeaud
 */
public final class SampleFilterPredicate extends AbstractPredicate<SampleFilter>
{
    private final ArrayPredicate<SampleOwnerIdentifier> arrayPredicate;

    public SampleFilterPredicate()
    {
        arrayPredicate =
                new ArrayPredicate<SampleOwnerIdentifier>(new SampleOwnerIdentifierPredicate());
    }

    private final static SampleOwnerIdentifier[] getSampleOwnerIdentifiers(
            SampleIdentifierPattern[] patterns)
    {
        int len = patterns.length;
        final SampleOwnerIdentifier[] sampleOwnerIdentifiers = new SampleOwnerIdentifier[len];
        for (int i = 0; i < patterns.length; i++)
        {
            sampleOwnerIdentifiers[i] = patterns[i].getSampleOwner();
        }
        return sampleOwnerIdentifiers;
    }

    //
    // AbstractPredicate
    //

    public final void init(IAuthorizationDataProvider provider)
    {
        arrayPredicate.init(null);
    }

    @Override
    final String getCandidateDescription()
    {
        return "sample filter";
    }

    @Override
    final Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final SampleFilter value)
    {
        final GroupPE homeGroup = person.getHomeGroup();
        final SampleIdentifierPattern[] patterns = value.getPatternsOrDefaultPatterns(homeGroup);
        final SampleOwnerIdentifier[] ownerIdentifiers = getSampleOwnerIdentifiers(patterns);
        return arrayPredicate.doEvaluation(person, allowedRoles, ownerIdentifiers);
    }
}
