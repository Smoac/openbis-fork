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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.cifex.server.business.bo.BusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;

/**
 * Test cases for corresponding {@link UserManager} class.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = UserManager.class)
public class UserManagerTest extends AbstractFileSystemTestCase
{

    private Mockery context;

    private IDAOFactory daoFactory;

    private IUserDAO userDAO;

    private IUserManager userManager;

    private IBusinessObjectFactory boFactory;

    private IBusinessContext businessContext;

    private UserDTO userAlice;

    private UserDTO newUserAlice;

    private IUserSessionInvalidator userSessionInvalidator;

    private IAuthenticationService externalAuthService;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        userAlice = FileManagerTest.createSampleUserDTO(1L, "alice@users.com");
        newUserAlice = FileManagerTest.createSampleUserDTO(null, "alice@users.com");
        daoFactory = context.mock(IDAOFactory.class);
        userDAO = context.mock(IUserDAO.class);
        boFactory =
                new BusinessObjectFactory(daoFactory, businessContext,
                        SystemTimeProvider.SYSTEM_TIME_PROVIDER);
        businessContext = context.mock(IBusinessContext.class);
        userSessionInvalidator = context.mock(IUserSessionInvalidator.class);
        externalAuthService = context.mock(IAuthenticationService.class);
        userManager = new UserManager(daoFactory, boFactory, businessContext, externalAuthService);

    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Transactional
    @Test
    public void testCreateUser()
    {
        final UserDTO user = newUserAlice;
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).createUser(newUserAlice);
                }
            });
        userManager.createUser(user, null);
        context.assertIsSatisfied();
    }

    @Transactional
    @Test(dataProvider = "booleans")
    public final void testDeleteExpiredUsers(final boolean areThereAnyExpiredUsers)
    {
        final List<UserDTO> expiredUsers = new ArrayList<UserDTO>();
        final int numberOfExpiredUsers = areThereAnyExpiredUsers ? 3 : 0;
        for (int i = 0; i < numberOfExpiredUsers; i++)
        {
            expiredUsers.add(userAlice);
        }
        if (areThereAnyExpiredUsers)
        {
            assertEquals(numberOfExpiredUsers, expiredUsers.size());
        }
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).listExpiredUsers();
                    will(returnValue(expiredUsers));
                    exactly(numberOfExpiredUsers).of(userDAO).deleteUser(userAlice, null);
                    will(returnValue(true));
                    exactly(numberOfExpiredUsers).of(businessContext).getUserSessionInvalidator();
                    will(returnValue(userSessionInvalidator));
                    exactly(numberOfExpiredUsers).of(userSessionInvalidator)
                            .invalidateSessionWithUser(userAlice);

                }
            });
        userManager.deleteExpiredUsers(null);
        context.assertIsSatisfied();
    }

    @DataProvider(name = "booleans")
    private final Object[][] provideAllBooleans()
    {
        return new Object[][]
            {
                { true },
                { false } };
    }

    @Transactional
    @Test(dataProvider = "booleans")
    public final void testIsDatabaseEmpty(final boolean isDatabaseEmpty)
    {
        final int numberOfUsers = isDatabaseEmpty ? 0 : 2;
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).getNumberOfUsers();
                    will(returnValue(numberOfUsers));
                }
            });
        assertEquals(isDatabaseEmpty, userManager.isDatabaseEmpty());
        context.assertIsSatisfied();
    }

    @DataProvider(name = "userCodesAndUsers")
    private final Object[][] provideUserCodes()
    {

        return new Object[][]
            {
                { 1L, "alice@users.com", getSimpleUser(1L, "alice@users.com") },
                { 2L, "alice", getSimpleUser(2L, "alice") } };
    }

    private static final UserDTO getSimpleUser(final long id, final String userCode)
    {
        final UserDTO user = new UserDTO();
        user.setID(id);
        user.setUserCode(userCode);
        return user;
    }

    @Test(dataProvider = "userCodesAndUsers")
    public final void testTryToFindUserByCode(final long id, final String userCode,
            final UserDTO user)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).tryFindUserByCode(userCode);
                    will(returnValue(user));
                    one(businessContext).getMaxFileRetention();
                    will(returnValue(0));
                    one(businessContext).getMaxUserRetention();
                    will(returnValue(0));
                    one(businessContext).getMaxFileCountPerQuotaGroup();
                    will(returnValue(0));
                    one(businessContext).getMaxFileSizePerQuotaGroupInMB();
                    will(returnValue(0L));
                }
            });
        assertEquals(user, userManager.tryFindUserByCode(userCode));
        context.assertIsSatisfied();
    }

    @Test
    public final void changeUserCode()
    {
        final String before = "before";
        final String after = "after";
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).tryFindUserByCode(before);
                    will(returnValue(new UserDTO()));
                    one(userDAO).tryFindUserByCode(after);
                    will(returnValue(null));
                    one(userDAO).changeUserCode(before, after);
                }
            });
        userManager.changeUserCode(before, after);
        context.assertIsSatisfied();
    }

    @Test
    public final void testDoNotChangeUserIsExternallyAuthenticated()
    {
        final UserDTO oldUserToUpdate = userAlice;
        final UserDTO userToUpdate = FileManagerTest.createSampleUserDTO(1L, "alice@users.com");

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).isMainUserOfQuotaGroup(userToUpdate);
                    will(returnValue(true));
                    one(userDAO).updateUser(userToUpdate);
                }
            });
        userManager.updateUser(oldUserToUpdate, userToUpdate, null,
                oldUserToUpdate.getRegistrator(), null);
        assertEquals(oldUserToUpdate.isExternallyAuthenticated(),
                userToUpdate.isExternallyAuthenticated());
        context.assertIsSatisfied();
    }

    @Test
    public final void testChangeUserIsExternallyAuthenticated()
    {
        final UserDTO oldUserToUpdate = userAlice;
        final UserDTO userToUpdate = FileManagerTest.createSampleUserDTO(1L, "alice@users.com");
        userToUpdate.setExternallyAuthenticated(true);

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).isMainUserOfQuotaGroup(userToUpdate);
                    will(returnValue(true));
                    one(userDAO).updateUser(userToUpdate);
                }
            });
        userManager.updateUser(oldUserToUpdate, userToUpdate, null,
                oldUserToUpdate.getRegistrator(), null);
        assertEquals(oldUserToUpdate.isExternallyAuthenticated() == false,
                userToUpdate.isExternallyAuthenticated());
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = UserFailureException.class)
    public final void changeUserCodeOfNonexistenUser()
    {
        final String before = "before";
        final String after = "after";
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).tryFindUserByCode(before);
                    will(returnValue(null));
                }
            });
        userManager.changeUserCode(before, after);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = UserFailureException.class)
    public final void changeUserCodeToAlreadyExistingUser()
    {
        final String before = "before";
        final String after = "after";
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).tryFindUserByCode(before);
                    will(returnValue(new UserDTO()));
                    one(userDAO).tryFindUserByCode(after);
                    will(returnValue(new UserDTO()));
                }
            });
        userManager.changeUserCode(before, after);
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "userEmptyCodesBeforeAndAfter", expectedExceptions = UserFailureException.class)
    public final void changeUserCodeEmpty(final String before, final String after)
    {
        userManager.changeUserCode(before, after);
        context.assertIsSatisfied();
    }

    @DataProvider(name = "userEmptyCodesBeforeAndAfter")
    private final Object[][] provideUserCodesBeforeAndAfter()
    {

        final Object[][] data =
            {
                { null, null },
                { "before", null },
                { null, "after" },
                { "", "" },
                { "before", "" },
                { "", "after" } };
        return data;
    }

    @DataProvider(name = "listsOfUsers")
    private final Object[][] provideListOfUsers()
    {
        final int numberOfUsers = 3;
        final List<UserDTO> users = new ArrayList<UserDTO>();
        final Object[][] data = new Object[numberOfUsers][1];
        for (int i = 0; i < numberOfUsers; i++)
        {
            data[i][0] = new ArrayList<UserDTO>(users);
            users.add(getSimpleUser(i + 1, "user" + i));
        }
        return data;
    }

    @Test(dataProvider = "listsOfUsers")
    public final void testListUsers(final List<UserDTO> usersFromDAO)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).listUsers();
                    will(returnValue(usersFromDAO));
                    if (usersFromDAO.size() > 0)
                    {
                        one(businessContext).getMaxFileRetention();
                        will(returnValue(0));
                        one(businessContext).getMaxUserRetention();
                        will(returnValue(0));
                        one(businessContext).getMaxFileCountPerQuotaGroup();
                        will(returnValue(0));
                        one(businessContext).getMaxFileSizePerQuotaGroupInMB();
                        will(returnValue(0L));
                    }
                }
            });
        final List<UserDTO> users = userManager.listUsers();
        assertEquals(usersFromDAO, users);
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "listsOfUsers")
    public final void testListUsersFileSharedWith(final List<UserDTO> usersFromDAOFileSharedWith)
    {
        final long fileId = 1L;
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).listUsersFileSharedWith(fileId);
                    will(returnValue(usersFromDAOFileSharedWith));
                }
            });
        final List<UserDTO> users = userManager.listUsersFileSharedWith(fileId);
        assertEquals(usersFromDAOFileSharedWith, users);
        context.assertIsSatisfied();
    }

    @Transactional
    @Test(dataProvider = "userCodesAndUsers")
    public final void testDeleteUser(final long id, final String userCode, final UserDTO user)
    {

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).getUserById(id);
                    will(returnValue(user));
                    if (user != null)
                    {
                        one(userDAO).deleteUser(user, null);
                        will(returnValue(true));
                        one(businessContext).getUserSessionInvalidator();
                        will(returnValue(userSessionInvalidator));
                        one(userSessionInvalidator).invalidateSessionWithUser(user);
                    }

                }
            });
        userManager.deleteUser(id, new UserDTO(), null);
        context.assertIsSatisfied();
    }

    @Transactional
    @Test(expectedExceptions = IllegalArgumentException.class)
    public final void testDeleteUserUserNotFound()
    {
        final long id = 111L;
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).getUserById(id);
                    will(throwException(new EmptyResultDataAccessException(1)));
                }
            });
        userManager.deleteUser(id, null, null);
    }

    @Test
    public void testGetUsersCreateOneExternalUser() throws Exception
    {
        final boolean active = true;
        final String userId = "newuser";
        final String firstName = "New";
        final String lastName = "User";
        final String email = "new@users.com";
        final UserDTO user =
                UserManager.createExternalUser(userId, firstName + " " + lastName, email, active);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));

                    one(userDAO).listUsersByCode(userId);
                    will(returnValue(Collections.emptyList()));

                    one(externalAuthService).tryGetAndAuthenticateUser(userId, null);
                    will(returnValue(new Principal(userId, firstName, lastName, email)));

                    one(businessContext).isNewExternallyAuthenticatedUserStartActive();
                    will(returnValue(active));

                    // create user
                    one(userDAO).createUser(user);
                }
            });
        final Collection<UserDTO> users = userManager.getUsers(Arrays.asList(userId), null, null);
        assertEquals(1, users.size());
        assertEquals(user, users.iterator().next());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUsersCreateOneExternalUserByEmail() throws Exception
    {
        final boolean active = true;
        final String userId = "newuser";
        final String firstName = "New";
        final String lastName = "User";
        final String email = "new@users.com";
        final UserDTO user =
                UserManager.createExternalUser(userId, firstName + " " + lastName, email, active);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));

                    one(userDAO).listUsersByEmail(email);
                    will(returnValue(Collections.emptyList()));

                    one(externalAuthService).supportsListingByEmail();
                    will(returnValue(true));

                    one(externalAuthService).tryGetAndAuthenticateUserByEmail(email, null);
                    will(returnValue(new Principal(userId, firstName, lastName, email)));

                    one(businessContext).isNewExternallyAuthenticatedUserStartActive();
                    will(returnValue(active));

                    // create user
                    one(userDAO).createUser(user);
                }
            });
        final Collection<UserDTO> users = userManager.getUsers(null, Arrays.asList(email), null);
        assertEquals(1, users.size());
        assertEquals(user, users.iterator().next());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUsersUnknownUserByEmailExternalServiceCannotListByEmail() throws Exception
    {
        final String email = "new@users.com";
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));

                    one(userDAO).listUsersByEmail(email);
                    will(returnValue(Collections.emptyList()));

                    one(externalAuthService).supportsListingByEmail();
                    will(returnValue(false));
                }
            });
        assertTrue(userManager.getUsers(null, Arrays.asList(email), null).isEmpty());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUsersCreateManyExternalUser() throws Exception
    {
        final boolean active = true;
        final String userId = "newuser";
        final String firstName = "New";
        final String lastName = "User";
        final String email = "new@users.com";
        final String userId2 = "newuser2";
        final String email2 = "new2@users.com";
        final String lastName2 = "User2";
        final String userId3 = "newuser3";
        final String email3 = "new3@users.com";
        final String lastName3 = "User3";
        final UserDTO user1 =
                UserManager.createExternalUser(userId, firstName + " " + lastName, email, active);
        final UserDTO user2 =
                UserManager
                        .createExternalUser(userId2, firstName + " " + lastName2, email2, active);
        final UserDTO user3 =
                UserManager
                        .createExternalUser(userId3, firstName + " " + lastName3, email3, active);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));

                    one(userDAO).listUsersByCode(userId, userId2);
                    will(returnValue(new ArrayList<UserDTO>()));

                    one(userDAO).listUsersByEmail(email3);
                    will(returnValue(new ArrayList<UserDTO>()));

                    one(externalAuthService).tryGetAndAuthenticateUser(userId, null);
                    will(returnValue(new Principal(userId, firstName, lastName, email)));

                    one(externalAuthService).tryGetAndAuthenticateUser(userId2, null);
                    will(returnValue(new Principal(userId2, firstName, lastName2, email2)));

                    one(externalAuthService).supportsListingByEmail();
                    will(returnValue(true));

                    one(externalAuthService).tryGetAndAuthenticateUserByEmail(email3, null);
                    will(returnValue(new Principal(userId3, firstName, lastName3, email3)));

                    exactly(3).of(businessContext).isNewExternallyAuthenticatedUserStartActive();
                    will(returnValue(active));

                    // create user
                    one(userDAO).createUser(user1);
                    will(new CustomAction("set technical user id")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                ((UserDTO) invocation.getParameter(0)).setID(1L);
                                return null;
                            }
                        });

                    // create user
                    one(userDAO).createUser(user2);
                    will(new CustomAction("set technical user id")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                ((UserDTO) invocation.getParameter(0)).setID(2L);
                                return null;
                            }
                        });

                    // create user
                    one(userDAO).createUser(user3);
                    will(new CustomAction("set technical user id")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                ((UserDTO) invocation.getParameter(0)).setID(3L);
                                return null;
                            }
                        });
                }
            });
        final Collection<UserDTO> users =
                userManager.getUsers(Arrays.asList(userId, userId2), Arrays.asList(email3), null);
        assertEquals(3, users.size());
        final Iterator<UserDTO> it = users.iterator();
        user1.setID(1L);
        assertEquals(user1, it.next());
        user2.setID(2L);
        assertEquals(user2, it.next());
        user3.setID(3L);
        assertEquals(user3, it.next());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUsersNullAuthenticationService() throws Exception
    {
        final String userId = "newuser";
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));

                    one(userDAO).listUsersByCode(userId);
                }
            });
        assertTrue(new UserManager(daoFactory, boFactory, businessContext,
                new NullAuthenticationService()).getUsers(Arrays.asList(userId), null, null)
                .isEmpty());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUsersUserIsKnown() throws Exception
    {
        final boolean active = true;
        final String userId = "newuser";
        final String firstName = "New";
        final String lastName = "User";
        final String email = "new@users.com";
        final UserDTO user =
                UserManager.createExternalUser(userId, firstName + " " + lastName, email, active);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));

                    one(userDAO).listUsersByCode(userId);
                    will(returnValue(Arrays.asList(user)));
                }
            });
        final Collection<UserDTO> users = userManager.getUsers(Arrays.asList(userId), null, null);
        assertEquals(1, users.size());
        assertEquals(user, users.iterator().next());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUsersUserForEmailIsKnown() throws Exception
    {
        final boolean active = true;
        final String userId = "newuser";
        final String firstName = "New";
        final String lastName = "User";
        final String email = "new@users.com";
        final UserDTO user =
                UserManager.createExternalUser(userId, firstName + " " + lastName, email, active);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));

                    one(userDAO).listUsersByEmail(email);
                    will(returnValue(Arrays.asList(user)));
                }
            });
        final Collection<UserDTO> users = userManager.getUsers(null, Arrays.asList(email), null);
        assertEquals(1, users.size());
        assertEquals(user, users.iterator().next());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUsersUserForIdAndEmailIsKnownAndIdentical() throws Exception
    {
        final boolean active = true;
        final String userId = "newuser";
        final String firstName = "New";
        final String lastName = "User";
        final String email = "new@users.com";
        final UserDTO user =
                UserManager.createExternalUser(userId, firstName + " " + lastName, email, active);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));

                    one(userDAO).listUsersByCode(userId);
                    will(returnValue(Arrays.asList(user)));

                    one(userDAO).listUsersByEmail(email);
                    will(returnValue(Arrays.asList(user)));
                }
            });
        final Collection<UserDTO> users =
                userManager.getUsers(Arrays.asList(userId), Arrays.asList(email), null);
        assertEquals(1, users.size());
        assertEquals(user, users.iterator().next());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUsersUnknownUserNotFoundInExtAuth() throws Exception
    {
        final String userId = "newuser";
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));

                    one(userDAO).listUsersByCode(userId);
                    will(returnValue(Collections.emptyList()));

                    one(externalAuthService).tryGetAndAuthenticateUser(userId, null);
                    will(returnValue(null));
                }
            });
        assertTrue(userManager.getUsers(Arrays.asList(userId), null, null).isEmpty());
        context.assertIsSatisfied();
    }

}
