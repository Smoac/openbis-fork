/*
 * Copyright 2007 ETH Zuerich, CISD
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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A <code>AbstractController</code> extension which tries to forward the request to the named servlet found in the
 * <i>URL</i>.
 * 
 * @author Christian Ribeaud
 */
public final class UrlServletForwardingController extends AbstractController
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, UrlServletForwardingController.class);

    private final String getServletName(final HttpServletRequest request)
    {
        return (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    }

    //
    // AbstractController
    //

    @Override
    protected final ModelAndView handleRequestInternal(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
    {
        operationLog.info("forward " + request.getRequestURL());
        String servletName = getServletName(request);
        if (servletName.charAt(0) == '/')
        {
            servletName = servletName.substring(1);
        }
        final RequestDispatcher rd = getServletContext().getNamedDispatcher(servletName);
        if (rd == null)
        {
            throw new ServletException(String.format("No servlet with name '%s' defined in web.xml", servletName));
        }
        rd.forward(request, response);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Forwarded to servlet [%s] in '%s'.", servletName, getClass()
                    .getSimpleName()));
        }
        return null;
    }
}
