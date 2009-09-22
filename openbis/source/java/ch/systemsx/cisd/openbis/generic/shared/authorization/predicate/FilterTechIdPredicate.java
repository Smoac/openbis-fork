/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * An <code>IPredicate</code> implementation based on {@link TechId} of a filter.
 * 
 * @author Piotr Buczek
 */

public abstract class FilterTechIdPredicate extends AbstractDatabaseInstancePredicate<TechId>
{

    public static class UpdateFilterTechIdPredicate extends FilterTechIdPredicate
    {
        public UpdateFilterTechIdPredicate()
        {
            super(OperationKind.MODIFY);
        }
    }

    public static class DeleteFilterTechIdPredicate extends FilterTechIdPredicate
    {
        public DeleteFilterTechIdPredicate()
        {
            super(OperationKind.DELETE);
        }
    }

    private static enum OperationKind
    {
        MODIFY, DELETE;

        public String getDescription()
        {
            return name().toLowerCase();
        }
    }

    private final OperationKind operationKind;

    public FilterTechIdPredicate(OperationKind operationKind)
    {
        this.operationKind = operationKind;
    }

    //
    // AbstractPredicate
    //

    @Override
    public final String getCandidateDescription()
    {
        return "filter technical id";
    }

    @Override
    final Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final TechId techId)
    {

        final FilterPE filter = authorizationDataProvider.getFilter(techId);
        final boolean matching = isMatching(person, filter);
        if (matching)
        {
            return Status.OK;
        }
        String userId = person.getUserId();
        return Status.createError(createErrorMsg(filter, userId));
    }

    private static boolean isMatching(PersonPE person, FilterPE filter)
    {
        // needs to be an instance admin in filter database instance or registrator of a filter
        return isRegistrator(person, filter)
                || isInstanceAdmin(person, filter.getDatabaseInstance());
    }

    private String createErrorMsg(FilterPE filter, String userId)
    {
        return String
                .format(
                        STATUS_MESSAGE_PREFIX_FORMAT
                                + operationKind.getDescription()
                                + " filter '%s'. One needs to be either filter registrator or database instance admin.",
                        userId, filter.getName());
    }

    private static boolean isRegistrator(final PersonPE person, final FilterPE filter)
    {
        return person.equals(filter.getRegistrator());
    }

    private static boolean isInstanceAdmin(final PersonPE person,
            final DatabaseInstancePE databaseInstance)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            final DatabaseInstancePE roleInstance = roleAssignment.getDatabaseInstance();
            if (databaseInstance.equals(roleInstance))
            {
                return true;
            }
        }
        return false;
    }
}
