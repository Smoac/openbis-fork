/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.business.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * Test cases for corresponding {@link UserDAO} class.
 * 
 * @author Basil Neff
 */
@Test(groups =
    { "db", "user" })
public final class UserDAOTest extends AbstractDAOTest
{

    final UserDTO testTemporaryUser = createUser(false, false, null);

    final UserDTO testAdminUser = createUser(true, true, "admin@systemsx.ch");

    final static String getTestUserName()
    {
        return "bneff";
    }

    final static UserDTO createUser(boolean permanent, boolean admin, String email)
    {
        UserDTO user = new UserDTO();
        if (email == null)
        {
            user.setEmail("basil.neff@systemsx.ch");
        } else
        {
            user.setEmail(email);
        }

        user.setUserName(getTestUserName());
        user.setEncryptedPassword("9df6dafa014bb90272bcc6707a0eef87");
        user.setExternallyAuthenticated(false);
        user.setAdmin(admin);
        user.setPermanent(permanent);
        user.setRegistrationDate(new Date(new Long("402218403000").longValue()));
        if (permanent == false)
        {
            user.setExpirationDate(new Date(new Long("1222249782000").longValue()));
        }
        return user;
    }

    @Test
    @Transactional
    public final void testCreateUser()
    {
        IUserDAO userDAO = daoFactory.getUserDAO();
        List<UserDTO> listUsers = userDAO.listUsers();
        try
        {
            // Try with <code>null</code>
            userDAO.createUser(null);
            AssertJUnit.fail("AssertionError not thrown.");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }

        // TODO 2008-01-22, Basil Neff: Test with fields, which are too long.

        assertEquals("ID is not null on the newly created user.", null, testAdminUser.getID());
        userDAO.createUser(testAdminUser);
        assertEquals(listUsers.size() + 1, userDAO.listUsers().size());

        userDAO.createUser(testTemporaryUser);
        assertEquals(listUsers.size() + 2, userDAO.listUsers().size());

        setComplete();
    }

    @Test(dependsOnMethods =
        { "testCreateUser" })
    @Transactional
    public final void testTryFindUserByEmail()
    {
        IUserDAO userDAO = daoFactory.getUserDAO();
        try
        {
            assert userDAO.tryFindUserByEmail(null) != null;
            fail("Email Adress is null");
        } catch (AssertionError e)
        {
            assertEquals("No email specified!", e.getMessage());
        }

        // Unknown Mail Address
        UserDTO testUnknownUserFromDB = userDAO.tryFindUserByEmail("unknown@cisd.user");
        assertEquals("Unknown user is not null", null, testUnknownUserFromDB);

        // Existing admin User
        UserDTO testAdminUserFromDB = userDAO.tryFindUserByEmail(testAdminUser.getEmail());
        assert testAdminUserFromDB != null;
        assert testAdminUserFromDB.getID() != null;
        assert testAdminUserFromDB.getID() > 0;

        assertEquals(testAdminUser, testAdminUserFromDB);

        // Existing Temporary User
        UserDTO testTemporaryUserFromDB = userDAO.tryFindUserByEmail(testTemporaryUser.getEmail());
        assert testTemporaryUserFromDB != null;
        assert testTemporaryUserFromDB.getID() != null;
        assert testTemporaryUserFromDB.getID() > 0;

        assertEquals(testTemporaryUser, testTemporaryUserFromDB);
    }

    @Test(dependsOnMethods =
        { "testTryFindUserByEmail" })
    @Transactional
    public final void testDeleteUser()
    {
        IUserDAO userDAO = daoFactory.getUserDAO();

        List<UserDTO> listUsers = userDAO.listUsers();

        assertFalse(userDAO.removeUser(new Long(-1)));

        assertTrue(userDAO.removeUser(testAdminUser.getID()));
        assertEquals(listUsers.size() - 1, userDAO.listUsers().size());

        assertTrue(userDAO.removeUser(testTemporaryUser.getID()));
        assertEquals(listUsers.size() - 2, userDAO.listUsers().size());
    }

}