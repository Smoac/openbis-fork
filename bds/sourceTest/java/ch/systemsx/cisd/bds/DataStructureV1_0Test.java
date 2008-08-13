/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds;

import static ch.systemsx.cisd.bds.DataStructureV1_0.DIR_METADATA;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.exception.StorageException;
import ch.systemsx.cisd.bds.handler.ChecksumHandler;
import ch.systemsx.cisd.bds.handler.MappingFileHandler;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;
import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.common.filesystem.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link DataStructureV1_0} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class DataStructureV1_0Test extends AbstractFileSystemTestCase
{
    private static void assertPartOfString(final String part, final String string)
    {
        assertTrue("Expected <" + part + "> is part of <" + string + ">", string.indexOf(part) >= 0);
    }

    private FileStorage storage;

    private DataStructureV1_0 dataStructure;

    //
    // AbstractFileSystemTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp() throws IOException
    {
        super.setUp();
        storage = new FileStorage(workingDirectory);
        dataStructure = new DataStructureV1_0(storage);
    }

    @Test
    public void testGetOriginalData()
    {
        dataStructure.create();
        final IDirectory dataFolder = dataStructure.getOriginalData();
        assertEquals(DataStructureV1_0.DIR_ORIGINAL, dataFolder.getName());
        assertEquals(DataStructureV1_0.DIR_DATA, dataFolder.tryGetParent().getName());
    }

    @Test
    public void testGetFormattedData()
    {
        dataStructure.create();
        dataStructure.setFormat(UnknownFormatV1_0.UNKNOWN_1_0);
        final IFormattedData formattedData = dataStructure.getFormattedData();
        assertTrue(formattedData instanceof NoFormattedData);
        assertEquals(UnknownFormatV1_0.UNKNOWN_1_0, formattedData.getFormat());
    }

    @Test
    public void testGetFormattedDataBeforeInvokingSetVersion()
    {
        dataStructure.create();
        try
        {
            dataStructure.getFormattedData();
            fail("DataStructureException expected.");
        } catch (final DataStructureException e)
        {
            assertEquals("Couldn't create formatted data because of unspecified format.", e
                    .getMessage());
        }
    }

    @Test
    public void testSetExperimentIdentifier()
    {
        dataStructure.create();
        final ExperimentIdentifier id = new ExperimentIdentifier("i", "g", "p", "e");
        dataStructure.setExperimentIdentifier(id);
        final IDirectory root = storage.getRoot();
        final IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        final IDirectory idDir =
                Utilities.getSubDirectory(metaData, ExperimentIdentifier.EXPERIMENT_IDENTIFIER);
        assertEquals("i\n", Utilities.getString(idDir, ExperimentIdentifier.INSTANCE_CODE));
        assertEquals("g\n", Utilities.getString(idDir, ExperimentIdentifier.GROUP_CODE));
        assertEquals("p\n", Utilities.getString(idDir, ExperimentIdentifier.PROJECT_CODE));
        assertEquals("e\n", Utilities.getString(idDir, ExperimentIdentifier.EXPERIMENT_CODE));
    }

    @Test
    public void testSetExperimentIdentifierTwice()
    {
        dataStructure.create();
        dataStructure.setExperimentIdentifier(new ExperimentIdentifier("0", "a", "b", "c"));
        final ExperimentIdentifier id = new ExperimentIdentifier("i", "g", "p", "e");
        dataStructure.setExperimentIdentifier(id);
        final IDirectory root = storage.getRoot();
        final IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        final IDirectory idDir =
                Utilities.getSubDirectory(metaData, ExperimentIdentifier.EXPERIMENT_IDENTIFIER);
        assertEquals("i\n", Utilities.getString(idDir, ExperimentIdentifier.INSTANCE_CODE));
        assertEquals("g\n", Utilities.getString(idDir, ExperimentIdentifier.GROUP_CODE));
        assertEquals("p\n", Utilities.getString(idDir, ExperimentIdentifier.PROJECT_CODE));
        assertEquals("e\n", Utilities.getString(idDir, ExperimentIdentifier.EXPERIMENT_CODE));
    }

    @Test
    public void testGetNonExistingExperimentIdentifier()
    {
        dataStructure.create();
        try
        {
            dataStructure.getExperimentIdentifier();
            fail("DataStructureException expected.");
        } catch (final DataStructureException e)
        {
            assertPartOfString(ExperimentIdentifier.EXPERIMENT_IDENTIFIER, e.getMessage());
        }
    }

    @Test
    public void testGetExperimentIdentifier()
    {
        dataStructure.create();
        final ExperimentIdentifier id = new ExperimentIdentifier("i", "g", "p", "e");
        dataStructure.setExperimentIdentifier(id);
        assertEquals(id, dataStructure.getExperimentIdentifier());
    }

    @Test
    public void testGetVersion()
    {
        dataStructure.create();
        assertEquals(new Version(1, 0), dataStructure.getVersion());
    }

    @Test
    public void testAddReference()
    {
        dataStructure.create();
        dataStructure.addReference(new Reference("a", "b", ReferenceType.IDENTICAL));
        final Set<Reference> mapping = dataStructure.getStandardOriginalMapping();
        assertEquals(1, mapping.size());
        final Reference actualReference = mapping.iterator().next();
        assertEquals("a", actualReference.getPath());
        assertEquals(ReferenceType.IDENTICAL, actualReference.getReferenceType());
        assertEquals("b", actualReference.getOriginalPath());
    }

    @Test
    public void testAddReferenceTwice()
    {
        dataStructure.create();
        dataStructure.addReference(new Reference("a", "b", ReferenceType.IDENTICAL));
        try
        {
            dataStructure.addReference(new Reference("a", "b", ReferenceType.IDENTICAL));
            fail("DataStructureException expected");
        } catch (final DataStructureException e)
        {
            assertEquals("There is already a reference for file 'a'.", e.getMessage());
        }
    }

    @Test
    public void testThatGetStandardOriginalMappingReturnsAnUnmodifiableMap()
    {
        dataStructure.create();
        try
        {
            dataStructure.getStandardOriginalMapping().add(null);
            fail("DataStructureException expected");
        } catch (final UnsupportedOperationException e)
        {
            // ignored
        }
    }

    //
    // Note: validation has currently been switched off. We want to make the feature configurable,
    // though. At that point, these tests are supposed to work again.
    //

    @Test(groups = "broken")
    public void testCloseIfNoFormat()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (final DataStructureException e)
        {
            assertEquals("Unspecified format.", e.getMessage());
        }
    }

    @Test(groups = "broken")
    public void testCloseIfNoExperimentID()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormatV1_0.UNKNOWN_1_0);
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (final DataStructureException e)
        {
            assertEquals("Unspecified experiment identifier.", e.getMessage());
        }
    }

    @Test(groups = "broken")
    public void testCloseIfNoExperimentRegistrator()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormatV1_0.UNKNOWN_1_0);
        dataStructure.setExperimentIdentifier(new ExperimentIdentifier("i", "g", "p", "e"));
        dataStructure.setExperimentRegistrationTimestamp(new ExperimentRegistrationTimestamp(
                new Date(0)));
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (final DataStructureException e)
        {
            assertEquals("Unspecified experiment registrator.", e.getMessage());
        }
    }

    @Test(groups = "broken")
    public void testCloseIfNoExperimentRegistrationDate()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormatV1_0.UNKNOWN_1_0);
        dataStructure.setExperimentIdentifier(new ExperimentIdentifier("i", "g", "p", "e"));
        dataStructure.setExperimentRegistrator(new ExperimentRegistrator("g", "p", "g@p"));
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (final DataStructureException e)
        {
            assertEquals("Unspecified experiment registration timestamp.", e.getMessage());
        }
    }

    @Test(groups = "broken")
    public void testCloseIfNoMeasurementEntity()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormatV1_0.UNKNOWN_1_0);
        dataStructure.setExperimentIdentifier(new ExperimentIdentifier("i", "g", "p", "e"));
        dataStructure.setExperimentRegistrator(new ExperimentRegistrator("g", "p", "g@p"));
        dataStructure.setExperimentRegistrationTimestamp(new ExperimentRegistrationTimestamp(
                new Date(0)));
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (final DataStructureException e)
        {
            assertEquals("Unspecified sample.", e.getMessage());
        }
    }

    @Test
    public void testClose()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormatV1_0.UNKNOWN_1_0);
        final ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifier("i", "g", "p", "e");
        dataStructure.setExperimentIdentifier(experimentIdentifier);
        final ExperimentRegistrationTimestamp experimentRegistratorDate =
                new ExperimentRegistrationTimestamp(new Date(4711L * 4711000L));
        System.out.println(experimentRegistratorDate);
        dataStructure.setExperimentRegistrationTimestamp(experimentRegistratorDate);
        final ExperimentRegistrator experimentRegistrator =
                new ExperimentRegistrator("john", "doe", "j@doe");
        dataStructure.setExperimentRegistrator(experimentRegistrator);
        final Sample sample = new Sample("cp001", "CELL_PLATE", "plate");
        dataStructure.setSample(sample);
        addReference("path1", "origFile1", ReferenceType.IDENTICAL);
        addReference("path2", "origFile2", ReferenceType.TRANSFORMED);
        dataStructure.addFormatParameter(new FormatParameter("plate_dimension", "16x24"));
        checkFormattedData(dataStructure.getFormattedData());
        dataStructure.setDataSet(new DataSet("data_set", "HCS_IMAGE_ANALYSIS_DATA"));
        final IDirectory root = storage.getRoot();
        dataStructure.close();
        assertEquals(dataStructure.getVersion(), Version.loadFrom(root));
        try
        {
            storage.getRoot();
            fail("StorageException expected because save() should unmount storage.");
        } catch (final StorageException e)
        {
            assertEquals("Can not get root of an unmounted storage.", e.getMessage());
        }

        final DataStructureV1_0 reloadedDataStructure = new DataStructureV1_0(storage);
        reloadedDataStructure.open();
        assertEquals("42\n", Utilities.getString(reloadedDataStructure.getOriginalData(), "answer"));
        assertEquals(experimentIdentifier, reloadedDataStructure.getExperimentIdentifier());
        assertEquals(experimentRegistratorDate, reloadedDataStructure
                .getExperimentRegistratorTimestamp());
        assertEquals(experimentRegistrator, reloadedDataStructure.getExperimentRegistrator());
        assertEquals(sample, reloadedDataStructure.getSample());
        final Set<Reference> mapping = reloadedDataStructure.getStandardOriginalMapping();
        assertEquals(2, mapping.size());
        checkFormattedData(reloadedDataStructure.getFormattedData());

        final IDirectory metaDataDir = Utilities.getSubDirectory(root, DIR_METADATA);
        final IDirectory checksumDir =
                Utilities.getSubDirectory(metaDataDir, ChecksumHandler.CHECKSUM_DIRECTORY);
        assertEquals("a1d0c6e83f027327d8461063f4ac58a6  answer\n"
                + "d41d8cd98f00b204e9800998ecf8427e  origFile1\n"
                + "d41d8cd98f00b204e9800998ecf8427e  origFile2\n", Utilities.getString(checksumDir,
                DataStructureV1_0.DIR_ORIGINAL));
    }

    private final void addReference(final String path, final String originalPath,
            final ReferenceType type)
    {
        dataStructure.getOriginalData().addKeyValuePair(originalPath, "");
        dataStructure.getStandardData().addKeyValuePair(path, "");
        dataStructure.addReference(new Reference(path, originalPath, type));
    }

    private void checkFormattedData(final IFormattedData formattedData)
    {
        assertEquals(UnknownFormatV1_0.UNKNOWN_1_0, formattedData.getFormat());
        final IFormatParameters formatParameters = formattedData.getFormatParameters();
        final Iterator<FormatParameter> iterator = formatParameters.iterator();
        assertTrue(iterator.hasNext());
        final FormatParameter parameter = iterator.next();
        assertEquals("plate_dimension", parameter.getName());
        assertEquals("16x24", parameter.getValue());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testOpenIfVersionMissing()
    {
        createExampleDataStructure();
        storage.mount();
        final IDirectory root = storage.getRoot();
        root.removeNode(Utilities.getSubDirectory(root, Version.VERSION));
        storage.unmount();
        try
        {
            dataStructure.open();
            fail("DataStructureException expected.");
        } catch (final DataStructureException e)
        {
            assertPartOfString(Version.VERSION, e.getMessage());
        }
    }

    @Test
    public void testOpen()
    {
        createExampleDataStructure();
        dataStructure.open();
    }

    @Test
    public void testOpenVersionV2_0()
    {
        createExampleDataStructure();
        storage.mount();
        final IDirectory root = storage.getRoot();
        new Version(2, 0).saveTo(root);
        storage.unmount();
        try
        {
            dataStructure.open();
            fail("DataStructureException expected.");
        } catch (final DataStructureException e)
        {
            assertEquals(
                    "Version of loaded data structure is V2.0 which is not backward compatible with V1.0",
                    e.getMessage());
        }
    }

    @Test
    public void testOpenWithUnknownFormatV1_1()
    {
        createExampleDataStructure();
        storage.mount();
        final IDirectory root = storage.getRoot();
        final IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        new Format(UnknownFormatV1_0.UNKNOWN_1_0.getCode(), new Version(1, 1), null)
                .saveTo(metaData);
        storage.unmount();
        dataStructure.open();
        assertEquals(UnknownFormatV1_0.UNKNOWN_1_0, dataStructure.getFormattedData().getFormat());
    }

    @Test
    public void testOpenWithUnknownFormatV2_0()
    {
        createExampleDataStructure();
        storage.mount();
        final IDirectory root = storage.getRoot();
        final IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        new Format(UnknownFormatV1_0.UNKNOWN_1_0.getCode(), new Version(2, 0), null)
                .saveTo(metaData);
        storage.unmount();
        dataStructure.open();
        try
        {
            dataStructure.getFormattedData();
            fail("DataStructureException expected.");
        } catch (final DataStructureException e)
        {
            assertEquals("No class found for version V2.0", e.getMessage());
        }
    }

    @Test
    public void testOpenWithAnotherFormat()
    {
        createExampleDataStructure();
        storage.mount();
        final IDirectory root = storage.getRoot();
        final IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        new Format("another format", new Version(1, 1), null).saveTo(metaData);
        storage.unmount();
        dataStructure.open();
        assertEquals(UnknownFormatV1_0.UNKNOWN_1_0, dataStructure.getFormattedData().getFormat());
    }

    private final void createExampleDataStructure()
    {
        createExampleDataStructure(storage, new Version(1, 0));
    }

    public final static void createExampleDataStructure(final IStorage storage,
            final Version version)
    {
        storage.mount();
        final IDirectory root = storage.getRoot();
        version.saveTo(root);
        final IDirectory data = root.makeDirectory(DataStructureV1_0.DIR_DATA);
        final IDirectory originalDataDir = data.makeDirectory(DataStructureV1_0.DIR_ORIGINAL);
        originalDataDir.addKeyValuePair("file1", "This is my first file.");
        final IDirectory metaData = root.makeDirectory(DataStructureV1_0.DIR_METADATA);
        new Format(UnknownFormatV1_0.UNKNOWN_1_0.getCode(), new Version(2, 0), null)
                .saveTo(metaData);
        new ExperimentIdentifier("i", "g", "p", "e").saveTo(metaData);
        new ExperimentRegistrationTimestamp(new Date(0)).saveTo(metaData);
        new ExperimentRegistrator("john", "doe", "j@doe").saveTo(metaData);
        new Sample("a", "CELL_PLATE", "b").saveTo(metaData);
        new DataSet("d", "HCS_IMAGE").saveTo(metaData);
        createExampleChecksum(metaData);
        metaData.addKeyValuePair(MappingFileHandler.MAPPING_FILE, "");
        storage.unmount();
    }

    final static void createExampleChecksum(final IDirectory metaData)
    {
        final IDirectory checksumDir = metaData.makeDirectory(ChecksumHandler.CHECKSUM_DIRECTORY);
        checksumDir.addKeyValuePair(DataStructureV1_0.DIR_ORIGINAL,
                "23b447be20a6ddfe875a8b59ceae83ff  file1");
    }
}
