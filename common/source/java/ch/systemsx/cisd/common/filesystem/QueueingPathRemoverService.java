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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.collections.ExtendedBlockingQueueFactory;
import ch.systemsx.cisd.common.collections.ExtendedLinkedBlockingQueue;
import ch.systemsx.cisd.common.collections.IExtendedBlockingQueue;
import ch.systemsx.cisd.common.collections.PersistentExtendedBlockingQueueDecorator;
import ch.systemsx.cisd.common.collections.RecordBasedQueuePersister;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.exceptions.StopException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A service for removing (deep) paths. It provides a {@link IFileRemover} that marks {@link File}s
 * for deletion and queues them up, using a separate thread to actually delete them.
 * <p>
 * Note that the service needs to be started via {@link #start(File, TimingParameters)}.
 * <p>
 * A file can be specified that keeps track of all the items that are to be deleted in order to
 * persist program restart.
 * 
 * @author Bernd Rinn
 */
public class QueueingPathRemoverService implements IFileRemover
{

    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, QueueingPathRemoverService.class);

    private static final int INITIAL_RECORD_SIZE = 128;

    // @Private
    final static String SHREDDER_PREFIX = ".SHREDDER_";

    private static final AtomicInteger counter = new AtomicInteger();

    private static IExtendedBlockingQueue<File> queue = null;

    private static ICloseable queueCloseableOrNull = null;

    private static Thread thread = null;

    private static IFileRemover deepRemover = null;

    private static IFileRemover queueingRemover = null;

    private static QueueingPathRemoverService instance = new QueueingPathRemoverService();

    /**
     * Returns the instance of the shredder.
     */
    public static final QueueingPathRemoverService getInstance()
    {
        return instance;
    }

    /**
     * Initializes the shredder thread. Will not persist over program restart. <i>Needs to be called
     * before this class is constructed for the first time.</i>
     */
    public static final void start()
    {
        start(null);
    }

    /**
     * Initializes the shredder thread. <i>Needs to be called before this class is constructed for
     * the first time.</i>
     * 
     * @param queueFileOrNull If not <code>null</code>, the file will be used to persist the items
     *            to be deleted over program restart.
     */
    public static final void start(File queueFileOrNull)
    {
        start(queueFileOrNull, TimingParameters.getDefaultParameters());
    }

    /**
     * Initializes the shredder thread. <i>Needs to be called before this class is constructed for
     * the first time.</i>
     * 
     * @param queueFileOrNull If not <code>null</code>, the file will be used to persist the items
     *            to be deleted over program restart.
     */
    public static final void start(final File queueFileOrNull, TimingParameters parameters)
    {
        final ISimpleLogger logger = new Log4jSimpleLogger(operationLog);
        final IFileRemover monitoringProxy = FileOperations.createMonitoredInstance(parameters);
        deepRemover = new LoggingPathRemoverDecorator(monitoringProxy, logger, false);
        queueingRemover = MonitoringProxy.create(IFileRemover.class, new IFileRemover()
            {
                public boolean removeRecursively(File fileToRemove)
                {
                    if (fileToRemove.isFile())
                    {
                        return fileToRemove.delete();
                    } else
                    {
                        final String name =
                                SHREDDER_PREFIX + System.currentTimeMillis() + "-"
                                        + counter.incrementAndGet() + "-" + fileToRemove.getName();
                        final File shredderFile = new File(fileToRemove.getParentFile(), name);
                        final boolean ok = fileToRemove.renameTo(shredderFile);
                        if (ok)
                        {
                            queue.add(shredderFile);
                        }
                        return ok;
                    }
                }
            }).timing(parameters).errorLog(logger).get();
        if (queueFileOrNull != null)
        {
            final PersistentExtendedBlockingQueueDecorator<File> persistentQueue =
                    ExtendedBlockingQueueFactory.createPersistRecordBased(queueFileOrNull,
                            INITIAL_RECORD_SIZE);
            queue = persistentQueue;
            queueCloseableOrNull = persistentQueue;
        } else
        {
            queue = new ExtendedLinkedBlockingQueue<File>();
        }
        thread = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        while (true)
                        {
                            final File fileToRemove = queue.peekWait();
                            deepRemover.removeRecursively(fileToRemove);
                            // Note: this is the only consumer of this queue.
                            queue.take();
                        }
                    } catch (InterruptedException ex)
                    {
                        // Exit thread.
                    } catch (StopException ex)
                    {
                        // Exit thread.
                    }
                }
            }, "Shredder Queue");
        thread.setDaemon(true);
        thread.start();
    }

    private static final void close()
    {
        if (queueCloseableOrNull != null)
        {
            queueCloseableOrNull.close();
        }
    }

    public static final void stop()
    {
        thread.interrupt();
        close();
        thread = null;
        queue = null;
        queueCloseableOrNull = null;
        deepRemover = null;
    }

    public static final boolean stopAndWait(long timeoutMillis)
    {
        thread.interrupt();
        try
        {
            thread.join(timeoutMillis);
        } catch (InterruptedException ex)
        {
        }
        final boolean ok = (thread.isAlive() == false);
        close();
        return ok;
    }

    public static final boolean isRunning()
    {
        return queueingRemover != null;
    }
    
    /**
     * Return list of shredder items.
     */
    public static final List<File> listShredderItems(File queueFile)
    {
        return RecordBasedQueuePersister.list(File.class, queueFile);
    }

    private QueueingPathRemoverService()
    {
        // Cannot be instantiated.
    }

    public boolean removeRecursively(File fileToRemove)
    {
        return queueingRemover.removeRecursively(fileToRemove);
    }

}
