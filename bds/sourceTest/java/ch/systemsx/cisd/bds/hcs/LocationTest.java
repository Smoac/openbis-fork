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
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link Location} class.
 * 
 * @author Christian Ribeaud
 */
public final class LocationTest
{

    @Test
    public final void testConstructor()
    {
        boolean fail = true;
        try
        {
            new Location(-1, 0);
        } catch (AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
        fail = true;
        try
        {
            new Location(1, 0);
        } catch (AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
        fail = true;
        try
        {
            new Location(0, 1);
        } catch (AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
    }

    @Test
    public final void testEquals()
    {
        final Location location = new Location(2, 3);
        assertEquals(new Location(2, 3), location);
        assertFalse(new Location(3, 2).equals(location));
        assertFalse(location.equals(null));
    }

    @Test
    public final void testCreateLocationFromPosition()
    {
        final Geometry geometry = new Geometry(4, 5);
        boolean fail = true;
        try
        {
            Location.tryCreateLocationFromPosition(1, null);
        } catch (AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
        assertEquals(new Location(2, 3), Location.tryCreateLocationFromPosition(12, geometry));
        assertEquals(new Location(5, 3), Location.tryCreateLocationFromPosition(15, geometry));
        assertEquals(new Location(2, 1), Location.tryCreateLocationFromPosition(2, geometry));
        assertEquals(new Location(1, 2), Location.tryCreateLocationFromPosition(6, geometry));
        assertNull(Location.tryCreateLocationFromPosition(100, geometry));
    }

    @Test
    public final void testCreateLocationFromMatrixCoordinate()
    {
        assertNull(Location.tryCreateLocationFromMatrixCoordinate(""));
        assertNull(Location.tryCreateLocationFromMatrixCoordinate("8"));
        assertNull(Location.tryCreateLocationFromMatrixCoordinate("M"));
        assertEquals(new Location(2, 1), Location.tryCreateLocationFromMatrixCoordinate("A02"));
        assertEquals(new Location(7, 26), Location.tryCreateLocationFromMatrixCoordinate("z7"));
        assertEquals(new Location(34, 15), Location.tryCreateLocationFromMatrixCoordinate("O34"));
    }

    @Test
    public final void testCreateMatrixCoordinateFromLocation()
    {
        assertEquals("A1", Location.tryCreateMatrixCoordinateFromLocation(new Location(1, 1)));
        assertEquals("A2", Location.tryCreateMatrixCoordinateFromLocation(new Location(2, 1)));
        assertEquals("Z7", Location.tryCreateMatrixCoordinateFromLocation(new Location(7, 26)));
        assertEquals("O34", Location.tryCreateMatrixCoordinateFromLocation(new Location(34, 15)));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public final void testCreateMatrixCoordinateFromTooBigNumber()
    {
        Location.tryCreateMatrixCoordinateFromLocation(new Location(134, 27));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public final void testCreateMatrixCoordinateFromNullLocation()
    {
        Location.tryCreateMatrixCoordinateFromLocation(null);
    }
}
