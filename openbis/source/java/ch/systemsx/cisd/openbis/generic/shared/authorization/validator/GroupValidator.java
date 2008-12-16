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

package ch.systemsx.cisd.openbis.generic.shared.authorization.validator;

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * A {@link IValidator} implementation suitable for {@link GroupPE}.
 * 
 * @author Christian Ribeaud
 */
public final class GroupValidator extends AbstractValidator<GroupPE>
{
    private final IValidator<DatabaseInstancePE> databaseInstanceValidator;

    public GroupValidator()
    {
        databaseInstanceValidator = new DatabaseInstanceValidator();
    }

    //
    // AbstractValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final GroupPE value)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getRoleAssignments();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            final GroupPE group = roleAssignment.getGroup();
            if (group != null && group.equals(value))
            {
                return true;
            }
        }
        return databaseInstanceValidator.isValid(person, value.getDatabaseInstance());
    }
}
