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
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * An <code>IPredicate</code> implementation based on {@link SampleOwnerIdentifier}.
 * 
 * @author Christian Ribeaud
 */
public final class SampleOwnerIdentifierPredicate extends AbstractPredicate<SampleOwnerIdentifier>
{
    private final GroupIdentifierPredicate groupIdentifierPredicate;

    private final DatabaseInstanceIdentifierPredicate databaseInstanceIdentifierPredicate;

    public SampleOwnerIdentifierPredicate()
    {
        this(true);
    }

    public SampleOwnerIdentifierPredicate(boolean isReadAccess)
    {
        groupIdentifierPredicate = new GroupIdentifierPredicate();
        databaseInstanceIdentifierPredicate = new DatabaseInstanceIdentifierPredicate(isReadAccess);
    }

    //
    // AbstractPredicate
    //

    public final void init(IAuthorizationDataProvider provider)
    {
        groupIdentifierPredicate.init(null);
        databaseInstanceIdentifierPredicate.init(null);
    }

    @Override
    final String getCandidateDescription()
    {
        return "sample identifier";
    }

    @Override
    final Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final SampleOwnerIdentifier value)
    {
        if (value.isDatabaseInstanceLevel())
        {
            return databaseInstanceIdentifierPredicate.doEvaluation(person, allowedRoles, value
                    .getDatabaseInstanceLevel());
        } else
        {
            return groupIdentifierPredicate.doEvaluation(person, allowedRoles, value
                    .getGroupLevel());
        }
    }

}
