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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.testng.annotations.Test;

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
public abstract class CommonValidatorSystemTest<O> extends CommonAuthorizationSystemTest
{

    protected abstract O createObject(SpacePE spacePE, ProjectPE projectPE, Object param);

    protected abstract O validateObject(IAuthSessionProvider sessionProvider, O object, Object param);

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNull(PersonPE person, Object param)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        ValidationResult result = tryValidateObject(createSessionProvider(person), null, param);

        assertWithNull(person, result.getResult(), result.getError(), param);
    }

    protected void assertWithNull(PersonPE person, O result, Throwable t, Object param)
    {
        assertNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNoAllowedRoles(PersonPE person, Object param)
    {
        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11(), param), param);

        assertWithNoAllowedRoles(person, result.getResult(), result.getError(), param);
    }

    protected void assertWithNoAllowedRoles(PersonPE person, O result, Throwable t, Object param)
    {
        assertNull(result);
        assertAuthorizationFailureExceptionThatNoRoles(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithInstanceAdminUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.ADMIN)));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11(), param), param);

        assertWithInstanceAdminUser(person, result.getResult(), result.getError(), param);
    }

    protected void assertWithInstanceAdminUser(PersonPE person, O result, Throwable t, Object param)
    {
        assertNotNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithInstanceObserverUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(Collections.singleton(createInstanceRole(RoleCode.OBSERVER)));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11(), param), param);

        assertWithInstanceObserverUser(person, result.getResult(), result.getError(), param);
    }

    protected void assertWithInstanceObserverUser(PersonPE person, O result, Throwable t, Object param)
    {
        assertNotNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithMatchingSpaceAndNonMatchingSpaceUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace2()), createSpaceRole(RoleCode.ADMIN, getSpace1()))));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11(), param), param);

        assertWithMatchingSpaceAndNonMatchingSpaceUser(person, result.getResult(), result.getError(), param);
    }

    protected void assertWithMatchingSpaceAndNonMatchingSpaceUser(PersonPE person, O result, Throwable t, Object param)
    {
        assertNotNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithMatchingSpaceAndMatchingProjectUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace1()), createProjectRole(RoleCode.ADMIN, getProject11()))));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11(), param), param);

        assertWithMatchingSpaceAndMatchingProjectUser(person, result.getResult(), result.getError(), param);
    }

    protected void assertWithMatchingSpaceAndMatchingProjectUser(PersonPE person, O result, Throwable t, Object param)
    {
        assertNotNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithMatchingSpaceAndNonMatchingProjectUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace1()), createProjectRole(RoleCode.ADMIN, getProject21()))));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11(), param), param);

        assertWithMatchingSpaceAndNonMatchingProjectUser(person, result.getResult(), result.getError(), param);
    }

    protected void assertWithMatchingSpaceAndNonMatchingProjectUser(PersonPE person, O result, Throwable t, Object param)
    {
        assertNotNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace2()), createProjectRole(RoleCode.ADMIN, getProject11()))));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11(), param), param);

        assertWithNonMatchingSpaceAndMatchingProjectUser(person, result.getResult(), result.getError(), param);
    }

    protected void assertWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person, O result, Throwable t, Object param)
    {
        if (getAuthorizationConfig().isProjectLevelEnabled() && getAuthorizationConfig().isProjectLevelUser(person.getUserId()))
        {
            assertNotNull(result);
        } else
        {
            assertNull(result);
        }

        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNonMatchingSpaceAndNonMatchingProjectUser(PersonPE person, Object param)
    {
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(
                createSpaceRole(RoleCode.ADMIN, getSpace2()), createProjectRole(RoleCode.ADMIN, getProject21()))));

        ValidationResult result = tryValidateObject(createSessionProvider(person), createObject(getSpace1(), getProject11(), param), param);

        assertWithNonMatchingSpaceAndNonMatchingProjectUser(person, result.getResult(), result.getError(), param);
    }

    protected void assertWithNonMatchingSpaceAndNonMatchingProjectUser(PersonPE person, O result, Throwable t, Object param)
    {
        assertNull(result);
        assertNoException(t);
    }

    protected ValidationResult tryValidateObject(IAuthSessionProvider session, O object, Object param)
    {
        try
        {
            O result = validateObject(session, object, param);
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
