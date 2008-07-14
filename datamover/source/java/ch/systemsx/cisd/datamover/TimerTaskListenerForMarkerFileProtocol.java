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

package ch.systemsx.cisd.datamover;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.concurrent.DummyTimerTaskListener;
import ch.systemsx.cisd.common.concurrent.ITimerTaskListener;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.ITimerTaskStatusProvider;

/**
 * An implementation of {@link ITimerTaskListener} which creates an empty marker file before the
 * timer task is executed and which removes this marker file when the task has been finished.
 * Optionally, the listener can create another marker file when the task is finished (which it will
 * not remove itself).
 * 
 * @author Franz-Josef Elmer
 */
public class TimerTaskListenerForMarkerFileProtocol extends DummyTimerTaskListener
{
    private final File markerFile;

    private final File errorMarkerFileOrNull;

    private final File successorMarkerFileOrNull;

    /**
     * Creates an instance for the specified marker file.
     * 
     * @param markerFileName The name of the marker file that indicates that the
     *            {@link java.util.TimerTask} is currently running.
     * @param errorMarkerFileNameOrNull The name of the file indicating that the last time the timer
     *            task was running it produced an error.
     * @param successorMarkerFileNameOrNull The name of the file to indicate that the successor of
     *            this timer task has some work to do.
     * @throws IllegalArgumentException if the <var>markerFileName</var> is <code>null</code> or
     *             it denotes a directory.
     */
    public TimerTaskListenerForMarkerFileProtocol(String markerFileName,
            String errorMarkerFileNameOrNull, String successorMarkerFileNameOrNull)
    {
        if (markerFileName == null)
        {
            throw new IllegalArgumentException("Unspecified start marker file name.");
        }
        markerFile = new File(markerFileName);
        failIfDirectory(markerFile);
        if (errorMarkerFileNameOrNull != null)
        {
            errorMarkerFileOrNull = new File(errorMarkerFileNameOrNull);
            failIfDirectory(errorMarkerFileOrNull);
        } else
        {
            errorMarkerFileOrNull = null;
        }
        if (successorMarkerFileNameOrNull != null)
        {
            successorMarkerFileOrNull = new File(successorMarkerFileNameOrNull);
            failIfDirectory(successorMarkerFileOrNull);
        } else
        {
            successorMarkerFileOrNull = null;
        }
    }

    /**
     * Creates empty marker file.
     * 
     * @throws EnvironmentFailureException if an {@link IOException} occurs.
     */
    @Override
    public void startRunning()
    {
        touch(markerFile);
    }

    /**
     * Deletes the marker file.
     */
    @Override
    public void finishRunning(ITimerTaskStatusProvider statusProviderOrNull)
    {
        if (successorMarkerFileOrNull != null && hasPerformedMeaningfullWork(statusProviderOrNull))
        {
            touch(successorMarkerFileOrNull);
        }
        if (errorMarkerFileOrNull != null && hasErrors(statusProviderOrNull))
        {
            touch(errorMarkerFileOrNull);
        }
        // Avoid deleting the marker file when it is used as error marker file, too, and an error
        // occurred.
        if (markerFile.equals(errorMarkerFileOrNull) == false
                || hasErrors(statusProviderOrNull) == false)
        {
            markerFile.delete();
        }
    }

    private boolean hasPerformedMeaningfullWork(ITimerTaskStatusProvider statusProviderOrNull)
    {
        return (statusProviderOrNull == null) || statusProviderOrNull.hasPerformedMeaningfulWork();
    }

    private boolean hasErrors(ITimerTaskStatusProvider statusProviderOrNull)
    {
        return (statusProviderOrNull != null) && statusProviderOrNull.hasErrors();
    }

    private static void failIfDirectory(File markerFile)
    {
        if (markerFile.isDirectory())
        {
            throw new IllegalArgumentException("Marker file is a directory: "
                    + markerFile.getAbsolutePath());
        }
    }

    private static void touch(final File markerFile) throws EnvironmentFailureException
    {
        try
        {
            FileUtils.touch(markerFile);
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("Can not create marker file '"
                    + markerFile.getAbsolutePath() + "'.", ex);
        }
    }

}
