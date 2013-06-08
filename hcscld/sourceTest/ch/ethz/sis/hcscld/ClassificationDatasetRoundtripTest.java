/*
 * Copyright 2012-2013 ETH Zuerich, Scientific IT Services
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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.Arrays;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.ethz.sis.hcscld.CellLevelClassificationsEnum;
import ch.ethz.sis.hcscld.CellLevelDataFactory;
import ch.ethz.sis.hcscld.ICellLevelClassificationDataset;
import ch.ethz.sis.hcscld.ICellLevelClassificationWritableDataset;
import ch.ethz.sis.hcscld.ICellLevelDataReader;
import ch.ethz.sis.hcscld.ICellLevelDataWriter;
import ch.ethz.sis.hcscld.ImageId;
import ch.ethz.sis.hcscld.ImageQuantityStructure;
import ch.ethz.sis.hcscld.ObjectNamespace;

/**
 * Roundtrip test cases for classification datasets.
 * 
 * @author Bernd Rinn
 */
public class ClassificationDatasetRoundtripTest
{
    enum CellState
    {
        MITOTIC, APOPTOTIC, STEADY, DEAD
    }

    private static final File rootDirectory = new File("targets", "unit-test-wd");

    private static final File workingDirectory = new File(rootDirectory,
            "classification-roundtrip-wd");

    @BeforeSuite
    public void init()
    {
        workingDirectory.mkdirs();
        assertTrue(workingDirectory.isDirectory());
        workingDirectory.deleteOnExit();
        rootDirectory.deleteOnExit();
    }

    @Test
    public void testClassification()
    {
        File f = new File(workingDirectory, "classification.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelClassificationWritableDataset wds =
                writer.addClassificationDataset("456", new ImageQuantityStructure(1, 1, 3),
                        CellState.class);
        ObjectNamespace ns = wds.addObjectNamespace("main");
        wds.writeClassification(new ImageId(0, 0, 0), ns, new CellState[]
            { CellState.STEADY, CellState.APOPTOTIC, CellState.DEAD });
        wds.writeClassification(new ImageId(0, 0, 1), ns, new CellState[]
            { CellState.MITOTIC, CellState.DEAD, CellState.APOPTOTIC, CellState.STEADY });
        writer.close();

        ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        ICellLevelClassificationDataset rds = reader.getDataSet("456").toClassificationDataset();
        final String[] cls1 = rds.getClassifications(new ImageId(0, 0, 0));
        assertTrue(Arrays.toString(cls1), Arrays.equals(new String[]
            { "STEADY", "APOPTOTIC", "DEAD" }, cls1));
        final String[] cls2 = rds.getClassifications(new ImageId(0, 0, 1));
        assertTrue(Arrays.toString(cls2), Arrays.equals(new String[]
            { "MITOTIC", "DEAD", "APOPTOTIC", "STEADY" }, cls2));

        assertTrue(rds.hasClassifications(new ImageId(0, 0, 0)));
        assertTrue(rds.hasClassifications(new ImageId(0, 0, 1)));
        assertFalse(rds.hasClassifications(new ImageId(0, 0, 2)));
        for (CellLevelClassificationsEnum<CellState> clcs : rds.getClassifications(CellState.class))
        {
            if (clcs.getId().equals(new ImageId(0, 0, 0)))
            {
                assertTrue(Arrays.toString(clcs.getData()), Arrays.equals(new CellState[]
                    { CellState.STEADY, CellState.APOPTOTIC, CellState.DEAD }, clcs.getData()));
            } else if (clcs.getId().equals(new ImageId(0, 0, 1)))
            {
                assertTrue(
                        Arrays.toString(clcs.getData()),
                        Arrays.equals(new CellState[]
                            { CellState.MITOTIC, CellState.DEAD, CellState.APOPTOTIC,
                                    CellState.STEADY }, clcs.getData()));
            } else
            {
                fail("Unexpected id " + clcs.getId());
            }
        }
        reader.close();
    }

}
