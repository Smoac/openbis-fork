/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Helps deciding what kind of sql query would be more efficient: full table scan with filtering or
 * querying about each single item separately. Decides on the basis of the number of items in the
 * whole table.
 * 
 * @author Tomasz Pylak
 */
public class QueryStrategyChooser
{
    private final long UPDATE_INTERVAL = 10 * 60 * 1000L; // 10 minutes

    private final float FULL_TABLE_SCAN_THRESHOLD = 0.2f;

    public interface IEntitiesCountProvider
    {
        long count();
    }

    private final IEntitiesCountProvider countProvider;

    private long numberOfEntitiesLastUpdatedTime;

    private long numberOfEntities;

    public QueryStrategyChooser(IEntitiesCountProvider countProvider)
    {
        this.countProvider = countProvider;
    }

    private synchronized long getNumberOfEntities()
    {
        if (System.currentTimeMillis() - numberOfEntitiesLastUpdatedTime > UPDATE_INTERVAL)
        {
            numberOfEntities = countProvider.count();
            numberOfEntitiesLastUpdatedTime = System.currentTimeMillis();
        }
        return numberOfEntities;
    }

    public boolean useFullTableScan(final LongSet entityIDs)
    {
        return entityIDs.size() >= getNumberOfEntities() * FULL_TABLE_SCAN_THRESHOLD;
    }
}
