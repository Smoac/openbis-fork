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

package ch.systemsx.cisd.datamover;

import java.io.File;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathCopier;
import ch.systemsx.cisd.datamover.testhelper.FileOperationsUtil;

/**
 * Test cases for the {@link SelfTest}.
 * 
 * @author Bernd Rinn
 */
public class SelfTestTest
{

    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "SelfTestTest");

    private static final File incomingDirectory = new File(workingDirectory, "local/incoming");

    private static final FileStore incomingStore = createLocalStore(incomingDirectory, "incoming");

    private static final File bufferDirectory = new File(workingDirectory, "local/buffer");

    private static final IFileStore bufferStore = createLocalStore(bufferDirectory, "buffer");

    private static final File outgoingDirectory = new File(workingDirectory, "outgoing");

    private static final IFileStore outgoingStore = createLocalStore(outgoingDirectory, "outgoing");

    private static final IPathCopier mockCopier = createMockCopier();

    //
    // Initialization methods.
    //

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        unitTestRootDirectory.mkdirs();
        assert unitTestRootDirectory.isDirectory();
    }

    @BeforeMethod
    public void setUp()
    {
        FileUtilities.deleteRecursively(workingDirectory);
        assert workingDirectory.mkdirs();
        assert incomingDirectory.mkdirs();
        assert bufferDirectory.mkdirs();
        assert outgoingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
        incomingDirectory.deleteOnExit();
        bufferDirectory.deleteOnExit();
        outgoingDirectory.deleteOnExit();
    }

    // 
    // Mocks.
    //

    private static IPathCopier createMockCopier()
    {
        return new IPathCopier()
            {
                public Status copy(File sourcePath, File destinationDirectory)
                {
                    throw new AssertionError();
                }

                public Status copyFromRemote(File sourcePath, String sourceHost,
                        File destinationDirectory)
                {
                    throw new AssertionError();
                }

                public Status copyToRemote(File sourcePath, File destinationDirectory,
                        String destinationHost)
                {
                    throw new AssertionError();
                }

                public boolean existsRemotely(File destinationDirectory, String destinationHost)
                {
                    throw new AssertionError();
                }

                public boolean terminate()
                {
                    return true;
                }

                public void check() throws EnvironmentFailureException,
                        ConfigurationFailureException
                {
                }

            };
    }

    //
    // Test cases.
    //

    @Test(expectedExceptions = NotImplementedException.class)
    public void testHappyCaseWithRemoteHost()
    {
        final String outgoingHost = "some_remote_host";
        final FileStore remoteHostOutgoingStore =
                createRemoteStore(outgoingDirectory, outgoingHost, "outgoing");
        SelfTest.check(mockCopier, remoteHostOutgoingStore);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testEqualPaths()
    {
        SelfTest.check(mockCopier, incomingStore, bufferStore, incomingStore);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testContainingPaths()
    {
        final File illegalBufferDirectory = new File(incomingDirectory, "temp");
        final IFileStore illegalBufferStore = createLocalStore(illegalBufferDirectory, "buffer");
        SelfTest.check(mockCopier, incomingStore, illegalBufferStore, outgoingStore);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testNonExistentPaths()
    {
        final File nonExistentIncomingDirectory = new File(workingDirectory, "data");
        final FileStore nonExistentIncomingStore =
                createLocalStore(nonExistentIncomingDirectory, "incoming");
        SelfTest.check(mockCopier, nonExistentIncomingStore, bufferStore, outgoingStore);
    }

    private FileStore createRemoteStore(File path, String host, String description)
    {
        return FileStoreFactory.createRemoteHost(path, host, description, FileOperationsUtil
                .createTestFatory());
    }

    private static FileStore createLocalStore(File path, String description)
    {
        return FileStoreFactory.createLocal(path, description, FileOperationsUtil
                .createTestFatory());
    }

}
