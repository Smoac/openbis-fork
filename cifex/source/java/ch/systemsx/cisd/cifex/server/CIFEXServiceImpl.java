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
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.client.FileNotFoundException;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.InsufficientPrivilegesException;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.UserNotFoundException;
import ch.systemsx.cisd.cifex.client.dto.AdminFileInfoDTO;
import ch.systemsx.cisd.cifex.client.dto.Configuration;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;
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
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileUploadFeedback;
import ch.systemsx.cisd.cifex.shared.basic.dto.Message;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.utilities.BeanUtils;

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

    private final static boolean DOWNLOAD = true;

    private final static boolean UPLOAD = false;

    public CIFEXServiceImpl(final IDomainModel domainModel,
            final IRequestContextProvider requestContextProvider,
            final IUserActionLog userBehaviorLog,
            final IAuthenticationService externalAuthenticationService,
            final int sessionExpirationPeriodMinutes)
    {
        super(domainModel, requestContextProvider, userBehaviorLog, externalAuthenticationService,
                createLoggingContextHandler(requestContextProvider), sessionExpirationPeriodMinutes);
    }

    public final boolean showSwitchToExternalOption(final UserInfoDTO user)
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

    private void updateCurrentUser(String userID) throws InvalidSessionException
    {
        if (userID.equals(privGetCurrentUser().getUserCode()) == false)
        {
            return;
        }
        UserDTO user = domainModel.getUserManager().tryFindUserByCode(userID);
        assert user != null : "Just updated user '" + userID + "' does not exist in database.";
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

    public final UserInfoDTO tryLogin(final String userCode, final String plainPassword)
            throws EnvironmentFailureException
    {
        try
        {
            final UserDTO userDTOOrNull = super.tryLoginUser(userCode, plainPassword, true);
            if (userDTOOrNull == null)
            {
                ConcurrencyUtilities.sleep(DELAY_AFTER_FAILED_LOGIN_MILLIS);
            }
            return BeanUtils.createBean(UserInfoDTO.class, userDTOOrNull);
        } catch (ch.systemsx.cisd.common.exceptions.EnvironmentFailureException ex)
        {
            throw new EnvironmentFailureException(ex.getMessage());
        }
    }

    public final Configuration getConfiguration() throws InvalidSessionException
    {
        return BeanUtils.createBean(Configuration.class, domainModel.getBusinessContext());
    }

    public final UserInfoDTO getCurrentUser() throws InvalidSessionException
    {
        return BeanUtils.createBean(UserInfoDTO.class, privGetCurrentUser());
    }

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
            userDTOOrNull.setPermanent(true);
            userDTOOrNull.setRegistrator(null);

            userManager.updateUser(userDTOOrNull, null);

            copyUserDetailsExceptCode(privGetCurrentUser(), userDTOOrNull); // updating session

            if (userBehaviorLogOrNull != null)
            {
                userBehaviorLogOrNull.logSwitchToExternalAuthentication(userCode, true);
            }
            return BeanUtils.createBean(UserInfoDTO.class, userDTOOrNull);
        } else
        {
            userBehaviorLogOrNull.logSwitchToExternalAuthentication(userCode, false);
            throw new InsufficientPrivilegesException("Password incorrect.");
        }

    }

    private void ensureUserIsNotExternallyAuthenticated(final String userCode,
            final UserDTO userDTOOrNull) throws EnvironmentFailureException
    {
        if (userDTOOrNull.isExternallyAuthenticated())
        {
            if (userBehaviorLogOrNull != null)
            {
                userBehaviorLogOrNull.logSwitchToExternalAuthentication(userCode, false);
            }
            throw new EnvironmentFailureException(String.format(
                    "User with code '%s' is already authenticated externally.", userCode));
        }
    }

    private void ensureUserExistsInDatabase(final String userCode, final UserDTO userDTOOrNull)
            throws EnvironmentFailureException
    {
        if (userDTOOrNull == null)
        {
            if (userBehaviorLogOrNull != null)
            {
                userBehaviorLogOrNull.logSwitchToExternalAuthentication(userCode, false);
            }
            throw new EnvironmentFailureException(String.format(
                    "User with code '%s' does not exist.", userCode));
        }
    }

    private void ensureHasExternalAuthentication(final String userCode)
            throws EnvironmentFailureException
    {
        if (hasExternalAuthenticationService() == false)
        {
            if (userBehaviorLogOrNull != null)
            {
                userBehaviorLogOrNull.logSwitchToExternalAuthentication(userCode, false);
            }
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

    public final UserInfoDTO[] listUsers() throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        checkAdmin("listUsers");
        final List<UserDTO> users = domainModel.getUserManager().listUsers();
        return BeanUtils.createBeanArray(UserInfoDTO.class, users, null);
    }

    public void createUser(final UserInfoDTO user, final String password,
            final UserInfoDTO registratorOrNull, final String comment)
            throws EnvironmentFailureException, InvalidSessionException,
            InsufficientPrivilegesException, UserFailureException
    {
        checkCreateUserAllowed(user);
        ensureCodeIsValid(user.getUserCode());
        final IUserManager userManager = domainModel.getUserManager();

        if (couldCreateUserFromExternalAuthenticationService(user))
        {
            return;
        }

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

    private void ensureCodeIsValid(final String code)
    {
        final Pattern userCodePattern = Pattern.compile("^" + Constants.USER_CODE_REGEX);
        if (code == null || code.length() == 0 || userCodePattern.matcher(code).matches() == false)
        {
            throw new IllegalArgumentException("Invalid user code. "
                    + Constants.VALID_USER_CODE_DESCRIPTION);
        }
    }

    private boolean couldCreateUserFromExternalAuthenticationService(final UserInfoDTO user)
            throws EnvironmentFailureException
    {
        if (hasExternalAuthenticationService() == false)
        {
            return false;
        }
        final String applicationToken = externalAuthenticationService.authenticateApplication();
        final String userOrEmail = user.getUserCode();
        if (applicationToken == null)
        {
            final String msg =
                    "Authentication of the application at the external authentication service failed.";
            throw new EnvironmentFailureException(msg);
        }

        final Principal principalOrNull =
                tryGetUserFromExternalService(applicationToken, userOrEmail);
        if (principalOrNull != null)
        {
            createOrUpdateUserFromExternalAuthenticationService(principalOrNull, user);
            return true;
        } else
        {
            return false;
        }
    }

    private Principal tryGetUserFromExternalService(final String applicationToken,
            final String userOrEmail)
    {
        try
        {
            return externalAuthenticationService.getPrincipal(applicationToken, userOrEmail);
        } catch (final IllegalArgumentException e)
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

    public AdminFileInfoDTO[] listFiles() throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        checkAdmin("listFiles");
        final List<FileDTO> files = domainModel.getFileManager().listFiles();
        return BeanUtils.createBeanArray(AdminFileInfoDTO.class, files, null);
    }

    public final FileInfoDTO[] listDownloadFiles() throws InvalidSessionException
    {
        return listFiles(DOWNLOAD);
    }

    public final FileInfoDTO[] listUploadedFiles() throws InvalidSessionException
    {
        return listFiles(UPLOAD);
    }

    private final FileInfoDTO[] listFiles(final boolean showDownload)
            throws InvalidSessionException
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
        return BeanUtils.createBeanArray(FileInfoDTO.class, files, null);
    }

    public void deleteUser(final String code) throws InvalidSessionException,
            InsufficientPrivilegesException, UserNotFoundException
    {
        final UserDTO user = domainModel.getUserManager().tryFindUserByCode(code);
        checkUpdateOfUserIsAllowed(user, user);
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
    public void updateUser(final UserInfoDTO user, final String plainPassword,
            final boolean sendUpdateInformationToUser) throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException
    {
        assert user != null : "User can't be null";
        final IUserManager userManager = domainModel.getUserManager();
        final UserDTO newUserDTO = BeanUtils.createBean(UserDTO.class, user);
        final UserDTO oldUserDTO = userManager.tryFindUserByCode(user.getUserCode());

        checkUpdateOfUserIsAllowed(oldUserDTO, newUserDTO);

        userManager.updateUser(oldUserDTO, newUserDTO, new Password(plainPassword));
        updateCurrentUser(newUserDTO.getUserCode());
        if (sendUpdateInformationToUser)
        {
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
    }

    /**
     * Changes the user code from <var>before</var> to <var>after</var>.
     */
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
            userManager.changeUserCode(before, after);
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

    @Private
    public static void dummyMethod()
    {
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
        // Only admins may change the 'active' flag
        if (userToUpdate.isActive() != oldUser.isActive())
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

    public UserInfoDTO tryFindUserByUserCode(final String userCode) throws InvalidSessionException
    {
        privGetCurrentUser();
        final IUserManager userManager = domainModel.getUserManager();
        final UserDTO userDTO = userManager.tryFindUserByCode(userCode);
        return BeanUtils.createBean(UserInfoDTO.class, userDTO);
    }

    public UserInfoDTO[] tryFindUserByEmail(final String email) throws InvalidSessionException
    {
        privGetCurrentUser();
        final IUserManager userManager = domainModel.getUserManager();
        final List<UserDTO> users = userManager.tryFindUserByEmail(email);
        return BeanUtils.createBeanArray(UserInfoDTO.class, users);
    }

    public UserInfoDTO[] listUsersRegisteredBy(final String userCode)
            throws InvalidSessionException
    {
        privGetCurrentUser();
        final IUserManager userManager = domainModel.getUserManager();

        final List<UserDTO> users = userManager.listUsersRegisteredBy(userCode);

        return BeanUtils.createBeanArray(UserInfoDTO.class, users, null);
    }

    public void updateFileExpiration(final String idStr) throws InvalidSessionException,
            FileNotFoundException
    {
        final IFileManager fileManager = domainModel.getFileManager();
        final long fileId = Long.parseLong(idStr);
        final FileInformation fileInformation = fileManager.getFileInformation(fileId);
        if (fileInformation.isFileAvailable() == false)
        {
            throw new FileNotFoundException(fileInformation.getErrorMessage());
        }
        fileManager.updateFileExpiration(fileId);
    }

    public UserInfoDTO[] listUsersFileSharedWith(final String idStr) throws InvalidSessionException
    {
        privGetCurrentUser();
        final IUserManager userManager = domainModel.getUserManager();

        final List<UserDTO> users = userManager.listUsersFileSharedWith(Long.parseLong(idStr));

        return BeanUtils.createBeanArray(UserInfoDTO.class, users, null);
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

    public void createSharingLink(final String idStr, final String userIdentifiers)
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
                            fileInfo.getFileDTO().getComment());
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
