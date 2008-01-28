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
 * @author Izabela Adamczyk
 */
public class CleanUpDaemon extends TimerTask
{
    IDomainModel domainModel;

    private static final Logger logger = LogFactory.getLogger(LogCategory.OPERATION, CleanUpDaemon.class);

    private final Stopwatch timer = new Stopwatch();

    @Override
    public void run()
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

    public CleanUpDaemon(final IDomainModel domainModel)
    {
        this.domainModel = domainModel;
    }

    private void deleteExpiredFiles()
    {
        domainModel.getFileManager().deleteExpiredFiles();
    }

    private void deleteExpiredUsers()
    {
        domainModel.getUserManager().deleteExpiredUsers();

    }

    private class Stopwatch
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
