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

package ch.ethz.sis.hcscld.examples;

import java.io.File;

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

/**
 * A demo program for writing and reading cell-level object tracking data.
 * 
 * @author Bernd Rinn
 */
public class CellLevelTrackingDemo
{
    public static void main(String[] args)
    {
        File f = new File("tracking.clt");
        f.delete();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelTrackingWritableDataset wds =
                writer.addTrackingDataset("abc", new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace cellA = wds.addObjectNamespace("CellType_A");
        ObjectNamespace cellB = wds.addObjectNamespace("CellType_B");
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
        trackingBuilder.addLink(3, 5);
        wds.writeObjectTracking(new ImageSequenceId(1, 2, 3), type, trackingBuilder);
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        ICellLevelTrackingDataset rds = reader.getDataSet("abc").toTrackingDataset();
        ObjectTrackingTypes types = rds.getObjectTrackingTypes();
        System.out.println(types);
        ObjectTrackingType typeR =
                types.get(rds.getObjectNamespace("CellType_A"), rds.getObjectNamespace("CellType_B"));
        System.out.println();
        ObjectTracking tracking = rds.getObjectTracking(new ImageSequenceId(1, 2, 3), typeR);
        System.out.println("P0 -> " + tracking.getChildIds(0));
        System.out.println("P1 -> " + tracking.getChildIds(1));
        System.out.println("P2 -> " + tracking.getChildIds(2));
        System.out.println("P3 -> " + tracking.getChildIds(3));
        System.out.println("P4 -> " + tracking.getChildIds(4));
        System.out.println("C0 -> " + tracking.getParentIds(0));
        System.out.println("C1 -> " + tracking.getParentIds(1));
        System.out.println("C2 -> " + tracking.getParentIds(2));
        System.out.println("C3 -> " + tracking.getParentIds(3));
        System.out.println("C4 -> " + tracking.getParentIds(4));
        System.out.println("C5 -> " + tracking.getParentIds(5));
        System.out.println("C6 -> " + tracking.getParentIds(6));
        System.out.println("C7 -> " + tracking.getParentIds(7));
        System.out.println("C8 -> " + tracking.getParentIds(8));
        reader.close();
    }
}
