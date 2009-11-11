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
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.concurrent.TimerTaskWithListeners;
import ch.systemsx.cisd.common.filesystem.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.filesystem.FaultyPathDirectoryScanningHandler;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IStoreHandler;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkDirectoryScanningHandler;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.utilities.CompoundTriggerable;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.TriggeringTimerTask;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.RemoteMonitoredMoverFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IRecoverableTimerTaskFactory;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * A class that starts up the processing pipeline and its monitoring, based on the parameters
 * provided.
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak
 */
public final class DataMover
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DataMover.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, DataMover.class);

    private final static String LOCAL_COPY_IN_PROGRESS_DIR = "copy-in-progress";

    private final static String LOCAL_COPY_COMPLETE_DIR = "copy-complete";

    private final static String LOCAL_READY_TO_MOVE_DIR = "ready-to-move";

    private final static String LOCAL_TEMP_DIR = "tmp";

    @Private
    static final String OUTGOING_TARGET_LOCATION_FILE = ".outgoing_target_location";

    @Private
    static final String RECOVERY_MARKER_FIILENAME = Constants.MARKER_PREFIX + "recovery";

    @Private
    static final String PROCESS_MARKER_PREFIX = Constants.MARKER_PREFIX + "thread_";

    private static final String PROCESSING_MARKER_TEMPLATE =
            PROCESS_MARKER_PREFIX + "%s_processing";

    private static final String ERROR_MARKER_TEMPLATE = PROCESS_MARKER_PREFIX + "%s_error";

    @Private
    static final String INCOMING_PROCESS_MARKER_FILENAME =
            String.format(PROCESSING_MARKER_TEMPLATE, "incoming");

    @Private
    static final String OUTGOING_PROCESS_MARKER_FILENAME =
            String.format(PROCESSING_MARKER_TEMPLATE, "outgoing");

    @Private
    static final String LOCAL_PROCESS_MARKER_FILENAME =
            String.format(PROCESSING_MARKER_TEMPLATE, "local");

    @Private
    static final String INCOMING_ERROR_MARKER_FILENAME =
            String.format(ERROR_MARKER_TEMPLATE, "incoming");

    @Private
    static final String OUTGOING_ERROR_MARKER_FILENAME =
            String.format(ERROR_MARKER_TEMPLATE, "outgoing");

    @Private
    static final String LOCAL_ERROR_MARKER_FILENAME = String.format(ERROR_MARKER_TEMPLATE, "local");

    @Private
    static final String RECOVERY_PROCESS_MARKER_FILENAME =
            String.format(PROCESSING_MARKER_TEMPLATE, "recovery");

    /**
     * This marker file indicates that we are in a <i>shutdown</i> mode, started by the program.
     */
    static final String SHUTDOWN_PROCESS_MARKER_FILENAME =
            String.format(PROCESSING_MARKER_TEMPLATE, "shutdown");

    private static final String[] PROCESS_MARKER_FILENAMES =
                { INCOMING_PROCESS_MARKER_FILENAME, OUTGOING_PROCESS_MARKER_FILENAME,
                        LOCAL_PROCESS_MARKER_FILENAME, INCOMING_ERROR_MARKER_FILENAME,
                        OUTGOING_ERROR_MARKER_FILENAME, LOCAL_ERROR_MARKER_FILENAME,
                        RECOVERY_PROCESS_MARKER_FILENAME, SHUTDOWN_PROCESS_MARKER_FILENAME };

    private final Parameters parameters;

    private final IFileSysOperationsFactory factory;

    private final LocalBufferDirs bufferDirs;

    /**
     * Indicates that a <i>shutdown</i> should be performed by the program (has been asked by
     * <code>datamover.sh</code>).
     */
    public static final String SHUTDOWN_MARKER_FILENAME = Constants.MARKER_PREFIX + "shutdown";

    /**
     * starts the process of moving data and monitoring it
     * 
     * @return object which can be used to terminate the process and all its threads
     */
    static final ITerminable start(final Parameters parameters,
            final IFileSysOperationsFactory factory)
    {
        return start(parameters, factory, createLocalBufferDirs(parameters));
    }

    static TimerTask createTimerTaskForMarkerFileProtocol(final TimerTask timerTask,
            final String markerFileName, final String errorFileNameOrNull,
            final String successorMarkerFileNameOrNull)
    {
        final TimerTaskWithListeners timerTaskWithListeners = new TimerTaskWithListeners(timerTask);
        timerTaskWithListeners.addListener(new TimerTaskListenerForMarkerFileProtocol(
                markerFileName, errorFileNameOrNull, successorMarkerFileNameOrNull));
        return timerTaskWithListeners;
    }

    private static TimerTask createTimerTaskForMarkerFileProtocol(final TimerTask timerTask,
            final String markerFileName, final String errorMarkerFileName)
    {
        return createTimerTaskForMarkerFileProtocol(timerTask, markerFileName, errorMarkerFileName,
                null);
    }

    private static TimerTask createTimerTaskForMarkerFileProtocol(final TimerTask timerTask,
            final String markerFileName)
    {
        return createTimerTaskForMarkerFileProtocol(timerTask, markerFileName, null, null);
    }

    private static LocalBufferDirs createLocalBufferDirs(final Parameters parameters)
    {
        return new LocalBufferDirs(parameters.getBufferDirectoryPath(), LOCAL_COPY_IN_PROGRESS_DIR,
                LOCAL_COPY_COMPLETE_DIR, LOCAL_READY_TO_MOVE_DIR, LOCAL_TEMP_DIR);
    }

    /** Allows to specify buffer directories. Exposed for testing purposes. */
    static final ITerminable start(final Parameters parameters,
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

    private final ITerminable start()
    {
        cleanUpProcessMarkerFiles();
        final DataMoverProcess outgoingProcess = createAndStartOutgoingProcess();
        final DataMoverProcess localProcess = createLocalProcess();
        final DataMoverProcess incomingProcess = createIncomingProcess();
        final DataMoverProcess recoveryProcess =
                createAndStartRecoveryProcess(localProcess, incomingProcess);
        // We start the incoming and local processes after the recovery one.
        final long checkIntervalInternalMillis = parameters.getCheckIntervalInternalMillis();
        localProcess.startup(checkIntervalInternalMillis / 2L, checkIntervalInternalMillis);
        incomingProcess.startup(0L, parameters.getCheckIntervalMillis());
        // The ITerminable order here is important.
        return new DataMoverTerminable(recoveryProcess, incomingProcess, localProcess,
                outgoingProcess);
    }

    private final static void cleanUpProcessMarkerFiles()
    {
        for (String fileName : PROCESS_MARKER_FILENAMES)
        {
            DataMoverShutdownHook.deleteFile(new File(fileName), "marker");
        }
    }

    private final DataMoverProcess createAndStartRecoveryProcess(
            final DataMoverProcess localProcessor, final DataMoverProcess incomingProcessor)
    {
        final CompoundTriggerable triggerable =
                new CompoundTriggerable(localProcessor, incomingProcessor);
        // Trigger initial recovery cycle.
        triggerable.trigger();
        final TriggeringTimerTask recoveryTimerTask =
                new TriggeringTimerTask(new File(RECOVERY_MARKER_FIILENAME), triggerable);
        final TimerTask timerTask =
                createTimerTaskForMarkerFileProtocol(recoveryTimerTask,
                        RECOVERY_PROCESS_MARKER_FILENAME);
        final DataMoverProcess recoveryProcess = new DataMoverProcess(timerTask, "Recovery");
        recoveryProcess.startup(0, parameters.getCheckIntervalInternalMillis());
        return recoveryProcess;
    }

    private final DataMoverProcess createIncomingProcess()
    {
        return IncomingProcessor.createMovingProcess(parameters, INCOMING_PROCESS_MARKER_FILENAME,
                INCOMING_ERROR_MARKER_FILENAME, LOCAL_PROCESS_MARKER_FILENAME, factory, bufferDirs);
    }

    private final DataMoverProcess createLocalProcess()
    {
        final LocalProcessor localProcessor =
                new LocalProcessor(parameters, bufferDirs, factory.getImmutableCopier(), factory
                        .getMover());
        final File sourceDirectory = bufferDirs.getCopyCompleteDir();
        final DirectoryScanningTimerTask localProcessingTask =
                new DirectoryScanningTimerTask(sourceDirectory, FileUtilities.ACCEPT_ALL_FILTER,
                        localProcessor);
        final TimerTask timerTask =
                createTimerTaskForMarkerFileProtocol(localProcessingTask,
                        LOCAL_PROCESS_MARKER_FILENAME, LOCAL_ERROR_MARKER_FILENAME,
                        OUTGOING_PROCESS_MARKER_FILENAME);
        final DataMoverProcess dataMoverProcess =
                new RunOnceMoreAfterTerminateDataMoverProcess(timerTask, "Local Processor",
                        localProcessor);
        return dataMoverProcess;
    }

    private final DataMoverProcess createAndStartOutgoingProcess()
    {
        final IFileStore outgoingStore = parameters.getOutgoingStore(factory);
        final File sourceDirectory = bufferDirs.getReadyToMoveDir();
        final IFileStore readyToMoveStore =
                FileStoreFactory.createLocal(sourceDirectory, "ready-to-move", factory, false);
        final IStoreHandler remoteStoreMover =
                createOutgoingPathMover(readyToMoveStore, outgoingStore);
        final HighwaterMarkDirectoryScanningHandler directoryScanningHandler =
                new HighwaterMarkDirectoryScanningHandler(new FaultyPathDirectoryScanningHandler(
                        sourceDirectory, remoteStoreMover), outgoingStore.getHighwaterMarkWatcher());
        final DirectoryScanningTimerTask outgoingMovingTask =
                new DirectoryScanningTimerTask(sourceDirectory, FileUtilities.ACCEPT_ALL_FILTER,
                        remoteStoreMover, directoryScanningHandler);
        final TimerTask timerTask =
                createTimerTaskForMarkerFileProtocol(outgoingMovingTask,
                        OUTGOING_PROCESS_MARKER_FILENAME, OUTGOING_ERROR_MARKER_FILENAME);
        final DataMoverProcess outgoingProcess =
                new RunOnceMoreAfterTerminateDataMoverProcess(timerTask, "Final Destination Mover");
        outgoingProcess.startup(0L, parameters.getCheckIntervalInternalMillis());
        return outgoingProcess;

    }

    private final IStoreHandler createOutgoingPathMover(final IFileStore source,
            final IFileStore destination)
    {
        final IStoreHandler moveHandler =
                RemoteMonitoredMoverFactory.create(source, destination, parameters);
        final String transferFinishedExecutable = parameters.getTransferFinishedExecutable();
        if (transferFinishedExecutable == null)
        {
            return moveHandler;
        } else
        {
            // calls a specified script when the transfer has been finished
            return new IStoreHandler()
                {

                    public void handle(StoreItem item)
                    {
                        moveHandler.handle(item);
                        callScript(transferFinishedExecutable, item);
                    }

                    public boolean isStopped()
                    {
                        return moveHandler.isStopped();
                    }
                };
        }
    }

    private void callScript(String scriptExecutable, StoreItem item)
    {
        List<String> cmd = Arrays.asList(scriptExecutable, item.getName());
        boolean ok = ProcessExecutionHelper.runAndLog(cmd, operationLog, machineLog);
        if (ok == false)
        {
            operationLog.error(String.format("The script '%s' could not be launched or finished"
                    + " with an error for the item '%s'.", scriptExecutable, item.getName()));
        }
    }

    //
    // Helper classes
    //

    private final static class RunOnceMoreAfterTerminateDataMoverProcess extends DataMoverProcess
    {
        RunOnceMoreAfterTerminateDataMoverProcess(final TimerTask timerTask, final String taskName,
                final IRecoverableTimerTaskFactory recoverableTimerTaskFactory)
        {
            super(timerTask, taskName, recoverableTimerTaskFactory);
        }

        RunOnceMoreAfterTerminateDataMoverProcess(final TimerTask timerTask, final String taskName)
        {
            super(timerTask, taskName);
        }

        //
        // DataMoverProcess
        //

        @Override
        public final boolean terminate()
        {
            final boolean terminated = super.terminate();
            getTimerTask().run();
            return terminated;
        }
    }
}
