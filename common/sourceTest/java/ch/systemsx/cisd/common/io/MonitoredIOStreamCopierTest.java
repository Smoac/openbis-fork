/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.io.FileUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.MockLogger;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;

/**
 * @author Franz-Josef Elmer
 */
public class MonitoredIOStreamCopierTest extends AssertJUnit
{
    @Test
    public void testCopyingWithoutMonitoring()
    {
        MonitoredIOStreamCopier copier = new MonitoredIOStreamCopier((int) FileUtils.ONE_MB);

        copy(copier, (int) (2.5 * FileUtils.ONE_MB));
        
        copier.close();
    }

    @Test
    public void testCopyingInThreeChunks()
    {
        MonitoredIOStreamCopier copier = new MonitoredIOStreamCopier((int) FileUtils.ONE_MB);
        ITimeProvider timeProvider = new MockTimeProvider(123456789, 1125, 0, 2500, 0, 3400, 0, 6790,
                0, 700, 0, 1400);
        copier.setTimeProvider(timeProvider);
        MockLogger logger = new MockLogger();
        copier.setLogger(logger);

        copy(copier, (int) (2.5 * FileUtils.ONE_MB));
        copier.close();

        assertEquals("INFO: Reading statistics for input stream: 2.50 MB in 3 chunks took 5sec. "
                + "Average speed: 489.95 KB/sec. Median speed: 605.70 KB/sec.\n"
                + "INFO: Writing statistics for output stream: 2.50 MB in 3 chunks took 11sec. "
                + "Average speed: 239.48 KB/sec. Median speed: 280.21 KB/sec.\n", logger.toString());
    }

    @Test
    public void testCopyingInFourChunks()
    {
        MonitoredIOStreamCopier copier = new MonitoredIOStreamCopier((int) FileUtils.ONE_MB);
        ITimeProvider timeProvider = new MockTimeProvider(123456789, 1125, 0, 2500, 0, 3400, 0, 6790,
                0, 0, 0, 5700, 0, 7750, 0, 700, 0, 1400);
        copier.setTimeProvider(timeProvider);
        MockLogger logger = new MockLogger();
        copier.setLogger(logger);

        copy(copier, (int) (2 * FileUtils.ONE_MB));
        copy(copier, (int) (1.5 * FileUtils.ONE_MB));
        copier.close();

        assertEquals("INFO: Reading statistics for input stream: 3.50 MB in 4 chunks took 11sec. "
                + "Average speed: 328.05 KB/sec. Median speed: 301.18 KB/sec.\n"
                + "INFO: Writing statistics for output stream: 3.50 MB in 4 chunks took 18sec. "
                + "Average speed: 194.36 KB/sec. Median speed: 150.81 KB/sec.\n", logger.toString());
    }
    
    @Test
    public void testCopyingWithChunksToSmallForMedianCalculation()
    {
        MonitoredIOStreamCopier copier = new MonitoredIOStreamCopier((int) FileUtils.ONE_MB);
        ITimeProvider timeProvider = new MockTimeProvider(123456789, 1125, 0, 2500, 0, 0, 0, 3400, 0, 6790);
        copier.setTimeProvider(timeProvider);
        MockLogger logger = new MockLogger();
        copier.setLogger(logger);
        
        copy(copier, (int) (0.5 * FileUtils.ONE_MB));
        copy(copier, (int) (0.5 * FileUtils.ONE_MB));
        copier.close();
        
        assertEquals("INFO: Reading statistics for input stream: 1.00 MB in 2 chunks took 5sec. "
                + "Average speed: 226.30 KB/sec.\n"
                + "INFO: Writing statistics for output stream: 1.00 MB in 2 chunks took 9sec. "
                + "Average speed: 110.23 KB/sec.\n", logger.toString());
    }

    @Test
    public void testCopyingWithChunksToSmallForAverageCalculation()
    {
        MonitoredIOStreamCopier copier = new MonitoredIOStreamCopier((int) FileUtils.ONE_MB);
        ITimeProvider timeProvider = new MockTimeProvider(123456789, 1125, 0, 2500, 0, 0, 0, 3400, 0, 6790);
        copier.setTimeProvider(timeProvider);
        MockLogger logger = new MockLogger();
        copier.setLogger(logger);
        
        copy(copier, (int) (0.25 * FileUtils.ONE_MB));
        copy(copier, (int) (0.5 * FileUtils.ONE_MB));
        copier.close();
        
        assertEquals("INFO: Reading statistics for input stream: 768.00 KB in 2 chunks took 5sec.\n"
                + "INFO: Writing statistics for output stream: 768.00 KB in 2 chunks took 9sec.\n",
                logger.toString());
    }
    
    private void copy(MonitoredIOStreamCopier copier, int numberOfBytes)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        copier.copy(new ByteArrayInputStream(createTestData(numberOfBytes)), outputStream);
        assertCorrectCopy(numberOfBytes, outputStream);
    }

    private byte[] createTestData(int numberOfBytes)
    {
        byte[] data = new byte[numberOfBytes];
        for (int i = 0; i < numberOfBytes; i++)
        {
            data[i] = (byte) (i % 42);
        }
        return data;
    }

    private void assertCorrectCopy(int numberOfBytes, ByteArrayOutputStream outputStream)
    {
        byte[] data = outputStream.toByteArray();
        for (int i = 0; i < data.length; i++)
        {
            assertEquals("data[" + i + "]", (byte) (i % 42), data[i]);
        }
        assertEquals(numberOfBytes, data.length);
    }

}
