package ch.systemsx.cisd.common.maintenance;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.ClassUtils;

public class MaintenancePlugin
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MaintenancePlugin.class);

    private static ReentrantLock dataStoreLock = new ReentrantLock();

    private final IMaintenanceTask task;

    private final MaintenanceTaskParameters parameters;

    private final boolean requiresDataStoreLock;

    private Timer workerTimer;

    private volatile boolean stopped;

    public MaintenancePlugin(MaintenanceTaskParameters parameters)
    {
        this.parameters = parameters;
        try
        {
            this.task = ClassUtils.create(IMaintenanceTask.class, parameters.getClassName());
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot find the plugin class '"
                    + parameters.getClassName() + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
        // The following order is important because only after set up the task knows whether it
        // needs a lock or not.
        try
        {
            task.setUp(parameters.getPluginName(), parameters.getProperties());
        } catch (Throwable t)
        {
            throw new ConfigurationFailureException("Set up of maintenance task '" + parameters.getPluginName()
                    + "' failed: " + t.getMessage(), t);
        }
        this.requiresDataStoreLock = requiresDataStoreLock();
    }

    /**
     * Constructor that takes a configured maintenance task.
     * 
     * @param task
     */
    public MaintenancePlugin(IMaintenanceTask task, MaintenanceTaskParameters parameters)
    {
        this.parameters = parameters;
        this.task = task;
        this.requiresDataStoreLock = requiresDataStoreLock();
    }

    private boolean requiresDataStoreLock()
    {
        if (task instanceof IDataStoreLockingMaintenanceTask)
        {
            return ((IDataStoreLockingMaintenanceTask) task).requiresDataStoreLock();
        }
        return false;
    }

    public String getPluginName()
    {
        return parameters.getPluginName();
    }

    public synchronized void start()
    {
        final String timerThreadName = parameters.getPluginName() + " - Maintenance Plugin";
        workerTimer = new Timer(timerThreadName);

        final TimerTask timerTask = new MaintenanceTimerTask();
        final Date startDate = parameters.getStartDate();
        if (parameters.isExecuteOnlyOnce())
        {
            workerTimer.schedule(timerTask, startDate);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Plugin scheduled: " + parameters.getPluginName()
                        + ", single execution at " + startDate);
            }
        } else
        {
            workerTimer.schedule(timerTask, startDate, parameters.getIntervalSeconds() * 1000);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Plugin scheduled: " + parameters.getPluginName()
                        + ", first execution at " + startDate + ", scheduling interval: "
                        + parameters.getIntervalSeconds() + "s.");
            }
        }
    }

    public synchronized void execute()
    {
        final MaintenanceTimerTask timerTask = new MaintenanceTimerTask();
        timerTask.doRun();
    }

    public synchronized void shutdown()
    {
        if (workerTimer != null)
        {
            workerTimer.cancel();
            workerTimer = null;
        }
        stopped = true;
        if (task instanceof Closeable)
        {
            try
            {
                ((Closeable) task).close();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    private class MaintenanceTimerTask extends TimerTask
    {
        @Override
        public void run()
        {
            if (stopped)
            {
                return;
            }

            doRun();
        }

        private void doRun()
        {
            acquireLockIfNecessary();
            try
            {
                task.execute();
            } catch (Throwable th)
            {
                operationLog.error("Exception when running maintenance task '"
                        + task.getClass().getCanonicalName() + "'.", th);
            } finally
            {
                releaseLockIfNecessay();
            }
        }

        private void acquireLockIfNecessary()
        {
            if (requiresDataStoreLock)
            {
                dataStoreLock.lock();
            }
        }

        private void releaseLockIfNecessay()
        {
            if (requiresDataStoreLock)
            {
                dataStoreLock.unlock();
            }
        }
    }

}