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

package ch.systemsx.cisd.openbis.systemtest.authorization.validator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

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
public abstract class CommonValidatorSystemTest<O> extends CommonAuthorizationSystemTest
{

    @Autowired
    private IAuthorizationConfig authorizationConfig;

    protected abstract O createObject(SpacePE spacePE, ProjectPE projectPE);

    protected abstract O validateObject(IAuthSessionProvider sessionProvider, O object);

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNull(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        ValidationResult result = tryValidateObject(createSessionProvider(person), null);

        assertWithNull(person, result.getResult(), result.getError());
    }

    protected void assertWithNull(PersonPE person, O result, Throwable t)
    {
        assertNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNoAllowedRoles(PersonPE person)
    {
        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11()));

        assertWithNoAllowedRoles(person, result.getResult(), result.getError());
    }

    protected void assertWithNoAllowedRoles(PersonPE person, O result, Throwable t)
    {
        assertNull(result);
        assertAuthorizationFailureExceptionThatNoRoles(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithMultipleAllowedRoles(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace2()), createSpaceRole(RoleCode.ADMIN, getSpace1()))));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11()));

        assertWithMultipleAllowedRoles(person, result.getResult(), result.getError());
    }

    protected void assertWithMultipleAllowedRoles(PersonPE person, O result, Throwable t)
    {
        assertNotNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithInstanceUser(PersonPE person)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11()));

        assertWithInstanceUser(person, result.getResult(), result.getError());
    }

    protected void assertWithInstanceUser(PersonPE person, O result, Throwable t)
    {
        assertNotNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithMatchingSpaceAndMatchingProjectUser(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace1()), createProjectRole(RoleCode.ADMIN, getProject11()))));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11()));

        assertWithMatchingSpaceAndMatchingProjectUser(person, result.getResult(), result.getError());
    }

    protected void assertWithMatchingSpaceAndMatchingProjectUser(PersonPE person, O result, Throwable t)
    {
        assertNotNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithMatchingSpaceAndNonMatchingProjectUser(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace1()), createProjectRole(RoleCode.ADMIN, getProject21()))));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11()));

        assertWithMatchingSpaceAndNonMatchingProjectUser(person, result.getResult(), result.getError());
    }

    protected void assertWithMatchingSpaceAndNonMatchingProjectUser(PersonPE person, O result, Throwable t)
    {
        assertNotNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace2()), createProjectRole(RoleCode.ADMIN, getProject11()))));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11()));

        assertWithNonMatchingSpaceAndMatchingProjectUser(person, result.getResult(), result.getError());
    }

    protected void assertWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person, O result, Throwable t)
    {
        if (authorizationConfig.isProjectLevelEnabled() && authorizationConfig.isProjectLevelUser(person.getUserId()))
        {
            assertNotNull(result);
        } else
        {
            assertNull(result);
        }

        assertNoException(t);
    }

    @Test(dataProvider = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public void testWithNonMatchingSpaceAndNonMatchingProjectUser(PersonPE person)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace2()), createProjectRole(RoleCode.ADMIN, getProject21()))));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11()));

        assertWithNonMatchingSpaceAndNonMatchingProjectUser(person, result.getResult(), result.getError());
    }

    protected void assertWithNonMatchingSpaceAndNonMatchingProjectUser(PersonPE person, O result, Throwable t)
    {
        assertNull(result);
        assertNoException(t);
    }

    protected ValidationResult tryValidateObject(IAuthSessionProvider session, O object)
    {
        try
        {
            O result = validateObject(session, object);
            return new ValidationResult(result);
        } catch (Throwable t)
        {
            return new ValidationResult(t);
        }
    }

    private class ValidationResult
    {

        private O result;

        private Throwable error;

        public ValidationResult(O result)
        {
            this.result = result;
        }

        public ValidationResult(Throwable error)
        {
            this.error = error;
        }

        public O getResult()
        {
            return result;
        }

        public Throwable getError()
        {
            return error;
        }
    }

}
