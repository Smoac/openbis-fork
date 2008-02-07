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

import java.util.Date;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.utilities.BeanUtils;

/**
 * Test cases for corresponding {@link UserDAO} class.
 * 
 * @author Basil Neff
 */
@Test(groups =
    { "db", "user" })
public final class UserDAOTest extends AbstractDAOTest
{

    final UserDTO testAdminUser = createUser(true, true, "admin", "admin@systemsx.ch", null);

    final UserDTO testPermanentUser = createUser(true, true, "user", "someuser@systemsx.ch", testAdminUser);

    final UserDTO testTemporaryUser =
            createUser(false, false, "tempuser", "someuser@somewhereelse.edu", testPermanentUser);

    final static String getTestUserName()
    {
        return "bneff";
    }

    private void checkUser(final UserDTO expectedUser, final UserDTO actualUser)
    {
        assertNotNull(actualUser.getID());
        assertTrue(actualUser.getID() > 0);
        assertEquals(expectedUser.isAdmin(), actualUser.isAdmin());
        assertEquals(expectedUser.isExternallyAuthenticated(), actualUser.isExternallyAuthenticated());
        assertEquals(expectedUser.isPermanent(), actualUser.isPermanent());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getEncryptedPassword(), actualUser.getEncryptedPassword());
        assertEquals(expectedUser.getExpirationDate(), actualUser.getExpirationDate());
        assertEquals(expectedUser.getUserFullName(), actualUser.getUserFullName());
        assertNotNull(actualUser.getID());
        assertNotNull(actualUser.getRegistrationDate());
    }

    final static UserDTO createUser(boolean permanent, boolean admin, String code, String email, UserDTO registrator)
    {
        UserDTO user = new UserDTO();
        user.setEmail(email);
        user.setUserCode(code);
        user.setUserFullName(getTestUserName());
        user.setEncryptedPassword("9df6dafa014bb90272bcc6707a0eef87");
        user.setExternallyAuthenticated(false);
        user.setAdmin(admin);
        user.setPermanent(permanent);
        user.setRegistrator(registrator);
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
            AssertJUnit.fail("null user not detected");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }

        // TODO 2008-01-22, Basil Neff: Test with fields, which are too long.

        assertEquals("ID is not null on the newly created user.", null, testAdminUser.getID());
        userDAO.createUser(testAdminUser);
        assertEquals(listUsers.size() + 1, userDAO.listUsers().size());

        userDAO.createUser(testPermanentUser);
        assertEquals(listUsers.size() + 2, userDAO.listUsers().size());

        userDAO.createUser(testTemporaryUser);
        assertEquals(listUsers.size() + 3, userDAO.listUsers().size());

        setComplete();
    }

    @Test(dependsOnMethods =
        { "testTryFindUserByCode" })
    @Transactional
    public final void testListUserRegisteredBy()
    {
        IUserDAO userDAO = daoFactory.getUserDAO();

        List<UserDTO> listUsersRegisteredByAdmin = userDAO.listUsersRegisteredBy(testAdminUser.getUserCode());
        assertEquals(1, listUsersRegisteredByAdmin.size());
        assertEqualsUserRegisteredBy(testPermanentUser, listUsersRegisteredByAdmin.get(0));

        List<UserDTO> listUsersRegisteredByPermanent = userDAO.listUsersRegisteredBy(testPermanentUser.getUserCode());
        assertEquals(1, listUsersRegisteredByPermanent.size());
        assertEqualsUserRegisteredBy(testTemporaryUser, listUsersRegisteredByPermanent.get(0));

        setComplete();
    }

    private void assertEqualsUserRegisteredBy(UserDTO expected, UserDTO actual)
    {
        UserDTO expectedForComparison = BeanUtils.createBean(UserDTO.class, expected);
        UserDTO registratorMinimal = new UserDTO();
        registratorMinimal.setID(expected.getRegistrator().getID());
        registratorMinimal.setUserCode(expected.getRegistrator().getUserCode());
        expectedForComparison.setRegistrator(registratorMinimal);
        actual.setRegistrationDate(null);
        assertEquals(expectedForComparison, actual);
    }
    
    @Test(dependsOnMethods =
        { "testCreateUser" }, expectedExceptions = DataIntegrityViolationException.class)
    @Transactional
    public final void testCreateDuplicateUserID()
    {
        IUserDAO userDAO = daoFactory.getUserDAO();
        userDAO.createUser(testPermanentUser);
    }

    @Test(dependsOnMethods =
        { "testCreateDuplicateUserID" })
    @Transactional
    public final void testTryFindUserByCode()
    {
        IUserDAO userDAO = daoFactory.getUserDAO();
        try
        {
            userDAO.tryFindUserByCode(null);
            fail("Email Adress is null");
        } catch (AssertionError e)
        {
            assertEquals("No code specified!", e.getMessage());
        }

        // Unknown Mail Address
        UserDTO testUnknownUserFromDB = userDAO.tryFindUserByCode("unknown");
        assertEquals("Unknown user is not null", null, testUnknownUserFromDB);

        // Existing admin User
        UserDTO testAdminUserFromDB = userDAO.tryFindUserByCode(testAdminUser.getUserCode());
        assert testAdminUserFromDB != null;
        assert testAdminUserFromDB.getID() != null;
        assert testAdminUserFromDB.getID() > 0;

        checkUser(testAdminUser, testAdminUserFromDB);

        // Existing Temporary User
        UserDTO testTemporaryUserFromDB = userDAO.tryFindUserByCode(testTemporaryUser.getUserCode());
        assert testTemporaryUserFromDB != null;
        assert testTemporaryUserFromDB.getID() != null;
        assert testTemporaryUserFromDB.getID() > 0;

        checkUser(testTemporaryUser, testTemporaryUserFromDB);
    }

    @Test(dependsOnMethods =
        { "testTryFindUserByCode" })
    @Transactional
    public final void testDeleteUser()
    {
        IUserDAO userDAO = daoFactory.getUserDAO();

        List<UserDTO> listUsers = userDAO.listUsers();

        assertFalse(userDAO.deleteUser(new Long(-1)));

        assertTrue(userDAO.deleteUser(testAdminUser.getID()));
        assertEquals(listUsers.size() - 1, userDAO.listUsers().size());

        assertTrue(userDAO.deleteUser(testTemporaryUser.getID()));
        assertEquals(listUsers.size() - 2, userDAO.listUsers().size());
    }

}