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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AbstractSearchMethodExecutorStressTest
{

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
    }

    @Test
    public void testConcurrencyWithoutEvicting() throws InterruptedException
    {
        for (int run = 0; run < 5; run++)
        {
            TestSearchMethodExecutor executor = testConcurrency(0);

            for (Map.Entry<SearchCacheKey, Integer> entry : executor.getSearchCounts().entrySet())
            {
                if (entry.getValue() != 1)
                {
                    executor.addError("Run " + run + " : " + entry.getValue() + " searches were executed instead of 1 for the key " + entry.getKey());
                }
            }

            if (executor.getErrors().size() > 0)
            {
                Assert.fail(StringUtils.join(executor.getErrors(), "\n"));
            }
        }
    }

    @Test
    public void testConcurrencyWithEvicting() throws InterruptedException
    {
        for (int run = 0; run < 5; run++)
        {
            TestSearchMethodExecutor executor = testConcurrency(10 * FileUtils.ONE_KB);

            if (executor.getErrors().size() > 0)
            {
                Assert.fail(StringUtils.join(executor.getErrors(), "\n"));
            }
        }
    }

    private TestSearchMethodExecutor testConcurrency(long cacheSize) throws InterruptedException
    {
        int SESSION_COUNT = 5;
        int THREAD_COUNT = 5;
        int KEY_VERSION_COUNT = 20;

        final TestSearchMethodExecutor executor = new TestSearchMethodExecutor(cacheSize);

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
                SearchCacheKey spaceKey =
                        new SearchCacheKey(sessionToken, spaceSearchCriteria, new SpaceFetchOptions().cacheMode(CacheMode.CACHE));
                keys.add(spaceKey);
                executor.setSearchResult(spaceKey, new RandomSizeArray(cacheSize));

                ProjectSearchCriteria projectSearchCriteria = new ProjectSearchCriteria();
                projectSearchCriteria.withCode().thatEquals(String.valueOf(k));
                SearchCacheKey projectKey =
                        new SearchCacheKey(sessionToken, projectSearchCriteria, new ProjectFetchOptions().cacheMode(CacheMode.CACHE));
                keys.add(projectKey);
                executor.setSearchResult(projectKey, new RandomSizeArray(cacheSize));

                ExperimentSearchCriteria experimentSearchCriteria = new ExperimentSearchCriteria();
                experimentSearchCriteria.withCode().thatEquals(String.valueOf(k));
                SearchCacheKey experimentKey =
                        new SearchCacheKey(sessionToken, experimentSearchCriteria, new ExperimentFetchOptions().cacheMode(CacheMode.CACHE));
                keys.add(experimentKey);
                executor.setSearchResult(experimentKey, new RandomSizeArray(cacheSize));

                SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
                sampleSearchCriteria.withCode().thatEquals(String.valueOf(k));
                SearchCacheKey sampleKey =
                        new SearchCacheKey(sessionToken, sampleSearchCriteria, new SampleFetchOptions().cacheMode(CacheMode.CACHE));
                keys.add(sampleKey);
                executor.setSearchResult(sampleKey, new RandomSizeArray(cacheSize));
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

                            Object actualResult = searchResult.getObjects().get(0);
                            Object expectedResult = executor.getSearchResult(key);

                            if (false == actualResult.equals(expectedResult))
                            {
                                executor.addError("Actual search result: " + actualResult + " but expected: " + expectedResult + " for key: "
                                        + key);
                            }
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

        return executor;
    }

    private static class TestSearchMethodExecutor extends AbstractSearchMethodExecutor
    {
        private Map<String, Session> sessions = new HashMap<>();

        private Map<SearchCacheKey, Integer> searchCounts = new HashMap<>();

        private Map<SearchCacheKey, Object> searchResults = new HashMap<>();

        private List<String> errors = Collections.synchronizedList(new ArrayList<String>());

        public TestSearchMethodExecutor(final long cacheSize)
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

                    @Override
                    protected long getCacheSize()
                    {
                        if (cacheSize > 0)
                        {
                            return cacheSize;
                        } else
                        {
                            return super.getCacheSize();
                        }
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

            return Collections.singleton(searchResults.get(key));
        }

        public Map<SearchCacheKey, Integer> getSearchCounts()
        {
            return searchCounts;
        }

        public void setSearchResult(SearchCacheKey key, Object result)
        {
            searchResults.put(key, result);
        }

        public Object getSearchResult(SearchCacheKey key)
        {
            return searchResults.get(key);
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

        public void addError(String error)
        {
            errors.add(error);
        }

        public List<String> getErrors()
        {
            return errors;
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

    private static class RandomSizeArray
    {

        private byte[] array;

        public RandomSizeArray(long maxSize)
        {
            array = new byte[(int) (Math.random() * maxSize)];
        }

        @Override
        public int hashCode()
        {
            return array.length;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RandomSizeArray other = (RandomSizeArray) obj;
            return array.length == other.array.length;
        }

    }

}
