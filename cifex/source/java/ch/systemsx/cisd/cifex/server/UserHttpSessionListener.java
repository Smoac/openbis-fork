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

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.systemsx.cisd.cifex.server.business.UserHttpSessionHolder;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A <code>HttpSessionListener</code> implementation which registers the active user sessions in a <i>Spring</i>
 * bean.
 * 
 * @author Christian Ribeaud
 */
public final class UserHttpSessionListener implements HttpSessionListener
{
    private static final Logger authLog = LogFactory.getLogger(LogCategory.AUTH, UserHttpSessionListener.class);

    private final static UserHttpSessionHolder getUserHttpSessionHolder(final HttpSession session)
    {
        final WebApplicationContext ctx =
                WebApplicationContextUtils.getRequiredWebApplicationContext(session.getServletContext());
        final String beanName = UserHttpSessionHolder.USER_SESSION_HOLDER_BEAN_NAME;
        final UserHttpSessionHolder sessionHolder = (UserHttpSessionHolder) ctx.getBean(beanName);
        assert sessionHolder != null : String.format("No bean '%s' defined.", beanName);
        return sessionHolder;
    }

    //
    // HttpSessionListener
    //

    public final void sessionCreated(final HttpSessionEvent sessionEvent)
    {
        final HttpSession session = sessionEvent.getSession();
        final UserHttpSessionHolder sessionHolder = getUserHttpSessionHolder(session);
        sessionHolder.addUserSession(session);
    }

    public final void sessionDestroyed(final HttpSessionEvent sessionEvent)
    {
        final HttpSession session = sessionEvent.getSession();
        final UserHttpSessionHolder sessionHolder = getUserHttpSessionHolder(session);
        if (session != null && authLog.isInfoEnabled())
        {
            final UserDTO user = (UserDTO) session.getAttribute(CIFEXServiceImpl.SESSION_NAME);
            String userCode = (user != null) ? user.getUserCode() : "";
            authLog.info("Close session '" + session.getId() + "' of user '" + userCode + "'.");
        }
        sessionHolder.removeUserSession(session);
    }
}
