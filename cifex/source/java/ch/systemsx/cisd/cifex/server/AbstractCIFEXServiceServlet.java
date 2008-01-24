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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * An abstract <code>HttpServlet</code> extension.
 * <p>
 * This internally prepares an <code>ICIFEXService</code> instance for subclasses and has already the necessary
 * <code>Logger</code>s.
 * </p>
 * 
 * @author Christian Ribeaud
 */
abstract class AbstractCIFEXServiceServlet extends HttpServlet
{
    protected final Logger operationLog;

    protected final Logger notificationLog;

    private static final String DOMAIN_MODEL_BEAN_NAME = "domain-model";

    protected IDomainModel domainModel;

    public AbstractCIFEXServiceServlet()
    {
        operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());
        notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, getClass());
    }

    private final void initDomainModel()
    {
        final BeanFactory context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        domainModel = (IDomainModel) context.getBean(DOMAIN_MODEL_BEAN_NAME);
    }

    protected final UserDTO getUserDTO(final HttpServletRequest request) throws InvalidSessionException
    {
        final HttpSession session = request.getSession(false);
        if (session == null)
        {
            throw new InvalidSessionException("You are not logged in. Please log in.");
        }
        return (UserDTO) session.getAttribute(CIFEXServiceImpl.SESSION_NAME);
    }

    //
    // HttpServlet
    //

    @Override
    public final void init(final ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
        LogInitializer.init();
        try
        {
            initDomainModel();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("'%s' successfully initialized.", getClass().getName()));
            }
        } catch (final Exception ex)
        {
            notificationLog
                    .fatal("Failure during '" + servletConfig.getServletName() + "' servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }
}
