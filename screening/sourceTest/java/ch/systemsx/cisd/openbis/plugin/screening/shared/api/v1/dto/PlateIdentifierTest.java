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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class PlateIdentifierTest extends AssertJUnit
{

    @Test
    public void testEqualityWithNonNullSpaceCode()
    {
        PlateIdentifier plateId1 = new PlateIdentifier("abcd", "MySpace");
        PlateIdentifier plateId2 = new PlateIdentifier("abcd", "MySpace");
        assertEquals(plateId1, plateId2);
        assertEquals(plateId1.hashCode(), plateId2.hashCode());
    }

    @Test
    public void testEqualityWithDifferentCode()
    {
        PlateIdentifier plateId1 = new PlateIdentifier("abcd", "MySpace");
        PlateIdentifier plateId2 = new PlateIdentifier("abcde", "MySpace");
        assertFalse(plateId1.equals(plateId2));
        assertFalse(plateId1.hashCode() == plateId2.hashCode());
    }

    @Test
    public void testEqualityWithDifferentSpaceCode()
    {
        PlateIdentifier plateId1 = new PlateIdentifier("abcd", "MySpace");
        PlateIdentifier plateId2 = new PlateIdentifier("abcd", "OtherSpace");
        assertFalse(plateId1.equals(plateId2));
        assertFalse(plateId1.hashCode() == plateId2.hashCode());
    }

    @Test
    public void testEqualityWithNullSpaceCode()
    {
        PlateIdentifier plateId1 = new PlateIdentifier("28948045-348", null);
        PlateIdentifier plateId2 = new PlateIdentifier("28948045-348", null);
        assertEquals(plateId1, plateId2);
        assertEquals(plateId1.hashCode(), plateId2.hashCode());
    }

}
