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

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractGridExpression;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;

/**
 * A {@link IValidator} implementation for grid custom filter or column. Public internal class
 * provide predicates for updates and deletions based on {@link TechId}.
 * 
 * @author Izabela Adamczyk
 */
public final class CustomGridExpressionValidator extends
        AbstractValidator<AbstractGridExpression>
{
    //
    // IValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final AbstractGridExpression value)
    {
        return value.isPublic() || isRegistrator(person, value)
                || isInstanceAdmin(person, value.getDatabaseInstance());

    }

    private boolean isRegistrator(final PersonPE person, final AbstractGridExpression value)
    {
        Person registrator = value.getRegistrator();
        return person.getUserId().equals(registrator.getUserId())
                && person.getDatabaseInstance().getCode().equals(
                        registrator.getDatabaseInstance().getCode());
    }

    public boolean isInstanceAdmin(final PersonPE person, final DatabaseInstance databaseInstance)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            final DatabaseInstancePE roleInstance = roleAssignment.getDatabaseInstance();
            if (roleInstance != null && roleInstance.getUuid().equals(databaseInstance.getUuid())
                    && roleAssignment.getRole().equals(RoleCode.ADMIN))
            {
                return true;
            }
        }
        return false;
    }

}
