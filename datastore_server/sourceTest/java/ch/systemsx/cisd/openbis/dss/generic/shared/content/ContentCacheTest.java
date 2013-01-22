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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.ConsoleLogger;
import ch.systemsx.cisd.common.test.ProxyAction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * @author Franz-Josef Elmer
 */
public class ContentCacheTest extends AbstractRemoteHierarchicalContentTestCase
{
    @Test
    public void testDataSetLocking()
    {
        ContentCache cache = createCache();

        cache.lockDataSet(SESSION_TOKEN, "DS-1");

        assertEquals(true, cache.isDataSetLocked(SESSION_TOKEN, "DS-1"));
        assertEquals(false, cache.isDataSetLocked(SESSION_TOKEN, "DS-2"));

        cache.lockDataSet(SESSION_TOKEN, "DS-1");

        assertEquals(true, cache.isDataSetLocked(SESSION_TOKEN, "DS-1"));

        cache.unlockDataSet(SESSION_TOKEN, "DS-1");

        assertEquals(true, cache.isDataSetLocked(SESSION_TOKEN, "DS-1"));

        cache.unlockDataSet(SESSION_TOKEN, "DS-1");

        assertEquals(false, cache.isDataSetLocked(SESSION_TOKEN, "DS-1"));

        context.assertIsSatisfied();
    }

    @Test
    public void testGetInputStream()
    {
        final DataSetPathInfo pathInfo1 = prepareForDownloading(remoteFile1);

        ContentCache cache = createCache();
        InputStream inputStream = cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);

        assertEquals(FILE1_CONTENT, readContent(inputStream, true));
        context.assertIsSatisfied();
    }

    @Test(invocationCount = 1, invocationTimeOut = 10000)
    public void testGetInputStreamForSameContentInTwoThreads()
    {
        final DataSetPathInfo pathInfo1 = prepareForDownloading(remoteFile1);
        ConsoleLogger logger = new ConsoleLogger();
        final MessageChannel channel1 =
                new MessageChannelBuilder(10000).name("1").logger(logger).getChannel();
        final MessageChannel channel2 =
                new MessageChannelBuilder(10000).name("2").logger(logger).getChannel();
        final MessageChannel channel3 =
                new MessageChannelBuilder(10000).name("3").logger(logger).getChannel();
        final ContentCache cache = createCache();
        File fileInCache =
                new File(workSpace, ContentCache.CACHE_FOLDER + "/" + DATA_SET_CODE + "/"
                        + remoteFile1.getName());
        assertEquals(false, fileInCache.exists());

        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    InputStream inputStream =
                            cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);
                    channel1.send(STARTED_MESSAGE);
                    channel2.assertNextMessage(STARTED_MESSAGE);
                    channel1.send(readContent(inputStream, true));
                }
            }, "thread1").start();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    channel1.assertNextMessage(STARTED_MESSAGE);
                    channel2.send(STARTED_MESSAGE);
                    InputStream inputStream =
                            cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);
                    channel1.assertNextMessage(FILE1_CONTENT);
                    channel2.send(readContent(inputStream, true));
                    channel3.send(FINISHED_MESSAGE);
                }
            }, "thread2").start();

        channel3.assertNextMessage(FINISHED_MESSAGE);
        channel2.assertNextMessage(FILE1_CONTENT);

        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(fileInCache).trim());
        File fileFromCache = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);
        assertEquals(fileInCache.getAbsolutePath(), fileFromCache.getAbsolutePath());
        context.assertIsSatisfied();
    }

    @Test(invocationCount = 1, invocationTimeOut = 10000)
    public void testGetFileAndGetInputStreamForSameContentInTwoThreads() throws Exception
    {
        final DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setRelativePath(remoteFile1.getName());
        pathInfo.setDirectory(false);
        ConsoleLogger logger = new ConsoleLogger();
        final MessageChannel channel1 =
                new MessageChannelBuilder(10000).name("11").logger(logger).getChannel();
        final MessageChannel channel2 =
                new MessageChannelBuilder(10000).name("12").logger(logger).getChannel();
        final MessageChannel channel3 =
                new MessageChannelBuilder(10000).name("13").logger(logger).getChannel();
        final ContentCache cache = createCache();
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
                            }
                        });
                }
            });
        File fileInCache =
                new File(workSpace, ContentCache.CACHE_FOLDER + "/" + DATA_SET_CODE + "/"
                        + remoteFile1.getName());
        assertEquals(false, fileInCache.exists());

        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    File file = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
                    channel1.send(FileUtilities.loadToString(file).trim());
                }
            }, "thread1").start();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    channel1.assertNextMessage(STARTED_MESSAGE);
                    channel2.send(STARTED_MESSAGE);
                    InputStream inputStream =
                            cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
                    channel1.assertNextMessage(FILE1_CONTENT);
                    channel2.send(readContent(inputStream, true));
                    channel3.send(FINISHED_MESSAGE);
                }
            }, "thread2").start();

        channel3.assertNextMessage(FINISHED_MESSAGE);
        channel2.assertNextMessage(FILE1_CONTENT);

        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(fileInCache).trim());
        File fileFromCache = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
        assertEquals(fileInCache.getAbsolutePath(), fileFromCache.getAbsolutePath());
        context.assertIsSatisfied();
    }

    @Test(invocationCount = 1, invocationTimeOut = 10000)
    public void testGetInputStreamGetFileForSameContentInTwoThreads() throws Exception
    {
        final DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setRelativePath(remoteFile1.getName());
        pathInfo.setDirectory(false);
        ConsoleLogger logger = new ConsoleLogger();
        final MessageChannel channel1 =
                new MessageChannelBuilder(10000).name("21").logger(logger).getChannel();
        final MessageChannel channel2 =
                new MessageChannelBuilder(10000).name("22").logger(logger).getChannel();
        final MessageChannel channel3 =
                new MessageChannelBuilder(10000).name("3").logger(logger).getChannel();
        final ContentCache cache = createCache();
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
                            }
                        });
                }
            });
        File fileInCache =
                new File(workSpace, ContentCache.CACHE_FOLDER + "/" + DATA_SET_CODE + "/"
                        + remoteFile1.getName());
        assertEquals(false, fileInCache.exists());

        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    InputStream inputStream =
                            cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
                    channel1.send(readContent(inputStream, true));
                }
            }, "thread1").start();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    channel1.assertNextMessage(STARTED_MESSAGE);
                    channel2.send(STARTED_MESSAGE);
                    File file = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
                    channel1.assertNextMessage(FILE1_CONTENT);
                    channel2.send(FileUtilities.loadToString(file).trim());
                    channel3.send(FINISHED_MESSAGE);
                }
            }, "thread2").start();

        channel3.assertNextMessage(FINISHED_MESSAGE);
        channel2.assertNextMessage(FILE1_CONTENT);

        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(fileInCache).trim());
        File fileFromCache = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
        assertEquals(fileInCache.getAbsolutePath(), fileFromCache.getAbsolutePath());
        context.assertIsSatisfied();
    }

    private String readContent(InputStream inputStream, boolean closeStream)
    {
        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, outputStream);
            return outputStream.toString();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (closeStream)
            {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    private DataSetPathInfo prepareForDownloading(final File remoteFile)
    {
        final DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setRelativePath(remoteFile.getName());
        pathInfo.setDirectory(false);
        context.checking(new Expectations()
            {
                {
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo.getRelativePath());
                    try
                    {
                        will(returnValue(remoteFile.toURI().toURL().toString()));
                    } catch (MalformedURLException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            });
        return pathInfo;
    }

}
