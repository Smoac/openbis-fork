/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.internal.NamedSequence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.ConsoleLogger;
import ch.systemsx.cisd.common.test.ProxyAction;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;

/**
 * @author Franz-Josef Elmer
 */
public class RemoteHierarchicalContentNodeMultiThreadTest extends AbstractFileSystemTestCase
{
    private static final String STARTED_MESSAGE = "started";

    private static final String FINISHED_MESSAGE = "finished";

    private static final String FILE1_CONTENT = "hello file one";

    private static final String FILE2_CONTENT = "hello file two";

    private static final String SESSION_TOKEN = "token";

    private static final String DATA_STORE_URL = "http://a.b.c";

    private static final String DATA_STORE_CODE = "DSS";
    
    private static final String DATA_SET_CODE = "DS-123";
    
    private static final IDatasetLocation DATA_SET_LOCATION = new DatasetLocation(
            DATA_SET_CODE, "a/b/c", DATA_STORE_CODE, DATA_STORE_URL);

    private Mockery context;

    private IFileOperations fileOperations;
    
    private ISingleDataSetPathInfoProvider provider;

    private IDssServiceRpcGeneric remoteDss;

    private OpenBISSessionHolder sessionHolder;

    private ContentCache cache;

    private File workSpace;

    private File remoteFile1;

    private File remoteFile2;

    @BeforeMethod
    public void setUpFixture()
    {
        context = new Mockery();
        fileOperations = context.mock(IFileOperations.class);
        provider = context.mock(ISingleDataSetPathInfoProvider.class);
        remoteDss = context.mock(IDssServiceRpcGeneric.class, "remote DSS");
        sessionHolder = new OpenBISSessionHolder();
        sessionHolder.setSessionToken(SESSION_TOKEN);
        File remoteStore = new File(workingDirectory, "remote-store");
        File remoteDataSetFolder = new File(remoteStore, DATA_SET_CODE);
        remoteDataSetFolder.mkdirs();
        remoteFile1 = new File(remoteDataSetFolder, "file1.txt");
        FileUtilities.writeToFile(remoteFile1, FILE1_CONTENT);
        remoteFile2 = new File(remoteDataSetFolder, "file2.txt");
        FileUtilities.writeToFile(remoteFile2, FILE2_CONTENT);
        workSpace = new File(workingDirectory, "workspace");
        final IDssServiceRpcGenericFactory serviceFactory = context.mock(IDssServiceRpcGenericFactory.class);
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).removeRecursivelyQueueing(
                            new File(workSpace, ContentCache.DOWNLOADING_FOLDER));
                    
                    allowing(serviceFactory).getService(DATA_STORE_URL);
                    will(returnValue(remoteDss));
                }
            });
        cache = new ContentCache(serviceFactory, sessionHolder, workSpace, fileOperations);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testGetTwoDifferentFilesInSequence() throws Exception
    {
        final DataSetPathInfo pathInfo1 = new DataSetPathInfo();
        pathInfo1.setRelativePath(remoteFile1.getName());
        pathInfo1.setDirectory(false);
        IHierarchicalContentNode node1 = createRemoteNode(pathInfo1);
        context.checking(new Expectations()
            {
                {
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo1.getRelativePath());
                    will(returnValue(remoteFile1.toURI().toURL().toString()));
                }
            });
        final DataSetPathInfo pathInfo2 = new DataSetPathInfo();
        pathInfo2.setRelativePath(remoteFile2.getName());
        pathInfo2.setDirectory(false);
        IHierarchicalContentNode node2 = createRemoteNode(pathInfo2);
        context.checking(new Expectations()
            {
                {
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo2.getRelativePath());
                    will(returnValue(remoteFile2.toURI().toURL().toString()));
                }
            });

        File file1 = node1.getFile();
        File file2 = node2.getFile();

        assertEquals(new File(workSpace, ContentCache.CHACHED_FOLDER + "/" + DATA_SET_CODE + "/"
                + remoteFile1.getName()).getAbsolutePath(), file1.getAbsolutePath());
        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(file1).trim());
        assertEquals(new File(workSpace, ContentCache.CHACHED_FOLDER + "/" + DATA_SET_CODE + "/"
                + remoteFile2.getName()).getAbsolutePath(), file2.getAbsolutePath());
        assertEquals(FILE2_CONTENT, FileUtilities.loadToString(file2).trim());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetSameFileInSequenceFirstTryFailing() throws Exception
    {
        final DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setRelativePath(remoteFile1.getName());
        pathInfo.setDirectory(false);
        IHierarchicalContentNode node = createRemoteNode(pathInfo);
        context.checking(new Expectations()
            {
                {
                    NamedSequence sequence = new NamedSequence("s1");
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo.getRelativePath());
                    will(throwException(new RuntimeException("error")));
                    inSequence(sequence);

                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo.getRelativePath());
                    will(returnValue(remoteFile1.toURI().toURL().toString()));
                    inSequence(sequence);
                }
            });

        try
        {
            node.getFile();
            fail("RuntimeException expected");
        } catch (RuntimeException ex)
        {
            assertEquals("error", ex.getMessage());
        }
        File file = node.getFile();

        assertEquals(new File(workSpace, ContentCache.CHACHED_FOLDER + "/" + DATA_SET_CODE + "/"
                + remoteFile1.getName()).getAbsolutePath(), file.getAbsolutePath());
        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(file).trim());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetTwoDifferentFilesInTwoThreads() throws Exception
    {
        final DataSetPathInfo pathInfo1 = new DataSetPathInfo();
        pathInfo1.setRelativePath(remoteFile1.getName());
        pathInfo1.setDirectory(false);
        IHierarchicalContentNode node1 = createRemoteNode(pathInfo1);
        final MessageChannel channel1 = new MessageChannel(10000);
        GetFileRunnable fileRunnable1 = new GetFileRunnable(node1, channel1)
            {
                @Override
                public void run()
                {
                    channel1.send(STARTED_MESSAGE);
                    super.run();
                }
            };
        Thread thread1 = new Thread(fileRunnable1);
        final DataSetPathInfo pathInfo2 = new DataSetPathInfo();
        pathInfo2.setRelativePath(remoteFile2.getName());
        pathInfo2.setDirectory(false);
        IHierarchicalContentNode node2 = createRemoteNode(pathInfo2);
        final MessageChannel channel2 = new MessageChannel(10000);
        GetFileRunnable fileRunnable2 = new GetFileRunnable(node2, channel2)
            {
                @Override
                public void run()
                {
                    channel1.assertNextMessage(STARTED_MESSAGE);
                    channel2.send(STARTED_MESSAGE);
                    super.run();
                }
            };
        Thread thread2 = new Thread(fileRunnable2);
        final MessageChannel channel3 = new MessageChannel(10000);
        context.checking(new Expectations()
            {
                {
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo1.getRelativePath());
                    will(new ProxyAction(returnValue(remoteFile1.toURI().toURL().toString()))
                        {
                            @Override
                            protected void doBeforeReturn()
                            {
                                channel2.assertNextMessage(STARTED_MESSAGE);
                            }
                        });
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo2.getRelativePath());
                    will(new ProxyAction(returnValue(remoteFile2.toURI().toURL().toString()))
                        {
                            @Override
                            protected void doBeforeReturn()
                            {
                                channel1.assertNextMessage(FINISHED_MESSAGE);
                                channel3.send(FINISHED_MESSAGE);
                            }
                        });
                }
            });

        thread1.start();
        thread2.start();
        channel3.assertNextMessage(FINISHED_MESSAGE);
        channel2.assertNextMessage(FINISHED_MESSAGE);
        File file1 = fileRunnable1.tryGetResult();
        File file2 = fileRunnable2.tryGetResult();

        assertEquals(new File(workSpace, ContentCache.CHACHED_FOLDER + "/" + DATA_SET_CODE + "/"
                + remoteFile1.getName()).getAbsolutePath(), file1.getAbsolutePath());
        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(file1).trim());
        assertEquals(new File(workSpace, ContentCache.CHACHED_FOLDER + "/" + DATA_SET_CODE + "/"
                + remoteFile2.getName()).getAbsolutePath(), file2.getAbsolutePath());
        assertEquals(FILE2_CONTENT, FileUtilities.loadToString(file2).trim());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetSameFileInTwoThreads() throws Exception
    {
        final DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setRelativePath(remoteFile1.getName());
        pathInfo.setDirectory(false);
        final IHierarchicalContentNode node1 = createRemoteNode(pathInfo);
        ConsoleLogger logger = new ConsoleLogger();
        final MessageChannel channel1 =
                new MessageChannelBuilder(10000).name("1").logger(logger).getChannel();
        final IHierarchicalContentNode node2 = createRemoteNode(pathInfo);
        final MessageChannel channel2 =
                new MessageChannelBuilder(10000).name("2").logger(logger).getChannel();
        final MessageChannel channel3 =
                new MessageChannelBuilder(10000).name("3").logger(logger).getChannel();
        GetFileRunnable fileRunnable1 = new GetFileRunnable(node1, channel1);
        GetFileRunnable fileRunnable2 = new GetFileRunnable(node2, channel2)
            {
                @Override
                public void run()
                {
                    channel1.assertNextMessage(STARTED_MESSAGE);
                    channel2.send(STARTED_MESSAGE);
                    super.run();
                }
            };
        final Thread thread1 = new Thread(fileRunnable1, "thread1");
        final Thread thread2 = new Thread(fileRunnable2, "thread2");
        context.checking(new Expectations()
            {
                {
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo.getRelativePath());
                    will(new ProxyAction(returnValue(remoteFile1.toURI().toURL().toString()))
                        {
                            @Override
                            protected void doBeforeReturn()
                            {
                                channel1.send(STARTED_MESSAGE);
                                channel2.assertNextMessage(STARTED_MESSAGE);
                                channel3.send(FINISHED_MESSAGE);
                            }
                        });
                }
            });

        thread1.start();
        thread2.start();
        channel3.assertNextMessage(FINISHED_MESSAGE);
        channel1.assertNextMessage(FINISHED_MESSAGE);
        channel2.assertNextMessage(FINISHED_MESSAGE);

        File file1 = fileRunnable1.tryGetResult();
        File file2 = fileRunnable2.tryGetResult();
        assertEquals(new File(workSpace, ContentCache.CHACHED_FOLDER + "/" + DATA_SET_CODE + "/"
                + remoteFile1.getName()).getAbsolutePath(), file1.getAbsolutePath());
        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(file1).trim());
        assertEquals(file1, file2);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetSameFileInTwoThreadsFirstDownloadFails() throws Exception
    {
        final DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setRelativePath(remoteFile1.getName());
        pathInfo.setDirectory(false);
        final IHierarchicalContentNode node1 = createRemoteNode(pathInfo);
        ConsoleLogger logger = new ConsoleLogger();
        final MessageChannel channel1 =
                new MessageChannelBuilder(10000).name("1").logger(logger).getChannel();
        final IHierarchicalContentNode node2 = createRemoteNode(pathInfo);
        final MessageChannel channel2 =
                new MessageChannelBuilder(10000).name("2").logger(logger).getChannel();
        final MessageChannel channel3 =
                new MessageChannelBuilder(10000).name("3").logger(logger).getChannel();
        GetFileRunnable fileRunnable1 = new GetFileRunnable(node1, channel1);
        GetFileRunnable fileRunnable2 = new GetFileRunnable(node2, channel2)
            {
                @Override
                public void run()
                {
                    channel1.assertNextMessage(STARTED_MESSAGE);
                    channel2.send(STARTED_MESSAGE);
                    super.run();
                }
            };
        final Thread thread1 = new Thread(fileRunnable1, "thread1");
        final Thread thread2 = new Thread(fileRunnable2, "thread2");
        final RuntimeException exception = new RuntimeException("error");
        context.checking(new Expectations()
            {
                {
                    NamedSequence sequence = new NamedSequence("s");
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo.getRelativePath());
                    will(new ProxyAction(throwException(exception))
                        {
                            @Override
                            protected void doBeforeReturn()
                            {
                                channel1.send(STARTED_MESSAGE);
                                channel2.assertNextMessage(STARTED_MESSAGE);
                            }
                        });
                    inSequence(sequence);
                    
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo.getRelativePath());
                    will(new ProxyAction(returnValue(remoteFile1.toURI().toURL().toString()))
                    {
                        @Override
                        protected void doBeforeReturn()
                        {
                            channel3.send(FINISHED_MESSAGE);
                        }
                    });
                    inSequence(sequence);
                }
            });

        thread1.start();
        thread2.start();
        channel3.assertNextMessage(FINISHED_MESSAGE);
        channel1.assertNextMessage(exception);
        channel2.assertNextMessage(FINISHED_MESSAGE);

        File file1 = fileRunnable1.tryGetResult();
        assertEquals(null, file1);
        File file2 = fileRunnable2.tryGetResult();
        assertEquals(new File(workSpace, ContentCache.CHACHED_FOLDER + "/" + DATA_SET_CODE + "/"
                + remoteFile1.getName()).getAbsolutePath(), file2.getAbsolutePath());
        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(file2).trim());
        context.assertIsSatisfied();
    }
    
    private IHierarchicalContentNode createRemoteNode(DataSetPathInfo pathInfo)
    {
        return new RemoteHierarchicalContentNode(DATA_SET_LOCATION, pathInfo, provider, 
                sessionHolder, cache);
    }

    private static interface IRunnableWithResult<T> extends Runnable
    {
        public T tryGetResult();
    }

    private static class GetFileRunnable implements IRunnableWithResult<File>
    {
        private final IHierarchicalContentNode node;

        private final MessageChannel channel;

        private File file;

        GetFileRunnable(IHierarchicalContentNode node, MessageChannel channel)
        {
            this.node = node;
            this.channel = channel;
        }

        @Override
        public void run()
        {
            try
            {
                file = node.getFile();
                channel.send(FINISHED_MESSAGE);
            } catch (Throwable ex)
            {
                channel.send(ex);
            }
        }

        @Override
        public File tryGetResult()
        {
            return file;
        }
    }
}
