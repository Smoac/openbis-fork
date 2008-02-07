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

import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.bo.IUserBO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link UserManager} class.
 * 
 * @author Izabela Adamczyk
 */
public class UserManagerTest extends AbstractFileSystemTestCase
{

    Mockery context = new Mockery();

    private IDAOFactory daoFactory;

    private IUserDAO userDAO;

    private IUserManager userManager;

    private IBusinessObjectFactory boFactory;

    private IBusinessContext businessContext;

    private UserHttpSessionHolder sessionHolder;

    private IUserBO userBO;

    private UserDTO userAlice;

    @BeforeMethod
    public final void setUp()
    {
        userAlice = FileManagerTest.createSampleUserDTO(1L, "alice@users.com");
        daoFactory = context.mock(IDAOFactory.class);
        userDAO = context.mock(IUserDAO.class);
        boFactory = context.mock(IBusinessObjectFactory.class);
        userBO = context.mock(IUserBO.class);
        businessContext = context.mock(IBusinessContext.class);
        sessionHolder = new UserHttpSessionHolder();
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
                    one(userBO).define(user);
                    one(userBO).save();
                }
            });
        userManager.createUser(user);
        context.assertIsSatisfied();
    }

    @Transactional
    @Test(dataProvider = "booleans")
    public void testDeleteExpiredUsers(final boolean areThereAnyExpiredUsers)
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
                    exactly(numberOfExpiredUsers).of(userDAO).deleteUser(userAlice.getID());
                    will(returnValue(true));
                    exactly(numberOfExpiredUsers).of(businessContext).getUserHttpSessionHolder();
                    will(returnValue(sessionHolder));
                    // TODO 2008-02-06, Izabela Adamczyk: check if invalidate(user) called
                    // - IUserHttpSessionHolder necessary
                    // one(sessionHolder).invalidateSessionWithUser(user);

                }
            });
        userManager.deleteExpiredUsers();
        context.assertIsSatisfied();
    }

    @SuppressWarnings("unused")
    @DataProvider(name = "booleans")
    private Object[][] provideAllBooleans()
    {
        return new Object[][]
            {
                { true },
                { false } };
    }

    @Transactional
    @Test(dataProvider = "booleans")
    public void testIsDatabaseEmpty(final boolean isDatabaseEmpty)
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
    private Object[][] provideUserCodes()
    {

        return new Object[][]
            {
                { "nonexistent", null },
                { "alice@users.com", getSimpleUser("alice@users.com") },
                { "alice", getSimpleUser("alice") } };
    }

    private static final UserDTO getSimpleUser(String userCode)
    {
        UserDTO user = new UserDTO();
        user.setUserCode(userCode);
        user.setID(1L);
        return user;
    }

    @Test(dataProvider = "userCodesAndUsers")
    public void testTryToFindUserByCode(final String userCode, final UserDTO user)
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

    @SuppressWarnings("unused")
    @DataProvider(name = "listsOfUsers")
    private Object[][] provideListOfUsers()
    {
        final int numberOfUsers = 3;
        List<UserDTO> users = new ArrayList<UserDTO>();
        Object[][] data = new Object[numberOfUsers][1];
        for (int i = 0; i < numberOfUsers; i++)
        {
            data[i][0] = new ArrayList<UserDTO>(users);
            users.add(getSimpleUser("user" + i));
        }
        return data;
    }

    @Test(dataProvider = "listsOfUsers")
    public void testListUsers(final List<UserDTO> usersFromDAO)
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

    @Transactional
    @Test(dataProvider = "userCodesAndUsers")
    public void testTryToDeleteUser(final String userCode, final UserDTO user)
    {

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    one(userDAO).tryFindUserByCode(userCode);
                    will(returnValue(user));
                    if (user != null)
                    {
                        one(userDAO).deleteUser(user.getID());
                        will(returnValue(true));
                        one(businessContext).getUserHttpSessionHolder();
                        will(returnValue(sessionHolder));
                        // TODO 2008-02-06, Izabela Adamczyk: check if invalidate(user) called
                        // - IUserHttpSessionHolder necessary
                        // one(sessionHolder).invalidateSessionWithUser(user);
                    }

                }
            });
        userManager.deleteUser(userCode);
        context.assertIsSatisfied();
    }
}
