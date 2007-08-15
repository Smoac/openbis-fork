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
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A <code>TimerTask</code> extension that watch a given file.
 * 
 * @author Christian Ribeaud
 */
public abstract class FileWatcher extends TimerTask
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, FileWatcher.class);

    /** The default <code>FileWatcherState</code> implementation used. */
    private static final IFileWatcherState DEFAULT_FILE_WATCHER_STATE = new LastModifiedState();

    private final File fileToWatch;

    private final IFileWatcherState fileWatcherState;

    /** Whether the user has already been warned. */
    private boolean warnedAlready = false;

    public FileWatcher(File fileToWatch)
    {
        this(fileToWatch, DEFAULT_FILE_WATCHER_STATE);
    }

    public FileWatcher(File fileToWatch, IFileWatcherState fileWatcherState)
    {
        this.fileToWatch = fileToWatch;
        this.fileWatcherState = fileWatcherState;
        fileWatcherState.saveInitialState(fileToWatch);
    }

    public final File getFileToWatch()
    {
        return fileToWatch;
    }

    /** The action we should perform when the file we are watching has changed its state. */
    protected abstract void onChange();

    //
    // TimerTask
    //

    @Override
    public final void run()
    {
        boolean fileExists = false;
        try
        {
            fileExists = fileToWatch.exists();
        } catch (SecurityException e)
        {
            operationLog.warn(String.format("Was not allowed to check existence of file '%s'.", fileToWatch), e);
            return;
        }
        if (fileExists)
        {
            if (fileWatcherState.stateChanged(fileToWatch))
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format("Watched file '%s' has changed.", fileToWatch));
                }
                onChange();
                warnedAlready = false;
            } else
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format("Watched file '%s' did not change.", fileToWatch));
                }
            }
        } else
        {
            if (warnedAlready == false)
            {
                operationLog.warn(String.format("Given file '%s' does not exist.", fileToWatch));
                warnedAlready = true;
            }
        }
    }

    /**
     * Specifies the file property (or character) we are watching.
     * 
     * @author Christian Ribeaud
     */
    public static interface IFileWatcherState
    {

        /**
         * Saves the initial state of given <var>fileToWatch</var>.
         * <p>
         * This method is called when <code>FileWatcher</code> gets instantiated.
         * </p>
         */
        public void saveInitialState(File fileToWatch);

        /**
         * Whether the file state has changed.
         * <p>
         * If the file state has changed, each implementation should save the new state before returning
         * <code>true</code>.
         * </p>
         */
        public boolean stateChanged(File fileToWatch);
    }

    /**
     * A <code>FileWatcherState</code> implementation that works with the value returned by
     * {@link File#lastModified()}.
     * 
     * @author Christian Ribeaud
     */
    public final static class LastModifiedState implements IFileWatcherState
    {

        private long lastModified;

        //
        // FileWatcherCondition
        //

        public final void saveInitialState(File fileToWatch)
        {
            lastModified = fileToWatch.lastModified();
        }

        public final boolean stateChanged(File fileToWatch)
        {
            long current = fileToWatch.lastModified();
            if (current > lastModified)
            {
                lastModified = current;
                return true;
            }
            return false;
        }
    }
}
