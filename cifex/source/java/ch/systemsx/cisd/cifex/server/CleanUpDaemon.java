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

import java.util.TimerTask;

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
    private static final Logger logger = LogFactory.getLogger(LogCategory.OPERATION, CleanUpDaemon.class);

    private final Stopwatch timer = new Stopwatch();

    final IDomainModel domainModel;

    public CleanUpDaemon(final IDomainModel domainModel)
    {
        this.domainModel = domainModel;
    }

    private final void deleteExpiredFiles()
    {
        domainModel.getFileManager().deleteExpiredFiles();
    }

    private final void deleteExpiredUsers()
    {
        domainModel.getUserManager().deleteExpiredUsers();
    }

    //
    // TimerTask
    //

    @Override
    public final void run()
    {
        timer.start();
        deleteExpiredUsers();
        deleteExpiredFiles();
        timer.stop();
        if (logger.isInfoEnabled())
        {
            logger.info("Cleaning time: " + timer.getTimeElapsed() + " ms");
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
