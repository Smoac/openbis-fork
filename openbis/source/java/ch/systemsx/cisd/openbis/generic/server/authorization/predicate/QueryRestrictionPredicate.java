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

import ch.systemsx.cisd.lims.base.dto.QueryRestriction;
import ch.systemsx.cisd.lims.base.identifier.GroupIdentifier;

/**
 * An <code>IPredicate</code> implementation for {@link QueryRestriction}.
 * 
 * @author Christian Ribeaud
 */
public final class QueryRestrictionPredicate extends
        DelegatedPredicate<GroupIdentifier, QueryRestriction>
{
    public QueryRestrictionPredicate()
    {
        super(new GroupIdentifierPredicate());
    }

    //
    // DelegatedPredicate
    //

    @Override
    final GroupIdentifier convert(final QueryRestriction value)
    {
        GroupIdentifier groupIdentifier = value.tryGetGroupIdentifier();
        if (groupIdentifier == null)
        {
            groupIdentifier = value.tryGetProjectIdentifier();
        }
        return groupIdentifier == null ? GroupIdentifier.createHome() : groupIdentifier;
    }

    @Override
    final String getCandidateDescription()
    {
        return "query restriction";
    }
}
