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

/**
 * An implementation of {@link ITimerTaskListener} which creates an empty marker file before
 * the timer task is executed and which removes this marker file if the task has been finished.
 *
 * @author Franz-Josef Elmer
 */
public class TimerTaskListenerForMarkerFileProtocol extends DummyTimerTaskListener
{
    private final File markerFile;

    /**
     * Creates an instance for the specified marker file.
     * 
     * @throws IllegalArgumentException if the argument is <code>null</code> or it denotes a
     *             directory.
     */
    public TimerTaskListenerForMarkerFileProtocol(String markerFileName)
    {
        if (markerFileName == null)
        {
            throw new IllegalArgumentException("Unspecified marker file name.");
        }
        markerFile = new File(markerFileName);
        if (markerFile.isDirectory())
        {
            throw new IllegalArgumentException("Marker file is a directory: "
                    + markerFile.getAbsolutePath());
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
        try
        {
            FileUtils.touch(markerFile);
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("Can not create marker file '"
                    + markerFile.getAbsolutePath() + "'.", ex);
        }
    }
    
    /**
     * Deletes the marker file.
     */
    @Override
    public void finishRunning()
    {
        markerFile.delete();
    }

    
}
