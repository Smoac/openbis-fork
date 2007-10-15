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
import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

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
        IDirectory dataFolder = dataStructure.getOriginalData();
        assertEquals(DataStructureV1_0.DIR_ORIGINAL, dataFolder.getName());
        assertEquals(DataStructureV1_0.DIR_DATA, dataFolder.tryToGetParent().getName());
    }
    
    @Test
    public void testGetFormatedData()
    {
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        IFormatedData formatedData = dataStructure.getFormatedData();
        assertTrue(formatedData instanceof NoFormatedData);
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, formatedData.getFormat());
    }
    
    @Test
    public void testGetFormatedDataBeforeInvokingSetVersion()
    {
        try
        {
            dataStructure.getFormatedData();
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertEquals("Couldn't create formated data because of undefined format.", e.getMessage());
        }
    }
    
    @Test
    public void testSetProcessingType()
    {
        dataStructure.setProcessingType(ProcessingType.COMPUTED_DATA);
        IDirectory root = storage.getRoot();
        IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        assertEquals("COMPUTED_DATA\n", Utilities.getString(metaData, ProcessingType.PROCESSING_TYPE));
    }
    
    @Test
    public void testSetProcessingTypeTwice()
    {
        dataStructure.setProcessingType(ProcessingType.RAW_DATA);
        dataStructure.setProcessingType(ProcessingType.COMPUTED_DATA);
        IDirectory root = storage.getRoot();
        IDirectory metaData = Utilities.getSubDirectory(root, DataStructureV1_0.DIR_METADATA);
        assertEquals("COMPUTED_DATA\n", Utilities.getString(metaData, ProcessingType.PROCESSING_TYPE));
    }
    
    @Test
    public void testGetProcessingType()
    {
        dataStructure.setProcessingType(ProcessingType.COMPUTED_DATA);
        assertEquals(ProcessingType.COMPUTED_DATA, dataStructure.getProcessingType());
    }
    
    @Test
    public void testGetUnknownProcessingType()
    {
        dataStructure.setProcessingType(ProcessingType.COMPUTED_DATA);
        IDirectory s = Utilities.getSubDirectory(storage.getRoot(), DataStructureV1_0.DIR_METADATA);
        s.addKeyValuePair(ProcessingType.PROCESSING_TYPE, "blabla");
        assertEquals(ProcessingType.OTHER, dataStructure.getProcessingType());
    }
    
    @Test
    public void testSetExperimentIdentifier()
    {
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
        try
        {
            dataStructure.getExperimentIdentifier();
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertPartOfString(ExperimentIdentifier.FOLDER, e.getMessage());
        }
    }
    
    @Test
    public void testGetExperimentIdentifier()
    {
        ExperimentIdentifier id = new ExperimentIdentifier("g", "p", "e");
        dataStructure.setExperimentIdentifier(id);
        assertEquals(id, dataStructure.getExperimentIdentifier());
    }
    
    @Test
    public void testGetVersion()
    {
        assertEquals(new Version(1, 0), dataStructure.getVersion());
    }
    
    @Test
    public void testSaveForEmptyData()
    {
        try
        {
            dataStructure.save();
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertEquals("Empty orginal data directory.", e.getMessage());
        }
    }
    
    @Test
    public void testSaveIfNoFormat()
    {
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        try
        {
            dataStructure.save();
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertEquals("Unspecified format.", e.getMessage());
        }
    }
    
    @Test
    public void testSaveIfNoExperimentID()
    {
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        try
        {
            dataStructure.save();
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertEquals("Unspecified experiment identifier.", e.getMessage());
        }
    }
    
    @Test
    public void testSaveIfNoProcessingType()
    {
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        dataStructure.setExperimentIdentifier(new ExperimentIdentifier("g", "p", "e"));
        try
        {
            dataStructure.save();
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertEquals("Unspecified processing type.", e.getMessage());
        }
    }
    
    @Test
    public void testSave()
    {
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier("g", "p", "e");
        dataStructure.setExperimentIdentifier(experimentIdentifier);
        dataStructure.setProcessingType(ProcessingType.RAW_DATA);
        
        IDirectory root = storage.getRoot();
        dataStructure.save();
        assertEquals(dataStructure.getVersion(), Version.loadFrom(root));
        try
        {
            storage.getRoot();
            fail("UserFailureException expected because save() should unmount storage.");
        } catch (UserFailureException e)
        {
            assertEquals("Can not get root of an unmounted storage.", e.getMessage());
        }
        
        DataStructureV1_0 reloadedDataStructure = new DataStructureV1_0(storage);
        reloadedDataStructure.load();
        assertEquals("42\n", Utilities.getString(reloadedDataStructure.getOriginalData(), "answer"));
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, reloadedDataStructure.getFormatedData().getFormat());
        assertEquals(experimentIdentifier, reloadedDataStructure.getExperimentIdentifier());
        assertEquals(ProcessingType.RAW_DATA, reloadedDataStructure.getProcessingType());
    }
    
    @Test
    public void testLoadIfVersionMissing()
    {
        try
        {
            dataStructure.load();
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertPartOfString(Version.VERSION, e.getMessage());
        }
    }
    
    @Test
    public void testLoad()
    {
        IDirectory root = storage.getRoot();
        new Version(1, 0).saveTo(root);
        IDirectory metaData = root.makeDirectory(DataStructureV1_0.DIR_METADATA);
        UnknownFormat1_0.UNKNOWN_1_0.saveTo(metaData);
        dataStructure.load();
    }
    
    @Test
    public void testLoadVersion1_1()
    {
        IDirectory root = storage.getRoot();
        new Version(1, 1).saveTo(root);
        IDirectory metaData = root.makeDirectory(DataStructureV1_0.DIR_METADATA);
        UnknownFormat1_0.UNKNOWN_1_0.saveTo(metaData);
        dataStructure.load();
    }
    
    @Test
    public void testLoadVersion2_0()
    {
        IDirectory root = storage.getRoot();
        new Version(2, 0).saveTo(root);
        try
        {
            dataStructure.load();
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertEquals("Version of loaded data structure is V2.0 which is not backward compatible with V1.0", 
                    e.getMessage());
        }
    }
    
    @Test
    public void testLoadWithUnknownFormat1_1()
    {
        IDirectory root = storage.getRoot();
        new Version(1, 0).saveTo(root);
        IDirectory metaData = root.makeDirectory(DataStructureV1_0.DIR_METADATA);
        new Format(UnknownFormat1_0.UNKNOWN_1_0.getCode(), new Version(1, 1)).saveTo(metaData);
        dataStructure.load();
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, dataStructure.getFormatedData().getFormat());
    }
    
    @Test
    public void testLoadWithUnknownFormat2_0()
    {
        IDirectory root = storage.getRoot();
        new Version(1, 0).saveTo(root);
        IDirectory metaData = root.makeDirectory(DataStructureV1_0.DIR_METADATA);
        new Format(UnknownFormat1_0.UNKNOWN_1_0.getCode(), new Version(2, 0)).saveTo(metaData);
        try
        {
            dataStructure.load();
            dataStructure.getFormatedData();
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertEquals("No class found for version V2.0", e.getMessage());
        }
    }
    
    @Test
    public void testLoadWithAnotherFormat()
    {
        IDirectory root = storage.getRoot();
        new Version(1, 0).saveTo(root);
        IDirectory metaData = root.makeDirectory(DataStructureV1_0.DIR_METADATA);
        new Format("another format", new Version(1,1)).saveTo(metaData);
        dataStructure.load();
        assertEquals(UnknownFormat1_0.UNKNOWN_1_0, dataStructure.getFormatedData().getFormat());
    }
    
}
