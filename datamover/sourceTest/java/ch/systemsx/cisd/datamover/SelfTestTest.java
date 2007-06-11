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
import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * Test cases for the {@link SelfTest}.
 * 
 * @author Bernd Rinn
 */
public class SelfTestTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "SelfTestTest");

    private static final File localDataDirectory = new File(workingDirectory, "local/data");

    private static final File localTemporaryDirectory = new File(workingDirectory, "local/temp");

    private static final File remoteDataDirectory = new File(workingDirectory, "remote");

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
        assert localDataDirectory.mkdirs();
        assert localTemporaryDirectory.mkdirs();
        assert remoteDataDirectory.mkdirs();
        workingDirectory.deleteOnExit();
        localDataDirectory.deleteOnExit();
        localTemporaryDirectory.deleteOnExit();
        remoteDataDirectory.deleteOnExit();
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

        public Status copy(File sourcePath, File destinationDirectory, String destinationHost)
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

    private static class MockSelfTestable implements ISelfTestable
    {

        private boolean checkCalled = false;

        public void check()
        {
            checkCalled = true;
        }

        public boolean isCheckCalled()
        {
            return checkCalled;
        }

    }

    // ////////////////////////////////////////
    // Test cases.
    //

    @Test
    public void testHappyCaseWithRemoteShare()
    {
        SelfTest.check(localDataDirectory, localTemporaryDirectory, remoteDataDirectory, null, mockCopier);
    }

    @Test
    public void testHappyCaseWithRemoteHost()
    {
        final String remoteHost = "some_remote_host";
        final MockPathCopier myMockCopier = new MockPathCopier(true, true);
        SelfTest.check(localDataDirectory, localTemporaryDirectory, remoteDataDirectory, remoteHost, myMockCopier);
        assert remoteHost.equals(myMockCopier.destinationHostQueried);
        assert remoteDataDirectory.equals(myMockCopier.destinationDirectoryQueried);
    }

    @Test
    public void testSelfTestableCalled()
    {
        final MockSelfTestable selfTestable = new MockSelfTestable();
        SelfTest
                .check(localDataDirectory, localTemporaryDirectory, remoteDataDirectory, null, mockCopier, selfTestable);
        assert selfTestable.isCheckCalled();
    }

    private static class FailingSelfTestException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;

    }

    private static class FailingSelfTestable implements ISelfTestable
    {

        public void check()
        {
            throw new FailingSelfTestException();
        }

    }

    @Test(expectedExceptions = FailingSelfTestException.class)
    public void testFailingSelfTestablePassedOn()
    {
        SelfTest.check(localDataDirectory, localTemporaryDirectory, remoteDataDirectory, null, mockCopier,
                new FailingSelfTestable());
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testEqualPaths()
    {
        SelfTest.check(localDataDirectory, localTemporaryDirectory, localDataDirectory, null, mockCopier);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testContainingPaths()
    {
        final File illegalTemporaryDirectory = new File(localDataDirectory, "temp");
        SelfTest.check(localDataDirectory, illegalTemporaryDirectory, remoteDataDirectory, null, mockCopier);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testNonExistentPaths()
    {
        final File nonExistentLocalDataDirectory = new File(workingDirectory, "data");
        SelfTest.check(nonExistentLocalDataDirectory, localTemporaryDirectory, remoteDataDirectory, null, mockCopier);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testRemoteHostNotSupported()
    {
        final String remoteHost = "some_remote_host";
        SelfTest.check(localDataDirectory, localTemporaryDirectory, remoteDataDirectory, remoteHost, mockCopier);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testRemoteHostAndDirectoryDoesNotExist()
    {
        final String remoteHost = "some_remote_host";
        final MockPathCopier myMockCopier = new MockPathCopier(true, false);
        SelfTest.check(localDataDirectory, localTemporaryDirectory, remoteDataDirectory, remoteHost, myMockCopier);
    }

}
