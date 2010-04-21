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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implements {@link Filter} and sets the cache to store data for resources that don't have
 * '.nocache.' in their name. Static resources such as images should stay in cache for a long time.
 * 
 * @author Piotr Buczek
 */
public class CacheFilter implements Filter
{
    private static final int HOUR_IN_SECONDS = 60 * 60;

    private static final int DAY_IN_SECONDS = 24 * 60 * 60;

    private static final int SPRINT_IN_SECONDS = 14 * DAY_IN_SECONDS;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException
    {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String requestURI = httpRequest.getRequestURI();
        if (requestURI.contains(".nocache.") == false)
        {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.addHeader("Cache-Control", "max-age="
                    + (isPictureOrCache(requestURI) ? SPRINT_IN_SECONDS : HOUR_IN_SECONDS));
        }
        filterChain.doFilter(request, response);

    }

    private boolean isPictureOrCache(String requestURI)
    {
        return requestURI.contains(".cache.") || requestURI.endsWith(".gif")
                || requestURI.endsWith(".png");
    }

    public void destroy()
    {
    }

    public void init(FilterConfig arg0) throws ServletException
    {
    }

}
