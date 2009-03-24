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

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.bo.IUserBO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Test cases for corresponding {@link UserManager} class.
 * 
 * @author Izabela Adamczyk
 */
public class UserManagerTest extends AbstractFileSystemTestCase
{

    private Mockery context;

    private IDAOFactory daoFactory;

    private IUserDAO userDAO;

    private IUserManager userManager;

    private IBusinessObjectFactory boFactory;

    private IBusinessContext businessContext;

    private IUserBO userBO;

    private UserDTO userAlice;

    private IUserSessionInvalidator userSessionInvalidator;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        userAlice = FileManagerTest.createSampleUserDTO(1L, "alice@users.com");
        daoFactory = context.mock(IDAOFactory.class);
        userDAO = context.mock(IUserDAO.class);
        boFactory = context.mock(IBusinessObjectFactory.class);
        userBO = context.mock(IUserBO.class);
        businessContext = context.mock(IBusinessContext.class);
        userSessionInvalidator = context.mock(IUserSessionInvalidator.class);
        userManager = new UserManager(daoFactory, boFactory, businessContext);

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
        final UserDTO user = userAlice;
        context.checking(new Expectations()
            {
                {
                    allowing(boFactory).createUserBO();
                    will(returnValue(userBO));
                    allowing(businessContext).getUserActionLog();
                    will(returnValue(new DummyUserActionLog()));
                    one(userBO).define(user);
                    one(userBO).save();
                }
            });
        userManager.createUser(user);
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
                    allowing(businessContext).getUserActionLog();
                    will(returnValue(new DummyUserActionLog()));
                    exactly(numberOfExpiredUsers).of(userDAO).deleteUser(userAlice.getID());
                    will(returnValue(true));
                    exactly(numberOfExpiredUsers).of(businessContext).getUserSessionInvalidator();
                    will(returnValue(userSessionInvalidator));
                    exactly(numberOfExpiredUsers).of(userSessionInvalidator)
                            .invalidateSessionWithUser(userAlice);

                }
            });
        userManager.deleteExpiredUsers();
        context.assertIsSatisfied();
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    @DataProvider(name = "userCodesAndUsers")
    private final Object[][] provideUserCodes()
    {

        return new Object[][]
            {
                { "alice@users.com", getSimpleUser("alice@users.com") },
                { "alice", getSimpleUser("alice") } };
    }

    private static final UserDTO getSimpleUser(final String userCode)
    {
        final UserDTO user = new UserDTO();
        user.setUserCode(userCode);
        user.setID(1L);
        return user;
    }

    @Test(dataProvider = "userCodesAndUsers")
    public final void testTryToFindUserByCode(final String userCode, final UserDTO user)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).tryFindUserByCode(userCode);
                    will(returnValue(user));
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
                    one(businessContext).getUserActionLog();
                    will(returnValue(new DummyUserActionLog()));
                }
            });
        userManager.changeUserCode(before, after);
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
                    one(businessContext).getUserActionLog();
                    will(returnValue(new DummyUserActionLog()));
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
                    one(businessContext).getUserActionLog();
                    will(returnValue(new DummyUserActionLog()));
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    @DataProvider(name = "listsOfUsers")
    private final Object[][] provideListOfUsers()
    {
        final int numberOfUsers = 3;
        final List<UserDTO> users = new ArrayList<UserDTO>();
        final Object[][] data = new Object[numberOfUsers][1];
        for (int i = 0; i < numberOfUsers; i++)
        {
            data[i][0] = new ArrayList<UserDTO>(users);
            users.add(getSimpleUser("user" + i));
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
    public final void testDeleteUser(final String userCode, final UserDTO user)
    {

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).tryFindUserByCode(userCode);
                    will(returnValue(user));
                    allowing(businessContext).getUserActionLog();
                    will(returnValue(new DummyUserActionLog()));
                    if (user != null)
                    {
                        one(userDAO).deleteUser(user.getID());
                        will(returnValue(true));
                        one(businessContext).getUserSessionInvalidator();
                        will(returnValue(userSessionInvalidator));
                        one(userSessionInvalidator).invalidateSessionWithUser(user);
                    }

                }
            });
        userManager.deleteUser(userCode);
        context.assertIsSatisfied();
    }

    @Transactional
    @Test(expectedExceptions = UserFailureException.class)
    public final void testDeleteUserUserNotFound()
    {
        final String userCode = "nonexistent";
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).tryFindUserByCode(userCode);
                    will(returnValue(null));
                }
            });
        userManager.deleteUser(userCode);
    }
}
