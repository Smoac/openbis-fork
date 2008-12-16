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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class PredicateTestCase extends AuthorizationTestCase
{
    protected static final String ANOTHER_INSTANCE_CODE = "DB2";
    
    /** Identifier with code {@link #ANOTHER_INSTANCE_CODE}. */
    protected static final DatabaseInstanceIdentifier ANOTHER_INSTANCE_IDENTIFIER =
            new DatabaseInstanceIdentifier(ANOTHER_INSTANCE_CODE);
    
    protected static final String GROUP_CODE = "G1";
    protected static final String ANOTHER_GROUP_CODE = "G2";

    protected Mockery context;

    protected IAuthorizationDataProvider provider;

    /**
     * Creates a new instance of {@link DatabaseInstancePE} with code {@link #ANOTHER_INSTANCE_CODE}.
     * Shortcut for <code>createDatabaseInstance(ANOTHER_INSTANCE_CODE)</code>.
     */
    protected DatabaseInstancePE createAnotherDatabaseInstance()
    {
        return createDatabaseInstance(ANOTHER_INSTANCE_CODE);
    }

    /**
     * Creates a person. Only userId and databaseInstance are definied.
     */
    protected PersonPE createPerson()
    {
        final PersonPE personPE = new PersonPE();
        personPE.setUserId("megapixel");
        personPE.setDatabaseInstance(createDatabaseInstance());
        return personPE;
    }

    /**
     * Creates a list of groups which contains {@link #createGroup()} and
     * {@link #createAnotherGroup()}.
     */
    protected List<GroupPE> createGroups()
    {
        final List<GroupPE> groups = new ArrayList<GroupPE>();
        groups.add(createGroup());
        groups.add(createAnotherGroup());
        return groups;
    }

    /**
     * Creates a group with code {@link #GROUP_CODE} and database instance with code
     * {@link AuthorizationTestCase#INSTANCE_CODE}.
     */
    protected GroupPE createGroup()
    {
        return createGroup(GROUP_CODE, createDatabaseInstance());
    }
    
    /**
     * Creates a group with code {@link #ANOTHER_GROUP_CODE} and database instance with code
     * {@link #ANOTHER_INSTANCE_CODE}.
     */
    protected GroupPE createAnotherGroup()
    {
        return createGroup(ANOTHER_GROUP_CODE, createAnotherDatabaseInstance());
    }
    
    /**
     * Creates a group based on the specified identifier.
     */
    protected GroupPE createGroup(GroupIdentifier identifier)
    {
        final String databaseInstanceCode = identifier.getDatabaseInstanceCode();
        final DatabaseInstancePE instance = createDatabaseInstance(databaseInstanceCode);
        return createGroup(identifier.getGroupCode(), instance);
    }

    /**
     * Creates a group with specified group code and database instance.
     */
    protected GroupPE createGroup(final String groupCode, final DatabaseInstancePE databaseInstancePE)
    {
        final GroupPE group = new GroupPE();
        group.setCode(groupCode);
        group.setDatabaseInstance(databaseInstancePE);
        return group;
    }
    
    /**
     * Creates a list of roles which contains a group role for a USER and group defined by
     * code {@link #GROUP_CODE} and database instance {@link AuthorizationTestCase#INSTANCE_CODE}.
     * If <code>withInstanceRole == true</code> the list contains in addition an instance role
     * for a USER and database instance defined by {@link #ANOTHER_INSTANCE_CODE}.
     */
    protected List<RoleWithIdentifier> createRoles(final boolean withInstanceRole)
    {
        final List<RoleWithIdentifier> list = new ArrayList<RoleWithIdentifier>();
        final RoleWithIdentifier groupRole =
                createGroupRole(RoleCode.USER, new GroupIdentifier(INSTANCE_CODE, GROUP_CODE));
        list.add(groupRole);
        if (withInstanceRole)
        {
            final RoleWithIdentifier databaseInstanceRole =
                    createInstanceRole(RoleCode.USER, new DatabaseInstanceIdentifier(
                            ANOTHER_INSTANCE_CODE));
            list.add(databaseInstanceRole);
        }
        return list;
    }

    /**
     * Prepares {@link #provider} to expect a query for the home database instance and groups.
     */
    protected final void prepareProvider(final DatabaseInstancePE databaseInstance,
            final List<GroupPE> groups)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).getHomeDatabaseInstance();
                    will(returnValue(databaseInstance));

                    allowing(provider).listGroups();
                    will(returnValue(groups));
                }
            });
    }
    
    /**
     * Prepares {@link #provider} to expect a query for the specified database instance code and
     * to return the specified database instance.
     */
    protected final void prepareProvider(final String databaseInstanceCode,
            final DatabaseInstancePE databaseInstance)
    {
        context.checking(new Expectations()
        {
            {
                allowing(provider).tryFindDatabaseInstanceByCode(databaseInstanceCode);
                will(returnValue(databaseInstance));
            }
        });
    }
    
    /**
     * Prepares {@link #provider} to expect a query for the specified database instance code and
     * to return the specified database instance and to list groups which will return the specified
     * list of groups.
     */
    protected final void prepareProvider(final String databaseInstanceCode,
            final DatabaseInstancePE databaseInstance, final List<GroupPE> groups)
    {
        prepareProvider(databaseInstanceCode, databaseInstance);
        context.checking(new Expectations()
            {
                {
                    allowing(provider).listGroups();
                    will(returnValue(groups));
                }
            });
    }
    

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        provider = context.mock(IAuthorizationDataProvider.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

}
