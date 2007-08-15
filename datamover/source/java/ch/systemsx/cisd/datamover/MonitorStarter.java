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
import java.io.FileFilter;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.utilities.IntraFSPathMover;
import ch.systemsx.cisd.common.utilities.NamePrefixFileFilter;
import ch.systemsx.cisd.common.utilities.RegexFileFilter;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IPathHandler;
import ch.systemsx.cisd.common.utilities.RegexFileFilter.PathType;

/**
 * A class that starts up the monitoring processes, based on the parameters provided.
 * 
 * @author Bernd Rinn
 */
public class MonitorStarter
{
    private final static String LOCAL_IN_PROGRESS_DIR = "in-progress";

    private final static String LOCAL_READY_TO_MOVE_DIR = "ready-to-move";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, RemotePathMover.class);

    private final Parameters parameters;

    private final IFileSysOperationsFactory operations;

    public MonitorStarter(Parameters parameters, IFileSysOperationsFactory operations)
    {
        this.parameters = parameters;
        this.operations = operations;
    }

    public void start()
    {
        File buffer = parameters.getBufferStore().getPath();
        // here data are copied from incoming
        File inProgressDir = ensureDirectoryExists(buffer, LOCAL_IN_PROGRESS_DIR);
        // from here data are moved to outgoing directory
        File readyToMoveDir = ensureDirectoryExists(buffer, LOCAL_READY_TO_MOVE_DIR);

        startupIncomingMovingProcess(parameters.getIncomingStore(), inProgressDir, readyToMoveDir);
        startupOutgoingMovingProcess(readyToMoveDir, parameters.getOutgoingStore());
    }

    private void startupIncomingMovingProcess(FileStore incomingStore, File inProgressDir, File readyToMoveDir)
    {

        final File manualInterventionDir = parameters.getManualInterventionDirectory();
        final RegexFileFilter cleansingFilter = new RegexFileFilter();
        if (parameters.getCleansingRegex() != null)
        {
            cleansingFilter.add(PathType.FILE, parameters.getCleansingRegex());
        }
        final RegexFileFilter manualInterventionFilter = new RegexFileFilter();
        if (parameters.getManualInterventionRegex() != null)
        {
            manualInterventionFilter.add(PathType.ALL, parameters.getManualInterventionRegex());
        }
        IPathHandler pathHandler =
                createIncomingMovingPathHandler(incomingStore.getHost(), inProgressDir, readyToMoveDir,
                        manualInterventionDir, manualInterventionFilter, cleansingFilter);

        final DirectoryScanningTimerTask movingTask =
                new DirectoryScanningTimerTask(incomingStore.getPath(), new QuietPeriodFileFilter(parameters,
                        operations), pathHandler);
        final Timer movingTimer = new Timer("Mover of Incomming Data");
        schedule(movingTimer, movingTask, 0, parameters.getCheckIntervalMillis(), parameters.getTreatIncomingAsRemote());
    }

    private IPathHandler createIncomingMovingPathHandler(String sourceHost, File inProgressDir, File readyToMoveDir,
            File manualInterventionDir, RegexFileFilter manualInterventionFilter, RegexFileFilter cleansingFilter)
    {
        IPathHandler moveFromIncoming = createPathMoverToLocal(sourceHost, inProgressDir);
        IPathHandler processMoved = createProcessMovedFile(readyToMoveDir);
        IPathHandler moveAndProcess = createMoveAndProcess(moveFromIncoming, inProgressDir, processMoved);
        IPathHandler manualInterventionMover = createPathMoverToLocal(sourceHost, manualInterventionDir);
        CleansingPathHandlerDecorator cleansingOrMover =
                new CleansingPathHandlerDecorator(cleansingFilter, moveAndProcess);
        return new GatePathHandlerDecorator(manualInterventionFilter, cleansingOrMover, manualInterventionMover);
    }

    private static IPathHandler createProcessMovedFile(File destDirectory)
    {
        FileFilter cleanMarkers = new NamePrefixFileFilter(Constants.IS_FINISHED_PREFIX, true);
        // TODO [2007-08-13 tpylak] add possibility to make hard-link copy for images analysis
        IPathHandler moveToDone = new IntraFSPathMover(destDirectory);
        return new CleansingPathHandlerDecorator(cleanMarkers, moveToDone);
    }

    private static IPathHandler createMoveAndProcess(final IPathHandler moveFromIncoming, final File destinationDir,
            final IPathHandler processMovedFile)
    {
        return new IPathHandler()
            {
                public boolean handle(File path)
                {
                    boolean ok = moveFromIncoming.handle(path);
                    if (ok)
                    {
                        // create path in destination directory
                        File movedFile = new File(destinationDir, path.getName());
                        File markFile = new File(destinationDir, Constants.IS_FINISHED_PREFIX + path.getName());
                        assert movedFile.exists();
                        assert markFile.exists();
                        operationLog.info(String.format("Processing moved file locally %s\n.", movedFile.getAbsoluteFile()));
                        markFile.delete(); // process even if mark file could not be deleted
                        ok = processMovedFile.handle(movedFile);
                    }
                    return ok;
                }
            };
    }

    private IPathHandler createPathMoverToLocal(String sourceHost, final File localDestDir)
    {
        if (parameters.getTreatIncomingAsRemote())
        {
            return createRemotePathMover(sourceHost, localDestDir, /* local host */null);
        } else
        {
            return new IntraFSPathMover(localDestDir);
        }
    }

    private IPathHandler createRemotePathMover(String sourceHost, File destinationDirectory, String destinationHost)
    {
        IPathCopier copier = operations.getCopier(destinationDirectory);
        CopyActivityMonitor monitor = new CopyActivityMonitor(destinationDirectory, operations, copier, parameters);
        IPathRemover remover = operations.getRemover();
        return new RemotePathMover(destinationDirectory, destinationHost, monitor, remover, copier, sourceHost,
                parameters);
    }

    private void startupOutgoingMovingProcess(File srcDir, FileStore destDir)
    {
        final File outgoingDirectory = destDir.getPath();
        final String outgoingHost = destDir.getHost();
        final IPathHandler remoteMover = createRemotePathMover(null, outgoingDirectory, outgoingHost);
        final DirectoryScanningTimerTask remoteMovingTask =
                new DirectoryScanningTimerTask(srcDir, new NamePrefixFileFilter(Constants.IS_FINISHED_PREFIX, false),
                        remoteMover);
        final Timer remoteMovingTimer = new Timer("Remote Mover");

        // Implementation notes:
        // The startup of the remote moving task is delayed for half the time of the check interval. Thus the
        // incoming
        // moving task should have enough time to finish its job.
        schedule(remoteMovingTimer, remoteMovingTask, parameters.getCheckIntervalMillis() / 2, parameters
                .getCheckIntervalMillis(), true);
    }

    private void schedule(Timer timer, TimerTask task, long delay, long period, boolean isRemote)
    {
        // The remote moving task is scheduled at fixed rate. The rationale behind this is that if new items are
        // added to the source directory while the remote timer task has been running for a long time, busy moving data
        // to or from
        // remote, the task shoulnd't sit idle for the check time when there is actually work to do.
        if (isRemote)
        {
            timer.scheduleAtFixedRate(task, delay, period);
        } else
        {
            timer.schedule(task, delay, period);
        }
    }

    private static File ensureDirectoryExists(File dir, String newDirName)
    {
        File dataDir = new File(dir, newDirName);
        if (!dataDir.exists())
        {
            if (!dataDir.mkdir())
                throw new EnvironmentFailureException("Could not create local data directory " + dataDir);
        }
        return dataDir;
    }
}
