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

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.server.business.IBusinessContext;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * Some utilities around <i>Http</i>.
 * 
 * @author Franz-Josef Elmer
 */
public final class HttpUtils
{
    /**
     * Extracts the basic URL from the request URL.
     * <p>
     * The basic URL is composed of the scheme (<i>http</i>,...), the host and the port.
     * Additionally it contains the context path it not empty.
     * </p>
     */
    // requestURI: /cifex/file-upload
    // servletPath: /cifex
    // pathInfo: /file-upload
    // requestURL: http://localhost:8888/cifex/file-upload
    public final static String getBasicURL(final HttpServletRequest request)
    {
        final String scheme = request.getScheme();
        final String serverName = request.getServerName();
        final int port = request.getServerPort();
        final String contextPath = StringUtils.defaultString(request.getContextPath());
        try
        {
            return new URL(scheme, serverName, port, contextPath).toString();
        } catch (final MalformedURLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final static String getURLForEmail(final HttpServletRequest request,
            IBusinessContext businessContext)
    {
        final String overrideURL = businessContext.getOverrideURL();
        if (StringUtils.isBlank(overrideURL))
        {
            return HttpUtils.getBasicURL(request);
        } else
        {
            return overrideURL;
        }
    }

    private HttpUtils()
    {
        // Can not be instantiated.
    }

}
