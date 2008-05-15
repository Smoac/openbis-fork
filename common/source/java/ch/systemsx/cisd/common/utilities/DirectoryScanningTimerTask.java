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
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collections.CollectionIO;
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
 * 
 * @author Bernd Rinn
 */
public final class DirectoryScanningTimerTask extends TimerTask
{

    static final String FAULTY_PATH_FILENAME = ".faulty_paths";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DirectoryScanningTimerTask.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DirectoryScanningTimerTask.class);

    private final IStoreHandler handler;

    private final IScannedStore sourceDirectory;

    /**
     * The number of consecutive errors of reading a directory that need to occur before the event
     * is logged.
     */
    private final int ignoredErrorCount;

    private int errorCountReadingDirectory;

    private final Set<String> faultyPaths;

    private final File faultyPathsFile;

    private long faultyPathsLastChanged;

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param filter The file filter that picks the entries to handle.
     * @param handler The handler that is used for treating the matching paths.
     */
    public DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter filter,
            final IPathHandler handler)
    {
        this(sourceDirectory, filter, handler, 0);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param filter The file filter that picks the entries to handle.
     * @param handler The handler that is used for treating the matching paths.
     * @param ignoredErrorCount The number of consecutive errors of reading the directory that need
     *            to occur before the next error is logged (can be used to suppress error when the
     *            directory is on a remote share and the server is flaky sometimes)
     */
    public DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter filter,
            final IPathHandler handler, final int ignoredErrorCount)
    {
        this(asScannedStore(sourceDirectory, filter), sourceDirectory, PathHandlerAdapter
                .asScanningHandler(sourceDirectory, handler), ignoredErrorCount);
    }

    public DirectoryScanningTimerTask(final File sourceDirectory, final FileFilter filter,
            final IStoreHandler handler)
    {
        this(asScannedStore(sourceDirectory, filter), sourceDirectory, handler, 0);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param scannedStore The store which is scan for entries.
     * @param faultyPathDirectory The directory in which file with faulty paths is should be stored.
     * @param handler The handler that is used for treating the matching paths.
     * @param ignoredErrorCount The number of consecutive errors of reading the directory that need
     *            to occur before the next error is logged (can be used to suppress error when the
     *            directory is on a remote share and the server is flaky sometimes)
     */
    public DirectoryScanningTimerTask(final IScannedStore scannedStore,
            final File faultyPathDirectory, final IStoreHandler handler, final int ignoredErrorCount)
    {
        assert scannedStore != null;
        assert handler != null;
        assert ignoredErrorCount >= 0;

        this.ignoredErrorCount = ignoredErrorCount;
        this.sourceDirectory = scannedStore;
        this.handler = handler;
        this.faultyPaths = new HashSet<String>();
        this.faultyPathsFile = new File(faultyPathDirectory, FAULTY_PATH_FILENAME);
        faultyPathsFile.delete();
    }

    private final static IScannedStore asScannedStore(final File directory, final FileFilter filter)
    {
        return new DirectoryScannedStore(filter, directory);
    }

    private final static String getLocationDescription(final File file)
    {
        return file.getPath();
    }

    /**
     * Handles all entries in the source directory that are picked by the filter.
     */
    @Override
    public final void run()
    {
        try
        {
            if (operationLog.isTraceEnabled())
            {
                operationLog.trace("Start scanning directory " + sourceDirectory + ".");
            }
            checkForFaultyPathsFileChanged();
            final StoreItem[] paths = listFiles();
            for (final StoreItem path : paths)
            {
                if (isFaultyPathsFile(path)) // Never touch the faultyPathsFile.
                {
                    continue;
                }
                try
                {
                    handle(path);
                } catch (final Exception ex) // do not stop when processing of one file has
                // failed,
                // continue with other files
                {
                    printNotification(ex);
                }

            }
            if (operationLog.isTraceEnabled())
            {
                operationLog.trace("Finished scanning directory " + sourceDirectory + ".");
            }
        } catch (final Exception ex)
        {
            printNotification(ex);
        }
    }

    private final void printNotification(final Exception ex)
    {
        notificationLog.error("An exception has occurred. (thread still running)", ex);
    }

    private boolean isFaultyPathsFile(final StoreItem item)
    {
        final String itemLocation = sourceDirectory.getLocationDescription(item);
        final String faultyPathsLocation = getLocationDescription(faultyPathsFile);
        return itemLocation.equals(faultyPathsLocation);
    }

    private final void checkForFaultyPathsFileChanged()
    {
        if (faultyPathsFile.exists())
        {
            if (faultyPathsFile.lastModified() > faultyPathsLastChanged) // Handles manual
            // manipulation.
            {
                faultyPaths.clear();
                CollectionIO.readCollection(faultyPathsFile, faultyPaths);
                faultyPathsLastChanged = faultyPathsFile.lastModified();
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format(
                            "Reread faulty paths file (%s), new set contains %d entries",
                            getLocationDescription(faultyPathsFile), faultyPaths.size()));
                }
            }
        } else
        // Handles manual removal.
        {
            faultyPaths.clear();
        }
    }

    private final StoreItem[] listFiles()
    {
        final boolean logNotifyError = (errorCountReadingDirectory == ignoredErrorCount); // Avoid
        // mailbox
        // flooding.
        final boolean logOperationError = (errorCountReadingDirectory < ignoredErrorCount);
        final ISimpleLogger errorLogger =
                logNotifyError ? createSimpleErrorLogger(LogCategory.NOTIFY)
                        : (logOperationError ? createSimpleErrorLogger(LogCategory.OPERATION)
                                : null);

        final StoreItem[] paths = sourceDirectory.tryListSortedReadyToProcess(errorLogger);
        if (errorCountReadingDirectory > ignoredErrorCount && paths != null)
        {
            if (notificationLog.isInfoEnabled())
            {
                notificationLog.info(String.format("Directory '%s' is available again.",
                        sourceDirectory));
            }
        }
        if (paths == null)
        {
            ++errorCountReadingDirectory;
        } else
        {
            errorCountReadingDirectory = 0;
        }
        return (paths == null) ? new StoreItem[0] : paths;
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

    private final void handle(final StoreItem item)
    {
        if (isFaultyPath(item))
        { // Guard: skip faulty paths.
            return;
        }
        try
        {
            if (handler.mayHandle(item))
            {
                handler.handle(item);
            }
        } finally
        {
            // If the item still exists, we assume that it has not been handled. So it should be
            // added to the faulty
            // paths.
            if (sourceDirectory.exists(item))
            {
                addToFaultyPaths(item);
            }
        }
    }

    private final boolean isFaultyPath(final StoreItem item)
    {
        final String path = sourceDirectory.getLocationDescription(item);
        return faultyPaths.contains(path);
    }

    private final void addToFaultyPaths(final StoreItem item)
    {
        final String path = sourceDirectory.getLocationDescription(item);
        faultyPaths.add(path);
        refreshFaultyPathsFile();
    }

    private final void refreshFaultyPathsFile()
    {
        CollectionIO.writeIterable(faultyPathsFile, faultyPaths);
        faultyPathsLastChanged = faultyPathsFile.lastModified();
    }

    /** Removes given <var>storeItems</var> from the set of faulty ones. */
    public final void removeFaultyPaths(final StoreItem... storeItems)
    {
        assert storeItems != null : "Unspecified store items.";
        final int size = storeItems.length;
        if (size == 0)
        {
            return;
        }
        final Set<String> paths = new HashSet<String>(size);
        for (final StoreItem storeItem : storeItems)
        {
            paths.add(sourceDirectory.getLocationDescription(storeItem));
        }
        faultyPaths.removeAll(paths);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following paths %s have been removed from the the faulty ones.", paths));
        }
        refreshFaultyPathsFile();
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
