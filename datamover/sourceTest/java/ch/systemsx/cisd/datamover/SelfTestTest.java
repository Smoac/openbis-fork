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

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.common.utilities.ISelfTestable;
import ch.systemsx.cisd.common.utilities.ITextHandler;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.AbstractFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.store.FileStoreRemote;
import ch.systemsx.cisd.datamover.testhelper.FileOperationsUtil;

/**
 * Test cases for the {@link SelfTest}.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
    { FileStoreFactory.class })
public class SelfTestTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator
            + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "SelfTestTest");

    private static final File incomingDirectory = new File(workingDirectory, "local/incoming");

    private static final IFileStore incomingStore = createLocalStore(incomingDirectory, "incoming");

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
                @Override
                public boolean isProgressEnabled()
                {
                    return false;
                }

                @Override
                public Status copy(File sourcePath, File destinationDirectory,
                        ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
                {
                    throw new AssertionError();
                }

                @Override
                public Status copyFromRemote(String sourcePath, String sourceHost,
                        File destinationDirectory, String rsyncModuleNameOrNull,
                        String rsyncPasswordFileOrNull, 
                        ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
                {
                    throw new AssertionError();
                }

                @Override
                public Status copyToRemote(File sourcePath, String destinationDirectory,
                        String destinationHostOrNull, String rsyncModuleNameOrNull,
                        String rsyncPasswordFileOrNull, 
                        ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
                {
                    throw new AssertionError();
                }

                @Override
                public Status copyContent(File sourcePath, File destinationDirectory, 
                        ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
                {
                    throw new AssertionError();
                }

                @Override
                public Status copyContentFromRemote(String sourcePath, String sourceHost,
                        File destinationDirectory, String rsyncModuleNameOrNull,
                        String rsyncPasswordFileOrNull, 
                        ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
                {
                    throw new AssertionError();
                }

                @Override
                public Status copyContentToRemote(File sourcePath, String destinationDirectory,
                        String destinationHostOrNull, String rsyncModuleNameOrNull,
                        String rsyncPasswordFileOrNull, 
                        ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
                {
                    throw new AssertionError();
                }

                @Override
                public boolean terminate()
                {
                    return true;
                }

                @Override
                public void check() throws EnvironmentFailureException,
                        ConfigurationFailureException
                {
                }

                @Override
                public boolean isRemote()
                {
                    return false;
                }

                @Override
                public boolean checkRsyncConnectionViaRsyncServer(String host, String rsyncModule,
                        String rsyncPassworFileOrNull, long millisToWaitForCompletion)
                {
                    throw new AssertionError();
                }

                @Override
                public boolean checkRsyncConnectionViaSsh(String host,
                        String rsyncExecutableOnHostOrNull, long millisToWaitForCompletion)
                {
                    throw new AssertionError();
                }

            };
    }

    //
    // Test cases.
    //

    @Test
    public void testUnknownRemoteHost()
    {
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY, "Self-test failed");
        final String outgoingHost = "unknown_remote_host";
        final IFileStore remoteHostOutgoingStore =
                createRemoteStore(outgoingDirectory.getPath(), outgoingHost, null, "outgoing");
        SelfTest.check(mockCopier, new IFileStore[]
            { remoteHostOutgoingStore }, new ISelfTestable[0]);
        appender.verifyLogHasHappened();
        LogMonitoringAppender.removeAppender(appender);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testEqualPaths()
    {
        SelfTest.check(mockCopier, new IFileStore[]
            { incomingStore, bufferStore, incomingStore }, new ISelfTestable[0]);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class, groups = "slow")
    public void testContainingPaths()
    {
        final File illegalBufferDirectory = new File(incomingDirectory, "temp");
        final IFileStore illegalBufferStore = createLocalStore(illegalBufferDirectory, "buffer");
        SelfTest.check(mockCopier, new IFileStore[]
            { incomingStore, illegalBufferStore, outgoingStore }, new ISelfTestable[0]);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class, groups = "slow")
    public void testNonExistentPaths()
    {
        final File nonExistentIncomingDirectory = new File(workingDirectory, "data");
        final IFileStore nonExistentIncomingStore =
                createLocalStore(nonExistentIncomingDirectory, "incoming");
        SelfTest.check(mockCopier, new IFileStore[]
            { nonExistentIncomingStore, bufferStore, outgoingStore }, new ISelfTestable[0]);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class, groups = "slow")
    public void testFailingISelfTestable()
    {
        SelfTest.check(mockCopier, new IFileStore[0], new ISelfTestable[]
            { new ISelfTestable()
                {
                    @Override
                    public void check() throws EnvironmentFailureException,
                            ConfigurationFailureException
                    {
                        throw new ConfigurationFailureException("some failure");
                    }

                    @Override
                    public boolean isRemote()
                    {
                        return false;
                    }

                } });
    }

    @Test
    public void testFailingRemoteISelfTestable()
    {
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY, "Self-test failed");
        SelfTest.check(mockCopier, new IFileStore[0], new ISelfTestable[]
            { new ISelfTestable()
                {
                    @Override
                    public void check() throws EnvironmentFailureException,
                            ConfigurationFailureException
                    {
                        throw new ConfigurationFailureException("some failure");
                    }

                    @Override
                    public boolean isRemote()
                    {
                        return true;
                    }

                } });
        appender.verifyLogHasHappened();
        LogMonitoringAppender.removeAppender(appender);
    }

    private final IFileStore createRemoteStore(String path, String host, String rsyncModule,
            String description)
    {
        return FileStoreFactory.createRemoteHost(path, host, rsyncModule, description,
                FileOperationsUtil.createTestFactory(),
                AbstractFileStore.DEFAULT_REMOTE_CONNECTION_TIMEOUT_MILLIS,
                FileStoreRemote.DEFAULT_REMOTE_OPERATION_TIMEOUT_MILLIS);
    }

    private final static IFileStore createLocalStore(File path, String description)
    {
        return FileStoreFactory.createLocal(path.getPath(), description,
                FileOperationsUtil.createTestFactory(), false,
                AbstractFileStore.DEFAULT_REMOTE_CONNECTION_TIMEOUT_MILLIS);
    }

}
