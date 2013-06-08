/*
 * Copyright 2012 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.ethz.sis.hcscld.CellLevelDataFactory;
import ch.ethz.sis.hcscld.ICellLevelDataReader;
import ch.ethz.sis.hcscld.ICellLevelDataWriter;
import ch.ethz.sis.hcscld.ICellLevelTrackingDataset;
import ch.ethz.sis.hcscld.ICellLevelTrackingWritableDataset;
import ch.ethz.sis.hcscld.ImageQuantityStructure;
import ch.ethz.sis.hcscld.ImageSequenceId;
import ch.ethz.sis.hcscld.ObjectNamespace;
import ch.ethz.sis.hcscld.ObjectTracking;
import ch.ethz.sis.hcscld.ObjectTrackingBuilder;
import ch.ethz.sis.hcscld.ObjectTrackingType;
import ch.ethz.sis.hcscld.ObjectTrackingTypes;
import ch.ethz.sis.hcscld.ImageQuantityStructure.SequenceType;

/**
 * A roundtrip test for tracking datasets.
 * 
 * @author Bernd Rinn
 */
public class TrackingDatasetRoundtripTest
{
    private static final File rootDirectory = new File("targets", "unit-test-wd");

    private static final File workingDirectory = new File(rootDirectory, "tracking-roundtrip-wd");

    @BeforeSuite
    public void init()
    {
        workingDirectory.mkdirs();
        assertTrue(workingDirectory.isDirectory());
        workingDirectory.deleteOnExit();
        rootDirectory.deleteOnExit();
    }

    @Test
    public void testTrackingOneTypeNoSequence()
    {
        File f = new File(workingDirectory, "TrackingOneTypeNoSequence.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelTrackingWritableDataset wds =
                writer.addTrackingDataset("abc", new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace cellA = wds.addObjectNamespace("Cell_A");
        ObjectNamespace cellB = wds.addObjectNamespace("Cell_B");
        ObjectTrackingType type = wds.createObjectTrackingType(cellA, cellB);
        ObjectTrackingBuilder trackingBuilder = new ObjectTrackingBuilder();
        trackingBuilder.addLink(1, 5);
        trackingBuilder.addLink(1, 8);
        trackingBuilder.addLink(1, 1);
        trackingBuilder.addLink(0, 3);
        trackingBuilder.addLink(0, 2);
        trackingBuilder.addLink(0, 7);
        trackingBuilder.addLink(2, 7);
        trackingBuilder.addLink(2, 5);
        trackingBuilder.addLink(2, 6);
        wds.writeObjectTracking(new ImageSequenceId(1, 2, 3), type, trackingBuilder);
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        ICellLevelTrackingDataset rds = reader.getDataSet("abc").toTrackingDataset();
        ObjectTrackingTypes types = rds.getObjectTrackingTypes();
        assertEquals(1, types.list().size());
        ObjectTrackingType typeR =
                types.get(rds.getObjectNamespace("Cell_A"), rds.getObjectNamespace("Cell_B"));
        assertEquals(typeR, types.list().get(0));
        assertEquals("CELL_A", typeR.getParentObjectNamespace().getId());
        assertEquals("CELL_B", typeR.getChildObjectNamespace().getId());
        assertEquals(0, typeR.getParentImageSequenceIdx());
        assertEquals(0, typeR.getChildImageSequenceIdx());
        assertTrue(rds.hasObjectTracking(new ImageSequenceId(1, 2, 3), typeR));
        assertFalse(rds.hasObjectTracking(new ImageSequenceId(0, 0, 0), typeR));
        ObjectTracking tracking = rds.getObjectTracking(new ImageSequenceId(1, 2, 3), typeR);
        assertTrue(Arrays.equals(new int[]
            { 2, 3, 7 }, tracking.getChildIds(0).toArray()));
        assertTrue(Arrays.equals(new int[]
            { 1, 5, 8 }, tracking.getChildIds(1).toArray()));
        assertTrue(Arrays.equals(new int[]
            { 5, 6, 7 }, tracking.getChildIds(2).toArray()));
        assertTrue(tracking.getChildIds(3).isEmpty());

        reader.close();
    }

    @Test
    public void testTrackingNeighbors()
    {
        File f = new File(workingDirectory, "TrackingNeighbors.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelTrackingWritableDataset wds =
                writer.addTrackingDataset("abc", new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace cellA = wds.addObjectNamespace("Cell_A");
        ObjectTrackingType type = wds.createObjectTrackingType(cellA, cellA);
        ObjectTrackingBuilder trackingBuilder = new ObjectTrackingBuilder();
        trackingBuilder.addLink(1, 5);
        trackingBuilder.addLink(1, 8);
        trackingBuilder.addLink(0, 3);
        trackingBuilder.addLink(0, 2);
        trackingBuilder.addLink(0, 7);
        trackingBuilder.addLink(2, 0);
        trackingBuilder.addLink(3, 0);
        trackingBuilder.addLink(3, 5);
        trackingBuilder.addLink(5, 1);
        trackingBuilder.addLink(5, 3);
        trackingBuilder.addLink(7, 0);
        trackingBuilder.addLink(8, 1);
        wds.writeObjectTracking(new ImageSequenceId(1, 2, 3), type, trackingBuilder);
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        ICellLevelTrackingDataset rds = reader.getDataSet("abc").toTrackingDataset();
        ObjectTrackingTypes types = rds.getObjectTrackingTypes();
        assertEquals(1, types.list().size());
        ObjectTrackingType typeR =
                types.get(rds.getObjectNamespace("Cell_A"), rds.getObjectNamespace("Cell_A"));
        assertEquals(typeR, types.list().get(0));
        assertEquals("CELL_A", typeR.getParentObjectNamespace().getId());
        assertEquals("CELL_A", typeR.getChildObjectNamespace().getId());
        assertEquals(0, typeR.getParentImageSequenceIdx());
        assertEquals(0, typeR.getChildImageSequenceIdx());
        assertTrue(rds.hasObjectTracking(new ImageSequenceId(1, 2, 3), typeR));
        assertFalse(rds.hasObjectTracking(new ImageSequenceId(0, 0, 0), typeR));
        ObjectTracking tracking = rds.getObjectTracking(new ImageSequenceId(1, 2, 3), typeR);
        assertTrue(Arrays.equals(new int[]
            { 2, 3, 7 }, tracking.getChildIds(0).toArray()));
        assertTrue(Arrays.equals(new int[]
            { 5, 8 }, tracking.getChildIds(1).toArray()));
        assertTrue(Arrays.equals(new int[]
            { 0 }, tracking.getChildIds(2).toArray()));
        assertTrue(Arrays.equals(new int[]
                { 0, 5 }, tracking.getChildIds(3).toArray()));
        assertTrue(tracking.getChildIds(4).isEmpty());
        assertTrue(Arrays.equals(new int[]
                { 1, 3 }, tracking.getChildIds(5).toArray()));
        assertTrue(tracking.getChildIds(6).isEmpty());
        assertTrue(Arrays.equals(new int[]
                { 0 }, tracking.getChildIds(7).toArray()));
        assertTrue(Arrays.equals(new int[]
                { 1 }, tracking.getChildIds(8).toArray()));

        reader.close();
    }

    @Test
    public void testObjectTrackingTypeOrdering()
    {
        File f = new File(workingDirectory, "ObjectTrackingTypeOrdering.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelTrackingWritableDataset wds =
                writer.addTrackingDataset("abc", new ImageQuantityStructure(10,
                        SequenceType.TIMESERIES, false));
        ObjectNamespace cellA = wds.addObjectNamespace("Cell_A");
        ObjectNamespace cellB = wds.addObjectNamespace("Cell_B");
        ObjectTrackingType type0 = wds.createObjectTrackingType(cellB, 0, cellB, 1);
        ObjectTrackingType type1 = wds.createObjectTrackingType(cellA, 9, cellB, 9);
        ObjectTrackingType type2 = wds.createObjectTrackingType(cellA, 0, cellB, 0);
        ObjectTrackingType type3 = wds.createObjectTrackingType(cellA, 1, cellB, 1);
        ObjectTrackingType type4 = wds.createObjectTrackingType(cellA, 0, cellA, 5);
        ObjectTrackingType type5 = wds.createObjectTrackingType(cellA, 3, cellB, 3);
        ObjectTrackingType type6 = wds.createObjectTrackingType(cellA, 4, cellB, 4);
        ObjectTrackingType type7 = wds.createObjectTrackingType(cellA, 5, cellB, 5);
        ObjectTrackingType type8 = wds.createObjectTrackingType(cellA, 6, cellB, 6);
        ObjectTrackingType type9 = wds.createObjectTrackingType(cellA, 7, cellB, 7);
        ObjectTrackingType type10 = wds.createObjectTrackingType(cellA, 8, cellB, 8);
        ObjectTrackingType type11 = wds.createObjectTrackingType(cellA, 0, cellA, 1);
        ObjectTrackingType type12 = wds.createObjectTrackingType(cellA, 0, cellA, 2);
        ObjectTrackingType type13 = wds.createObjectTrackingType(cellA, 0, cellA, 3);
        ObjectTrackingType type14 = wds.createObjectTrackingType(cellA, 0, cellA, 4);
        ObjectTrackingType type15 = wds.createObjectTrackingType(cellA, 2, cellB, 2);
        ObjectTrackingType type16 = wds.createObjectTrackingType(cellA, 0, cellA, 6);
        ObjectTrackingType type17 = wds.createObjectTrackingType(cellA, 0, cellA, 7);
        ObjectTrackingType type18 = wds.createObjectTrackingType(cellA, 0, cellA, 8);
        ObjectTrackingType type19 = wds.createObjectTrackingType(cellA, 0, cellA, 9);
        ObjectTrackingBuilder trackingBuilder = new ObjectTrackingBuilder();
        trackingBuilder.addLink(1, 1);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type0, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type1, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type2, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type3, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type4, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type5, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type6, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type7, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type8, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type9, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type10, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type11, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type12, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type13, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type14, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type15, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type16, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type17, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type18, trackingBuilder);
        wds.writeObjectTracking(new ImageSequenceId(0, 0, 0), type19, trackingBuilder);
        ObjectTrackingTypes types = wds.getObjectTrackingTypes();
        assertNotNull(types.tryGet(cellA, cellB));
        assertNotNull(types.tryGet(cellA, 4, cellB, 4));
        assertNotNull(types.tryGet(cellA, 0, cellA, 8));
        assertNotNull(types.tryGet(cellB, 0, cellB, 1));
        assertNull(types.tryGet(cellB, 0, cellB, 2));
        assertNull(types.tryGet(cellA, 4, cellB, 6));
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        ICellLevelTrackingDataset rds = reader.getDataSet("abc").toTrackingDataset();
        types = rds.getObjectTrackingTypes();
        assertNotNull(types.tryGet(rds.getObjectNamespace("Cell_A"),
                rds.getObjectNamespace("Cell_B")));
        assertNotNull(types.tryGet(rds.getObjectNamespace("Cell_A"), 4,
                rds.getObjectNamespace("Cell_B"), 4));
        assertNotNull(types.tryGet(rds.getObjectNamespace("Cell_A"), 0,
                rds.getObjectNamespace("Cell_A"), 8));
        assertNotNull(types.tryGet(rds.getObjectNamespace("Cell_B"), 0,
                rds.getObjectNamespace("Cell_B"), 1));
        assertNull(types.tryGet(rds.getObjectNamespace("Cell_B"), 0,
                rds.getObjectNamespace("Cell_B"), 2));
        assertNull(types.tryGet(rds.getObjectNamespace("Cell_A"), 4,
                rds.getObjectNamespace("Cell_B"), 6));
        reader.close();
    }
}
