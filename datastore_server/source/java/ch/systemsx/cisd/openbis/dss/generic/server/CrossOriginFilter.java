/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * Implements CORS (Cross Origin Resource Sharing) to allow a web page served from the openBIS AS to
 * access resources on the DSS.
 * <p>
 * For more details on CORS see
 * http://www.nczonline.net/blog/2010/05/25/cross-domain-ajax-with-cross-origin-resource-sharing/.
 * 
 * @author Kaloyan Enimanev
 */
public class CrossOriginFilter implements Filter
{
    private static final String ORIGIN_HEADER = "Origin";

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";

    private String openBisServerUrl;

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain)
            throws IOException, ServletException
    {
        String originHeader = ((HttpServletRequest) request).getHeader(ORIGIN_HEADER);
        if (originHeader != null && originHeader.startsWith(openBisServerUrl))
        {
            ((HttpServletResponse) response).setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER,
                    originHeader);
        }
        filterChain.doFilter(request, response);
    }

    public void destroy()
    {
    }


    public void init(FilterConfig arg0) throws ServletException
    {
        openBisServerUrl = ServiceProvider.getConfigProvider().getOpenBisServerUrl();
    }

}
