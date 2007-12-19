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

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A {@link TimerTask} that scans a source directory for entries that are accepted by some {@link FileFilter} and
 * handles the accepted entries by some {@link IPathHandler}. It maintains a list of faulty paths that failed to be
 * handled OK in the past. Clearing the list will make the class to retry handling the paths.
 * <p>
 * The class should be constructed in the start-up phase and as part of the system's self-test in order to reveal
 * problems with incorrect paths timely.
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

    public static interface IScannedStore
    {
        /**
         * List items in the scanned store in order in which they should be handled.
         */
        StoreItem[] tryListSortedReadyToProcess(ISimpleLogger loggerOrNull);

        boolean exists(StoreItem item);

        /**
         * returned description should give the user the idea about file location. You should not use the result for
         * something else than printing it for user. It should not be especially assumed that the result is the path
         * which could be used in java.io.File constructor.
         */
        String getLocationDescription(StoreItem item);
    }

    private final IStoreHandler handler;

    private final IScannedStore sourceDirectory;

    /** The number of consecutive errors of reading a directory that need to occur before the event is logged. */
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
    public DirectoryScanningTimerTask(File sourceDirectory, FileFilter filter, IPathHandler handler)
    {
        this(sourceDirectory, filter, handler, 0);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param filter The file filter that picks the entries to handle.
     * @param handler The handler that is used for treating the matching paths.
     * @param ignoredErrorCount The number of consecutive errors of reading the directory that need to occur before the
     *            next error is logged (can be used to suppress error when the directory is on a remote share and the
     *            server is flaky sometimes)
     */
    public DirectoryScanningTimerTask(File sourceDirectory, FileFilter filter, IPathHandler handler,
            int ignoredErrorCount)
    {
        this(asScannedStore(sourceDirectory, filter), sourceDirectory, asScanningHandler(sourceDirectory, handler),
                ignoredErrorCount);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param scannedStore The store which is scan for entries.
     * @param faultyPathDirectory The directory in which file with faulty paths is should be stored.
     * @param handler The handler that is used for treating the matching paths.
     * @param ignoredErrorCount The number of consecutive errors of reading the directory that need to occur before the
     *            next error is logged (can be used to suppress error when the directory is on a remote share and the
     *            server is flaky sometimes)
     */
    public DirectoryScanningTimerTask(IScannedStore scannedStore, File faultyPathDirectory, IStoreHandler handler,
            int ignoredErrorCount)
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

    private static IStoreHandler asScanningHandler(final File directory, final IPathHandler handler)
    {
        return new IStoreHandler()
            {
                public void handle(StoreItem item)
                {
                    File path = asFile(directory, item);
                    handler.handle(path);
                }
            };
    }

    private static IScannedStore asScannedStore(final File directory, final FileFilter filter)
    {
        return new IScannedStore()
            {
                public String getLocationDescription(StoreItem item)
                {
                    return DirectoryScanningTimerTask.getLocationDescription(asFile(item));
                }

                public boolean exists(StoreItem item)
                {
                    return asFile(item).exists();
                }

                public StoreItem[] tryListSortedReadyToProcess(ISimpleLogger loggerOrNull)
                {
                    File[] files = FileUtilities.tryListFiles(directory, filter, loggerOrNull);
                    if (files != null)
                    {
                        FileUtilities.sortByLastModified(files);
                        return asItems(files);
                    } else
                    {
                        return null;
                    }
                }

                private StoreItem[] asItems(File[] files)
                {
                    StoreItem[] items = new StoreItem[files.length];
                    for (int i = 0; i < items.length; i++)
                    {
                        items[i] = new StoreItem(files[i].getName());
                    }
                    return items;
                }

                private File asFile(StoreItem item)
                {
                    return DirectoryScanningTimerTask.asFile(directory, item);
                }
            };
    }

    private static String getLocationDescription(File file)
    {
        return file.getPath();
    }

    private static File asFile(File parentDirectory, StoreItem item)
    {
        return new File(parentDirectory, item.getName());
    }

    /**
     * Handles all entries in the source directory that are picked by the filter.
     */
    @Override
    public void run()
    {
        try
        {
            if (operationLog.isTraceEnabled())
            {
                operationLog.trace("Start scanning directory " + sourceDirectory + ".");
            }
            checkForFaultyPathsFileChanged();
            final StoreItem[] paths = listFiles();
            for (StoreItem path : paths)
            {
                if (isFaultyPathsFile(path)) // Never touch the faultyPathsFile.
                {
                    continue;
                }
                handle(path);
            }
            if (operationLog.isTraceEnabled())
            {
                operationLog.trace("Finished scanning directory " + sourceDirectory + ".");
            }
        } catch (Exception ex)
        {
            notificationLog.error("An exception has occurred. (thread still running)", ex);
        }
    }

    private boolean isFaultyPathsFile(StoreItem item)
    {
        String itemLocation = sourceDirectory.getLocationDescription(item);
        String faultyPathsLocation = getLocationDescription(faultyPathsFile);
        return itemLocation.equals(faultyPathsLocation);
    }

    private void checkForFaultyPathsFileChanged()
    {
        if (faultyPathsFile.exists())
        {
            if (faultyPathsFile.lastModified() > faultyPathsLastChanged) // Handles manual manipulation.
            {
                faultyPaths.clear();
                CollectionIO.readCollection(faultyPathsFile, faultyPaths);
                faultyPathsLastChanged = faultyPathsFile.lastModified();
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format("Reread faulty paths file (%s), new set contains %d entries",
                            getLocationDescription(faultyPathsFile), faultyPaths.size()));
                }
            }
        } else
        // Handles manual removal.
        {
            faultyPaths.clear();
        }
    }

    private StoreItem[] listFiles()
    {
        final boolean logNotifyError = (errorCountReadingDirectory == ignoredErrorCount); // Avoid mailbox flooding.
        final boolean logOperationError = (errorCountReadingDirectory < ignoredErrorCount);
        final ISimpleLogger errorLogger =
                logNotifyError ? createSimpleErrorLogger(LogCategory.NOTIFY)
                        : (logOperationError ? createSimpleErrorLogger(LogCategory.OPERATION) : null);

        final StoreItem[] paths = sourceDirectory.tryListSortedReadyToProcess(errorLogger);
        if (errorCountReadingDirectory > ignoredErrorCount && paths != null)
        {
            if (notificationLog.isInfoEnabled())
            {
                notificationLog.info(String.format("Directory '%s' is available again.", sourceDirectory));
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

    private ISimpleLogger createSimpleErrorLogger(final LogCategory category)
    {
        return new ISimpleLogger()
            {
                public void log(ISimpleLogger.Level dummyLevel, String message)
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

    private void handle(StoreItem item)
    {
        if (isFaultyPath(item))
        { // Guard: skip faulty paths.
            return;
        }
        try
        {
            handler.handle(item);
        } finally
        {
            // If the item still exists, we assume that it has not been handled. So it should be added to the faulty
            // paths.
            if (sourceDirectory.exists(item))
            {
                addToFaultyPaths(item);
            }
        }
    }

    private boolean isFaultyPath(StoreItem item)
    {
        String path = sourceDirectory.getLocationDescription(item);
        return faultyPaths.contains(path);
    }

    private void addToFaultyPaths(StoreItem item)
    {
        String path = sourceDirectory.getLocationDescription(item);
        faultyPaths.add(path);
        CollectionIO.writeIterable(faultyPathsFile, faultyPaths);
        faultyPathsLastChanged = faultyPathsFile.lastModified();
    }
}
