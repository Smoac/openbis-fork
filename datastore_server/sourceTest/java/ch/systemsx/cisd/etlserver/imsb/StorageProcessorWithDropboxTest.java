/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.imsb;

import static ch.systemsx.cisd.etlserver.imsb.StorageProcessorWithDropbox.DATASET_CODE_SEPARATOR_PROPERTY;
import static ch.systemsx.cisd.etlserver.imsb.StorageProcessorWithDropbox.DELEGATE_PROCESSOR_CLASS_PROPERTY;
import static ch.systemsx.cisd.etlserver.imsb.StorageProcessorWithDropbox.DROPBOX_INCOMING_DIRECTORY_PROPERTY;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = StorageProcessorWithDropbox.class)
public class StorageProcessorWithDropboxTest extends AbstractFileSystemTestCase
{
    @Test
    public final void testStore()
    {
        Mockery context = new Mockery();
        final IStorageProcessor storageProcessor = context.mock(IStorageProcessor.class);
        final IFileOperations fileOperations = context.mock(IFileOperations.class);

        final DataSetInformation dataSetInfo = new DataSetInformation();
        dataSetInfo.setDataSetCode("datasetCode");

        final File incomingDirectory = new File("incomingData");
        final String dropboxIncomingDirName = "dropboxIncomingDir";

        context.checking(new Expectations()
            {
                {
                    one(storageProcessor).storeData(null, dataSetInfo, null, null,
                            incomingDirectory, null);
                    will(returnValue(incomingDirectory));

                    File dropboxIncomingDir = new File(dropboxIncomingDirName);
                    one(fileOperations).isDirectory(dropboxIncomingDir);
                    will(returnValue(true));

                    one(storageProcessor).tryGetProprietaryData(incomingDirectory);
                    final File dataset = new File("incomingData.xml");
                    will(returnValue(dataset));

                    one(fileOperations).copyToDirectoryAs(dataset, dropboxIncomingDir,
                            "incomingData.datasetCode.xml");

                }
            });

        Properties props = new Properties();
        props.setProperty(DROPBOX_INCOMING_DIRECTORY_PROPERTY, dropboxIncomingDirName);

        new StorageProcessorWithDropbox(props, storageProcessor, fileOperations).storeData(null,
                dataSetInfo, null, null, incomingDirectory, null);
        context.assertIsSatisfied();
    }

    @Test
    public final void testCreateDelegateStorageProcessor()
    {
        Properties props = new Properties();
        props.setProperty(DELEGATE_PROCESSOR_CLASS_PROPERTY, DefaultStorageProcessor.class
                .getCanonicalName());
        StorageProcessorWithDropbox.createDelegateStorageProcessor(props);
    }

    @Test
    public final void testCreateDelegateStorageProcessorFails()
    {
        String expectedErrorMsg = "Given key 'processor' not found in properties '[]'";
        try
        {
            StorageProcessorWithDropbox.createDelegateStorageProcessor(new Properties());
        } catch (ConfigurationFailureException e)
        {
            assertEquals(expectedErrorMsg, e.getMessage());
            return;
        }
        fail("Expected error: " + expectedErrorMsg);
    }
    
    @Test
    public void testNestedStorageProcessorsWithDropBox()
    {
        File dropbox1 = new File(workingDirectory, "dropbox1");
        dropbox1.mkdirs();
        File dropbox2 = new File(workingDirectory, "dropbox2");
        dropbox2.mkdirs();
        Properties properties = new Properties();
        properties.setProperty(DROPBOX_INCOMING_DIRECTORY_PROPERTY, dropbox1.getAbsolutePath());
        properties.setProperty(DELEGATE_PROCESSOR_CLASS_PROPERTY, StorageProcessorWithDropbox.class.getName());
        String prefix = DELEGATE_PROCESSOR_CLASS_PROPERTY + ".";
        properties.setProperty(prefix + DELEGATE_PROCESSOR_CLASS_PROPERTY, DefaultStorageProcessor.class.getName());
        properties.setProperty(prefix + DROPBOX_INCOMING_DIRECTORY_PROPERTY, dropbox2.getAbsolutePath());
        properties.setProperty(prefix + DATASET_CODE_SEPARATOR_PROPERTY, "-");
        StorageProcessorWithDropbox processor = new StorageProcessorWithDropbox(properties);
        DataSetInformation dataSetInfo = new DataSetInformation();
        dataSetInfo.setDataSetCode("1234-1");
        File dataSetFile = new File(workingDirectory, "data.txt");
        FileUtilities.writeToFile(dataSetFile, "hello world");
        File store = new File(workingDirectory, "store");
        store.mkdirs();
        
        processor.storeData(null, dataSetInfo, null, null, dataSetFile, store);
        
        File storeData = new File(store, "original/data.txt");
        assertEquals(true, storeData.exists());
        assertEquals("hello world", FileUtilities.loadToString(storeData).trim());
        File data1 = new File(dropbox1, "data.1234-1.txt");
        assertEquals(true, data1.exists());
        assertEquals("hello world", FileUtilities.loadToString(data1).trim());
        File data2 = new File(dropbox2, "data-1234-1.txt");
        assertEquals(true, data2.exists());
        assertEquals("hello world", FileUtilities.loadToString(data2).trim());
    }
}
