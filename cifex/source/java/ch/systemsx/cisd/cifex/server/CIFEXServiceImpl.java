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
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.client.EnvironmentFailureException;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.InsufficientPrivilegesException;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.UserFailureException;
import ch.systemsx.cisd.cifex.client.dto.Configuration;
import ch.systemsx.cisd.cifex.client.dto.File;
import ch.systemsx.cisd.cifex.client.dto.FooterData;
import ch.systemsx.cisd.cifex.client.dto.User;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.logging.IRemoteHostProvider;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LoggingContextHandler;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
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

    /**
     * The attribute name that holds the queue that has the names of the files that should be uploaded in the next
     * request.
     */
    static final String UPLOAD_QUEUE = "filenames-for-upload";

    /** The attribute name that holds the queue that has the messages of the upload. */
    static final String UPLOAD_MSG_QUEUE = "upload-messages";

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd hh:mm:ss";

    private static final Logger authenticationLog = LogFactory.getLogger(LogCategory.AUTH, CIFEXServiceImpl.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, CIFEXServiceImpl.class);

    private final IDomainModel domainModel;

    private final IRequestContextProvider requestContextProvider;

    private final LoggingContextHandler loggingContextHandler;

    private final IAuthenticationService externalAuthenticationService;

    private final static boolean DOWNLOAD = true;

    private final static boolean UPLOAD = false;

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
        httpSession.setAttribute(UPLOAD_QUEUE, new LinkedBlockingQueue<String[]>());
        httpSession.setAttribute(UPLOAD_MSG_QUEUE, new LinkedBlockingQueue<String>());
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

    private String describeUser(UserDTO user)
    {
        if (user.isAdmin())
        {
            return "admin user " + user.getEmail();
        } else if (user.isPermanent())
        {
            return "permanent user " + user.getEmail();
        } else
        {
            return "temporary user " + user.getEmail();
        }
    }

    private final void checkAdmin(final String methodName) throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        final UserDTO user = privGetCurrentUser();
        if (privGetCurrentUser().isAdmin() == false)
        {
            throw new InsufficientPrivilegesException("Method '" + methodName + "': insufficient privileges for "
                    + describeUser(user) + ".");
        }
    }

    //
    // ICifexService
    //

    public Configuration getConfiguration() throws InvalidSessionException
    {
        return BeanUtils.createBean(Configuration.class, domainModel.getBusinessContext());
    }

    public final User getCurrentUser() throws InvalidSessionException
    {
        return BeanUtils.createBean(User.class, privGetCurrentUser());
    }

    public final User tryToLogin(final String userOrEmail, final String password, final boolean requestAdmin)
            throws UserFailureException, EnvironmentFailureException
    {
        authenticationLog.info("Try to login user '" + userOrEmail + "'.");
        final IUserManager userManager = domainModel.getUserManager();
        if (userManager.isDatabaseEmpty())
        {
            final UserDTO userDTO = new UserDTO();
            userDTO.setEmail(userOrEmail);
            userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
            userDTO.setAdmin(true);
            userDTO.setPermanent(true);
            userManager.createUser(userDTO);
            return finishLogin(userDTO, true);
        }
        UserDTO userDTOOrNull = tryExternalAuthenticationServiceLogin(userOrEmail, password, requestAdmin);
        if (userDTOOrNull == null)
        {
            final String encryptedPassword = StringUtilities.computeMD5Hash(password);
            userDTOOrNull = userManager.tryToFindUser(userOrEmail);
            if (userDTOOrNull == null || StringUtils.isBlank(userDTOOrNull.getEncryptedPassword())
                    || encryptedPassword.equals(userDTOOrNull.getEncryptedPassword()) == false)
            {
                return null;
            }
        }
        return finishLogin(userDTOOrNull, requestAdmin);
    }

    private UserDTO tryExternalAuthenticationServiceLogin(String userOrEmail, String password, boolean requestAdmin)
            throws UserFailureException, EnvironmentFailureException
    {
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
            final IUserManager userManager = domainModel.getUserManager();
            UserDTO userDTO = userManager.tryToFindUser(email);
            if (userDTO == null)
            {
                userDTO = new UserDTO();
                userDTO.setUserFullName(userOrEmail);
                userDTO.setEmail(email);
                userDTO.setEncryptedPassword(null);
                userDTO.setExternallyAuthenticated(true);
                userDTO.setAdmin(false);
                userDTO.setPermanent(true);
                try
                {
                    userManager.createUser(userDTO);
                } catch (DataIntegrityViolationException ex)
                {
                    final String msg =
                            "User '"
                                    + userOrEmail
                                    + "' with email '"
                                    + email
                                    + "' cannot be created because a user with this email already exists in the database.";
                    operationLog.error(msg, ex);
                    throw new EnvironmentFailureException(msg);
                }
            }
            return userDTO;
        } else
        {
            return null;
        }
    }

    public final User[] listUsers() throws InvalidSessionException, InsufficientPrivilegesException
    {
        checkAdmin("listUsers");
        final List<UserDTO> users = domainModel.getUserManager().listUsers();
        return BeanUtils.createBeanArray(User.class, users, null);
    }

    public void tryToCreateUser(User user, String password, User registratorOrNull) throws EnvironmentFailureException,
            InvalidSessionException, InsufficientPrivilegesException, UserFailureException
    {
        checkCreateUserAllowed(user);
        final IUserManager userManager = domainModel.getUserManager();

        final UserDTO userDTO = BeanUtils.createBean(UserDTO.class, user);
        final String finalPassword = getFinalPassword(password);
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(finalPassword));
        final UserDTO registratorDTO = BeanUtils.createBean(UserDTO.class, registratorOrNull);
        userDTO.setRegistrator(registratorDTO);
        try
        {
            userManager.createUser(userDTO);
        } catch (DataIntegrityViolationException ex)
        {
            final String msg =
                    "User with email '" + user.getEmail()
                            + "' already exists in the database but email needs to be unique.";
            operationLog.error(msg, ex);
            throw new UserFailureException(msg);
        }

        try
        {
            sendPasswordToNewUser(user, finalPassword);
        } catch (final Exception ex)
        {
            final String msg = "Sending email to email '" + user.getEmail() + "' failed: " + ex.getMessage();
            operationLog.error(msg, ex);
            throw new EnvironmentFailureException(msg);
        }
    }

    private void checkCreateUserAllowed(User user) throws InvalidSessionException, InsufficientPrivilegesException
    {
        final UserDTO requestUser = privGetCurrentUser();
        if (requestUser.isPermanent() == false)
        {
            throw new InsufficientPrivilegesException("Method 'tryToCreateUser': insufficient privileges for "
                    + describeUser(requestUser) + ".");
        } else if (requestUser.isAdmin() == false && (user.isPermanent() || user.isAdmin()))
        {
            throw new InsufficientPrivilegesException("Method 'tryToCreateUser': insufficient privileges for "
                    + describeUser(requestUser) + ".");
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
            for (final Enumeration<String> enumeration = httpSession.getAttributeNames(); enumeration.hasMoreElements();)
            {
                httpSession.removeAttribute(enumeration.nextElement());
            }
            httpSession.invalidate();
            if (authenticationLog.isInfoEnabled())
            {
                authenticationLog.info("Logout of user " + user);
            }
        }
    }

    public final File[] listDownloadFiles() throws InvalidSessionException
    {
        return listFiles(DOWNLOAD);
    }

    public final File[] listUploadedFiles() throws InvalidSessionException
    {
        return listFiles(UPLOAD);
    }

    private final File[] listFiles(final boolean showDownload) throws InvalidSessionException
    {
        final UserDTO user = privGetCurrentUser();
        final List<FileDTO> files;
        if (user.isAdmin())
        {
            files = domainModel.getFileManager().listFiles();
        } else if (showDownload)
        {
            files = domainModel.getFileManager().listDownloadFiles(user.getID());
        } else
        {
            files = domainModel.getFileManager().listUploadedFiles(user.getID());
        }
        return BeanUtils.createBeanArray(File.class, files, null);
    }

    public void tryToDeleteUser(final String email) throws InvalidSessionException, InsufficientPrivilegesException
    {
        checkAdmin("tryToDeleteUser");
        domainModel.getUserManager().tryToDeleteUser(email);
    }

    public void tryToDeleteFile(final long id) throws InvalidSessionException
    {
        final UserDTO currentUser = privGetCurrentUser();
        final IFileManager fileManager = domainModel.getFileManager();
        // Following throws UserFailureException if no access to the file.
        fileManager.getFile(currentUser, id);
        fileManager.deleteFile(id);
    }

    public void registerFilenamesForUpload(String[] filenamesForUpload) throws InvalidSessionException
    {
        privGetCurrentUser();
        final BlockingQueue<String[]> uploadQueue =
                (BlockingQueue<String[]>) getSession(false).getAttribute(UPLOAD_QUEUE);
        uploadQueue.add(filenamesForUpload);
    }

    public String waitForUploadToFinish() throws InvalidSessionException
    {
        privGetCurrentUser();
        final BlockingQueue<String> uploadQueue =
                (BlockingQueue<String>) getSession(false).getAttribute(UPLOAD_MSG_QUEUE);
        try
        {
            return uploadQueue.take();
        } catch (InterruptedException ex)
        {
            return ex.getClass().getSimpleName();
        }
    }

    private void sendPasswordToNewUser(User user, String password)
    {
        StringBuilder builder = new StringBuilder();
        String url = HttpUtils.getBasicURL(requestContextProvider.getHttpServletRequest());
        String role = "temporary";
        if (user.isAdmin())
        {
            role = "administration";
        } else if (user.isPermanent())
        {
            role = "permanent";
        }

        builder.append("There is a " + role + " user created for you on the Cifex Server. "
                + "You can reach the service with the following login information: ");
        final String email = user.getEmail();
        builder.append("\nURL:\t\t").append(url).append("&email=").append(email);
        builder.append("\nLogin:\t\t").append(email);
        builder.append("\nPassword:\t").append(password);

        if (user.isPermanent() == false)
        {
            builder.append("\n\nThe user is only temporary, the login expires in a few days. "
                    + "You can see the expiration date when you login.");
        }
        final IMailClient mailClient = domainModel.getMailClient();
        mailClient.sendMessage("A " + role + " user is created on the Cifex Server", builder.toString(), new String[]
            { email });
    }

    public FooterData getFooterData() throws InvalidSessionException
    {
        String administratorEmail = domainModel.getAdministratorEmail();
        String systemVersion = BuildAndEnvironmentInfo.INSTANCE.getFullVersion();
        FooterData footerData = new FooterData();
        footerData.setAdministratorEmail(administratorEmail);
        footerData.setSystemVersion(systemVersion);
        return footerData;
    }
}
