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

import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.TimerHelper;
import ch.systemsx.cisd.datamover.filesystem.intf.IRecoverableTimerTaskFactory;

/**
 * A class that represents the incoming moving process.
 */
public class DataMoverProcess implements ITerminable
{
    private final Timer timer;

    private final TimerTask dataMoverTimerTask;

    private final ITerminable terminable;

    private final IRecoverableTimerTaskFactory recoverableTimerTaskFactory;

    DataMoverProcess(TimerTask timerTask, String taskName)
    {
        this(timerTask, taskName, null);
    }

    DataMoverProcess(TimerTask dataMoverTimerTask, String taskName,
            IRecoverableTimerTaskFactory recoverableTimerTaskFactory)
    {
        this.dataMoverTimerTask = dataMoverTimerTask;
        this.recoverableTimerTaskFactory = recoverableTimerTaskFactory;
        this.timer = new Timer(taskName);
        this.terminable = TimerHelper.asTerminable(timer);
    }

    /**
     * Starts up the process with a the given <var>delay</var> and <var>period</var> in milli
     * seconds.
     */
    public void startup(long delay, long period)
    {
        timer.schedule(dataMoverTimerTask, delay, period);
    }

    public boolean terminate()
    {
        return terminable.terminate();
    }

    public void recover()
    {
        if (recoverableTimerTaskFactory != null)
        {
            timer.schedule(recoverableTimerTaskFactory.createRecoverableTimerTask(), 0);
        }
    }

}