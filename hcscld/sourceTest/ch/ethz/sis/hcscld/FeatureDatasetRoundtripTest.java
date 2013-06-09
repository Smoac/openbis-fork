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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.ethz.sis.hcscld.CellLevelDataFactory;
import ch.ethz.sis.hcscld.CellLevelFeatures;
import ch.ethz.sis.hcscld.DepthScanAnnotation;
import ch.ethz.sis.hcscld.Feature;
import ch.ethz.sis.hcscld.ICellLevelDataReader;
import ch.ethz.sis.hcscld.ICellLevelDataWriter;
import ch.ethz.sis.hcscld.ICellLevelFeatureDataset;
import ch.ethz.sis.hcscld.ICellLevelFeatureWritableDataset;
import ch.ethz.sis.hcscld.IFeatureGroup;
import ch.ethz.sis.hcscld.ImageId;
import ch.ethz.sis.hcscld.ImageQuantityStructure;
import ch.ethz.sis.hcscld.ObjectNamespace;
import ch.ethz.sis.hcscld.UnsupportedFileFormatException;
import ch.ethz.sis.hcscld.WrongNumberOfSegmentedObjectsException;
import ch.ethz.sis.hcscld.WrongSequenceTypeException;
import ch.ethz.sis.hcscld.CellLevelFeatureDataset.FeatureGroupDescriptor;
import ch.ethz.sis.hcscld.Feature.FeatureDataType;
import ch.ethz.sis.hcscld.IFeatureGroup.FeatureGroupDataType;
import ch.ethz.sis.hcscld.ImageQuantityStructure.SequenceType;
import ch.systemsx.cisd.hdf5.HDF5CompoundMemberMapping;
import ch.systemsx.cisd.hdf5.HDF5CompoundType;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.HDF5TimeDurationArray;
import ch.systemsx.cisd.hdf5.HDF5TimeUnit;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
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
        wds.createFeaturesDefinition(namespace).addInt32Feature("one").addFloat32Feature("two")
                .addEnumFeature("three", "State", Arrays.asList("A", "B", "C")).create();
        for (ImageId id : wds.getImageQuantityStructure())
        {
            wds.writeFeatures(id, createStandardValue(id));
        }
        return wds;
    }

    private void createMainFeatureGroupDataset(File file, String dsCode, ImageId idOrNull)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace namespace = wds.addObjectNamespace("main");
        IFeatureGroup fg =
                wds.createFeaturesDefinition(namespace).addInt32Feature("one")
                        .addFloat32Feature("two")
                        .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                        .createFeatureGroup("main");
        if (idOrNull != null)
        {
            wds.writeFeatures(idOrNull, fg, createStandardValue(idOrNull));
        } else
        {
            for (ImageId id : wds.getImageQuantityStructure())
            {
                wds.writeFeatures(id, fg, createStandardValue(id));
            }
        }
        writer.close();
    }

    private void createMainFloat32FeatureGroupDataset(File file, String dsCode, ImageId idOrNull)
    {
        createMainFloat32FeatureGroupDataset(file, dsCode, idOrNull, false);
    }

    private void createMainFloat32FeatureGroupDataset(File file, String dsCode, ImageId idOrNull,
            boolean enforceCompoundStorage)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace namespace = wds.addObjectNamespace("main");
        IFeatureGroup fg =
                wds.createFeaturesDefinition(namespace).addFloat32Feature("a")
                        .addFloat32Feature("b").addFloat32Feature("c")
                        .enforceCompoundGroupStorageType(enforceCompoundStorage)
                        .createFeatureGroup("main");
        if (idOrNull != null)
        {
            if (enforceCompoundStorage)
            {
                wds.writeFeatures(idOrNull, fg, createStandardFloat32ValueAsObject(idOrNull));
            } else
            {
                wds.writeFeatures(idOrNull, fg, createStandardFloat32Value(idOrNull));
            }
        } else
        {
            for (ImageId id : wds.getImageQuantityStructure())
            {
                if (enforceCompoundStorage)
                {
                    wds.writeFeatures(id, fg, createStandardFloat32ValueAsObject(id));
                } else
                {
                    wds.writeFeatures(id, fg, createStandardFloat32Value(id));
                }
            }
        }
        writer.close();
    }

    private void createMainInt32FeatureGroupDataset(File file, String dsCode, ImageId idOrNull)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace namespace = wds.addObjectNamespace("main");
        IFeatureGroup fg =
                wds.createFeaturesDefinition(namespace).addInt32Feature("a").addInt32Feature("b")
                        .addInt32Feature("c").createFeatureGroup("main");
        if (idOrNull != null)
        {
            wds.writeFeatures(idOrNull, fg, createStandardInt32Value(idOrNull));
        } else
        {
            for (ImageId id : wds.getImageQuantityStructure())
            {
                wds.writeFeatures(id, fg, createStandardInt32Value(id));
            }
        }
        writer.close();
    }

    private void createMainEnumFeatureGroupDataset(File file, String dsCode, ImageId idOrNull)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace namespace = wds.addObjectNamespace("main");
        IFeatureGroup fg =
                wds.createFeaturesDefinition(namespace).addEnumFeature("a", State.class)
                        .addEnumFeature("b", State.class).addEnumFeature("c", State.class)
                        .createFeatureGroup("main");
        if (idOrNull != null)
        {
            wds.writeFeatures(idOrNull, fg, createStandardEnumValue(idOrNull));
        } else
        {
            for (ImageId id : wds.getImageQuantityStructure())
            {
                wds.writeFeatures(id, fg, createStandardEnumValue(id));
            }
        }
        writer.close();
    }

    private void createMainBoolFeatureGroupDataset(File file, String dsCode, ImageId idOrNull)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace namespace = wds.addObjectNamespace("main");
        IFeatureGroup fg =
                wds.createFeaturesDefinition(namespace).addBooleanFeature("a")
                        .addBooleanFeature("b").addBooleanFeature("c").createFeatureGroup("main");
        if (idOrNull != null)
        {
            wds.writeFeatures(idOrNull, fg, createStandardBoolValue(idOrNull));
        } else
        {
            for (ImageId id : wds.getImageQuantityStructure())
            {
                wds.writeFeatures(id, fg, createStandardBoolValue(id));
            }
        }
        writer.close();
    }

    private void createEmptyFeatureGroupDataset(File file, String dsCode, ImageId idOrNull)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
        writer.close();
    }

    private void createTwoFeatureGroupsSameNamespaceDataset(File file, String dsCode)
    {
        ICellLevelDataWriter writer = CellLevelDataFactory.open(file);
        ICellLevelFeatureWritableDataset wds =
                writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
        ObjectNamespace namespace = wds.addObjectNamespace("cell");
        IFeatureGroup fg1 =
                wds.createFeaturesDefinition(namespace).addInt32Feature("one")
                        .addFloat32Feature("two")
                        .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                        .createFeatureGroup("Main");
        IFeatureGroup fg2 =
                wds.createFeaturesDefinition(namespace).addBooleanFeature("ok")
                        .addStringFeature("comment", 10).createFeatureGroup("Quality");
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
                wds.createFeaturesDefinition(namespaceA).addInt32Feature("one")
                        .addFloat32Feature("two")
                        .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                        .createFeatureGroup("Main");
        IFeatureGroup fg2 =
                wds.createFeaturesDefinition(namespaceB).addBooleanFeature("ok")
                        .addStringFeature("comment", 10).createFeatureGroup("Quality");
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

    private float[][] createStandardFloat32Value(ImageId id)
    {
        return new float[][]
            {
                { id.getRow(), 0 + id.getColumn() / 10f, 1e0f },
                { id.getRow() + 1, 1 + id.getColumn() / 10f, 1e1f },
                { id.getRow() + 2, 2 + id.getColumn() / 10f, 1e2f },
                { id.getRow() + 3, 3 + id.getColumn() / 10f, 1e3f },
                { id.getRow() + 4, 4 + id.getColumn() / 10f, 1e4f },
                { id.getRow() + 5, 5 + id.getColumn() / 10f, 1e5f },
                { id.getRow() + 6, 6 + id.getColumn() / 10f, 1e6f },
                { id.getRow() + 7, 7 + id.getColumn() / 10f, 1e7f },
                { id.getRow() + 8, 8 + id.getColumn() / 10f, 1e8f },
                { id.getRow() + 9, 9 + id.getColumn() / 10f, 1e9f } };
    }

    private Object[][] createStandardFloat32ValueAsObject(ImageId id)
    {
        return new Object[][]
            {
                { id.getRow(), 0 + id.getColumn() / 10f, 1e0f },
                { id.getRow() + 1, 1 + id.getColumn() / 10f, 1e1f },
                { id.getRow() + 2, 2 + id.getColumn() / 10f, 1e2f },
                { id.getRow() + 3, 3 + id.getColumn() / 10f, 1e3f },
                { id.getRow() + 4, 4 + id.getColumn() / 10f, 1e4f },
                { id.getRow() + 5, 5 + id.getColumn() / 10f, 1e5f },
                { id.getRow() + 6, 6 + id.getColumn() / 10f, 1e6f },
                { id.getRow() + 7, 7 + id.getColumn() / 10f, 1e7f },
                { id.getRow() + 8, 8 + id.getColumn() / 10f, 1e8f },
                { id.getRow() + 9, 9 + id.getColumn() / 10f, 1e9f } };
    }

    private Object[][] createStandardInt32Value(ImageId id)
    {
        return new Object[][]
            {
                { id.getRow(), 0 + id.getColumn(), -5 },
                { id.getRow() + 1, 1 + id.getColumn(), -4 },
                { id.getRow() + 2, 2 + id.getColumn(), -3 },
                { id.getRow() + 3, 3 + id.getColumn(), -2 },
                { id.getRow() + 4, 4 + id.getColumn(), -1 },
                { id.getRow() + 5, 5 + id.getColumn(), 0 },
                { id.getRow() + 6, 6 + id.getColumn(), 1 },
                { id.getRow() + 7, 7 + id.getColumn(), 2 },
                { id.getRow() + 8, 8 + id.getColumn(), 3 },
                { id.getRow() + 9, 9 + id.getColumn(), 4 } };
    }

    private State[][] createStandardEnumValue(ImageId id)
    {
        return new State[][]
            {
                { State.A, State.B, State.C },
                { State.C, State.B, State.A },
                { State.A, State.C, State.B },
                { State.B, State.B, State.B }, };
    }

    private Object[][] createStandardBoolValue(ImageId id)
    {
        return new Object[][]
            {
                { true, true, true },
                { false, false, false },
                { true, false, true },
                { false, true, false }, };
    }

    private Object[][] createNonStandardValue(ImageId id)
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
                { id.getRow() + 9, 9 + id.getColumn() / 10f, "A" },
                { id.getRow() + 10, 10 + id.getColumn() / 10f, 1 } };
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
            assertEquals(FeatureGroupDataType.COMPOUND, clf.getFeatureGroup().getDataType());
            assertEquals("DEFAULT", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("one", "two", "three"), clf.getFeatureGroup()
                    .getFeatureNames());
            assertEquals("CELL", clf.getFeatureGroup().getNamespace().getId());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(3, clf.getValues()[i].length);
                assertEquals(clf.getImageId().getRow() + i, clf.getValues()[i][0]);
                assertEquals(i + clf.getImageId().getColumn() / 10f, clf.getValues()[i][1]);
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
            assertEquals(FeatureGroupDataType.COMPOUND, clf.getFeatureGroup().getDataType());
            assertEquals("DEFAULT", clf.getFeatureGroup().getId());
            assertEquals("CELL", clf.getFeatureGroup().getNamespace().getId());
            assertEquals(Arrays.asList("one", "two", "three"), clf.getFeatureGroup()
                    .getFeatureNames());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(3, clf.getValues()[i].length);
                assertEquals(clf.getImageId().getRow() + i, clf.getValues()[i][0]);
                assertEquals(i + clf.getImageId().getColumn() / 10f, clf.getValues()[i][1]);
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
        final File f = new File(workingDirectory, "oneFeatureGroup.cld");
        f.delete();
        f.deleteOnExit();
        createMainFeatureGroupDataset(f, dsCode, null);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        assertTrue(System.currentTimeMillis() - ds.getCreationDate().getTime() < 100);
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals(FeatureGroupDataType.COMPOUND, clf.getFeatureGroup().getDataType());
            assertEquals("All", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("one", "two", "three"), clf.getFeatureGroup()
                    .getFeatureNames());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(3, clf.getValues()[i].length);
                assertEquals(clf.getImageId().getRow() + i, clf.getValues()[i][0]);
                assertEquals(i + clf.getImageId().getColumn() / 10f, clf.getValues()[i][1]);
                assertEquals(State.values()[i % 3].toString(), clf.getValues()[i][2]);
            }
        }
        reader.close();
    }

    @Test
    public void testFloat32FeatureGroup()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "float32FeatureGroup.cld");
        f.delete();
        f.deleteOnExit();
        createMainFloat32FeatureGroupDataset(f, dsCode, null);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        assertTrue(System.currentTimeMillis() - ds.getCreationDate().getTime() < 100);
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals(FeatureGroupDataType.FLOAT32, clf.getFeatureGroup().getDataType());
            assertEquals("All", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("a", "b", "c"), clf.getFeatureGroup().getFeatureNames());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(3, clf.getValues()[i].length);
                assertEquals((float) (clf.getImageId().getRow() + i), clf.getValues()[i][0]);
                assertEquals(i + clf.getImageId().getColumn() / 10f, clf.getValues()[i][1]);
                assertEquals((float) Math.pow(10.0, i), clf.getValues()[i][2]);
            }
        }
        reader.close();
    }

    @Test
    public void testFloat32FeatureGroupEnforceCompoundStorage()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "float32FeatureGroupEnforceCompoundStorage.cld");
        f.delete();
        f.deleteOnExit();
        createMainFloat32FeatureGroupDataset(f, dsCode, null, true);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        assertTrue(System.currentTimeMillis() - ds.getCreationDate().getTime() < 100);
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals(FeatureGroupDataType.COMPOUND, clf.getFeatureGroup().getDataType());
            assertEquals("All", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("a", "b", "c"), clf.getFeatureGroup().getFeatureNames());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(3, clf.getValues()[i].length);
                assertEquals((float) (clf.getImageId().getRow() + i), clf.getValues()[i][0]);
                assertEquals(i + clf.getImageId().getColumn() / 10f, clf.getValues()[i][1]);
                assertEquals((float) Math.pow(10.0, i), clf.getValues()[i][2]);
            }
        }
        reader.close();
    }

    @Test
    public void testInt32FeatureGroup()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "int32FeatureGroup.cld");
        f.delete();
        f.deleteOnExit();
        createMainInt32FeatureGroupDataset(f, dsCode, null);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        assertTrue(System.currentTimeMillis() - ds.getCreationDate().getTime() < 100);
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals(FeatureGroupDataType.INT32, clf.getFeatureGroup().getDataType());
            assertEquals("All", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("a", "b", "c"), clf.getFeatureGroup().getFeatureNames());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(3, clf.getValues()[i].length);
                assertEquals(clf.getImageId().getRow() + i, clf.getValues()[i][0]);
                assertEquals(i + clf.getImageId().getColumn(), clf.getValues()[i][1]);
                assertEquals(-5 + i, clf.getValues()[i][2]);
            }
        }
        assertTrue(ds.getObjectTypes().isEmpty());
        assertEquals(1, ds.getObjectNamespaces().size());
        assertEquals(10,
                ds.getNumberOfSegmentedObjects(new ImageId(1, 2, 3), ds.getObjectNamespace("MAIN")));
        assertEquals("MAIN", ds.getObjectNamespaces().iterator().next().getId());
        assertTrue(Arrays.equals(new Object[]
            { 1, 2, -5 }, ds.getValues(new ImageId(1, 2, 3), 0)));
        assertTrue(Arrays.equals(new Object[]
            { 2, 3, -4 }, ds.getValues(new ImageId(1, 2, 3), 1)));
        assertTrue(Arrays.equals(new Object[]
            { 3, 4, -3 }, ds.getValues(new ImageId(1, 2, 3), 2)));
        assertTrue(Arrays.equals(
                new Object[]
                    { 10, 11, 4 },
                ds.getValues(
                        new ImageId(1, 2, 3),
                        ds.getNumberOfSegmentedObjects(new ImageId(1, 2, 3),
                                ds.getObjectNamespace("MAIN")) - 1)));
        reader.close();
    }

    @Test
    public void testEnumFeatureGroup()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "enumFeatureGroup.cld");
        f.delete();
        f.deleteOnExit();
        createMainEnumFeatureGroupDataset(f, dsCode, null);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        assertTrue(System.currentTimeMillis() - ds.getCreationDate().getTime() < 100);
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals(FeatureGroupDataType.ENUM, clf.getFeatureGroup().getDataType());
            assertEquals("All", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("a", "b", "c"), clf.getFeatureGroup().getFeatureNames());
            assertEquals(4, clf.getValues().length);
            assertTrue(Arrays.equals(clf.getValues()[0], new Object[]
                { "A", "B", "C" }));
            assertTrue(Arrays.equals(clf.getValues()[1], new Object[]
                { "C", "B", "A" }));
            assertTrue(Arrays.equals(clf.getValues()[2], new Object[]
                { "A", "C", "B" }));
            assertTrue(Arrays.equals(clf.getValues()[3], new Object[]
                { "B", "B", "B" }));
            assertEquals(4,
                    ds.getNumberOfSegmentedObjects(clf.getImageId(), ds.getObjectNamespace("MAIN")));
        }
        Object[] vals = ds.getValues(new ImageId(1, 2, 3), 0);
        assertTrue(Arrays.equals(new Object[]
            { "A", "B", "C" }, vals));
        vals = ds.getValues(new ImageId(1, 2, 3), 1);
        assertTrue(Arrays.equals(new Object[]
            { "C", "B", "A" }, vals));
        vals = ds.getValues(new ImageId(1, 2, 3), 2);
        assertTrue(Arrays.equals(new Object[]
            { "A", "C", "B" }, vals));
        vals = ds.getValues(new ImageId(1, 2, 3), 3);
        assertTrue(Arrays.equals(new Object[]
            { "B", "B", "B" }, vals));
        reader.close();
    }

    @Test
    public void testBoolFeatureGroup()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "boolFeatureGroup.cld");
        f.delete();
        f.deleteOnExit();
        createMainBoolFeatureGroupDataset(f, dsCode, null);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        assertTrue(System.currentTimeMillis() - ds.getCreationDate().getTime() < 100);
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals(FeatureGroupDataType.BOOL, clf.getFeatureGroup().getDataType());
            assertEquals("All", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("a", "b", "c"), clf.getFeatureGroup().getFeatureNames());
            assertEquals(4, clf.getValues().length);
            assertTrue(Arrays.equals(clf.getValues()[0], new Object[]
                { true, true, true }));
            assertTrue(Arrays.equals(clf.getValues()[1], new Object[]
                { false, false, false }));
            assertTrue(Arrays.equals(clf.getValues()[2], new Object[]
                { true, false, true }));
            assertTrue(Arrays.equals(clf.getValues()[3], new Object[]
                { false, true, false }));
            assertEquals(4,
                    ds.getNumberOfSegmentedObjects(clf.getImageId(), ds.getObjectNamespace("MAIN")));
        }
        Object[] vals = ds.getValues(new ImageId(1, 2, 3), 0);
        assertTrue(Arrays.toString(vals), Arrays.equals(new Object[]
            { true, true, true }, vals));
        vals = ds.getValues(new ImageId(1, 2, 3), 1);
        assertTrue(Arrays.toString(vals), Arrays.equals(new Object[]
            { false, false, false }, vals));
        vals = ds.getValues(new ImageId(1, 2, 3), 2);
        assertTrue(Arrays.toString(vals), Arrays.equals(new Object[]
            { true, false, true }, vals));
        vals = ds.getValues(new ImageId(1, 2, 3), 3);
        assertTrue(Arrays.toString(vals), Arrays.equals(new Object[]
            { false, true, false }, vals));
        reader.close();
    }

    @Test
    public void testEmptyFeatureGroup()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "emptyFeatureGroup.cld");
        f.delete();
        f.deleteOnExit();
        createEmptyFeatureGroupDataset(f, dsCode, null);
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        assertTrue(System.currentTimeMillis() - ds.getCreationDate().getTime() < 100);
        int count = 0;
        for (@SuppressWarnings("unused")
        CellLevelFeatures clf : ds.getValues())
        {
            ++count;
        }
        assertEquals(0, count);
        reader.close();
    }

    @Test
    public void testOneFeatureGroupMissingValues()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "OneFeatureGroupMissingValues.cld");
        f.delete();
        f.deleteOnExit();
        createMainFeatureGroupDataset(f, dsCode, new ImageId(1, 2, 3));
        final ICellLevelDataReader reader = CellLevelDataFactory.openForReading(f);
        final ICellLevelFeatureDataset ds = reader.getDataSet("123").toFeatureDataset();
        assertTrue(ds.hasValues(new ImageId(1, 2, 3)));
        assertFalse(ds.hasValues(new ImageId(0, 0, 0)));
        for (CellLevelFeatures clf : ds.getValues())
        {
            assertEquals("All", clf.getFeatureGroup().getId());
            assertEquals(new ImageId(1, 2, 3), clf.getImageId());
            assertEquals(Arrays.asList("one", "two", "three"), clf.getFeatureGroup()
                    .getFeatureNames());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(3, clf.getValues()[i].length);
                assertEquals(clf.getImageId().getRow() + i, clf.getValues()[i][0]);
                assertEquals(i + clf.getImageId().getColumn() / 10f, clf.getValues()[i][1]);
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
            assertEquals(FeatureGroupDataType.COMPOUND, clf.getFeatureGroup().getDataType());
            assertEquals("All", clf.getFeatureGroup().getId());
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
                assertEquals(Integer.toString(i), clf.getImageId().getRow() + i,
                        clf.getValues()[i][0]);
                assertEquals(Integer.toString(i), i + clf.getImageId().getColumn() / 10f,
                        clf.getValues()[i][1]);
                assertEquals(Integer.toString(i), i % 3, clf.getValues()[i][2]);
                assertEquals(Integer.toString(i), i % 2 == 0, clf.getValues()[i][3]);
                assertEquals(Integer.toString(i), i % 2 != 0 ? Integer.toString(i) : "",
                        clf.getValues()[i][4]);
            }
        }

        for (CellLevelFeatures clf : ds.getValues(ds.getFeatureGroup("main")))
        {
            assertEquals(FeatureGroupDataType.COMPOUND, clf.getFeatureGroup().getDataType());
            assertEquals("Main", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("one", "two", "three"), clf.getFeatureGroup()
                    .getFeatureNames());
            assertEquals(Arrays.asList(new Feature("one", FeatureDataType.INT32), new Feature(
                    "two", FeatureDataType.FLOAT32), new Feature("three", new String[]
                { "A", "B", "C" })), clf.getFeatureGroup().getFeatures());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(Integer.toString(i), 3, clf.getValues()[i].length);
                assertEquals(Integer.toString(i), clf.getImageId().getRow() + i,
                        clf.getValues()[i][0]);
                assertEquals(Integer.toString(i), i + clf.getImageId().getColumn() / 10f,
                        clf.getValues()[i][1]);
                assertEquals(Integer.toString(i), i % 3, clf.getValues()[i][2]);
            }
        }

        for (CellLevelFeatures clf : ds.getValues(ds.getFeatureGroup("quality")))
        {
            assertEquals(FeatureGroupDataType.COMPOUND, clf.getFeatureGroup().getDataType());
            assertEquals("Quality", clf.getFeatureGroup().getId());
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
        int count = 0;
        for (CellLevelFeatures clf : ds.getValues(ds.getObjectNamespace("cell_a")))
        {
            ++count;
            assertEquals(FeatureGroupDataType.COMPOUND, clf.getFeatureGroup().getDataType());
            assertEquals("All", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("one", "two", "three"), clf.getFeatureGroup()
                    .getFeatureNames());
            assertEquals(Arrays.asList(new Feature("one", FeatureDataType.INT32), new Feature(
                    "two", FeatureDataType.FLOAT32), new Feature("three", new String[]
                { "A", "B", "C" })), clf.getFeatureGroup().getFeatures());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(Integer.toString(i), 3, clf.getValues()[i].length);
                assertEquals(Integer.toString(i), clf.getImageId().getRow() + i,
                        clf.getValues()[i][0]);
                assertEquals(Integer.toString(i), i + clf.getImageId().getColumn() / 10f,
                        clf.getValues()[i][1]);
                assertEquals(Integer.toString(i), i % 3, clf.getValues()[i][2]);
            }
        }
        assertEquals(24, count);
        count = 0;
        for (CellLevelFeatures clf : ds.getValues(ds.getObjectNamespace("cell_b")))
        {
            ++count;
            assertEquals("All", clf.getFeatureGroup().getId());
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
        assertEquals(24, count);
        count = 0;
        for (CellLevelFeatures clf : ds.getValues(ds.getFeatureGroup("main")))
        {
            ++count;
            assertEquals("Main", clf.getFeatureGroup().getId());
            assertEquals(Arrays.asList("one", "two", "three"), clf.getFeatureGroup()
                    .getFeatureNames());
            assertEquals(Arrays.asList(new Feature("one", FeatureDataType.INT32), new Feature(
                    "two", FeatureDataType.FLOAT32), new Feature("three", new String[]
                { "A", "B", "C" })), clf.getFeatureGroup().getFeatures());
            assertEquals(10, clf.getValues().length);
            for (int i = 0; i < clf.getValues().length; ++i)
            {
                assertEquals(Integer.toString(i), 3, clf.getValues()[i].length);
                assertEquals(Integer.toString(i), clf.getImageId().getRow() + i,
                        clf.getValues()[i][0]);
                assertEquals(Integer.toString(i), i + clf.getImageId().getColumn() / 10f,
                        clf.getValues()[i][1]);
                assertEquals(Integer.toString(i), i % 3, clf.getValues()[i][2]);
            }
        }
        assertEquals(24, count);

        count = 0;
        for (CellLevelFeatures clf : ds.getValues(ds.getFeatureGroup("quality")))
        {
            ++count;
            assertEquals("Quality", clf.getFeatureGroup().getId());
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
        assertEquals(24, count);
        reader.close();
    }

    @Test(expectedExceptions = WrongNumberOfSegmentedObjectsException.class)
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
                    wds.createFeaturesDefinition(namespace).addInt32Feature("one")
                            .addFloat32Feature("two")
                            .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                            .createFeatureGroup("main");
            IFeatureGroup fg2 =
                    wds.createFeaturesDefinition(namespace).addBooleanFeature("ok")
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

    @Test
    public void testFeaturesTwoImages()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "twoFeaturesTwoImages.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        try
        {
            ICellLevelFeatureWritableDataset wds =
                    writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
            ObjectNamespace namespace = wds.addObjectNamespace("main");
            IFeatureGroup fg1 =
                    wds.createFeaturesDefinition(namespace).addInt32Feature("one")
                            .addFloat32Feature("two")
                            .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                            .createFeatureGroup("main");
            wds.writeFeatures(new ImageId(1, 1, 1), fg1, createStandardValue(new ImageId(1, 1, 1)));
            wds.writeFeatures(new ImageId(1, 1, 2), fg1,
                    createNonStandardValue(new ImageId(1, 1, 2)));
        } finally
        {
            writer.close();
        }
        // The test is that setting features with different number of objects for different images
        // does not fail.
    }

    @Test
    public void testFeatureGroupSorting()
    {
        final String dsCode = "123";
        final File f = new File(workingDirectory, "featureGroupSorting.cld");
        f.delete();
        f.deleteOnExit();
        ICellLevelDataWriter writer = CellLevelDataFactory.open(f);
        try
        {
            ICellLevelFeatureWritableDataset wds =
                    writer.addFeatureDataset(dsCode, new ImageQuantityStructure(2, 3, 4));
            ObjectNamespace namespace = wds.addObjectNamespace("main");
            wds.createFeaturesDefinition(namespace).addInt32Feature("one").addFloat32Feature("two")
                    .addEnumFeature("three", "State", Arrays.asList("A", "B", "C"))
                    .createFeatureGroup("M");
            wds.createFeaturesDefinition(namespace).addStringFeature("comment", 10)
                    .addBooleanFeature("valid").createFeatureGroup("a");
            wds.createFeaturesDefinition(namespace).addInt16Feature("someval")
                    .createFeatureGroup("Zz");
        } finally
        {
            writer.close();
        }
        final IHDF5Reader reader = HDF5Factory.openForReading(f);
        HDF5CompoundType<FeatureGroupDescriptor> type =
                reader.compound().getDataSetType("Dataset_123/FeatureGroups",
                        FeatureGroupDescriptor.class,
                        HDF5CompoundMemberMapping.mapping("id").dimensions(new int[]
                            { 100 }), HDF5CompoundMemberMapping.mapping("namespaceId"));
        final FeatureGroupDescriptor[] featureGroups =
                reader.compound().readArray("/Dataset_123/FeatureGroups", type);
        assertEquals(3, featureGroups.length);
        assertEquals("a", featureGroups[0].getId());
        assertEquals("M", featureGroups[1].getId());
        assertEquals("Zz", featureGroups[2].getId());
        reader.close();
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
