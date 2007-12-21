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

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

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
        try
        {
            new Location(-1, 0);
            fail("Wrong parameters.");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
        try
        {
            new Location(1, 0);
            fail("Wrong parameters.");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
        try
        {
            new Location(0, 1);
            fail("Wrong parameters.");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
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
        try
        {
            Location.tryCreateLocationFromPosition(1, null);
            fail("Null geometry not allowed.");
        } catch (AssertionError ex)
        {
            // Nothing to do here.
        }
        assertEquals(new Location(2, 3), Location.tryCreateLocationFromPosition(12, geometry));
        assertEquals(new Location(5, 3), Location.tryCreateLocationFromPosition(15, geometry));
        assertEquals(new Location(2, 1), Location.tryCreateLocationFromPosition(2, geometry));
        assertEquals(new Location(1, 2), Location.tryCreateLocationFromPosition(6, geometry));
        try
        {
            Location.tryCreateLocationFromPosition(100, geometry);
            fail("Position is out of range.");
        } catch (AssertionError ex)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testCreateLocationFromMatrixCoordinate()
    {
        try
        {
            Location.tryCreateLocationFromMatrixCoordinate(null);
            fail("Coordinate can not be null.");
        } catch (AssertionError ex)
        {
            // Nothing to do here.
        }
        assertNull(Location.tryCreateLocationFromMatrixCoordinate(""));
        assertNull(Location.tryCreateLocationFromMatrixCoordinate("8"));
        assertNull(Location.tryCreateLocationFromMatrixCoordinate("M"));
        assertEquals(new Location(2, 1), Location.tryCreateLocationFromMatrixCoordinate("A02"));
        assertEquals(new Location(7, 26), Location.tryCreateLocationFromMatrixCoordinate("z7"));
        assertEquals(new Location(34, 15), Location.tryCreateLocationFromMatrixCoordinate("O34"));
    }
}
