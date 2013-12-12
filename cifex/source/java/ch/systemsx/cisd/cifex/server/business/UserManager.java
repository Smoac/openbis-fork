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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import ch.systemsx.cisd.cifex.server.business.exception.ExternalAuthenticationServiceNotConfiguredForExternalUsersException;
import ch.systemsx.cisd.cifex.server.business.exception.ExternalUserNotFoundInExternalAuthenticationServiceException;
import ch.systemsx.cisd.cifex.server.business.exception.UserNotFoundException;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.collection.TableMapNonUniqueKey;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.security.PasswordGenerator;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;

/**
 * The (only) <code>IUserManager</code> implementation.
 * 
 * @author Franz-Josef Elmer
 */
class UserManager extends AbstractManager implements IUserManager
{
    static final int PASSWORD_LENGTH = 20;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            UserManager.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            UserManager.class);

    private final IAuthenticationService authenticationService;

    public UserManager(final IDAOFactory daoFactory, final IBusinessObjectFactory boFactory,
            final IBusinessContext businessContext, IAuthenticationService authenticationService)
    {
        this(daoFactory, boFactory, businessContext, authenticationService,
                SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    public UserManager(final IDAOFactory daoFactory, final IBusinessObjectFactory boFactory,
            final IBusinessContext businessContext,
            final IAuthenticationService authenticationService, final ITimeProvider timeProvider)
    {
        super(daoFactory, boFactory, businessContext, timeProvider);
        assert authenticationService != null;
        this.authenticationService = authenticationService;
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

    @Override
    @Transactional
    public final boolean isDatabaseEmpty()
    {
        return daoFactory.getUserDAO().getNumberOfUsers() == 0;
    }

    @Override
    @Transactional
    public final UserDTO tryFindUserByCode(final String code)
    {
        assert code != null : "User Code is null!";

        final UserDTO userOrNull = daoFactory.getUserDAO().tryFindUserByCode(code);
        fillInDefaultQuotaInformation(userOrNull);
        return userOrNull;
    }

    @Override
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

    @Override
    public List<UserDTO> findUserByEmail(final String email)
    {
        assert email != null : "Given Email Address is null";

        final List<UserDTO> users =
                daoFactory.getUserDAO().findUserByEmail(email.toLowerCase().toLowerCase());
        for (UserDTO user : users)
        {
            fillInDefaultQuotaInformation(user);
        }
        return users;
    }

    @Override
    @Transactional
    public final UserDTO createUser(final UserDTO user, UserDTO registratorOrNull)
            throws UserFailureException
    {
        try
        {
            final IUserBO userBO = boFactory.createUserBO();
            userBO.defineForCreate(user, registratorOrNull, false);
            userBO.save();
            return userBO.getUser();
        } catch (final DataIntegrityViolationException ex)
        {
            final Throwable rootCauseOrNull = ex.getRootCause();
            final String msg;
            if (rootCauseOrNull != null && ex.getMessage().contains("value too long"))
            {
                msg =
                        "Cannot create user '" + user.getUserCode()
                                + "': length constraint exceeded.";
            } else
            {
                msg = "Cannot create user '" + user.getUserCode() + "': user exists.";
            }
            operationLog.error(msg, ex);
            throw new UserFailureException(msg);
        }
    }

    @Override
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
            return passwordGenerator.generatePassword(PASSWORD_LENGTH);
        } else
        {
            return password;
        }
    }

    @Override
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

    @Override
    @Transactional
    public void deleteExpiredUsers(final IUserActionLog logOrNull)
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
                if (logOrNull != null)
                {
                    logOrNull.logExpireUser(user, success);
                }
            } catch (final RuntimeException ex)
            {
                if (logOrNull != null)
                {
                    logOrNull.logExpireUser(user, false);
                }
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

    @Override
    @Transactional
    public final void deleteUser(final long id, final UserDTO requestUser,
            final IUserActionLog logOrNull) throws IllegalArgumentException
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
            if (user != null && logOrNull != null)
            {
                logOrNull.logDeleteUser(user, success);
            }
        }
    }

    @Override
    @Transactional
    public final UserDTO updateUser(final UserDTO userToUpdate, final Password passwordOrNull,
            final UserDTO requestUserOrNull, final IUserActionLog logOrNull)
            throws UserFailureException, IllegalArgumentException
    {
        return updateUser(null, userToUpdate, passwordOrNull, requestUserOrNull, logOrNull);
    }

    @Override
    @Transactional
    public final UserDTO updateUser(final UserDTO oldUserToUpdateOrNull,
            final UserDTO userToUpdate, final Password passwordOrNull,
            final UserDTO requestUserOrNull, final IUserActionLog logOrNull)
            throws UserFailureException, IllegalArgumentException
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
            if (existingUser == null)
            {
                existingUser =
                        (oldUserToUpdateOrNull != null) ? oldUserToUpdateOrNull : userToUpdate;
            }
            if (logOrNull != null)
            {
                logOrNull.logUpdateUser(existingUser, userToUpdate, success);
            }
        }
    }

    @Override
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

    @Override
    @Transactional
    public List<UserDTO> listUsersFileSharedWith(final long fileId) throws UserFailureException
    {

        final IUserDAO userDAO = daoFactory.getUserDAO();
        return userDAO.listUsersFileSharedWith(fileId);
    }

    @Override
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

    @Override
    @Transactional
    public boolean hasUserFilesForDownload(UserDTO user)
    {
        return daoFactory.getUserDAO().hasUserFilesForDownload(user.getID());
    }

    @Override
    @Transactional
    public void changeUserCode(final String before, final String after) throws UserFailureException
    {
        if (StringUtils.isBlank(before) || StringUtils.isBlank(after))
        {
            throw new UserFailureException("User code cannot be empty.");
        }

        UserDTO oldUserOrNull = null;
        UserDTO newUserOrNull = null;
        final IUserDAO userDAO = daoFactory.getUserDAO();
        // Get old user entry
        oldUserOrNull = userDAO.tryFindUserByCode(before);
        if (oldUserOrNull == null)
        {
            throw new UserFailureException(
                    String.format("User with code %s doesn't exist.", before));
        }
        if (oldUserOrNull.isExternallyAuthenticated())
        {
            throw new UserFailureException(String.format(
                    "User with code %s is externally authenticated.", before));
        }
        newUserOrNull = userDAO.tryFindUserByCode(after);
        if (newUserOrNull != null)
        {
            throw new UserFailureException(
                    String.format("User with code %s already exist.", before));
        }
        userDAO.changeUserCode(before, after);
    }

    @Override
    @Transactional
    /**
        Searching by USER_NAME
        ======================
    
        Example: "id:test"
    
        Algorithm:
    
        IF (found a user by USER_NAME in the local DB):
            IF (is_externally_authenticated):
                IF (found a user by USER_NAME in the external DB):
                    Take the user from the external DB
                ELSE:
                    Error - user probably has left ETHZ
            ELSE:
                Take the user from the local DB
        ELSE:
            IF (found a user by USER_NAME in the external DB):
                Create a new user in the local DB
            ELSE:
                Error - incorrect user name
    
        Searching by EMAIL
        ==================
    
        Example: "test@ethz.ch"
        
        Algorithm:
    
        IF (found a user by EMAIL in the local DB):
            IF (is_externally_authenticated):
                IF (found a user by USER_NAME in the external DB):
                    Take the user from the external DB
                ELSE:
                    Error - user probably has left ETHZ
            ELSE:
                Take the user from the local DB
        ELSE:
            IF (found a user by EMAIL in the external DB):
                Take the user from the external DB
                IF (did not find a user by USER_NAME in the local DB):
                    Create a new user 
            ELSE:
                Return null

     */
    public Collection<UserDTO> getUsers(List<String> userCodesOrNull,
            List<String> emailAddressesOrNull, final IUserActionLog logOrNull)
    {
        Collection<UserDTO> usersByCodes = getUsersByCodes(userCodesOrNull, logOrNull);
        Collection<UserDTO> usersByEmails = getUsersByEmails(emailAddressesOrNull, logOrNull);

        Set<UserDTO> users = new LinkedHashSet<UserDTO>();
        users.addAll(usersByCodes);
        users.addAll(usersByEmails);

        return users;
    }

    private Collection<UserDTO> getUsersByCodes(List<String> userCodesOrNull, IUserActionLog logOrNull)
    {
        if (userCodesOrNull == null || userCodesOrNull.isEmpty())
        {
            return new LinkedList<UserDTO>();
        }

        if (authenticationService instanceof NullAuthenticationService)
        {
            return getUsersByCodesWithNullAuthenticationService(userCodesOrNull, logOrNull);
        } else
        {
            return getUsersByCodesWithNotNullAuthenticationService(userCodesOrNull, logOrNull);
        }
    }

    private Collection<UserDTO> getUsersByCodesWithNullAuthenticationService(List<String> userCodesOrNull, IUserActionLog logOrNull)
    {
        Collection<UserDTO> usersFromLocalDB = getUsersByCodesFromLocalDB(userCodesOrNull);
        Set<String> externallyAuthenticatedUsersCodes = new TreeSet<String>();

        for (UserDTO userFromLocalDB : usersFromLocalDB)
        {
            if (userFromLocalDB.isExternallyAuthenticated())
            {
                externallyAuthenticatedUsersCodes.add(userFromLocalDB.getUserCode());
            }
        }

        if (false == externallyAuthenticatedUsersCodes.isEmpty())
        {
            throw new ExternalAuthenticationServiceNotConfiguredForExternalUsersException(externallyAuthenticatedUsersCodes);
        }

        return usersFromLocalDB;
    }

    private Collection<UserDTO> getUsersByCodesWithNotNullAuthenticationService(List<String> userCodesOrNull, IUserActionLog logOrNull)
    {
        LinkedHashSet<UserDTO> users = new LinkedHashSet<UserDTO>();

        Collection<UserDTO> usersFromLocalDB = getUsersByCodesFromLocalDB(userCodesOrNull);
        TableMap<String, UserDTO> usersFromLocalDBMap = null;

        if (usersFromLocalDB != null && usersFromLocalDB.isEmpty() == false)
        {
            usersFromLocalDBMap = UserUtils.createTableMapOfExistingUsersWithUserCodeAsKey(usersFromLocalDB);
        }

        for (String userCode : userCodesOrNull)
        {
            UserDTO userFromLocalDB = usersFromLocalDBMap != null ? usersFromLocalDBMap.tryGet(userCode) : null;

            if (userFromLocalDB != null)
            {
                if (userFromLocalDB.isExternallyAuthenticated())
                {
                    UserDTO userFromExternalAuthenticationService = getUserByCodeFromExternalAuthenticationService(userCode);

                    if (userFromExternalAuthenticationService != null)
                    {
                        mergeLocalAndExternalUsers(userFromLocalDB, userFromExternalAuthenticationService);
                        users.add(userFromLocalDB);
                    } else
                    {
                        throw new ExternalUserNotFoundInExternalAuthenticationServiceException(userCode);
                    }
                } else
                {
                    users.add(userFromLocalDB);
                }
            } else
            {
                UserDTO userFromExternalAuthenticationService = getUserByCodeFromExternalAuthenticationService(userCode);

                if (userFromExternalAuthenticationService != null)
                {
                    UserDTO newUser = createLocalUserFromExternalUser(userFromExternalAuthenticationService, logOrNull);
                    users.add(newUser);
                } else
                {
                    throw new UserNotFoundException(userCode);
                }
            }
        }

        return users;
    }

    private Collection<UserDTO> getUsersByEmails(List<String> emailAddressesOrNull, final IUserActionLog logOrNull)
    {
        if (emailAddressesOrNull == null || emailAddressesOrNull.isEmpty())
        {
            return new LinkedList<UserDTO>();
        }

        if (authenticationService instanceof NullAuthenticationService)
        {
            return getUsersByEmailsWithNullAuthenticationService(emailAddressesOrNull, logOrNull);
        } else
        {
            return getUsersByEmailsWithNotNullAuthenticationService(emailAddressesOrNull, logOrNull);
        }
    }

    private Collection<UserDTO> getUsersByEmailsWithNullAuthenticationService(List<String> emailAddressesOrNull, final IUserActionLog logOrNull)
    {
        Collection<UserDTO> usersFromLocalDB = getUsersByEmailsFromLocalDB(emailAddressesOrNull);
        Set<String> externallyAuthenticatedUsersCodes = new TreeSet<String>();

        for (UserDTO userFromLocalDB : usersFromLocalDB)
        {
            if (userFromLocalDB.isExternallyAuthenticated())
            {
                externallyAuthenticatedUsersCodes.add(userFromLocalDB.getUserCode());
            }
        }

        if (externallyAuthenticatedUsersCodes.isEmpty() == false)
        {
            throw new ExternalAuthenticationServiceNotConfiguredForExternalUsersException(externallyAuthenticatedUsersCodes);
        }

        return usersFromLocalDB;
    }

    private Collection<UserDTO> getUsersByEmailsWithNotNullAuthenticationService(List<String> emailAddressesOrNull, final IUserActionLog logOrNull)
    {
        LinkedHashSet<UserDTO> users = new LinkedHashSet<UserDTO>();

        Collection<UserDTO> usersFromLocalDB = getUsersByEmailsFromLocalDB(emailAddressesOrNull);
        TableMapNonUniqueKey<String, UserDTO> usersFromLocalDBMap = null;

        if (usersFromLocalDB != null && usersFromLocalDB.isEmpty() == false)
        {
            usersFromLocalDBMap = UserUtils.createTableMapOfExistingUsersWithEmailAsKey(usersFromLocalDB);
        }

        for (String emailAddress : emailAddressesOrNull)
        {
            Set<UserDTO> usersFromLocalDBForEmail = usersFromLocalDBMap != null ? usersFromLocalDBMap.tryGet(emailAddress) : null;

            if (usersFromLocalDBForEmail == null || usersFromLocalDBForEmail.isEmpty())
            {
                UserDTO userFromExternalAuthenticationService = getUserByEmailFromExternalAuthenticationService(emailAddress);

                if (userFromExternalAuthenticationService != null)
                {
                    Collection<UserDTO> usersFromLocalDBForCode =
                            getUsersByCodesFromLocalDB(Collections.singletonList(userFromExternalAuthenticationService.getUserCode()));

                    if (usersFromLocalDBForCode == null || usersFromLocalDBForCode.isEmpty())
                    {
                        UserDTO newUser = createLocalUserFromExternalUser(userFromExternalAuthenticationService, logOrNull);
                        users.add(newUser);
                    } else
                    {
                        UserDTO userFromLocalDBForCode = usersFromLocalDBForCode.iterator().next();
                        mergeLocalAndExternalUsers(userFromLocalDBForCode, userFromExternalAuthenticationService);
                        users.add(userFromLocalDBForCode);
                    }
                }
            } else
            {
                for (UserDTO userFromLocalDBForEmail : usersFromLocalDBForEmail)
                {
                    if (userFromLocalDBForEmail.isExternallyAuthenticated())
                    {
                        UserDTO userFromExternalAuthenticationService =
                                getUserByCodeFromExternalAuthenticationService(userFromLocalDBForEmail.getUserCode());

                        if (userFromExternalAuthenticationService != null)
                        {
                            mergeLocalAndExternalUsers(userFromLocalDBForEmail, userFromExternalAuthenticationService);
                            users.add(userFromLocalDBForEmail);
                        } else
                        {
                            throw new ExternalUserNotFoundInExternalAuthenticationServiceException(userFromLocalDBForEmail.getUserCode());
                        }
                    } else
                    {
                        users.add(userFromLocalDBForEmail);
                    }
                }
            }

        }

        return users;
    }

    private Collection<UserDTO> getUsersByCodesFromLocalDB(final List<String> userCodes)
    {
        return daoFactory.getUserDAO().listUsersByCode(userCodes.toArray(new String[userCodes.size()]));
    }

    private Collection<UserDTO> getUsersByEmailsFromLocalDB(final List<String> emailAddresses)
    {
        return daoFactory.getUserDAO().listUsersByEmail(emailAddresses.toArray(new String[emailAddresses.size()]));
    }

    private UserDTO getUserByCodeFromExternalAuthenticationService(final String userCode)
    {
        final Principal principalOrNull =
                authenticationService.tryGetAndAuthenticateUser(userCode, null);

        if (principalOrNull != null)
        {
            UserDTO user = createExternalUser(principalOrNull.getUserId(),
                    UserUtils.extractDisplayName(principalOrNull),
                    principalOrNull.getEmail(),
                    businessContext.isNewExternallyAuthenticatedUserStartActive());
            return user;
        } else
        {
            return null;
        }
    }

    private UserDTO getUserByEmailFromExternalAuthenticationService(final String emailAddress)
    {
        if (authenticationService.supportsListingByEmail() == false)
        {
            return null;
        }

        Principal principalOrNull =
                authenticationService.tryGetAndAuthenticateUserByEmail(emailAddress, null);

        if (principalOrNull != null)
        {
            UserDTO user = createExternalUser(principalOrNull.getUserId(),
                    UserUtils.extractDisplayName(principalOrNull),
                    principalOrNull.getEmail(),
                    businessContext.isNewExternallyAuthenticatedUserStartActive());
            if (false == emailAddress.equals(principalOrNull.getEmail()))
            {
                user.setEmailAlias(emailAddress);
            }
            return user;
        } else
        {
            return null;
        }
    }

    private void mergeLocalAndExternalUsers(UserDTO localUser, UserDTO externalUser)
    {
        localUser.setUserFullName(externalUser.getUserFullName());
        localUser.setEmail(externalUser.getEmail());
        localUser.setEmailAlias(externalUser.getEmailAlias());
    }

    private UserDTO createLocalUserFromExternalUser(UserDTO externalUser, final IUserActionLog logOrNull)
    {
        UserDTO newUser = null;
        boolean success = false;
        try
        {
            newUser = createUser(externalUser, null);
            mergeLocalAndExternalUsers(newUser, externalUser);
            success = true;
        } finally
        {
            if (logOrNull != null)
            {
                logOrNull.logCreateUser(newUser, success);
            }
        }
        return newUser;
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

    @Private
    static UserDTO createLocalUser(String code, String displayName, String email)
    {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserCode(code);
        userDTO.setUserFullName(displayName);
        userDTO.setEmail(email);
        userDTO.setPassword(null);
        userDTO.setExternallyAuthenticated(false);
        userDTO.setAdmin(false);
        return userDTO;
    }

}
