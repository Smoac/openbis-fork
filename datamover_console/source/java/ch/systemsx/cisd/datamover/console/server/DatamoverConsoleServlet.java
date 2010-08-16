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

package ch.systemsx.cisd.datamover.console.server;

import static ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.GWTSpringController;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.datamover.console.client.EnvironmentFailureException;
import ch.systemsx.cisd.datamover.console.client.IDatamoverConsoleService;
import ch.systemsx.cisd.datamover.console.client.UserFailureException;
import ch.systemsx.cisd.datamover.console.client.dto.ApplicationInfo;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverInfo;
import ch.systemsx.cisd.datamover.console.client.dto.User;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DatamoverConsoleServlet extends GWTSpringController implements IDatamoverConsoleService
{
    private static final long serialVersionUID = 1L;

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DatamoverConsoleServlet.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatamoverConsoleServlet.class);

    private IDatamoverConsoleService service;
    
    @Override
    public final void init(final ServletConfig config) throws ServletException
    {
        super.init(config);
        try
        {
            initService(config.getServletContext());
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("'%s' successfully initialized.", getClass()
                        .getName()));
            }
        } catch (final Exception ex)
        {
            notificationLog.fatal("Failure during servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }

    private final void initService(final ServletContext servletContext)
    {
        final BeanFactory context =
                WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        service =
                (IDatamoverConsoleService) context.getBean("service",
                        IDatamoverConsoleService.class);
        if (service instanceof DatamoverConsoleService)
        {
            DatamoverConsoleService datamoverConsoleService = (DatamoverConsoleService) service;
            final ExposablePropertyPlaceholderConfigurer configurer =
                    (ExposablePropertyPlaceholderConfigurer) context
                            .getBean(PROPERTY_CONFIGURER_BEAN_NAME);

            ConfigParameters configParameters = new ConfigParameters(configurer.getResolvedProps());
            datamoverConsoleService.setConfigParameters(configParameters);
        }
    }
    
    public ApplicationInfo getApplicationInfo()
    {
        return service.getApplicationInfo();
    }

    public User tryToGetCurrentUser()
    {
        return service.tryToGetCurrentUser();
    }

    public User tryToLogin(String user, String password) throws UserFailureException,
            EnvironmentFailureException
    {
        return service.tryToLogin(user, password);
    }

    public void logout()
    {
        service.logout();
    }

    public List<DatamoverInfo> listDatamoverInfos()
    {
        return service.listDatamoverInfos();
    }

    public Map<String, String> getTargets()
    {
        return service.getTargets();
    }

    public void startDatamover(String datamoverName, String targetLocation)
    {
        service.startDatamover(datamoverName, targetLocation);
    }

    public void shutdownDatamover(String datamoverName)
    {
        service.shutdownDatamover(datamoverName);
    }
    
}
