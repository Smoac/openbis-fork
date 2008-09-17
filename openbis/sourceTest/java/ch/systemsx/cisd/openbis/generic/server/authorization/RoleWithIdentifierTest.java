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
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.authorization.Role.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Test cases for corresponding {@link RoleWithIdentifier} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = RoleWithIdentifier.class)
public final class RoleWithIdentifierTest
{

    private final DatabaseInstanceIdentifier identifier = new DatabaseInstanceIdentifier("DB1");

    public static final RoleWithIdentifier createGroupRole(RoleCode roleCode,
            GroupIdentifier groupIdentifier)
    {
        GroupPE groupPE = new GroupPE();
        groupPE.setCode(groupIdentifier.getGroupCode());
        DatabaseInstancePE instance = createDatabaseInstancePE(groupIdentifier);
        groupPE.setDatabaseInstance(instance);
        return new RoleWithIdentifier(RoleLevel.GROUP, roleCode, null, groupPE);
    }

    public static final RoleWithIdentifier createInstanceRole(RoleCode roleCode,
            DatabaseInstanceIdentifier instanceIdentifier)
    {
        DatabaseInstancePE instance = createDatabaseInstancePE(instanceIdentifier);
        return new RoleWithIdentifier(RoleLevel.INSTANCE, roleCode, instance, null);
    }

    private static DatabaseInstancePE createDatabaseInstancePE(
            DatabaseInstanceIdentifier instanceIdentifier)
    {
        DatabaseInstancePE instance = new DatabaseInstancePE();
        String code = instanceIdentifier.getDatabaseInstanceCode();
        instance.setCode(code);
        instance.setUuid("global_" + code);
        return instance;
    }

    @Test
    public final void testEqualityWithRole()
    {
        final Role role = new Role(RoleLevel.GROUP, RoleCode.ADMIN);
        RoleWithIdentifier roleWithCode =
                createGroupRole(RoleCode.ADMIN, new GroupIdentifier(identifier, "CISD"));
        assertEquals(role, roleWithCode);
        roleWithCode = createGroupRole(RoleCode.ADMIN, new GroupIdentifier(identifier, ""));
        assertEquals(role, roleWithCode);
    }

    @Test
    public final void testRetainAll()
    {
        final Set<Role> singleton =
                Collections.singleton(new Role(RoleLevel.GROUP, RoleCode.ADMIN));
        final List<RoleWithIdentifier> list = new ArrayList<RoleWithIdentifier>();
        list.add(createGroupRole(RoleCode.ADMIN, new GroupIdentifier(identifier, "CISD")));
        list.add(createGroupRole(RoleCode.USER, new GroupIdentifier(identifier, "3V")));
        list.add(createGroupRole(RoleCode.ADMIN, new GroupIdentifier(identifier, "IMSB")));
        list.add(createInstanceRole(RoleCode.USER, identifier));
        list.retainAll(singleton);
        assertEquals(2, list.size());
    }

    @Test
    public final void testFactory()
    {
        GroupPE group = new GroupPE();
        DatabaseInstancePE instance = new DatabaseInstancePE();
        new RoleWithIdentifier(RoleLevel.GROUP, RoleCode.USER, null, group);
        new RoleWithIdentifier(RoleLevel.INSTANCE, RoleCode.USER, instance, null);
        boolean fail = true;
        try
        {
            new RoleWithIdentifier(RoleLevel.GROUP, RoleCode.USER, instance, null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            new RoleWithIdentifier(RoleLevel.INSTANCE, RoleCode.USER, null, group);
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
        Role role = RoleWithIdentifier.createRole(roleAssignment);
        assertEquals(role.getRoleGroup(), Role.RoleLevel.INSTANCE);
        assertEquals(role.getRoleName(), RoleCode.ADMIN);
    }

    @Test
    public final void testCreateRoleFromRoleAssignment()
    {
        final RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        GroupPE group = new GroupPE();
        group.setDatabaseInstance(new DatabaseInstancePE());
        roleAssignment.setGroup(group);
        roleAssignment.setRole(RoleCode.OBSERVER);
        Role role = RoleWithIdentifier.createRole(roleAssignment);
        assertEquals(role.getRoleGroup(), Role.RoleLevel.GROUP);
        assertEquals(role.getRoleName(), RoleCode.OBSERVER);
    }
}
