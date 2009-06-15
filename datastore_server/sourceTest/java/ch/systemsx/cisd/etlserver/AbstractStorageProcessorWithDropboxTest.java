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

package ch.systemsx.cisd.etlserver;

import static ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessorWithDropbox.DATASET_CODE_SEPARATOR_PROPERTY;
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
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = AbstractDelegatingStorageProcessorWithDropbox.class)
public class AbstractStorageProcessorWithDropboxTest extends AbstractFileSystemTestCase
{
    @Test
    public final void testStore()
    {
        Mockery context = new Mockery();
        final IStorageProcessor delegateStorageProcessor = context.mock(IStorageProcessor.class);
        final IFileOperations fileOperations = context.mock(IFileOperations.class);

        final DataSetInformation dataSetInfo = new DataSetInformation();
        dataSetInfo.setDataSetCode("datasetCode");

        final File incomingDirectory = new File("incomingData");
        final String dropboxIncomingDirName = "dropboxIncomingDir";

        context.checking(new Expectations()
            {
                {
                    one(delegateStorageProcessor).storeData(null, dataSetInfo, null, null,
                            incomingDirectory, null);
                    will(returnValue(incomingDirectory));

                    File dropboxIncomingDir = new File(dropboxIncomingDirName);
                    one(fileOperations).isDirectory(dropboxIncomingDir);
                    will(returnValue(true));

                    one(delegateStorageProcessor).tryGetProprietaryData(incomingDirectory);
                    final File dataset = new File("incomingData.xml");
                    will(returnValue(dataset));

                    one(fileOperations).copyToDirectoryAs(dataset, dropboxIncomingDir,
                            StorageProcessorWithDropboxTest.COPY_NAME);

                }
            });

        Properties props = new Properties();
        props.setProperty(StorageProcessorWithDropboxTest.DROPBOX_INCOMING_DIRECTORY_PROPERTY,
                dropboxIncomingDirName);

        AbstractDelegatingStorageProcessorWithDropbox storageProcessor =
                new StorageProcessorWithDropboxTest(props, delegateStorageProcessor, fileOperations);
        storageProcessor.storeData(null, dataSetInfo, null, null, incomingDirectory, null);

        context.assertIsSatisfied();
    }

    public static class StorageProcessorWithDropboxTest extends
            AbstractDelegatingStorageProcessorWithDropbox
    {
        public final static String DROPBOX_INCOMING_DIRECTORY_PROPERTY = "dropbox-dir";

        public final static String COPY_NAME = "datasetCopy";

        private final File dropboxDir;

        public StorageProcessorWithDropboxTest(Properties properties)
        {
            super(properties);
            this.dropboxDir = tryGetDropboxDir(properties);
        }

        private File tryGetDropboxDir(Properties properties)
        {
            return tryGetDirectory(DROPBOX_INCOMING_DIRECTORY_PROPERTY, properties);
        }

        StorageProcessorWithDropboxTest(Properties properties,
                IStorageProcessor delegateStorageProcessor, IFileOperations fileOperations)
        {
            super(properties, delegateStorageProcessor, fileOperations);
            this.dropboxDir = tryGetDropboxDir(properties);
        }

        @Override
        protected File tryGetDropboxDir(File originalData, DataSetInformation dataSetInformation)
        {
            return dropboxDir;
        }

        @Override
        protected String createDropboxDestinationFileName(DataSetInformation dataSetInformation,
                File incomingDataSetDirectory)
        {
            return COPY_NAME;
        }
    }

    @Test
    public final void testCreateDelegateStorageProcessor()
    {
        Properties props = new Properties();
        props.setProperty(AbstractDelegatingStorageProcessor.DELEGATE_PROCESSOR_CLASS_PROPERTY,
                DefaultStorageProcessor.class.getCanonicalName());
        AbstractDelegatingStorageProcessor.createDelegateStorageProcessor(props);
    }

    @Test
    public final void testCreateDelegateStorageProcessorFails()
    {
        String expectedErrorMsg = "Given key 'processor' not found in properties '[]'";
        try
        {
            AbstractDelegatingStorageProcessorWithDropbox
                    .createDelegateStorageProcessor(new Properties());
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
        properties.setProperty(StorageProcessorWithDropboxTest.DROPBOX_INCOMING_DIRECTORY_PROPERTY,
                dropbox1.getAbsolutePath());
        properties.setProperty(
                AbstractDelegatingStorageProcessor.DELEGATE_PROCESSOR_CLASS_PROPERTY,
                StorageProcessorWithDropboxTest.class.getName());
        String prefix = AbstractDelegatingStorageProcessor.DELEGATE_PROCESSOR_CLASS_PROPERTY + ".";
        properties.setProperty(prefix
                + AbstractDelegatingStorageProcessor.DELEGATE_PROCESSOR_CLASS_PROPERTY,
                DefaultStorageProcessor.class.getName());
        properties.setProperty(prefix
                + StorageProcessorWithDropboxTest.DROPBOX_INCOMING_DIRECTORY_PROPERTY, dropbox2
                .getAbsolutePath());
        properties.setProperty(prefix + DATASET_CODE_SEPARATOR_PROPERTY, "-");
        AbstractDelegatingStorageProcessorWithDropbox processor =
                new StorageProcessorWithDropboxTest(properties);
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
        File data1 = new File(dropbox1, StorageProcessorWithDropboxTest.COPY_NAME);
        assertEquals(true, data1.exists());
        assertEquals("hello world", FileUtilities.loadToString(data1).trim());
        File data2 = new File(dropbox2, StorageProcessorWithDropboxTest.COPY_NAME);
        assertEquals(true, data2.exists());
        assertEquals("hello world", FileUtilities.loadToString(data2).trim());
    }
}
