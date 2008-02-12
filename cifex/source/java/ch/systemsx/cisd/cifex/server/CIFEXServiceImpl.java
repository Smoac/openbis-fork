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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
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
import ch.systemsx.cisd.cifex.client.UserNotFoundException;
import ch.systemsx.cisd.cifex.client.dto.Configuration;
import ch.systemsx.cisd.cifex.client.dto.File;
import ch.systemsx.cisd.cifex.client.dto.FileUploadFeedback;
import ch.systemsx.cisd.cifex.client.dto.Message;
import ch.systemsx.cisd.cifex.client.dto.User;
import ch.systemsx.cisd.cifex.server.business.EMailBuilderForNewUser;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.UserHttpSessionHolder;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.util.FileUploadFeedbackProvider;
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
    public static final String SESSION_NAME = "cifex-user";

    /**
     * The attribute name that holds the absolute paths of the files that should be uploaded in the next request.
     */
    static final String FILES_TO_UPLOAD = "files-to-upload";

    /** The attribute name that holds the queue that has the feedbacks of the upload. */
    static final String UPLOAD_FEEDBACK_QUEUE = "upload-feedback-queue";

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
        httpSession.setAttribute(UPLOAD_FEEDBACK_QUEUE, new FileUploadFeedbackProvider());
        return httpSession.getId();
    }

    private final HttpSession getSession(final boolean create)
    {
        return requestContextProvider.getHttpServletRequest().getSession(create);
    }

    private User finishLogin(final UserDTO userDTO) throws UserFailureException
    {
        // Do not transfer the hash value to the client (security).
        userDTO.setEncryptedPassword(null);
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

    private String describeUser(final UserDTO user)
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

    public final User tryLogin(final String userCode, final String password) throws UserFailureException,
            EnvironmentFailureException
    {
        authenticationLog.info("Try to login user '" + userCode + "'.");
        final IUserManager userManager = domainModel.getUserManager();
        if (userManager.isDatabaseEmpty())
        {
            final UserDTO userDTO = new UserDTO();
            userDTO.setUserCode(userCode);
            userDTO.setEmail(userCode);
            userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
            userDTO.setAdmin(true);
            userDTO.setPermanent(true);
            userManager.createUser(userDTO);
            return finishLogin(userDTO);
        }
        UserDTO userDTOOrNull = tryExternalAuthenticationServiceLogin(userCode, password);
        if (userDTOOrNull == null)
        {
            final String encryptedPassword = StringUtilities.computeMD5Hash(password);
            userDTOOrNull = userManager.tryFindUserByCode(userCode);
            if (userDTOOrNull == null || StringUtils.isBlank(userDTOOrNull.getEncryptedPassword())
                    || encryptedPassword.equals(userDTOOrNull.getEncryptedPassword()) == false)
            {
                return null;
            }
        }
        return finishLogin(userDTOOrNull);
    }

    private UserDTO tryExternalAuthenticationServiceLogin(final String userOrEmail, final String password)
            throws UserFailureException, EnvironmentFailureException
    {
        if (hasExternalAuthenticationService())
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
            final String code = principal.getUserId();
            final String email = principal.getEmail();
            final IUserManager userManager = domainModel.getUserManager();
            UserDTO userDTO = userManager.tryFindUserByCode(code);
            if (userDTO == null)
            {
                userDTO = new UserDTO();
                userDTO.setUserCode(code);
                userDTO.setUserFullName(userOrEmail);
                userDTO.setEmail(email);
                userDTO.setEncryptedPassword(null);
                userDTO.setExternallyAuthenticated(true);
                userDTO.setAdmin(false);
                userDTO.setPermanent(true);
                try
                {
                    userManager.createUser(userDTO);
                } catch (final DataIntegrityViolationException ex)
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

    // TODO 2008-02-06, Basil Neff Move logic to UserManager: tryToCreateUser(User user, String encryptedPassword)
    public void createUser(final User user, final String password, final User registratorOrNull, final String comment)
            throws EnvironmentFailureException, InvalidSessionException, InsufficientPrivilegesException,
            UserFailureException
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
        } catch (final DataIntegrityViolationException ex)
        {
            final String msg =
                    "User with Username '" + user.getUserCode()
                            + "' already exists in the database but email needs to be unique.";
            operationLog.error(msg, ex);
            throw new UserFailureException(msg);
        }

        try
        {
            final IMailClient mailClient = domainModel.getMailClient();
            final EMailBuilderForNewUser builder = new EMailBuilderForNewUser(mailClient, registratorDTO, userDTO);
            builder.setURL(HttpUtils.getBasicURL(requestContextProvider.getHttpServletRequest()));
            builder.setPassword(finalPassword);
            if (comment != null && comment.equals("") == false)
            {
                builder.setComment(comment);
            }
            builder.sendEMail();
        } catch (final Exception ex)
        {
            final String msg = "Sending email to email '" + user.getEmail() + "' failed: " + ex.getMessage();
            operationLog.error(msg, ex);
            throw new EnvironmentFailureException(msg);
        }
    }

    private void checkCreateUserAllowed(final User user) throws InvalidSessionException,
            InsufficientPrivilegesException
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

    private String getFinalPassword(final String password)
    {
        if (StringUtils.isBlank(password))
        {
            final PasswordGenerator passwordGenerator = domainModel.getPasswordGenerator();
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
            UserHttpSessionHolder.invalidateSession(httpSession);
            if (authenticationLog.isInfoEnabled())
            {
                authenticationLog.info("Logout of user " + user);
            }
        }
    }

    public File[] listFiles() throws InvalidSessionException, InsufficientPrivilegesException
    {
        checkAdmin("listFiles");
        final List<FileDTO> files = domainModel.getFileManager().listFiles();
        return BeanUtils.createBeanArray(File.class, files, null);
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
        if (showDownload)
        {
            files = domainModel.getFileManager().listDownloadFiles(user.getID());
        } else
        {
            files = domainModel.getFileManager().listUploadedFiles(user.getID());
        }
        return BeanUtils.createBeanArray(File.class, files, null);
    }

    public void deleteUser(final String code) throws InvalidSessionException, InsufficientPrivilegesException,
            UserNotFoundException
    {
        final UserDTO user = domainModel.getUserManager().tryFindUserByCode(code);
        checkUpdateOfUserIsAllowed(user);
        try
        {
            domainModel.getUserManager().deleteUser(code);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException ex)
        {
            throw new UserNotFoundException(ex.getMessage());
        }
    }

    public void deleteFile(final long id) throws InvalidSessionException
    {
        final UserDTO currentUser = privGetCurrentUser();
        final IFileManager fileManager = domainModel.getFileManager();
        // Following throws UserFailureException if no access to the file.
        fileManager.getFile(currentUser, id);
        fileManager.deleteFile(id);
    }

    public final void registerFilenamesForUpload(final String[] filenamesForUpload) throws InvalidSessionException
    {
        assert filenamesForUpload != null && filenamesForUpload.length > 0 : "No file path found.";
        privGetCurrentUser();
        final HttpSession session = getSession(false);
        session.setAttribute(FILES_TO_UPLOAD, filenamesForUpload);
        final FileUploadFeedbackProvider feedbackProvider =
            (FileUploadFeedbackProvider) session.getAttribute(UPLOAD_FEEDBACK_QUEUE);
        feedbackProvider.set(new FileUploadFeedback());
    }

    public final FileUploadFeedback getFileUploadFeedback() throws InvalidSessionException
    {
        privGetCurrentUser();
        final HttpSession session = getSession(false);
        final FileUploadFeedbackProvider feedbackProvider =
                (FileUploadFeedbackProvider) session.getAttribute(UPLOAD_FEEDBACK_QUEUE);
        assert feedbackProvider != null : "Provider must not be null.";
        try
        {
            return feedbackProvider.take();
        } catch (final InterruptedException ex)
        {
            final FileUploadFeedback feedback = new FileUploadFeedback();
            feedback.setMessage(new Message(Message.ERROR, ex.getClass().getSimpleName()));
            return feedback;
        }
    }

    /** Update the fields of the user in the database. */
    public void updateUser(final User user, final String password) throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        assert user != null : "User can't be null";
        final IUserManager userManager = domainModel.getUserManager();
        final UserDTO userDTO = BeanUtils.createBean(UserDTO.class, user);

        checkUpdateOfUserIsAllowed(userDTO);

        String encryptedPassword = null;
        if (password != null && password.equals("") == false)
        {
            encryptedPassword = StringUtilities.computeMD5Hash(password);
        }

        userManager.updateUser(userDTO, encryptedPassword);
    }

    /** Check if the current user is allowed to update the given user. */
    private final void checkUpdateOfUserIsAllowed(final UserDTO userToUpdate) throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        final IUserManager userManager = domainModel.getUserManager();
        final UserDTO currentUser = privGetCurrentUser();
        final List<UserDTO> ownedUsers = userManager.listUsersRegisteredBy(currentUser);

        if (currentUser.isAdmin())
        {
            return;
        }
        // Check if the current user is the owner
        for (int i = 0; i < ownedUsers.size(); i++)
        {
            if (ownedUsers.get(i).getRegistrator().getUserCode() == currentUser.getUserCode())
            {
                return;
            }
        }

        throw new InsufficientPrivilegesException("Insufficient privileges for " + describeUser(privGetCurrentUser())
                + ".");
    }

    public User tryFindUserByUserCode(final String userCode)
    {
        final IUserManager userManager = domainModel.getUserManager();

        final UserDTO userDTO = userManager.tryFindUserByCode(userCode);
        return BeanUtils.createBean(User.class, userDTO);
    }

    public User[] listUsersRegisteredBy(final User user)
    {
        final IUserManager userManager = domainModel.getUserManager();

        final List<UserDTO> users = userManager.listUsersRegisteredBy(BeanUtils.createBean(UserDTO.class, user));

        return BeanUtils.createBeanArray(User.class, users, null);
    }

    public void updateFileExpiration(final long id, final Date newExpirationDate) throws InvalidSessionException
    {
        final IFileManager fileManager = domainModel.getFileManager();
        Date expirationDate;
        if (privGetCurrentUser().isAdmin() == true && newExpirationDate != null)
        {
            expirationDate = newExpirationDate;
        } else
        {
            expirationDate = DateUtils.addMinutes(new Date(), domainModel.getBusinessContext().getFileRetention());
        }

        fileManager.updateFileExpiration(id, expirationDate);
    }

}
