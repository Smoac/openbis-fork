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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
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
    private static final Integer EXAMPLE_FILE_RETENTION = new Integer(42);

    private static final Integer EXAMPLE_FILE_RETENTION_ADMIN = new Integer(128);

    private static final String MY_FILE_001_TXT = "my-file-001.txt";

    final static UserDTO testAdminUser =
            createUser(true, true, "admin", "admin@systemsx.ch", null, EXAMPLE_FILE_RETENTION_ADMIN);

    final static UserDTO testPermanentUser =
            createUser(true, false, "user", "someuser@systemsx.ch", testAdminUser,
                    EXAMPLE_FILE_RETENTION);

    final static UserDTO testTemporaryUser =
            createUser(false, false, "tempuser", "someuser@somewhereelse.edu", testPermanentUser,
                    EXAMPLE_FILE_RETENTION);

    final static UserDTO nonExistingUser =
            createUser(true, false, "nouser", "nouser@systemsx.ch", testAdminUser,
                    EXAMPLE_FILE_RETENTION_ADMIN);

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
        assertEquals(expectedUser.getMaxFileRetention(), actualUserFromDB.getMaxFileRetention());
        assertTrue(expectedUser.getPassword().matches(actualUserFromDB.getPasswordHash()));
        assertEquals(expectedUser.getExpirationDate(), actualUserFromDB.getExpirationDate());
        assertEquals(expectedUser.getUserFullName(), actualUserFromDB.getUserFullName());
        assertNotNull(actualUserFromDB.getID());
        assertNotNull(actualUserFromDB.getRegistrationDate());
    }

    final static UserDTO createUser(final boolean permanent, final boolean admin,
            final String code, final String email, final UserDTO registrator, Integer fileRetention)
    {
        final UserDTO user = new UserDTO();
        user.setEmail(email);
        user.setUserCode(code);
        user.setUserFullName(getTestUserName());
        user.setPassword(new Password("the admin passw0rd"));
        user.setExternallyAuthenticated(false);
        user.setAdmin(admin);
        user.setMaxFileRetention(fileRetention);
        user.setCustomMaxFileRetention(true);
        if (registrator != null)
        {
            user.setRegistrator(registrator);
        }
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
    @Rollback(false)
    public final void testCreateUser(final UserDTO userDTO)
    {
        final UserDTO registrator =
                (userDTO.getRegistrator() != null) ? daoFactory.getUserDAO().tryFindUserByCode(
                        userDTO.getRegistrator().getUserCode()) : null;
        if (registrator != null && registrator.isAdmin() == false)
        {
            userDTO.setQuotaGroupId(registrator.getQuotaGroupId());
        }
        final IUserDAO userDAO = daoFactory.getUserDAO();
        int currentNumberOfUsers = userDAO.listUsers().size();
        assertNull(userDTO.getID());

        userDAO.createUser(userDTO);

        Long id = userDTO.getID();
        assertNotNull(id);
        List<UserDTO> users = userDAO.listUsers();
        assertEquals(currentNumberOfUsers + 1, users.size());
        for (UserDTO user : users)
        {
            if (user.getID().equals(id))
            {
                if (user.isAdmin())
                {
                    assertEquals(EXAMPLE_FILE_RETENTION_ADMIN, user.getMaxFileRetention());
                } else
                {
                    assertEquals(EXAMPLE_FILE_RETENTION, user.getMaxFileRetention());
                }
                return;
            }
        }
        if (testTemporaryUser.equals(userDTO))
        {
            assertFalse(userDAO.isMainUserOfQuotaGroup(userDTO));
        } else
        {
            assertTrue(userDAO.isMainUserOfQuotaGroup(userDTO));
        }
        fail("Created user not found.");
    }

    @Test(groups =
        { "user.create" }, dependsOnMethods = "testCreateUser", expectedExceptions = DataAccessException.class)
    public final void testCreateUserWithTooLong()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final UserDTO newUser =
                createUser(true, false, StringUtils.repeat("A", 51), "u@v.org", testAdminUser, null);
        userDAO.createUser(newUser);
    }

    @Test(groups = "user.create", dependsOnMethods = "testCreateUser", expectedExceptions = DataIntegrityViolationException.class)
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
    public final void testRefreshQuotaInfo()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final UserDTO user = userDAO.getUserById(testTemporaryUser.getID());
        assertFalse(userDAO.hasUserFilesForDownload(user.getID()));
        checkUser(testTemporaryUser, user);
        // Fake some changed quota values
        user.setMaxFileRetention(17);
        user.setCurrentFileCount(21);
        user.setCurrentFileSize(103);
        user.setMaxFileCountPerQuotaGroup(22);
        user.setMaxFileSizePerQuotaGroupInMB(1L);
        // Now restore those values from the database and check.
        userDAO.refreshQuotaInformation(user);
        checkUser(testTemporaryUser, user);
    }

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    public final void testListUserRegisteredBy()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();

        final List<UserDTO> listUsersRegisteredByAdmin =
                userDAO.listUsersRegisteredBy(testAdminUser.getID());
        assertEquals(1, listUsersRegisteredByAdmin.size());
        UserDTO actual = listUsersRegisteredByAdmin.get(0);
        assertEquals(testPermanentUser.getID(), actual.getID());

        final List<UserDTO> listUsersRegisteredByPermanent =
                userDAO.listUsersRegisteredBy(testPermanentUser.getID());
        assertEquals(1, listUsersRegisteredByPermanent.size());
        actual = listUsersRegisteredByPermanent.get(0);
        assertEquals(testTemporaryUser.getID(), actual.getID());

        final List<UserDTO> listUsersRegisteredByTemporary =
                userDAO.listUsersRegisteredBy(testTemporaryUser.getID());
        assertEquals(0, listUsersRegisteredByTemporary.size());
    }

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    public final void testGetNumberOfUsers()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        assertEquals(3, userDAO.getNumberOfUsers());
    }

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    public final void testListExpiredUsers()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final List<UserDTO> expiredUsers = userDAO.listExpiredUsers();
        assertEquals(1, expiredUsers.size());
        assertEquals(testTemporaryUser.getID(), expiredUsers.get(0).getID());
    }

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    public final void testListUsers()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        assertEquals(3, userDAO.listUsers().size());
    }

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    public final void testListUserWithIds()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        assertEquals(1, userDAO.listUsersById(1).size());
        assertEquals(2, userDAO.listUsersById(1, 2).size());
        assertEquals(2, userDAO.listUsersById(1, 2, 1).size());
        assertEquals(3, userDAO.listUsersById(1, 2, 3).size());
    }

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    public final void testListUserWithCodes()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        assertEquals(1, userDAO.listUsersByCode("admin").size());
        assertEquals(2, userDAO.listUsersByCode("admin", "user").size());
        assertEquals(2, userDAO.listUsersByCode("admin", "user", "admin").size());
        assertEquals(3, userDAO.listUsersByCode("admin", "user", "tempuser").size());
    }

    @Test(dependsOnGroups =
        { "user.create" }, groups = "user.read")
    public final void testListUserWithEmail()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        assertEquals(1, userDAO.listUsersByEmail("admin@systemsx.ch").size());
        assertEquals(2, userDAO.listUsersByEmail("admin@systemsx.ch", "someuser@systemsx.ch")
                .size());
        assertEquals(2, userDAO.listUsersByEmail("admin@systemsx.ch", "someuser@systemsx.ch",
                "admin@systemsx.ch").size());
        assertEquals(3, userDAO.listUsersByEmail("admin@systemsx.ch", "someuser@systemsx.ch",
                "someuser@somewhereelse.edu").size());
    }

    //
    // 'update' group
    //

    @Test(dependsOnGroups =
        { "user.read" }, groups = "user.update")
    public final void testUpdateUser()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();

        // No change
        userDAO.updateUser(testAdminUser);
        final UserDTO testAdminUserFromDB = userDAO.tryFindUserByCode(testAdminUser.getUserCode());
        checkUser(testAdminUser, testAdminUserFromDB);

        // Try Update Email and max upload size
        testTemporaryUser.setEmail("updated@temporary.cifex");
        testTemporaryUser.setMaxFileSizePerQuotaGroupInMB(1000L);
        testTemporaryUser.setCustomMaxFileSizePerQuotaGroup(true);
        userDAO.updateUser(testTemporaryUser);
        UserDTO testTemporaryUserFromDB =
                userDAO.tryFindUserByCode(testTemporaryUser.getUserCode());
        checkUser(testTemporaryUser, testTemporaryUserFromDB);

        // Try update Password and file retention
        testPermanentUser.setPassword(new Password("NewPassword"));
        testPermanentUser.setAdmin(true);
        testPermanentUser.setExternallyAuthenticated(false);
        testPermanentUser.setUserFullName("User Full Name");
        testPermanentUser.setMaxFileRetention(new Integer(999));
        userDAO.updateUser(testPermanentUser);
        UserDTO testPermanentUserFromDB =
                userDAO.tryFindUserByCode(testPermanentUser.getUserCode());
        checkUser(testPermanentUser, testPermanentUserFromDB);

        // Remove admin Permissions of permanent user
        testPermanentUser.setAdmin(false);
        userDAO.updateUser(testPermanentUser);
        testPermanentUserFromDB = userDAO.tryFindUserByCode(testPermanentUser.getUserCode());
        checkUser(testPermanentUser, testPermanentUserFromDB);

        // Check that initially testPermanentUser and testTemporaryUser are in the same quota group
        assertEquals(testPermanentUserFromDB.getQuotaGroupId(), testTemporaryUserFromDB
                .getQuotaGroupId());
        // Now let the database create a new quota group for testTemporaryUser
        testTemporaryUser.setQuotaGroupId(null);
        userDAO.updateUser(testTemporaryUser);
        testTemporaryUserFromDB = userDAO.tryFindUserByCode(testTemporaryUser.getUserCode());
        // Now temporary user has a brand new quota group.
        assertNotNull(testTemporaryUserFromDB.getQuotaGroupId());
        assertFalse(testPermanentUserFromDB.getQuotaGroupId().equals(
                testTemporaryUserFromDB.getQuotaGroupId()));
    }

    //
    // 'delete' group
    //

    @Test(dependsOnGroups =
        { "user.update" }, groups =
        { "user.delete" })
    public final void testDeleteUser()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();

        assertFalse(userDAO.deleteUser(nonExistingUser, null));

        try
        {
            userDAO.deleteUser(testAdminUser, null);
            fail("didn't detect that deleting the user would violate a fk constraint");
        } catch (DataIntegrityViolationException ex)
        {
            // Expected cannot delete a user that still own other users.
        }
    }

    @Test(dependsOnGroups =
        { "user.delete" }, groups =
        { "user.delete2" })
    public final void testDeleteUser2()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();

        final List<UserDTO> listUsers = userDAO.listUsers();

        assertTrue(userDAO.deleteUser(testAdminUser, testPermanentUser.getID()));
        assertEquals(listUsers.size() - 1, userDAO.listUsers().size());

        assertTrue(userDAO.deleteUser(testTemporaryUser, null));
        assertEquals(listUsers.size() - 2, userDAO.listUsers().size());
    }

    @Test(dependsOnGroups =
        { "user.create" })
    public final void testListUsersFileSharedWith()
    {
        final IFileDAO fileDAO = daoFactory.getFileDAO();
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final List<UserDTO> users = userDAO.listUsers();
        assertTrue(users.size() > 0);
        final UserDTO userDTO = users.get(0);

        final FileDTO fileDTO = new FileDTO(userDTO.getID());
        fileDTO.setName(MY_FILE_001_TXT);
        fileDTO.setPath("/me/" + userDTO.getUserFullName() + "/" + MY_FILE_001_TXT);
        fileDTO.setExpirationDate(new Date(new Long("1222249782000").longValue()));
        fileDTO.setSize(1L);
        fileDTO.setCompleteSize(1L);
        fileDAO.createFile(fileDTO);
        assertEquals(1, fileDAO.listDirectlyAndIndirectlyOwnedFiles(userDTO.getID()).size());
        List<UserDTO> shared = userDAO.listUsersFileSharedWith(fileDTO.getID());
        assertTrue(shared != null);
        assertEquals(shared.size(), 0);

        fileDAO.createSharingLink(fileDTO.getID(), userDTO.getID());
        shared = userDAO.listUsersFileSharedWith(fileDTO.getID());
        assertEquals(shared.size(), 1);
        assertEquals(shared.get(0).getEmail(), userDTO.getEmail());
        assertEquals(shared.get(0).getUserCode(), userDTO.getUserCode());
        assertEquals(shared.get(0).getUserFullName(), userDTO.getUserFullName());
        assertEquals(shared.get(0).getID(), userDTO.getID());

    }

    @Test(dependsOnGroups =
        { "user.create" })
    public final void testChangeUserCode()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final List<UserDTO> users = userDAO.listUsers();
        assertTrue(users.size() > 1);
        final UserDTO userDTO = users.get(0);
        final String oldCode = userDTO.getUserCode();
        final String newCode = oldCode + "renamed";
        final long oldId = userDTO.getID();
        final UserDTO foundOldUser = userDAO.tryFindUserByCode(oldCode);
        assertTrue(userDAO.tryFindUserByCode(newCode) == null);
        assertTrue(foundOldUser != null);
        assertEquals(foundOldUser.getUserCode(), oldCode);
        assertEquals(foundOldUser.getID().longValue(), oldId);
        userDAO.changeUserCode(oldCode, newCode);
        final UserDTO foundNewUser = userDAO.tryFindUserByCode(newCode);
        assertTrue(foundNewUser != null);
        assertEquals(foundNewUser.getUserCode(), newCode);
        assertEquals(foundNewUser.getID().longValue(), oldId);
    }

}