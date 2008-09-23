/*
 * Copyright 2007 ETH Zuerich, CISD
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

import org.hibernate.Hibernate;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * The only productive implementation of {@link IPersonBO}. We are using an interface here to keep
 * the system testable.
 * 
 * @author Christian Ribeaud
 */
final class PersonBO extends AbstractBusinessObject implements IPersonBO
{

    private PersonPE personPE;

    PersonBO(final IAuthorizationDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    private final String getUserId()
    {
        final String userName = session.getUserName();
        assert userName != null : "User id can not be null";
        return userName;
    }

    //
    // IPersonBO
    //

    public final void enrichSessionWithPerson()
    {
        personPE = getPersonDAO().tryFindPersonByUserId(getUserId());
        if (personPE == null)
        {
            final String userId = getUserId();
            final Principal principal = session.getPrincipal();
            personPE = new PersonPE();
            if (principal != null)
            {
                personPE.setFirstName(principal.getFirstName());
                personPE.setLastName(principal.getLastName());
                personPE.setEmail(principal.getEmail());
            }
            personPE.setUserId(userId);
            save();
        } else
        {
            Hibernate.initialize(personPE.getRoleAssignments());
        }
        if (session.tryGetPerson() == null)
        {
            session.setPerson(personPE);
        }
    }

    public final void registerPerson(String code)
    {
        personPE = getPersonDAO().tryFindPersonByUserId(code);
        if (personPE == null)
        {
            personPE = new PersonPE();
            personPE.setUserId(code);
            personPE.setRegistrator(session.tryGetPerson());
            save();
        } else
        {
            throw UserFailureException.fromTemplate("Person '%s' already exists.", code);
        }
    }

    public final void load(final String userId) throws UserFailureException
    {
        assert userId != null : "Unspecified user id.";

        personPE = getPersonDAO().tryFindPersonByUserId(userId);
        if (personPE == null)
        {
            throw UserFailureException.fromTemplate("Unknown person '%s'", userId);
        }
    }

    public final void save()
    {
        assert personPE != null : "Person not defined.";
        try
        {
            getPersonDAO().createPerson(personPE);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("User id '%s'", personPE.getUserId()));
        }
    }

    public final void setHomeGroup(final GroupPE group)
    {
        assert personPE != null : "Person not loaded.";

        personPE.setHomeGroup(group);
        try
        {
            getPersonDAO().updatePerson(personPE);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("User id '%s'", personPE.getUserId()));
        }
    }

    public final Long getLoggedInUserId()
    {
        if (personPE != null)
        {
            return personPE.getId();
        } else
        {
            return findRegistratorID();
        }
    }
}
