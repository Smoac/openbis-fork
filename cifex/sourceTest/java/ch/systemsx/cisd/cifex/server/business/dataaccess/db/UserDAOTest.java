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

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IFileDAO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;

/**
 * Test cases for corresponding {@link UserDAO} class.
 * 
 * @author Basil Neff
 */
@Test(groups =
    { "db", "user" })
public final class UserDAOTest extends AbstractDAOTest
{
    private static final String MY_FILE_001_TXT = "my-file-001.txt";

    final static UserDTO testAdminUser = createUser(true, true, "admin", "admin@systemsx.ch", null);

    final static UserDTO testPermanentUser =
            createUser(true, true, "user", "someuser@systemsx.ch", testAdminUser);

    final static UserDTO testTemporaryUser =
            createUser(false, false, "tempuser", "someuser@somewhereelse.edu", testPermanentUser);

    final static String getTestUserName()
    {
        return "bneff";
    }

    private void checkUser(final UserDTO expectedUser, final UserDTO actualUserFromDB)
    {
        assertNotNull(actualUserFromDB.getID());
        assertTrue(actualUserFromDB.getID() > 0);
        assertEquals(expectedUser.isAdmin(), actualUserFromDB.isAdmin());
        assertEquals(expectedUser.isExternallyAuthenticated(), actualUserFromDB
                .isExternallyAuthenticated());
        assertEquals(expectedUser.isPermanent(), actualUserFromDB.isPermanent());
        assertTrue(expectedUser.getPassword().matches(actualUserFromDB.getPasswordHash()));
        assertEquals(expectedUser.getExpirationDate(), actualUserFromDB.getExpirationDate());
        assertEquals(expectedUser.getUserFullName(), actualUserFromDB.getUserFullName());
        assertNotNull(actualUserFromDB.getID());
        assertNotNull(actualUserFromDB.getRegistrationDate());
    }

    final static UserDTO createUser(final boolean permanent, final boolean admin,
            final String code, final String email, final UserDTO registrator)
    {
        final UserDTO user = new UserDTO();
        user.setEmail(email);
        user.setUserCode(code);
        user.setUserFullName(getTestUserName());
        user.setPassword(new Password("the admin passw0rd"));
        user.setExternallyAuthenticated(false);
        user.setAdmin(admin);
        if (registrator == null)
        {
            user.setRegistrator(new UserDTO());
        } else
        {
            user.setRegistrator(registrator);
        }
        user.setPermanent(permanent);
        if (permanent == false)
        {
            // Set temporary user expiration date to 0 (in the past).
            user.setExpirationDate(new Date(0L));
        }
        return user;
    }

    //
    // 'create' group
    //

    @Test(groups = "user.create", expectedExceptions = AssertionError.class)
    @Transactional
    public final void testCreateUserRainyDay()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        userDAO.createUser(null);

    }

    @SuppressWarnings("unused")
    @DataProvider(name = "userProvider")
    private final static Object[][] getUser()
    {
        return new Object[][]
            {
                { testAdminUser },
                { testPermanentUser },
                { testTemporaryUser } };
    }

    @Test(dataProvider = "userProvider", groups = "user.create")
    @Transactional
    public final void testCreateUser(final UserDTO userDTO)
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final List<UserDTO> listUsers = userDAO.listUsers();
        assertNull(userDTO.getID());
        userDAO.createUser(userDTO);
        assertNotNull(userDTO.getID());
        assertEquals(listUsers.size() + 1, userDAO.listUsers().size());
        setComplete();
    }

    @Test(groups =
        { "user.create" }, dependsOnMethods = "testCreateUser", expectedExceptions = DataAccessException.class)
    @Transactional
    public final void testCreateUserWithTooLong()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final UserDTO newUser =
                createUser(true, false, StringUtils.repeat("A", 51), "u@v.org", testAdminUser);
        userDAO.createUser(newUser);
    }

    @Test(groups = "user.create", dependsOnMethods = "testCreateUser", expectedExceptions = DataIntegrityViolationException.class)
    @Transactional
    public final void testCreateDuplicateUserID()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        userDAO.createUser(testPermanentUser);
    }

    //
    // 'read' group
    //

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    @Transactional
    public final void testTryFindUserByCode()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        try
        {
            userDAO.tryFindUserByCode(null);
            fail("Email Adress is null");
        } catch (final AssertionError e)
        {
            assertEquals("No code specified!", e.getMessage());
        }

        // Unknown Mail Address
        final UserDTO testUnknownUserFromDB = userDAO.tryFindUserByCode("unknown");
        assertEquals("Unknown user is not null", null, testUnknownUserFromDB);

        // Existing admin User
        final UserDTO testAdminUserFromDB = userDAO.tryFindUserByCode(testAdminUser.getUserCode());
        assert testAdminUserFromDB != null;
        assert testAdminUserFromDB.getID() != null;
        assert testAdminUserFromDB.getID() > 0;

        checkUser(testAdminUser, testAdminUserFromDB);

        // Existing Temporary User
        final UserDTO testTemporaryUserFromDB =
                userDAO.tryFindUserByCode(testTemporaryUser.getUserCode());
        assert testTemporaryUserFromDB != null;
        assert testTemporaryUserFromDB.getID() != null;
        assert testTemporaryUserFromDB.getID() > 0;

        checkUser(testTemporaryUser, testTemporaryUserFromDB);
    }

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    @Transactional
    public final void testListUserRegisteredBy()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();

        final List<UserDTO> listUsersRegisteredByAdmin =
                userDAO.listUsersRegisteredBy(testAdminUser.getUserCode());
        assertEquals(1, listUsersRegisteredByAdmin.size());
        UserDTO actual = listUsersRegisteredByAdmin.get(0);
        assertEquals(testPermanentUser.getID(), actual.getID());

        final List<UserDTO> listUsersRegisteredByPermanent =
                userDAO.listUsersRegisteredBy(testPermanentUser.getUserCode());
        assertEquals(1, listUsersRegisteredByPermanent.size());
        actual = listUsersRegisteredByPermanent.get(0);
        assertEquals(testTemporaryUser.getID(), actual.getID());

        final List<UserDTO> listUsersRegisteredByTemporary =
                userDAO.listUsersRegisteredBy(testTemporaryUser.getUserCode());
        assertEquals(0, listUsersRegisteredByTemporary.size());
    }

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    @Transactional
    public final void testGetNumberOfUsers()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        assertEquals(3, userDAO.getNumberOfUsers());
    }

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    @Transactional
    public final void testListExpiredUsers()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final List<UserDTO> expiredUsers = userDAO.listExpiredUsers();
        assertEquals(1, expiredUsers.size());
        assertEquals(testTemporaryUser.getID(), expiredUsers.get(0).getID());
    }

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    @Transactional
    public final void testListUsers()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        assertEquals(3, userDAO.listUsers().size());
    }

    //
    // 'update' group
    //

    @Test(dependsOnGroups =
        { "user.read" }, groups = "user.update")
    @Transactional
    public final void testUpdateUser()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();

        // No change
        userDAO.updateUser(testAdminUser);
        final UserDTO testAdminUserFromDB = userDAO.tryFindUserByCode(testAdminUser.getUserCode());
        checkUser(testAdminUser, testAdminUserFromDB);

        // Try Update Email
        testTemporaryUser.setEmail("updated@temporary.cifex");
        userDAO.updateUser(testTemporaryUser);
        final UserDTO testTemporaryUserFromDB =
                userDAO.tryFindUserByCode(testTemporaryUser.getUserCode());
        checkUser(testTemporaryUser, testTemporaryUserFromDB);

        // Try update Password
        testPermanentUser.setPassword(new Password("NewPassword"));
        testPermanentUser.setAdmin(true);
        testPermanentUser.setExternallyAuthenticated(false);
        testPermanentUser.setUserFullName("User Full Name");
        userDAO.updateUser(testPermanentUser);
        UserDTO testPermanentUserFromDB =
                userDAO.tryFindUserByCode(testPermanentUser.getUserCode());
        checkUser(testPermanentUser, testPermanentUserFromDB);

        // Remove admin Permissions of permanent user
        testPermanentUser.setAdmin(false);
        userDAO.updateUser(testPermanentUser);
        testPermanentUserFromDB = userDAO.tryFindUserByCode(testPermanentUser.getUserCode());
        checkUser(testPermanentUser, testPermanentUserFromDB);
    }

    //
    // 'delete' group
    //

    @Test(dependsOnGroups =
        { "user.update" }, groups =
        { "user.delete" })
    @Transactional
    public final void testDeleteUser()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();

        final List<UserDTO> listUsers = userDAO.listUsers();

        assertFalse(userDAO.deleteUser(-1));

        assertTrue(userDAO.deleteUser(testAdminUser.getID()));
        assertEquals(listUsers.size() - 1, userDAO.listUsers().size());

        assertTrue(userDAO.deleteUser(testTemporaryUser.getID()));
        assertEquals(listUsers.size() - 2, userDAO.listUsers().size());
    }

    @Test(dependsOnGroups =
        { "user.create" })
    @Transactional
    public final void testListUsersFileSharedWith()
    {
        final IFileDAO fileDAO = daoFactory.getFileDAO();
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final List<UserDTO> users = userDAO.listUsers();
        Assert.assertTrue(users.size() > 0);
        final UserDTO userDTO = users.get(0);

        final FileDTO fileDTO = new FileDTO(userDTO.getID());
        fileDTO.setName(MY_FILE_001_TXT);
        fileDTO.setPath("/me/" + userDTO.getUserFullName() + "/" + MY_FILE_001_TXT);
        fileDTO.setExpirationDate(new Date(new Long("1222249782000").longValue()));
        fileDAO.createFile(fileDTO);
        Assert.assertEquals(fileDAO.listUploadedFiles(userDTO.getID()).size(), 1);
        List<UserDTO> shared = userDAO.listUsersFileSharedWith(fileDTO.getID());
        Assert.assertTrue(shared != null);
        Assert.assertEquals(shared.size(), 0);

        fileDAO.createSharingLink(fileDTO.getID(), userDTO.getID());
        shared = userDAO.listUsersFileSharedWith(fileDTO.getID());
        Assert.assertEquals(shared.size(), 1);
        Assert.assertEquals(shared.get(0).getEmail(), userDTO.getEmail());
        Assert.assertEquals(shared.get(0).getUserCode(), userDTO.getUserCode());
        Assert.assertEquals(shared.get(0).getUserFullName(), userDTO.getUserFullName());
        Assert.assertEquals(shared.get(0).getID(), userDTO.getID());

    }

    @Test(dependsOnGroups =
        { "user.create" })
    @Transactional
    public final void testChangeUserCode()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final List<UserDTO> users = userDAO.listUsers();
        Assert.assertTrue(users.size() > 1);
        final UserDTO userDTO = users.get(0);
        final String oldCode = userDTO.getUserCode();
        final String newCode = oldCode + "renamed";
        final long oldId = userDTO.getID();
        final UserDTO foundOldUser = userDAO.tryFindUserByCode(oldCode);
        Assert.assertTrue(userDAO.tryFindUserByCode(newCode) == null);
        Assert.assertTrue(foundOldUser != null);
        Assert.assertEquals(foundOldUser.getUserCode(), oldCode);
        Assert.assertEquals(foundOldUser.getID().longValue(), oldId);
        userDAO.changeUserCode(oldCode, newCode);
        final UserDTO foundNewUser = userDAO.tryFindUserByCode(newCode);
        Assert.assertTrue(foundNewUser != null);
        Assert.assertEquals(foundNewUser.getUserCode(), newCode);
        Assert.assertEquals(foundNewUser.getID().longValue(), oldId);
    }

}