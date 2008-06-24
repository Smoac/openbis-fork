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

import java.util.Timer;
import java.util.TimerTask;

import ch.systemsx.cisd.common.concurrent.TimerUtilities;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.ITriggerable;
import ch.systemsx.cisd.datamover.filesystem.intf.IRecoverableTimerTaskFactory;

/**
 * A class that represents the incoming moving process.
 * 
 * @author Bernd Rinn
 */
class DataMoverProcess implements ITerminable, ITriggerable
{
    private final Timer timer;

    private final TimerTask timerTask;

    private final IRecoverableTimerTaskFactory recoverableTimerTaskFactory;

    private final String taskName;

    DataMoverProcess(final TimerTask timerTask, final String taskName)
    {
        this(timerTask, taskName, null);
    }

    DataMoverProcess(final TimerTask timerTask, final String taskName,
            final IRecoverableTimerTaskFactory recoverableTimerTaskFactory)
    {
        this.timerTask = timerTask;
        this.recoverableTimerTaskFactory = recoverableTimerTaskFactory;
        this.timer = new Timer(taskName);
        this.taskName = taskName;
    }

    final TimerTask getTimerTask()
    {
        return timerTask;
    }

    final String getTaskName()
    {
        return taskName;
    }

    /**
     * Starts up the process with a the given <var>delay</var> and <var>period</var> in
     * milliseconds.
     */
    public final void startup(final long delay, final long period)
    {
        timer.schedule(timerTask, delay, period);
    }

    /**
     * Schedules the specified <code>recoverableTimerTaskFactory</code>.
     */
    private final void recover()
    {
        if (recoverableTimerTaskFactory != null)
        {
            timer.schedule(recoverableTimerTaskFactory.createRecoverableTimerTask(), 0);
        }
    }

    //
    // ITerminable
    //

    public boolean terminate()
    {
        timerTask.cancel();
        timer.cancel();
        return TimerUtilities.tryJoinTimerThread(timer, Long.MAX_VALUE);
    }

    //
    // ITriggerable
    //

    public final void trigger()
    {
        recover();
    }
}