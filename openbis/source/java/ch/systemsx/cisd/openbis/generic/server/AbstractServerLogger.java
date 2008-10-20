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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * An <i>abstract</i> server logger.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractServerLogger implements IServer
{
    private static final String RESULT_SUCCESS = "";

    private static final String RESULT_FAILURE = " ...FAILED";

    private static final Logger accessLog =
            LogFactory.getLogger(LogCategory.ACCESS, AbstractServerLogger.class);

    private static final Logger trackingLog =
            LogFactory.getLogger(LogCategory.TRACKING, AbstractServerLogger.class);

    protected final ISessionManager<Session> sessionManager;

    protected final boolean invocationSuccessful;

    protected final LogMessagePrefixGenerator logMessagePrefixGenerator;

    public AbstractServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful)
    {
        this.sessionManager = sessionManager;
        this.invocationSuccessful = invocationSuccessful;
        logMessagePrefixGenerator = new LogMessagePrefixGenerator();
    }

    protected final void logAccess(final String sessionToken, final String commandName)
    {
        logAccess(sessionToken, commandName, "");
    }

    protected final void logAccess(final String sessionToken, final String commandName,
            final String parameterDisplayFormat, final Object... parameters)
    {
        logMessage(accessLog, sessionToken, commandName, parameterDisplayFormat, parameters);
    }

    protected final void logTracking(final String sessionToken, final String commandName,
            final String parameterDisplayFormat, final Object... parameters)
    {
        logMessage(trackingLog, sessionToken, commandName, parameterDisplayFormat, parameters);
    }

    private final void logMessage(final Logger logger, final String sessionToken,
            final String commandName, final String parameterDisplayFormat, final Object[] parameters)
    {
        final Session session = sessionManager.getSession(sessionToken);
        final String prefix = logMessagePrefixGenerator.createPrefix(session);
        for (int i = 0; i < parameters.length; i++)
        {
            final Object parameter = parameters[i];
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

    public final IAuthSession getSession(final String sessionToken)
    {
        return null;
    }

    //
    // IServer
    //

    public final int getVersion()
    {
        return 0;
    }

    public final Session tryToAuthenticate(final String user, final String password)
    {
        // No logging because already done by the session manager
        return null;
    }

    public final void logout(final String sessionToken)
    {
        // No logging because already done by the session manager
    }

}