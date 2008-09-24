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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

public class RoleAssignmentTranslator
{
    private RoleAssignmentTranslator()
    {
    }

    public static RoleAssignment translate(RoleAssignmentPE role)
    {
        if (role == null)
        {
            return null;
        }
        RoleAssignment result = new RoleAssignment();
        result.setGroup(GroupTranslater.translate(role.getGroup()));
        result.setInstance(InstanceTranslater.translate(role.getDatabaseInstance()));
        result.setPerson(PersonTranslator.translate(role.getPerson()));
        result.setCode(getRoleCode(role));
        return result;
    }

    private static String getRoleCode(RoleAssignmentPE role)
    {
        String code;
        switch (role.getRole())
        {
            case ADMIN:
                if (role.getGroup() == null)
                {
                    code = "INSTANCE_ADMIN";
                } else
                {
                    code = "GROUP_ADMIN";
                }
                break;
            case OBSERVER:
                code = "OBSERVER";
                break;
            case USER:
                code = "USER";
                break;
            case ETL_SERVER:
                code = "ETL_SERVER";
                break;
            default:
                throw new IllegalArgumentException("Unknown role");
        }
        return code;
    }

}
