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

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.cifex.rpc.server.SessionManager;
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
public final class UserHttpSessionHolder implements IUserSessionInvalidator
{
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, UserHttpSessionHolder.class);

    public final static String USER_SESSION_HOLDER_BEAN_NAME = "user-session-holder";

    private final Map<HttpSession, Object> activeSessions;
    
    private final SessionManager rpcSessionManager;

    /**
     * A flag to avoid <code>ConcurrentModificationException</code> exception when session gets
     * invalidated here.
     */
    private boolean isInvalidating;

    public UserHttpSessionHolder(SessionManager rpcSessionManager)
    {
        this.rpcSessionManager = rpcSessionManager;
        activeSessions = new WeakHashMap<HttpSession, Object>();
    }

    public final synchronized void addUserSession(final HttpSession session)
    {
        assert session != null : "Unspecified HTTP session";
        activeSessions.put(session, ObjectUtils.NULL);
    }

    public final synchronized void removeUserSession(final HttpSession session)
    {
        assert session != null : "Unspecified HTTP session";
        if (isInvalidating == false)
        {
            activeSessions.remove(session);
        }
    }

    //
    // IUserSessionInvalidator
    //

    public final synchronized void invalidateSessionWithUser(final UserDTO userDTO)
    {
        assert userDTO != null : "Unspecified user";
        isInvalidating = true;
        for (final Iterator<HttpSession> iterator = activeSessions.keySet().iterator(); iterator
                .hasNext(); /**/)
        {
            final HttpSession httpSession = iterator.next();
            try
            {
                final UserDTO user =
                        (UserDTO) httpSession.getAttribute(CIFEXServiceImpl.SESSION_ATTRIBUTE_USER_NAME);
                if (user != null && user.getID().equals(userDTO.getID()))
                {
                    // This unbinds all the attributes as well. So do not do clever cleaning here.
                    httpSession.invalidate();
                    // As removeUserSession will not remove it from the list, we have to do it
                    // manually here.
                    iterator.remove();
                    if (operationLog.isInfoEnabled())
                    {
                        final String fullName = user.getUserFullName();
                        String description =
                                StringUtils.isBlank(fullName) ? user.getUserCode() : fullName;
                        description += " <" + user.getEmail() + ">";
                        operationLog.info("Currently logged in user [" + description
                                + "] has been logged out.");
                    }
                }
            } catch (final IllegalStateException ex)
            {
                // For some reason, the current httpSession has already been invalidated but still
                // present in the list. So remove it and ignore this exception.
                iterator.remove();
            }
        }
        rpcSessionManager.removeSessionsForUser(userDTO.getUserCode());
        isInvalidating = false;
    }

}