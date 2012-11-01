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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.client.Configuration;
import ch.systemsx.cisd.cifex.client.FileNotFoundException;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.InsufficientPrivilegesException;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.UserNotFoundException;
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
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.EnvironmentFailureException;
import ch.systemsx.cisd.cifex.shared.basic.UserFailureException;
import ch.systemsx.cisd.cifex.shared.basic.dto.CurrentUserInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileUploadFeedback;
import ch.systemsx.cisd.cifex.shared.basic.dto.Message;
import ch.systemsx.cisd.cifex.shared.basic.dto.OwnerFileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.reflection.BeanUtils;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;

/**
 * The real <code>ICifexService</code> implementation.
 * 
 * @author Christian Ribeaud
 */
public final class CIFEXServiceImpl extends AbstractCIFEXService implements ICIFEXService
{
    /**
     * The attribute name that holds the absolute paths of the files that should be uploaded in the
     * next request.
     */
    static final String FILES_TO_UPLOAD = "files-to-upload";

    private static final long DELAY_AFTER_FAILED_LOGIN_MILLIS = 500L;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CIFEXServiceImpl.class);

    public CIFEXServiceImpl(final IDomainModel domainModel,
            final IRequestContextProvider requestContextProvider,
            final IUserActionLog userActionLog,
            final IAuthenticationService externalAuthenticationService,
            final int sessionExpirationPeriodMinutes)
    {
        super(domainModel, requestContextProvider, userActionLog, externalAuthenticationService,
                createLoggingContextHandler(requestContextProvider), sessionExpirationPeriodMinutes);
    }

    @Override
    public final Boolean showSwitchToExternalOption(final UserInfoDTO user)
    {
        return hasExternalAuthenticationService() && user.isExternallyAuthenticated() == false;
    }

    private final UserDTO privGetCurrentUser() throws InvalidSessionException
    {
        final HttpSession session = getSession(false);
        if (session == null)
        {
            throw new InvalidSessionException(
                    "You are not logged in or your session has expired. Please log in.");
        }
        return (UserDTO) session.getAttribute(SESSION_ATTRIBUTE_USER_NAME);
    }

    private void updateCurrentUser(final UserDTO user) throws InvalidSessionException
    {
        if (user.getID().equals(privGetCurrentUser().getID()) == false)
        {
            return;
        }
        getSession(false).setAttribute(SESSION_ATTRIBUTE_USER_NAME, user);
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

    @Override
    public Boolean keepSessionAlive()
    {
        try
        {
            privGetCurrentUser();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(userActionLog.getUserHostSessionDescription()
                        + "Keep alive ping: OK");
            }
            return true;
        } catch (InvalidSessionException ex)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(userActionLog.getUserHostSessionDescription()
                        + "Keep alive ping: FAILED");
            }
            return false;
        }
    }

    @Override
    public final CurrentUserInfoDTO tryLogin(final String userCode, final String plainPassword)
            throws EnvironmentFailureException
    {
        try
        {
            final UserDTO userDTOOrNull = super.tryLoginUser(userCode, plainPassword);
            if (userDTOOrNull == null)
            {
                userActionLog.logFailedLoginAttempt(userCode);
                ConcurrencyUtilities.sleep(DELAY_AFTER_FAILED_LOGIN_MILLIS);
                return null;
            } else
            {
                userActionLog.logSuccessfulLogin();
            }
            final CurrentUserInfoDTO currentUser =
                    BeanUtils.createBean(CurrentUserInfoDTO.class, userDTOOrNull);
            currentUser.setHasFilesForDownload(domainModel.getUserManager()
                    .hasUserFilesForDownload(userDTOOrNull));
            return currentUser;
        } catch (ch.systemsx.cisd.common.exceptions.EnvironmentFailureException ex)
        {
            throw new EnvironmentFailureException(ex.getMessage());
        }
    }

    @Override
    public final Configuration getConfiguration() throws InvalidSessionException
    {
        final Configuration configuration =
                BeanUtils.createBean(Configuration.class, domainModel.getBusinessContext());
        configuration.setSystemHasExternalAuthentication(hasExternalAuthenticationService());
        return configuration;
    }

    @Override
    public final CurrentUserInfoDTO getCurrentUser() throws InvalidSessionException
    {
        final UserDTO userOrNull = privGetCurrentUser();
        if (userOrNull == null)
        {
            return null;
        }
        final CurrentUserInfoDTO currentUser =
                BeanUtils.createBean(CurrentUserInfoDTO.class, userOrNull);
        currentUser.setHasFilesForDownload(domainModel.getUserManager().hasUserFilesForDownload(
                userOrNull));
        return currentUser;
    }

    @Override
    public UserInfoDTO refreshQuotaInformationOfCurrentUser() throws InvalidSessionException
    {
        final IUserManager userManager = domainModel.getUserManager();
        final UserDTO currentUser = privGetCurrentUser();
        userManager.refreshQuotaInformation(currentUser);
        return BeanUtils.createBean(UserInfoDTO.class, currentUser);
    }

    @Override
    public final UserInfoDTO trySwitchToExternalAuthentication(final String userCode,
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

        try
        {
            userDTOOrNull = tryExternalAuthenticationServiceLogin(userCode, plainPassword);
        } catch (ch.systemsx.cisd.common.exceptions.EnvironmentFailureException ex)
        {
            throw new EnvironmentFailureException(ex.getMessage());
        }

        if (userDTOOrNull != null)
        {
            userDTOOrNull.setExternallyAuthenticated(true);
            userDTOOrNull.setExpirationDate(null);
            userDTOOrNull.setRegistrator(null);

            userManager.updateUser(userDTOOrNull, null, privGetCurrentUser(), null);

            copyUserDetailsExceptCode(privGetCurrentUser(), userDTOOrNull); // updating session

            userActionLog.logSwitchToExternalAuthentication(userCode, true);
            return BeanUtils.createBean(UserInfoDTO.class, userDTOOrNull);
        } else
        {
            userActionLog.logSwitchToExternalAuthentication(userCode, false);
            throw new InsufficientPrivilegesException("Password incorrect.");
        }

    }

    private void ensureUserIsNotExternallyAuthenticated(final String userCode,
            final UserDTO userDTOOrNull) throws EnvironmentFailureException
    {
        if (userDTOOrNull.isExternallyAuthenticated())
        {
            userActionLog.logSwitchToExternalAuthentication(userCode, false);
            throw new EnvironmentFailureException(String.format(
                    "User with code '%s' is already authenticated externally.", userCode));
        }
    }

    private void ensureUserExistsInDatabase(final String userCode, final UserDTO userDTOOrNull)
            throws EnvironmentFailureException
    {
        if (userDTOOrNull == null)
        {
            userActionLog.logSwitchToExternalAuthentication(userCode, false);
            throw new EnvironmentFailureException(String.format(
                    "User with code '%s' does not exist.", userCode));
        }
    }

    private void ensureHasExternalAuthentication(final String userCode)
            throws EnvironmentFailureException
    {
        if (hasExternalAuthenticationService() == false)
        {
            userActionLog.logSwitchToExternalAuthentication(userCode, false);
            throw new EnvironmentFailureException("No external authentication service available.");
        }
    }

    private void copyUserDetailsExceptCode(final UserDTO to, final UserDTO from)
    {
        to.setAdmin(from.isAdmin());
        to.setEmail(from.getEmail());
        to.setExpirationDate(from.getExpirationDate());
        to.setExternallyAuthenticated(from.isExternallyAuthenticated());
        to.setRegistrationDate(from.getRegistrationDate());
        to.setRegistrator(from.getRegistrator());
        to.setUserFullName(from.getUserFullName());
    }

    @Override
    public final List<UserInfoDTO> listUsers() throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        checkAdmin("listUsers");
        final List<UserDTO> users = domainModel.getUserManager().listUsers();
        return BeanUtils.createBeanList(UserInfoDTO.class, users, null);
    }

    @Override
    public UserInfoDTO createUser(final UserInfoDTO user, final String password,
            final String comment) throws EnvironmentFailureException, InvalidSessionException,
            InsufficientPrivilegesException, UserFailureException
    {
        boolean success = false;
        UserDTO userDTO = BeanUtils.createBean(UserDTO.class, user);
        try
        {
            checkCreateUserAllowed(user);
            ensureCodeIsValid(user.getUserCode());
            final IUserManager userManager = domainModel.getUserManager();

            final UserDTO userFromExternalAuthServiceOrNull =
                    tryCreateUserFromExternalAuthenticationService(user);
            if (userFromExternalAuthServiceOrNull != null)
            {
                userDTO = userFromExternalAuthServiceOrNull;
                success = true;
                return BeanUtils.createBean(UserInfoDTO.class, userFromExternalAuthServiceOrNull);
            }
            if (StringUtils.isBlank(user.getEmail()))
            {
                throw new UserFailureException(
                        "No email address but user not found in external authentication service!");
            }

            try
            {
                final UserDTO createdUser =
                        userManager.createUserAndSendEmail(userDTO, password, privGetCurrentUser(),
                                comment, getBasicURL());
                userDTO = createdUser;
                success = true;
                return BeanUtils.createBean(UserInfoDTO.class, createdUser);
            } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException ex)
            {
                throw new UserFailureException(ex.getMessage());
            } catch (final ch.systemsx.cisd.common.exceptions.EnvironmentFailureException ex)
            {
                throw new EnvironmentFailureException(ex.getMessage());
            }
        } finally
        {
            userActionLog.logCreateUser(userDTO, success);
        }
    }

    private void ensureCodeIsValid(final String code)
    {
        final Pattern userCodePattern = Pattern.compile("^" + Constants.USER_CODE_REGEX);
        if (code == null || code.length() == 0 || userCodePattern.matcher(code).matches() == false)
        {
            throw new IllegalArgumentException("Invalid user code. "
                    + Constants.VALID_USER_CODE_DESCRIPTION);
        }
    }

    private UserDTO tryCreateUserFromExternalAuthenticationService(final UserInfoDTO user)
            throws EnvironmentFailureException, UserFailureException, InvalidSessionException
    {
        if (hasExternalAuthenticationService() == false)
        {
            return null;
        }
        final String userCode = user.getUserCode();

        final Principal principalOrNull =
                externalAuthenticationService.tryGetAndAuthenticateUser(userCode, null);
        if (principalOrNull != null)
        {
            if (privGetCurrentUser().isAdmin() == false)
            {
                final String msg = "Cannot create user '" + user.getUserCode() + "': user exists.";
                operationLog.error(msg);
                throw new UserFailureException(msg);
            }
            return createOrUpdateUserFromExternalAuthenticationService(principalOrNull, user);
        } else
        {
            return null;
        }
    }

    private final String getBasicURL()
    {
        return HttpUtils.getBasicURL(requestContextProvider.getHttpServletRequest());
    }

    private void checkCreateUserAllowed(final UserInfoDTO user) throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        final UserDTO currentUser = privGetCurrentUser();
        if (currentUser.isPermanent() == false)
        {
            operationLog.warn(String.format(
                    "Insufficient privileges: temporary user %s tried to create a user (%s).",
                    currentUser.getUserCode(), user.getUserCode()));
            throw new InsufficientPrivilegesException(
                    "Method 'createUser': insufficient privileges for " + describeUser(currentUser)
                            + ".");
        } else if (currentUser.isAdmin() == false && (user.isPermanent() || user.isAdmin()))
        {
            operationLog.warn(String.format(
                    "Insufficient privileges: non-admin user %s tried to create a%s user (%s).",
                    currentUser.getUserCode(), user.isAdmin() ? "n admin" : " permanent", user
                            .getUserCode()));
            throw new InsufficientPrivilegesException(
                    "Method 'createUser': insufficient privileges for " + describeUser(currentUser)
                            + ".");
        }
    }

    @Override
    public FileInfoDTO getFile(long fileId) throws InvalidSessionException,
            InsufficientPrivilegesException, IllegalArgumentException
    {
        final UserDTO requestUser = privGetCurrentUser();
        final IFileManager fileManager = domainModel.getFileManager();
        final FileDTO fileDTO = fileManager.getFile(fileId);
        if (fileManager.isAllowedAccess(requestUser, fileDTO) == false)
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }
        return BeanUtils.createBean(FileInfoDTO.class, fileDTO);
    }

    @Override
    public List<OwnerFileInfoDTO> listFiles() throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        checkAdmin("listFiles");
        final List<FileDTO> files = domainModel.getFileManager().listFiles();
        return BeanUtils.createBeanList(OwnerFileInfoDTO.class, files, null);
    }

    @Override
    public final List<FileInfoDTO> listDownloadFiles() throws InvalidSessionException
    {
        final UserDTO user = privGetCurrentUser();
        final List<FileDTO> files = domainModel.getFileManager().listDownloadFiles(user.getID());
        return BeanUtils.createBeanList(FileInfoDTO.class, files, null);
    }

    @Override
    public final List<OwnerFileInfoDTO> listOwnedFiles() throws InvalidSessionException
    {
        final UserDTO user = privGetCurrentUser();
        final List<FileDTO> files = domainModel.getFileManager().listOwnedFiles(user.getID());
        return BeanUtils.createBeanList(OwnerFileInfoDTO.class, files, null);
    }

    @Override
    public void deleteUser(final long id) throws InvalidSessionException,
            InsufficientPrivilegesException, UserNotFoundException
    {
        final UserDTO user = domainModel.getUserManager().getUser(id);
        checkUpdateOfUserIsAllowed(user, user);
        try
        {
            domainModel.getUserManager().deleteUser(id, privGetCurrentUser(), userActionLog);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException ex)
        {
            throw new UserNotFoundException(ex.getMessage());
        }
    }

    @Override
    public void deleteFile(final long id) throws InvalidSessionException,
            InsufficientPrivilegesException, FileNotFoundException
    {
        final UserDTO requestUser = privGetCurrentUser();
        final IFileManager fileManager = domainModel.getFileManager();
        final FileInformation fileInfo = fileManager.getFileInformationFilestoreUnimportant(id);
        if (fileInfo.isFileAvailable() == false)
        {
            throw new FileNotFoundException(fileInfo.getErrorMessage());
        }
        boolean success = false;
        try
        {
            if (fileManager.isControlling(requestUser, fileInfo.getFileDTO()) == false)
            {
                throw new InsufficientPrivilegesException("Insufficient privileges for "
                        + describeUser(requestUser) + ".");
            }
            fileManager.deleteFile(fileInfo.getFileDTO());
            success = true;
        } finally
        {
            userActionLog.logDeleteFile(fileInfo.getFileDTO(), success);
        }
    }

    @Override
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

    @Override
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
            feedback.setMessage(new Message(Message.Type.ERROR, ex.getClass().getSimpleName()));
            return feedback;
        }
    }

    /**
     * Update the fields of the user in the database.
     */
    @Override
    public UserInfoDTO updateUser(final UserInfoDTO user, final String plainPassword,
            final boolean sendUpdateInformationToUser) throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException
    {
        assert user != null : "User can't be null";
        final IUserManager userManager = domainModel.getUserManager();
        final UserDTO newUserDTO = BeanUtils.createBean(UserDTO.class, user);
        final UserDTO oldUserDTO = userManager.tryFindUserByCode(user.getUserCode());
        final UserDTO newUserRegistratorOrNull = newUserDTO.getRegistrator();
        if (newUserRegistratorOrNull != null
                && (oldUserDTO.getRegistrator().getID().equals(newUserRegistratorOrNull.getID()) || newUserRegistratorOrNull
                        .getID() == 0))
        {
            newUserRegistratorOrNull.setID(null);
        }

        checkUpdateOfUserIsAllowed(oldUserDTO, newUserDTO);

        final UserDTO updatedUser =
                userManager.updateUser(oldUserDTO, newUserDTO, new Password(plainPassword),
                        privGetCurrentUser(), userActionLog);
        final UserInfoDTO updatedUserInfo = BeanUtils.createBean(UserInfoDTO.class, updatedUser);
        updateCurrentUser(updatedUser);
        if (sendUpdateInformationToUser)
        {
            if (StringUtils.isEmpty(user.getEmail()))
            {
                operationLog.warn(String.format(
                        "Sending email to user '%s' not possible: email address is empty.", user));
                return updatedUserInfo;
            }
            try
            {
                final IMailClient mailClient = domainModel.getMailClient();
                final EMailBuilderForUpdateUser builder =
                        new EMailBuilderForUpdateUser(mailClient, privGetCurrentUser(), newUserDTO);
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
        return updatedUserInfo;
    }

    /**
     * Changes the user code from <var>before</var> to <var>after</var>.
     */
    @Override
    public final void changeUserCode(final String before, final String after)
            throws InvalidSessionException, InsufficientPrivilegesException,
            EnvironmentFailureException
    {

        final IUserManager userManager = domainModel.getUserManager();

        final UserDTO requestUser = privGetCurrentUser();
        ensureCodeIsValid(after);
        final UserDTO userBefore = userManager.tryFindUserByCode(before);
        if (requestUser.isAdmin() && requestUser.getUserCode().equals(before) == false
                && (userBefore == null || userBefore.isExternallyAuthenticated() == false))
        {
            boolean success = false;
            try
            {
                userManager.changeUserCode(before, after);
                success = true;
            } finally
            {
                userActionLog.logChangeUserCode(before, after, success);
            }
            final UserDTO user = userManager.tryFindUserByCode(after);
            if (StringUtils.isEmpty(user.getEmail()))
            {
                operationLog.warn(String.format(
                        "Sending email to user '%s' not possible: email address is empty.", user));
                return;
            }
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
    private final void checkUpdateOfUserIsAllowed(final UserDTO oldUser, final UserDTO userToUpdate)
            throws InvalidSessionException, InsufficientPrivilegesException
    {
        checkUpdateOfUserIsAllowed(oldUser, userToUpdate, privGetCurrentUser(), domainModel
                .getUserManager());
    }

    /** Check if the current user is allowed to update the given user. */
    @Private
    static final void checkUpdateOfUserIsAllowed(final UserDTO oldUser, final UserDTO userToUpdate,
            final UserDTO requestUser, final IUserManager userManager)
            throws InvalidSessionException, InsufficientPrivilegesException
    {
        // Inactive users may not do anything.
        if (requestUser.isActive() == false)
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }
        if (requestUser.isAdmin())
        {
            return;
        }
        if (requestUser.isPermanent() == false)
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }
        // Only admins may change the 'active' flag or the privilege level
        if (userToUpdate.isActive() != oldUser.isActive()
                || userToUpdate.isPermanent() != oldUser.isPermanent()
                || userToUpdate.isAdmin() != oldUser.isAdmin())
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }
        // A user is allowed to edit him or herself.
        if (requestUser.getUserCode().equals(userToUpdate.getUserCode()))
        {
            return;
        }
        final List<UserDTO> usersCreatedByRequestUser =
                userManager.listUsersRegisteredBy(requestUser.getID());

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

    @Override
    public UserInfoDTO getUser(final long id) throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        final IUserManager userManager = domainModel.getUserManager();
        final UserDTO userDTO = userManager.getUser(id);
        checkUpdateOfUserIsAllowed(userDTO, userDTO);
        return BeanUtils.createBean(UserInfoDTO.class, userDTO);
    }

    @Override
    public UserInfoDTO tryFindUserByUserCode(final String userCode) throws InvalidSessionException
    {
        privGetCurrentUser();
        final IUserManager userManager = domainModel.getUserManager();
        final UserDTO userDTO = userManager.tryFindUserByCode(userCode);
        return BeanUtils.createBean(UserInfoDTO.class, userDTO);
    }

    @Override
    public UserInfoDTO tryFindUserByUserCodeOrCreate(final String userCode)
            throws InvalidSessionException
    {
        privGetCurrentUser();
        final IUserManager userManager = domainModel.getUserManager();
        final Collection<UserDTO> users =
                userManager.getUsers(Arrays.asList(userCode), null, userActionLog);
        if (users.isEmpty())
        {
            return null;
        }
        assert users.size() == 1; // A user code is unique in the database.
        return BeanUtils.createBean(UserInfoDTO.class, users.iterator().next());
    }

    @Override
    public List<UserInfoDTO> findUserByEmail(final String email) throws InvalidSessionException
    {
        privGetCurrentUser();
        final IUserManager userManager = domainModel.getUserManager();
        final Collection<UserDTO> users =
                userManager.getUsers(null, Arrays.asList(email), userActionLog);
        return BeanUtils.createBeanList(UserInfoDTO.class, users);
    }

    @Override
    public List<UserInfoDTO> listUsersOwnedBy(final long userId) throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        final UserDTO requestUser = privGetCurrentUser();
        if (requestUser.isAdmin() == false && requestUser.getID() != userId)
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }
        final IUserManager userManager = domainModel.getUserManager();

        final List<UserDTO> users = userManager.listUsersRegisteredBy(userId);

        return BeanUtils.createBeanList(UserInfoDTO.class, users, null);
    }

    @Override
    public Date updateFileUserData(long fileId, String name, String commentOrNull,
            Date expirationDate) throws InvalidSessionException, InsufficientPrivilegesException
    {
        final UserDTO requestUser = privGetCurrentUser();
        final IFileManager fileManager = domainModel.getFileManager();
        final FileDTO file = fileManager.getFile(fileId);
        Date newExpirationDate = null;
        boolean success = false;
        try
        {
            if (fileManager.isControlling(requestUser, file) == false)
            {
                throw new InsufficientPrivilegesException("Insufficient privileges for "
                        + describeUser(requestUser) + ".");
            }
            newExpirationDate =
                    fileManager.updateFileUserData(fileId, name, commentOrNull, expirationDate,
                            requestUser);
            success = true;
            return newExpirationDate;
        } finally
        {
            userActionLog.logEditFile(fileId, name, newExpirationDate, success);
        }
    }

    @Override
    public List<UserInfoDTO> listUsersFileSharedWith(final long fileId)
            throws InvalidSessionException, InsufficientPrivilegesException
    {
        final UserDTO requestUser = privGetCurrentUser();
        final IFileManager fileManager = domainModel.getFileManager();
        final FileDTO file = fileManager.getFile(fileId);
        if (fileManager.isAllowedAccess(requestUser, file) == false)
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }
        final IUserManager userManager = domainModel.getUserManager();

        final List<UserDTO> users = userManager.listUsersFileSharedWith(fileId);

        return BeanUtils.createBeanList(UserInfoDTO.class, users, null);
    }

    @Override
    public void updateSharingLinks(long fileId, List<String> usersToAdd, List<String> usersToRemove)
            throws InvalidSessionException, InsufficientPrivilegesException, FileNotFoundException,
            UserFailureException
    {
        for (String user : usersToAdd)
        {
            createSharingLink(fileId, user);
        }
        for (String user : usersToRemove)
        {
            deleteSharingLink(fileId, user);
        }
    }

    private void deleteSharingLink(final long fileId, final String userCode)
            throws InvalidSessionException, InsufficientPrivilegesException, FileNotFoundException
    {
        final UserDTO requestUser = privGetCurrentUser();
        final IFileManager fileManager = domainModel.getFileManager();
        final FileInformation fileInfo = fileManager.getFileInformationFilestoreUnimportant(fileId);
        if (fileInfo.isFileAvailable() == false)
        {
            throw new FileNotFoundException(fileInfo.getErrorMessage());
        }
        boolean success = false;
        try
        {
            if (fileManager.isControlling(requestUser, fileInfo.getFileDTO()) == false)
            {
                throw new InsufficientPrivilegesException("Insufficient privileges for "
                        + describeUser(requestUser) + ".");
            }
            fileManager.deleteSharingLink(fileId, userCode);
            success = true;
        } finally
        {
            userActionLog.logDeleteSharingLink(fileId, userCode, success);
        }
    }

    private void createSharingLink(final long fileId, final String userIdentifiers)
            throws UserFailureException, InvalidSessionException, InsufficientPrivilegesException,
            FileNotFoundException
    {
        final UserDTO requestUser = privGetCurrentUser();
        final IFileManager fileManager = domainModel.getFileManager();
        final FileInformation fileInfo = fileManager.getFileInformation(fileId);
        if (fileInfo.isFileAvailable() == false)
        {
            throw new FileNotFoundException(fileInfo.getErrorMessage());
        }
        if (fileManager.isControlling(requestUser, fileInfo.getFileDTO()) == false)
        {
            throw new InsufficientPrivilegesException("Insufficient privileges for "
                    + describeUser(requestUser) + ".");
        }
        final StringTokenizer stringTokenizer = new StringTokenizer(userIdentifiers, ", \t\n\r\f");
        final List<String> userIdentifierList = new ArrayList<String>();
        while (stringTokenizer.hasMoreTokens())
        {
            userIdentifierList.add(stringTokenizer.nextToken());
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
                    fileManager.shareFilesWith(url, requestUser, userIdentifierList, files,
                            fileInfo.getFileDTO().getComment(), userActionLog);
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
