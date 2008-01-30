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
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.client.EnvironmentFailureException;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.UserFailureException;
import ch.systemsx.cisd.cifex.client.dto.File;
import ch.systemsx.cisd.cifex.client.dto.User;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.logging.IRemoteHostProvider;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LoggingContextHandler;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.PasswordGenerator;
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

                //
                // IRemoteHostProvider
                //

                public final String getRemoteHost()
                {
                    return requestContextProvider.getHttpServletRequest().getRemoteHost();
                }
            });
        if (hasExternalAuthenticationService())
        {
            this.externalAuthenticationService.check();
        }
    }

    public final void setSessionExpirationPeriodInMinutes(final int sessionExpirationPeriodInMinutes)
    {
        sessionExpirationPeriod = sessionExpirationPeriodInMinutes * 60;
    }

    private final boolean hasExternalAuthenticationService()
    {
        return externalAuthenticationService != null
                && externalAuthenticationService instanceof NullAuthenticationService == false;
    }

    private final String createSession(final UserDTO user)
    {
        final HttpSession httpSession = getSession(true);
        // A negative time (in seconds) indicates the session should never timeout.
        httpSession.setMaxInactiveInterval(sessionExpirationPeriod);
        httpSession.setAttribute(SESSION_NAME, user);
        return httpSession.getId();
    }

    private final HttpSession getSession(final boolean create)
    {
        return requestContextProvider.getHttpServletRequest().getSession(create);
    }

    private User finishLogin(final UserDTO userDTO, final boolean requestAdmin) throws UserFailureException
    {
        // Do not transfer the hash value to the client (security).
        userDTO.setEncryptedPassword(null);
        if (requestAdmin == false)
        {
            userDTO.setAdmin(false);
        } else if (userDTO.isAdmin() == false)
        {
            throw new UserFailureException("User does not have admin permissions on this server.");
        }
        authenticationLog.info("Successful login of user " + userDTO);
        final String sessionToken = createSession(userDTO);
        loggingContextHandler.addContext(sessionToken, "user (email):" + userDTO.getEmail() + ", session start:"
                + DateFormatUtils.format(new Date(), DATE_FORMAT_PATTERN));
        return BeanUtils.createBean(User.class, userDTO);
    }

    private final UserDTO privGetCurrentUser() throws InvalidSessionException
    {
        final HttpSession session = getSession(false);
        if (session == null)
        {
            throw new InvalidSessionException("You are not logged in. Please log in.");
        }
        return (UserDTO) session.getAttribute(SESSION_NAME);
    }

    //
    // ICifexService
    //

    public final User getCurrentUser() throws InvalidSessionException
    {
        return BeanUtils.createBean(User.class, privGetCurrentUser());
    }

    public final User tryToLogin(final String userOrEmail, final String password, final boolean requestAdmin)
            throws UserFailureException
    {
        authenticationLog.info("Try to login user '" + userOrEmail + "'.");
        final IUserManager userManager = domainModel.getUserManager();
        if (hasExternalAuthenticationService() && requestAdmin == false)
        {
            final String applicationToken = externalAuthenticationService.authenticateApplication();
            if (applicationToken == null)
            {
                authenticationLog.error("User '" + userOrEmail
                        + "' couldn't be authenticated because authentication of "
                        + "the application at the external authentication service failed.");
                throw new UserFailureException("Authentication of the server application at "
                        + "the external authentication service failed.");
            }
            final boolean authenticated =
                    externalAuthenticationService.authenticateUser(applicationToken, userOrEmail, password);
            if (authenticated == false)
            {
                return null;
            }
            final Principal principal = externalAuthenticationService.getPrincipal(applicationToken, userOrEmail);
            if (principal == null)
            {
                authenticationLog.error("Principal is null for successfully authenticated user '" + userOrEmail + "'.");
                throw new UserFailureException("Unable to retrieve user information.");
            }
            final String email = principal.getEmail();
            UserDTO userDTO = userManager.tryToFindUser(email);
            if (userDTO == null)
            {
                userDTO = new UserDTO();
                userDTO.setUserName(userOrEmail);
                userDTO.setEmail(email);
                userDTO.setEncryptedPassword(null);
                userDTO.setExternallyAuthenticated(true);
                userDTO.setAdmin(false);
                userDTO.setPermanent(true);
                userManager.createUser(userDTO);
            }
            return finishLogin(userDTO, false);
        } else
        {
            final String encryptedPassword = StringUtilities.computeMD5Hash(password);
            final UserDTO userDTO = userManager.tryToFindUser(userOrEmail);
            if (userDTO == null || StringUtils.isBlank(userDTO.getEncryptedPassword())
                    || encryptedPassword.equals(userDTO.getEncryptedPassword()) == false)
            {
                return null;
            }
            return finishLogin(userDTO, requestAdmin);
        }
    }

    public final List listUsers()
    {
        final List<UserDTO> users = domainModel.getUserManager().listUsers();
        List<User> userList = BeanUtils.createBeanList(User.class, users);
        return userList;
    }

    public void tryToCreateUser(User user, String password) throws EnvironmentFailureException
    {
        final IUserManager userManager = domainModel.getUserManager();

        UserDTO userDTO = BeanUtils.createBean(UserDTO.class, user);
        final String finalPassword = getFinalPassword(password);
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(finalPassword));
        userManager.createUser(userDTO);

        try
        {
            sendPasswordToNewUser(user, finalPassword);
        } catch (Exception ex)
        {
            throw new EnvironmentFailureException("Sending email to email '" + user.getEmail() + "' failed: "
                    + ex.getMessage());
        }
    }

    private String getFinalPassword(String password)
    {
        if (StringUtils.isBlank(password))
        {
            PasswordGenerator passwordGenerator = domainModel.getPasswordGenerator();
            return passwordGenerator.generatePassword(10);
        } else
        {
            return password;
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

    public final File[] listDownloadFiles() throws UserFailureException
    {
        final UserDTO user = privGetCurrentUser();
        final List<FileDTO> files = domainModel.getFileManager().listFiles(user.getID());
        return BeanUtils.createBeanArray(File.class, files, new BeanUtils.Converter()
            {
                @SuppressWarnings("unused")
                public final String convertToSize(final FileDTO fileDTO)
                {
                    return FileUtils.byteCountToDisplaySize(fileDTO.getSize());
                }
            });
    }

    private void sendPasswordToNewUser(User user, String password)
    {
        StringBuilder builder = new StringBuilder();
        String url = requestContextProvider.getHttpServletRequest().getRequestURL().toString();
        String role = "temporary";
        if (user.isAdmin())
        {
            role = "administration";
        } else if (user.isPermanent())
        {
            role = "permanent";
        }

        builder
                .append("There is a "
                        + role
                        + " user created for you on the Cifex Server. You can reach the service with the following login information: ");
        builder.append("\nURL:\t").append(url).append("/index.html");
        builder.append("\nLogin:\t").append(user.getEmail());
        builder.append("\nPassword:\t").append(password);

        if (user.isPermanent() == false)
        {
            builder
                    .append("\n\nThe user is only temporary, the login expires in a few days. You can see the expiration date when you login.");
        }
        IMailClient mailClient = domainModel.getMailClient();
        mailClient.sendMessage("A " + role + " user is created on the Cifex Server", builder.toString(), new String[]
            { user.getEmail() });
    }
}
