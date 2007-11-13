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

import static ch.systemsx.cisd.bds.DataStructureV1_0.CHECKSUM_DIRECTORY;
import static ch.systemsx.cisd.bds.DataStructureV1_0.DIR_METADATA;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.StorageException;
import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link DataStructureV1_0} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class DataStructureV1_0Test extends AbstractFileSystemTestCase
{
    private static void assertPartOfString(String part, String string)
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
    public final void setup() throws IOException
    {
        super.setup();
        storage = new FileStorage(workingDirectory);
        dataStructure = new DataStructureV1_0(storage);
    }

    @Test
    public void testGetOriginalData()
    {
        dataStructure.create();
        IDirectory dataFolder = dataStructure.getOriginalData();
        assertEquals(DataStructureV1_0.DIR_ORIGINAL, dataFolder.getName());
        assertEquals(DataStructureV1_0.DIR_DATA, dataFolder.tryToGetParent().getName());
    }

    @Test
    public void testGetFormatedData()
    {
        dataStructure.create();
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        IFormattedData formatedData = dataStructure.getFormattedData();
        assertTrue(formatedData instanceof NoFormattedData);
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, formatedData.getFormat());
    }

    @Test
    public void testGetFormatedDataBeforeInvokingSetVersion()
    {
        dataStructure.create();
        try
        {
            dataStructure.getFormattedData();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
        {
            assertEquals("Couldn't create formated data because of unspecified format.", e.getMessage());
        }
    }

    @Test
    public void testSetProcessingType()
    {
        dataStructure.create();
        dataStructure.setProcessingType(ProcessingType.COMPUTED_DATA);
        IDirectory root = storage.getRoot();
        IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        assertEquals("COMPUTED_DATA\n", Utilities.getString(metaData, ProcessingType.PROCESSING_TYPE));
    }

    @Test
    public void testSetProcessingTypeTwice()
    {
        dataStructure.create();
        dataStructure.setProcessingType(ProcessingType.RAW_DATA);
        dataStructure.setProcessingType(ProcessingType.COMPUTED_DATA);
        IDirectory root = storage.getRoot();
        IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        assertEquals("COMPUTED_DATA\n", Utilities.getString(metaData, ProcessingType.PROCESSING_TYPE));
    }

    @Test
    public void testGetProcessingType()
    {
        dataStructure.create();
        dataStructure.setProcessingType(ProcessingType.COMPUTED_DATA);
        assertEquals(ProcessingType.COMPUTED_DATA, dataStructure.getProcessingType());
    }

    @Test
    public void testGetUnknownProcessingType()
    {
        dataStructure.create();
        dataStructure.setProcessingType(ProcessingType.COMPUTED_DATA);
        IDirectory s = Utilities.getSubDirectory(storage.getRoot(), DataStructureV1_0.DIR_METADATA);
        s.addKeyValuePair(ProcessingType.PROCESSING_TYPE, "blabla");
        assertEquals(ProcessingType.OTHER, dataStructure.getProcessingType());
    }

    @Test
    public void testSetExperimentIdentifier()
    {
        dataStructure.create();
        ExperimentIdentifier id = new ExperimentIdentifier("g", "p", "e");
        dataStructure.setExperimentIdentifier(id);
        IDirectory root = storage.getRoot();
        IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        IDirectory idDir = Utilities.getSubDirectory(metaData, ExperimentIdentifier.FOLDER);
        assertEquals("g\n", Utilities.getString(idDir, ExperimentIdentifier.GROUP_CODE));
        assertEquals("p\n", Utilities.getString(idDir, ExperimentIdentifier.PROJECT_CODE));
        assertEquals("e\n", Utilities.getString(idDir, ExperimentIdentifier.EXPERIMENT_CODE));
    }

    @Test
    public void testSetExperimentIdentifierTwice()
    {
        dataStructure.create();
        dataStructure.setExperimentIdentifier(new ExperimentIdentifier("a", "b", "c"));
        ExperimentIdentifier id = new ExperimentIdentifier("g", "p", "e");
        dataStructure.setExperimentIdentifier(id);
        IDirectory root = storage.getRoot();
        IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        IDirectory idDir = Utilities.getSubDirectory(metaData, ExperimentIdentifier.FOLDER);
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
        } catch (DataStructureException e)
        {
            assertPartOfString(ExperimentIdentifier.FOLDER, e.getMessage());
        }
    }

    @Test
    public void testGetExperimentIdentifier()
    {
        dataStructure.create();
        ExperimentIdentifier id = new ExperimentIdentifier("g", "p", "e");
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
        Map<String, Reference> mapping = dataStructure.getStandardOriginalMapping();
        assertEquals(1, mapping.size());
        Reference actualReference = mapping.get("a");
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
        } catch (DataStructureException e)
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
            dataStructure.getStandardOriginalMapping().put("a", null);
            fail("DataStructureException expected");
        } catch (UnsupportedOperationException e)
        {
            // ignored
        }
    }

    @Test
    public void testCloseForEmptyData()
    {
        dataStructure.create();
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
        {
            assertEquals("Empty original data directory.", e.getMessage());
        }
    }

    @Test
    public void testCloseIfNoFormat()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
        {
            assertEquals("Unspecified format.", e.getMessage());
        }
    }

    @Test
    public void testCloseIfNoExperimentID()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
        {
            assertEquals("Unspecified experiment identifier.", e.getMessage());
        }
    }

    @Test
    public void testCloseIfNoExperimentRegistrator()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        dataStructure.setExperimentIdentifier(new ExperimentIdentifier("g", "p", "e"));
        dataStructure.setExperimentRegistrationDate(new ExperimentRegistratorDate(new Date(0)));
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
        {
            assertEquals("Unspecified experiment registrator.", e.getMessage());
        }
    }

    @Test
    public void testCloseIfNoExperimentRegistrationDate()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        dataStructure.setExperimentIdentifier(new ExperimentIdentifier("g", "p", "e"));
        dataStructure.setExperimentRegistrator(new ExperimentRegistrator("g", "p", "g@p"));
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
        {
            assertEquals("Unspecified experiment registration date.", e.getMessage());
        }
    }

    @Test
    public void testCloseIfNoMeasurementEntity()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        dataStructure.setExperimentIdentifier(new ExperimentIdentifier("g", "p", "e"));
        dataStructure.setExperimentRegistrator(new ExperimentRegistrator("g", "p", "g@p"));
        dataStructure.setExperimentRegistrationDate(new ExperimentRegistratorDate(new Date(0)));
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
        {
            assertEquals("Unspecified measurement entity.", e.getMessage());
        }
    }

    @Test
    public void testCloseIfNoProcessingType()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        dataStructure.setExperimentIdentifier(new ExperimentIdentifier("g", "p", "e"));
        dataStructure.setExperimentRegistrator(new ExperimentRegistrator("g", "p", "g@p"));
        dataStructure.setExperimentRegistrationDate(new ExperimentRegistratorDate(new Date(0)));
        dataStructure.setMeasurementEntity(new MeasurementEntity("a", "b"));
        try
        {
            dataStructure.close();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
        {
            assertEquals("Unspecified processing type.", e.getMessage());
        }
    }

    @Test
    public void testClose()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier("g", "p", "e");
        dataStructure.setExperimentIdentifier(experimentIdentifier);
        ExperimentRegistratorDate experimentRegistratorDate = new ExperimentRegistratorDate(new Date(4711L * 4711000L));
        System.out.println(experimentRegistratorDate);
        dataStructure.setExperimentRegistrationDate(experimentRegistratorDate);
        ExperimentRegistrator experimentRegistrator = new ExperimentRegistrator("john", "doe", "j@doe");
        dataStructure.setExperimentRegistrator(experimentRegistrator);
        MeasurementEntity measurementEntity = new MeasurementEntity("cp001", "plate");
        dataStructure.setMeasurementEntity(measurementEntity);
        dataStructure.setProcessingType(ProcessingType.RAW_DATA);
        dataStructure.addReference(new Reference("a/b/c", "a6b8/x.t", ReferenceType.IDENTICAL));
        dataStructure.addReference(new Reference("a78/jjh", "a b/x\tt", ReferenceType.TRANSFORMED));
        dataStructure.addFormatParameter(new FormatParameter("plate_dimension", "16x24"));
        checkFormattedData(dataStructure.getFormattedData());

        IDirectory root = storage.getRoot();
        dataStructure.close();
        assertEquals(dataStructure.getVersion(), Version.loadFrom(root));
        try
        {
            storage.getRoot();
            fail("StorageException expected because save() should unmount storage.");
        } catch (StorageException e)
        {
            assertEquals("Can not get root of an unmounted storage.", e.getMessage());
        }

        DataStructureV1_0 reloadedDataStructure = new DataStructureV1_0(storage);
        reloadedDataStructure.open();
        assertEquals("42\n", Utilities.getString(reloadedDataStructure.getOriginalData(), "answer"));
        assertEquals(experimentIdentifier, reloadedDataStructure.getExperimentIdentifier());
        assertEquals(experimentRegistratorDate, reloadedDataStructure.getExperimentRegistratorDate());
        assertEquals(experimentRegistrator, reloadedDataStructure.getExperimentRegistrator());
        assertEquals(measurementEntity, reloadedDataStructure.getMeasurementEntity());
        assertEquals(ProcessingType.RAW_DATA, reloadedDataStructure.getProcessingType());
        Map<String, Reference> mapping = reloadedDataStructure.getStandardOriginalMapping();
        assertEquals(2, mapping.size());
        Reference reference = mapping.get("a/b/c");
        assertEquals("a/b/c", reference.getPath());
        assertEquals(ReferenceType.IDENTICAL, reference.getReferenceType());
        assertEquals("a6b8/x.t", reference.getOriginalPath());
        reference = mapping.get("a78/jjh");
        assertEquals("a78/jjh", reference.getPath());
        assertEquals(ReferenceType.TRANSFORMED, reference.getReferenceType());
        assertEquals("a b/x\tt", reference.getOriginalPath());
        checkFormattedData(reloadedDataStructure.getFormattedData());

        IDirectory metaDataDir = Utilities.getSubDirectory(root, DIR_METADATA);
        IDirectory checksumDir = Utilities.getSubDirectory(metaDataDir, CHECKSUM_DIRECTORY);
        assertEquals("a1d0c6e83f027327d8461063f4ac58a6  answer\n", Utilities.getString(checksumDir,
                DataStructureV1_0.DIR_ORIGINAL));
    }

    private void checkFormattedData(IFormattedData formattedData)
    {
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, formattedData.getFormat());
        IFormatParameters formatParameters = formattedData.getFormatParameters();
        Iterator<FormatParameter> iterator = formatParameters.iterator();
        assertTrue(iterator.hasNext());
        FormatParameter parameter = iterator.next();
        assertEquals("plate_dimension", parameter.getName());
        assertEquals("16x24", parameter.getValue());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testOpenIfVersionMissing()
    {
        createExampleDataStructure();
        storage.mount();
        IDirectory root = storage.getRoot();
        root.removeNode(Utilities.getSubDirectory(root, Version.VERSION));
        storage.unmount();
        try
        {
            dataStructure.open();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
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
    public void testOpenVersion1_1()
    {
        createExampleDataStructure();
        storage.mount();
        IDirectory root = storage.getRoot();
        new Version(1, 1).saveTo(root);
        storage.unmount();
        dataStructure.open();
    }

    @Test
    public void testOpenVersion2_0()
    {
        createExampleDataStructure();
        storage.mount();
        IDirectory root = storage.getRoot();
        new Version(2, 0).saveTo(root);
        storage.unmount();
        try
        {
            dataStructure.open();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
        {
            assertEquals("Version of loaded data structure is V2.0 which is not backward compatible with V1.0", e
                    .getMessage());
        }
    }

    @Test
    public void testOpenWithUnknownFormat1_1()
    {
        createExampleDataStructure();
        storage.mount();
        IDirectory root = storage.getRoot();
        IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        new Format(UnknownFormat1_0.UNKNOWN_1_0.getCode(), new Version(1, 1), null).saveTo(metaData);
        storage.unmount();
        dataStructure.open();
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, dataStructure.getFormattedData().getFormat());
    }

    @Test
    public void testOpenWithUnknownFormat2_0()
    {
        createExampleDataStructure();
        storage.mount();
        IDirectory root = storage.getRoot();
        IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        new Format(UnknownFormat1_0.UNKNOWN_1_0.getCode(), new Version(2, 0), null).saveTo(metaData);
        storage.unmount();
        dataStructure.open();
        try
        {
            dataStructure.getFormattedData();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
        {
            assertEquals("No class found for version V2.0", e.getMessage());
        }
    }

    @Test
    public void testOpenWithAnotherFormat()
    {
        createExampleDataStructure();
        storage.mount();
        IDirectory root = storage.getRoot();
        IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        new Format("another format", new Version(1, 1), null).saveTo(metaData);
        storage.unmount();
        dataStructure.open();
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, dataStructure.getFormattedData().getFormat());
    }

    private void createExampleDataStructure()
    {
        storage.mount();
        IDirectory root = storage.getRoot();
        new Version(1, 0).saveTo(root);
        IDirectory data = root.makeDirectory(DataStructureV1_0.DIR_DATA);
        IDirectory originalDataDir = data.makeDirectory(DataStructureV1_0.DIR_ORIGINAL);
        originalDataDir.addKeyValuePair("hello", "world");
        IDirectory metaData = root.makeDirectory(DataStructureV1_0.DIR_METADATA);
        new Format(UnknownFormat1_0.UNKNOWN_1_0.getCode(), new Version(2, 0), null).saveTo(metaData);
        new ExperimentIdentifier("g", "p", "e").saveTo(metaData);
        new ExperimentRegistratorDate(new Date(0)).saveTo(metaData);
        new ExperimentRegistrator("john", "doe", "j@doe").saveTo(metaData);
        new MeasurementEntity("a", "b").saveTo(metaData);
        metaData.addKeyValuePair(DataStructureV1_0.MAPPING_FILE, "");
        ProcessingType.COMPUTED_DATA.saveTo(metaData);
        storage.unmount();
    }
}
