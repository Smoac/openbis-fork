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

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.DefaultSessionManager;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.servlet.RequestContextProviderAdapter;
import ch.systemsx.cisd.common.spring.IInvocationLoggerFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.GenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Implementation of client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericServer implements IGenericServer, ISessionProvider,
        IInvocationLoggerFactory<IGenericServer>
{
    private final ISessionManager<Session> sessionManager;

    private final IDAOFactory daoFactory;

    private final IGenericBusinessObjectFactory boFactory;

    private final IAuthenticationService authenticationService;

    public GenericServer(IAuthenticationService authenticationService,
            IRequestContextProvider requestContextProvider, IDAOFactory daoFactory,
            int sessionExpirationPeriodInMinutes)
    {
        this(authenticationService, new DefaultSessionManager<Session>(new SessionFactory(),
                new LogMessagePrefixGenerator(), authenticationService,
                new RequestContextProviderAdapter(requestContextProvider),
                sessionExpirationPeriodInMinutes), daoFactory, new GenericBusinessObjectFactory(
                daoFactory));
    }

    @Private
    GenericServer(IAuthenticationService authenticationService,
            ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            IGenericBusinessObjectFactory boFactory)
    {
        this.authenticationService = authenticationService;
        this.daoFactory = daoFactory;
        this.sessionManager = sessionManager;
        this.boFactory = boFactory;
    }

    private PersonPE createPerson(Principal principal, PersonPE registrator)
    {
        PersonPE person;
        person = new PersonPE();
        person.setUserId(principal.getUserId());
        person.setFirstName(principal.getFirstName());
        person.setLastName(principal.getLastName());
        person.setEmail(principal.getEmail());
        person.setRegistrator(registrator);
        daoFactory.getPersonDAO().createPerson(person);
        return person;
    }

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public GenericServerLogger createLogger(boolean invocationSuccessful)
    {
        return new GenericServerLogger(sessionManager, invocationSuccessful);
    }

    public IAuthSession getSession(String sessionToken)
    {
        return sessionManager.getSession(sessionToken);
    }

    public int getVersion()
    {
        return 1;
    }

    public void logout(String sessionToken)
    {
        sessionManager.closeSession(sessionToken);
    }

    @Transactional
    public Session tryToAuthenticate(String user, String password)
    {
        String sessionToken = sessionManager.tryToOpenSession(user, password);
        if (sessionToken == null)
        {
            return null;
        }
        Session session = sessionManager.getSession(sessionToken);
        final List<PersonPE> persons = daoFactory.getPersonDAO().listPersons();
        final boolean isFirstLoggedUser = persons.size() == 1;
        PersonPE registrator = persons.get(0);
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

    @Transactional
    public List<GroupPE> listGroups(String sessionToken, DatabaseInstanceIdentifier identifier)
    {
        Session session = sessionManager.getSession(sessionToken);
        DatabaseInstancePE databaseInstance =
                GroupIdentifierHelper.getDatabaseInstance(identifier, daoFactory);
        List<GroupPE> groups = daoFactory.getGroupDAO().listGroups(databaseInstance);
        Long homeGroupID = session.tryGetHomeGroupId();
        for (final GroupPE group : groups)
        {
            group.setHome(homeGroupID != null && homeGroupID.equals(group.getId()));
        }
        return groups;
    }

    @Transactional
    public void registerGroup(String sessionToken, String groupCode, String descriptionOrNull,
            String groupLeaderOrNull)
    {
        Session session = sessionManager.getSession(sessionToken);
        IGroupBO groupBO = boFactory.createGroupBO(session);
        groupBO.define(groupCode, descriptionOrNull, groupLeaderOrNull);
        groupBO.save();
    }

    @Transactional
    public void registerPerson(String sessionToken, String userID)
    {
        Session session = sessionManager.getSession(sessionToken);
        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(userID);
        if (person != null)
        {
            throw UserFailureException.fromTemplate("Person '%s' already exists.", userID);
        }
        String applicationToken = authenticationService.authenticateApplication();
        if (applicationToken == null)
        {
            throw new EnvironmentFailureException("Authentication service cannot be accessed.");
        }
        try
        {
            Principal principal = authenticationService.getPrincipal(applicationToken, userID);
            createPerson(principal, session.tryGetPerson());
        } catch (IllegalArgumentException e)
        {
            throw new UserFailureException("Person '" + userID
                    + "' unknown by the authentication service.");
        }
    }

    public List<RoleAssignmentPE> listRoles(String sessionToken)
    {
        sessionManager.getSession(sessionToken);
        return daoFactory.getRoleAssignmentDAO().listRoleAssignments();
    }

    public void registerRole(String sessionToken, String roleSetCode, String group, String person)
    {
        Session session = sessionManager.getSession(sessionToken);

        NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setUserId(person);
        if (StringUtils.isBlank(group) == false)
        {
            newRoleAssignment.setGroupIdentifier(new GroupIdentifier(
                    DatabaseInstanceIdentifier.HOME, group));
        }
        newRoleAssignment.setRole(translateRoleSetCode(roleSetCode));

        final IRoleAssignmentTable table = boFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    private RoleCode translateRoleSetCode(String code)
    {

        if ("INSTANCE_ADMIN".compareTo(code) == 0)
        {
            return RoleCode.ADMIN;
        } else if ("GROUP_ADMIN".compareTo(code) == 0)
        {
            return RoleCode.ADMIN;
        } else if ("USER".compareTo(code) == 0)
        {
            return RoleCode.USER;
        } else if ("OBSERVER".compareTo(code) == 0)
        {
            return RoleCode.OBSERVER;
        } else
        {
            throw new IllegalArgumentException("Unknown role set");
        }

    }

    public void deleteRole(String sessionToken, String roleSetCode, String group, String person)
    {

        sessionManager.getSession(sessionToken);

        RoleAssignmentPE roleAssignment =
                daoFactory.getRoleAssignmentDAO().getRoleAssignment(
                        translateRoleSetCode(roleSetCode), group, person);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given role does not exist.");
        }
        daoFactory.getRoleAssignmentDAO().deleteRoleAssignment(roleAssignment);
    }

    @Transactional
    public List<PersonPE> listPersons(String sessionToken)
    {
        sessionManager.getSession(sessionToken);
        List<PersonPE> persons = daoFactory.getPersonDAO().listPersons();
        return persons;
    }

}
