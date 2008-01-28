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
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * The only <code>IUserManager</code> implementation.
 * 
 * @author Franz-Josef Elmer
 */
public class UserManager extends AbstractManager implements IUserManager
{

    private final IUserDAO userDAO;

    private final int userRetentionInMinutes;

    public UserManager(final IDAOFactory daoFactory, final int userRetentionInMinutes)
    {
        super(daoFactory);
        userDAO = daoFactory.getUserDAO();
        this.userRetentionInMinutes = userRetentionInMinutes;
    }

    //
    // IUserManager
    //

    @Transactional
    public final UserDTO tryToFindUser(final String email)
    {
        assert email != null : "Email Adress is null!";

        return userDAO.tryFindUserByEmail(email);
    }

    @Transactional
    public final void createUser(final UserDTO user)
    {
        assert user != null : "Given user can not be null.";
        assert user.getID() == null : "User ID is set, this will be done from the UserDAO.";
        assert user.getExpirationDate() == null : "Expiration date should not have been specified yet.";

        if (user.isPermanent() == false)
        {
            user.setExpirationDate(DateUtils.addMinutes(new Date(), userRetentionInMinutes));
        }
        userDAO.createUser(user);
    }

    @Transactional
    public final List<UserDTO> listUsers()
    {
        return userDAO.listUsers();
    }

    public void deleteExpiredUsers()
    {
        userDAO.deleteExpiredUsers();
    }

}
