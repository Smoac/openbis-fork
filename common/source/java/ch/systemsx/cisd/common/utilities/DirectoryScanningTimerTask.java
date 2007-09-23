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

    private boolean errorReadingDirectory;

    private final FileFilter filter;

    private final Set<File> faultyPaths;

    private final File faultyPathsFile;

    private long faultyPathsLastChanged;

    /**
     * A handler for paths. The paths are supposed to go away when they have been handled successfully.
     */
    public interface IPathHandler
    {
        /**
         * Handles the <var>path</var>.
         * 
         * @return <code>true</code> if the <var>path</var> has been handled correctly and <code>false</code>
         *         otherwise.
         */
        public boolean handle(File path);
    }

    /**
     * Creates a <var>DirectoryScanningTimerTask</var>.
     * 
     * @param sourceDirectory The directory to scan for entries.
     * @param filter The file filter that picks the entries to handle.
     * @param handler The handler that is used for treating the matching paths.
     */
    public DirectoryScanningTimerTask(File sourceDirectory, FileFilter filter, IPathHandler handler)
    {
        assert sourceDirectory != null;
        assert filter != null;
        assert handler != null;

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
            if (paths == null) // Means: error reading directory listing
            {
                return;
            }
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
        boolean logErrors = (errorReadingDirectory == false);
        final ISimpleLogger errorLogger = logErrors ? createSimpleErrorLogger() : null;

        File[] paths = FileUtilities.tryListFiles(sourceDirectory, filter, errorLogger);
        errorReadingDirectory = (paths == null); // Avoid mailbox flooding.
        return paths;
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
            final boolean handledOK = handler.handle(path);
            if (handledOK && path.exists())
            {
                operationLog.warn(String.format("Handler %s reports path '%s' be handled OK, but path still exists.",
                        handler.getClass().getSimpleName(), path));
            }
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
