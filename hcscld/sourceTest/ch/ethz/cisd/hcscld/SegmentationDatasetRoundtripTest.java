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
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * Roundtrip test for segmentation datasets.
 * 
 * @author Bernd Rinn
 */
public class SegmentationDatasetRoundtripTest
{

    private static final File rootDirectory = new File("targets", "unit-test-wd");

    private static final File workingDirectory = new File(rootDirectory,
            "segmentation-roundtrip-wd");

    @BeforeSuite
    public void init()
    {
        workingDirectory.mkdirs();
        assertTrue(workingDirectory.isDirectory());
        workingDirectory.deleteOnExit();
        rootDirectory.deleteOnExit();
    }

    @Test
    public void testSegmentationOneObjectType()
    {
        File f = new File(workingDirectory, "segmentationOneObjectType.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelSegmentationWritableDataset wds =
                writer.addSegmentationDataset("789", new ImageQuantityStructure(2, 3, 4),
                        new ImageGeometry(1024, 1024), true);
        List<SegmentedObject> cells =
                Arrays.asList(
                        new SegmentedObject((short) 50, (short) 60, (short) 100, (short) 110),
                        new SegmentedObject((short) 200, (short) 220, (short) 220, (short) 240));
        cells.get(0).setMaskPoint(70, 80);
        cells.get(1).setMaskPoint(220, 240);
        final ObjectType cellObjects = wds.addObjectType("cell");
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cellObjects, cells);
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        ICellLevelSegmentationDataset rds = reader.getDataSet("789").toSegmentationDataset();
        assertEquals("789", rds.getDatasetCode());
        final ObjectType[] objectTypes = rds.getObjectTypes();
        assertEquals(1, objectTypes.length);
        assertEquals("CELL", objectTypes[0].getId());
        assertEquals("789", objectTypes[0].getDatasetCode());
        assertEquals("segmentationOneObjectType.cld", objectTypes[0].getFile().getName());
        assertEquals(Collections.singleton(objectTypes[0]), objectTypes[0].getCompanions());
        assertEquals(new ImageQuantityStructure(2, 3, 4), rds.getImageQuantityStructure());
        assertEquals(new ImageGeometry(1024, 1024), rds.getImageGeometry());
        SegmentedObject[] objects = rds.getObjects(new ImageId(1, 2, 3), objectTypes[0], true);
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

    @Test
    public void testSegmentationTwoCompanionObjectTypes()
    {
        File f = new File(workingDirectory, "segmentationTwoCompanionObjectTypes.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelSegmentationWritableDataset wds =
                writer.addSegmentationDataset("789", new ImageQuantityStructure(2, 3, 4),
                        new ImageGeometry(1024, 1024), true);
        List<SegmentedObject> cells =
                Arrays.asList(
                        new SegmentedObject((short) 50, (short) 60, (short) 100, (short) 110),
                        new SegmentedObject((short) 200, (short) 220, (short) 220, (short) 240));
        cells.get(0).setMaskPoint(70, 80);
        cells.get(1).setMaskPoint(220, 240);
        final ObjectType cellObjects = wds.addObjectType("cell");
        List<SegmentedObject> nuclei =
                Arrays.asList(new SegmentedObject((short) 55, (short) 65, (short) 95, (short) 105),
                        new SegmentedObject((short) 205, (short) 225, (short) 215, (short) 235));
        nuclei.get(0).setMaskPoint(71, 81);
        nuclei.get(1).setMaskPoint(210, 230);
        final ObjectType nucleusObjects = wds.addObjectType("nucleus", cellObjects);
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cellObjects, cells);
        wds.writeImageSegmentation(new ImageId(1, 2, 3), nucleusObjects, nuclei);
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        ICellLevelSegmentationDataset rds = reader.getDataSet("789").toSegmentationDataset();
        assertEquals("789", rds.getDatasetCode());
        final ObjectType[] objectTypes = rds.getObjectTypes();
        assertEquals(2, objectTypes.length);
        assertEquals("CELL", objectTypes[0].getId());
        assertEquals("789", objectTypes[0].getDatasetCode());
        assertEquals("segmentationTwoCompanionObjectTypes.cld", objectTypes[0].getFile().getName());
        assertEquals("NUCLEUS", objectTypes[1].getId());
        assertEquals("789", objectTypes[1].getDatasetCode());
        assertEquals("segmentationTwoCompanionObjectTypes.cld", objectTypes[1].getFile().getName());
        assertEquals(new HashSet<ObjectType>(Arrays.asList(objectTypes)),
                objectTypes[0].getCompanions());
        assertEquals(new ImageQuantityStructure(2, 3, 4), rds.getImageQuantityStructure());
        assertEquals(new ImageGeometry(1024, 1024), rds.getImageGeometry());
        SegmentedObject[] cellsRead =
                rds.getObjects(new ImageId(1, 2, 3), rds.tryGetObjectType("cell"), true);
        assertEquals(2, cellsRead.length);
        assertEquals(50, cellsRead[0].getLeftUpperX());
        assertEquals(60, cellsRead[0].getLeftUpperY());
        assertEquals(100, cellsRead[0].getRightLowerX());
        assertEquals(110, cellsRead[0].getRightLowerY());
        assertTrue(cellsRead[0].getMaskPoint(70, 80));
        assertFalse(cellsRead[0].getMaskPoint(69, 80));
        assertFalse(cellsRead[1].getMaskPoint(1000, 1000));
        assertEquals(200, cellsRead[1].getLeftUpperX());
        assertEquals(220, cellsRead[1].getLeftUpperY());
        assertEquals(220, cellsRead[1].getRightLowerX());
        assertEquals(240, cellsRead[1].getRightLowerY());
        assertTrue(cellsRead[1].getMaskPoint(220, 240));
        assertFalse(cellsRead[1].getMaskPoint(200, 220));
        assertFalse(cellsRead[1].getMaskPoint(0, 0));

        SegmentedObject[] nucleiRead =
                rds.getObjects(new ImageId(1, 2, 3), rds.tryGetObjectType("nucleus"), true);
        assertEquals(2, nucleiRead.length);
        assertEquals(55, nucleiRead[0].getLeftUpperX());
        assertEquals(65, nucleiRead[0].getLeftUpperY());
        assertEquals(95, nucleiRead[0].getRightLowerX());
        assertEquals(105, nucleiRead[0].getRightLowerY());
        assertTrue(nucleiRead[0].getMaskPoint(71, 81));
        assertFalse(nucleiRead[0].getMaskPoint(70, 80));
        assertFalse(nucleiRead[1].getMaskPoint(1000, 1000));
        assertEquals(205, nucleiRead[1].getLeftUpperX());
        assertEquals(225, nucleiRead[1].getLeftUpperY());
        assertEquals(215, nucleiRead[1].getRightLowerX());
        assertEquals(235, nucleiRead[1].getRightLowerY());
        assertTrue(nucleiRead[1].getMaskPoint(210, 230));
        assertFalse(nucleiRead[1].getMaskPoint(200, 220));
        assertFalse(nucleiRead[1].getMaskPoint(0, 0));
        reader.close();
    }

    // FIXME: this should be a more specific exception
    @Test(expectedExceptions = RuntimeException.class)
    public void testSegmentationTwoCompanionObjectTypesInconsistentLength()
    {
        File f =
                new File(workingDirectory,
                        "segmentationTwoCompanionObjectTypesInconsistentLength.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelSegmentationWritableDataset wds =
                writer.addSegmentationDataset("789", new ImageQuantityStructure(2, 3, 4),
                        new ImageGeometry(1024, 1024), true);
        List<SegmentedObject> cells =
                Arrays.asList(
                        new SegmentedObject((short) 50, (short) 60, (short) 100, (short) 110),
                        new SegmentedObject((short) 200, (short) 220, (short) 220, (short) 240));
        cells.get(0).setMaskPoint(70, 80);
        cells.get(1).setMaskPoint(220, 240);
        final ObjectType cellObjects = wds.addObjectType("cell");
        List<SegmentedObject> nuclei =
                Arrays.asList(new SegmentedObject((short) 55, (short) 65, (short) 95, (short) 105));
        nuclei.get(0).setMaskPoint(71, 81);
        final ObjectType nucleusObjects = wds.addObjectType("nucleus", cellObjects);
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cellObjects, cells);
        wds.writeImageSegmentation(new ImageId(1, 2, 3), nucleusObjects, nuclei);
        writer.close();

    }

    @Test
    public void testSegmentationObjectTypeNotWritten()
    {
        File f = new File(workingDirectory, "segmentationObjectTypeNotWritten.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelSegmentationWritableDataset wds =
                writer.addSegmentationDataset("789", new ImageQuantityStructure(2, 3, 4),
                        new ImageGeometry(1024, 1024), true);
        List<SegmentedObject> cells =
                Arrays.asList(
                        new SegmentedObject((short) 50, (short) 60, (short) 100, (short) 110),
                        new SegmentedObject((short) 200, (short) 220, (short) 220, (short) 240));
        cells.get(0).setMaskPoint(70, 80);
        cells.get(1).setMaskPoint(220, 240);
        final ObjectType cellObjects = wds.addObjectType("cell");
        wds.addObjectType("nucleus", cellObjects);
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cellObjects, cells);
        try
        {
            writer.close();
            fail("Forgotten object type not spotted.");
        } catch (IOExceptionUnchecked ex)
        {
            assertEquals("Dataset 789: Object types not written: NUCLEUS.", ex.getCause()
                    .getMessage());
        }

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        assertTrue(reader.getDataSets().isEmpty());
        reader.close();
    }

    @Test(expectedExceptions = UninitalizedSegmentationException.class)
    public void testUninitializedSegmentation()
    {
        File f = new File(workingDirectory, "uninitalizedSegmentation.h5");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelSegmentationWritableDataset wds =
                writer.addSegmentationDataset("789", new ImageQuantityStructure(2, 3, 4),
                        new ImageGeometry(1024, 1024), true);
        final ObjectType cellObjects = wds.addObjectType("cell");
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cellObjects,
                Arrays.asList(new SegmentedObject()));
    }
}
