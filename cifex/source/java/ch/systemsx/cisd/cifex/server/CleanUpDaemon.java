/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A <code>TimerTask</code> extension which deletes expired files and users.
 * <p>
 * It gets registered via <i>Spring</i> framework.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
public final class CleanUpDaemon extends TimerTask
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CleanUpDaemon.class);

    private final Stopwatch timer = new Stopwatch();

    private final IDomainModel domainModel;

    private Date dayLastRun;

    public CleanUpDaemon(final IDomainModel domainModel)
    {
        this.domainModel = domainModel;
    }

    private final void deleteExpiredFiles()
    {
        domainModel.getFileManager().deleteExpiredFiles(
                domainModel.getBusinessContext().getUserActionLogHttp());
    }

    private final void deleteExpiredUsers()
    {
        domainModel.getUserManager().deleteExpiredUsers(
                domainModel.getBusinessContext().getUserActionLogHttp());
    }

    //
    // TimerTask
    //

    @Override
    public final void run()
    {
        // All expiration dates are in the last milli-second of the old day. Thus we need to do the
        // expiration check only every day.
        final Date today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        if (today.equals(dayLastRun))
        {
            return;
        }
        dayLastRun = today;
        try
        {
            timer.start();
            deleteExpiredUsers();
            deleteExpiredFiles();
            timer.stop();
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Cleaning time: " + timer.getTimeElapsed() + " ms");
            }
        } catch (Throwable th)
        {
            operationLog.error("Unexpected exception or error, thread is still running.", th);
        }
    }

    //
    // Helper classes
    //

    private final static class Stopwatch
    {
        private long start = 0;

        private long stop = 0;

        void start()
        {
            start = System.currentTimeMillis();
        }

        void stop()
        {
            stop = System.currentTimeMillis();
        }

        long getTimeElapsed()
        {
            return stop - start;

        }
    }

}
