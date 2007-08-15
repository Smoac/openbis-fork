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
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for the {@link SelfTest}.
 * 
 * @author Bernd Rinn
 */
public class SelfTestTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "SelfTestTest");

    private static final File incomingDirectory = new File(workingDirectory, "local/incoming");

    private static final FileStore incomingStore = new FileStore(incomingDirectory, "incoming", null, false);

    private static final File bufferDirectory = new File(workingDirectory, "local/buffer");

    private static final FileStore bufferStore = new FileStore(bufferDirectory, "buffer", null, false);

    private static final File outgoingDirectory = new File(workingDirectory, "outgoing");

    private static final FileStore outgoingStore = new FileStore(outgoingDirectory, "outgoing", null, false);

    private static final FileStore dummyStore = new FileStore(null, "dummy", null, false);

    private static final IPathCopier mockCopier = new MockPathCopier(false, false);

    // ////////////////////////////////////////
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

    // ////////////////////////////////////////
    // Mocks.
    //

    private static class MockPathCopier implements IPathCopier
    {
        private final boolean supportsExplicitHost;

        private final boolean existRemote;

        File destinationDirectoryQueried;

        String destinationHostQueried;

        MockPathCopier(boolean supportsExplicitHost, boolean existRemote)
        {
            this.supportsExplicitHost = supportsExplicitHost;
            this.existRemote = existRemote;
        }

        public Status copy(File sourcePath, File destinationDirectory)
        {
            throw new AssertionError();
        }

        public Status copy(File sourcePath, String sourceHost, File destinationDirectory, String destinationHost)
        {
            throw new AssertionError();
        }

        public boolean exists(File destinationDirectory, String destinationHost)
        {
            assert supportsExplicitHost;
            assert destinationDirectoryQueried == null;
            assert destinationHostQueried == null;
            assert destinationDirectory != null;
            assert destinationHost != null;

            destinationDirectoryQueried = destinationDirectory;
            destinationHostQueried = destinationHost;
            return existRemote;
        }

        public boolean supportsExplicitHost()
        {
            return supportsExplicitHost;
        }

        public boolean terminate()
        {
            return false;
        }

        public void check()
        {
        }

    }

    // ////////////////////////////////////////
    // Test cases.
    //

    @Test
    public void testHappyCaseWithRemoteShare()
    {
        SelfTest.check(mockCopier, incomingStore, bufferStore, outgoingStore, dummyStore);
    }

    @Test
    public void testHappyCaseWithRemoteHost()
    {
        final String outgoingHost = "some_remote_host";
        final FileStore remoteHostOutgoingStore = new FileStore(outgoingDirectory, "outgoing", outgoingHost, true);
        final MockPathCopier myMockCopier = new MockPathCopier(true, true);
        SelfTest.check(myMockCopier, incomingStore, bufferStore, remoteHostOutgoingStore, dummyStore);
        assert outgoingHost.equals(myMockCopier.destinationHostQueried);
        assert outgoingDirectory.equals(myMockCopier.destinationDirectoryQueried);
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
        final FileStore illegalBufferStore = new FileStore(illegalBufferDirectory, "buffer", null, false);
        SelfTest.check(mockCopier, incomingStore, illegalBufferStore, outgoingStore);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testNonExistentPaths()
    {
        final File nonExistentIncomingDirectory = new File(workingDirectory, "data");
        final FileStore nonExistentIncomingStore = new FileStore(nonExistentIncomingDirectory, "incoming", null, false);
        SelfTest.check(mockCopier, nonExistentIncomingStore, bufferStore, outgoingStore);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testRemoteHostNotSupported()
    {
        final String remoteHost = "some_remote_host";
        final FileStore outgoingStoreWithRemoteHost = new FileStore(outgoingDirectory, "outgoing", remoteHost, true);
        SelfTest.check(mockCopier, incomingStore, bufferStore, outgoingStoreWithRemoteHost);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testRemoteHostAndDirectoryDoesNotExist()
    {
        final String remoteHost = "some_remote_host";
        final FileStore outgoingStoreWithRemoteHost = new FileStore(outgoingDirectory, "outgoing", remoteHost, true);
        final MockPathCopier myMockCopier = new MockPathCopier(true, false);
        SelfTest.check(myMockCopier, incomingStore, bufferStore, outgoingStoreWithRemoteHost);
    }

}
