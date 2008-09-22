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

package ch.systemsx.cisd.openbis.generic.shared.authorization.validator;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.GroupValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;

/**
 * Test cases for corresponding {@link GroupValidator} class.
 * 
 * @author Christian Ribeaud
 */
public final class GroupValidatorTest
{
    private static final long GROUP_ID = 123L;

    private static final long ANOTHER_GROUP_ID = 456L;

    private static final long INSTANCE_ID = 987L;

    private final static GroupPE createGroup()
    {
        final GroupPE group = new GroupPE();
        group.setId(GROUP_ID);
        group.setDatabaseInstance(createDatabaseInstance());
        return group;
    }

    private final static GroupPE createAnotherGroup()
    {
        final GroupPE group = new GroupPE();
        group.setId(ANOTHER_GROUP_ID);
        group.setDatabaseInstance(createDatabaseInstance());
        return group;
    }

    private final static DatabaseInstancePE createDatabaseInstance()
    {
        final DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setId(INSTANCE_ID);
        return databaseInstance;
    }

    final static PersonPE createPerson(final boolean withRoleAssignment)
    {
        final PersonPE person = new PersonPE();
        if (withRoleAssignment)
        {
            final List<RoleAssignmentPE> list = new ArrayList<RoleAssignmentPE>();
            // Database assignment
            RoleAssignmentPE assignment = new RoleAssignmentPE();
            assignment.setPerson(person);
            assignment.setDatabaseInstance(createDatabaseInstance());
            assignment.setRole(RoleCode.ADMIN);
            list.add(assignment);
            // Group assignment
            assignment = new RoleAssignmentPE();
            assignment.setPerson(person);
            assignment.setGroup(createAnotherGroup());
            assignment.setRole(RoleCode.USER);
            list.add(assignment);
            person.setRoleAssignments(list);
        }
        return person;
    }

    @Test
    public final void testIsValidWithNull()
    {
        boolean fail = true;
        try
        {
            new GroupValidator().isValid(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testIsValid()
    {
        final GroupValidator groupValidator = new GroupValidator();
        assertFalse(groupValidator.isValid(createPerson(false), createGroup()));
        // Valid because another group is in the same database instance and the person has a role in
        // it.
        assertTrue(groupValidator.isValid(createPerson(true), createAnotherGroup()));
        assertTrue(groupValidator.isValid(createPerson(true), createGroup()));
    }

}
