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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;

/**
 * A <i>CIFEX</i> specific <code>DefaultHandler</code> extension.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultHandler extends org.mortbay.jetty.handler.DefaultHandler
{

    //
    // DefaultHandler
    //

    @Override
    public final void handle(final String target, final HttpServletRequest request, final HttpServletResponse response,
            final int dispatch) throws IOException, ServletException
    {
        final Request baseRequest =
                request instanceof Request ? (Request) request : HttpConnection.getCurrentConnection().getRequest();

        if (response.isCommitted() || baseRequest.isHandled())
        {
            return;
        }
        baseRequest.setHandled(true);

        final String method = request.getMethod();

        if (method.equals(HttpMethods.GET) == false || request.getRequestURI().equals("/") == false)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final Server server = getServer();
        final Handler[] handlers = server == null ? null : server.getChildHandlersByClass(ContextHandler.class);

        for (int i = 0; handlers != null && i < handlers.length; i++)
        {
            final ContextHandler context = (ContextHandler) handlers[i];
            if (context.isRunning())
            {
                response.sendRedirect("/cifex");
            }
        }
        return;
    }
}
