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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.bo.IUserBO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
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

    public UserManager(final IDAOFactory daoFactory, final IBusinessObjectFactory boFactory,
            final IBusinessContext businessContext)
    {
        super(daoFactory, boFactory, businessContext);
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

        return daoFactory.getUserDAO().tryFindUserByCode(code);
    }

    public UserDTO tryFindUserByCodeFillRegistrator(String code)
    {
        assert code != null : "User Code is null!";

        final UserDTO user = daoFactory.getUserDAO().tryFindUserByCode(code);
        final UserDTO registrator = daoFactory.getUserDAO().tryFindUserById(user.getRegistrator().getID());
        user.setRegistrator(registrator);
        return user;
    }

    public List<UserDTO> tryFindUserByEmail(final String email)
    {
        assert email != null : "Given Email Adress is null";

        return daoFactory.getUserDAO().tryFindUserByEmail(email);
    }

    @Transactional
    public final void createUser(final UserDTO user) throws UserFailureException
    {
        boolean success = false;
        try
        {
            final IUserBO userBO = boFactory.createUserBO();
            userBO.define(user);
            userBO.save();
            success = true;
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
    public void createUserAndSendEmail(final UserDTO user, final String password,
            final UserDTO registrator, final String comment, final String basicURL)
            throws UserFailureException, EnvironmentFailureException
    {
        final String finalPassword = getFinalPassword(password);
        user.setPassword(new Password(finalPassword));
        user.setRegistrator(registrator);
        createUser(user);
        sendEmailToNewUser(user, registrator, comment, basicURL, finalPassword);
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
        return daoFactory.getUserDAO().listUsers();
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
                final boolean success = userDAO.deleteUser(user.getID());
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
    public final void deleteUser(final String userCode) throws UserFailureException
    {
        assert userCode != null : "userCode is null";

        UserDTO userOrNull = null;
        boolean success = false;
        try
        {
            final IUserDAO userDAO = daoFactory.getUserDAO();
            userOrNull = userDAO.tryFindUserByCode(userCode);
            if (userOrNull != null)
            {
                success = userDAO.deleteUser(userOrNull.getID());
                if (success)
                {
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info("User [" + getUserDescription(userOrNull)
                                + "] deleted from user database.");
                    }
                    businessContext.getUserSessionInvalidator().invalidateSessionWithUser(
                            userOrNull);
                } else
                {
                    operationLog.warn("Could not delete user [" + getUserDescription(userOrNull)
                            + "] from user database.");
                }
            } else
            {
                final String msg =
                        String.format("Could not delete user '%s' (user not found)", userCode);
                operationLog.warn(msg);
                throw new UserFailureException(msg);
            }
        } finally
        {
            if (userOrNull != null)
            {
                businessContext.getUserActionLog().logDeleteUser(userOrNull, success);
            }
        }
    }

    @Transactional
    public final void updateUser(final UserDTO userToUpdate, final Password passwordOrNull)
            throws UserFailureException, IllegalArgumentException
    {
        updateUser(null, userToUpdate, passwordOrNull);
    }

    @Transactional
    public final void updateUser(final UserDTO oldUserToUpdateOrNull, final UserDTO userToUpdate,
            final Password passwordOrNull) throws UserFailureException, IllegalArgumentException
    {
        assert userToUpdate != null : "Unspecified user";

        boolean success = false;
        UserDTO existingUser = null;
        try
        {
            final IUserDAO userDAO = daoFactory.getUserDAO();
            // Get old user entry
            existingUser =
                    oldUserToUpdateOrNull != null ? oldUserToUpdateOrNull : getUserByCode(userDAO,
                            userToUpdate.getUserCode());

            userToUpdate.setID(existingUser.getID());

            checkIllegalModifications(existingUser, userToUpdate);

            // Renew the expiration Date
            if (userToUpdate.isPermanent() == false)
            {
                userToUpdate.setExpirationDate(DateUtils.addMinutes(new Date(), businessContext
                        .getUserRetention()));
            }

            // Password, update it if it has been provided.
            if (Password.isEmpty(passwordOrNull) == false)
            {
                userToUpdate.setPassword(passwordOrNull);
            }

            userDAO.updateUser(userToUpdate);
            success = true;
        } finally
        {
            businessContext.getUserActionLog().logUpdateUser(existingUser, userToUpdate, success);
        }

    }

    private static UserDTO getUserByCode(final IUserDAO userDAO, final String userCode)
            throws UserFailureException
    {
        assert userCode != null;

        final UserDTO existingUser = userDAO.tryFindUserByCode(userCode);
        if (existingUser == null)
        {
            final String msg = String.format("User '%s' does not exist in the database.", userCode);
            throw new UserFailureException(msg);
        }
        assert userCode.equals(existingUser.getUserCode()) : "Mismatch in user code";
        return existingUser;
    }

    private static void checkIllegalModifications(final UserDTO oldUser, final UserDTO newUser)
            throws IllegalArgumentException
    {
        if (oldUser.isPermanent() && newUser.isPermanent() == false)
        {
            throw new IllegalArgumentException("Cannot make a regular user temporary.");
        }
    }

    @Transactional
    public final List<UserDTO> listUsersRegisteredBy(final String userCode)
    {
        assert userCode != null : "Unspecified user";

        final IUserDAO userDAO = daoFactory.getUserDAO();
        return userDAO.listUsersRegisteredBy(userCode);
    }

    @Transactional
    public List<UserDTO> listUsersFileSharedWith(final long fileId) throws UserFailureException
    {

        final IUserDAO userDAO = daoFactory.getUserDAO();
        return userDAO.listUsersFileSharedWith(fileId);
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

}
