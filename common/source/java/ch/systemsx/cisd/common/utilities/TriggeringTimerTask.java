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

package ch.systemsx.cisd.common.utilities;

import java.io.File;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A role that triggers an {@link ITriggerable} when a given trigger file has been found.
 * 
 * @author Bernd Rinn
 */
public class TriggeringTimerTask extends TimerTask
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, TriggeringTimerTask.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, TriggeringTimerTask.class);

    private final ITriggerable triggerable;

    private final File triggerFile;

    private final SynchronizationMonitor monitor;

    /**
     * Creates a <var>TriggeringTimerTask</var>.
     * 
     * @param triggerFile The file that triggers.
     * @param triggerable The handler that can perform a recovery action, or <code>null</code> if
     *            there is no recovery action available.
     */
    public TriggeringTimerTask(File triggerFile, ITriggerable triggerable)
    {
        this(triggerFile, triggerable, SynchronizationMonitor.create());
    }

    /**
     * Creates a <var>TriggeringTimerTask</var>.
     * 
     * @param triggerFile The file that triggers.
     * @param triggerable The handler that can perform a recovery action, or <code>null</code> if
     *            there is no recovery action available.
     * @param monitor The monitor to synchronize on.
     */
    public TriggeringTimerTask(File triggerFile, ITriggerable triggerable,
            SynchronizationMonitor monitor)
    {
        assert triggerFile != null;
        assert triggerable != null;
        assert monitor != null;

        this.triggerFile = triggerFile;
        this.triggerable = triggerable;
        this.monitor = monitor;
    }

    /**
     * Handles all entries in the source directory that are picked by the filter.
     */
    @Override
    public synchronized void run()
    {
        synchronized (monitor)
        {
            try
            {
                if (triggerFile.exists())
                {
                    trigger();
                    triggerFile.delete();
                }
            } catch (Exception ex)
            {
                notificationLog.error("An exception has occurred. (thread still running)", ex);
            }
        }
    }

    private void trigger()
    {
        if (triggerable != null)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("File '%s' found - triggering.", triggerFile
                        .getAbsolutePath()));
            }
            triggerable.trigger();
        }

    }

}
