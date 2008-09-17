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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.utilities.StringUtilities;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.IPredicate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Test cases for corresponding {@link PredicateExecutor} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = PredicateExecutor.class)
public final class PredicateExecutorTest
{

    private Mockery context;

    private IPredicate<String> stringPredicate;

    private IPredicateFactory predicateFactory;

    private IAuthorizationDAOFactory daoFactory;

    private final static PersonPE createPerson()
    {
        return new PersonPE();
    }

    private final static List<RoleWithIdentifier> createAllowedRoles()
    {
        return Collections.singletonList(RoleWithIdentifierTest.createGroupRole(RoleCode.USER,
                new GroupIdentifier("DB1", "3V")));
    }

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        stringPredicate = context.mock(IPredicate.class);
        predicateFactory = context.mock(IPredicateFactory.class);
        daoFactory = context.mock(IAuthorizationDAOFactory.class);
        PredicateExecutor.setPredicateFactory(predicateFactory);
        PredicateExecutor.setDAOFactory(daoFactory);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    private final Class<? extends IPredicate<String>> castToStringPredicateClass()
    {
        return (Class<? extends IPredicate<String>>) stringPredicate.getClass();
    }

    @Test
    public final void testEvaluateWithSimpleObject()
    {
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> allowedRoles = createAllowedRoles();
        final String value = StringUtilities.getString();
        context.checking(new Expectations()
            {
                {
                    one(predicateFactory).createPredicateForClass(stringPredicate.getClass());
                    will(returnValue(stringPredicate));

                    one(stringPredicate).init(daoFactory);

                    one(stringPredicate).evaluate(person, allowedRoles, value);
                    will(returnValue(Status.OK));
                }
            });
        assertEquals(Status.OK, PredicateExecutor.evaluate(person, allowedRoles, value,
                castToStringPredicateClass(), String.class));
        context.assertIsSatisfied();
    }

    @Test
    public final void testEvaluateWithArray()
    {
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> allowedRoles = createAllowedRoles();
        final String[] array = StringUtilities.getStrings(2);
        context.checking(new Expectations()
            {
                {
                    one(predicateFactory).createPredicateForClass(stringPredicate.getClass());
                    will(returnValue(stringPredicate));

                    one(stringPredicate).init(daoFactory);

                    one(stringPredicate).evaluate(person, allowedRoles, array[0]);
                    will(returnValue(Status.OK));

                    one(stringPredicate).evaluate(person, allowedRoles, array[1]);
                    will(returnValue(Status.OK));
                }
            });
        assertEquals(Status.OK, PredicateExecutor.evaluate(person, allowedRoles, array,
                castToStringPredicateClass(), String[].class));
        context.assertIsSatisfied();
    }

    @Test
    public final void testEvaluateWithCollection()
    {
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> allowedRoles = createAllowedRoles();
        final List<String> list = Arrays.asList(StringUtilities.getStrings(2));
        context.checking(new Expectations()
            {
                {
                    one(predicateFactory).createPredicateForClass(stringPredicate.getClass());
                    will(returnValue(stringPredicate));

                    one(stringPredicate).init(daoFactory);

                    one(stringPredicate).evaluate(person, allowedRoles, list.get(0));
                    will(returnValue(Status.OK));

                    one(stringPredicate).evaluate(person, allowedRoles, list.get(1));
                    will(returnValue(Status.OK));
                }
            });
        assertEquals(Status.OK, PredicateExecutor.evaluate(person, allowedRoles, list,
                castToStringPredicateClass(), List.class));
        context.assertIsSatisfied();
    }

}