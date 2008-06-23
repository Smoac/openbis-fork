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

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.concurrent.TimerUtilities;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.ITriggerable;
import ch.systemsx.cisd.datamover.filesystem.intf.IRecoverableTimerTaskFactory;

/**
 * A class that represents the incoming moving process.
 * 
 * @author Bernd Rinn
 */
public final class DataMoverProcess implements ITerminable, ITriggerable
{
    private final Timer timer;

    private final TimerTask dataMoverTimerTask;

    private final IRecoverableTimerTaskFactory recoverableTimerTaskFactory;

    DataMoverProcess(final TimerTask timerTask, final String taskName)
    {
        this(timerTask, taskName, null);
    }

    DataMoverProcess(final TimerTask dataMoverTimerTask, final String taskName,
            final IRecoverableTimerTaskFactory recoverableTimerTaskFactory)
    {
        this.dataMoverTimerTask = dataMoverTimerTask;
        this.recoverableTimerTaskFactory = recoverableTimerTaskFactory;
        this.timer = new Timer(taskName);
    }

    @Private
    TimerTask getDataMoverTimerTask()
    {
        return dataMoverTimerTask;
    }

    /**
     * Starts up the process with a the given <var>delay</var> and <var>period</var> in
     * milliseconds.
     */
    public final void startup(final long delay, final long period)
    {
        timer.schedule(dataMoverTimerTask, delay, period);
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

    public final boolean terminate()
    {
        dataMoverTimerTask.cancel();
        timer.cancel();
        TimerUtilities.tryJoinTimerThread(timer, Long.MAX_VALUE);
        return true;
    }

    //
    // ITriggerable
    //

    public final void trigger()
    {
        recover();
    }
}