/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;

/**
 * Test cases for {@link RoleWithHierarchy}.
 * 
 * @author Izabela Adamczyk
 */
public class RoleWithHierarchyTest extends AssertJUnit
{

    @Test
    public void testNamingConvention()
    {
        for (RoleWithHierarchy role : RoleWithHierarchy.values())
        {
            boolean matchingConvention = false;
            for (RoleLevel level : RoleLevel.values())
            {
                for (RoleCode code : RoleCode.values())
                {
                    if (role.name().equals(level.name() + "_" + code.name()))
                    {
                        matchingConvention = true;
                    }
                }
            }
            assertTrue(matchingConvention);
        }
    }

    @Test
    public void testValueOf() throws Exception
    {
        assertEquals(RoleWithHierarchy.INSTANCE_OBSERVER, RoleWithHierarchy.valueOf(
                RoleLevel.INSTANCE, RoleCode.OBSERVER));
        assertEquals(RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.valueOf(RoleLevel.SPACE,
                RoleCode.ADMIN));
    }

    @Test
    public void testFigureRoleCode() throws Exception
    {
        assertEquals(RoleCode.USER, RoleWithHierarchy.figureRoleCode("SPACE_USER", RoleLevel.SPACE));
        assertEquals(RoleCode.POWER_USER, RoleWithHierarchy.figureRoleCode("SPACE_POWER_USER",
                RoleLevel.SPACE));
        assertEquals(RoleCode.ADMIN, RoleWithHierarchy.figureRoleCode("INSTANCE_ADMIN",
                RoleLevel.INSTANCE));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFigureRoleCodeNotMatchingConvention() throws Exception
    {
        RoleWithHierarchy.figureRoleCode("INSTANCE_ADMIN", RoleLevel.SPACE);
    }

    @Test
    public void testFigureRoleLevel() throws Exception
    {
        assertEquals(RoleLevel.SPACE, RoleWithHierarchy.figureRoleLevel("SPACE_USER"));
        assertEquals(RoleLevel.INSTANCE, RoleWithHierarchy.figureRoleLevel("INSTANCE_USER"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFigureRoleLevelNotMatchingConvention() throws Exception
    {
        RoleWithHierarchy.figureRoleLevel("NONE");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFigureRoleLevelNotMatchingConventionNoSeparator() throws Exception
    {
        RoleWithHierarchy.figureRoleLevel("SPACEUSER");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFigureRoleLevelNotMatchingConventionNonexistentLevel() throws Exception
    {
        RoleWithHierarchy.figureRoleLevel("PROJECT_USER");
    }

}
