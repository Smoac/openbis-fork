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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.AbstractSearchMethodExecutor.CacheEntry;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author pkupczyk
 */
public class SearchCacheEventListener implements CacheEventListener
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    @Override
    public void dispose()
    {
        operationLog.info("Disposing cache listener");
    }

    @Override
    public void notifyElementEvicted(Ehcache cache, Element element)
    {
        logOperation(element, "has been evicted from the cache");
    }

    @Override
    public void notifyElementExpired(Ehcache cache, Element element)
    {
        logOperation(element, "has expired");
    }

    @Override
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException
    {
        logOperation(element, "has been put to the cache");
    }

    @Override
    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException
    {
        logOperation(element, "has been removed from the cache");
    }

    @Override
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException
    {
        logOperation(element, "has been updated");
    }

    @Override
    public void notifyRemoveAll(Ehcache cache)
    {
        operationLog.info("All search results have been removed from the cache.");
    }

    @SuppressWarnings("rawtypes")
    private void logOperation(Element element, String operation)
    {
        CacheEntry entry = (CacheEntry) element.getObjectValue();
        if (entry != null)
        {
            operationLog.info("Cache entry " + entry.hashCode() + " that contains search result with "
                    + (entry.getObjects() != null ? entry.getObjects().size() : 0) + " objects " + operation + ".");
        }
    }

    @Override
    public Object clone()
    {
        return new SearchCacheEventListener();
    }

}
