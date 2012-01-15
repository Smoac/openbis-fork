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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.ethz.cisd.hcscld.Feature.FeatureDataType;
import ch.ethz.cisd.hcscld.ImageQuantityStructure.SequenceType;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.HDF5TimeDurationArray;
import ch.systemsx.cisd.hdf5.HDF5TimeUnit;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * Roundtrip test for feature datasets.
 * 
 * @author Bernd Rinn
 */
public class FeatureDatasetRoundtripTest
{
    private static final File rootDirectory = new File("targets", "unit-test-wd");

    private static final File workingDirectory = new File(rootDirectory, "feature-roundtrip-wd");

    @BeforeSuite
    public void init()
    {
        workingDirectory.mkdirs();
        assertTrue(workingDirectory.isDirectory());
        workingDirectory.deleteOnExit();
        rootDirectory.deleteOnExit();
    }

    enum State
    {
        A, B, C
    }

    private ICellLevelFeatureWritableDataset createDefaultFeatureGroupDataset(
            ICellLevelDataWriter writer, String dsCode, String objectNamespaceId,
            ImageQuantityStructure structure)
    {
        ICellLevelFeatureWritableDataset wds = writer.addFeatureDataset(dsCode, structure);
        ObjectNamespace namespace = wds.addObjectNamespace(objectNamespaceId);
        wds.createFeaturesDefinition().objectNamespace(namespace).addInt32Feature("one")
                .addFloat32Feature("two")
                .addEnumFeature("three", "State", Arrays.asList("A", "B", "C")).create();
        for (ImageId id : wds.getImageQuantityStructure())
        {
            wds.writeFeatures(id, createStandardValue(id));
        }
        return wds;
    }

    private void createMainFeatureGroupDataset(File file, String dsCode)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace namespace = wds.addObjectNamespace("main");
        IFeatureGroup fg =
                wds.createFeaturesDefinition().objectNamespace(namespace).addInt32Feature("one")
                        .addFloat32Feature("two")
                        .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                        .createFeatureGroup("main");
        for (ImageId id : wds.getImageQuantityStructure())
        {
            wds.writeFeatures(id, fg, createStandardValue(id));
        }
        writer.close();
    }

    private void createTwoFeatureGroupsSameNamespaceDataset(File file, String dsCode)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace namespace = wds.addObjectNamespace("cell");
        IFeatureGroup fg1 =
                wds.createFeaturesDefinition().objectNamespace(namespace).addInt32Feature("one")
                        .addFloat32Feature("two")
                        .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                        .createFeatureGroup("main");
        IFeatureGroup fg2 =
                wds.createFeaturesDefinition().objectNamespace(namespace).addBooleanFeature("ok")
                        .addStringFeature("comment", 10).createFeatureGroup("quality");
        for (ImageId id : wds.getImageQuantityStructure())
        {
            wds.writeFeatures(id, fg1, createStandardValue(id));
            wds.writeFeatures(id, fg2, createQualityValue1(id));
        }
        writer.close();
    }

    private void createTwoFeatureGroupsDifferentNamespacesDataset(File file, String dsCode)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace namespaceA = wds.addObjectNamespace("cell_a");
        ObjectNamespace namespaceB = wds.addObjectNamespace("cell_b");
        IFeatureGroup fg1 =
                wds.createFeaturesDefinition().objectNamespace(namespaceA).addInt32Feature("one")
                        .addFloat32Feature("two")
                        .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                        .createFeatureGroup("main");
        IFeatureGroup fg2 =
                wds.createFeaturesDefinition().objectNamespace(namespaceB).addBooleanFeature("ok")
                        .addStringFeature("comment", 10).createFeatureGroup("quality");
        for (ImageId id : wds.getImageQuantityStructure())
        {
            wds.writeFeatures(id, fg1, createStandardValue(id));
            wds.writeFeatures(id, fg2, createQualityValue2(id));
        }
        writer.close();
    }

    private Object[][] createStandardValue(ImageId id)
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

    private Object[][] createQualityValue1(ImageId id)
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

    private Object[][] createQualityValue2(ImageId id)
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
                { false, "9" },
                { true, "END" }, };
    }

    @Test
    public void testDefaultFeatureGroup()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "default.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelFeatureWritableDataset dsw =
                createDefaultFeatureGroupDataset(writer, dsCode, "cell",
                        new ImageQuantityStructure(2, 3, 4));
        dsw.setPlateBarcode("plate_abc");
        dsw.setParentDatasetCode("ds_xyz");
        final String now = new Date().toString();
        dsw.addDatasetAnnotation("dateOfCreation", now);
        writer.close();
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f).enumAsOrdinal();
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals("DEFAULT", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("one", "two", "three"), clf.getFeatureGroup()
                    .getFeatureNames());
            assertEquals("CELL", clf.getFeatureGroup().getNamespace().getId());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(3, clf.getValues()[i].length);
                assertEquals(clf.getWellFieldId().getRow() + i, clf.getValues()[i][0]);
                assertEquals(i + clf.getWellFieldId().getColumn() / 10f, clf.getValues()[i][1]);
                assertEquals(i % 3, clf.getValues()[i][2]);
            }
        }
        assertEquals("plate_abc", ds.tryGetPlateBarcode());
        assertEquals("ds_xyz", ds.tryGetParentDatasetCode());
        assertEquals(new HashSet<String>(Arrays.asList("dateOfCreation")),
                ds.getDatasetAnnotationKeys());
        assertEquals(now, ds.tryGetDatasetAnnotation("dateOfCreation"));
        assertNull(ds.tryGetDatasetAnnotation("creator"));
        reader.close();
    }

    @Test
    public void testDefaultFeatureGroupForSequence()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "sequence.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        ICellLevelFeatureWritableDataset dsw =
                createDefaultFeatureGroupDataset(writer, dsCode, "cell",
                        new ImageQuantityStructure(3, SequenceType.TIMESERIES, true));
        try
        {
            dsw.setDepthScanSequenceAnnotation(new DepthScanAnnotation("mm", new double[]
                { 2, 4, 6 }));
            fail("Attempt to add depth scan annotation to time series not detected.");
        } catch (WrongSequenceTypeException ex)
        {
            assertTrue(ex.getMessage(), ex.getMessage()
                    .contains(SequenceType.TIMESERIES.toString()));
        }
        try
        {
            dsw.setCustomSequenceAnnotation(new String[]
                { "One", "Two", "Three", "Four" });
            fail("Attempt to add annotation of wrong length not detected.");
        } catch (IllegalArgumentException ex)
        {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Wrong sequence length"));
        }
        dsw.setTimeSeriesSequenceAnnotation(HDF5TimeDurationArray.create(HDF5TimeUnit.MINUTES, 1,
                2, 4));
        dsw.setCustomSequenceAnnotation(new String[]
            { "One", "Two", "Three" });
        writer.close();

        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f).enumAsOrdinal();
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        assertEquals(new HDF5TimeDurationArray(new long[]
            { 1, 2, 4 }, HDF5TimeUnit.MINUTES), ds.tryGetTimeSeriesSequenceAnnotation());
        assertNull(ds.tryGetDepthScanSequenceAnnotation());
        assertEquals(Arrays.asList("One", "Two", "Three"),
                Arrays.asList(ds.tryGetCustomSequenceAnnotation()));
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals("DEFAULT", clf.getFeatureGroup().getId());
            assertEquals("CELL", clf.getFeatureGroup().getNamespace().getId());
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
        assertNull(ds.tryGetPlateBarcode());
        assertNull(ds.tryGetParentDatasetCode());
        assertTrue(ds.getDatasetAnnotationKeys().isEmpty());
        reader.close();
    }

    @Test
    public void testOneFeatureGroup()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "main.cld");
        f.delete();
        f.deleteOnExit();
        createMainFeatureGroupDataset(f, dsCode);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals("ALL", clf.getFeatureGroup().getId());
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
    public void testTwoFeatureGroupsSameNamespace()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "twoFeatureGroupsSameNamespace.cld");
        f.delete();
        f.deleteOnExit();
        createTwoFeatureGroupsSameNamespaceDataset(f, dsCode);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f).enumAsOrdinal();
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals("ALL", clf.getFeatureGroup().getId());
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
            assertEquals("MAIN", clf.getFeatureGroup().getId());
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
            assertEquals("QUALITY", clf.getFeatureGroup().getId());
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

    @Test
    public void testTwoFeatureGroupsDifferentNamespaces()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "twoFeatureGroupsDifferentNamespaces.cld");
        f.delete();
        f.deleteOnExit();
        createTwoFeatureGroupsDifferentNamespacesDataset(f, dsCode);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f).enumAsOrdinal();
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        for (CellLevelFeatures clf : ds.getValues(ds.getObjectNamespace("cell_a")))
        {
            assertEquals("ALL", clf.getFeatureGroup().getId());
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
        for (CellLevelFeatures clf : ds.getValues(ds.getObjectNamespace("cell_b")))
        {
            assertEquals("ALL", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("ok", "comment"), clf.getFeatureGroup().getFeatureNames());
            assertEquals(Arrays.asList(new Feature("ok", FeatureDataType.BOOL), new Feature(
                    "comment", 10)), clf.getFeatureGroup().getFeatures());
            assertEquals(11, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(Integer.toString(i), 2, clf.getValues()[i].length);
                assertEquals(Integer.toString(i), i % 2 == 0, clf.getValues()[i][0]);
                assertEquals(Integer.toString(i), i % 2 != 0 ? Integer.toString(i)
                        : (i == 10 ? "END" : ""), clf.getValues()[i][1]);
            }
        }
        for (CellLevelFeatures clf : ds.getValues(ds.getFeatureGroup("main")))
        {
            assertEquals("MAIN", clf.getFeatureGroup().getId());
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
            assertEquals("QUALITY", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("ok", "comment"), clf.getFeatureGroup().getFeatureNames());
            assertEquals(Arrays.asList(new Feature("ok", FeatureDataType.BOOL), new Feature(
                    "comment", 10)), clf.getFeatureGroup().getFeatures());
            assertEquals(11, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(Integer.toString(i), 2, clf.getValues()[i].length);
                assertEquals(Integer.toString(i), i % 2 == 0, clf.getValues()[i][0]);
                assertEquals(Integer.toString(i), i % 2 != 0 ? Integer.toString(i)
                        : (i == 10 ? "END" : ""), clf.getValues()[i][1]);
            }
        }
        reader.close();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testTwoFeatureGroupsInconsistentLength()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "twoFeatureGroupsInconsistentLength.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        try
        {
            ICellLevelFeatureWritableDataset wds =
                    writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
            ObjectNamespace namespace = wds.addObjectNamespace("main");
            IFeatureGroup fg1 =
                    wds.createFeaturesDefinition().objectNamespace(namespace).addInt32Feature("one")
                            .addFloat32Feature("two")
                            .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                            .createFeatureGroup("main");
            IFeatureGroup fg2 =
                    wds.createFeaturesDefinition().objectNamespace(namespace).addBooleanFeature("ok")
                            .addStringFeature("comment", 10).createFeatureGroup("quality");
            for (ImageId id : wds.getImageQuantityStructure())
            {
                wds.writeFeatures(id, fg1, createStandardValue(id));
                Object[][] quality = createQualityValue2(id);
                wds.writeFeatures(id, fg2, quality);
            }
        } finally
        {
            writer.close();
        }
    }

    @Test(expectedExceptions = UnsupportedFileFormatException.class)
    public void testIllegalFileFormat()
    {
        final File f = new File(workingDirectory, "wantabee.cld");
        f.delete();
        f.deleteOnExit();
        final IHDF5Writer writer = HDF5Factory.open(f);
        writer.writeString("message", "I am not a CLD file.");
        try
        {
            CellLevelDataFactory.open(writer);
        } finally
        {
            writer.close();
        }
    }
}
