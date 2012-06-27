/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.conversation;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.systemsx.cisd.common.serviceconversation.server.ProgressInfo;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;

/**
 * Sends incoming progress updates to the remote client through the ServiceConversationServer. The
 * maximum reporting rate is limited by the value given as an argument. If a progress update is
 * received while the previous update is still waiting to be sent (because of the maximum reporting
 * rate), the previous update will be discarded.
 * 
 * @author anttil
 */
public class RateLimitedProgressListener implements IProgressListener
{

    private ServiceConversationServer server;

    private String conversationId;

    private ScheduledThreadPoolExecutor executor;

    private Update lastUpdate;

    private int interval;

    private ScheduledFuture<?> future;

    public RateLimitedProgressListener(ServiceConversationServer server, String conversationId,
            int reportingInterval)
    {
        this.server = server;
        this.conversationId = conversationId;
        this.interval = reportingInterval;
        this.executor = new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public synchronized void update(final String label, final int totalItemsToProcess,
            final int numItemsProcessed)
    {
        if (future != null)
        {
            future.cancel(false);
        }

        long lastExecution = 0;
        if (this.lastUpdate != null)
        {
            lastExecution = this.lastUpdate.getLastExecution();
        }

        Update update =
                new Update(this.server, this.conversationId, new ProgressInfo(label,
                        totalItemsToProcess, numItemsProcessed), lastExecution);

        if (System.currentTimeMillis() - lastExecution > this.interval)
        {
            this.executor.execute(update);
            this.future = null;
        } else
        {
            future =
                    this.executor.schedule(update, lastExecution + this.interval,
                            TimeUnit.MILLISECONDS);
        }

        this.lastUpdate = update;
    }

    public synchronized void close()
    {
        if (future != null)
        {
            future.cancel(false);
        }
        this.executor.shutdown();
    }

    private static class Update implements Runnable
    {

        private ServiceConversationServer server;

        private String conversationId;

        private ProgressInfo progress;

        private long lastExecution;

        public Update(ServiceConversationServer server, String conversationId,
                ProgressInfo progress, long lastExecution)
        {
            this.server = server;
            this.conversationId = conversationId;
            this.progress = progress;
            this.lastExecution = lastExecution;
        }

        @Override
        public void run()
        {
            this.lastExecution = System.currentTimeMillis();
            server.reportProgress(conversationId, progress);
        }

        public long getLastExecution()
        {
            return this.lastExecution;
        }
    }
}
