/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.dss.generic.server.oaipmh;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author pkupczyk
 */
public class OaipmhServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    private static final String HANDLER_PARAMETER_NAME = "handler";

    private static final String HANDLER_PARAMETER_PREFIX = HANDLER_PARAMETER_NAME + ".";

    private IOaipmhHandler handler;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        this.handler = initHandler(config);
    }

    private IOaipmhHandler initHandler(ServletConfig config) throws ServletException
    {
        String className = config.getInitParameter(HANDLER_PARAMETER_NAME);

        if (className == null || className.trim().length() == 0)
        {
            throw new ServletException("Handler class is null or empty");
        } else
        {
            try
            {
                Class<?> clazz = Class.forName(className);

                if (false == IOaipmhHandler.class.isAssignableFrom(clazz))
                {
                    throw new ServletException("Handler class should implement: '" + IOaipmhHandler.class.getName() + "' interface");
                } else
                {
                    IOaipmhHandler instance = (IOaipmhHandler) clazz.newInstance();

                    Properties properties = new Properties();
                    Enumeration<String> parameterNames = config.getInitParameterNames();

                    while (parameterNames.hasMoreElements())
                    {
                        String parameterName = parameterNames.nextElement();
                        if (parameterName.startsWith(HANDLER_PARAMETER_PREFIX))
                        {
                            properties
                                    .setProperty(parameterName.substring(HANDLER_PARAMETER_PREFIX.length()), config.getInitParameter(parameterName));
                        }
                    }

                    instance.init(properties);
                    return instance;
                }

            } catch (Exception ex)
            {
                throw new ServletException("Cannot create an instance of handler class: '" + className + "'", ex);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        handler.handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        handler.handle(req, resp);
    }

}
