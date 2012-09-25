/*
 * Copyright 2011 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * Tests for {@link CapabilityMap}.
 * 
 * @author Bernd Rinn
 */
public class CapabilityMapTest
{
    @BeforeClass
    public void init()
    {
        LogInitializer.init();
    }

    @Capability("A")
    void dummyA1()
    {
    }

    @Capability("A")
    void dummyA2()
    {
    }

    @Capability("B")
    void dummyB(String dummy)
    {
    }

    void dummyC()
    {
    }

    @Test
    public void testHappyCase() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A: SPACE_POWER_USER\t", "# Some comment", "",
                        " B  INSTANCE_ETL_SERVER"), "<memory>");
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1")).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA2")).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.INSTANCE_ETL_SERVER,
                capMap.tryGetRoles(CapabilityMapTest.class
                        .getDeclaredMethod("dummyB", String.class)).toArray()[0]);
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyC")));
    }

    @Test
    public void testHappyCaseAlternative() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A:SPACE_POWER_USER\t", "# Some comment", "",
                        " B  INSTANCE_ETL_SERVER"), "<memory>");
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1")).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA2")).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.INSTANCE_ETL_SERVER,
                capMap.tryGetRoles(CapabilityMapTest.class
                        .getDeclaredMethod("dummyB", String.class)).toArray()[0]);
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyC")));
    }

    @Test
    public void testHappyCaseAlternative2() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A :SPACE_POWER_USER\t", "# Some comment", "",
                        " B  INSTANCE_ETL_SERVER"), "<memory>");
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1")).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA2")).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.INSTANCE_ETL_SERVER,
                capMap.tryGetRoles(CapabilityMapTest.class
                        .getDeclaredMethod("dummyB", String.class)).toArray()[0]);
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyC")));
    }

    @Test
    public void testHappyCaseAlternative3() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A : SPACE_POWER_USER\t", "# Some comment", "",
                        " B  INSTANCE_ETL_SERVER"), "<memory>");
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1")).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.SPACE_POWER_USER,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA2")).toArray()[0]);
        assertEquals(
                RoleWithHierarchy.INSTANCE_ETL_SERVER,
                capMap.tryGetRoles(CapabilityMapTest.class
                        .getDeclaredMethod("dummyB", String.class)).toArray()[0]);
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyC")));
    }

    @Test
    public void testHappyCaseAlternative4() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A : SPACE_POWER_USER\t",
                        " A  INSTANCE_ETL_SERVER"), "<memory>");
        assertEquals(2, capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"))
                .size());
        assertTrue(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"))
                .contains(RoleWithHierarchy.SPACE_POWER_USER));
        assertTrue(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"))
                .contains(RoleWithHierarchy.INSTANCE_ETL_SERVER));
    }

    @Test
    public void testHappyCaseAlternative5() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A : SPACE_POWER_USER,INSTANCE_ETL_SERVER\t"),
                        "<memory>");
        assertEquals(2, capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"))
                .size());
        assertTrue(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"))
                .contains(RoleWithHierarchy.SPACE_POWER_USER));
        assertTrue(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"))
                .contains(RoleWithHierarchy.INSTANCE_ETL_SERVER));
    }

    @Test
    public void testInvalidMapLines() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList(
                        "CapabilityMapTest.dummyA: SPACE_POWER_USER #wrong",
                        "CapabilityMapTest.dummyB  NO_ROLE"), "<memory>");
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1")));
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyB",
                String.class)));
        assertNull(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyC")));
    }

    @Test
    public void testUserRoleDisabled() throws SecurityException, NoSuchMethodException
    {
        CapabilityMap capMap =
                new CapabilityMap(Arrays.asList("A: INSTANCE_DISABLED\t"), "<memory>");
        assertEquals(
                RoleWithHierarchy.INSTANCE_DISABLED,
                capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1")).toArray()[0]);
        assertTrue(capMap.tryGetRoles(CapabilityMapTest.class.getDeclaredMethod("dummyA1"))
                .toArray(new RoleWithHierarchy[0])[0]
                .getRoles().isEmpty());
    }

}
