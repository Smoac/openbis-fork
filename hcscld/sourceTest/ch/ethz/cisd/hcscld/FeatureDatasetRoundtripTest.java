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

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.cisd.hcscld.Feature.FeatureDataType;

/**
 * Roundtrip test for feature datasets.
 * 
 * @author Bernd Rinn
 */
public class FeatureDatasetRoundtripTest
{
    enum State
    {
        A, B, C
    }

    private void createDefaultFeatureGroupDataset(File file, String dsCode)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new WellFieldGeometry(2, 3, 4));
        wds.createFeaturesDefinition().addInt32Feature("one").addFloat32Feature("two")
                .addEnumFeature("three", "State", Arrays.asList("A", "B", "C")).create();
        for (WellFieldId id : wds.getGeometry())
        {
            wds.writeFeatures(id, createStandardValue(id));
        }
        writer.close();
    }

    private void createMainFeatureGroupDataset(File file, String dsCode)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new WellFieldGeometry(2, 3, 4));
        IFeatureGroup fg =
                wds.createFeaturesDefinition().addInt32Feature("one").addFloat32Feature("two")
                        .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                        .createFeatureGroup("main");
        for (WellFieldId id : wds.getGeometry())
        {
            wds.writeFeatures(id, fg, createStandardValue(id));
        }
        writer.close();
    }

    private void createTwoFeatureGroupsDataset(File file, String dsCode)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new WellFieldGeometry(2, 3, 4));
        IFeatureGroup fg1 =
                wds.createFeaturesDefinition().addInt32Feature("one").addFloat32Feature("two")
                        .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                        .createFeatureGroup("main");
        IFeatureGroup fg2 =
                wds.createFeaturesDefinition().addBooleanFeature("ok")
                        .addStringFeature("comment", 10).createFeatureGroup("quality");
        for (WellFieldId id : wds.getGeometry())
        {
            wds.writeFeatures(id, fg1, createStandardValue(id));
            wds.writeFeatures(id, fg2, createQualityValue(id));
        }
        writer.close();
    }

    private void createTwoFeatureGroupsDatasetInconsistentLength(File file, String dsCode)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        try
        {
            ICellLevelFeatureWritableDataset wds =
                    writer.addFeatureDataset(dsCode, new WellFieldGeometry(2, 3, 4));
            IFeatureGroup fg1 =
                    wds.createFeaturesDefinition().addInt32Feature("one").addFloat32Feature("two")
                            .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                            .createFeatureGroup("main");
            IFeatureGroup fg2 =
                    wds.createFeaturesDefinition().addBooleanFeature("ok")
                            .addStringFeature("comment", 10).createFeatureGroup("quality");
            for (WellFieldId id : wds.getGeometry())
            {
                wds.writeFeatures(id, fg1, createStandardValue(id));
                Object[][] quality = createQualityValue(id);
                Object[][] qualityTruncated = new Object[9][];
                System.arraycopy(quality, 0, qualityTruncated, 0, qualityTruncated.length);
                wds.writeFeatures(id, fg2, qualityTruncated);
            }
        } finally
        {
            writer.close();
        }
    }

    private Object[][] createStandardValue(WellFieldId id)
    {
        return new Object[][]
            {
                { id.getRow(), 0 + id.getColumn() / 10f, State.A },
                { id.getRow() + 1, 1 + id.getColumn() / 10f, "B" },
                { id.getRow() + 2, 2 + id.getColumn() / 10f, "C" },
                { id.getRow() + 3, 3 + id.getColumn() / 10f, "A" },
                { id.getRow() + 4, 4 + id.getColumn() / 10f, 1 },
                { id.getRow() + 5, 5 + id.getColumn() / 10f, "C" },
                { id.getRow() + 6, 6 + id.getColumn() / 10f, 0 },
                { id.getRow() + 7, 7 + id.getColumn() / 10f, "B" },
                { id.getRow() + 8, 8 + id.getColumn() / 10f, State.C },
                { id.getRow() + 9, 9 + id.getColumn() / 10f, "A" } };
    }

    private Object[][] createQualityValue(WellFieldId id)
    {
        return new Object[][]
            {
                { true, "" },
                { false, "1" },
                { true, "" },
                { false, "3" },
                { true, "" },
                { false, "5" },
                { true, "" },
                { false, "7" },
                { true, "" },
                { false, "9" }, };
    }

    @Test
    public void testDefaultFeatureGroup()
    {
        final String dsCode = "123";
        final File f = new File("default.h5");
        f.delete();
        f.deleteOnExit();
        createDefaultFeatureGroupDataset(f, dsCode);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f).enumAsOrdinal();
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals("default", clf.getFeatureGroup().getName());
            assertEquals(Arrays.asList("one", "two", "three"), clf.getFeatureGroup()
                    .getFeatureNames());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(3, clf.getValues()[i].length);
                assertEquals(clf.getWellFieldId().getRow() + i, clf.getValues()[i][0]);
                assertEquals(i + clf.getWellFieldId().getColumn() / 10f, clf.getValues()[i][1]);
                assertEquals(i % 3, clf.getValues()[i][2]);
            }
        }
        reader.close();
    }

    @Test
    public void testOneFeatureGroup()
    {
        final String dsCode = "123";
        final File f = new File("main.h5");
        f.delete();
        f.deleteOnExit();
        createMainFeatureGroupDataset(f, dsCode);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals("all", clf.getFeatureGroup().getName());
            assertEquals(Arrays.asList("one", "two", "three"), clf.getFeatureGroup()
                    .getFeatureNames());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(3, clf.getValues()[i].length);
                assertEquals(clf.getWellFieldId().getRow() + i, clf.getValues()[i][0]);
                assertEquals(i + clf.getWellFieldId().getColumn() / 10f, clf.getValues()[i][1]);
                assertEquals(State.values()[i % 3].toString(), clf.getValues()[i][2]);
            }
        }
        reader.close();
    }

    @Test
    public void testTwoFeatureGroups()
    {
        final String dsCode = "123";
        final File f = new File("twogroups.h5");
        f.delete();
        f.deleteOnExit();
        createTwoFeatureGroupsDataset(f, dsCode);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f).enumAsOrdinal();
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals("all", clf.getFeatureGroup().getName());
            assertEquals(Arrays.asList("one", "two", "three", "ok", "comment"), clf
                    .getFeatureGroup().getFeatureNames());
            assertEquals(Arrays.asList(new Feature("one", FeatureDataType.INT32), new Feature(
                    "two", FeatureDataType.FLOAT32), new Feature("three", new String[]
                { "A", "B", "C" }), new Feature("ok", FeatureDataType.BOOL), new Feature("comment",
                    10)), clf.getFeatureGroup().getFeatures());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(Integer.toString(i), 5, clf.getValues()[i].length);
                assertEquals(Integer.toString(i), clf.getWellFieldId().getRow() + i,
                        clf.getValues()[i][0]);
                assertEquals(Integer.toString(i), i + clf.getWellFieldId().getColumn() / 10f,
                        clf.getValues()[i][1]);
                assertEquals(Integer.toString(i), i % 3, clf.getValues()[i][2]);
                assertEquals(Integer.toString(i), i % 2 == 0, clf.getValues()[i][3]);
                assertEquals(Integer.toString(i), i % 2 != 0 ? Integer.toString(i) : "",
                        clf.getValues()[i][4]);
            }
        }

        for (CellLevelFeatures clf : ds.getValues(ds.getFeatureGroup("main")))
        {
            assertEquals("main", clf.getFeatureGroup().getName());
            assertEquals(Arrays.asList("one", "two", "three"), clf.getFeatureGroup()
                    .getFeatureNames());
            assertEquals(Arrays.asList(new Feature("one", FeatureDataType.INT32), new Feature(
                    "two", FeatureDataType.FLOAT32), new Feature("three", new String[]
                { "A", "B", "C" })), clf.getFeatureGroup().getFeatures());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(Integer.toString(i), 3, clf.getValues()[i].length);
                assertEquals(Integer.toString(i), clf.getWellFieldId().getRow() + i,
                        clf.getValues()[i][0]);
                assertEquals(Integer.toString(i), i + clf.getWellFieldId().getColumn() / 10f,
                        clf.getValues()[i][1]);
                assertEquals(Integer.toString(i), i % 3, clf.getValues()[i][2]);
            }
        }

        for (CellLevelFeatures clf : ds.getValues(ds.getFeatureGroup("quality")))
        {
            assertEquals("quality", clf.getFeatureGroup().getName());
            assertEquals(Arrays.asList("ok", "comment"), clf.getFeatureGroup().getFeatureNames());
            assertEquals(Arrays.asList(new Feature("ok", FeatureDataType.BOOL), new Feature(
                    "comment", 10)), clf.getFeatureGroup().getFeatures());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(Integer.toString(i), 2, clf.getValues()[i].length);
                assertEquals(Integer.toString(i), i % 2 == 0, clf.getValues()[i][0]);
                assertEquals(Integer.toString(i), i % 2 != 0 ? Integer.toString(i) : "",
                        clf.getValues()[i][1]);
            }
        }
        reader.close();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testTwoFeatureGroupsInconsistentLength()
    {
        final String dsCode = "123";
        final File f = new File("twogroups.h5");
        f.delete();
        f.deleteOnExit();
        createTwoFeatureGroupsDatasetInconsistentLength(f, dsCode);
    }
}
