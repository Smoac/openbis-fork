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

    public UserManager(IDAOFactory daoFactory, IBusinessObjectFactory boFactory, IBusinessContext businessContext)
    {
        super(daoFactory, boFactory, businessContext);
    }

    @Transactional
    public boolean isDatabaseEmpty()
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
        IUserBO userBO = boFactory.createUserBO();
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
        IUserDAO userDAO = daoFactory.getUserDAO();
        List<UserDTO> expiredUsers = userDAO.listExpiredUsers();
        if (logger.isInfoEnabled() && expiredUsers.size() > 0)
        {
            logger.info("Found " + expiredUsers.size() + " expired users.");
        }
        RuntimeException ex_all = null;
        for (final UserDTO user : expiredUsers)
        {
            try
            {
                boolean success = userDAO.deleteUser(user.getID());
                if (success)
                {
                    if (logger.isInfoEnabled())
                    {
                        logger.info("Expired user [" + user.getUserFullName() + " - " + user.getEmail()
                                + "] removed from database.");
                    }
                    businessContext.getUserHttpSessionHolder().invalidateSessionWithUser(user);
                } else
                {
                    logger.warn("Expired user [" + user.getUserFullName() + " - " + user.getEmail()
                            + "] could not be found in the database.");
                }
            } catch (RuntimeException ex)
            {
                logger.error("Error deleting user [" + user.getUserFullName() + " - " + user.getEmail() + "].", ex);
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
    public void deleteUser(String userCode) throws UserFailureException
    {
        assert userCode != null : "User is null";

        IUserDAO userDAO = daoFactory.getUserDAO();
        UserDTO userOrNull = userDAO.tryFindUserByCode(userCode);
        if (userOrNull != null)
        {
            boolean userSuccesfullyDeletedFromDatabase = userDAO.deleteUser(userOrNull.getID());

            if (userSuccesfullyDeletedFromDatabase)
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("User [" + userOrNull.getUserFullName() + " - " + userOrNull.getEmail()
                            + "] deleted from user database.");
                }
                businessContext.getUserHttpSessionHolder().invalidateSessionWithUser(userOrNull);
            } else
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("Could not delete User [" + userOrNull.getUserFullName() + " - " + userOrNull.getEmail()
                            + "] from user database.");
                }
            }
        } else if (logger.isInfoEnabled())
        {
            final String msg = String.format("Could not delete user '%s' (user not found)", userCode);
            logger.info(msg);
            throw new UserFailureException(msg);
        }

    }

    public void updateUser(UserDTO user, String encryptedPassword)
    {
        assert user != null;

        IUserDAO userDAO = daoFactory.getUserDAO();
        // Get existing user
        UserDTO existingUser = userDAO.tryFindUserByCode(user.getUserCode());
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

}
