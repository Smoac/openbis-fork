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

package ch.systemsx.cisd.datamover;

import java.io.File;

import ch.systemsx.cisd.common.utilities.IPathHandler;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.QueuingPathHandler;
import ch.systemsx.cisd.datamover.filesystem.RemoteMonitoredMoverFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.utils.FileStore;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * A class that starts up the processing pipeline and its monitoring, based on the parameters provided.
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak on Aug 24, 2007
 */
public class DataMover
{
    private final static String LOCAL_COPY_IN_PROGRESS_DIR = "copy-in-progress";

    private final static String LOCAL_COPY_COMPLETE_DIR = "copy-complete";

    private final static String LOCAL_READY_TO_MOVE_DIR = "ready-to-move";

    private final static String LOCAL_TEMP_DIR = "tmp";

    private final Parameters parameters;

    private final IFileSysOperationsFactory factory;

    private final LocalBufferDirs bufferDirs;

    /**
     * starts the process of moving data and monitoring it
     * 
     * @return object which can be used to terminate the process and all its threads
     */
    public static final ITerminable start(Parameters parameters, IFileSysOperationsFactory factory)
    {
        return start(parameters, factory, createLocalBufferDirs(parameters));
    }

    private static LocalBufferDirs createLocalBufferDirs(Parameters parameters)
    {
        return new LocalBufferDirs(parameters.getBufferStore().getPath(), LOCAL_COPY_IN_PROGRESS_DIR,
                LOCAL_COPY_COMPLETE_DIR, LOCAL_READY_TO_MOVE_DIR, LOCAL_TEMP_DIR);
    }

    /** Allows to specify buffer directories. Exposed for testing purposes. */
    public static final ITerminable start(Parameters parameters, IFileSysOperationsFactory factory,
            LocalBufferDirs localBufferDirs)
    {
        return new DataMover(parameters, factory, localBufferDirs).start();
    }

    private DataMover(Parameters parameters, IFileSysOperationsFactory factory, LocalBufferDirs bufferDirs)
    {
        this.parameters = parameters;
        this.factory = factory;
        this.bufferDirs = bufferDirs;
    }

    private ITerminable start()
    {
        QueuingPathHandler outgoingProcessor = startupOutgoingMovingProcess(parameters.getOutgoingStore());
        QueuingPathHandler localProcessor = startupLocalProcessing(outgoingProcessor);
        ITerminable incomingProcessor = startupIncomingMovingProcess(localProcessor);
        return createCompoundTerminable(outgoingProcessor, localProcessor, incomingProcessor);
    }

    private ITerminable startupIncomingMovingProcess(IPathHandler localProcessor)
    {
        return IncomingProcessor.startupMovingProcess(parameters, factory, bufferDirs, localProcessor);
    }

    private QueuingPathHandler startupLocalProcessing(QueuingPathHandler outgoingHandler)
    {
        final IPathHandler localProcessingHandler =
                LocalProcessor.createAndRecover(parameters, bufferDirs.getCopyCompleteDir(), bufferDirs
                        .getReadyToMoveDir(), bufferDirs.getTempDir(), outgoingHandler, factory);
        return QueuingPathHandler.create(localProcessingHandler, "Local Processor");
    }

    private QueuingPathHandler startupOutgoingMovingProcess(FileStore outputDir)
    {
        final IPathHandler remoteMover = createRemotePathMover(null, outputDir.getPath(), outputDir.getHost());
        return QueuingPathHandler.create(remoteMover, "Final Destination Mover");
    }

    private IPathHandler createRemotePathMover(String sourceHost, File destinationDirectory, String destinationHost)
    {
        return RemoteMonitoredMoverFactory.create(sourceHost, destinationDirectory, destinationHost, factory,
                parameters);
    }

    private static ITerminable createCompoundTerminable(final ITerminable... terminables)
    {
        return new ITerminable()
            {
                public boolean terminate()
                {
                    boolean ok = true;
                    for (ITerminable terminable : terminables)
                    {
                        ok = ok && terminable.terminate();
                    }
                    return ok;
                }
            };
    }
}
