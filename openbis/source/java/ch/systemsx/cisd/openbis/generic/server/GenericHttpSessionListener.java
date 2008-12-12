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

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * Implements {@link HttpSessionListener} and allows to perform chosen actions when session is being
 * created or destroyed.
 * <p>
 * This ensures that the corresponding {@link Session} gets removed when a {@link HttpSession} gets
 * destroyed.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
public final class GenericHttpSessionListener implements HttpSessionListener
{

    //
    // HttpSessionListener
    //

    public final void sessionCreated(final HttpSessionEvent sessionEvent)
    {
    }

    public final void sessionDestroyed(final HttpSessionEvent sessionEvent)
    {
        final HttpSession httpSession = sessionEvent.getSession();
        final Session session =
                (Session) httpSession.getAttribute(SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY);
        if (session != null)
        {
            final IServer server =
                    (IServer) httpSession
                            .getAttribute(SessionConstants.OPENBIS_SERVER_ATTRIBUTE_KEY);
            if (server != null)
            {
                server.logout(session.getSessionToken());
            }
        }
    }
}
