/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.hcs;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link Geometry} class.
 * 
 * @author Christian Ribeaud
 */
public final class GeometryTest
{

    @Test
    public final void testContainsLocation()
    {
        final Geometry geometry = new Geometry(2, 3);
        boolean fail = true;
        try
        {
            geometry.contains(null);
        } catch (AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
        assertTrue(geometry.contains(new Location(1, 2)));
        assertFalse(geometry.contains(new Location(1, 3)));
        assertTrue(geometry.contains(new Location(3, 1)));
        assertFalse(geometry.contains(new Location(4, 1)));
    }

    @Test
    public final void testCreateFromString()
    {
        boolean fail = true;
        try
        {
            Geometry.createFromString(null);
        } catch (AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
        assertNull(Geometry.createFromString("a"));
        assertNull(Geometry.createFromString("x4"));
        final Geometry geometry = Geometry.createFromString("1x4");
        assertNotNull(geometry);
        assertEquals(1, geometry.getRows());
        assertEquals(4, geometry.getColumns());
        fail = true;
        try
        {
            Geometry.createFromString("0x4");
        } catch (AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
    }
}