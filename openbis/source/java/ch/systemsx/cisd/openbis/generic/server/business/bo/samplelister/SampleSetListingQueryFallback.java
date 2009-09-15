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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import it.unimi.dsi.fastutil.longs.LongSet;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.QueryStrategyChooser;

/**
 * A fallback implementation of {@link ISampleSetListingQuery} for database engines who don't
 * support querying for identifier sets.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = ISampleListingQuery.class)
class SampleSetListingQueryFallback implements ISampleSetListingQuery
{
    private final ISampleSetListingQuery oneByOneDelegate;

    private final ISampleSetListingQuery fullTableScanDelegate;

    private final QueryStrategyChooser strategyChooser;

    public SampleSetListingQueryFallback(final ISampleListingQuery query,
            QueryStrategyChooser strategyChooser, long databaseInstanceId)
    {
        this.strategyChooser = strategyChooser;
        this.oneByOneDelegate = new SampleSetListingQueryOneByOne(query);
        this.fullTableScanDelegate =
                new SampleSetListingQueryFullTableScan(query, databaseInstanceId);
    }

    public Iterable<SampleRecord> getSamples(final LongSet sampleIds)
    {
        if (strategyChooser.useFullTableScan(sampleIds))
        {
            return fullTableScanDelegate.getSamples(sampleIds);
        } else
        {
            return oneByOneDelegate.getSamples(sampleIds);
        }
    }
}
