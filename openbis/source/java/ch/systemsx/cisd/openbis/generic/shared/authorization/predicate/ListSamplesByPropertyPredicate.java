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
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <code>IPredicate</code> implementation based on {@link ListSamplesByPropertyCriteria}. Checks
 * that the user has the right to access the group from which samples are referenced. Note that we
 * can ignore the experiment, since it belongs to the same group.
 * 
 * @author Tomasz Pylak
 */
public class ListSamplesByPropertyPredicate extends
        AbstractPredicate<ListSamplesByPropertyCriteria>
{
    private final SpaceIdentifierPredicate spacePredicate;

    public ListSamplesByPropertyPredicate()
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
        return "list samples by property";
    }

    @Override
    Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final ListSamplesByPropertyCriteria criteria)
    {
        assert spacePredicate.initialized : "Predicate has not been initialized";
        return spacePredicate.doEvaluation(person, allowedRoles, criteria.getGroupIdentifier());
    }
}
