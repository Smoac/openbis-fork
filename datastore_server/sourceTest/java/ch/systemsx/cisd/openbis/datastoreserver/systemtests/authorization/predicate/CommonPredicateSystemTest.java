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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public abstract class CommonPredicateSystemTest<O> extends CommonAuthorizationSystemTest
{

    protected abstract O createNonexistentObject(Object param);

    protected abstract O createObject(SpacePE spacePE, ProjectPE projectPE, Object param);

    protected boolean isCollectionPredicate()
    {
        return false;
    }

    protected abstract void evaluateObjects(IAuthSessionProvider sessionProvider, List<O> objects, Object param);

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNullForInstanceUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        List<O> objects = Arrays.asList((O) null);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithNullForInstanceUser(person, t, param);
    }

    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "Unspecified value");
    }

    protected void assertWithNullForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        assertWithNull(person, t, param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNullForSpaceUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(Collections.singleton(createSpaceRole(RoleCode.ADMIN, getSpace1())));

        List<O> objects = Arrays.asList((O) null);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithNullForSpaceUser(person, t, param);
    }

    protected void assertWithNullForSpaceUser(PersonPE person, Throwable t, Object param)
    {
        assertWithNull(person, t, param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNullForProjectUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(Collections.singleton(createProjectRole(RoleCode.ADMIN, getProject11())));

        List<O> objects = Arrays.asList((O) null);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithNullForProjectUser(person, t, param);
    }

    protected void assertWithNullForProjectUser(PersonPE person, Throwable t, Object param)
    {
        assertWithNull(person, t, param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNonexistentObjectForInstanceUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        O object = createNonexistentObject(param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithNonexistentObjectForInstanceUser(person, t, param);
    }

    protected void assertWithNonexistentObject(PersonPE person, Throwable t, Object param)
    {
        assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
    }

    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        assertWithNonexistentObject(person, t, param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNonexistentObjectForSpaceUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(Collections.singleton(createSpaceRole(RoleCode.ADMIN, getSpace1())));

        O object = createNonexistentObject(param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithNonexistentObjectForSpaceUser(person, t, param);
    }

    protected void assertWithNonexistentObjectForSpaceUser(PersonPE person, Throwable t, Object param)
    {
        assertWithNonexistentObject(person, t, param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNonexistentObjectForProjectUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(Collections.singleton(createProjectRole(RoleCode.ADMIN, getProject11())));

        O object = createNonexistentObject(param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithNonexistentObjectForProjectUser(person, t, param);
    }

    protected void assertWithNonexistentObjectForProjectUser(PersonPE person, Throwable t, Object param)
    {
        assertWithNonexistentObject(person, t, param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNoAllowedRoles(PersonPE person, Object param)
    {
        O object = createObject(getSpace1(), getProject11(), param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithNoAllowedRoles(person, t, param);
    }

    protected void assertWithNoAllowedRoles(PersonPE person, Throwable t, Object param)
    {
        assertAuthorizationFailureExceptionThatNoRoles(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithInstanceAdminUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        O object = createObject(getSpace1(), getProject11(), param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithInstanceAdminUser(person, t, param);
    }

    protected void assertWithInstanceAdminUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithInstanceObserverUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        O object = createObject(getSpace1(), getProject11(), param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithInstanceObserverUser(person, t, param);
    }

    protected void assertWithInstanceObserverUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithMatchingSpaceAndNonMatchingSpaceUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(
                Arrays.asList(createSpaceRole(RoleCode.ADMIN, getSpace2()), createSpaceRole(RoleCode.ADMIN, getSpace1()))));

        O object = createObject(getSpace1(), getProject11(), param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithMatchingSpaceAndNonMatchingSpaceUser(person, t, param);
    }

    protected void assertWithMatchingSpaceAndNonMatchingSpaceUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithMatchingSpaceAndMatchingProjectUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace1()), createProjectRole(RoleCode.ADMIN, getProject11()))));

        O object = createObject(getSpace1(), getProject11(), param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithMatchingSpaceAndMatchingProjectUser(person, t, param);
    }

    protected void assertWithMatchingSpaceAndMatchingProjectUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithMatchingSpaceAndNonMatchingProjectUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace1()), createProjectRole(RoleCode.ADMIN, getProject21()))));

        O object = createObject(getSpace1(), getProject11(), param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithMatchingSpaceAndNonMatchingProjectUser(person, t, param);
    }

    protected void assertWithMatchingSpaceAndNonMatchingProjectUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace2()), createProjectRole(RoleCode.ADMIN, getProject11()))));

        O object = createObject(getSpace1(), getProject11(), param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithNonMatchingSpaceAndMatchingProjectUser(person, t, param);
    }

    protected void assertWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person, Throwable t, Object param)
    {
        if (getAuthorizationConfig().isProjectLevelEnabled() && getAuthorizationConfig().isProjectLevelUser(person.getUserId()))
        {
            assertNoException(t);
        } else
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        }
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNonMatchingSpaceAndNonMatchingProjectUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace2()), createProjectRole(RoleCode.ADMIN, getProject21()))));

        O object = createObject(getSpace1(), getProject11(), param);
        List<O> objects = Arrays.asList(object);
        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);
        assertWithNonMatchingSpaceAndNonMatchingProjectUser(person, t, param);
    }

    protected void assertWithNonMatchingSpaceAndNonMatchingProjectUser(PersonPE person, Throwable t, Object param)
    {
        assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNullCollection(PersonPE person, Object param)
    {
        if (false == isCollectionPredicate())
        {
            return;
        }

        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        Throwable t = tryEvaluateObjects(createSessionProvider(person), null, param);
        assertWithNullCollection(person, t, param);
    }

    protected void assertWithNullCollection(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "Unspecified value");
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(PersonPE person, Object param)
    {
        if (false == isCollectionPredicate())
        {
            return;
        }

        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createProjectRole(RoleCode.ADMIN, getProject11()), createSpaceRole(RoleCode.ADMIN, getSpace2()))));

        O objectInProject11 = createObject(getSpace1(), getProject11(), param);
        O objectInProject22 = createObject(getSpace2(), getProject22(), param);

        List<O> objects = Arrays.asList(objectInProject11, objectInProject22);

        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);

        assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(person, t, param);
    }

    protected void assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(PersonPE person, Throwable t,
            Object param)
    {
        if (getAuthorizationConfig().isProjectLevelEnabled() && getAuthorizationConfig().isProjectLevelUser(person.getUserId()))
        {
            assertNoException(t);
        } else
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        }
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(PersonPE person, Object param)
    {
        if (false == isCollectionPredicate())
        {
            return;
        }

        person.setRoleAssignments(Collections.singleton(createProjectRole(RoleCode.ADMIN, getProject11())));

        O objectInProject11 = createObject(getSpace1(), getProject11(), param);
        O objectInProject22 = createObject(getSpace2(), getProject22(), param);

        List<O> objects = Arrays.asList(objectInProject11, objectInProject22);

        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);

        assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(person, t, param);
    }

    protected void assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(PersonPE person, Throwable t,
            Object param)
    {
        assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithTwoObjectsWhereOneObjectMatchesAtSpaceLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(PersonPE person, Object param)
    {
        if (false == isCollectionPredicate())
        {
            return;
        }

        person.setRoleAssignments(Collections.singleton(createSpaceRole(RoleCode.ADMIN, getSpace1())));

        O objectInProject11 = createObject(getSpace1(), getProject11(), param);
        O objectInProject22 = createObject(getSpace2(), getProject22(), param);

        List<O> objects = Arrays.asList(objectInProject11, objectInProject22);

        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);

        assertWithTwoObjectsWhereOneObjectMatchesAtSpaceLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(person, t, param);
    }

    protected void assertWithTwoObjectsWhereOneObjectMatchesAtSpaceLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(PersonPE person, Throwable t,
            Object param)
    {
        assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithTwoObjectsWhereBothObjectsMatchAtProjectLevel(PersonPE person, Object param)
    {
        if (false == isCollectionPredicate())
        {
            return;
        }

        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createProjectRole(RoleCode.ADMIN, getProject11()), createProjectRole(RoleCode.ADMIN, getProject22()))));

        O objectInProject11 = createObject(getSpace1(), getProject11(), param);
        O objectInProject22 = createObject(getSpace2(), getProject22(), param);

        List<O> objects = Arrays.asList(objectInProject11, objectInProject22);

        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);

        assertWithTwoObjectsWhereBothObjectsMatchAtProjectLevel(person, t, param);
    }

    protected void assertWithTwoObjectsWhereBothObjectsMatchAtProjectLevel(PersonPE person, Throwable t, Object param)
    {
        if (getAuthorizationConfig().isProjectLevelEnabled() && getAuthorizationConfig().isProjectLevelUser(person.getUserId()))
        {
            assertNoException(t);
        } else
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        }
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithTwoObjectsWhereBothObjectsMatchAtSpaceLevel(PersonPE person, Object param)
    {
        if (false == isCollectionPredicate())
        {
            return;
        }

        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace1()), createSpaceRole(RoleCode.ADMIN, getSpace2()))));

        O objectInProject11 = createObject(getSpace1(), getProject11(), param);
        O objectInProject22 = createObject(getSpace2(), getProject22(), param);

        List<O> objects = Arrays.asList(objectInProject11, objectInProject22);

        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects, param);

        assertWithTwoObjectsWhereBothObjectsMatchAtSpaceLevel(person, t, param);
    }

    protected void assertWithTwoObjectsWhereBothObjectsMatchAtSpaceLevel(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    protected Throwable tryEvaluateObjects(IAuthSessionProvider session, List<O> objects, Object param)
    {
        try
        {
            evaluateObjects(session, objects, param);
            return null;
        } catch (Throwable t)
        {
            return t;
        }
    }

}
