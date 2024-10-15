/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public class InternalPropertyTypeAuthorization
{

    public void canCreatePropertyType(Session session, PropertyTypePE propertyType)
    {
        checkPropertyType(session, propertyType);
    }

    public void canUpdatePropertyType(Session session, PropertyTypePE propertyType)
    {
        checkPropertyType(session, propertyType);
    }

    public void canDeletePropertyType(Session session, PropertyTypePE propertyType)
    {
        checkPropertyType(session, propertyType);
    }

    public void canCreatePropertyAssignment(Session session, PropertyTypePE propertyType, EntityTypePropertyTypePE propertyAssignment)
    {
        if(propertyAssignment.isManagedInternallyNamespace())
        {
            if(propertyType.isManagedInternally())
            {
                if(isSystemUser(session) == false)
                {
                    throw new AuthorizationFailureException(
                            "Internal property assignments can be managed only by the system user.");
                }
            } else {
                throw new UserFailureException(
                        "Internal property assignments can be added only to internal property types.");
            }
        }
    }

    public void canUpdatePropertyAssignment(Session session, PropertyTypePE propertyType, EntityTypePropertyTypePE propertyAssignment)
    {
        checkPropertyAssignment(session, propertyAssignment);
    }

    public void canDeletePropertyAssignment(Session session, PropertyTypePE propertyType, EntityTypePropertyTypePE propertyAssignment)
    {
        checkPropertyAssignment(session, propertyAssignment);
    }

    private void checkPropertyType(Session session, PropertyTypePE propertyType)
    {
        if (propertyType.isManagedInternally() && isSystemUser(session) == false)
        {
            throw new AuthorizationFailureException("Internal property types can be managed only by the system user.");
        }
    }

    private void checkPropertyAssignment(Session session, EntityTypePropertyTypePE propertyAssignment)
    {
        if (propertyAssignment.isManagedInternallyNamespace() && isSystemUser(session) == false)
        {
            throw new AuthorizationFailureException(
                    "Property assignments created by the system user for internal property types can be managed only by the system user.");
        }
    }

    private boolean isSystemUser(Session session)
    {
        PersonPE user = session.tryGetPerson();

        if (user == null)
        {
            throw new AuthorizationFailureException("Could not check access because the current session does not have any user assigned.");
        } else
        {
            return user.isSystemUser();
        }
    }

}
