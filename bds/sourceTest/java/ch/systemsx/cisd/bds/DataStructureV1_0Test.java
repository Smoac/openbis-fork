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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.StorageException;
import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataStructureV1_0Test
{
    static final File TEST_DIR = new File("targets" + File.separator + "unit-test-wd" + File.separator + "ds");
    
    private static void assertPartOfString(String part, String string)
    {
        assertTrue("Expected <" + part + "> is part of <" + string + ">", string.indexOf(part) >= 0);
    }
    
    private FileStorage storage;

    private DataStructureV1_0 dataStructure;
    
    @BeforeMethod
    public void setup() throws IOException
    {
        TEST_DIR.mkdirs();
        FileUtils.cleanDirectory(TEST_DIR);
        storage = new FileStorage(TEST_DIR);
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
        IFormattedData formatedData = dataStructure.getFormatedData();
        assertTrue(formatedData instanceof NoFormattedData);
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, formatedData.getFormat());
    }
    
    @Test
    public void testGetFormatedDataBeforeInvokingSetVersion()
    {
        dataStructure.create();
        try
        {
            dataStructure.getFormatedData();
            fail("DataStructureException expected.");
        } catch (DataStructureException e)
        {
            assertEquals("Couldn't create formated data because of undefined format.", e.getMessage());
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
    public void testCloseIfNoProcessingType()
    {
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        dataStructure.setExperimentIdentifier(new ExperimentIdentifier("g", "p", "e"));
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
        dataStructure.setProcessingType(ProcessingType.RAW_DATA);
        
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
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, reloadedDataStructure.getFormatedData().getFormat());
        assertEquals(experimentIdentifier, reloadedDataStructure.getExperimentIdentifier());
        assertEquals(ProcessingType.RAW_DATA, reloadedDataStructure.getProcessingType());
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
            assertEquals("Version of loaded data structure is V2.0 which is not backward compatible with V1.0", 
                    e.getMessage());
        }
    }
    
    @Test
    public void testOpenWithUnknownFormat1_1()
    {
        createExampleDataStructure();
        storage.mount();
        IDirectory root = storage.getRoot();
        IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        new Format(UnknownFormat1_0.UNKNOWN_1_0.getCode(), new Version(1, 1)).saveTo(metaData);
        storage.unmount();
        dataStructure.open();
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, dataStructure.getFormatedData().getFormat());
    }
    
    @Test
    public void testOpenWithUnknownFormat2_0()
    {
        createExampleDataStructure();
        storage.mount();
        IDirectory root = storage.getRoot();
        IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        new Format(UnknownFormat1_0.UNKNOWN_1_0.getCode(), new Version(2, 0)).saveTo(metaData);
        storage.unmount();
        try
        {
            dataStructure.open();
            dataStructure.getFormatedData();
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
        new Format("another format", new Version(1,1)).saveTo(metaData);
        storage.unmount();
        dataStructure.open();
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, dataStructure.getFormatedData().getFormat());
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
        new Format(UnknownFormat1_0.UNKNOWN_1_0.getCode(), new Version(2, 0)).saveTo(metaData);
        new ExperimentIdentifier("g", "p", "e").saveTo(metaData);
        ProcessingType.COMPUTED_DATA.saveTo(metaData);
        storage.unmount();
        
    }
}
