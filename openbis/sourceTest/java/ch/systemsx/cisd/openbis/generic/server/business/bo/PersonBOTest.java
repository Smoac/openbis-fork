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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static org.testng.AssertJUnit.assertFalse;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Test cases for corresponding {@link PersonBO} class.
 * 
 * @author Christian Ribeaud
 */
public final class PersonBOTest
{

    private BufferedAppender logRecorder;

    private Mockery context;

    private IAuthorizationDAOFactory daoFactory;

    private IPersonDAO personDAO;

    private final PersonBO createPersonBO()
    {
        return new PersonBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @BeforeMethod
    public final void beforeMethod()
    {
        context = new Mockery();
        daoFactory = context.mock(IAuthorizationDAOFactory.class);
        personDAO = context.mock(IPersonDAO.class);
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public final void afterMethod()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testSaveWithNullPersonUnchanged()
    {
        final PersonBO personBO = createPersonBO();
        boolean fail = true;
        try
        {
            personBO.save();
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testEnrichSessionWithPersonWithNoMatchingUserId()
    {
        final String userId = ManagerTestTool.EXAMPLE_SESSION.getUserName();
        final PersonPE personPE = ManagerTestTool.EXAMPLE_PERSON;
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getPersonDAO();
                    will(returnValue(personDAO));

                    one(personDAO).tryFindPersonByUserId(userId);
                    will(returnValue(null));

                    one(personDAO).createPerson(personPE);
                }
            });
        createPersonBO().enrichSessionWithPerson();
        context.assertIsSatisfied();
    }

    @Test
    public final void testEnrichSessionWithPerson()
    {
        final String userId = ManagerTestTool.EXAMPLE_SESSION.getUserName();
        final PersonPE personPE = ManagerTestTool.EXAMPLE_PERSON;
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getPersonDAO();
                    will(returnValue(personDAO));

                    one(personDAO).tryFindPersonByUserId(userId);
                    will(returnValue(personPE));
                }
            });
        final PersonBO personBO = createPersonBO();
        personBO.enrichSessionWithPerson();
        context.assertIsSatisfied();
    }
}