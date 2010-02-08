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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * The base class for file compression.
 * 
 * @author Bernd Rinn
 */
public class Compressor
{

    static
    {
        LogInitializer.init();
    }

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, Compressor.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, Compressor.class);

    private static final int NUMBER_OF_WORKERS = Runtime.getRuntime().availableProcessors();

    private static Queue<File> tryFillWorkerQueue(File directory, final FileFilter filter)
            throws EnvironmentFailureException
    {
        final File[] filesToCompressOrNull =
                FileUtilities.tryListFiles(directory, filter, new Log4jSimpleLogger(machineLog));
        if (filesToCompressOrNull == null)
        {
            String errorMsg = String.format("Path '%s' is not a directory.\n", directory.getPath());
            machineLog.error(errorMsg);
            throw new EnvironmentFailureException(errorMsg);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Found %d files to compress.",
                    filesToCompressOrNull.length));
        }
        return new ArrayBlockingQueue<File>(filesToCompressOrNull.length, false, Arrays
                .asList(filesToCompressOrNull));
    }

    private static void startUpWorkerThreads(Queue<File> workerQueue,
            Collection<FailureRecord> failed, ICompressionMethod compressor)
    {
        AtomicInteger activeWorkers = new AtomicInteger(NUMBER_OF_WORKERS);
        for (int i = 0; i < NUMBER_OF_WORKERS; ++i)
        {
            new Thread(new CompressionWorker(workerQueue, failed, compressor, activeWorkers),
                    "Compressor " + i).start();
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Started up %d worker threads.", NUMBER_OF_WORKERS));
        }
    }

    public static Collection<FailureRecord> start(String directoryName,
            ICompressionMethod compressionMethod) throws InterruptedException,
            EnvironmentFailureException
    {
        if (compressionMethod instanceof ISelfTestable)
        {
            ((ISelfTestable) compressionMethod).check();
        }
        final Queue<File> workerQueue =
                tryFillWorkerQueue(new File(directoryName), compressionMethod);
        final Collection<FailureRecord> failed =
                Collections.synchronizedCollection(new ArrayList<FailureRecord>());
        if (workerQueue.size() == 0)
        {
            System.out.println("No files to compress.");
            return failed;
        }
        startUpWorkerThreads(workerQueue, failed, compressionMethod);
        synchronized (failed)
        {
            failed.wait();
        }
        return failed;
    }
}
