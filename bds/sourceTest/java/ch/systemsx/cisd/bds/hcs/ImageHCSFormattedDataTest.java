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
 * Test cases for corresponding {@link ImageHCSFormattedData} class.
 * 
 * @author Christian Ribeaud
 */
public class ImageHCSFormattedDataTest
{

    @Test
    public final void testCreateWellFileName()
    {
        try
        {
            ImageHCSFormattedData.createWellFileName(null);
            fail("Location can not be null.");
        } catch (AssertionError ex)
        {
            // Nothing to do here.
        }
        final Location location = new Location(1, 2);
        final String expected = "row2_column1.tiff";
        assertEquals(expected, ImageHCSFormattedData.createWellFileName(location));
    }
}