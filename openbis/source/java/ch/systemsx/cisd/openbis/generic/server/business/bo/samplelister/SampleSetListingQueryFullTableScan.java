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

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.BaseEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;

/**
 * An implementation of {@link ISampleSetListingQuery} which gets all all rows and then filters them
 * down by sample id. This will be a faster way of getting the samples then getting them one by one
 * (as {@link SampleSetListingQueryOneByOne} does) when a the requested samples are a considerable
 * part of all samples.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses={SampleRecord.class, ISampleListingQuery.class})
class SampleSetListingQueryFullTableScan implements ISampleSetListingQuery
{
    private final ISampleListingQuery query;

    public SampleSetListingQueryFullTableScan(final ISampleListingQuery query)
    {
        this.query = query;
    }

    public Iterable<GenericEntityPropertyRecord> getEntityPropertyGenericValues(final LongSet entityIDs)
    {
        return new Iterable<GenericEntityPropertyRecord>()
            {
                public Iterator<GenericEntityPropertyRecord> iterator()
                {
                    return new FilterIterator<GenericEntityPropertyRecord>(query
                            .getSamplePropertyGenericValues(),
                            new Predicate<BaseEntityPropertyRecord>()
                                {
                                    public boolean evaluate(BaseEntityPropertyRecord baseSample)
                                    {
                                        return entityIDs.contains(baseSample.entity_id);
                                    }
                                });
                }
            };
    }

    public Iterable<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
            final LongSet entityIDs)
    {
        return new Iterable<MaterialEntityPropertyRecord>()
            {
                public Iterator<MaterialEntityPropertyRecord> iterator()
                {
                    return new FilterIterator<MaterialEntityPropertyRecord>(query
                            .getSamplePropertyMaterialValues(),
                            new Predicate<BaseEntityPropertyRecord>()
                                {
                                    public boolean evaluate(BaseEntityPropertyRecord baseSample)
                                    {
                                        return entityIDs.contains(baseSample.entity_id);
                                    }
                                });
                }
            };
    }

    public Iterable<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
            final LongSet entityIDs)
    {
        return new Iterable<VocabularyTermRecord>()
            {
                public Iterator<VocabularyTermRecord> iterator()
                {
                    return new FilterIterator<VocabularyTermRecord>(query
                            .getSamplePropertyVocabularyTermValues(),
                            new Predicate<BaseEntityPropertyRecord>()
                                {
                                    public boolean evaluate(BaseEntityPropertyRecord baseSample)
                                    {
                                        return entityIDs.contains(baseSample.entity_id);
                                    }
                                });
                }
            };
    }

    public Iterable<SampleRecord> getSamples(final LongSet sampleIds)
    {
        return new Iterable<SampleRecord>()
            {
                public Iterator<SampleRecord> iterator()
                {
                    return new FilterIterator<SampleRecord>(query.getSamples(),
                            new Predicate<SampleRecord>()
                                {
                                    public boolean evaluate(SampleRecord sample)
                                    {
                                        return sampleIds.contains(sample.id);
                                    }
                                });
                }
            };
    }

}
