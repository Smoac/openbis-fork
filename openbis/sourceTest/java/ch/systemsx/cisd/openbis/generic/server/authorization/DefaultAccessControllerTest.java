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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * Test cases for corresponding {@link DefaultAccessController} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = DefaultAccessController.class)
public final class DefaultAccessControllerTest
{
    private final DefaultAccessController accessController = new DefaultAccessController(null);

    private final static Set<RoleAssignmentPE> createRoleAssignments()
    {
        final Set<RoleAssignmentPE> roleAssignments = new HashSet<RoleAssignmentPE>();

        final RoleAssignmentPE groupRole = new RoleAssignmentPE();

        final GroupPE groupPE = new GroupPE();
        groupPE.setCode("CISD");
        final DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode("DB2");
        groupPE.setDatabaseInstance(databaseInstancePE);
        groupRole.setGroup(groupPE);
        groupRole.setRole(RoleCode.USER);
        roleAssignments.add(groupRole);

        final RoleAssignmentPE instanceRole = new RoleAssignmentPE();

        final DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode("DB1");
        instanceRole.setDatabaseInstance(databaseInstance);
        instanceRole.setRole(RoleCode.OBSERVER);
        roleAssignments.add(instanceRole);

        return roleAssignments;
    }

    @Test
    public final void testIsAuthorizedWithEmptyMethodRoles() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        Method method = MyInterface.class.getMethod("myMethod");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.ERROR, authorized.getFlag());
        assertTrue(authorized.tryGetErrorMessage().indexOf(
                String.format(DefaultAccessController.METHOD_ROLES_NOT_FOUND_TEMPLATE,
                        "MyInterface.myMethod")) > -1);
        method = MyInterface.class.getMethod("myMethodWithEmptyRoles");
        authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.ERROR, authorized.getFlag());
        assertTrue(authorized.tryGetErrorMessage().indexOf(
                String.format(DefaultAccessController.METHOD_ROLES_NOT_FOUND_TEMPLATE,
                        "MyInterface.myMethodWithEmptyRoles")) > -1);
    }

    @Test
    public final void testIsAuthorizedWithEmptyUserRoles() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        final Method method = MyInterface.class.getMethod("myMethodWithSomeRoles");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        final Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.ERROR, authorized.getFlag());
        assertEquals(String.format(
                DefaultAccessController.USER_ROLE_ASSIGNMENTS_NOT_FOUND_TEMPLATE, session
                        .getUserName()), authorized.tryGetErrorMessage());
    }

    @Test
    public final void testIsAuthorizedWithNoMatchingRole() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithSomeRoles");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        final Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.ERROR, authorized.getFlag());
        Set<RoleWithHierarchy> roles =
                new HashSet<RoleWithHierarchy>(Arrays.asList(RoleWithHierarchy.INSTANCE_ADMIN));
        String expectedMessage =
                String.format(DefaultAccessController.MATCHING_ROLE_NOT_FOUND_TEMPLATE, roles,
                        session.getUserName());
        assertEquals(expectedMessage, authorized.tryGetErrorMessage());
    }

    @Test
    public final void testIsAuthorizedWithMatchingRole() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithOtherRoles");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        final Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.OK, authorized.getFlag());
        assertNull(authorized.tryGetErrorMessage());
    }

    @Test
    public final void testIsAuthorizedWithMatchingFirstRole() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithTwoRoles");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        final Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.OK, authorized.getFlag());
        assertNull(authorized.tryGetErrorMessage());
    }

    @Test
    public final void testIsAuthorizedWithMatchingSecondRole() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithTwoRoles");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        final Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.OK, authorized.getFlag());
        assertNull(authorized.tryGetErrorMessage());
    }

    //
    // Helper classes
    //

    private static interface MyInterface
    {
        public void myMethod();

        @RolesAllowed
        public void myMethodWithEmptyRoles();

        @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
        public void myMethodWithSomeRoles();

        @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
        public void myMethodWithOtherRoles();

        @RolesAllowed(
            { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_OBSERVER })
        public void myMethodWithTwoRoles();
    }
}
