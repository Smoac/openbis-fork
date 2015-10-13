/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import java.lang.Thread.State;
import java.util.Collections;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.api.v3.cache.ISearchCache;
import ch.ethz.sis.openbis.generic.server.api.v3.cache.SearchCacheEntry;
import ch.ethz.sis.openbis.generic.server.api.v3.cache.SearchCacheKey;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.CacheMode;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriteria;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.logging.ConsoleLogger;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class AbstractSearchMethodExecutorTest
{

    @Test
    public void test()
    {
        final MessageChannel channel1 = new MessageChannelBuilder(1000).logger(new ConsoleLogger()).getChannel();
        final TestSearchMethodExecutor executor = new TestSearchMethodExecutor(channel1);

        Thread t1 = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    executor.search(null, new SampleSearchCriteria(), new SampleFetchOptions().cacheMode(CacheMode.CACHE));
                }
            }, "t1");
        Thread t2 = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    executor.search(null, new ExperimentSearchCriteria(), new ExperimentFetchOptions().cacheMode(CacheMode.CACHE));
                }
            }, "t2");
        executor.setBlockedThread(t2);
        t1.start();
        channel1.assertNextMessage("t1.get");
        t2.start();
        channel1.assertNextMessage("t2.get");

    }

    @SuppressWarnings("rawtypes")
    private class TestSearchMethodExecutor extends AbstractSearchMethodExecutor
    {

        private Session session;
        private Thread blockedThread;

        public TestSearchMethodExecutor(final MessageChannel channel)
        {
            this.session = new Session();
            this.cache = new ISearchCache()
                {
                    @Override
                    public SearchCacheEntry get(SearchCacheKey key)
                    {
                        String threadName = Thread.currentThread().getName();
                        channel.send(threadName + ".get");
                        if (threadName.equals("t1"))
                        {
                            long t0 = System.currentTimeMillis();
                            while (blockedThread.getState().equals(State.BLOCKED) == false)
                            {
                                if (System.currentTimeMillis() - t0 > 900)
                                {
                                    channel.send("thread t2 never blocked");
                                    break;
                                }
                            }
                        }
                        SearchCacheEntry<Object> entry = new SearchCacheEntry<>();
                        entry.setObjects(Collections.emptySet());
                        return entry;
                    }

                    @Override
                    public void put(SearchCacheKey key, SearchCacheEntry entry)
                    {
                    }

                    @Override
                    public void remove(SearchCacheKey key)
                    {
                    }
                };
        }
        
        void setBlockedThread(Thread blockedThread)
        {
            this.blockedThread = blockedThread;
            
        }

        @Override
        protected ISearchObjectExecutor getSearchExecutor()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected ITranslator getTranslator()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected Session getSession(String sessionToken)
        {
            return session;
        }

        @Override
        protected void clearCurrentSession()
        {
        }

        @Override
        protected void flushCurrentSession()
        {
        }

    }

}
