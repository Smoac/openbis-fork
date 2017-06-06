/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization.predicate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.CommonAuthorizationSystemTest;

/**
 * @author pkupczyk
 */
public abstract class CommonPredicateSystemTest<O> extends CommonAuthorizationSystemTest
{

    @Autowired
    private IAuthorizationConfig authorizationConfig;

    protected abstract O createNonexistentObject();

    protected abstract O createObject(SpacePE spacePE, ProjectPE projectPE);

    protected abstract void evaluateObjects(IAuthSessionProvider sessionProvider, List<O> objects);

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNullForInstanceUser(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        List<O> objects = Arrays.asList((O) null);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithNullForInstanceUser(person, t);
    }

    protected void assertWithNull(PersonPE person, Throwable t)
    {
        assertException(t, UserFailureException.class, "Unspecified value");
    }

    protected void assertWithNullForInstanceUser(PersonPE person, Throwable t)
    {
        assertWithNull(person, t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNullForSpaceUser(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createSpaceRole(RoleCode.ADMIN, getSpace1())));

        List<O> objects = Arrays.asList((O) null);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithNullForSpaceUser(person, t);
    }

    protected void assertWithNullForSpaceUser(PersonPE person, Throwable t)
    {
        assertWithNull(person, t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNullForProjectUser(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createProjectRole(RoleCode.ADMIN, getProject11())));

        List<O> objects = Arrays.asList((O) null);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithNullForProjectUser(person, t);
    }

    protected void assertWithNullForProjectUser(PersonPE person, Throwable t)
    {
        assertWithNull(person, t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNonexistentObjectForInstanceUser(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        O object = createNonexistentObject();
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithNonexistentObjectForInstanceUser(person, t);
    }

    protected void assertWithNonexistentObject(PersonPE person, Throwable t)
    {
        assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
    }

    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t)
    {
        assertWithNonexistentObject(person, t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNonexistentObjectForSpaceUser(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createSpaceRole(RoleCode.ADMIN, getSpace1())));

        O object = createNonexistentObject();
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithNonexistentObjectForSpaceUser(person, t);
    }

    protected void assertWithNonexistentObjectForSpaceUser(PersonPE person, Throwable t)
    {
        assertWithNonexistentObject(person, t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNonexistentObjectForProjectUser(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createProjectRole(RoleCode.ADMIN, getProject11())));

        O object = createNonexistentObject();
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithNonexistentObjectForProjectUser(person, t);
    }

    protected void assertWithNonexistentObjectForProjectUser(PersonPE person, Throwable t)
    {
        assertWithNonexistentObject(person, t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNoAllowedRoles(PersonPE person)
    {
        O object = createObject(getSpace1(), getProject11());
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithNoAllowedRoles(person, t);
    }

    protected void assertWithNoAllowedRoles(PersonPE person, Throwable t)
    {
        assertAuthorizationFailureExceptionThatNoRoles(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithMultipleAllowedRoles(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(
                Arrays.asList(createSpaceRole(RoleCode.ADMIN, getSpace2()), createSpaceRole(RoleCode.ADMIN, getSpace1()))));

        O object = createObject(getSpace1(), getProject11());
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithMultipleAllowedRoles(person, t);
    }

    protected void assertWithMultipleAllowedRoles(PersonPE person, Throwable t)
    {
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithInstanceUser(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        O object = createObject(getSpace1(), getProject11());
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithInstanceUser(person, t);
    }

    protected void assertWithInstanceUser(PersonPE person, Throwable t)
    {
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithMatchingSpaceAndMatchingProjectUser(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace1()), createProjectRole(RoleCode.ADMIN, getProject11()))));

        O object = createObject(getSpace1(), getProject11());
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithMatchingSpaceAndMatchingProjectUser(person, t);
    }

    protected void assertWithMatchingSpaceAndMatchingProjectUser(PersonPE person, Throwable t)
    {
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithMatchingSpaceAndNonMatchingProjectUser(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace1()), createProjectRole(RoleCode.ADMIN, getProject21()))));

        O object = createObject(getSpace1(), getProject11());
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithMatchingSpaceAndNonMatchingProjectUser(person, t);
    }

    protected void assertWithMatchingSpaceAndNonMatchingProjectUser(PersonPE person, Throwable t)
    {
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace2()), createProjectRole(RoleCode.ADMIN, getProject11()))));

        O object = createObject(getSpace1(), getProject11());
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithNonMatchingSpaceAndMatchingProjectUser(person, t);
    }

    protected void assertWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person, Throwable t)
    {
        if (authorizationConfig.isProjectLevelEnabled() && authorizationConfig.isProjectLevelUser(person.getUserId()))
        {
            assertNoException(t);
        } else
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        }
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNonMatchingSpaceAndNonMatchingProjectUser(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace2()), createProjectRole(RoleCode.ADMIN, getProject21()))));

        O object = createObject(getSpace1(), getProject11());
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);
        assertWithNonMatchingSpaceAndNonMatchingProjectUser(person, t);
    }

    protected void assertWithNonMatchingSpaceAndNonMatchingProjectUser(PersonPE person, Throwable t)
    {
        assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
    }

    protected Throwable tryEvaluateObjects(IAuthSessionProvider session, List<O> objects)
    {
        try
        {
            evaluateObjects(session, objects);
            return null;
        } catch (Throwable t)
        {
            return t;
        }
    }

}
