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
     * Starts up the process with a the given <var>delay</var> and <var>period</var> in milli seconds.
     */
    public void startup(long delay, long period)
    {
        // The moving task is scheduled at fixed rate. It makes sense especially if the task is moving data from the
        // remote share. The rationale behind this is that if new items are
        // added to the source directory while the incoming timer task has been running for a long time, busy moving
        // data, the task shouldn't sit idle for the check time when there is actually work to do.
        timer.scheduleAtFixedRate(dataMoverTimerTask, delay, period);
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