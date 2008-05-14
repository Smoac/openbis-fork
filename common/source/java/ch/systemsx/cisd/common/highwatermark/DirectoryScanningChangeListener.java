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

package ch.systemsx.cisd.common.highwatermark;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher.HighwaterMarkEvent;
import ch.systemsx.cisd.common.utilities.DirectoryScannedStore;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask;

/**
 * A <code>ChangeListener</code> implementation that informs the encapsulated
 * {@link DirectoryScanningTimerTask} when the free space is again OK.
 * <p>
 * Subclasses will fill the unhandled paths set.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class DirectoryScanningChangeListener implements ChangeListener
{
    private static final File[] EMPTY_FILE_ARRAY = new File[0];

    private DirectoryScanningTimerTask directoryScanning;

    protected final Set<File> unhandledPaths = new HashSet<File>();

    protected DirectoryScanningChangeListener()
    {
    }

    /**
     * Sets the <code>DirectoryScanningTimerTask</code> that should get informed (and remove the
     * unhandled paths from the faulty ones) when free space is again OK.
     */
    public final void setDirectoryScanning(final DirectoryScanningTimerTask directoryScanning)
    {
        assert directoryScanning != null : "Unspecified DirectoryScanningTimerTask.";
        this.directoryScanning = directoryScanning;
    }

    //
    // ChangeListener
    //

    public final void stateChanged(final ChangeEvent e)
    {
        assert directoryScanning != null : "Unspecified DirectoryScanningTimerTask.";
        final HighwaterMarkEvent event = (HighwaterMarkEvent) e;
        if (event.isBelow() == false)
        {
            directoryScanning.removeFaultyPaths(DirectoryScannedStore.asItems(unhandledPaths
                    .toArray(EMPTY_FILE_ARRAY)));
            unhandledPaths.clear();
        }
    }

}
