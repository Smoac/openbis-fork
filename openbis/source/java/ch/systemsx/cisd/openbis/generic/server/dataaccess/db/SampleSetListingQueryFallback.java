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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import it.unimi.dsi.fastutil.longs.LongSet;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.CoVoSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.GenericSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.MaterialSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.SampleRowVO;

/**
 * A fallback implementation of {@link ISampleSetListingQuery} for database engines who don't
 * support querying for identifier sets.
 * 
 * @author Bernd Rinn
 */
public class SampleSetListingQueryFallback implements ISampleSetListingQuery
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

    public Iterable<GenericSamplePropertyVO> getSamplePropertyGenericValues(final LongSet sampleIds)
    {
        if (sampleIds.size() >= getNumberOfSamples() * FULL_TABLE_SCAN_THRESHOLD)
        {
            return fullTableScanDelegate.getSamplePropertyGenericValues(sampleIds);
        } else
        {
            return oneByOneDelegate.getSamplePropertyGenericValues(sampleIds);
        }
    }

    public Iterable<MaterialSamplePropertyVO> getSamplePropertyMaterialValues(
            final LongSet sampleIds)
    {
        if (sampleIds.size() >= getNumberOfSamples() * FULL_TABLE_SCAN_THRESHOLD)
        {
            return fullTableScanDelegate.getSamplePropertyMaterialValues(sampleIds);
        } else
        {
            return oneByOneDelegate.getSamplePropertyMaterialValues(sampleIds);
        }
    }

    public Iterable<CoVoSamplePropertyVO> getSamplePropertyVocabularyTermValues(
            final LongSet sampleIds)
    {
        if (sampleIds.size() >= getNumberOfSamples() * FULL_TABLE_SCAN_THRESHOLD)
        {
            return fullTableScanDelegate.getSamplePropertyVocabularyTermValues(sampleIds);
        } else
        {
            return oneByOneDelegate.getSamplePropertyVocabularyTermValues(sampleIds);
        }
    }

    public Iterable<SampleRowVO> getSamples(final LongSet sampleIds)
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
