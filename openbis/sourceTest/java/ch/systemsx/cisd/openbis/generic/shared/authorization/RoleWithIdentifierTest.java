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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.authorization.DefaultAccessController;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Test cases for corresponding {@link RoleWithIdentifier} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = RoleWithIdentifier.class)
public final class RoleWithIdentifierTest extends AuthorizationTestCase
{
    @Test
    public final void testEqualityWithRole()
    {
        final RoleWithHierarchy role = RoleWithHierarchy.valueOf(RoleLevel.SPACE, RoleCode.ADMIN);
        RoleWithIdentifier roleWithCode =
                createGroupRole(RoleCode.ADMIN, new GroupIdentifier(INSTANCE_IDENTIFIER, "CISD"));
        assertEquals(role, roleWithCode.getRole());
        roleWithCode =
                createGroupRole(RoleCode.ADMIN, new GroupIdentifier(INSTANCE_IDENTIFIER, ""));
        assertEquals(role, roleWithCode.getRole());
    }

    @Test
    public final void testRetainAll()
    {
        final Set<RoleWithHierarchy> singleton =
                Collections.singleton(RoleWithHierarchy.valueOf(RoleLevel.SPACE, RoleCode.ADMIN));
        final List<RoleWithIdentifier> list = new ArrayList<RoleWithIdentifier>();
        list.add(createGroupRole(RoleCode.ADMIN, new GroupIdentifier(INSTANCE_IDENTIFIER, "CISD")));
        list.add(createGroupRole(RoleCode.USER, new GroupIdentifier(INSTANCE_IDENTIFIER, "3V")));
        list.add(createGroupRole(RoleCode.ADMIN, new GroupIdentifier(INSTANCE_IDENTIFIER, "IMSB")));
        list.add(createInstanceRole(RoleCode.ETL_SERVER, INSTANCE_IDENTIFIER));
        DefaultAccessController.retainMatchingRoleWithIdentifiers(list, singleton);
        assertEquals(2, list.size());
    }

    @Test
    public final void testFactory()
    {
        SpacePE group = new SpacePE();
        DatabaseInstancePE instance = new DatabaseInstancePE();
        new RoleWithIdentifier(RoleLevel.SPACE, RoleCode.USER, null, group);
        new RoleWithIdentifier(RoleLevel.INSTANCE, RoleCode.OBSERVER, instance, null);
        boolean fail = true;
        try
        {
            new RoleWithIdentifier(RoleLevel.SPACE, RoleCode.USER, instance, null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            new RoleWithIdentifier(RoleLevel.INSTANCE, RoleCode.OBSERVER, null, group);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testCreateRoleFromRoleAssignmentRainyDay()
    {
        boolean fail = true;
        try
        {
            RoleWithIdentifier.createRole((RoleAssignmentPE) null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            RoleWithIdentifier.createRole(new RoleAssignmentPE());
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testCreateRoleFromRoleAssignmentDbLevel()
    {
        final RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setDatabaseInstance(new DatabaseInstancePE());
        roleAssignment.setRole(RoleCode.ADMIN);
        RoleWithIdentifier role = RoleWithIdentifier.createRole(roleAssignment);
        assertEquals(role.getRoleLevel(), RoleLevel.INSTANCE);
        assertEquals(role.getRoleName(), RoleCode.ADMIN);
    }

    @Test
    public final void testCreateRoleFromRoleAssignment()
    {
        final RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        SpacePE group = new SpacePE();
        group.setDatabaseInstance(new DatabaseInstancePE());
        roleAssignment.setSpace(group);
        roleAssignment.setRole(RoleCode.OBSERVER);
        RoleWithIdentifier role = RoleWithIdentifier.createRole(roleAssignment);
        assertEquals(role.getRoleLevel(), RoleLevel.SPACE);
        assertEquals(role.getRoleName(), RoleCode.OBSERVER);
    }
}
