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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.datamover.console.client.EnvironmentFailureException;
import ch.systemsx.cisd.datamover.console.client.IDatamoverConsoleService;
import ch.systemsx.cisd.datamover.console.client.UserFailureException;
import ch.systemsx.cisd.datamover.console.client.dto.User;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DatamoverConsoleService implements IDatamoverConsoleService
{
    /**
     * The Crowd property for the display name.
     */
    private static final String DISPLAY_NAME_PROPERTY = "displayName";
    
    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, DatamoverConsoleService.class);

    private final IAuthenticationService authenticationService;

    /** Session timeout in seconds. */
    private int sessionExpirationPeriod;

    private final IActionLog actionLog;
    
    public DatamoverConsoleService(final IAuthenticationService authenticationService, IActionLog actionLog)
    {
        this.authenticationService = authenticationService;
        this.actionLog = actionLog;
    }
    
    public final void setSessionExpirationPeriodInMinutes(final int sessionExpirationPeriodInMinutes)
    {
        sessionExpirationPeriod = sessionExpirationPeriodInMinutes * 60;
    }

    public User tryLogin(String user, String password) throws UserFailureException,
            EnvironmentFailureException
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
            userBean.setUserFullName(principal.getProperty(DISPLAY_NAME_PROPERTY));
        } else
        {
            userBean.setUserFullName(principal.getFirstName() + " " + principal.getLastName());
        }
        userBean.setEmail(principal.getEmail());
        actionLog.logSuccessfulLogin();
        return userBean;
    }
    
    public void logout()
    {
        // TODO Auto-generated method stub
        
    }

}
