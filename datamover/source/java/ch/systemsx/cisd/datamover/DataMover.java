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
import java.util.Timer;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkDirectoryScanningHandler;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.utilities.FaultyPathDirectoryScanningHandler;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.IStoreHandler;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.ITriggerable;
import ch.systemsx.cisd.common.utilities.TimerHelper;
import ch.systemsx.cisd.common.utilities.TriggeringTimerTask;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.RemoteMonitoredMoverFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * A class that starts up the processing pipeline and its monitoring, based on the parameters
 * provided.
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak
 */
public class DataMover
{
    private final static String LOCAL_COPY_IN_PROGRESS_DIR = "copy-in-progress";

    private final static String LOCAL_COPY_COMPLETE_DIR = "copy-complete";

    private final static String LOCAL_READY_TO_MOVE_DIR = "ready-to-move";

    private final static String LOCAL_TEMP_DIR = "tmp";

    // @Private
    static final String RECOVERY_MARKER_FIILENAME = Constants.MARKER_PREFIX + "recovery";

    private final Parameters parameters;

    private final IFileSysOperationsFactory factory;

    private final LocalBufferDirs bufferDirs;

    /**
     * starts the process of moving data and monitoring it
     * 
     * @return object which can be used to terminate the process and all its threads
     */
    public static final ITerminable start(final Parameters parameters,
            final IFileSysOperationsFactory factory)
    {
        return start(parameters, factory, createLocalBufferDirs(parameters));
    }

    private static LocalBufferDirs createLocalBufferDirs(final Parameters parameters)
    {
        return new LocalBufferDirs(parameters.getBufferDirectoryPath(), LOCAL_COPY_IN_PROGRESS_DIR,
                LOCAL_COPY_COMPLETE_DIR, LOCAL_READY_TO_MOVE_DIR, LOCAL_TEMP_DIR);
    }

    /** Allows to specify buffer directories. Exposed for testing purposes. */
    public static final ITerminable start(final Parameters parameters,
            final IFileSysOperationsFactory factory, final LocalBufferDirs localBufferDirs)
    {
        return new DataMover(parameters, factory, localBufferDirs).start();
    }

    private DataMover(final Parameters parameters, final IFileSysOperationsFactory factory,
            final LocalBufferDirs bufferDirs)
    {
        this.parameters = parameters;
        this.factory = factory;
        this.bufferDirs = bufferDirs;
    }

    private ITerminable start()
    {
        final DataMoverProcess outgoingMovingProcess = createOutgoingMovingProcess();
        final DataMoverProcess localProcessor = createLocalProcessor();
        final DataMoverProcess incomingProcess = createIncomingMovingProcess();
        final ITerminable recoveryProcess = startupRecoveryProcess(localProcessor, incomingProcess);
        outgoingMovingProcess.startup(0L, parameters.getCheckIntervalInternalMillis());
        localProcessor.startup(parameters.getCheckIntervalInternalMillis() / 2L, parameters
                .getCheckIntervalInternalMillis());
        incomingProcess.startup(0L, parameters.getCheckIntervalMillis());
        return createCompoundTerminable(recoveryProcess, outgoingMovingProcess, localProcessor,
                incomingProcess);
    }

    private ITerminable startupRecoveryProcess(final DataMoverProcess localProcessor,
            final DataMoverProcess incomingProcessor)
    {
        final ITriggerable recoverable = new ITriggerable()
            {
                public void trigger()
                {
                    localProcessor.recover();
                    incomingProcessor.recover();
                }
            };
        // Trigger initial recovery cycle.
        recoverable.trigger();
        final TriggeringTimerTask recoveryingTimerTask =
                new TriggeringTimerTask(new File(RECOVERY_MARKER_FIILENAME), recoverable);
        final Timer recoveryTimer = new Timer("Recovery");
        recoveryTimer
                .schedule(recoveryingTimerTask, 0, parameters.getCheckIntervalInternalMillis());
        return TimerHelper.asTerminable(recoveryTimer);
    }

    private DataMoverProcess createIncomingMovingProcess()
    {
        return IncomingProcessor.createMovingProcess(parameters, factory, bufferDirs);
    }

    private final DataMoverProcess createLocalProcessor()
    {
        final LocalProcessor localProcessor =
                new LocalProcessor(parameters, bufferDirs, factory.getImmutableCopier(), factory
                        .getMover());
        final File sourceDirectory = bufferDirs.getCopyCompleteDir();
        final DirectoryScanningTimerTask localProcessingTask =
                new DirectoryScanningTimerTask(sourceDirectory, FileUtilities.ACCEPT_ALL_FILTER,
                        localProcessor);
        return new DataMoverProcess(localProcessingTask, "Local Processor", localProcessor);
    }

    private final DataMoverProcess createOutgoingMovingProcess()
    {
        final IFileStore outgoingStore = parameters.getOutgoingStore(factory);
        final File sourceDirectory = bufferDirs.getReadyToMoveDir();
        final IFileStore readyToMoveStore =
                FileStoreFactory.createLocal(sourceDirectory, "ready-to-move", factory);
        final IStoreHandler remoteStoreMover =
                createRemotePathMover(readyToMoveStore, outgoingStore);
        final HighwaterMarkDirectoryScanningHandler directoryScanningHandler =
                new HighwaterMarkDirectoryScanningHandler(new FaultyPathDirectoryScanningHandler(
                        sourceDirectory), outgoingStore.getHighwaterMarkWatcher());
        final DirectoryScanningTimerTask outgoingMovingTask =
                new DirectoryScanningTimerTask(sourceDirectory, FileUtilities.ACCEPT_ALL_FILTER,
                        remoteStoreMover, directoryScanningHandler);
        return new DataMoverProcess(outgoingMovingTask, "Final Destination Mover");
    }

    private IStoreHandler createRemotePathMover(final IFileStore source,
            final IFileStore destination)
    {
        return RemoteMonitoredMoverFactory.create(source, destination, parameters);
    }

    private static ITerminable createCompoundTerminable(final ITerminable... terminables)
    {
        return new ITerminable()
            {
                public boolean terminate()
                {
                    boolean ok = true;
                    for (final ITerminable terminable : terminables)
                    {
                        ok = ok && terminable.terminate();
                    }
                    return ok;
                }
            };
    }
}
