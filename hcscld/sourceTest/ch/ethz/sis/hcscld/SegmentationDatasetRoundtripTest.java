/*
 * Copyright 2011-2013 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.hcscld;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.ethz.sis.hcscld.CellLevelDataFactory;
import ch.ethz.sis.hcscld.ICellLevelDataReader;
import ch.ethz.sis.hcscld.ICellLevelDataWriter;
import ch.ethz.sis.hcscld.ICellLevelSegmentationDataset;
import ch.ethz.sis.hcscld.ICellLevelSegmentationWritableDataset;
import ch.ethz.sis.hcscld.ImageGeometry;
import ch.ethz.sis.hcscld.ImageId;
import ch.ethz.sis.hcscld.ImageQuantityStructure;
import ch.ethz.sis.hcscld.ObjectNamespace;
import ch.ethz.sis.hcscld.ObjectType;
import ch.ethz.sis.hcscld.SegmentedObject;
import ch.ethz.sis.hcscld.UninitalizedSegmentationException;
import ch.ethz.sis.hcscld.WrongNumberOfSegmentedObjectsException;
import ch.ethz.sis.hcscld.ImageQuantityStructure.SequenceType;
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
                        new SegmentedObject((short) 200, (short) 220, (short) 220, (short) 240),
                        new SegmentedObject((short) 400, (short) 300, (short) 500, (short) 600));
        cells.get(0).setMaskPoint(70, 80);
        cells.get(1).setMaskPoint(220, 240);
        final ObjectType cellObjects = wds.addObjectType("cell");
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cellObjects, cells);
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final List<ICellLevelDataset> datasets = reader.getDataSets();
        assertEquals(1, datasets.size());
        assertEquals("789", datasets.get(0).getDatasetCode());
        assertEquals(CellLevelDatasetType.SEGMENTATION, datasets.get(0).getType());
        ICellLevelSegmentationDataset rds = reader.getDataSet("789").toSegmentationDataset();
        assertEquals("789", rds.getDatasetCode());
        final Collection<ObjectType> objectTypes = rds.getObjectTypes();
        final Iterator<ObjectType> objectTypeIt = objectTypes.iterator();
        ObjectType type = objectTypeIt.next();
        assertEquals(1, objectTypes.size());
        assertEquals("CELL", type.getId());
        final ImageId[] imageIds = rds.getImageIds(type);
        assertEquals(1, imageIds.length);
        assertEquals(new ImageId(1, 2, 3), imageIds[0]);
        assertEquals("789", type.getDatasetCode());
        assertEquals(3, rds.getNumberOfSegmentedObjects(new ImageId(1, 2, 3), type));
        assertEquals("segmentationOneObjectType.cld", type.getFile().getName());
        assertEquals(Collections.singleton(type), type.getCompanions());
        assertEquals(new ImageQuantityStructure(2, 3, 4), rds.getImageQuantityStructure());
        assertEquals(new ImageGeometry(1024, 1024), rds.getImageGeometry());
        assertTrue(rds.hasObjects(new ImageId(1, 2, 3), type));
        assertFalse(rds.hasObjects(new ImageId(0, 0, 0), type));
        SegmentedObject[] objects = rds.getObjects(new ImageId(1, 2, 3), type, true);
        for (int i = 0; i < objects.length; ++i)
        {
            assertEquals(i, i, objects[i].getObjectIndex());
        }
        SegmentedObject so1 = rds.tryFindObject(new ImageId(1, 2, 3), 220, 240, false);
        assertNotNull(so1);
        assertEquals(1, so1.getObjectIndex());
        SegmentedObject so2 = rds.tryFindObject(new ImageId(1, 2, 3), 500, 600, false);
        assertNull(so2);
        assertEquals(3, objects.length);
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
        assertEquals(400, objects[2].getLeftUpperX());
        assertEquals(300, objects[2].getLeftUpperY());
        assertEquals(500, objects[2].getRightLowerX());
        assertEquals(600, objects[2].getRightLowerY());
        reader.close();
    }

    @Test
    public void testSegmentationTwoObjectTypesAsCompanions()
    {
        File f = new File(workingDirectory, "segmentationTwoObjectTypesAsCompanions.cld");
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
        final ObjectType nucleusObjects =
                wds.addObjectType("nucleus", cellObjects.getObjectNamespace());
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cellObjects, cells);
        wds.writeImageSegmentation(new ImageId(1, 2, 3), nucleusObjects, nuclei);
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        ICellLevelSegmentationDataset rds = reader.getDataSet("789").toSegmentationDataset();
        assertEquals("789", rds.getDatasetCode());
        final Collection<ObjectType> objectTypes = rds.getObjectTypes();
        assertEquals(2, objectTypes.size());
        final Iterator<ObjectType> objectTypeIt = objectTypes.iterator();
        ObjectType type = objectTypeIt.next();
        assertEquals("CELL", type.getId());
        assertEquals("789", type.getDatasetCode());
        assertEquals("segmentationTwoObjectTypesAsCompanions.cld", type.getFile().getName());
        type = objectTypeIt.next();
        assertEquals("NUCLEUS", type.getId());
        assertEquals("789", type.getDatasetCode());
        assertEquals("segmentationTwoObjectTypesAsCompanions.cld", type.getFile().getName());
        assertEquals(new HashSet<ObjectType>(objectTypes), type.getCompanions());
        final Collection<ObjectNamespace> namespaces = rds.getObjectNamespaces();
        assertEquals(1, namespaces.size());
        final ObjectNamespace cgroup = namespaces.iterator().next();
        assertEquals("CELL", cgroup.getId());
        assertEquals(new HashSet<ObjectType>(objectTypes), cgroup.getObjectTypes());
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

    @Test
    public void testSegmentationTwoObjectTypesSeparateCompanionGroups()
    {
        File f =
                new File(workingDirectory, "segmentationTwoObjectTypesSeparateCompanionGroups.cld");
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
        final ObjectType cellObjects = wds.addObjectType("cell_a");
        List<SegmentedObject> nuclei =
                Arrays.asList(new SegmentedObject((short) 55, (short) 65, (short) 95, (short) 105));
        nuclei.get(0).setMaskPoint(71, 81);
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cellObjects, cells);
        final ObjectType nucleusObjects = wds.addObjectType("cell_b");
        wds.writeImageSegmentation(new ImageId(1, 2, 3), nucleusObjects, nuclei);
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        ICellLevelSegmentationDataset rds = reader.getDataSet("789").toSegmentationDataset();
        assertEquals("789", rds.getDatasetCode());
        final Collection<ObjectType> objectTypes = rds.getObjectTypes();
        assertEquals(2, objectTypes.size());
        final Iterator<ObjectType> objectTypeIt = objectTypes.iterator();
        final ObjectType cellType = objectTypeIt.next();
        assertEquals("CELL_A", cellType.getId());
        assertEquals("789", cellType.getDatasetCode());
        assertEquals("segmentationTwoObjectTypesSeparateCompanionGroups.cld", cellType.getFile()
                .getName());
        assertEquals(new HashSet<ObjectType>(Collections.singleton(cellType)),
                cellType.getCompanions());
        final ObjectType nucleusType = objectTypeIt.next();
        assertEquals("CELL_B", nucleusType.getId());
        assertEquals("789", nucleusType.getDatasetCode());
        assertEquals("segmentationTwoObjectTypesSeparateCompanionGroups.cld", nucleusType.getFile()
                .getName());
        assertEquals(new HashSet<ObjectType>(Collections.singleton(nucleusType)),
                nucleusType.getCompanions());
        final Collection<ObjectNamespace> namespaces = rds.getObjectNamespaces();
        assertEquals(2, namespaces.size());
        final Iterator<ObjectNamespace> namespaceIt = namespaces.iterator();
        ObjectNamespace cgroup = namespaceIt.next();
        assertEquals("CELL_A", cgroup.getId());
        assertEquals(new HashSet<ObjectType>(Collections.singleton(cellType)),
                cgroup.getObjectTypes());
        cgroup = namespaceIt.next();
        assertEquals("CELL_B", cgroup.getId());
        assertEquals(new HashSet<ObjectType>(Collections.singleton(nucleusType)),
                cgroup.getObjectTypes());
        assertEquals(new ImageQuantityStructure(2, 3, 4), rds.getImageQuantityStructure());
        assertEquals(new ImageGeometry(1024, 1024), rds.getImageGeometry());
        SegmentedObject[] cellsARead =
                rds.getObjects(new ImageId(1, 2, 3), rds.tryGetObjectType("cell_a"), true);
        assertEquals(2, cellsARead.length);
        assertEquals(50, cellsARead[0].getLeftUpperX());
        assertEquals(60, cellsARead[0].getLeftUpperY());
        assertEquals(100, cellsARead[0].getRightLowerX());
        assertEquals(110, cellsARead[0].getRightLowerY());
        assertTrue(cellsARead[0].getMaskPoint(70, 80));
        assertFalse(cellsARead[0].getMaskPoint(69, 80));
        assertFalse(cellsARead[1].getMaskPoint(1000, 1000));
        assertEquals(200, cellsARead[1].getLeftUpperX());
        assertEquals(220, cellsARead[1].getLeftUpperY());
        assertEquals(220, cellsARead[1].getRightLowerX());
        assertEquals(240, cellsARead[1].getRightLowerY());
        assertTrue(cellsARead[1].getMaskPoint(220, 240));
        assertFalse(cellsARead[1].getMaskPoint(200, 220));
        assertFalse(cellsARead[1].getMaskPoint(0, 0));

        SegmentedObject[] cellsBRead =
                rds.getObjects(new ImageId(1, 2, 3), rds.tryGetObjectType("cell_b"), true);
        assertEquals(1, cellsBRead.length);
        assertEquals(55, cellsBRead[0].getLeftUpperX());
        assertEquals(65, cellsBRead[0].getLeftUpperY());
        assertEquals(95, cellsBRead[0].getRightLowerX());
        assertEquals(105, cellsBRead[0].getRightLowerY());
        assertTrue(cellsBRead[0].getMaskPoint(71, 81));
        assertFalse(cellsBRead[0].getMaskPoint(70, 80));
        reader.close();
    }

    @Test
    public void testSegmentationTwoImages()
    {
        File f = new File(workingDirectory, "segmentationTwoImages.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelSegmentationWritableDataset wds =
                writer.addSegmentationDataset("789", new ImageQuantityStructure(2, 3, 4),
                        new ImageGeometry(1024, 1024), true);
        List<SegmentedObject> cells1 =
                Arrays.asList(
                        new SegmentedObject((short) 50, (short) 60, (short) 100, (short) 110),
                        new SegmentedObject((short) 200, (short) 220, (short) 220, (short) 240));
        cells1.get(0).setMaskPoint(70, 80);
        cells1.get(1).setMaskPoint(220, 240);
        final ObjectType cellObjects = wds.addObjectType("cell");
        List<SegmentedObject> cells2 =
                Arrays.asList(new SegmentedObject((short) 55, (short) 65, (short) 95, (short) 105));
        cells2.get(0).setMaskPoint(71, 81);
        // Here we check that the length consistency check is done per image.
        wds.writeImageSegmentation(new ImageId(1, 1, 1), cellObjects, cells1);
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cellObjects, cells2);
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        ICellLevelSegmentationDataset rds = reader.getDataSet("789").toSegmentationDataset();
        assertEquals(cells1,
                Arrays.asList(rds.getObjects(new ImageId(1, 1, 1), cellObjects, false)));
        assertEquals(cells2,
                Arrays.asList(rds.getObjects(new ImageId(1, 2, 3), cellObjects, false)));
        reader.close();
    }

    @Test(expectedExceptions = WrongNumberOfSegmentedObjectsException.class)
    public void testSegmentationTwoImagesInSequence()
    {
        File f = new File(workingDirectory, "segmentationTwoImagesInSequence.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelSegmentationWritableDataset wds =
                writer.addSegmentationDataset("789", new ImageQuantityStructure(2,
                        SequenceType.TIMESERIES, true), new ImageGeometry(1024, 1024), true);
        List<SegmentedObject> cells1 =
                Arrays.asList(
                        new SegmentedObject((short) 50, (short) 60, (short) 100, (short) 110),
                        new SegmentedObject((short) 200, (short) 220, (short) 220, (short) 240));
        cells1.get(0).setMaskPoint(70, 80);
        cells1.get(1).setMaskPoint(220, 240);
        final ObjectType cellObjects = wds.addObjectType("cell");
        List<SegmentedObject> cells2 =
                Arrays.asList(new SegmentedObject((short) 55, (short) 65, (short) 95, (short) 105));
        cells2.get(0).setMaskPoint(71, 81);
        // Here we check that the length consistency check is done per sequence.
        wds.writeImageSegmentation(new ImageId(0), cellObjects, cells1);
        wds.writeImageSegmentation(new ImageId(1), cellObjects, cells2);
    }

    @Test(expectedExceptions = WrongNumberOfSegmentedObjectsException.class)
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
        final ObjectType nucleusObjects =
                wds.addObjectType("nucleus", cellObjects.getObjectNamespace());
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cellObjects, cells);
        wds.writeImageSegmentation(new ImageId(1, 2, 3), nucleusObjects, nuclei);
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
        wds.addObjectType("nucleus", cellObjects.getObjectNamespace());
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
    }

    @Test
    public void testSegmentationObjectNamespaceEmpty()
    {
        File f = new File(workingDirectory, "segmentationObjectNamespaceEmpty.cld");
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
        wds.addObjectNamespace("empty");
        wds.writeImageSegmentation(new ImageId(1, 2, 3), cellObjects, cells);
        try
        {
            writer.close();
            fail("Empty object namespace not spotted.");
        } catch (IOExceptionUnchecked ex)
        {
            assertEquals("Dataset 789: Empty object namespaces: EMPTY.", ex.getCause().getMessage());
        }
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
