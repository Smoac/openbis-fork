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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import static ch.systemsx.cisd.common.spring.ExposablePropertyPaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.GWTSpringController;
import ch.systemsx.cisd.common.spring.ExposablePropertyPaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * @author Franz-Josef Elmer
 */
public class GenericClientServiceServlet extends GWTSpringController implements
        IGenericClientService
{
    private static final long serialVersionUID = 1L;

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, GenericClientServiceServlet.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, GenericClientServiceServlet.class);

    private IGenericClientService service;

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
            notificationLog.fatal("FailureExpectation during servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }

    private final void initService(final ServletContext servletContext)
    {
        final BeanFactory context =
                WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        service =
                (IGenericClientService) context.getBean("generic-service",
                        IGenericClientService.class);
        if (service instanceof GenericClientService)
        {
            GenericClientService genericService = (GenericClientService) service;
            final ExposablePropertyPaceholderConfigurer configurer =
                    (ExposablePropertyPaceholderConfigurer) context
                            .getBean(PROPERTY_CONFIGURER_BEAN_NAME);

            genericService.setConfigParameters(new GenericConfigParameters(configurer
                    .getResolvedProps()));
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Configuration parameters successfully set.");
            }
        }
    }

    public ApplicationInfo getApplicationInfo()
    {
        return service.getApplicationInfo();
    }

    public SessionContext tryToGetCurrentSessionContext()
    {
        return service.tryToGetCurrentSessionContext();
    }

    public SessionContext tryToLogin(String userID, String password)
    {
        return service.tryToLogin(userID, password);
    }

    public void logout()
    {
        service.logout();
    }

    public List<Group> listGroups(String databaseInstanceCode)
    {
        return service.listGroups(databaseInstanceCode);
    }

    public void registerGroup(String groupCode, String descriptionOrNull, String groupLeaderOrNull)
    {
        service.registerGroup(groupCode, descriptionOrNull, groupLeaderOrNull);
    }

    public List<Person> listPersons() throws UserFailureException
    {
        return service.listPersons();
    }

    public void registerPerson(String code)
    {
        service.registerPerson(code);

    }

    public List<RoleAssignment> listRoles() throws UserFailureException
    {
        return service.listRoles();
    }

    public void registerGroupRole(String roleSetCode, String group, String person)
            throws UserFailureException
    {
        service.registerGroupRole(roleSetCode, group, person);
    }

    public void deleteGroupRole(String roleSetCode, String group, String person)
            throws UserFailureException
    {
        service.deleteGroupRole(roleSetCode, group, person);

    }

    public void registerInstanceRole(String roleSetCode, String person) throws UserFailureException
    {
        service.registerInstanceRole(roleSetCode, person);
    }

    public void deleteInstanceRole(String roleSetCode, String person) throws UserFailureException
    {
        service.deleteInstanceRole(roleSetCode, person);

    }

}
