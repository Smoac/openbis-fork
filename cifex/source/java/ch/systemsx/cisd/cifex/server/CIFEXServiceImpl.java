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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

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
import ch.systemsx.cisd.cifex.client.FileNotFoundException;
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
import ch.systemsx.cisd.cifex.server.business.EMailBuilderForUpdateUser;
import ch.systemsx.cisd.cifex.server.business.FileInformation;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.cifex.server.util.FileUploadFeedbackProvider;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LoggingContextHandler;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.BeanUtils;

/**
 * The real <code>ICifexService</code> implementation.
 * 
 * @author Christian Ribeaud
 */
public final class CIFEXServiceImpl implements ICIFEXService
{
    /**
     * The Crowd property for the display name.
     */
    private static final String DISPLAY_NAME_PROPERTY = "displayName";

    /** The attribute name under which the session could be found. */
    public static final String SESSION_NAME = "cifex-user";

    /**
     * The attribute name that holds the absolute paths of the files that should be uploaded in the
     * next request.
     */
    static final String FILES_TO_UPLOAD = "files-to-upload";

    /** The attribute name that holds the queue that has the feedbacks of the upload. */
    static final String UPLOAD_FEEDBACK_QUEUE = "upload-feedback-queue";

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CIFEXServiceImpl.class);

    private final IDomainModel domainModel;

    private final IRequestContextProvider requestContextProvider;

    private final LoggingContextHandler loggingContextHandler;

    private final IAuthenticationService externalAuthenticationService;

    private final IUserActionLog userBehaviorLog;

    private final static boolean DOWNLOAD = true;

    private final static boolean UPLOAD = false;

    /** Session timeout in seconds. */
    private int sessionExpirationPeriod;

    public CIFEXServiceImpl(final IDomainModel domainModel,
            final IRequestContextProvider requestContextProvider,
            final IUserActionLog userBehaviorLog,
            final IAuthenticationService externalAuthenticationService)
    {
        this.domainModel = domainModel;
        this.requestContextProvider = requestContextProvider;
        this.userBehaviorLog = userBehaviorLog;
        this.externalAuthenticationService = externalAuthenticationService;
        loggingContextHandler =
                new LoggingContextHandler(new RequestContextProviderAdapter(requestContextProvider));
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

    private User finishLogin(final UserDTO userDTO)
    {
        // Do not transfer the password or its hash value to the client (security).
        userDTO.setPassword(null);
        userDTO.setPasswordHash(null);
        final String sessionToken = createSession(userDTO);
        loggingContextHandler.addContext(sessionToken, "user (email):" + userDTO.getEmail()
                + ", session start:" + DateFormatUtils.format(new Date(), DATE_FORMAT_PATTERN));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Successfully created session for user " + userDTO);
        }
        userBehaviorLog.logSuccessfulLogin();
        return BeanUtils.createBean(User.class, userDTO);
    }

    private final UserDTO privGetCurrentUser() throws InvalidSessionException
    {
        final HttpSession session = getSession(false);
        if (session == null)
        {
            throw new InvalidSessionException(
                    "You are not logged in or your session has expired. Please log in.");
        }
        return (UserDTO) session.getAttribute(SESSION_NAME);
    }

    private static String describeUser(final UserDTO user)
    {
        if (user.isAdmin())
        {
            return "admin user " + user.getUserCode();
        } else if (user.isPermanent())
        {
            return "permanent user " + user.getUserCode();
        } else
        {
            return "temporary user " + user.getUserCode();
        }
    }

    private final void checkAdmin(final String methodName) throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        final UserDTO user = privGetCurrentUser();
        if (privGetCurrentUser().isAdmin() == false)
        {
            throw new InsufficientPrivilegesException("Method '" + methodName
                    + "': insufficient privileges for " + describeUser(user) + ".");
        }
    }

    //
    // ICifexService
    //

    public final Configuration getConfiguration() throws InvalidSessionException
    {
        return BeanUtils.createBean(Configuration.class, domainModel.getBusinessContext());
    }

    public final User getCurrentUser() throws InvalidSessionException
    {
        return BeanUtils.createBean(User.class, privGetCurrentUser());
    }

    public final User trySwitchToExternalAuthentication(final String userCode,
            final String plainPassword) throws EnvironmentFailureException,
            InvalidSessionException, InsufficientPrivilegesException
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Try to switch to external authentication user '" + userCode + "'.");
        }
        ensureHasExternalAuthentication(userCode);

        final IUserManager userManager = domainModel.getUserManager();
        UserDTO userDTOOrNull = userManager.tryFindUserByCode(userCode);
        ensureUserExistsInDatabase(userCode, userDTOOrNull);
        ensureUserIsNotExternallyAuthenticated(userCode, userDTOOrNull);

        userDTOOrNull = tryExternalAuthenticationServiceLogin(userCode, plainPassword);

        if (userDTOOrNull != null)
        {
            userDTOOrNull.setExternallyAuthenticated(true);
            userDTOOrNull.setExpirationDate(null);
            userDTOOrNull.setPermanent(true);
            userDTOOrNull.setRegistrator(null);

            userManager.updateUser(userDTOOrNull, null);

            copyUserDetailsExceptCode(privGetCurrentUser(), userDTOOrNull); // updating session

            userBehaviorLog.logSwitchToExternalAuthentication(userCode, true);
            return BeanUtils.createBean(User.class, userDTOOrNull);
        } else
        {
            userBehaviorLog.logSwitchToExternalAuthentication(userCode, false);
            throw new InsufficientPrivilegesException("Password incorrect.");
        }

    }

    private void ensureUserIsNotExternallyAuthenticated(final String userCode,
            final UserDTO userDTOOrNull) throws EnvironmentFailureException
    {
        if (userDTOOrNull.isExternallyAuthenticated())
        {
            userBehaviorLog.logSwitchToExternalAuthentication(userCode, false);
            throw new EnvironmentFailureException(String.format(
                    "User with code '%s' is already authenticated externally.", userCode));
        }
    }

    private void ensureUserExistsInDatabase(final String userCode, final UserDTO userDTOOrNull)
            throws EnvironmentFailureException
    {
        if (userDTOOrNull == null)
        {
            userBehaviorLog.logSwitchToExternalAuthentication(userCode, false);
            throw new EnvironmentFailureException(String.format(
                    "User with code '%s' does not exist.", userCode));
        }
    }

    private void ensureHasExternalAuthentication(final String userCode)
            throws EnvironmentFailureException
    {
        if (hasExternalAuthenticationService() == false)
        {
            userBehaviorLog.logSwitchToExternalAuthentication(userCode, false);
            throw new EnvironmentFailureException("No external authentication service available.");
        }
    }

    private void copyUserDetailsExceptCode(final UserDTO to, final UserDTO from)
    {
        to.setAdmin(from.isAdmin());
        to.setEmail(from.getEmail());
        to.setExpirationDate(from.getExpirationDate());
        to.setExternallyAuthenticated(from.isExternallyAuthenticated());
        to.setPermanent(from.isPermanent());
        to.setRegistrationDate(from.getRegistrationDate());
        to.setRegistrator(from.getRegistrator());
        to.setUserFullName(from.getUserFullName());
    }

    public final User tryLogin(final String userCode, final String plainPassword)
            throws EnvironmentFailureException
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Try to login user '" + userCode + "'.");
        }
        final IUserManager userManager = domainModel.getUserManager();
        if (userManager.isDatabaseEmpty())
        {
            final UserDTO userDTO = new UserDTO();
            userDTO.setUserCode(userCode);
            userDTO.setEmail(userCode);
            userDTO.setPassword(new Password(plainPassword));
            userDTO.setAdmin(true);
            userDTO.setPermanent(true);
            userManager.createUser(userDTO);
            return finishLogin(userDTO);
        }
        UserDTO userDTOOrNull = userManager.tryFindUserByCode(userCode);
        if (userDTOOrNull == null || userDTOOrNull.isExternallyAuthenticated())
        {
            userDTOOrNull = tryExternalAuthenticationServiceLogin(userCode, plainPassword);
            if (userDTOOrNull != null)
            {
                return finishLogin(userDTOOrNull);
            }
        } else
        {
            final Password password = new Password(plainPassword);
            if (password.matches(userDTOOrNull.getPasswordHash()))
            {
                return finishLogin(userDTOOrNull);
            }

        }
        userBehaviorLog.logFailedLoginAttempt(userCode);
        return null;
    }

    private UserDTO tryExternalAuthenticationServiceLogin(final String userOrEmail,
            final String password) throws EnvironmentFailureException
    {
        if (hasExternalAuthenticationService())
        {
            final String applicationToken = externalAuthenticationService.authenticateApplication();
            if (applicationToken == null)
            {
                userBehaviorLog.logFailedLoginAttempt(userOrEmail);
                final String msg =
                        "User '" + userOrEmail
                                + "' couldn't be authenticated because authentication of "
                                + "the application at the external authentication service failed.";
                operationLog.error(msg);
                throw new EnvironmentFailureException(msg);
            }
            final boolean authenticated =
                    externalAuthenticationService.authenticateUser(applicationToken, userOrEmail,
                            password);
            if (authenticated == false)
            {
                return null;
            }
            final Principal principal;
            try
            {
                principal =
                        externalAuthenticationService.getPrincipal(applicationToken, userOrEmail);
            } catch (final IllegalArgumentException ex)
            {
                operationLog.error(ex.getMessage());
                throw new EnvironmentFailureException(ex.getMessage());
            }
            final String code = principal.getUserId();
            final String email = principal.getEmail();
            final String firstName = principal.getFirstName();
            final String lastName = principal.getLastName();
            final String displayName;
            if (principal.getProperty(DISPLAY_NAME_PROPERTY) != null)
            {
                displayName = principal.getProperty(DISPLAY_NAME_PROPERTY);
            } else
            {
                displayName = firstName + " " + lastName;
            }
            final IUserManager userManager = domainModel.getUserManager();
            UserDTO userDTO = userManager.tryFindUserByCode(code);
            if (userDTO == null)
            {
                userDTO = new UserDTO();
                userDTO.setUserCode(code);
                userDTO.setUserFullName(displayName);
                userDTO.setEmail(email);
                userDTO.setPassword(null);
                userDTO.setExternallyAuthenticated(true);
                userDTO.setAdmin(false);
                userDTO.setPermanent(true);
                try
                {
                    userManager.createUser(userDTO);
                } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException ex)
                {
                    operationLog.error(ex.getMessage(), ex);
                    // This is actually an environment failure since the user couldn't have done
                    // anything different.
                    throw new EnvironmentFailureException(ex.getMessage());
                }
            } else
            { // check whether name or email of the principal have changed, and update, if
                // necessary
                boolean changed = false;
                if (StringUtils.equals(displayName, userDTO.getUserFullName()) == false)
                {
                    userDTO.setUserFullName(displayName);
                    changed = true;
                }
                if (StringUtils.equals(email, userDTO.getEmail()) == false)
                {
                    userDTO.setEmail(email);
                    changed = true;
                }
                if (changed)
                {
                    try
                    {
                        userManager.updateUser(userDTO, null);
                    } catch (final DataIntegrityViolationException ex)
                    {
                        final String msg =
                                "User '" + code + "' with email '" + email + "' cannot be updated.";
                        operationLog.error(msg, ex);
                        throw new EnvironmentFailureException(msg);
                    }
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

    public void createUser(final User user, final String password, final User registratorOrNull,
            final String comment) throws EnvironmentFailureException, InvalidSessionException,
            InsufficientPrivilegesException, UserFailureException
    {
        checkCreateUserAllowed(user);
        final IUserManager userManager = domainModel.getUserManager();

        final UserDTO userDTO = BeanUtils.createBean(UserDTO.class, user);
        final UserDTO registratorDTO = BeanUtils.createBean(UserDTO.class, registratorOrNull);
        try
        {
            userManager.createUserAndSendEmail(userDTO, password, registratorDTO, comment,
                    getBasicURL());
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException ex)
        {
            throw new UserFailureException(ex.getMessage());
        } catch (final ch.systemsx.cisd.common.exceptions.EnvironmentFailureException ex)
        {
            throw new EnvironmentFailureException(ex.getMessage());
        }
    }

    private String getBasicURL()
    {
        return HttpUtils.getBasicURL(requestContextProvider.getHttpServletRequest());
    }

    private void checkCreateUserAllowed(final User user) throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        final UserDTO currentUser = privGetCurrentUser();
        if (currentUser.isPermanent() == false)
        {
            throw new InsufficientPrivilegesException(
                    "Method 'tryToCreateUser': insufficient privileges for "
                            + describeUser(currentUser) + ".");
        } else if (currentUser.isAdmin() == false && (user.isPermanent() || user.isAdmin()))
        {
            throw new InsufficientPrivilegesException(
                    "Method 'tryToCreateUser': insufficient privileges for "
                            + describeUser(currentUser) + ".");
        }
    }

    public final void logout()
    {
        final HttpSession httpSession = getSession(false);
        if (httpSession != null)
        {
            loggingContextHandler.destroyContext(httpSession.getId());
            // This unbinds all the attributes as well. So do not do clever cleaning here.
            httpSession.invalidate();
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

    public void deleteUser(final String code) throws InvalidSessionException,
            InsufficientPrivilegesException, UserNotFoundException
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

    public void deleteFile(final String idStr) throws InvalidSessionException,
            InsufficientPrivilegesException, FileNotFoundException
    {
        final UserDTO requestUser = privGetCurrentUser();
        final IFileManager fileManager = domainModel.getFileManager();
        final FileInformation fileInfo =
                fileManager.getFileInformationFilestoreUnimportant(Long.parseLong(idStr));
        if (fileInfo.isFileAvailable() == false)
        {
            throw new FileNotFoundException(fileInfo.getErrorMessage());
        }
        if (fileManager.isAllowedDeletion(requestUser, fileInfo.getFileDTO()) == false)
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }
        fileManager.deleteFile(fileInfo.getFileDTO());
    }

    public final void registerFilenamesForUpload(final String[] filenamesForUpload)
            throws InvalidSessionException
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

    /**
     * Update the fields of the user in the database.
     */
    public void updateUser(final User user, final String plainPassword,
            final boolean sendUpdateInformationToUser) throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException
    {
        assert user != null : "User can't be null";
        final IUserManager userManager = domainModel.getUserManager();
        final UserDTO userDTO = BeanUtils.createBean(UserDTO.class, user);

        checkUpdateOfUserIsAllowed(userDTO);

        userManager.updateUser(userDTO, new Password(plainPassword));
        if (sendUpdateInformationToUser)
        {
            try
            {
                final IMailClient mailClient = domainModel.getMailClient();
                final EMailBuilderForUpdateUser builder =
                        new EMailBuilderForUpdateUser(mailClient, this.privGetCurrentUser(),
                                userDTO);
                builder.setURL(getBasicURL());
                if (StringUtils.isNotBlank(plainPassword))
                {
                    builder.setPassword(plainPassword);
                }
                builder.sendEMail();
            } catch (final Exception ex)
            {
                final String msg =
                        "Sending email to email '" + user.getEmail() + "' failed: "
                                + ex.getMessage();
                operationLog.error(msg, ex);
                throw new EnvironmentFailureException(msg);
            }
        }
    }

    /**
     * Changes the user code from <var>before</var> to <var>after</var>.
     */
    public void changeUserCode(final String before, final String after)
            throws InvalidSessionException, InsufficientPrivilegesException,
            EnvironmentFailureException
    {

        final IUserManager userManager = domainModel.getUserManager();

        final UserDTO requestUser = privGetCurrentUser();
        final UserDTO userBefore = userManager.tryFindUserByCode(before);
        if (requestUser.isAdmin() && requestUser.getUserCode().equals(before) == false
                && (userBefore == null || userBefore.isExternallyAuthenticated() == false))
        {
            userManager.changeUserCode(before, after);
            final UserDTO user = userManager.tryFindUserByCode(after);
            try
            {
                final IMailClient mailClient = domainModel.getMailClient();
                final EMailBuilderForUpdateUser builder =
                        new EMailBuilderForUpdateUser(mailClient, this.privGetCurrentUser(), user);
                builder.setURL(getBasicURL());

                builder.sendEMail();
            } catch (final Exception ex)
            {
                final String msg =
                        "Sending email to email '" + user.getEmail() + "' failed: "
                                + ex.getMessage();
                operationLog.error(msg, ex);
                throw new EnvironmentFailureException(msg);
            }
        } else
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }

    }

    /** Check if the current user is allowed to update the given user. */
    private final void checkUpdateOfUserIsAllowed(final UserDTO userToUpdate)
            throws InvalidSessionException, InsufficientPrivilegesException
    {
        checkUpdateOfUserIsAllowed(userToUpdate, privGetCurrentUser(), domainModel.getUserManager());
    }

    /** Check if the current user is allowed to update the given user. */
    // @Private
    static final void checkUpdateOfUserIsAllowed(final UserDTO userToUpdate,
            final UserDTO requestUser, final IUserManager userManager)
            throws InvalidSessionException, InsufficientPrivilegesException
    {
        if (requestUser.isAdmin())
        {
            return;
        }
        if (requestUser.isPermanent() == false)
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }
        // A user is allowed to edit him or herself but not to raise his or her privilege level to
        // admin.
        if (requestUser.getUserCode().equals(userToUpdate.getUserCode()))
        {
            if (userToUpdate.isAdmin())
            {
                throw new InsufficientPrivilegesException("Insufficient privileges for "
                        + describeUser(requestUser) + ".");
            }
            return;
        }
        final List<UserDTO> usersCreatedByRequestUser =
                userManager.listUsersRegisteredBy(requestUser.getUserCode());

        // Check if the current user is the owner of the user to update.
        for (final UserDTO user : usersCreatedByRequestUser)
        {
            if (user.getUserCode().equals(userToUpdate.getUserCode()))
            {
                if (user.isAdmin() || user.isPermanent() || userToUpdate.isAdmin()
                        || userToUpdate.isPermanent())
                {
                    throw new InsufficientPrivilegesException("Insufficient privileges for "
                            + describeUser(requestUser) + ".");
                }
                return;
            }
        }

        throw new InsufficientPrivilegesException("Insufficient privileges for "
                + describeUser(requestUser) + ".");
    }

    public User tryFindUserByUserCode(final String userCode) throws InvalidSessionException
    {
        privGetCurrentUser();
        final IUserManager userManager = domainModel.getUserManager();
        final UserDTO userDTO = userManager.tryFindUserByCode(userCode);
        return BeanUtils.createBean(User.class, userDTO);
    }

    public User[] listUsersRegisteredBy(final String userCode) throws InvalidSessionException
    {
        privGetCurrentUser();
        final IUserManager userManager = domainModel.getUserManager();

        final List<UserDTO> users = userManager.listUsersRegisteredBy(userCode);

        return BeanUtils.createBeanArray(User.class, users, null);
    }

    public void updateFileExpiration(final String idStr, final Date newExpirationDate)
            throws InvalidSessionException, FileNotFoundException
    {
        final IFileManager fileManager = domainModel.getFileManager();
        Date expirationDate;
        if (privGetCurrentUser().isAdmin() == true && newExpirationDate != null)
        {
            expirationDate = newExpirationDate;
        } else
        {
            expirationDate =
                    DateUtils.addMinutes(new Date(), domainModel.getBusinessContext()
                            .getFileRetention());
        }
        final long fileId = Long.parseLong(idStr);
        final FileInformation fileInformation = fileManager.getFileInformation(fileId);
        if (fileInformation.isFileAvailable() == false)
        {
            throw new FileNotFoundException(fileInformation.getErrorMessage());
        }
        fileManager.updateFileExpiration(fileId, expirationDate);
    }

    public User[] listUsersFileSharedWith(final String idStr) throws InvalidSessionException
    {
        privGetCurrentUser();
        final IUserManager userManager = domainModel.getUserManager();

        final List<UserDTO> users = userManager.listUsersFileSharedWith(Long.parseLong(idStr));

        return BeanUtils.createBeanArray(User.class, users, null);
    }

    public void deleteSharingLink(final String idStr, final String userCode)
            throws InvalidSessionException, InsufficientPrivilegesException, FileNotFoundException
    {
        final long fileId = Long.parseLong(idStr);
        final UserDTO requestUser = privGetCurrentUser();
        final IFileManager fileManager = domainModel.getFileManager();
        final FileInformation fileInfo = fileManager.getFileInformationFilestoreUnimportant(fileId);
        if (fileInfo.isFileAvailable() == false)
        {
            throw new FileNotFoundException(fileInfo.getErrorMessage());
        }
        if (fileManager.isAllowedDeletion(requestUser, fileInfo.getFileDTO()) == false)
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }
        fileManager.deleteSharingLink(fileId, userCode);

    }

    public void createSharingLink(final String idStr, final String emailsOfUsers)
            throws UserFailureException, InvalidSessionException, InsufficientPrivilegesException,
            FileNotFoundException
    {
        final UserDTO requestUser = privGetCurrentUser();
        final IFileManager fileManager = domainModel.getFileManager();
        final FileInformation fileInfo = fileManager.getFileInformation(Long.parseLong(idStr));
        if (fileInfo.isFileAvailable() == false)
        {
            throw new FileNotFoundException(fileInfo.getErrorMessage());
        }
        if (fileManager.isAllowedDeletion(requestUser, fileInfo.getFileDTO()) == false)
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }
        final StringTokenizer stringTokenizer = new StringTokenizer(emailsOfUsers, ", \t\n\r\f");
        final List<String> emails = new ArrayList<String>();
        while (stringTokenizer.hasMoreTokens())
        {
            emails.add(stringTokenizer.nextToken());
        }
        final List<FileDTO> files = new ArrayList<FileDTO>();
        files.add(fileInfo.getFileDTO());
        String url = domainModel.getBusinessContext().getOverrideURL();
        if (StringUtils.isBlank(url))
        {
            url = getBasicURL();
        }
        List<String> invalidEmailAddresses;
        try
        {
            invalidEmailAddresses =
                    fileManager.shareFilesWith(url, requestUser, emails, files, fileInfo
                            .getFileDTO().getComment());
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw new UserFailureException(e.getMessage());
        }
        if (invalidEmailAddresses.isEmpty() == false)
        {
            final String msg =
                    "Some email addresses are invalid: "
                            + CollectionUtils.abbreviate(invalidEmailAddresses, 10);
            throw new UserFailureException(msg);
        }
    }
}
