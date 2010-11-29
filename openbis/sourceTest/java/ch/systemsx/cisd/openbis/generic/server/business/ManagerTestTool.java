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

package ch.systemsx.cisd.openbis.generic.server.business;

import java.sql.SQLException;
import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.internal.InvocationExpectationBuilder;
import org.jmock.lib.action.ReturnValueAction;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.db.SQLStateUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * Class with static methods useful for writing test code for Manager classes.
 * 
 * @author Franz-Josef Elmer
 */
public class ManagerTestTool
{
    private static final String HOST = "some_ip";

    public static final DatabaseInstancePE EXAMPLE_DATABASE_INSTANCE = createDatabaseInstance();

    public static final SpacePE EXAMPLE_GROUP = createGroup();

    public static final SpacePE EXAMPLE_GROUP2 = createGroup2();

    public static final PersonPE EXAMPLE_PERSON = createPerson();

    public static final Session EXAMPLE_SESSION = createSession();

    public static final Session EXAMPLE_SESSION_WITHOUT_HOME_GROUP = createSessionNoHomeGroup();

    public static final ProjectPE EXAMPLE_PROJECT = createProject();

    /**
     * Create session for the specified person. It will be associated with {@link #EXAMPLE_GROUP}.
     */
    public final static Session createSession(final PersonPE person)
    {
        final String userId = person.getUserId();
        final Principal principal =
                new Principal(userId, person.getFirstName(), person.getLastName(), person
                        .getEmail());
        final Session session =
                new Session(userId, "xyz", principal, HOST, System.currentTimeMillis());
        session.setPerson(person);
        return session;
    }

    /**
     * Prepares mock GroupDAO with {@link #EXAMPLE_GROUP};
     */
    public final static void prepareGroupDAO(final Mockery context,
            final IAuthorizationDAOFactory daoFactory, final ISpaceDAO groupDAO)
    {
        InvocationExpectationBuilder builder = new InvocationExpectationBuilder();
        builder.of(daoFactory).getSpaceDAO();
        context.addExpectation(builder.toExpectation(new ReturnValueAction(groupDAO)));

        builder = new InvocationExpectationBuilder();
        builder.of(groupDAO).listSpaces();
        context.addExpectation(builder.toExpectation(new ReturnValueAction(Arrays
                .asList(EXAMPLE_GROUP))));
    }

    public final static void prepareFindGroup(final Expectations expectations,
            final IAuthorizationDAOFactory daoFactory, final ISpaceDAO groupDAO,
            final IDatabaseInstanceDAO databaseInstanceDAO)
    {
        expectations.allowing(daoFactory).getSpaceDAO();
        expectations.will(Expectations.returnValue(groupDAO));

        expectations.allowing(daoFactory).getDatabaseInstanceDAO();
        expectations.will(Expectations.returnValue(databaseInstanceDAO));

        expectations.allowing(databaseInstanceDAO).tryFindDatabaseInstanceByCode(
                EXAMPLE_DATABASE_INSTANCE.getCode().toUpperCase());
        expectations.will(Expectations.returnValue(EXAMPLE_DATABASE_INSTANCE));

        expectations.allowing(groupDAO).tryFindSpaceByCodeAndDatabaseInstance(
                EXAMPLE_GROUP.getCode().toUpperCase(), EXAMPLE_DATABASE_INSTANCE);
        expectations.will(Expectations.returnValue(EXAMPLE_GROUP));
    }

    public static SpacePE prepareFindGroup(final Expectations exp, final String groupCode,
            final IAuthorizationDAOFactory daoFactory, final ISpaceDAO groupDAO)
    {
        exp.allowing(daoFactory).getHomeDatabaseInstance();
        DatabaseInstancePE db = new DatabaseInstancePE();
        db.setCode(EXAMPLE_DATABASE_INSTANCE.getCode());
        Long dbId = 3123L;
        db.setId(dbId);
        exp.will(Expectations.returnValue(db));

        exp.allowing(daoFactory).getSpaceDAO();
        exp.will(Expectations.returnValue(groupDAO));

        exp.allowing(groupDAO).tryFindSpaceByCodeAndDatabaseInstance(groupCode.toUpperCase(), db);
        exp.will(Expectations.returnValue(ManagerTestTool.EXAMPLE_GROUP));
        return ManagerTestTool.EXAMPLE_GROUP;
    }

    /**
     * Prepares the mock PersonDAO for a single {@link IPersonDAO#tryFindPersonByUserId(String)}
     * invocation which delivers the id of {@link #EXAMPLE_PERSON}.
     */
    public final static void prepareFindOrCreateRegistratorId(final Expectations expectations,
            final IPersonDAO personDAO, final Session session)
    {
        expectations.one(personDAO).tryFindPersonByUserId(session.getUserName());
        expectations.will(Expectations.returnValue(EXAMPLE_PERSON.getId()));
    }

    /**
     * Prepares the mock PersonDAO for a single {@link IPersonDAO#listPersons()} invocation which
     * delivers {@link #EXAMPLE_PERSON}.
     */
    public final static void prepareListPersons(final Expectations expectations,
            final IPersonDAO personDAO)
    {
        expectations.one(personDAO).listPersons();
        expectations.will(Expectations.returnValue(Arrays.asList(EXAMPLE_PERSON)));
    }

    public final static DataAccessException createUniqueViolationException()
    {
        final String sqlState = SQLStateUtils.UNIQUE_VIOLATION;
        return new DataIntegrityViolationException(null, new SQLException(null, sqlState));
    }

    public final static Session createSession()
    {
        final PersonPE person = createExamplePersonWithGroup();
        return createSession(person);
    }

    public final static Session createSessionNoHomeGroup()
    {
        final PersonPE person = createPerson();
        return createSession(person);
    }

    private static PersonPE createExamplePersonWithGroup()
    {
        final PersonPE person = createPerson();
        person.setHomeSpace(createGroup());
        return person;
    }

    public final static PersonPE createPerson()
    {
        final PersonPE person = new PersonPE();
        person.setUserId("john_doe");
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setEmail("John.Doe@group.org");
        person.setId(new Long(42));
        person.setDatabaseInstance(EXAMPLE_DATABASE_INSTANCE);
        return person;
    }

    public final static SpacePE createGroup()
    {
        final SpacePE group = new SpacePE();
        group.setDatabaseInstance(EXAMPLE_DATABASE_INSTANCE);
        group.setCode("MY_GROUP");
        group.setId(4242L);
        return group;
    }

    public final static SpacePE createGroup2()
    {
        final SpacePE group = new SpacePE();
        group.setDatabaseInstance(EXAMPLE_DATABASE_INSTANCE);
        group.setCode("MY_GROUP2");
        group.setId(1984L);
        return group;
    }

    public final static ProjectPE createProject()
    {
        final ProjectPE project = new ProjectPE();
        project.setSpace(EXAMPLE_GROUP);
        project.setCode("MY_GREAT_PROJECT");
        project.setId(314L);
        return project;
    }

    public final static DatabaseInstancePE createDatabaseInstance()
    {
        final DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode("MY_DATABASE_INSTANCE");
        databaseInstancePE.setId(841L);
        return databaseInstancePE;
    }

    private ManagerTestTool()
    {
        // Can not be instantiated.
    }

}
