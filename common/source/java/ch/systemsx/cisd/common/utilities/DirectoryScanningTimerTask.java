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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
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
public final class DirectoryScanningTimerTask extends TimerTask implements ISelfTestable
{

    public static final String FAULTY_PATH_FILENAME = ".faulty_paths";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DirectoryScanningTimerTask.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DirectoryScanningTimerTask.class);

    private static final IFromStringConverter<File> FILE_CONVERTER = new IFromStringConverter<File>()
        {
            public File fromString(String value)
            {
                return new File(value);
            }
        };

    private final IPathHandler handler;

    private final File sourceDirectory;

    /** The number of consecutive errors of reading a directory that need to occur before the event is logged. */
    private final int ignoredErrorCount;

    private int errorCountReadingDirectory;

    private final FileFilter filter;

    private final Set<File> faultyPaths;

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
        assert sourceDirectory != null;
        assert filter != null;
        assert handler != null;
        assert ignoredErrorCount >= 0;

        this.ignoredErrorCount = ignoredErrorCount;
        this.sourceDirectory = sourceDirectory;
        this.filter = filter;
        this.handler = handler;
        this.faultyPaths = new HashSet<File>();
        this.faultyPathsFile = new File(sourceDirectory, FAULTY_PATH_FILENAME);
        faultyPathsFile.delete();
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
            final File[] paths = listFiles();
            // Sort in order of "oldest first" in order to move older items before newer items. This becomes important
            // when doing online quality control of measurements.
            Arrays.sort(paths, FileComparator.BY_LAST_MODIFIED);
            for (File path : paths)
            {
                if (faultyPathsFile.equals(path)) // Never touch the faultyPathsFile.
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

    private void checkForFaultyPathsFileChanged()
    {
        if (faultyPathsFile.exists())
        {
            if (faultyPathsFile.lastModified() > faultyPathsLastChanged) // Handles manual manipulation.
            {
                faultyPaths.clear();
                CollectionIO.readCollection(faultyPathsFile, faultyPaths, FILE_CONVERTER);
                faultyPathsLastChanged = faultyPathsFile.lastModified();
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format("Reread faulty paths file (%s), new set contains %d entries",
                            faultyPathsFile.getPath(), faultyPaths.size()));
                }
            }
        } else
        // Handles manual removal.
        {
            faultyPaths.clear();
        }
    }

    private File[] listFiles()
    {
        final boolean logErrors = (errorCountReadingDirectory == ignoredErrorCount); // Avoid mailbox flooding.
        final ISimpleLogger errorLogger = logErrors ? createSimpleErrorLogger() : null;

        final File[] paths = FileUtilities.tryListFiles(sourceDirectory, filter, errorLogger);
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
        return (paths == null) ? new File[0] : paths;
    }

    private ISimpleLogger createSimpleErrorLogger()
    {
        return new ISimpleLogger()
            {
                public void log(String message)
                {
                    notificationLog.log(Level.ERROR, message);
                }

                public void log(String messageTemplate, Object... args)
                {
                    log(String.format(messageTemplate, args));
                }
            };
    }

    private void handle(File path)
    {
        if (faultyPaths.contains(path))
        { // Guard: skip faulty paths.
            return;
        }
        try
        {
            handler.handle(path);
        } finally
        {
            if (path.exists())
            {
                addToFaultyPaths(path);
            }
        }
    }

    private void addToFaultyPaths(File path)
    {
        faultyPaths.add(path);
        CollectionIO.writeIterable(faultyPathsFile, faultyPaths);
        faultyPathsLastChanged = faultyPathsFile.lastModified();
    }

    public void check() throws ConfigurationFailureException
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Checking source directory '" + sourceDirectory.getAbsolutePath() + "'.");
        }
        final String errorMessage = FileUtilities.checkDirectoryFullyAccessible(sourceDirectory, "source");
        if (errorMessage != null)
        {
            throw new ConfigurationFailureException(errorMessage);
        }
    }
}
