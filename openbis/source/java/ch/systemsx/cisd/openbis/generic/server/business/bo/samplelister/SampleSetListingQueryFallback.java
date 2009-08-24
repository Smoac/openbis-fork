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
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;

/**
 * A fallback implementation of {@link ISampleSetListingQuery} for database engines who don't
 * support querying for identifier sets.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses=ISampleListingQuery.class)
class SampleSetListingQueryFallback implements ISampleSetListingQuery
{
    private final long UPDATE_INTERVAL = 10 * 60 * 1000L; // 10 minutes
    
    private final float FULL_TABLE_SCAN_THRESHOLD = 0.2f;
    
    private long numberOfSamplesLastUpdated;
    
    private long numberOfSamples;
    
    private ISampleListingQuery query;
    
    private ISampleSetListingQuery oneByOneDelegate;
    
    private ISampleSetListingQuery fullTableScanDelegate;

    public SampleSetListingQueryFallback(final ISampleListingQuery query)
    {
        this.query = query;
        this.oneByOneDelegate = new SampleSetListingQueryOneByOne(query);
        this.fullTableScanDelegate = new SampleSetListingQueryFullTableScan(query);
    }

    private synchronized long getNumberOfSamples()
    {
        if (System.currentTimeMillis() - numberOfSamplesLastUpdated > UPDATE_INTERVAL)
        {
            numberOfSamples = query.getSampleCount();
            numberOfSamplesLastUpdated = System.currentTimeMillis();
        }
        return numberOfSamples;
    }

    public Iterable<GenericEntityPropertyRecord> getEntityPropertyGenericValues(final LongSet entityIDs)
    {
        if (entityIDs.size() >= getNumberOfSamples() * FULL_TABLE_SCAN_THRESHOLD)
        {
            return fullTableScanDelegate.getEntityPropertyGenericValues(entityIDs);
        } else
        {
            return oneByOneDelegate.getEntityPropertyGenericValues(entityIDs);
        }
    }

    public Iterable<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
            final LongSet entityIDs)
    {
        if (entityIDs.size() >= getNumberOfSamples() * FULL_TABLE_SCAN_THRESHOLD)
        {
            return fullTableScanDelegate.getEntityPropertyMaterialValues(entityIDs);
        } else
        {
            return oneByOneDelegate.getEntityPropertyMaterialValues(entityIDs);
        }
    }

    public Iterable<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
            final LongSet entityIDs)
    {
        if (entityIDs.size() >= getNumberOfSamples() * FULL_TABLE_SCAN_THRESHOLD)
        {
            return fullTableScanDelegate.getEntityPropertyVocabularyTermValues(entityIDs);
        } else
        {
            return oneByOneDelegate.getEntityPropertyVocabularyTermValues(entityIDs);
        }
    }

    public Iterable<SampleRecord> getSamples(final LongSet sampleIds)
    {
        if (sampleIds.size() >= getNumberOfSamples() * FULL_TABLE_SCAN_THRESHOLD)
        {
            return fullTableScanDelegate.getSamples(sampleIds);
        } else
        {
            return oneByOneDelegate.getSamples(sampleIds);
        }
    }

}
