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
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * An <code>IPredicate</code> implementation based on {@link GroupIdentifierPredicate} which allows
 * the identifier to be null.
 * 
 * @author Tomasz Pylak
 */
public class NullableGroupIdentifierPredicate extends AbstractPredicate<GroupIdentifier>
{
    private final GroupIdentifierPredicate delegator;

    public NullableGroupIdentifierPredicate()
    {
        this.delegator = new GroupIdentifierPredicate();
    }

    //
    // AbstractDatabaseInstancePredicate
    //

    public final void init(IAuthorizationDataProvider provider)
    {
        delegator.init(provider);
    }

    @Override
    protected boolean isNullValueAllowed()
    {
        return true;
    }

    @Override
    public final String getCandidateDescription()
    {
        return "nullable group identifier";
    }

    @Override
    Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final GroupIdentifier groupIdentifierOrNull)
    {
        if (groupIdentifierOrNull == null)
        {
            return Status.OK;
        } else
        {
            return delegator.doEvaluation(person, allowedRoles, groupIdentifierOrNull);
        }
    }
}
