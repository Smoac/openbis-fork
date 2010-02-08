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

package ch.systemsx.cisd.common.compression.file;

import java.io.File;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A worker {@link Runnable} for (image) compression.
 * 
 * @author Bernd Rinn
 */
class CompressionWorker implements Runnable
{

    @Private
    static final int MAX_RETRY_OF_FAILED_COMPRESSIONS = 3;

    @Private
    static final String COMPRESSING_MSG_TEMPLATE = "Compressing '%s'.";

    @Private
    static final String EXCEPTION_COMPRESSING_MSG_TEMPLATE =
            "Exceptional condition when trying to compress '%s'.";

    @Private
    static final String INTERRPTED_MSG = "Thread has been interrupted - exiting worker.";

    @Private
    static final String EXITING_MSG = "No more files to compress - exiting worker.";

    @Private
    final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CompressionWorker.class);

    private final Queue<File> workerQueue;

    private final Collection<FailureRecord> failures;

    private final ICompressionMethod compressor;

    private final AtomicInteger activeWorkers;

    CompressionWorker(final Queue<File> incommingQueue, final Collection<FailureRecord> failures,
            final ICompressionMethod compressor, final AtomicInteger activeWorkers)
    {
        assert incommingQueue != null;
        assert failures != null;
        assert compressor != null;
        assert activeWorkers != null;
        assert activeWorkers.get() > 0;

        this.workerQueue = incommingQueue;
        this.failures = failures;
        this.compressor = compressor;
        this.activeWorkers = activeWorkers;
    }

    public void run()
    {
        try
        {
            do
            {
                if (Thread.interrupted())
                {
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info(INTERRPTED_MSG);
                    }
                    return;
                }
                final File fileToCompressOrNull = workerQueue.poll();
                if (fileToCompressOrNull == null)
                {
                    operationLog.info(EXITING_MSG);
                    return;
                }
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String
                            .format(COMPRESSING_MSG_TEMPLATE, fileToCompressOrNull));
                }
                Status status = null;
                int count = 0;
                do
                {
                    try
                    {
                        status = compressor.compress(fileToCompressOrNull);
                    } catch (final Throwable th)
                    {
                        operationLog.error(String.format(EXCEPTION_COMPRESSING_MSG_TEMPLATE,
                                fileToCompressOrNull), th);
                        failures.add(new FailureRecord(fileToCompressOrNull, th));
                        status = null;
                        break;
                    }
                } while (StatusFlag.RETRIABLE_ERROR.equals(status.getFlag())
                        && ++count < MAX_RETRY_OF_FAILED_COMPRESSIONS);
                if (status != null && Status.OK.equals(status) == false)
                {
                    failures.add(new FailureRecord(fileToCompressOrNull, status));
                }
            } while (true);
        } finally
        {
            // if there are no remaining threads working notify main compressor thread that 
            // is waiting for all failures (see Compressor)
            if (0 == activeWorkers.decrementAndGet())
            {
                synchronized (failures)
                {
                    failures.notify();
                }
            }
        }
    }

}
