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
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;

import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link WellGeometry} class.
 * 
 * @author Christian Ribeaud
 */
public final class WellGeometryTest extends AbstractFileSystemTestCase
{
    private final Geometry geometry = new WellGeometry(2, 3);

    @Test
    public final void testConstructor()
    {
        try
        {
            new WellGeometry(-1, 0);
            fail("Rows and columns must be > 0.");
        } catch (AssertionError ex)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testEquals()
    {
        assertEquals(new WellGeometry(2, 3), geometry);
        assertFalse(new WellGeometry(3, 2).equals(geometry));
        assertFalse(geometry.equals(null));
    }

    @Test
    public final void testSaveTo()
    {
        final IDirectory dir = NodeFactory.createDirectoryNode(workingDirectory);
        geometry.saveTo(dir);
        File[] files = workingDirectory.listFiles();
        assertEquals(1, files.length);
        final File geometryDir = files[0];
        assertTrue(geometryDir.isDirectory());
        assertEquals(WellGeometry.WELL_GEOMETRY, geometryDir.getName());
        files = geometryDir.listFiles();
        assertEquals(2, files.length);
        assertEquals(WellGeometry.COLUMNS, files[0].getName());
        assertEquals(WellGeometry.ROWS, files[1].getName());
    }

    @Test(dependsOnMethods = "testSaveTo")
    public final void testLoadFrom()
    {
        testSaveTo();
        final IDirectory dir = NodeFactory.createDirectoryNode(workingDirectory);
        final Geometry loaded = WellGeometry.loadFrom(dir);
        assertNotNull(loaded);
        assertTrue(loaded.getRows() == 2);
        assertTrue(loaded.getColumns() == 3);
    }

}