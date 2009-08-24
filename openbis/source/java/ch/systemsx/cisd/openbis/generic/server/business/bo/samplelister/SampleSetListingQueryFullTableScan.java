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

import java.util.Iterator;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.iterators.FilterIterator;

import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.BaseSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.CoVoSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.GenericSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.MaterialSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.SampleRowVO;

/**
 * An implementation of {@link ISampleSetListingQuery} which gets all all rows and then filters them
 * down by sample id. This will be a faster way of getting the samples then getting them one by one
 * (as {@link SampleSetListingQueryOneByOne} does) when a the requested samples are a considerable
 * part of all samples.
 * 
 * @author Bernd Rinn
 */
class SampleSetListingQueryFullTableScan implements ISampleSetListingQuery
{
    private final ISampleListingQuery query;

    public SampleSetListingQueryFullTableScan(final ISampleListingQuery query)
    {
        this.query = query;
    }

    public Iterable<GenericSamplePropertyVO> getSamplePropertyGenericValues(final LongSet sampleIds)
    {
        return new Iterable<GenericSamplePropertyVO>()
            {
                public Iterator<GenericSamplePropertyVO> iterator()
                {
                    return new FilterIterator<GenericSamplePropertyVO>(query
                            .getSamplePropertyGenericValues(),
                            new Predicate<BaseSamplePropertyVO>()
                                {
                                    public boolean evaluate(BaseSamplePropertyVO baseSample)
                                    {
                                        return sampleIds.contains(baseSample.samp_id);
                                    }
                                });
                }
            };
    }

    public Iterable<MaterialSamplePropertyVO> getSamplePropertyMaterialValues(
            final LongSet sampleIds)
    {
        return new Iterable<MaterialSamplePropertyVO>()
            {
                public Iterator<MaterialSamplePropertyVO> iterator()
                {
                    return new FilterIterator<MaterialSamplePropertyVO>(query
                            .getSamplePropertyMaterialValues(),
                            new Predicate<BaseSamplePropertyVO>()
                                {
                                    public boolean evaluate(BaseSamplePropertyVO baseSample)
                                    {
                                        return sampleIds.contains(baseSample.samp_id);
                                    }
                                });
                }
            };
    }

    public Iterable<CoVoSamplePropertyVO> getSamplePropertyVocabularyTermValues(
            final LongSet sampleIds)
    {
        return new Iterable<CoVoSamplePropertyVO>()
            {
                public Iterator<CoVoSamplePropertyVO> iterator()
                {
                    return new FilterIterator<CoVoSamplePropertyVO>(query
                            .getSamplePropertyVocabularyTermValues(),
                            new Predicate<BaseSamplePropertyVO>()
                                {
                                    public boolean evaluate(BaseSamplePropertyVO baseSample)
                                    {
                                        return sampleIds.contains(baseSample.samp_id);
                                    }
                                });
                }
            };
    }

    public Iterable<SampleRowVO> getSamples(final LongSet sampleIds)
    {
        return new Iterable<SampleRowVO>()
            {
                public Iterator<SampleRowVO> iterator()
                {
                    return new FilterIterator<SampleRowVO>(query.getSamples(),
                            new Predicate<SampleRowVO>()
                                {
                                    public boolean evaluate(SampleRowVO sample)
                                    {
                                        return sampleIds.contains(sample.id);
                                    }
                                });
                }
            };
    }

}
