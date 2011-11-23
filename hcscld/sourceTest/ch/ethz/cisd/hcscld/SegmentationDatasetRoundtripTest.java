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

package ch.ethz.cisd.hcscld;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Roundtrip test for segmentation datasets.
 * 
 * @author Bernd Rinn
 */
public class SegmentationDatasetRoundtripTest
{

    private static final File rootDirectory = new File("targets", "unit-test-wd");

    private static final File workingDirectory = new File(rootDirectory, "segmentation-roundtrip-wd");

    @BeforeSuite
    public void init()
    {
        workingDirectory.mkdirs();
        assertTrue(workingDirectory.isDirectory());
        workingDirectory.deleteOnExit();
        rootDirectory.deleteOnExit();
    }

    @Test
    public void testSegmentation()
    {
        File f = new File(workingDirectory, "segmentation.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelSegmentationWritableDataset wds =
                writer.addSegmentationDataset("789", "cell", new ImageQuantityStructure(2, 3, 4),
                        new ImageGeometry(1024, 1024), true);
        List<SegmentedObject> cells =
                Arrays.asList(
                        new SegmentedObject((short) 50, (short) 60, (short) 100, (short) 110),
                        new SegmentedObject((short) 200, (short) 220, (short) 220, (short) 240));
        cells.get(0).setMaskPoint(70, 80);
        cells.get(1).setMaskPoint(220, 240);
        wds.addObjectType("cell");
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cells);
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        ICellLevelSegmentationDataset rds = reader.getDataSet("789").toSegmentationDataset();
        assertEquals("789", rds.getDatasetCode());
        final ObjectType[] objectTypes = rds.getObjectTypes();
        assertEquals(2, objectTypes.length);
        assertEquals("MIXED", objectTypes[0].getId());
        assertEquals("789", objectTypes[0].getDatasetCode());
        assertEquals("segmentation.cld", objectTypes[0].getFile().getName());
        assertEquals(Collections.singleton(objectTypes[0]), objectTypes[0].getCompanions());
        assertEquals("CELL", objectTypes[1].getId());
        assertEquals("789", objectTypes[1].getDatasetCode());
        assertEquals("segmentation.cld", objectTypes[1].getFile().getName());
        assertEquals(Collections.singleton(objectTypes[1]), objectTypes[1].getCompanions());
        assertEquals(new ImageQuantityStructure(2, 3, 4), rds.getImageQuantityStructure());
        assertEquals(new ImageGeometry(1024, 1024), rds.getImageGeometry());
        SegmentedObject[] objects = rds.getObjects(new ImageId(1, 2, 3), true);
        assertEquals(2, objects.length);
        assertEquals(50, objects[0].getLeftUpperX());
        assertEquals(60, objects[0].getLeftUpperY());
        assertEquals(100, objects[0].getRightLowerX());
        assertEquals(110, objects[0].getRightLowerY());
        assertTrue(objects[0].getMaskPoint(70, 80));
        assertFalse(objects[0].getMaskPoint(69, 80));
        assertFalse(objects[1].getMaskPoint(1000, 1000));
        assertEquals(200, objects[1].getLeftUpperX());
        assertEquals(220, objects[1].getLeftUpperY());
        assertEquals(220, objects[1].getRightLowerX());
        assertEquals(240, objects[1].getRightLowerY());
        assertTrue(objects[1].getMaskPoint(220, 240));
        assertFalse(objects[1].getMaskPoint(200, 220));
        assertFalse(objects[1].getMaskPoint(0, 0));
        reader.close();
    }

    @Test(expectedExceptions = UninitalizedSegmentationException.class)
    public void testUninitializedSegmentation()
    {
        File f = new File("uninitalizedSegmentation.h5");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        try
        {
            ICellLevelSegmentationWritableDataset wds =
                    writer.addSegmentationDataset("789", "cell", new ImageQuantityStructure(2, 3, 4),
                            new ImageGeometry(1024, 1024), true);
            wds.writeImageSegmentation(new ImageId(1, 2, 3),
                    Arrays.asList(new SegmentedObject()));
        } finally
        {
            writer.close();
        }

    }
}
