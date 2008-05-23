/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.io.File;
import java.io.FileFilter;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * A {@link TimerTask} that scans a source directory for entries that are accepted by some
 * {@link FileFilter} and handles the accepted entries by some {@link IPathHandler}. It maintains a
 * list of faulty paths that failed to be handled OK in the past. Clearing the list will make the
 * class to retry handling the paths.
 * <p>
 * The class should be constructed in the start-up phase and as part of the system's self-test in
 * order to reveal problems with incorrect paths timely.
 * </p>
 * 
 * @author Bernd Rinn
 */
public final class DirectoryScanningTimerTask extends TimerTask
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DirectoryScanningTimerTask.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DirectoryScanningTimerTask.class);

    private final IStoreHandler storeHandler;

    private final IScannedStore sourceDirectory;

    /**
     * The number of consecutive errors of reading a directory that need to occur before the event
     * is logged.
     */
    private final int ignoredErrorCount;

    private int errorCountReadingDirectory;

    private final IDirectoryScanningHandler directoryScanningHandler;

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param filter The file filter that picks the entries to handle.
     * @param handler The handler that is used for treating the matching paths.
     * @param directoryScanningHandler A directory scanning handler.
     */
    public DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter filter,
            final IPathHandler handler, final IDirectoryScanningHandler directoryScanningHandler)
    {
        this(asScannedStore(sourceDirectory, filter), directoryScanningHandler, PathHandlerAdapter
                .asScanningHandler(sourceDirectory, handler), 0);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param storeHandler The handler that is used for treating the matching paths.
     * @param directoryScanningHandler A directory scanning handler.
     */
    public DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter filter,
            final IStoreHandler storeHandler,
            final IDirectoryScanningHandler directoryScanningHandler)
    {
        this(asScannedStore(sourceDirectory, filter), directoryScanningHandler, storeHandler, 0);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param filter The file filter that picks the entries to handle.
     * @param pathHandler The handler that is used for treating the matching paths.
     */
    public DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter filter,
            final IPathHandler pathHandler)
    {
        this(sourceDirectory, filter, pathHandler, 0);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param scannedStore The store which is scan for entries.
     * @param directoryScanningHandler A directory scanning handler.
     * @param storeHandler The handler that is used for treating the matching paths.
     * @param ignoredErrorCount The number of consecutive errors of reading the directory that need
     *            to occur before the next error is logged (can be used to suppress error when the
     *            directory is on a remote share and the server is flaky sometimes)
     */
    public DirectoryScanningTimerTask(final IScannedStore scannedStore,
            final IDirectoryScanningHandler directoryScanningHandler,
            final IStoreHandler storeHandler, final int ignoredErrorCount)
    {
        assert scannedStore != null;
        assert storeHandler != null;
        assert directoryScanningHandler != null : "Unspecified IDirectoryScanningHandler implementation";
        assert ignoredErrorCount >= 0;

        this.ignoredErrorCount = ignoredErrorCount;
        this.sourceDirectory = scannedStore;
        this.storeHandler = storeHandler;
        this.directoryScanningHandler = directoryScanningHandler;
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param fileFilter The file filter that picks the entries to handle.
     * @param pathHandler The handler that is used for treating the matching paths.
     * @param ignoredErrorCount The number of consecutive errors of reading the directory that need
     *            to occur before the next error is logged (can be used to suppress error when the
     *            directory is on a remote share and the server is flaky sometimes)
     */
    DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter fileFilter,
            final IPathHandler pathHandler, final int ignoredErrorCount)
    {
        this(asScannedStore(sourceDirectory, fileFilter), new FaultyPathDirectoryScanningHandler(
                sourceDirectory), PathHandlerAdapter
                .asScanningHandler(sourceDirectory, pathHandler), ignoredErrorCount);
    }

    private final static IScannedStore asScannedStore(final File directory, final FileFilter filter)
    {
        return new DirectoryScannedStore(filter, directory);
    }

    /**
     * Handles all entries in the source directory that are picked by the filter.
     */
    @Override
    public final void run()
    {
        if (operationLog.isTraceEnabled())
        {
            operationLog.trace("Start scanning directory " + sourceDirectory + ".");
        }
        try
        {
            final StoreItem[] storeItems = listStoreItems();
            directoryScanningHandler.beforeHandle();
            for (final StoreItem storeItem : storeItems)
            {
                if (directoryScanningHandler.mayHandle(sourceDirectory, storeItem))
                {
                    try
                    {
                        storeHandler.handle(storeItem);
                        if (operationLog.isDebugEnabled())
                        {
                            operationLog.debug(String.format(
                                    "Following store item '%s' has been handled.", storeItem));
                        }
                    } catch (final Exception ex)
                    {
                        // Do not stop when processing of one file has failed,
                        // continue with other files.
                        printNotification(ex);
                    } finally
                    {
                        directoryScanningHandler.finishItemHandle(sourceDirectory, storeItem);
                    }
                } else
                {
                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug(String.format(
                                "Following store item '%s' has NOT been handled.", storeItem));
                    }

                }
            }
        } catch (final Exception ex)
        {
            printNotification(ex);
        }
        if (operationLog.isTraceEnabled())
        {
            operationLog.trace("Finished scanning directory " + sourceDirectory + ".");
        }
    }

    private final void printNotification(final Exception ex)
    {
        notificationLog.error("An exception has occurred. (thread still running)", ex);
    }

    private final StoreItem[] listStoreItems()
    {
        // Avoid mailbox flooding.
        final boolean logNotifyError = (errorCountReadingDirectory == ignoredErrorCount);
        final boolean logOperationError = (errorCountReadingDirectory < ignoredErrorCount);
        final ISimpleLogger errorLogger =
                logNotifyError ? createSimpleErrorLogger(LogCategory.NOTIFY)
                        : (logOperationError ? createSimpleErrorLogger(LogCategory.OPERATION)
                                : null);
        final StoreItem[] storeItems = sourceDirectory.tryListSortedReadyToProcess(errorLogger);
        if (errorCountReadingDirectory > ignoredErrorCount && storeItems != null)
        {
            if (notificationLog.isInfoEnabled())
            {
                notificationLog.info(String.format("Directory '%s' is available again.",
                        sourceDirectory));
            }
        }
        if (storeItems == null)
        {
            ++errorCountReadingDirectory;
        } else
        {
            errorCountReadingDirectory = 0;
        }
        return (storeItems == null) ? StoreItem.EMPTY_ARRAY : storeItems;
    }

    private final ISimpleLogger createSimpleErrorLogger(final LogCategory category)
    {
        return new ISimpleLogger()
            {

                //
                // ISimpleLogger
                //

                public final void log(final LogLevel dummyLevel, final String message)
                {
                    if (category == LogCategory.NOTIFY)
                    {
                        notificationLog.log(org.apache.log4j.Level.ERROR, message);
                    } else
                    {
                        operationLog.log(org.apache.log4j.Level.WARN, message);
                    }
                }
            };
    }

    //
    // Helper classes
    //

    public static interface IScannedStore
    {
        /**
         * List items in the scanned store in order in which they should be handled.
         */
        StoreItem[] tryListSortedReadyToProcess(ISimpleLogger loggerOrNull);

        boolean exists(StoreItem item);

        /**
         * returned description should give the user the idea about file location. You should not
         * use the result for something else than printing it for user. It should not be especially
         * assumed that the result is the path which could be used in java.io.File constructor.
         */
        String getLocationDescription(StoreItem item);
    }
}
