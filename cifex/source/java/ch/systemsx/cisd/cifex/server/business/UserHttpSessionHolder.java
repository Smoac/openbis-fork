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

package ch.systemsx.cisd.cifex.server.business;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.cifex.server.CIFEXServiceImpl;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A user session manager which holds the active sessions and invalidate them on demand.
 * <p>
 * This class is synchronized as it could be accessed by more than one thread.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class UserHttpSessionHolder
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, UserHttpSessionHolder.class);

    public final static String USER_SESSION_HOLDER_BEAN_NAME = "user-session-holder";

    private final List<HttpSession> activeSessions;

    /** A flag to avoid <code>ConcurrentModificationException</code> exception when session gets invalidated here. */
    private boolean isInvalidating;

    public UserHttpSessionHolder()
    {
        activeSessions = new ArrayList<HttpSession>();
    }

    public final synchronized void addUserSession(final HttpSession session)
    {
        activeSessions.add(session);
    }

    public final synchronized void invalidateSessionWithUser(final UserDTO userDTO)
    {
        isInvalidating = true;
        for (final HttpSession httpSession : activeSessions)
        {
            final UserDTO user = (UserDTO) httpSession.getAttribute(CIFEXServiceImpl.SESSION_NAME);
            if (user != null && user.getID().longValue() == userDTO.getID().longValue())
            {
                // This unbinds all the attributes as well. So do not do clever cleaning here.
                httpSession.invalidate();
                if (operationLog.isInfoEnabled())
                {
                    final String fullName = user.getUserFullName();
                    String description = StringUtils.isBlank(fullName) ? user.getUserCode() : fullName;
                    description += " <" + user.getEmail() + ">";
                    operationLog.info("Currently logged in user [" + description + "] has been logged out.");
                }
            }
        }
        isInvalidating = false;
    }

    public final synchronized void removeUserSession(final HttpSession session)
    {
        if (isInvalidating == false)
        {
            activeSessions.remove(session);
        }
    }
}
