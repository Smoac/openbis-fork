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
    
    private final Parameters parameters;
    
    private final IFileSystemOperations operations;
    
    public MonitorStarter(Parameters parameters, IFileSystemOperations operations)
    {
        this.parameters = parameters;
        this.operations = operations;
    }
    
    public void start()
    {
        startupIncomingMovingProcess();
        startupOutgoingMovingProcess();
    }

    private void startupIncomingMovingProcess()
    {
        final File incomingDirectory = parameters.getIncomingStore().getPath();
        final File bufferDirectory = parameters.getBufferStore().getPath();
        final File manualInterventionDirectory = parameters.getManualInterventionDirectory();
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
        final IPathHandler localPathMover =
                new GatePathHandlerDecorator(manualInterventionFilter, new CleansingPathHandlerDecorator(
                        cleansingFilter, new IntraFSPathMover(bufferDirectory)), new IntraFSPathMover(
                        manualInterventionDirectory));
        final DirectoryScanningTimerTask localMovingTask =
                new DirectoryScanningTimerTask(incomingDirectory, new QuietPeriodFileFilter(parameters, operations),
                        localPathMover);
        final Timer localMovingTimer = new Timer("Local Mover");
        localMovingTimer.schedule(localMovingTask, 0, parameters.getCheckIntervalMillis());

    }

    private void startupOutgoingMovingProcess()
    {
        final File bufferDirectory = parameters.getBufferStore().getPath();
        final File outgoingDirectory = parameters.getOutgoingStore().getPath();
        final String outgoingHost = parameters.getOutgoingStore().getHost();
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(outgoingDirectory, operations, operations.getCopier(), parameters);
        final IPathHandler remoteMover =
                new RemotePathMover(outgoingDirectory, outgoingHost, monitor, operations, parameters);
        final DirectoryScanningTimerTask remoteMovingTask =
                new DirectoryScanningTimerTask(bufferDirectory, new NamePrefixFileFilter(Constants.IS_FINISHED_PREFIX,
                        false), remoteMover);
        final Timer remoteMovingTimer = new Timer("Remote Mover");

        // Implementation notes:
        // 1. The startup of the remote moving task is delayed for half the time of the check interval. Thus the local
        // moving task should have enough time to finish its job.
        // 2. The remote moving task is scheduled at fixed rate. The rationale behind this is that if new items are
        // added
        // to the local temp directory while the remote timer task has been running for a long time, busy moving data to
        // remote, the task shoulnd't sit idle for the check time when there is actually work to do.
        remoteMovingTimer.scheduleAtFixedRate(remoteMovingTask, parameters.getCheckIntervalMillis() / 2, parameters
                .getCheckIntervalMillis());
    }

}
