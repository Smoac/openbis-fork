package ch.systemsx.cisd.common.maintenance;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.ClassUtils;

public class MaintenancePlugin
{
    private static final Map<String /* resource name */, Lock> resourceLocks =
            new ConcurrentHashMap<String /* resource name */, Lock>();

    private final IMaintenanceTask task;

    private final MaintenanceTaskParameters parameters;

    private final String requiredResourceLockName;

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
        task.setUp(parameters.getPluginName(), parameters.getProperties());

        if (task instanceof IResourceContendingMaintenanceTask)
        {
            requiredResourceLockName =
                    ((IResourceContendingMaintenanceTask) task).getRequiredResourceLock();
        } else
        {
            requiredResourceLockName = null;
        }
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
        } else
        {
            workerTimer.schedule(timerTask, startDate, parameters.getIntervalSeconds() * 1000);
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
            } finally
            {
                releaseLockIfNecessay();
            }

        }

        private void acquireLockIfNecessary()
        {
            if (requiredResourceLockName != null)
            {
                getLock(requiredResourceLockName).lock();
            }
        }

        private void releaseLockIfNecessay()
        {
            if (requiredResourceLockName != null)
            {
                getLock(requiredResourceLockName).unlock();
            }
        }

        private Lock getLock(String resourceName)
        {
            synchronized (resourceLocks)
            {
                Lock lock = resourceLocks.get(resourceName);
                if (lock == null)
                {
                    lock = new ReentrantLock();
                    resourceLocks.put(resourceName, lock);
                }
                return lock;
            }
        }
    }

}