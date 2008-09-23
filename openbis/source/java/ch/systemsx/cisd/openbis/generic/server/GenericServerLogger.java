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

import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;

/**
 * Logger class for {@link GenericServer} which creates readable logs of method invocations.
 * 
 * @author Franz-Josef Elmer
 */
class GenericServerLogger implements IGenericServer, ISessionProvider
{
    private final static String RESULT_SUCCESS = "";

    private final static String RESULT_FAILURE = " ...FAILED";

    private static final Logger accessLog =
            LogFactory.getLogger(LogCategory.ACCESS, GenericServer.class);

    private static final Logger trackingLog =
            LogFactory.getLogger(LogCategory.TRACKING, GenericServer.class);

    private final ISessionManager<Session> sessionManager;

    private final boolean invocationSuccessful;

    private final LogMessagePrefixGenerator logMessagePrefixGenerator;

    /**
     * Creates an instance for the specified session manager and invocation status. The session
     * manager is used to retrieve user information which will be a part of the log message.
     */
    GenericServerLogger(ISessionManager<Session> sessionManager, boolean invocationSuccessful)
    {
        this.sessionManager = sessionManager;
        this.invocationSuccessful = invocationSuccessful;
        logMessagePrefixGenerator = new LogMessagePrefixGenerator();
    }

    private void logAccess(final String sessionToken, final String commandName,
            final String parameterDisplayFormat, final Object... parameters)
    {
        logMessage(accessLog, sessionToken, commandName, parameterDisplayFormat, parameters);
    }

    private void logTracking(final String sessionToken, final String commandName,
            final String parameterDisplayFormat, final Object... parameters)
    {
        logMessage(trackingLog, sessionToken, commandName, parameterDisplayFormat, parameters);
    }

    private void logMessage(final Logger logger, final String sessionToken,
            final String commandName, final String parameterDisplayFormat, final Object[] parameters)
    {
        final Session session = sessionManager.getSession(sessionToken);
        final String prefix = logMessagePrefixGenerator.createPrefix(session);
        for (int i = 0; i < parameters.length; i++)
        {
            Object parameter = parameters[i];
            if (parameter == null)
            {
                parameters[i] = LogMessagePrefixGenerator.UNDEFINED;
            } else
            {
                parameters[i] = "'" + parameter + "'";
            }
        }
        final String message = String.format(parameterDisplayFormat, parameters);
        final String invocationStatusMessage = getInvocationStatusMessage();
        // We put on purpose 2 spaces between the command and the message derived from the
        // parameters.
        logger.info(prefix
                + String.format(": %s  %s%s", commandName, message, invocationStatusMessage));
    }

    private String getInvocationStatusMessage()
    {
        return invocationSuccessful ? RESULT_SUCCESS : RESULT_FAILURE;
    }

    //
    // ISessionProvider
    //

    public IAuthSession getSession(String sessionToken)
    {
        return null;
    }

    //
    // IGenericServer
    //

    public int getVersion()
    {
        return 0;
    }

    public Session tryToAuthenticate(String user, String password)
    {
        // No logging because already done by the session manager
        return null;
    }

    public void logout(String sessionToken)
    {
        // No logging because already done by the session manager
    }

    public List<GroupPE> listGroups(String sessionToken, DatabaseInstanceIdentifier identifier)
    {
        String command = "list_groups";
        if (identifier == null || identifier.getDatabaseInstanceCode() == null)
        {
            logAccess(sessionToken, command, "");
        } else
        {
            logAccess(sessionToken, command, "DATABASE-INSTANCE(%s)", identifier);
        }
        return null;
    }

    public void registerGroup(String sessionToken, String groupCode, String descriptionOrNull,
            String groupLeaderOrNull)
    {
        logTracking(sessionToken, "register_group", "CODE(%s)", groupCode);
    }

    public List<PersonPE> listPersons(String sessionToken)
    {
        String command = "list_persons";
        logAccess(sessionToken, command, "");

        return null;
    }

    public void registerPerson(String sessionToken, String userID)
    {
        logTracking(sessionToken, "register_person", "CODE(%s)", userID);

    }

}
