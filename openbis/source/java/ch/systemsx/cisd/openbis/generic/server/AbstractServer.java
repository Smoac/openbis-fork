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

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.IInvocationLoggerFactory;
import ch.systemsx.cisd.common.spring.LogInterceptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * An <i>abstract</i> {@link IServer} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractServer<T extends IServer> implements IServer,
        IInvocationLoggerFactory<T>, FactoryBean
{
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractServer.class);

    @Resource(name = ComponentNames.SESSION_MANAGER)
    private ISessionManager<Session> sessionManager;

    @Resource(name = ComponentNames.DAO_FACTORY)
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.LOG_INTERCEPTOR)
    private LogInterceptor logInterceptor;

    private ProxyFactory proxyFactory;

    protected AbstractServer()
    {
        operationLog.info(String.format("Creating new '%s' implementation: '%s'.", IServer.class
                .getSimpleName(), getClass().getName()));
    }

    protected AbstractServer(final ISessionManager<Session> sessionManager,
            final IDAOFactory daoFactory)
    {
        this();
        this.sessionManager = sessionManager;
        this.daoFactory = daoFactory;
    }

    private final ProxyFactory getProxyFactory()
    {
        if (proxyFactory == null)
        {
            proxyFactory = new ProxyFactory();
            proxyFactory.setTarget(this);
            proxyFactory.setInterfaces(new Class[]
                { getProxyInterface() });
            proxyFactory.addAdvice(logInterceptor);
        }
        return proxyFactory;
    }

    private final RoleAssignmentPE createRoleAssigment(final PersonPE registrator,
            final PersonPE person)
    {
        final RoleAssignmentPE roleAssignmentPE = new RoleAssignmentPE();
        roleAssignmentPE.setPerson(person);
        roleAssignmentPE.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        roleAssignmentPE.setRegistrator(registrator);
        roleAssignmentPE.setRole(RoleCode.ADMIN);
        return roleAssignmentPE;
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

    private final static PersonPE getSystemUser(final List<PersonPE> persons)
    {
        for (final PersonPE personPE : persons)
        {
            if (personPE.isSystemUser())
            {
                return personPE;
            }
        }
        throw new IllegalStateException(String.format(
                "No system user could be found in given list '%s'.", persons));
    }

    protected final ISessionManager<Session> getSessionManager()
    {
        return sessionManager;
    }

    protected final IDAOFactory getDAOFactory()
    {
        return daoFactory;
    }

    protected abstract Class<T> getProxyInterface();

    //
    // FactoryBean
    //

    public final Object getObject() throws Exception
    {
        return getProxyFactory().getProxy();
    }

    @SuppressWarnings("unchecked")
    public final Class getObjectType()
    {
        return getClass();
    }

    public final boolean isSingleton()
    {
        return true;
    }

    //
    // IServer
    //

    public final IAuthSession getSession(final String sessionToken) throws UserFailureException
    {
        return sessionManager.getSession(sessionToken);
    }

    public int getVersion()
    {
        return 1;
    }

    public final void logout(final String sessionToken) throws UserFailureException
    {
        try
        {
            sessionManager.closeSession(sessionToken);
        } catch (InvalidSessionException e)
        {
            // ignore the situation when session is not available
        }
    }

    public final Session tryToAuthenticate(final String user, final String password)
    {
        final String sessionToken = sessionManager.tryToOpenSession(user, password);
        if (sessionToken == null)
        {
            return null;
        }
        final Session session = sessionManager.getSession(sessionToken);
        final List<PersonPE> persons = daoFactory.getPersonDAO().listPersons();
        assert persons.size() > 0 : "At least system user should be in the database";
        // If only one user (system user), then this is the first logged user.
        final boolean isFirstLoggedUser = persons.size() == 1;
        final PersonPE systemUser = getSystemUser(persons);
        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(user);
        if (person == null)
        {
            person = createPerson(session.getPrincipal(), systemUser);
        } else
        {
            HibernateUtils.initialize(person.getRoleAssignments());
        }
        if (session.tryGetPerson() == null)
        {
            session.setPerson(person);
        }
        if (isFirstLoggedUser)
        {
            // First logged user does have any role assignment yet. Make him database instance
            // administrator.
            final RoleAssignmentPE roleAssignment = createRoleAssigment(systemUser, person);
            person.setRoleAssignments(Collections.singleton(roleAssignment));
            daoFactory.getPersonDAO().updatePerson(person);
        }
        return session;
    }
}
