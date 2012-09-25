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
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * An <code>IPredicate</code> implementation based on {@link SampleOwnerIdentifier}.
 * 
 * @author Christian Ribeaud
 */
public class SampleOwnerIdentifierPredicate extends AbstractPredicate<SampleOwnerIdentifier>
{
    private final SpaceIdentifierPredicate spacePredicate;

    private final DatabaseInstanceIdentifierPredicate databaseInstanceIdentifierPredicate;

    public SampleOwnerIdentifierPredicate()
    {
        this(true, false);
    }

    public SampleOwnerIdentifierPredicate(boolean isReadAccess)
    {
        this(isReadAccess, false);
    }

    public SampleOwnerIdentifierPredicate(boolean isReadAccess, boolean okForNonExistentSpaces)
    {
        spacePredicate = new SpaceIdentifierPredicate(okForNonExistentSpaces);
        databaseInstanceIdentifierPredicate = new DatabaseInstanceIdentifierPredicate(isReadAccess);
    }

    //
    // AbstractPredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        spacePredicate.init(provider);
        databaseInstanceIdentifierPredicate.init(provider);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "sample identifier";
    }

    @Override
    protected final Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final SampleOwnerIdentifier value)
    {
        return performEvaluation(person, allowedRoles, value);
    }

    /**
     * @deprecated exposed only for usage in screening api authorization - use 'doEvaluation()' in
     *             other places
     */
    @Deprecated
    public Status performEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final SampleOwnerIdentifier value)
    {
        // Skip all further checks if the person has instance-wide write permissions.
        if (hasInstanceWritePermissions(person, allowedRoles).isOK())
        {
            return Status.OK;
        }

        if (value.isDatabaseInstanceLevel())
        {
            return databaseInstanceIdentifierPredicate.doEvaluation(person, allowedRoles,
                    value.getDatabaseInstanceLevel());
        } else
        {
            return spacePredicate.doEvaluation(person, allowedRoles, value.getSpaceLevel());
        }
    }

}
