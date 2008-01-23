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

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.UserFailureException;
import ch.systemsx.cisd.cifex.client.dto.User;
import ch.systemsx.cisd.cifex.server.business.DomainModel;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.logging.IRemoteHostProvider;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LoggingContextHandler;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * The real <code>ICifexService</code> implementation.
 * 
 * @author Christian Ribeaud
 */
public final class CIFEXServiceImpl implements ICIFEXService
{
    /** The attribute name under which the session could be found. */
    private static final String SESSION_NAME = "cifex-session";
    
    private static final Logger authenticationLog = LogFactory.getLogger(LogCategory.AUTH, CIFEXServiceImpl.class);
    
    private final DomainModel domainModel;

    private final IRequestContextProvider requestContextProvider;
    
    private final LoggingContextHandler loggingContextHandler;

    private final IAuthenticationService externalAuthenticationService;

    private int sessionExpirationPeriod;

    public CIFEXServiceImpl(final DomainModel domainModel, final IRequestContextProvider requestContextProvider,
            final IAuthenticationService externalAuthenticationService)
    {
        this.domainModel = domainModel;
        this.requestContextProvider = requestContextProvider;
        this.externalAuthenticationService = externalAuthenticationService;
        loggingContextHandler = new LoggingContextHandler(new IRemoteHostProvider()
            {
                public String getRemoteHost()
                {
                    return requestContextProvider.getHttpServletRequest().getRemoteHost();
                }
            });
        if (hasExternalAuthenticationService())
        {
            this.externalAuthenticationService.check();
        }
    }
    
    public void setSessionExpirationPeriodInMinutes(int sessionExpirationPeriodInMinutes)
    {
        sessionExpirationPeriod = sessionExpirationPeriodInMinutes * 60;
    }

    private boolean hasExternalAuthenticationService()
    {
        return externalAuthenticationService != null
                && externalAuthenticationService instanceof NullAuthenticationService == false;
    }
    
    private String createSession(UserDTO user)
    {
        final HttpSession httpSession = getSession(true);
        httpSession.setMaxInactiveInterval(sessionExpirationPeriod);
        httpSession.setAttribute(SESSION_NAME, user);
        return httpSession.getId();
    }

    private HttpSession getSession(boolean create)
    {
        return requestContextProvider.getHttpServletRequest().getSession(create);
    }
    
    private UserDTO getCurrentUser()
    {
        HttpSession session = getSession(false);
        if (session == null)
        {
            return null;
        }
        return (UserDTO) session.getAttribute(SESSION_NAME);
    }

    //
    // ICifexService
    //

    public final boolean isAuthenticated()
    {
        UserDTO currentUser = getCurrentUser();
        return currentUser != null;
    }

    public final User tryToLogin(final String user, final String password) throws UserFailureException
    {
        authenticationLog.info("Try to login user '" + user + "'.");
        IUserManager userManager = domainModel.getUserManager();
        if (hasExternalAuthenticationService())
        {
            String applicationToken = externalAuthenticationService.authenticateApplication();
            if (applicationToken == null)
            {
                authenticationLog.error("User '" + user + "' couldn't be authenticated because authentication of "
                        + "the application at the external authentication service failed.");
                throw new UserFailureException("Authentication of the server at "
                        + "the external authentication service failed.");
            }
            boolean authenticated = externalAuthenticationService.authenticateUser(applicationToken, user, password);
            if (authenticated == false)
            {
                return null;
            }
            Principal principal = externalAuthenticationService.getPrincipal(applicationToken, user);
            if (principal == null)
            {
                authenticationLog.error("Unknown principal for successfully authenticated user '" + user + "'.");
                throw new UserFailureException("Authentication was successful but user information "
                        + "couldn't be retrieved");
            }
            String email = principal.getEmail();
            UserDTO userDTO = userManager.tryToFindUser(email);
            if (userDTO == null)
            {
                userDTO = new UserDTO();
                userDTO.setUserName(user);
                userDTO.setEmail(email);
                userDTO.setEncryptedPassword(StringUtilities.encrypt(password));
                userDTO.setExternallyAuthenticated(true);
                userDTO.setAdmin(false);
                userDTO.setPermanent(true);
                userManager.createUser(userDTO);
            }
            return finishLogin(userDTO);
        } else
        {
            String encryptedPassword = StringUtilities.encrypt(password);
            UserDTO userDTO = userManager.tryToFindUser(user);
            if (userDTO == null || encryptedPassword.equals(userDTO.getEncryptedPassword()) == false)
            {
                return null;
            }
            return finishLogin(userDTO);
        }
    }

    private User finishLogin(UserDTO userDTO)
    {
        authenticationLog.info("Sucsessfully login of user " + userDTO);
        createSession(userDTO);
        return BeanUtils.createBean(User.class, userDTO);
    }

    public final void logout()
    {
        final HttpSession httpSession = getSession(false);
        if (httpSession != null)
        {
            UserDTO user = (UserDTO) httpSession.getAttribute(SESSION_NAME);
            httpSession.removeAttribute(SESSION_NAME);
            httpSession.invalidate();
            authenticationLog.info("Logout of user " + user);
        }
    }
}
