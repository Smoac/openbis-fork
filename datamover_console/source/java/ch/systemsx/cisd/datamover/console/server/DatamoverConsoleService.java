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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.datamover.console.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.datamover.console.client.EnvironmentFailureException;
import ch.systemsx.cisd.datamover.console.client.IDatamoverConsoleService;
import ch.systemsx.cisd.datamover.console.client.InvalidSessionException;
import ch.systemsx.cisd.datamover.console.client.dto.ApplicationInfo;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverInfo;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverStatus;
import ch.systemsx.cisd.datamover.console.client.dto.User;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DatamoverConsoleService implements IDatamoverConsoleService
{
    static final String SESSION_USER = "user";

    /**
     * The Crowd property for the display name.
     */
    private static final String DISPLAY_NAME_PROPERTY = "displayName";
    
    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, DatamoverConsoleService.class);
    
    private final IAuthenticationService authenticationService;

    /** Session timeout in seconds. */
    private int sessionExpirationPeriod;

    private final IConsoleActionLog actionLog;

    private final IRequestContextProvider requestContextProvider;

    private final IDatamoverConsoleFactory factory;
    
    private Map<String, IDatamoverConsole> consoles;

    private Map<String, String> targets;

    private ApplicationInfo applicationInfo;
    
    public DatamoverConsoleService(final IAuthenticationService authenticationService,
            IRequestContextProvider requestContextProvider,
            IDatamoverConsoleFactory factory, IConsoleActionLog actionLog)
    {
        this.authenticationService = authenticationService;
        this.requestContextProvider = requestContextProvider;
        this.factory = factory;
        this.actionLog = actionLog;
    }
    
    public final void setSessionExpirationPeriodInMinutes(final int sessionExpirationPeriodInMinutes)
    {
        sessionExpirationPeriod = sessionExpirationPeriodInMinutes * 60;
    }
    
    public final void setConfigParameters(ConfigParameters configParameters)
    {
        targets = configParameters.getTargets();
        Map<String, String> workingDirectories = configParameters.getDatamoversWorkingDirectories();
        consoles = new LinkedHashMap<String, IDatamoverConsole>();
        for (Map.Entry<String, String> workingDirectoryEntry : workingDirectories.entrySet())
        {
            String name = workingDirectoryEntry.getKey();
            IDatamoverConsole console = factory.create(name, workingDirectoryEntry.getValue());
            console.check();
            consoles.put(name, console);
        }
        applicationInfo = new ApplicationInfo();
        applicationInfo.setVersion(BuildAndEnvironmentInfo.INSTANCE.getFullVersion());
        applicationInfo.setRefreshTimeInterval(configParameters.getRefreshTimeInterval());
    }

    public ApplicationInfo getApplicationInfo()
    {
        return applicationInfo;
    }

    public User tryToGetCurrentUser()
    {
        HttpSession session = getSession();
        if (session == null)
        {
            return null;
        }
        return (User) session.getAttribute(SESSION_USER);
    }

    public User tryToLogin(String user, String password) 
    {
        final String applicationToken = authenticationService.authenticateApplication();
        if (applicationToken == null)
        {
            actionLog.logFailedLoginAttempt(user);
            final String msg =
                    "User '" + user + "' failed to authenticate: application not authenticated.";
            operationLog.error(msg);
            throw new EnvironmentFailureException(msg);
        }
        final boolean authenticated =
                authenticationService.authenticateUser(applicationToken, user, password);
        if (authenticated == false)
        {
            actionLog.logFailedLoginAttempt(user);
            return null;
        }
        final Principal principal;
        try
        {
            principal = authenticationService.getPrincipal(applicationToken, user);
        } catch (final IllegalArgumentException ex)
        {
            operationLog.error(ex.getMessage());
            throw new EnvironmentFailureException(ex.getMessage());
        }
        User userBean = new User();
        userBean.setUserCode(principal.getUserId());
        if (principal.getProperty(DISPLAY_NAME_PROPERTY) != null)
        {
            userBean.setUserFullName(principal.getProperty(DISPLAY_NAME_PROPERTY).trim());
        } else
        {
            userBean.setUserFullName(principal.getFirstName() + " " + principal.getLastName().trim());
        }
        userBean.setEmail(principal.getEmail());
        HttpSession session = requestContextProvider.getHttpServletRequest().getSession(true);
        session.setMaxInactiveInterval(sessionExpirationPeriod);
        session.setAttribute(SESSION_USER, userBean);
        actionLog.logSuccessfulLogin();
        return userBean;
    }
    
    public void logout()
    {
        HttpSession session = getSession();
        if (session != null)
        {
            session.invalidate();
        }
    }

    public List<DatamoverInfo> listDatamoverInfos()
    {
        checkAuthenticated();
        List<DatamoverInfo> list = new ArrayList<DatamoverInfo>();
        for (Map.Entry<String, IDatamoverConsole> entry : consoles.entrySet())
        {
            String name = entry.getKey();
            IDatamoverConsole console = entry.getValue();
            DatamoverInfo datamoverInfo = new DatamoverInfo();
            datamoverInfo.setName(name);
            String target = console.tryObtainTarget();
            if (target != null)
            {
                datamoverInfo.setTargetLocation(target);
            }
            datamoverInfo.setStatus(console.obtainStatus());
            if (DatamoverStatus.ERROR.equals(datamoverInfo.getStatus()))
            {
                datamoverInfo.setErrorMessage(console.tryObtainErrorMessage());
            }
            list.add(datamoverInfo);
        }
        return list;
    }

    public Map<String, String> getTargets()
    {
        checkAuthenticated();
        return targets;
    }

    public void startDatamover(String datamoverName, String targetLocation)
    {
        checkAuthenticated();
        IDatamoverConsole datamoverConsole = consoles.get(datamoverName);
        if (datamoverConsole != null)
        {
            datamoverConsole.start(targetLocation);
            actionLog.logStartDatamover(datamoverName, targetLocation);
        }
    }

    public void shutdownDatamover(String datamoverName)
    {
        checkAuthenticated();
        IDatamoverConsole datamoverConsole = consoles.get(datamoverName);
        if (datamoverConsole != null)
        {
            datamoverConsole.shutdown();
            actionLog.logShutdownDatamover(datamoverName);
        }
    }
    
    private void checkAuthenticated()
    {
        HttpSession session = getSession();
        if (session == null)
        {
            throw new InvalidSessionException(
                    "You are not logged in or your session has expired. Please log in.");
        }
    }

    private HttpSession getSession()
    {
        return requestContextProvider.getHttpServletRequest().getSession(false);
    }

}
