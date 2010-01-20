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

package ch.systemsx.cisd.cifex.server.business;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.bo.IUserBO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PasswordGenerator;

/**
 * The only <code>IUserManager</code> implementation.
 * 
 * @author Franz-Josef Elmer
 */
class UserManager extends AbstractManager implements IUserManager
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, UserManager.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, UserManager.class);

    private final IAuthenticationService authenticationServiceOrNull;

    public UserManager(final IDAOFactory daoFactory, final IBusinessObjectFactory boFactory,
            final IBusinessContext businessContext,
            IAuthenticationService authenticationServiceOrNull)
    {
        super(daoFactory, boFactory, businessContext);
        this.authenticationServiceOrNull = authenticationServiceOrNull;
    }

    private final static String getUserDescription(final UserDTO user)
    {
        final String fullName = user.getUserFullName();
        String description = StringUtils.isBlank(fullName) ? user.getUserCode() : fullName;
        description += " <" + user.getEmail() + ">";
        return description;
    }

    //
    // IUserManager
    //

    @Transactional
    public final boolean isDatabaseEmpty()
    {
        return daoFactory.getUserDAO().getNumberOfUsers() == 0;
    }

    @Transactional
    public final UserDTO tryFindUserByCode(final String code)
    {
        assert code != null : "User Code is null!";

        final UserDTO userOrNull = daoFactory.getUserDAO().tryFindUserByCode(code);
        fillInDefaultQuotaInformation(userOrNull);
        return userOrNull;
    }

    public UserDTO getUser(final long id)
    {
        try
        {
            final UserDTO user = daoFactory.getUserDAO().getUserById(id);
            fillInDefaultQuotaInformation(user);
            return user;
        } catch (DataAccessException ex)
        {
            throw new IllegalArgumentException("User id=" + id + " not found in database.");
        }
    }

    public List<UserDTO> findUserByEmail(final String email)
    {
        assert email != null : "Given Email Adress is null";

        final List<UserDTO> users = daoFactory.getUserDAO().findUserByEmail(email);
        for (UserDTO user : users)
        {
            fillInDefaultQuotaInformation(user);
        }
        return users;
    }

    @Transactional
    public final UserDTO createUser(final UserDTO user, UserDTO registratorOrNull)
            throws UserFailureException
    {
        boolean success = false;
        try
        {
            final IUserBO userBO = boFactory.createUserBO();
            userBO.defineForCreate(user, registratorOrNull, false);
            userBO.save();
            success = true;
            return userBO.getUser();
        } catch (final DataIntegrityViolationException ex)
        {
            final String msg =
                    "Cannot create user '" + user.getUserCode()
                            + "' since a user with this id already exists in the database.";
            operationLog.error(msg, ex);
            throw new UserFailureException(msg);
        } finally
        {
            businessContext.getUserActionLog().logCreateUser(user, success);
        }
    }

    @Transactional
    public UserDTO createUserAndSendEmail(final UserDTO user, final String password,
            final UserDTO registrator, final String comment, final String basicURL)
            throws UserFailureException, EnvironmentFailureException
    {
        final String finalPassword = getFinalPassword(password);
        user.setPassword(new Password(finalPassword));
        final UserDTO createdUser = createUser(user, registrator);
        sendEmailToNewUser(createdUser, registrator, comment, basicURL, finalPassword);
        return createdUser;
    }

    private void sendEmailToNewUser(final UserDTO user, final UserDTO registrator,
            final String comment, final String basicURL, final String finalPassword)
    {
        if (StringUtils.isEmpty(user.getEmail()))
        {
            operationLog.warn(String.format(
                    "Sending email to user '%s' not possible: email address is empty.", user));
            return;
        }
        try
        {
            final EMailBuilderForNewUser builder =
                    new EMailBuilderForNewUser(businessContext.getMailClient(), registrator, user);
            builder.setURL(getURLForEmail(basicURL));
            builder.setPassword(finalPassword);
            if (StringUtils.isNotBlank(comment))
            {
                builder.setComment(comment);
            }
            builder.sendEMail();
        } catch (final Exception ex)
        {
            final String msg =
                    "Sending email to email '" + user.getEmail() + "' failed: " + ex.getMessage();
            operationLog.error(msg, ex);
            throw new EnvironmentFailureException(msg);
        }
    }

    private String getURLForEmail(final String basicURL)
    {
        final String overrideURL = businessContext.getOverrideURL();
        if (StringUtils.isBlank(overrideURL))
        {
            return basicURL;
        } else
        {
            return overrideURL;
        }
    }

    private String getFinalPassword(final String password)
    {
        if (StringUtils.isBlank(password))
        {
            final PasswordGenerator passwordGenerator = businessContext.getPasswordGenerator();
            return passwordGenerator.generatePassword(10);
        } else
        {
            return password;
        }
    }

    @Transactional
    public final List<UserDTO> listUsers()
    {
        final List<UserDTO> users = daoFactory.getUserDAO().listUsers();
        for (UserDTO user : users)
        {
            fillInDefaultQuotaInformation(user);
        }
        return users;
    }

    @Transactional
    public void deleteExpiredUsers()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final List<UserDTO> expiredUsers = userDAO.listExpiredUsers();
        if (operationLog.isInfoEnabled() && expiredUsers.size() > 0)
        {
            operationLog.info("Found " + expiredUsers.size() + " expired users.");
        }
        RuntimeException firstExceptionOrNull = null;
        for (final UserDTO user : expiredUsers)
        {
            try
            {
                final boolean success = userDAO.deleteUser(user, null);
                if (success)
                {
                    businessContext.getUserSessionInvalidator().invalidateSessionWithUser(user);
                } else
                {
                    operationLog.warn("Expired user [" + getUserDescription(user)
                            + "] could not be found in the database.");
                }
                businessContext.getUserActionLog().logExpireUser(user, success);
            } catch (final RuntimeException ex)
            {
                businessContext.getUserActionLog().logExpireUser(user, false);
                notificationLog
                        .error("Error deleting user [" + getUserDescription(user) + "].", ex);
                if (firstExceptionOrNull == null)
                {
                    firstExceptionOrNull = ex;
                }
            }
        }
        // Rethrow exception, if any
        if (firstExceptionOrNull != null)
        {
            throw firstExceptionOrNull;
        }
    }

    @Transactional
    public final void deleteUser(final long id, final UserDTO requestUser)
            throws IllegalArgumentException
    {
        UserDTO user = null;
        boolean success = false;
        try
        {
            final IUserDAO userDAO = daoFactory.getUserDAO();
            user = userDAO.getUserById(id);
            success = userDAO.deleteUser(user, requestUser.getID());
            if (success)
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("User [" + getUserDescription(user)
                            + "] deleted from user database.");
                }
                businessContext.getUserSessionInvalidator().invalidateSessionWithUser(user);
            } else
            {
                operationLog.warn("Could not delete user [" + getUserDescription(user)
                        + "] from user database.");
            }
        } catch (DataAccessException ex)
        {
            final String msg = String.format("Could not delete user id=%s (id not found)", id);
            operationLog.warn(msg);
            throw new IllegalArgumentException(msg);
        } finally
        {
            if (user != null)
            {
                businessContext.getUserActionLog().logDeleteUser(user, success);
            }
        }
    }

    @Transactional
    public final UserDTO updateUser(final UserDTO userToUpdate, final Password passwordOrNull,
            final UserDTO requestUserOrNull) throws UserFailureException, IllegalArgumentException
    {
        return updateUser(null, userToUpdate, passwordOrNull, requestUserOrNull);
    }

    @Transactional
    public final UserDTO updateUser(final UserDTO oldUserToUpdateOrNull,
            final UserDTO userToUpdate, final Password passwordOrNull,
            final UserDTO requestUserOrNull) throws UserFailureException, IllegalArgumentException
    {
        assert userToUpdate != null : "Unspecified user";

        boolean success = false;
        UserDTO existingUser = null;
        try
        {
            final IUserBO userBO = boFactory.createUserBO();
            userBO.defineForUpdate(oldUserToUpdateOrNull, userToUpdate, passwordOrNull,
                    requestUserOrNull);
            existingUser = userBO.getOldUser();
            userBO.save();
            success = true;
            return userBO.getUser();
        } finally
        {
            businessContext.getUserActionLog().logUpdateUser(existingUser, userToUpdate, success);
        }
    }

    @Transactional
    public final List<UserDTO> listUsersRegisteredBy(final long userId)
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final List<UserDTO> usersRegisteredBy = userDAO.listUsersRegisteredBy(userId);
        for (UserDTO user : usersRegisteredBy)
        {
            fillInDefaultQuotaInformation(user);
        }
        return usersRegisteredBy;
    }

    @Transactional
    public List<UserDTO> listUsersFileSharedWith(final long fileId) throws UserFailureException
    {

        final IUserDAO userDAO = daoFactory.getUserDAO();
        return userDAO.listUsersFileSharedWith(fileId);
    }

    @Transactional
    public void refreshQuotaInformation(UserDTO user)
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        userDAO.refreshQuotaInformation(user);
        fillInDefaultQuotaInformation(user);
    }

    private void fillInDefaultQuotaInformation(UserDTO userOrNull)
    {
        if (userOrNull == null)
        {
            return;
        }
        userOrNull.setCustomMaxFileRetention(userOrNull.getMaxFileRetention() != null);
        if (userOrNull.getMaxFileRetention() == null)
        {
            userOrNull.setMaxFileRetention(businessContext.getMaxFileRetention());
        }
        userOrNull.setCustomMaxUserRetention(userOrNull.getMaxUserRetention() != null);
        if (userOrNull.getMaxUserRetention() == null)
        {
            userOrNull.setMaxUserRetention(businessContext.getMaxUserRetention());
        }
        userOrNull
                .setCustomMaxFileCountPerQuotaGroup(userOrNull.getMaxFileCountPerQuotaGroup() != null);
        if (userOrNull.getMaxFileCountPerQuotaGroup() == null)
        {
            userOrNull.setMaxFileCountPerQuotaGroup(businessContext.getMaxFileCountPerQuotaGroup());
        } else if (userOrNull.getMaxFileCountPerQuotaGroup() <= 0)
        {
            userOrNull.setMaxFileCountPerQuotaGroup(null);
        }
        userOrNull
                .setCustomMaxFileSizePerQuotaGroup(userOrNull.getMaxFileSizePerQuotaGroupInMB() != null);
        if (userOrNull.getMaxFileSizePerQuotaGroupInMB() == null)
        {
            userOrNull.setMaxFileSizePerQuotaGroupInMB(businessContext
                    .getMaxFileSizePerQuotaGroupInMB());
        } else if (userOrNull.getMaxFileSizePerQuotaGroupInMB() <= 0)
        {
            userOrNull.setMaxFileSizePerQuotaGroupInMB(null);
        }
    }

    @Transactional
    public boolean hasUserFilesForDownload(UserDTO user)
    {
        return daoFactory.getUserDAO().hasUserFilesForDownload(user.getID());
    }

    @Transactional
    public void changeUserCode(final String before, final String after) throws UserFailureException
    {
        if (StringUtils.isBlank(before) || StringUtils.isBlank(after))
        {
            throw new UserFailureException("User code cannot be empty.");
        }

        boolean success = false;
        UserDTO oldUserOrNull = null;
        UserDTO newUserOrNull = null;
        try
        {
            final IUserDAO userDAO = daoFactory.getUserDAO();
            // Get old user entry
            oldUserOrNull = userDAO.tryFindUserByCode(before);
            if (oldUserOrNull == null)
            {
                throw new UserFailureException(String.format("User with code %s doesn't exist.",
                        before));
            }
            if (oldUserOrNull.isExternallyAuthenticated())
            {
                throw new UserFailureException(String.format(
                        "User with code %s is externally authenticated.", before));
            }
            newUserOrNull = userDAO.tryFindUserByCode(after);
            if (newUserOrNull != null)
            {
                throw new UserFailureException(String.format("User with code %s already exist.",
                        before));
            }

            userDAO.changeUserCode(before, after);
            success = true;
        } finally
        {
            businessContext.getUserActionLog().logChangeUserCodeUser(before, after, success);
        }
    }

	@Transactional
    public void createExternalUsers(List<String> userIdentifiers)
    {
        if (authenticationServiceOrNull != null
                && authenticationServiceOrNull instanceof NullAuthenticationService == false)
        {
            TableMap<String, UserDTO> existingUsers =
                    UserUtils.createTableMapOfExistingUsersWithUserCodeAsKey(daoFactory
                            .getUserDAO().listUsers());
            List<String> nonexistentUserIds = new ArrayList<String>();
            for (String identifier : userIdentifiers)
            {
                String lowerCaseIdentifier = identifier.toLowerCase();
                if (UserUtils.isUserCodeWithIdPrefix(lowerCaseIdentifier))
                {
                    String userId = UserUtils.extractUserId(lowerCaseIdentifier);
                    final UserDTO userOrNull = existingUsers.tryGet(userId);
                    if (userOrNull == null)
                    {
                        nonexistentUserIds.add(userId);
                    }
                }
            }
            if (nonexistentUserIds.isEmpty() == false)
            {
                String token = authenticationServiceOrNull.authenticateApplication();
                if (token == null)
                {
                    String message =
                            "Authentication of application in external authentocation service failed";
                    operationLog.error(message);
                    throw new EnvironmentFailureException(message);
                }
                for (String userId : nonexistentUserIds)
                {
                    Principal userDetails;
                    try
                    {
                        userDetails = authenticationServiceOrNull.getPrincipal(token, userId);
                    } catch (IllegalArgumentException ex)
                    {
                        userDetails = null;
                    }
                    if (userDetails != null)
                    {
                        createUser(createExternalUser(userDetails.getUserId(), UserUtils
                                .extractDisplayName(userDetails), userDetails.getEmail(),
                                businessContext.isNewExternallyAuthenticatedUserStartActive()),
                                null);
                    }
                }
            }
        }
    }

    @Private
    static UserDTO createExternalUser(String code, String displayName, String email,
            boolean isNewExternallyAuthenticatedUserStartActive)
    {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserCode(code);
        userDTO.setUserFullName(displayName);
        userDTO.setEmail(email);
        userDTO.setPassword(null);
        userDTO.setExternallyAuthenticated(true);
        userDTO.setAdmin(false);
        userDTO.setActive(isNewExternallyAuthenticatedUserStartActive);
        return userDTO;
    }

}
