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

package ch.ethz.cisd.hcscld;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

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

}
