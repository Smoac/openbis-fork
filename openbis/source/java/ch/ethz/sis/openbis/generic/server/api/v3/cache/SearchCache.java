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

package ch.ethz.sis.openbis.generic.server.api.v3.cache;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration.MaxDepthExceededBehavior;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.parser.MemorySizeFormatter;
import ch.systemsx.cisd.common.parser.PercentFormatter;

/**
 * @author pkupczyk
 */
@Component
public class SearchCache<CRITERIA, FETCH_OPTIONS, OBJECT> implements ISearchCache<CRITERIA, FETCH_OPTIONS, OBJECT>
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, SearchCache.class);

    private static final String CACHE_NAME = "searchCache";

    public static final String CACHE_SIZE_PROPERTY_NAME = "ch.ethz.sis.openbis.v3.searchcache.size";

    @Autowired
    private CacheManager cacheManager;

    @SuppressWarnings("unchecked")
    @Override
    public SearchCacheEntry<OBJECT> get(SearchCacheKey<CRITERIA, FETCH_OPTIONS> key)
    {
        Element element = getCache().get(key);

        if (element != null)
        {
            return (SearchCacheEntry<OBJECT>) element.getObjectValue();
        } else
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void put(SearchCacheKey<CRITERIA, FETCH_OPTIONS> key, SearchCacheEntry<OBJECT> entry)
    {
        Element element = getCache().get(key);

        if (element == null)
        {
            getCache().put(new Element(key, entry));
        } else
        {
            // do not call put() when updating an existing entry as the eviction won't work properly
            SearchCacheEntry<OBJECT> existingEntry = (SearchCacheEntry<OBJECT>) element.getObjectValue();
            existingEntry.setObjects(entry.getObjects());
            // manually log update message because the cache does not log anything during recalculateSize() call
            SearchCacheEventListenerFactory.getListener().notifyElementUpdated(getCache(), element);
            getCache().recalculateSize(key);
        }
    }

    @Override
    public void remove(SearchCacheKey<CRITERIA, FETCH_OPTIONS> key)
    {
        getCache().remove(key);
    }

    @PostConstruct
    public void initCache()
    {
        Cache cache = getCacheManager().getCache(CACHE_NAME);

        if (cache == null)
        {
            operationLog.info("Creating the cache.");

            CacheConfiguration config = new CacheConfiguration();
            config.setName(CACHE_NAME);
            config.setEternal(false);
            config.maxBytesLocalHeap(getCacheSize(), MemoryUnit.BYTES);
            config.persistence(new PersistenceConfiguration().strategy(Strategy.NONE));
            config.setTimeToIdleSeconds(3600);
            config.setTimeToLiveSeconds(0);
            config.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU);
            config.addCacheEventListenerFactory(new CacheEventListenerFactoryConfiguration().className(SearchCacheEventListenerFactory.class
                    .getName()));
            config.sizeOfPolicy(new SizeOfPolicyConfiguration().maxDepth(10000000).maxDepthExceededBehavior(MaxDepthExceededBehavior.CONTINUE));

            cache = new Cache(config);
            getCacheManager().addCache(cache);
        } else
        {
            operationLog.info("The cache already exists. It must have been configured in ehcache.xml file.");
        }
    }

    private Cache getCache()
    {
        return getCacheManager().getCache(CACHE_NAME);
    }

    protected long getCacheSize()
    {
        String propertyValue = getSystemProperty(CACHE_SIZE_PROPERTY_NAME);

        if (propertyValue == null || propertyValue.trim().length() == 0)
        {
            return getCacheDefaultSize();
        } else
        {
            try
            {
                long cacheSize = MemorySizeFormatter.parse(propertyValue);
                operationLog.info("Cache size was set to '" + propertyValue + "' in '" + CACHE_SIZE_PROPERTY_NAME + "' system property.");
                return cacheSize;
            } catch (IllegalArgumentException e1)
            {
                try
                {
                    int cachePercent = PercentFormatter.parse(propertyValue);
                    long cacheSize = (long) ((cachePercent / 100.0) * getMemorySize());
                    operationLog.info("Cache size was set to '" + propertyValue + "' in '" + CACHE_SIZE_PROPERTY_NAME
                            + "' system property. The memory available to the JVM is " + MemorySizeFormatter.format(getMemorySize())
                            + " which gives a cache size of " + MemorySizeFormatter.format(cacheSize));
                    return cacheSize;
                } catch (IllegalArgumentException e2)
                {
                    throw new IllegalArgumentException("Cache size was set to '" + propertyValue + "' in '" + CACHE_SIZE_PROPERTY_NAME
                            + "' system property. This value is incorrect. Please set the property to an absolute value like '512m' or '1g'."
                            + " You can also use a value like '25%' to set the cache size relative to the memory available to the JVM.");
                }
            }
        }
    }

    protected long getCacheDefaultSize()
    {
        long memorySize = getMemorySize();
        long cacheSize = memorySize / 4;
        operationLog.info("Cache size has been set to its default value. The default value is 25% (" + MemorySizeFormatter.format(cacheSize) + ")"
                + " of the memory available to the JVM (" + MemorySizeFormatter.format(memorySize) + ")."
                + " If you would like to change this value, then please set '"
                + CACHE_SIZE_PROPERTY_NAME + "' system property in openbis.conf file.");
        return cacheSize;
    }

    protected CacheManager getCacheManager()
    {
        return cacheManager;
    }

    protected long getMemorySize()
    {
        return Runtime.getRuntime().maxMemory();
    }

    protected String getSystemProperty(String propertyName)
    {
        return System.getProperty(propertyName);
    }

}
