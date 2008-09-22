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

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <code>IPredicate</code> implementation based on a {@link Collection}.
 * 
 * @author Christian Ribeaud
 */
public final class CollectionPredicate<T> extends AbstractPredicate<Collection<T>>
{
    private final IPredicate<T> predicate;

    public CollectionPredicate(final IPredicate<T> predicate)
    {
        this.predicate = predicate;
    }

    protected Status itemEvaluate(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final T item)
    {
        return predicate.evaluate(person, allowedRoles, item);
    }

    //
    // AbstractPredicate
    //

    public final void init(IAuthorizationDataProvider provider)
    {
        predicate.init(null);
    }

    @Override
    final Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final Collection<T> value)
    {
        for (final T item : value)
        {
            final Status status = itemEvaluate(person, allowedRoles, item);
            if (status.getFlag().equals(StatusFlag.OK) == false)
            {
                return status;
            }
        }
        return Status.OK;
    }

    @Override
    final String getCandidateDescription()
    {
        if (predicate instanceof AbstractPredicate)
        {
            return ((AbstractPredicate<?>) predicate).getCandidateDescription();
        }
        return "collection";
    }
}
