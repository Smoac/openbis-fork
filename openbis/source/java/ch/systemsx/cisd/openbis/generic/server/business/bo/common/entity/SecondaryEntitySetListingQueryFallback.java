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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Iterator;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.iterators.FilterIterator;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.QueryStrategyChooser;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO.ISecondaryEntitySetListingQuery;

/**
 * A fallback implementation of {@link ISecondaryEntitySetListingQuery} for database engines who
 * don't support querying for identifier sets.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { ISecondaryEntityListingQuery.class })
class SecondaryEntitySetListingQueryFallback implements ISecondaryEntitySetListingQuery
{
    private final ISecondaryEntitySetListingQuery oneByOneDelegate;

    private final ISecondaryEntitySetListingQuery fullTableScanDelegate;

    private final QueryStrategyChooser strategyChooser;

    public SecondaryEntitySetListingQueryFallback(final ISecondaryEntityListingQuery query,
            QueryStrategyChooser strategyChooser, long databaseInstanceId)
    {
        this.strategyChooser = strategyChooser;
        this.oneByOneDelegate = new SecondaryEntitySetListingQueryOneByOne(query);
        this.fullTableScanDelegate =
                new SecondaryEntitySetListingQueryFullTableScan(query, databaseInstanceId);
    }

    public Iterable<SampleReferenceRecord> getSamples(LongSet sampleIds)
    {
        if (strategyChooser.useFullTableScan(sampleIds))
        {
            return fullTableScanDelegate.getSamples(sampleIds);
        } else
        {
            return oneByOneDelegate.getSamples(sampleIds);
        }
    }

    private static class SecondaryEntitySetListingQueryOneByOne implements
            ISecondaryEntitySetListingQuery
    {
        private final ISecondaryEntityListingQuery query;

        public SecondaryEntitySetListingQueryOneByOne(ISecondaryEntityListingQuery query)
        {
            this.query = query;
        }

        public Iterable<SampleReferenceRecord> getSamples(final LongSet sampleIds)
        {
            return new Iterable<SampleReferenceRecord>()
                {
                    public Iterator<SampleReferenceRecord> iterator()
                    {
                        final LongIterator it = sampleIds.iterator();
                        return new Iterator<SampleReferenceRecord>()
                            {
                                public boolean hasNext()
                                {
                                    return it.hasNext();
                                }

                                public SampleReferenceRecord next()
                                {
                                    return query.getSample(it.nextLong());
                                }

                                public void remove() throws UnsupportedOperationException
                                {
                                    throw new UnsupportedOperationException();
                                }
                            };
                    }
                };
        }
    }

    private static class SecondaryEntitySetListingQueryFullTableScan implements
            ISecondaryEntitySetListingQuery
    {
        private final ISecondaryEntityListingQuery query;

        private final long databaseInstanceId;

        public SecondaryEntitySetListingQueryFullTableScan(ISecondaryEntityListingQuery query,
                long databaseInstanceId)
        {
            this.query = query;
            this.databaseInstanceId = databaseInstanceId;
        }

        public Iterable<SampleReferenceRecord> getSamples(final LongSet sampleIds)
        {
            return new Iterable<SampleReferenceRecord>()
                {
                    public Iterator<SampleReferenceRecord> iterator()
                    {
                        return new FilterIterator<SampleReferenceRecord>(query
                                .getAllSamples(databaseInstanceId),
                                new Predicate<SampleReferenceRecord>()
                                    {
                                        public boolean evaluate(SampleReferenceRecord sample)
                                        {
                                            return sampleIds.contains(sample.id);
                                        }
                                    });
                    }
                };
        }

    }
}
