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

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.cifex.server.business.bo.BusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.bo.IUserBO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
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

    public UserManager(IDAOFactory daoFactory, BusinessObjectFactory boFactory, BusinessContext businessContext)
    {
        super(daoFactory, boFactory, businessContext);
    }

    @Transactional
    public boolean isDatabaseEmpty()
    {
        return daoFactory.getUserDAO().getNumberOfUsers() == 0;
    }
    
    @Transactional
    public final UserDTO tryToFindUser(final String email)
    {
        assert email != null : "Email Adress is null!";

        return daoFactory.getUserDAO().tryFindUserByEmail(email);
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
        for (UserDTO user : expiredUsers)
        {
            try
            {
                boolean success = userDAO.deleteUser(user.getID());
                if (success)
                {
                    if (logger.isInfoEnabled())
                    {
                        logger.info("Expired user [" + user.getUserName() + " - " + user.getEmail()
                                + "] removed from database.");
                    }
                } else
                {
                    logger.warn("Expired user [" + user.getUserName() + " - " + user.getEmail()
                                + "] could not be found in the database.");
                }
            } catch (RuntimeException ex)
            {
                logger.error("Error deleting user [" + user.getUserName() + " - " + user.getEmail() + "].", ex);
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
    public void tryToDeleteUser(String email)
    {
        assert email != null : "User is null";

        IUserDAO userDAO = daoFactory.getUserDAO();
        UserDTO user = userDAO.tryFindUserByEmail(email);
        boolean returnValue = userDAO.deleteUser(user.getID());
        if (logger.isInfoEnabled())
        {
            if (returnValue)
            {
                logger.info("User [" + user.getUserName() + " - " + user.getEmail() + "] deleted from user database.");
            } else
            {
                logger.info("Could not delete User [" + user.getUserName() + " - " + user.getEmail()
                        + "] from user database.");
            }
        }

    }

}
