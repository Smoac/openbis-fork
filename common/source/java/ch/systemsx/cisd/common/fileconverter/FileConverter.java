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

package ch.systemsx.cisd.common.fileconverter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * The base class for file conversion.
 * 
 * @author Bernd Rinn
 */
public class FileConverter
{

    static
    {
        LogInitializer.init();
    }

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            FileConverter.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FileConverter.class);

    private static final int NUMBER_OF_CPU_CORES = Runtime.getRuntime().availableProcessors();

    private static Queue<File> tryFillWorkerQueue(final File directory,
            final IFileConversionStrategy conversionStrategy) throws EnvironmentFailureException
    {
        final List<File> filesToCompressOrNull =
                FileUtilities.listFiles(directory, new FileFilter()
                    {
                        public boolean accept(File pathname)
                        {
                            return conversionStrategy.tryCheckConvert(pathname) != null;
                        }
                    }, true, null, new Log4jSimpleLogger(machineLog));
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Found %d files to convert.",
                    filesToCompressOrNull.size()));
        }
        if (filesToCompressOrNull.isEmpty())
        {
            return null;
        }
        return new ArrayBlockingQueue<File>(filesToCompressOrNull.size(), false,
                filesToCompressOrNull);
    }

    @Private
    static int getInitialNumberOfWorkers(double machineLoad, int maxThreads)
    {
        return (int) Math.max(1,
                Math.min(Math.round(NUMBER_OF_CPU_CORES * machineLoad), maxThreads));
    }

    private static void startUpWorkerThreads(AtomicInteger workersCounter, Queue<File> workerQueue,
            Collection<FailureRecord> failed, IFileConversionStrategy conversionStrategy)
    {
        int counter = workersCounter.get();
        for (int i = 0; i < counter; ++i)
        {
            new Thread(new FileConversionWorker(workerQueue, failed, conversionStrategy,
                    workersCounter), "Compressor " + i).start();
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Started up %d worker threads.", counter));
        }
    }

    /**
     * Performs the conversion described by <var>conversionStrategy</var> on all files in
     * <var>directoryName</var>.
     * <p>
     * Uses #cores * <var>machineLoad</var> threads for the conversion, but not more than
     * <var>maxThreads</var>.
     */
    public static Collection<FailureRecord> performConversion(String directoryName,
            IFileConversionStrategy conversionStrategy, double machineLoad, int maxThreads)
            throws InterruptedExceptionUnchecked, EnvironmentFailureException
    {
        conversionStrategy.getConverter().check();
        final Queue<File> workerQueue =
                tryFillWorkerQueue(new File(directoryName), conversionStrategy);
        final Collection<FailureRecord> failed =
                Collections.synchronizedCollection(new ArrayList<FailureRecord>());
        if (workerQueue == null || workerQueue.isEmpty())
        {
            return failed;
        }
        final AtomicInteger workersCounter =
                new AtomicInteger(getInitialNumberOfWorkers(machineLoad, maxThreads));
        startUpWorkerThreads(workersCounter, workerQueue, failed, conversionStrategy);
        synchronized (failed)
        {
            while (workersCounter.get() > 0)
            {
                try
                {
                    failed.wait();
                } catch (InterruptedException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
        return failed;
    }

    private FileConverter()
    {
        // Do not instantiate.
    }
}
