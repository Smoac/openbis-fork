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
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.bo.IUserBO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * The only <code>IUserManager</code> implementation.
 * 
 * @author Franz-Josef Elmer
 */
class UserManager extends AbstractManager implements IUserManager
{
    private static final Logger logger = LogFactory.getLogger(LogCategory.OPERATION, UserManager.class);

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

    @Transactional
    public final void createUser(final UserDTO user)
    {
        final IUserBO userBO = boFactory.createUserBO();
        userBO.define(user);
        userBO.save();
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
        if (logger.isInfoEnabled() && expiredUsers.size() > 0)
        {
            logger.info("Found " + expiredUsers.size() + " expired users.");
        }
        RuntimeException ex_all = null;
        for (final UserDTO user : expiredUsers)
        {
            try
            {
                final boolean success = userDAO.deleteUser(user.getID());
                if (success)
                {
                    if (logger.isInfoEnabled())
                    {
                        logger.info("Expired user [" + getUserDescription(user) + "] removed from database.");
                    }
                    businessContext.getUserHttpSessionHolder().invalidateSessionWithUser(user);
                } else
                {
                    logger.warn("Expired user [" + getUserDescription(user) + "] could not be found in the database.");
                }
            } catch (final RuntimeException ex)
            {
                logger.error("Error deleting user [" + getUserDescription(user) + "].", ex);
                if (ex_all == null)
                {
                    ex_all = ex;
                }
            }
        }
        // Rethrow exception, if any
        if (ex_all != null)
        {
            throw ex_all;
        }
    }

    @Transactional
    public final void deleteUser(final String userCode) throws UserFailureException
    {
        assert userCode != null : "User is null";

        final IUserDAO userDAO = daoFactory.getUserDAO();
        final UserDTO userOrNull = userDAO.tryFindUserByCode(userCode);
        if (userOrNull != null)
        {
            final boolean userSuccesfullyDeletedFromDatabase = userDAO.deleteUser(userOrNull.getID());
            if (userSuccesfullyDeletedFromDatabase)
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("User [" + getUserDescription(userOrNull) + "] deleted from user database.");
                }
                businessContext.getUserHttpSessionHolder().invalidateSessionWithUser(userOrNull);
            } else
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("Could not delete User [" + getUserDescription(userOrNull) + "] from user database.");
                }
            }
        } else if (logger.isInfoEnabled())
        {
            final String msg = String.format("Could not delete user '%s' (user not found)", userCode);
            logger.info(msg);
            throw new UserFailureException(msg);
        }
    }

    @Transactional
    public final void updateUser(final UserDTO user, final String encryptedPassword)
    {
        assert user != null : "Unspecified user";

        final IUserDAO userDAO = daoFactory.getUserDAO();
        // Get existing user
        final UserDTO existingUser = userDAO.tryFindUserByCode(user.getUserCode());
        assert existingUser != null;
        assert existingUser.getUserCode().equals(user.getUserCode()) : "User code can not be changed";

        user.setID(existingUser.getID());

        // Permanent User can not get temporary user.
        if (existingUser.isPermanent() == true && user.isPermanent() == false)
        {
            user.setPermanent(true);
        }

        // Renew the expiration Date
        if (user.isPermanent() == false)
        {
            user.setExpirationDate(DateUtils.addMinutes(new Date(), businessContext.getUserRetention()));
        }

        // Password, renew it or leave it as it is
        if (encryptedPassword != null && encryptedPassword.equals("") == false)
        {
            user.setEncryptedPassword(encryptedPassword);
        } else
        {
            user.setEncryptedPassword(existingUser.getEncryptedPassword());
        }

        userDAO.updateUser(user);

    }

    @Transactional
    public final List<UserDTO> listUsersRegisteredBy(final UserDTO user)
    {
        assert user != null : "Unspecified user";

        final IUserDAO userDAO = daoFactory.getUserDAO();
        return userDAO.listUsersRegisteredBy(user.getUserCode());
    }

}
