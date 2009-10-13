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
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
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

    private final Logger accessLog;

    private final Logger trackingLog;

    protected final ISessionManager<Session> sessionManager;

    protected final boolean invocationSuccessful;

    protected final long elapsedTime;

    protected final LogMessagePrefixGenerator logMessagePrefixGenerator;

    public AbstractServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful, final long elapsedTime)
    {
        this.sessionManager = sessionManager;
        this.invocationSuccessful = invocationSuccessful;
        this.elapsedTime = elapsedTime;
        logMessagePrefixGenerator = new LogMessagePrefixGenerator();
        accessLog = LogFactory.getLogger(LogCategory.ACCESS, getClass());
        trackingLog = LogFactory.getLogger(LogCategory.TRACKING, getClass());
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
        Session sessionOrNull = null;
        try
        {
            sessionOrNull = sessionManager.getSession(sessionToken);
        } catch (InvalidSessionException e)
        {
            // ignore the situation when session is not available
        }
        final String prefix = logMessagePrefixGenerator.createPrefix(sessionOrNull);
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
        final String elapsedTimeMessage = getElapsedTimeMessage();
        // We put on purpose 2 spaces between the command and the message derived from the
        // parameters.
        logger.info(prefix
                + String.format(": (%s) %s  %s%s", elapsedTimeMessage, commandName, message,
                        invocationStatusMessage));
    }

    private String getInvocationStatusMessage()
    {
        return invocationSuccessful ? RESULT_SUCCESS : RESULT_FAILURE;
    }

    private String getElapsedTimeMessage()
    {
        return elapsedTime + "ms";
    }

    //
    // ISessionProvider
    //

    public final IAuthSession getAuthSession(final String sessionToken) throws UserFailureException
    {
        return null;
    }

    //
    // IServer
    //

    public final int getVersion()
    {
        return IServer.VERSION;
    }

    public final Session tryToAuthenticate(final String user, final String password)
    {
        // No logging because already done by the session manager
        return null;
    }

    public final void logout(final String sessionToken) throws UserFailureException
    {
        // No logging because already done by the session manager
    }

    public void saveDisplaySettings(String sessionToken, DisplaySettings displaySettings)
    {
        logTracking(sessionToken, "save_display_settings", "");
    }

    public void changeUserHomeGroup(String sessionToken, TechId groupIdOrNull)
    {
        String groupId = groupIdOrNull == null ? "null" : groupIdOrNull.toString();
        logTracking(sessionToken, "change_user_home_group (%s)", groupId);
    }

    public List<GridCustomColumn> listGridCustomColumns(String sessionToken, String gridId)
    {
        logAccess(sessionToken, "listGridCustomColumns GRID_ID(%s)", gridId);
        return null;
    }

    public void setBaseIndexURL(String sessionToken, String baseURL)
    {
        logAccess(sessionToken, "set_base_url", "BASE_URL(%s)", baseURL);
    }
}
