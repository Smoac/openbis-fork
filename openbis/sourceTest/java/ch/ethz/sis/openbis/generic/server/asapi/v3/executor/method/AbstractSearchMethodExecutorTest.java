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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import java.lang.Thread.State;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.CacheMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.ISearchCache;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.SearchCacheEntry;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.SearchCacheKey;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.logging.ConsoleLogger;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class AbstractSearchMethodExecutorTest
{
    private static final String SESSION_TOKEN1 = "sessionToken1";

    private static final String SESSION_TOKEN2 = "sessionToken2";

    private MessageChannel channel;

    private TestSearchMethodExecutor executor;

    @BeforeMethod
    public void setUpExecutor()
    {
        LogInitializer.init();
        channel = new MessageChannelBuilder(1000).logger(new ConsoleLogger()).getChannel();
        executor = new TestSearchMethodExecutor();
        executor.addSession(SESSION_TOKEN1, createSession("user1"));
        executor.addSession(SESSION_TOKEN2, createSession("user2"));
    }

    @Test
    public void testThatGetCacheEntryIsBlockedForSecondThreadIfSameSession()
    {
        Thread t1 = new Thread(new Runnable()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void run()
                {
                    FetchOptions<Sample> fetchOptions = new SampleFetchOptions().cacheMode(CacheMode.CACHE);
                    executor.search(SESSION_TOKEN1, new SampleSearchCriteria(), fetchOptions);
                }
            }, "t1");
        final Thread t2 = new Thread(new Runnable()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void run()
                {
                    FetchOptions<Experiment> fetchOptions = new ExperimentFetchOptions().cacheMode(CacheMode.CACHE);
                    executor.search(SESSION_TOKEN1, new ExperimentSearchCriteria(), fetchOptions);
                }
            }, "t2");

        executor.setGetAction(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String threadName = Thread.currentThread().getName();
                    channel.send(threadName + ".get started");
                    if (threadName.equals("t1"))
                    {
                        waitUntilThreadIsBlocked(t2);
                    }
                    channel.send(threadName + ".get finished");
                }
            });
        t1.start();
        channel.assertNextMessage("t1.get started"); // first thread is in the synchronized block
        t2.start();
        channel.assertNextMessage("t1.get finished"); // first thread left the synchronized block
        channel.assertNextMessage("t2.get started");
        channel.assertNextMessage("t2.get finished");
    }

    @Test
    public void testThatGetCacheEntryIsNotBlockedForSecondThreadIfDifferentSession()
    {
        final Thread t1 = new Thread(new Runnable()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void run()
                {
                    FetchOptions<Sample> fetchOptions = new SampleFetchOptions().cacheMode(CacheMode.CACHE);
                    executor.search(SESSION_TOKEN1, new SampleSearchCriteria(), fetchOptions);
                }
            }, "t1");
        final Thread t2 = new Thread(new Runnable()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void run()
                {
                    FetchOptions<Experiment> fetchOptions = new ExperimentFetchOptions().cacheMode(CacheMode.CACHE);
                    executor.search(SESSION_TOKEN2, new ExperimentSearchCriteria(), fetchOptions);
                }
            }, "t2");

        executor.setGetAction(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    Thread currentThread = Thread.currentThread();
                    if (currentThread == t1)
                    {
                        channel.send("t1.get started");
                        channel.assertNextMessage("t2.get started");
                        channel.send("t1.get finished");
                    } else
                    {
                        channel.send("t2.get started");
                    }
                }
            });
        t1.start();
        channel.assertNextMessage("t1.get started");
        t2.start();
        channel.assertNextMessage("t1.get finished");
    }

    @Test
    public void testThatGetCacheEntryIsBlockedForSecondThreadIfSameEntry()
    {
        final Thread t1 = new Thread(new Runnable()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void run()
                {
                    FetchOptions<Sample> fetchOptions = new SampleFetchOptions().cacheMode(CacheMode.CACHE);
                    executor.search(SESSION_TOKEN1, new SampleSearchCriteria(), fetchOptions);
                }
            }, "t1");
        final Thread t2 = new Thread(new Runnable()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void run()
                {
                    FetchOptions<Experiment> fetchOptions = new ExperimentFetchOptions().cacheMode(CacheMode.CACHE);
                    executor.search(SESSION_TOKEN1, new ExperimentSearchCriteria(), fetchOptions);
                }
            }, "t2");
        SearchCacheEntry<Object> entry = new SearchCacheEntry<>();
        executor.addEntry(t1, entry);
        executor.addEntry(t2, entry);

        executor.setPutAction(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String threadName = Thread.currentThread().getName();
                    channel.send(threadName + ".put started");
                    if (threadName.equals("t1"))
                    {
                        waitUntilThreadIsBlocked(t2);
                    }
                    channel.send(threadName + ".put finished");
                }
            });
        t1.start();
        channel.assertNextMessage("t1.put started");
        t2.start();
        channel.assertNextMessage("t1.put finished");
    }

    private Session createSession(String userId)
    {
        return new Session(userId, "token", new Principal(), "", 1);
    }

    private void waitUntilThreadIsBlocked(Thread thread)
    {
        long t0 = System.currentTimeMillis();
        while (thread.getState().equals(State.BLOCKED) == false)
        {
            if (System.currentTimeMillis() - t0 > 900)
            {
                channel.send("thread " + thread.getName() + " never blocked");
                break;
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private class TestSearchMethodExecutor extends AbstractSearchMethodExecutor
    {
        private Map<String, Session> sessions = new HashMap<>();

        private Map<Thread, SearchCacheEntry<Object>> entries = new HashMap<>();

        private IDelegatedAction getAction;
        
        private IDelegatedAction putAction;

        private SearchCacheEntry<Object> defaultEntry;

        @SuppressWarnings("unchecked")
        public TestSearchMethodExecutor()
        {
            defaultEntry = new SearchCacheEntry<>();
            defaultEntry.setObjects(Collections.emptySet());
            cache = new ISearchCache()
                {
                    @Override
                    public SearchCacheEntry get(SearchCacheKey key)
                    {
                        if (getAction != null)
                        {
                            getAction.execute();
                        }
                        SearchCacheEntry<Object> entry = entries.get(Thread.currentThread());
                        return entry == null ? defaultEntry : entry;
                    }

                    @Override
                    public void put(SearchCacheKey key, SearchCacheEntry entry)
                    {
                        if (putAction != null)
                        {
                            putAction.execute();
                        }
                    }

                    @Override
                    public void remove(SearchCacheKey key)
                    {
                    }
                };
        }

        @Override
        protected Collection doSearchAndTranslate(IOperationContext context, AbstractSearchCriteria criteria, 
                FetchOptions fetchOptions)
        {
            return Collections.emptySet();
        }

        void addSession(String sessionToken, Session session)
        {
            sessions.put(sessionToken, session);
        }

        void addEntry(Thread thread, SearchCacheEntry<Object> entry)
        {
            entries.put(thread, entry);
        }

        void setGetAction(IDelegatedAction getAction)
        {
            this.getAction = getAction;
        }
        
        void setPutAction(IDelegatedAction putAction)
        {
            this.putAction = putAction;
        }

        @Override
        protected Session getSession(String sessionToken)
        {
            return sessions.get(sessionToken);
        }

        @Override
        protected ISearchObjectExecutor getSearchExecutor()
        {
            return null;
        }

        @Override
        protected ITranslator getTranslator()
        {
            return null;
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
