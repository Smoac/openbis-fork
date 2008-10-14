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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.GroupTranslater;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.RoleAssignmentTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.UserFailureExceptionTranslater;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class GenericClientService implements IGenericClientService
{
    static final String SESSION_KEY = "openbis-session";

    static final String SERVER_KEY = "openbis-generic-server";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, GenericClientService.class);

    private final IGenericServer server;

    private final IRequestContextProvider requestContextProvider;

    public GenericClientService(final IGenericServer server,
            final IRequestContextProvider requestContextProvider)
    {
        this.server = server;
        this.requestContextProvider = requestContextProvider;
    }

    void setConfigParameters(GenericConfigParameters configParameters)
    {
    }

    private SessionContext createSessionContext(final Session session)
    {
        SessionContext sessionContext = new SessionContext();
        sessionContext.setSessionID(session.getSessionToken());
        User user = new User();
        user.setUserName(session.getUserName());
        PersonPE person = session.tryGetPerson();
        if (person != null)
        {
            GroupPE homeGroup = person.getHomeGroup();
            if (homeGroup != null)
            {
                user.setHomeGroupCode(homeGroup.getCode());
            }
        }
        sessionContext.setUser(user);
        return sessionContext;
    }

    private String getSessionToken()
    {
        HttpSession httpSession = getHttpSession();
        if (httpSession == null)
        {
            throw new InvalidSessionException("Session expired. Please login again.");
        }
        return getSession(httpSession).getSessionToken();
    }

    private Session getSession(final HttpSession httpSession)
    {
        Session session = (Session) httpSession.getAttribute(SESSION_KEY);
        if (session == null)
        {
            final String remoteHost =
                    requestContextProvider.getHttpServletRequest().getRemoteHost();
            final String msg =
                    "Attempt to get non-existent session from host '" + remoteHost
                            + "': user is not logged in.";
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(msg);
            }
            throw new InvalidSessionException(msg);

        }
        return session;
    }

    private HttpSession getHttpSession()
    {
        return getOrCreateHttpSession(false);
    }

    private HttpSession creatHttpSession()
    {
        return getOrCreateHttpSession(true);
    }

    private HttpSession getOrCreateHttpSession(boolean create)
    {
        return requestContextProvider.getHttpServletRequest().getSession(create);
    }

    public ApplicationInfo getApplicationInfo()
    {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.setVersion(BuildAndEnvironmentInfo.INSTANCE.getFullVersion());
        return applicationInfo;
    }

    public SessionContext tryToGetCurrentSessionContext()
    {
        final HttpSession httpSession = getHttpSession();
        if (httpSession == null)
        {
            return null;
        }
        final Session session = getSession(httpSession);
        return createSessionContext(session);
    }

    public SessionContext tryToLogin(String userID, String password)
    {
        try
        {
            Session session = server.tryToAuthenticate(userID, password);
            if (session == null)
            {
                return null;
            }
            HttpSession httpSession = creatHttpSession();
            // Expiration time of httpSession is 10 seconds less than of session
            httpSession.setMaxInactiveInterval(session.getSessionExpirationTime() / 1000 - 10);
            httpSession.setAttribute(SESSION_KEY, session);
            httpSession.setAttribute(SERVER_KEY, server);
            return createSessionContext(session);
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }
    }

    public void logout()
    {
        HttpSession httpSession = getHttpSession();
        if (httpSession != null)
        {
            Session session = getSession(httpSession);
            httpSession.removeAttribute(SESSION_KEY);
            httpSession.removeAttribute(SERVER_KEY);
            httpSession.invalidate();
            server.logout(session.getSessionToken());
        }
    }

    public List<Group> listGroups(String databaseInstanceCode)
    {
        try
        {
            DatabaseInstanceIdentifier identifier =
                    new DatabaseInstanceIdentifier(databaseInstanceCode);
            List<Group> result = new ArrayList<Group>();
            List<GroupPE> groups = server.listGroups(getSessionToken(), identifier);
            for (GroupPE group : groups)
            {
                result.add(GroupTranslater.translate(group));
            }
            return result;
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }
    }

    public void registerGroup(String groupCode, String descriptionOrNull, String groupLeaderOrNull)
    {
        try
        {
            String sessionToken = getSessionToken();
            server.registerGroup(sessionToken, groupCode, descriptionOrNull, groupLeaderOrNull);
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }
    }

    public List<Person> listPersons()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {

        try
        {
            List<Person> result = new ArrayList<Person>();
            List<PersonPE> persons = server.listPersons(getSessionToken());
            for (PersonPE person : persons)
            {
                result.add(PersonTranslator.translate(person));
            }
            return result;
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }
    }

    public void registerPerson(String code)
    {
        try
        {
            String sessionToken = getSessionToken();
            server.registerPerson(sessionToken, code);
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }
    }

    public List<RoleAssignment> listRoles()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            List<RoleAssignment> result = new ArrayList<RoleAssignment>();
            List<RoleAssignmentPE> roles = server.listRoles(getSessionToken());
            for (RoleAssignmentPE role : roles)
            {
                result.add(RoleAssignmentTranslator.translate(role));
            }
            return result;
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }
    }

    public void registerGroupRole(String roleSetCode, String group, String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            String sessionToken = getSessionToken();
            server.registerGroupRole(sessionToken, translateRoleSetCode(roleSetCode),
                    groupIdentifier, person);
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }
    }

    public void registerInstanceRole(String roleSetCode, String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            String sessionToken = getSessionToken();
            server.registerInstanceRole(sessionToken, translateRoleSetCode(roleSetCode), person);
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }
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

    public void deleteGroupRole(String roleSetCode, String group, String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            String sessionToken = getSessionToken();
            server.deleteGroupRole(sessionToken, translateRoleSetCode(roleSetCode),
                    groupIdentifier, person);
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }

    }

    public void deleteInstanceRole(String roleSetCode, String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            String sessionToken = getSessionToken();
            server.deleteInstanceRole(sessionToken, translateRoleSetCode(roleSetCode), person);
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }

    }

    public List<SampleType> listSampleTypes()
    {
        try
        {
            List<SampleTypePE> sampleTypes = server.listSampleTypes(getSessionToken());
            List<SampleType> result = new ArrayList<SampleType>();
            for (SampleTypePE sampleTypePE : sampleTypes)
            {
                result.add(SampleTypeTranslator.translate(sampleTypePE));
            }
            return result;
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }
    }

    public List<Sample> listSamples(SampleType sampleType, String groupCode, boolean includeGroup,
            boolean includeInstance)
    {
        try
        {
            List<SampleOwnerIdentifier> ownerIdentifiers = new ArrayList<SampleOwnerIdentifier>();
            if (includeGroup)
            {
                ownerIdentifiers.add(new SampleOwnerIdentifier(new GroupIdentifier(
                        DatabaseInstanceIdentifier.HOME, groupCode)));
            }
            if (includeInstance)
            {
                ownerIdentifiers.add(new SampleOwnerIdentifier(DatabaseInstanceIdentifier
                        .createHome()));
            }
            List<SamplePE> samples =
                    server.listSamples(getSessionToken(), ownerIdentifiers, SampleTypeTranslator
                            .translate(sampleType), SampleTypeTranslator
                            .extractPropertyTypeCodesToDisplay(sampleType));
            List<Sample> result = new ArrayList<Sample>();
            for (SamplePE sample : samples)
            {
                result.add(SampleTranslator.translate(sample, sampleType));
            }
            return result;
        } catch (UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }
    }
}
