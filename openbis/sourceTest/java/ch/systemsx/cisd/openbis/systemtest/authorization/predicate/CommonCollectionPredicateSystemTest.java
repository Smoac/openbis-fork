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
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * @author pkupczyk
 */
public abstract class CommonCollectionPredicateSystemTest<O> extends CommonPredicateSystemTest<O>
{

    @Autowired
    private IAuthorizationConfig authorizationConfig;

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNullCollection(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        Throwable t = tryEvaluateObjects(createSessionProvider(person), null);
        assertWithNullCollection(person, t);
    }

    protected void assertWithNullCollection(PersonPE person, Throwable t)
    {
        assertException(t, UserFailureException.class, "Unspecified value");
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createProjectRole(RoleCode.ADMIN, getProject11()), createSpaceRole(RoleCode.ADMIN, getSpace2()))));

        O objectInProject11 = createObject(getSpace1(), getProject11());
        O objectInProject22 = createObject(getSpace2(), getProject22());

        List<O> objects = Arrays.asList(objectInProject11, objectInProject22);

        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);

        assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(person, t);
    }

    protected void assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(PersonPE person, Throwable t)
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
    public void testWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createProjectRole(RoleCode.ADMIN, getProject11())));

        O objectInProject11 = createObject(getSpace1(), getProject11());
        O objectInProject22 = createObject(getSpace2(), getProject22());

        List<O> objects = Arrays.asList(objectInProject11, objectInProject22);

        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);

        assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(person, t);
    }

    protected void assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(PersonPE person, Throwable t)
    {
        assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithTwoObjectsWhereOneObjectMatchesAtSpaceLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createSpaceRole(RoleCode.ADMIN, getSpace1())));

        O objectInProject11 = createObject(getSpace1(), getProject11());
        O objectInProject22 = createObject(getSpace2(), getProject22());

        List<O> objects = Arrays.asList(objectInProject11, objectInProject22);

        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);

        assertWithTwoObjectsWhereOneObjectMatchesAtSpaceLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(person, t);
    }

    protected void assertWithTwoObjectsWhereOneObjectMatchesAtSpaceLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(PersonPE person, Throwable t)
    {
        assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithTwoObjectsWhereBothObjectsMatchAtProjectLevel(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createProjectRole(RoleCode.ADMIN, getProject11()), createProjectRole(RoleCode.ADMIN, getProject22()))));

        O objectInProject11 = createObject(getSpace1(), getProject11());
        O objectInProject22 = createObject(getSpace2(), getProject22());

        List<O> objects = Arrays.asList(objectInProject11, objectInProject22);

        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);

        assertWithTwoObjectsWhereBothObjectsMatchAtProjectLevel(person, t);
    }

    protected void assertWithTwoObjectsWhereBothObjectsMatchAtProjectLevel(PersonPE person, Throwable t)
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
    public void testWithTwoObjectsWhereBothObjectsMatchAtSpaceLevel(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace1()), createSpaceRole(RoleCode.ADMIN, getSpace2()))));

        O objectInProject11 = createObject(getSpace1(), getProject11());
        O objectInProject22 = createObject(getSpace2(), getProject22());

        List<O> objects = Arrays.asList(objectInProject11, objectInProject22);

        Throwable t = tryEvaluateObjects(createSessionProvider(person), objects);

        assertWithTwoObjectsWhereBothObjectsMatchAtSpaceLevel(person, t);
    }

    protected void assertWithTwoObjectsWhereBothObjectsMatchAtSpaceLevel(PersonPE person, Throwable t)
    {
        assertNoException(t);
    }

}