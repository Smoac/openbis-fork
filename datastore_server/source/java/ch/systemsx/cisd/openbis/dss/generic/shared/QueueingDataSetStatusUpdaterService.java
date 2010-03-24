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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.collections.ExtendedBlockingQueueFactory;
import ch.systemsx.cisd.common.collections.IExtendedBlockingQueue;
import ch.systemsx.cisd.common.collections.PersistentExtendedBlockingQueueDecorator;
import ch.systemsx.cisd.common.collections.RecordBasedQueuePersister;
import ch.systemsx.cisd.common.filesystem.ICloseable;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodeWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus;

/**
 * A service for updating data set status in openBIS. It provides a method
 * {@link #update(DataSetCodeWithStatus)} that queues updates using a separate thread to actually
 * perform update.
 * <p>
 * Note that the service needs to be started via {@link #start(File, TimingParameters)}.
 * <p>
 * A file that keeps track of all the data sets that are to be updated needs to be specified in
 * order to persist program restart.
 * 
 * @author Piotr Buczek
 */
public class QueueingDataSetStatusUpdaterService
{

    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, QueueingDataSetStatusUpdaterService.class);

    private static final int INITIAL_RECORD_SIZE = 128;

    @Private
    final static String UPDATER_PREFIX = ".UPDATER_";

    private static IExtendedBlockingQueue<DataSetCodeWithStatus> queue = null;

    private static ICloseable queueCloseableOrNull = null;

    private static Thread thread = null;

    private static IDataSetStatusUpdater updater = null;

    /**
     * Initializes the updater thread. <i>Needs to be called before this class is constructed for
     * the first time.</i>
     * 
     * @param queueFile the file that will be used to persist the items to be deleted over program
     *            restart.
     */
    public static final void start(File queueFile)
    {
        start(queueFile, TimingParameters.getDefaultParameters());
    }

    /**
     * Initializes the updater thread. <i>Needs to be called before this class is constructed for
     * the first time.</i>
     * 
     * @param queueFile the file that will be used to persist the items to be deleted over program
     *            restart.
     */
    public static synchronized final void start(final File queueFile, TimingParameters parameters)
    {
        updater = createDataSetStatusUpdater();
        final PersistentExtendedBlockingQueueDecorator<DataSetCodeWithStatus> persistentQueue =
                ExtendedBlockingQueueFactory.createPersistRecordBased(queueFile,
                        INITIAL_RECORD_SIZE);
        queue = persistentQueue;
        queueCloseableOrNull = persistentQueue;
        thread = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        while (true)
                        {
                            final DataSetCodeWithStatus dataSet = queue.peekWait();
                            updater.updateDataSetStatus(dataSet.getDataSetCode(), dataSet
                                    .getStatus());
                            // Note: this is the only consumer of this queue.
                            queue.take();
                        }
                    } catch (InterruptedException ex)
                    {
                        // Exit thread.
                    } catch (InterruptedExceptionUnchecked ex)
                    {
                        // Exit thread.
                    }
                }
            }, "Updater Queue");
        thread.setDaemon(true);
        thread.start();
    }

    private static IDataSetStatusUpdater createDataSetStatusUpdater()
    {
        return new IDataSetStatusUpdater()
            {
                public void updateDataSetStatus(String dataSetCode,
                        DataSetArchivizationStatus newStatus)
                {
                    ServiceProvider.getOpenBISService().updateDataSetStatus(dataSetCode, newStatus);
                    operationLog
                            .info("Data Set " + dataSetCode + " changed status to " + newStatus);
                }

            };
    }

    /**
     * Schedules update of given data set. If operation fails the updating thread will exit.
     */
    public static void update(DataSetCodeWithStatus dataSet)
    {
        queue.add(dataSet);
    }

    private static final void close()
    {
        if (queueCloseableOrNull != null)
        {
            queueCloseableOrNull.close();
        }
    }

    /**
     * Stop the service.
     */
    public static synchronized final void stop()
    {
        if (thread == null)
        {
            return;
        }
        thread.interrupt();
        close();
        thread = null;
        queue = null;
        queueCloseableOrNull = null;
        updater = null;
    }

    /**
     * Stop the service and wait for it to finish, but at most <var>timeoutMillis</var>
     * milli-seconds.
     * 
     * @return <code>true</code>, if stopping was successful, <code>false</code> otherwise.
     */
    public static synchronized final boolean stopAndWait(long timeoutMillis)
    {
        if (thread == null)
        {
            return true;
        }
        thread.interrupt();
        try
        {
            thread.join(timeoutMillis);
        } catch (InterruptedException ex)
        {
        }
        close();
        final boolean ok = (thread.isAlive() == false);
        thread = null;
        queue = null;
        queueCloseableOrNull = null;
        updater = null;
        return ok;
    }

    /**
     * Returns <code>true</code>, if the service is currently running, <code>false</code> otherwise.
     */
    public static synchronized final boolean isRunning()
    {
        return updater != null;
    }

    /**
     * Returns the list of currently queued up items.
     */
    public static final List<DataSetCodeWithStatus> listItems(File queueFile)
    {
        return RecordBasedQueuePersister.list(DataSetCodeWithStatus.class, queueFile);
    }

    private QueueingDataSetStatusUpdaterService()
    {
        // Cannot be instantiated.
    }

    /**
     * A role that can update data set status.
     * 
     * @author Piotr Buczek
     */
    public interface IDataSetStatusUpdater
    {
        /**
         * Updates status of data set with given code.
         * 
         * @param dataSetCode code of data set to be updated
         * @param newStatus status to be set
         */
        public void updateDataSetStatus(String dataSetCode, DataSetArchivizationStatus newStatus);
    }

}
