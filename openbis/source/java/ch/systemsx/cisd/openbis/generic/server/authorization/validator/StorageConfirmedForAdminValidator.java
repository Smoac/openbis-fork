/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * @author Jakub Straszewski
 */
public class StorageConfirmedForAdminValidator extends
        AbstractValidator<ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData>
{

    private static boolean isPersonAllowedForNotConfirmed(PersonPE person)
    {
        for (RoleAssignmentPE role : person.getAllPersonRoles())
        {
            if (role.getRole() == RoleCode.ADMIN || role.getRole() == RoleCode.ETL_SERVER)
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isValid(PersonPE person, boolean isStorageConfirmed)
    {
        return isStorageConfirmed || isPersonAllowedForNotConfirmed(person);
    }

    @Override
    public boolean doValidation(PersonPE person,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData value)
    {
        return isValid(person, value.isStorageConfirmation());
    }
}
