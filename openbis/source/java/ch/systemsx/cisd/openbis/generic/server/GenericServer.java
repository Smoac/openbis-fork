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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ch.systemsx.cisd.authentication.DefaultSessionManager;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.servlet.RequestContextProviderAdapter;
import ch.systemsx.cisd.lims.base.dto.GroupPE;
import ch.systemsx.cisd.lims.base.dto.PersonPE;
import ch.systemsx.cisd.lims.base.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GenericServer implements IGenericServer
{
    private final ISessionManager<Session> sessionManager;
    private List<GroupPE> groups;

    public GenericServer(IAuthenticationService authenticationService,
            IRequestContextProvider requestContextProvider, int sessionExpirationPeriodInMinutes)
    {
        this(new DefaultSessionManager<Session>(new SessionFactory(),
                new LogMessagePrefixGenerator(), authenticationService,
                new RequestContextProviderAdapter(requestContextProvider),
                sessionExpirationPeriodInMinutes));
        createFakeGroups();
    }

    private void createFakeGroups()
    {
        groups = new ArrayList<GroupPE>();
        Random random = new Random(137);
        for (int i = 0; i < 20; i++)
        {
            groups.add(createFakeGroup("TEST" + random.nextInt(1000)));
        }
    }

    private GroupPE createFakeGroup(String code)
    {
        GroupPE group = new GroupPE();
        group.setCode(code);
        group.setDescription("The " + code + " group");
        group.setRegistrationDate(new Date());
        PersonPE person = new PersonPE();
        person.setFirstName("John");
        person.setLastName("Doe");
        group.setRegistrator(person);
        return group;
    }
    
    GenericServer(ISessionManager<Session> sessionManager)
    {
        this.sessionManager = sessionManager;
    }
    
    public int getVersion()
    {
        return 1;
    }

    public void logout(String sessionToken)
    {
        sessionManager.closeSession(sessionToken);
    }

    public Session tryToAuthenticate(String user, String password)
    {
        String sessionToken = sessionManager.tryToOpenSession(user, password);
        if (sessionToken == null)
        {
            return null;
        }
        Session session = sessionManager.getSession(sessionToken);
        // TODO 2008-09-09, Franz-Josef Elmer: Load person info for user from database
        PersonPE person = new PersonPE();
        person.setFirstName(user);
        person.setLastName(user);
        person.setEmail(user + "@nowhere");
        session.setPerson(person);
        return session;
    }

    public List<GroupPE> listGroups(String sessionToken, DatabaseInstanceIdentifier identifier)
    {
        sessionManager.getSession(sessionToken);
        return groups;
    }

    public void registerGroup(String sessionToken, String groupCode, String descriptionOrNull,
            String groupLeaderOrNull)
    {
        Session session = sessionManager.getSession(sessionToken);
        for (GroupPE existingGroup : groups)
        {
            if (existingGroup.getCode().equalsIgnoreCase(groupCode))
            {
                throw new UserFailureException("There is already a group with code " + existingGroup.getCode());
            }
        }
        GroupPE group = new GroupPE();
        group.setCode(groupCode.toUpperCase());
        group.setDescription(descriptionOrNull);
        group.setRegistrationDate(new Date());
        group.setRegistrator(session.tryToGetPerson());
        groups.add(group);
    }

}
