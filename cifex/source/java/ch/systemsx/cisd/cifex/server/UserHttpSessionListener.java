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

package ch.systemsx.cisd.cifex.server;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.UserHttpSessionHolder;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * A <code>HttpSessionListener</code> implementation which registers the active user sessions in a
 * <i>Spring</i> bean.
 * 
 * @author Christian Ribeaud
 */
public final class UserHttpSessionListener implements HttpSessionListener
{
    private final static UserHttpSessionHolder getUserHttpSessionHolder(final HttpSession session)
            throws ConfigurationFailureException
    {
        final WebApplicationContext ctx =
                WebApplicationContextUtils.getRequiredWebApplicationContext(session
                        .getServletContext());
        final String beanName = UserHttpSessionHolder.USER_SESSION_HOLDER_BEAN_NAME;
        final UserHttpSessionHolder sessionHolder = (UserHttpSessionHolder) ctx.getBean(beanName);
        if (sessionHolder == null)
        {
            throw ConfigurationFailureException.fromTemplate("No bean '%s' defined.", beanName);
        }
        return sessionHolder;
    }

    private final static IUserActionLog getUserBehaviorLog(final HttpSession session)
            throws ConfigurationFailureException
    {
        final WebApplicationContext ctx =
                WebApplicationContextUtils.getRequiredWebApplicationContext(session
                        .getServletContext());
        final String beanName = IUserActionLog.USER_ACTION_LOG_BEAN_NAME;
        final IUserActionLog userBehaviorLog = (IUserActionLog) ctx.getBean(beanName);
        if (userBehaviorLog == null)
        {
            throw ConfigurationFailureException.fromTemplate("No bean '%s' defined.", beanName);
        }
        return userBehaviorLog;
    }

    //
    // HttpSessionListener
    //

    public final void sessionCreated(final HttpSessionEvent sessionEvent)
    {
        final HttpSession session = sessionEvent.getSession();
        if (session == null)
        {
            return;
        }
        getUserHttpSessionHolder(session).addUserSession(session);
    }

    public final void sessionDestroyed(final HttpSessionEvent sessionEvent)
    {
        final HttpSession session = sessionEvent.getSession();
        if (session == null)
        {
            return;
        }
        getUserHttpSessionHolder(session).removeUserSession(session);
        // Only log http sessions that do not belong to an rpc session.
        if (AbstractCIFEXService.tryGetRPCSession(session) == null)
        {
            getUserBehaviorLog(session).logLogout(session);
        }
    }
}
