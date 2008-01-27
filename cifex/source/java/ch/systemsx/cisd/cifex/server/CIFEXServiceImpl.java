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

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.UserFailureException;
import ch.systemsx.cisd.cifex.client.dto.User;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
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
    static final String SESSION_NAME = "cifex-user";

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd hh:mm:ss";

    private static final Logger authenticationLog = LogFactory.getLogger(LogCategory.AUTH, CIFEXServiceImpl.class);

    private final IDomainModel domainModel;

    private final IRequestContextProvider requestContextProvider;

    private final LoggingContextHandler loggingContextHandler;

    private final IAuthenticationService externalAuthenticationService;

    /** Session timeout in seconds. */
    private int sessionExpirationPeriod;

    public CIFEXServiceImpl(final IDomainModel domainModel, final IRequestContextProvider requestContextProvider,
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

    public void setSessionExpirationPeriodInMinutes(final int sessionExpirationPeriodInMinutes)
    {
        sessionExpirationPeriod = sessionExpirationPeriodInMinutes * 60;
    }

    private boolean hasExternalAuthenticationService()
    {
        return externalAuthenticationService != null
                && externalAuthenticationService instanceof NullAuthenticationService == false;
    }

    private String createSession(final UserDTO user)
    {
        final HttpSession httpSession = getSession(true);
        // A negative time (in seconds) indicates the session should never timeout.
        httpSession.setMaxInactiveInterval(sessionExpirationPeriod);
        httpSession.setAttribute(SESSION_NAME, user);
        return httpSession.getId();
    }

    private HttpSession getSession(final boolean create)
    {
        return requestContextProvider.getHttpServletRequest().getSession(create);
    }

    private User finishLogin(final UserDTO userDTO)
    {
        authenticationLog.info("Successful login of user " + userDTO);
        final String sessionToken = createSession(userDTO);
        loggingContextHandler.addContext(sessionToken, "user:" + userDTO.getEmail() + ", session start:"
                + DateFormatUtils.format(new Date(), DATE_FORMAT_PATTERN));
        return BeanUtils.createBean(User.class, userDTO);
    }

    //
    // ICifexService
    //

    public final User getCurrentUser() throws InvalidSessionException
    {
        final HttpSession session = getSession(false);
        if (session == null)
        {
            throw new InvalidSessionException("You are not logged in. Please log in.");
        }
        return BeanUtils.createBean(User.class, session.getAttribute(SESSION_NAME));
    }

    public final User tryToLogin(final String user, final String password) throws UserFailureException
    {
        authenticationLog.info("Try to login user '" + user + "'.");
        final IUserManager userManager = domainModel.getUserManager();
        if (hasExternalAuthenticationService())
        {
            final String applicationToken = externalAuthenticationService.authenticateApplication();
            if (applicationToken == null)
            {
                authenticationLog.error("User '" + user + "' couldn't be authenticated because authentication of "
                        + "the application at the external authentication service failed.");
                throw new UserFailureException("Authentication of the server at "
                        + "the external authentication service failed.");
            }
            final boolean authenticated =
                    externalAuthenticationService.authenticateUser(applicationToken, user, password);
            if (authenticated == false)
            {
                return null;
            }
            final Principal principal = externalAuthenticationService.getPrincipal(applicationToken, user);
            if (principal == null)
            {
                authenticationLog.error("Unknown principal for successfully authenticated user '" + user + "'.");
                throw new UserFailureException("Authentication was successful but user information "
                        + "couldn't be retrieved.");
            }
            final String email = principal.getEmail();
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
            final String encryptedPassword = StringUtilities.encrypt(password);
            final UserDTO userDTO = userManager.tryToFindUser(user);
            if (userDTO == null || encryptedPassword.equals(userDTO.getEncryptedPassword()) == false)
            {
                return null;
            }
            return finishLogin(userDTO);
        }
    }

    public final void logout()
    {
        final HttpSession httpSession = getSession(false);
        if (httpSession != null)
        {
            final UserDTO user = (UserDTO) httpSession.getAttribute(SESSION_NAME);
            loggingContextHandler.destroyContext(httpSession.getId());
            httpSession.removeAttribute(SESSION_NAME);
            httpSession.invalidate();
            authenticationLog.info("Logout of user " + user);
        }
    }
}
