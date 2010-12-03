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

package ch.systemsx.cisd.openbis.generic.shared.authorization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = RoleWithIdentifier.class)
public class AuthorizationTestCase extends AssertJUnit
{
    protected static final String INSTANCE_CODE = "DB1";

    /** Identifier with code {@link #INSTANCE_CODE}. */
    protected static final DatabaseInstanceIdentifier INSTANCE_IDENTIFIER =
            new DatabaseInstanceIdentifier(INSTANCE_CODE);

    protected static final String ANOTHER_INSTANCE_CODE = "DB2";

    /** Identifier with code {@link #ANOTHER_INSTANCE_CODE}. */
    protected static final DatabaseInstanceIdentifier ANOTHER_INSTANCE_IDENTIFIER =
            new DatabaseInstanceIdentifier(ANOTHER_INSTANCE_CODE);

    protected static final String SPACE_CODE = "G1";

    protected static final String ANOTHER_GROUP_CODE = "G2";

    protected Mockery context;

    protected IAuthorizationDataProvider provider;

    /**
     * Creates a role with level {@link RoleLevel#SPACE} with specified role code for specified
     * space.
     */
    protected RoleWithIdentifier createGroupRole(RoleCode roleCode, GroupIdentifier spaceIdentifier)
    {
        SpacePE groupPE = new SpacePE();
        groupPE.setCode(spaceIdentifier.getSpaceCode());
        DatabaseInstancePE instance = createDatabaseInstancePE(spaceIdentifier);
        groupPE.setDatabaseInstance(instance);
        return new RoleWithIdentifier(RoleLevel.SPACE, roleCode, null, groupPE);
    }

    /**
     * Creates a role with level {@link RoleLevel#INSTANCE} with specified role code for specified
     * database instance.
     */
    protected RoleWithIdentifier createInstanceRole(RoleCode roleCode,
            DatabaseInstanceIdentifier instanceIdentifier)
    {
        DatabaseInstancePE instance = createDatabaseInstancePE(instanceIdentifier);
        return new RoleWithIdentifier(RoleLevel.INSTANCE, roleCode, instance, null);
    }

    /**
     * Creates a new instance of {@link DatabaseInstancePE} for the specified identifier. Shortcut
     * for <code>createDatabaseInstance(instanceIdentifier.getDatabaseInstanceCode())</code>.
     */
    protected DatabaseInstancePE createDatabaseInstancePE(
            DatabaseInstanceIdentifier instanceIdentifier)
    {
        return createDatabaseInstance(instanceIdentifier.getDatabaseInstanceCode());
    }

    /**
     * Creates a new instance of {@link DatabaseInstancePE} with code {@link #INSTANCE_CODE}.
     * Shortcut for <code>createDatabaseInstance(INSTANCE_CODE)</code>.
     */
    protected final DatabaseInstancePE createDatabaseInstance()
    {
        return createDatabaseInstance(INSTANCE_CODE);
    }

    /**
     * Creates a new instance of {@link DatabaseInstancePE} for the specified code. Only code and
     * UUID will be set.
     */
    protected DatabaseInstancePE createDatabaseInstance(String code)
    {
        DatabaseInstancePE instance = new DatabaseInstancePE();
        instance.setCode(code);
        instance.setUuid("global_" + code);
        return instance;
    }

    /**
     * Creates a new instance of {@link DatabaseInstancePE} with code {@link #ANOTHER_INSTANCE_CODE}
     * . Shortcut for <code>createDatabaseInstance(ANOTHER_INSTANCE_CODE)</code>.
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
        return createPerson("megapixel", createDatabaseInstance());
    }

    /** Creates a person with specified userId and database instance. */
    protected PersonPE createPerson(String userId, DatabaseInstancePE instance)
    {
        final PersonPE personPE = new PersonPE();
        personPE.setUserId(userId);
        personPE.setDatabaseInstance(instance);
        return personPE;
    }

    /**
     * Creates a list of spaces which contains {@link #createGroup()} and
     * {@link #createAnotherGroup()}.
     */
    protected List<SpacePE> createGroups()
    {
        final List<SpacePE> groups = new ArrayList<SpacePE>();
        groups.add(createGroup());
        groups.add(createAnotherGroup());
        return groups;
    }

    /**
     * Creates a group with code {@link #SPACE_CODE} and database instance with code
     * {@link AuthorizationTestCase#INSTANCE_CODE}.
     */
    protected SpacePE createGroup()
    {
        return createGroup(SPACE_CODE, createDatabaseInstance());
    }

    /**
     * Creates a group with code {@link #ANOTHER_GROUP_CODE} and database instance with code
     * {@link #ANOTHER_INSTANCE_CODE}.
     */
    protected SpacePE createAnotherGroup()
    {
        return createGroup(ANOTHER_GROUP_CODE, createAnotherDatabaseInstance());
    }

    /**
     * Creates a group based on the specified identifier.
     */
    protected SpacePE createGroup(GroupIdentifier identifier)
    {
        final String databaseInstanceCode = identifier.getDatabaseInstanceCode();
        final DatabaseInstancePE instance = createDatabaseInstance(databaseInstanceCode);
        return createGroup(identifier.getSpaceCode(), instance);
    }

    /**
     * Creates a group with specified group code and database instance.
     */
    protected SpacePE createGroup(final String groupCode,
            final DatabaseInstancePE databaseInstancePE)
    {
        final SpacePE group = new SpacePE();
        group.setCode(groupCode);
        group.setDatabaseInstance(databaseInstancePE);
        return group;
    }

    /**
     * Creates a person with two {@link RoleAssignmentPE} instances. One ADMIN role for database
     * instance {@link #INSTANCE_CODE} and a USER role for the group {@link #createAnotherGroup()}.
     */
    protected PersonPE createPersonWithRoleAssignments()
    {
        final PersonPE person = createPerson();
        assignRoles(person);
        return person;
    }

    /**
     * Assigns two {@link RoleAssignmentPE} instances to specified person. One ADMIN role for
     * database instance {@link #INSTANCE_CODE} and a USER role for the group
     * {@link #createAnotherGroup()}.
     */
    protected void assignRoles(PersonPE person)
    {
        final Set<RoleAssignmentPE> list = new HashSet<RoleAssignmentPE>();
        // Database assignment
        RoleAssignmentPE assignment = new RoleAssignmentPE();
        assignment.setRole(RoleCode.ADMIN);
        assignment.setDatabaseInstance(createDatabaseInstance());
        person.addRoleAssignment(assignment);
        list.add(assignment);
        // Group assignment
        assignment = new RoleAssignmentPE();
        assignment.setRole(RoleCode.USER);
        assignment.setSpace(createAnotherGroup());
        person.addRoleAssignment(assignment);
        list.add(assignment);

        person.setRoleAssignments(list);
    }

    /**
     * Creates a project in the specified group.
     */
    protected ProjectPE createProject(SpacePE group)
    {
        final ProjectPE projectPE = new ProjectPE();
        projectPE.setSpace(group);
        return projectPE;
    }

    /**
     * Creates an experiment in the specified group.
     */
    protected ExperimentPE createExperiment(SpacePE group)
    {
        final ExperimentPE experiment = new ExperimentPE();
        final ExperimentTypePE experimentType = new ExperimentTypePE();
        experimentType.setCode("XXX");
        experiment.setExperimentType(experimentType);
        experiment.setProject(createProject(group));
        return experiment;
    }

    /**
     * Creates a sample in the specified group.
     */
    protected SamplePE createSample(SpacePE group)
    {
        final SamplePE sample = new SamplePE();
        sample.setSpace(group);
        sample.setSampleType(createSampleType());
        return sample;
    }

    /**
     * Creates sample type.
     */
    protected SampleTypePE createSampleType()
    {
        SampleTypePE type = new SampleTypePE();
        type.setContainerHierarchyDepth(0);
        type.setGeneratedFromHierarchyDepth(0);
        type.setListable(true);
        type.setAutoGeneratedCode(false);
        type.setSubcodeUnique(false);
        return type;
    }

    /**
     * Creates a sample in the specified database instance.
     */
    protected SamplePE createSample(DatabaseInstancePE databaseInstance)
    {
        final SamplePE sample = new SamplePE();
        sample.setDatabaseInstance(databaseInstance);
        sample.setSampleType(createSampleType());
        return sample;
    }

    /**
     * Creates a filter in the specified database instance and registrator and ownership flag.
     */
    protected GridCustomFilterPE createFilter(DatabaseInstancePE databaseInstance,
            PersonPE registrator, boolean isPublic)
    {
        final GridCustomFilterPE filter = new GridCustomFilterPE();
        filter.setDatabaseInstance(databaseInstance);
        filter.setRegistrator(registrator);
        filter.setPublic(isPublic);
        filter.setExpression(""); // needed for translation
        return filter;
    }

    /**
     * Creates a list of roles which contains a group role for a USER and group defined by code
     * {@link #SPACE_CODE} and database instance {@link AuthorizationTestCase#INSTANCE_CODE}. If
     * <code>withInstanceRole == true</code> the list contains in addition an instance role for a
     * ADMIN and database instance defined by {@link #ANOTHER_INSTANCE_CODE}.
     */
    protected List<RoleWithIdentifier> createRoles(final boolean withInstanceRole)
    {
        final List<RoleWithIdentifier> list = new ArrayList<RoleWithIdentifier>();
        final RoleWithIdentifier groupRole =
                createGroupRole(RoleCode.USER, new GroupIdentifier(INSTANCE_CODE, SPACE_CODE));
        list.add(groupRole);
        if (withInstanceRole)
        {
            final RoleWithIdentifier databaseInstanceRole =
                    createInstanceRole(RoleCode.ADMIN, new DatabaseInstanceIdentifier(
                            ANOTHER_INSTANCE_CODE));
            list.add(databaseInstanceRole);
        }
        return list;
    }

    /**
     * Prepares {@link #provider} to expect a query for the home database instance and groups.
     */
    protected final void prepareProvider(final DatabaseInstancePE databaseInstance,
            final List<SpacePE> groups)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).getHomeDatabaseInstance();
                    will(returnValue(databaseInstance));

                    allowing(provider).listSpaces();
                    will(returnValue(groups));
                }
            });
    }

    /**
     * Prepares {@link #provider} to expect a query for the specified database instance code and to
     * return the specified database instance.
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
     * Prepares {@link #provider} to expect a query for the specified database instance code and to
     * return the specified database instance and to list groups which will return the specified
     * list of groups.
     */
    protected final void prepareProvider(final String databaseInstanceCode,
            final DatabaseInstancePE databaseInstance, final List<SpacePE> groups)
    {
        prepareProvider(databaseInstanceCode, databaseInstance);
        context.checking(new Expectations()
            {
                {
                    allowing(provider).listSpaces();
                    will(returnValue(groups));
                }
            });
    }

    /**
     * Prepares {@link #provider} to expect a query to list groups which will return the specified
     * list of groups and a query for the specified entity kind and technical id which will return
     * the specifier group.
     */
    protected final void prepareProvider(final List<SpacePE> groups, final SpacePE groupPE,
            final SpaceOwnerKind entityKind, final TechId techId)
    {
        context.checking(new Expectations()
            {
                {
                    one(provider).listSpaces();
                    will(returnValue(groups));

                    one(provider).tryGetSpace(entityKind, techId);
                    will(returnValue(groupPE));
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
