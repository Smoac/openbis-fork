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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collections;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.spring.IInvocationLoggerFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * An <i>abstract</i> {@link IServer} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractServer<T extends IServer> implements IServer, ISessionProvider,
        IInvocationLoggerFactory<T>
{
    private static ISessionManager<Session> sessionManager;

    private IDAOFactory daoFactory;

    protected AbstractServer(final ISessionManager<Session> sessionManager,
            final IDAOFactory daoFactory)
    {
        if (AbstractServer.sessionManager == null)
        {
            AbstractServer.sessionManager = sessionManager;
        }
        this.daoFactory = daoFactory;
    }

    protected final PersonPE createPerson(final Principal principal, final PersonPE registrator)
    {
        final PersonPE person = new PersonPE();
        person.setUserId(principal.getUserId());
        person.setFirstName(principal.getFirstName());
        person.setLastName(principal.getLastName());
        person.setEmail(principal.getEmail());
        person.setRegistrator(registrator);
        daoFactory.getPersonDAO().createPerson(person);
        return person;
    }

    protected final ISessionManager<Session> getSessionManager()
    {
        return sessionManager;
    }

    protected final IDAOFactory getDAOFactory()
    {
        return daoFactory;
    }

    //
    // IServer
    //

    public final IAuthSession getSession(final String sessionToken)
    {
        return sessionManager.getSession(sessionToken);
    }

    public final int getVersion()
    {
        return 1;
    }

    public final void logout(final String sessionToken)
    {
        sessionManager.closeSession(sessionToken);
    }

    @Transactional
    public final Session tryToAuthenticate(final String user, final String password)
    {
        final String sessionToken = sessionManager.tryToOpenSession(user, password);
        if (sessionToken == null)
        {
            return null;
        }
        final Session session = sessionManager.getSession(sessionToken);
        final List<PersonPE> persons = daoFactory.getPersonDAO().listPersons();
        final boolean isFirstLoggedUser = persons.size() == 1;
        final PersonPE registrator = persons.get(0);
        PersonPE personPE = daoFactory.getPersonDAO().tryFindPersonByUserId(user);
        if (personPE == null)
        {
            personPE = createPerson(session.getPrincipal(), registrator);
        } else
        {
            Hibernate.initialize(personPE.getRoleAssignments());
        }
        if (session.tryGetPerson() == null)
        {
            session.setPerson(personPE);
        }
        if (isFirstLoggedUser)
        {
            final RoleAssignmentPE roleAssignmentPE = new RoleAssignmentPE();
            final PersonPE person = session.tryGetPerson();
            roleAssignmentPE.setPerson(person);
            roleAssignmentPE.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
            roleAssignmentPE.setRegistrator(registrator);
            roleAssignmentPE.setRole(RoleCode.ADMIN);
            daoFactory.getRoleAssignmentDAO().createRoleAssignment(roleAssignmentPE);
            person.setRoleAssignments(Collections.singletonList(roleAssignmentPE));
        }
        return session;
    }

}
