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

import ch.systemsx.cisd.common.utilities.IStoreHandler;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;
import ch.systemsx.cisd.datamover.filesystem.remote.RemotePathMover;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;

/**
 * @author Tomasz Pylak
 */
public final class RemoteMonitoredMoverFactory
{

    private RemoteMonitoredMoverFactory()
    {
        // Can not be instantiated.
    }

    /**
     * Creates a handler to move files remotely from source to destination and monitor the progress
     * 
     * @param sourceDirectory The directory to move paths from
     * @param destinationDirectory The directory to move paths to.
     * @param parameters The timing parameters used for monitoring and reporting stall situations.
     */
    public static final IStoreHandler create(final IFileStore sourceDirectory,
            final IFileStore destinationDirectory, final ITimingParameters parameters)
    {
        final IStoreCopier copier = sourceDirectory.getCopier(destinationDirectory);
        return new RemotePathMover(sourceDirectory, destinationDirectory, copier, parameters);
    }
}
