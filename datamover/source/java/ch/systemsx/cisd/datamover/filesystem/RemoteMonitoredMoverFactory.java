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

package ch.systemsx.cisd.datamover.filesystem;

import java.io.File;

import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IPathHandler;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathCopier;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathRemover;
import ch.systemsx.cisd.datamover.filesystem.remote.CopyActivityMonitor;
import ch.systemsx.cisd.datamover.filesystem.remote.RemotePathMover;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;

/**
 * @author Tomasz Pylak on Sep 7, 2007
 */
public class RemoteMonitoredMoverFactory
{
    /**
     * Creates a handler to move files remotely and monitor the progress
     * 
     * @param sourceHost The host to move paths from, or <code>null</code>, if data will be moved from the local file
     *            system
     * @param destinationDirectory The directory to move paths to.
     * @param destinationHost The host to move paths to, or <code>null</code>, if <var>destinationDirectory</var> is
     *            a remote share.
     * @param fsFactory operations on (remote) file system
     * @param parameters The timing parameters used for monitoring and reporting stall situations.
     */
    public static final IPathHandler create(String sourceHost, File destinationDirectory, String destinationHost,
            IFileSysOperationsFactory fsFactory, ITimingParameters parameters)
    {
        final IPathCopier copier = fsFactory.getCopier(destinationDirectory);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(destinationDirectory, fsFactory.getReadAccessor(), copier, parameters);
        final IPathRemover remover = fsFactory.getRemover();
        return new RemotePathMover(destinationDirectory, destinationHost, monitor, remover, copier, sourceHost,
                parameters);
    }
}
