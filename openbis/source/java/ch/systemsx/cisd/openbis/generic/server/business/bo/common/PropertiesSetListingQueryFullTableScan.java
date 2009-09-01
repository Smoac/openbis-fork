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

import java.util.Iterator;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.iterators.FilterIterator;

import ch.rinn.restrictions.Friend;

/**
 * An implementation of {@link IEntityPropertyListingQuery} which gets all all rows and then filters
 * them down by entity id. This will be a faster way of getting the entities then getting them one
 * by one (as {@link PropertiesSetListingQueryOneByOne} does) when a the requested entities are a
 * considerable part of all entities.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
    { IEntityPropertyListingQuery.class })
class PropertiesSetListingQueryFullTableScan implements IEntityPropertySetListingQuery
{
    private final IEntityPropertyListingQuery query;

    public PropertiesSetListingQueryFullTableScan(final IEntityPropertyListingQuery query)
    {
        this.query = query;
    }

    public Iterable<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            final LongSet entityIDs)
    {
        return new Iterable<GenericEntityPropertyRecord>()
            {
                public Iterator<GenericEntityPropertyRecord> iterator()
                {
                    return new FilterIterator<GenericEntityPropertyRecord>(query
                            .getEntityPropertyGenericValues(),
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
                            .getEntityPropertyMaterialValues(),
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
                            .getEntityPropertyVocabularyTermValues(),
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
}
