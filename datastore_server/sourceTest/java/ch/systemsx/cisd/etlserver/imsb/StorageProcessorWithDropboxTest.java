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

import java.io.File;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = StorageProcessorWithDropbox.class)
public class StorageProcessorWithDropboxTest
{
    @Test
    public final void testStore()
    {
        Mockery context = new Mockery();
        final IStorageProcessor storageProcessor = context.mock(IStorageProcessor.class);
        final IFileOperations fileOperations = context.mock(IFileOperations.class);

        final DataSetInformation dataSetInfo = new DataSetInformation();
        dataSetInfo.setDataSetCode("xxx");

        final File incomingDirectory = new File("incomingData.xml");
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

                    one(fileOperations).copyToDirectoryAs(incomingDirectory, dropboxIncomingDir,
                            "incomingData_xxx.xml");

                }
            });

        Properties props = new Properties();
        props.setProperty(StorageProcessorWithDropbox.DROPBOX_INCOMING_DIRECTORY_PROPERTY,
                dropboxIncomingDirName);

        new StorageProcessorWithDropbox(props, storageProcessor, fileOperations).storeData(null,
                dataSetInfo, null, null, incomingDirectory, null);
        context.assertIsSatisfied();
    }

    @Test
    public final void testCreateDelegateStorageProcessor()
    {
        Properties props = new Properties();
        props.setProperty(StorageProcessorWithDropbox.DELEGATE_PROCESSOR_CLASS_PROPERTY,
                DefaultStorageProcessor.class.getCanonicalName());
        StorageProcessorWithDropbox.createDelegateStorageProcessor(props);
    }

    @Test
    public final void testCreateDelegateStorageProcessorFails()
    {
        String expectedErrorMsg = "Given key 'default-processor' not found in properties '[]'";
        try
        {
            StorageProcessorWithDropbox.createDelegateStorageProcessor(new Properties());
        } catch (ConfigurationFailureException e)
        {
            AssertJUnit.assertEquals(expectedErrorMsg, e.getMessage());
            return;
        }
        AssertJUnit.fail("Expected error: " + expectedErrorMsg);
    }
}
