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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.IActivityObserver;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.concurrent.RecordingActivityObserverSensor;
import ch.systemsx.cisd.common.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;

/**
 * Test cases for {@link FileOperations}.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = FileOperations.class)
public class FileOperationsTest
{

    static class HangingIInputStream implements IInputStream
    {
        private final long hangMillis;

        HangingIInputStream(long hangMillis)
        {
            this.hangMillis = hangMillis;
        }

        private void hang()
        {
            ConcurrencyUtilities.sleep(hangMillis);
        }

        public int available() throws IOExceptionUnchecked
        {
            hang();
            return 0;
        }

        public void close() throws IOExceptionUnchecked
        {
            hang();
        }

        public void mark(int readlimit)
        {
            hang();
        }

        public boolean markSupported()
        {
            hang();
            return false;
        }

        public int read() throws IOExceptionUnchecked
        {
            hang();
            return 0;
        }

        public int read(byte[] b) throws IOExceptionUnchecked
        {
            hang();
            return 0;
        }

        public int read(byte[] b, int off, int len) throws IOExceptionUnchecked
        {
            hang();
            return 0;
        }

        public void reset() throws IOExceptionUnchecked
        {
            hang();
        }

        public long skip(long n) throws IOExceptionUnchecked
        {
            hang();
            return 0;
        }

    }

    static class HangingFileOperations extends FileOperations
    {

        private final long hangMillis;

        HangingFileOperations(TimingParameters parameters, IActivityObserver observer,
                long hangMillis)
        {
            super(parameters, observer);
            this.hangMillis = hangMillis;
        }

        @Override
        IInputStream internalGetIInputStream(File file) throws FileNotFoundException
        {
            return new HangingIInputStream(hangMillis);
        }

    }

    private static IFileOperations create(TimingParameters parameters, long hangMillis)
    {
        final RecordingActivityObserverSensor observerSensor =
                new RecordingActivityObserverSensor();
        return MonitoringProxy.create(
                IFileOperations.class,
                new HangingFileOperations(parameters, observerSensor,
                        hangMillis)).timing(parameters).sensor(
                observerSensor).errorLog(new Log4jSimpleLogger(FileOperations.operationLog)).name(
                "remote file operations").get();
    }

    @Test(groups = "slow", expectedExceptions = TimeoutExceptionUnchecked.class)
    public void testNoTimeoutOnInputStream() throws IOException
    {
        final IFileOperations ops = create(TimingParameters.createNoRetries(50L), 100L);
        final InputStream is = ops.getInputStream(new File("bla.txt"));
        is.read(); // times out
    }

    @Test(groups = "slow")
    public void testTimeoutOnInputStream() throws IOException
    {
        final IFileOperations ops = create(TimingParameters.createNoRetries(50L), 20L);
        final InputStream is = ops.getInputStream(new File("bla.txt"));
        is.read(); // doesn't time out
    }

}
