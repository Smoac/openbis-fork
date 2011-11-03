package ch.systemsx.cisd.common.maintenance;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ClassUtils;

public class MaintenancePlugin
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MaintenancePlugin.class);

    private static ReentrantLock dataStoreLock = new ReentrantLock();

    private final IMaintenanceTask task;

    private final MaintenanceTaskParameters parameters;

    private final boolean requiresDataStoreLock;

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
        task.setUp(parameters.getPluginName(), parameters.getProperties());
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

    public void start()
    {
        String timerThreadName = parameters.getPluginName() + " - Maintenance Plugin";
        Timer workerTimer = new Timer(timerThreadName);

        TimerTask timerTask = new MaintenanceTimerTask();
        Date startDate = parameters.getStartDate();
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
                        + ", first execution at " + startDate);
            }
        }
    }

    private class MaintenanceTimerTask extends TimerTask
    {
        @Override
        public void run()
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