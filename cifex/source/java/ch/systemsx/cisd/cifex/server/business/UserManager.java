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

    public void deleteExpiredUsers()
    {
        IUserDAO userDAO = daoFactory.getUserDAO();
        List<UserDTO> expiredUsers = userDAO.listExpiredUsers();
        for (UserDTO user : expiredUsers)
        {
            boolean success = userDAO.removeUser(user.getID());
            if (success)
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("Expired user [" + user.getUserName() + " - " + user.getEmail()
                            + "] removed from user database.");
                }
            } else
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("Expired user [" + user.getUserName() + " - " + user.getEmail()
                            + "] could not be deleted from user database.");
                }
            }
        }
    }

    public void tryToDeleteUser(UserDTO user)
    {
        assert user != null : "User is null";
        assert user.getID() == null : "User ID is not null";

        IUserDAO userDAO = daoFactory.getUserDAO();
        UserDTO userWithID = userDAO.tryFindUserByEmail(user.getEmail());
        boolean returnValue = userDAO.removeUser(userWithID.getID());
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
