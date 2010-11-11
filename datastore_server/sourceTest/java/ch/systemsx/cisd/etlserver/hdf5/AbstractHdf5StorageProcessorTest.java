/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.hdf5;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.hdf5.Hdf5Container.IHdf5ReaderClient;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;

/**
 * Tests for {@link Hdf5StorageProcessor}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractHdf5StorageProcessorTest extends AbstractFileSystemTestCase
{
    protected final Hdf5StorageProcessor storageProcessor;

    protected AbstractHdf5StorageProcessorTest(Properties properties)
    {
        super();

        storageProcessor = new Hdf5StorageProcessor(properties);
        storageProcessor.setStoreRootDirectory(workingDirectory);
    }

    private File createDirectory(final String directoryName)
    {
        final File file = new File(workingDirectory, directoryName);
        file.mkdir();
        return file;
    }

    protected void testStoreData()
    {
        final File incomingDataSetDirectory = createDirectory("incoming");
        FileUtilities.writeToFile(new File(incomingDataSetDirectory, "read.me"), "hello world");
        File rootDir = createDirectory("root");
        File storeData =
                storageProcessor.storeData(null, null, null, incomingDataSetDirectory, rootDir);
        assertTrue(incomingDataSetDirectory.exists());
        assertTrue(storeData.isDirectory());

        File hdf5ContainerFile = Hdf5StorageProcessor.getHdf5ContainerFile(rootDir);
        assertTrue(hdf5ContainerFile.exists());
        assertTrue(hdf5ContainerFile.isFile());

        final Hdf5Container container = Hdf5StorageProcessor.getHdf5Container(rootDir);

        container.runReaderClient(new IHdf5ReaderClient()
            {

                public void runWithSimpleReader(IHDF5SimpleReader reader)
                {
                    FileToHdf5DuplicationVerifier verifier =
                            new FileToHdf5DuplicationVerifier(incomingDataSetDirectory, container,
                                    reader);
                    verifier.verifyDuplicate();
                }
            });

        storageProcessor.commit(incomingDataSetDirectory, rootDir);

        assertFalse(incomingDataSetDirectory.exists());
    }

    protected void testUnstoreData()
    {
        File rootDir = createDirectory("root");
        File incomingDataSetDirectory = createDirectory("incoming");
        File readMeFile = new File(incomingDataSetDirectory, "read.me");
        FileUtilities.writeToFile(readMeFile, "hi");
        File storeData =
                storageProcessor.storeData(null, null, null, incomingDataSetDirectory, rootDir);
        assertTrue(incomingDataSetDirectory.exists());
        assertTrue(storeData.isDirectory());

        File hdf5ContainerFile = Hdf5StorageProcessor.getHdf5ContainerFile(rootDir);
        assertTrue(hdf5ContainerFile.exists());
        assertTrue(hdf5ContainerFile.isFile());

        storageProcessor.rollback(incomingDataSetDirectory, rootDir, null);
        assertEquals(true, incomingDataSetDirectory.exists());
        assertEquals("hi", FileUtilities.loadToString(readMeFile).trim());
    }

    protected void testGetStoreRootDirectory()
    {
        File storeRootDirectory = storageProcessor.getStoreRootDirectory();
        assertEquals(workingDirectory.getAbsolutePath(), storeRootDirectory.getAbsolutePath());
    }

    protected void testStoreDataNullValues()
    {
        storageProcessor.storeData(null, null, null, null, null);
    }

    protected void testRollbackNullValues()
    {
        storageProcessor.rollback(null, null, null);
    }

}
