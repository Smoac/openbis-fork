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

package ch.systemsx.cisd.common.utilities;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.utilities.WatermarkWatcher.IFreeSpaceProvider;
import ch.systemsx.cisd.common.utilities.WatermarkWatcher.WatermarkState;

/**
 * Test cases for the {@link WatermarkWatcher}.
 * 
 * @author Christian Ribeaud
 */
public final class WatermarkWatcherTest
{
    private static final File DEFAULT_PATH = new File("/my/path");

    private static final int DEFAULT_WATERMARK = 100;

    private Mockery context;

    private WatermarkWatcher watermarkWatcher;

    private IFreeSpaceProvider freeSpaceProvider;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        logRecorder = new BufferedAppender("%m", Level.INFO);
        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        watermarkWatcher = new WatermarkWatcher(DEFAULT_WATERMARK, freeSpaceProvider);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @DataProvider(name = "watermarks")
    public final Object[][] getWatermarks()
    {
        return new Object[][]
            {
                { 0 },
                { -1 },
                { 100 } };
    }

    @Test(dataProvider = "watermarks")
    public final void testConstructor(final long watermark)
    {
        final WatermarkWatcher watcher = new WatermarkWatcher(watermark);
        assertEquals(watermark, watcher.getWatermark());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRunFailed()
    {
        boolean fail = true;
        try
        {
            watermarkWatcher.run();
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @DataProvider(name = "freeSpaces")
    public final Object[][] getFreeSpaces()
    {
        return new Object[][]
            {
                { 99, true },
                { 100, false },
                { 101, false } };
    }

    @Test(dataProvider = "freeSpaces")
    public final void testIsBelow(final long freeSpace, final boolean isBelow) throws IOException
    {
        assertFalse(watermarkWatcher.isBelow());
        watermarkWatcher.setPath(DEFAULT_PATH);
        context.checking(new Expectations()
            {
                {
                    one(freeSpaceProvider).freeSpaceKb(DEFAULT_PATH);
                    will(returnValue(freeSpace));
                }
            });
        watermarkWatcher.run();
        assertEquals(isBelow, watermarkWatcher.isBelow());
        context.assertIsSatisfied();
    }

    private int i;

    @Test
    public final void testNofiticationChangeListener() throws IOException
    {
        i = 0;
        final long[] freeSpaces =
            { 99L, 101L };
        context.checking(new Expectations()
            {
                {
                    allowing(freeSpaceProvider).freeSpaceKb(DEFAULT_PATH);
                    will(new CustomAction("Free space provider")
                        {

                            //
                            // CustomAction
                            //

                            public final Object invoke(final Invocation invocation)
                                    throws Throwable
                            {
                                return freeSpaces[i];
                            }
                        });
                }
            });
        // Space becomes tight. So inform the administrator.
        watermarkWatcher.setPathAndRun(DEFAULT_PATH);
        assertEquals(String.format(
                WatermarkWatcher.NotificationLogChangeListener.WARNING_LOG_FORMAT, WatermarkWatcher
                        .displayKilobyteValue(freeSpaces[i]), DEFAULT_PATH, WatermarkWatcher
                        .displayKilobyteValue(DEFAULT_WATERMARK)), logRecorder.getLogContent());
        // Space still "red". Do not inform the administrator. He already knows it.
        logRecorder.resetLogContent();
        watermarkWatcher.setPathAndRun(DEFAULT_PATH);
        assertEquals("", logRecorder.getLogContent());
        // Space becomes again OK. So inform the administrator.
        i++;
        logRecorder.resetLogContent();
        watermarkWatcher.setPathAndRun(DEFAULT_PATH);
        assertEquals(String.format(WatermarkWatcher.NotificationLogChangeListener.INFO_LOG_FORMAT,
                WatermarkWatcher.displayKilobyteValue(freeSpaces[i]), DEFAULT_PATH,
                WatermarkWatcher.displayKilobyteValue(DEFAULT_WATERMARK)), logRecorder
                .getLogContent());
        // Space still OK. Do not inform the administrator.
        logRecorder.resetLogContent();
        watermarkWatcher.setPathAndRun(DEFAULT_PATH);
        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public final void testWatermarkState() throws IOException
    {
        final long freeSpace = 123L;
        context.checking(new Expectations()
            {
                {
                    one(freeSpaceProvider).freeSpaceKb(DEFAULT_PATH);
                    will(returnValue(freeSpace));
                }
            });
        final WatermarkState watermarkState = watermarkWatcher.getWatermarkState(DEFAULT_PATH);
        assertEquals(watermarkState.getFreeSpace(), freeSpace);
        assertEquals(watermarkState.getPath(), DEFAULT_PATH);
        assertEquals(watermarkState.getWatermark(), DEFAULT_WATERMARK);
        context.assertIsSatisfied();
    }
}