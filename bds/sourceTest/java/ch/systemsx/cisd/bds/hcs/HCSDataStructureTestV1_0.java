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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.DataStructureLoader;
import ch.systemsx.cisd.bds.DataStructureV1_0;
import ch.systemsx.cisd.bds.DataStructureV1_0Test;
import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.FormatParameter;
import ch.systemsx.cisd.bds.IDataStructure;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.Version;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link DataStructureV1_0} class specific to <i>HCS (High-Content
 * Screening) with Images</i>.
 * 
 * @author Christian Ribeaud
 */
public final class HCSDataStructureTestV1_0 extends AbstractFileSystemTestCase
{
    private FileStorage storage;

    private DataStructureV1_0 dataStructure;

    private final void setFormatAndFormatParameters()
    {
        dataStructure.setFormat(HCSImageFormatV1_0.HCS_IMAGE_1_0);
        dataStructure.addFormatParameter(new FormatParameter(
                HCSImageFormatV1_0.CONTAINS_ORIGINAL_DATA, Utilities.Boolean.TRUE));
        dataStructure.addFormatParameter(new FormatParameter(HCSImageFormatV1_0.NUMBER_OF_CHANNELS,
                new Integer(2)));
        dataStructure.addFormatParameter(new FormatParameter(PlateGeometry.PLATE_GEOMETRY,
                new PlateGeometry(2, 3)));
        dataStructure.addFormatParameter(new FormatParameter(WellGeometry.WELL_GEOMETRY,
                new WellGeometry(7, 5)));
    }

    //
    // AbstractFileSystemTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp() throws IOException
    {
        super.setUp();
        storage = new FileStorage(workingDirectory);
        dataStructure = new DataStructureV1_0(storage);
    }

    @Test
    public void testGetFormattedData()
    {
        dataStructure.create();
        final Format format = HCSImageFormatV1_0.HCS_IMAGE_1_0;
        try
        {
            dataStructure.getFormattedData();
            fail("Not all needed format parameters have been set.");
        } catch (DataStructureException ex)
        {
            // Nothing to do here
        }
        setFormatAndFormatParameters();
        final IFormattedData formattedData = dataStructure.getFormattedData();
        assertTrue(formattedData instanceof IHCSImageFormattedData);
        assertEquals(format, formattedData.getFormat());
    }

    @Test(dependsOnMethods = "testGetFormattedData")
    public final void testHCSImageDataStructure()
    {
        // Creating...
        dataStructure.create();
        DataStructureV1_0Test.createExampleDataStructure(storage, new Version(1, 0));
        setFormatAndFormatParameters();
        dataStructure.close();
        // And loading...
        final IDataStructure ds =
                new DataStructureLoader(workingDirectory.getParentFile())
                        .load(getClass().getName());
        assertNotNull(ds);
    }
}