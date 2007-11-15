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

}
