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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.ehcache.CacheManager;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.api.v3.cache.SearchCache;
import ch.ethz.sis.openbis.generic.server.api.v3.cache.SearchCacheKey;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.CacheMode;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SearchResult;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SpaceSearchCriteria;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class AbstractSearchMethodExecutorStressTest
{

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testMultipleThreads() throws InterruptedException
    {
        int SESSION_COUNT = 5;
        int THREAD_COUNT = 5;
        int KEY_VERSION_COUNT = 20;
        int RUN_COUNT = 5;

        for (int r = 0; r < RUN_COUNT; r++)
        {
            final TestSearchMethodExecutor executor = new TestSearchMethodExecutor();

            final List<String> sessionTokens = new ArrayList<String>();
            for (int s = 0; s < SESSION_COUNT; s++)
            {
                Session session = new Session("user" + s, "token" + s, new Principal(), "", 1);
                sessionTokens.add(session.getSessionToken());
                executor.addSession(session.getSessionToken(), session);
            }

            final List<SearchCacheKey> keys = new ArrayList<SearchCacheKey>();
            for (String sessionToken : sessionTokens)
            {
                for (int k = 0; k < KEY_VERSION_COUNT; k++)
                {
                    SpaceSearchCriteria spaceSearchCriteria = new SpaceSearchCriteria();
                    spaceSearchCriteria.withCode().thatEquals(String.valueOf(k));
                    keys.add(new SearchCacheKey(sessionToken, spaceSearchCriteria, new SpaceFetchOptions().cacheMode(CacheMode.CACHE)));

                    ProjectSearchCriteria projectSearchCriteria = new ProjectSearchCriteria();
                    projectSearchCriteria.withCode().thatEquals(String.valueOf(k));
                    keys.add(new SearchCacheKey(sessionToken, projectSearchCriteria, new ProjectFetchOptions().cacheMode(CacheMode.CACHE)));

                    ExperimentSearchCriteria experimentSearchCriteria = new ExperimentSearchCriteria();
                    experimentSearchCriteria.withCode().thatEquals(String.valueOf(k));
                    keys.add(new SearchCacheKey(sessionToken, experimentSearchCriteria, new ExperimentFetchOptions().cacheMode(CacheMode.CACHE)));

                    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
                    sampleSearchCriteria.withCode().thatEquals(String.valueOf(k));
                    keys.add(new SearchCacheKey(sessionToken, sampleSearchCriteria, new SampleFetchOptions().cacheMode(CacheMode.CACHE)));
                }
            }

            List<Thread> threads = new ArrayList<Thread>();

            for (int t = 0; t < THREAD_COUNT; t++)
            {
                threads.add(new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            for (int i = 0; i < keys.size() * 2; i++)
                            {
                                SearchCacheKey key = keys.get((int) (Math.random() * keys.size()));
                                SearchResult searchResult = executor.search(key.getSessionToken(), key.getCriteria(), key.getFetchOptions());
                                Assert.assertEquals(searchResult.getObjects().get(0), key);
                            }

                        }
                    }));
            }

            for (Thread thread : threads)
            {
                thread.start();
            }

            for (Thread thread : threads)
            {
                thread.join();
            }

            StringBuilder error = new StringBuilder();
            for (Map.Entry<SearchCacheKey, Integer> entry : executor.getSearchCounts().entrySet())
            {
                if (entry.getValue() != 1)
                {
                    error.append("Run " + r + " : " + entry.getValue() + " searches were executed instead of 1 for the key " + entry.getKey()
                            + "\n");
                }
            }

            if (error.length() > 0)
            {
                Assert.fail(error.toString());
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private class TestSearchMethodExecutor extends AbstractSearchMethodExecutor
    {
        private Map<String, Session> sessions = new HashMap<>();

        private Map<SearchCacheKey, Integer> searchCounts = new HashMap<>();

        public TestSearchMethodExecutor()
        {
            String managerConfig = "<ehcache name='" + UUID.randomUUID() + "'></ehcache>";
            final CacheManager manager = new CacheManager(new ByteArrayInputStream(managerConfig.getBytes()));

            SearchCache theCache = new SearchCache()
                {
                    @Override
                    protected CacheManager getCacheManager()
                    {
                        return manager;
                    }
                };
            theCache.initCache();
            this.cache = theCache;
        }

        @Override
        protected Collection doSearchAndTranslate(IOperationContext context, AbstractObjectSearchCriteria criteria,
                FetchOptions fetchOptions)
        {
            SearchCacheKey key = new SearchCacheKey(context.getSession().getSessionToken(), criteria, fetchOptions);

            Integer searchCount = searchCounts.get(key);
            if (searchCount == null)
            {
                searchCount = 0;
            }
            searchCounts.put(key, ++searchCount);

            try
            {
                Thread.sleep(1);
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            return Collections.singleton(key);
        }

        public Map<SearchCacheKey, Integer> getSearchCounts()
        {
            return searchCounts;
        }

        void addSession(String sessionToken, Session session)
        {
            sessions.put(sessionToken, session);
        }

        @Override
        protected Session getSession(String sessionToken)
        {
            return sessions.get(sessionToken);
        }

        @Override
        protected ISearchObjectExecutor getSearchExecutor()
        {
            throw new IllegalStateException("This should never be called as we did override doSearchAndTranslate()");
        }

        @Override
        protected ITranslator getTranslator()
        {
            throw new IllegalStateException("This should never be called as we did override doSearchAndTranslate()");
        }

        @Override
        protected void clearCurrentSession()
        {
            // nothing needed here in this test
        }

        @Override
        protected void flushCurrentSession()
        {
            // nothing needed here in this test
        }

    }

}
