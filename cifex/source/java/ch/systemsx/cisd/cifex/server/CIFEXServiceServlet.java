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

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.systemsx.cisd.cifex.client.EnvironmentFailureException;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.UserFailureException;
import ch.systemsx.cisd.cifex.client.dto.File;
import ch.systemsx.cisd.cifex.client.dto.User;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * The server <code>ICifexService</code> implementation.
 * <p>
 * Note that this is a servlet. It is usually good practice to let the constructor body empty and to put initialization
 * stuff in {@link #init(ServletConfig)}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class CIFEXServiceServlet extends GWTSpringController implements ICIFEXService
{
    private static final String CIFEX_SERVICE_BEAN_NAME = "cifex-service";

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, CIFEXServiceServlet.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, CIFEXServiceServlet.class);

    private static final long serialVersionUID = 1L;

    /** The encapsulated {@link ICIFEXService} which is doing the real work. */
    private ICIFEXService cifexServiceDelegate;

    private final void initService(final ServletContext servletContext)
    {
        final BeanFactory context = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        cifexServiceDelegate = (ICIFEXService) context.getBean(CIFEX_SERVICE_BEAN_NAME);
    }

    //
    // ICifexService
    //

    public final User getCurrentUser() throws InvalidSessionException
    {
        return cifexServiceDelegate.getCurrentUser();
    }

    public final User tryToLogin(final String user, final String password, final boolean requestAdmin)
            throws UserFailureException
    {
        return cifexServiceDelegate.tryToLogin(user, password, requestAdmin);
    }

    public final void logout()
    {
        cifexServiceDelegate.logout();
    }

    public File[] listDownloadFiles() throws UserFailureException
    {
        return cifexServiceDelegate.listDownloadFiles();
    }

    //
    // RemoteServiceServlet
    //

    @Override
    public final void init(final ServletConfig config) throws ServletException
    {
        super.init(config);
        try
        {
            initService(config.getServletContext());
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("'%s' successfully initialized.", getClass().getName()));
            }
        } catch (final Exception ex)
        {
            notificationLog.fatal("Failure during CIFEX service servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }

    public List listUsers()
    {
        return cifexServiceDelegate.listUsers();
    }

    public void tryToCreateUser(User user, String password) throws EnvironmentFailureException
    {
        cifexServiceDelegate.tryToCreateUser(user, password);

    }
}
