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

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Iterator;

import ch.rinn.restrictions.Friend;

/**
 * An implementation of {@link ISampleSetListingQuery} that gets the samples one by one.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = ISampleListingQuery.class)
class SampleSetListingQueryOneByOne implements ISampleSetListingQuery
{
    private final ISampleListingQuery query;

    public SampleSetListingQueryOneByOne(final ISampleListingQuery query)
    {
        this.query = query;
    }

    public Iterable<SampleRecord> getSamples(final LongSet sampleIds)
    {
        return new Iterable<SampleRecord>()
            {
                public Iterator<SampleRecord> iterator()
                {
                    final LongIterator it = sampleIds.iterator();
                    return new Iterator<SampleRecord>()
                        {
                            public boolean hasNext()
                            {
                                return it.hasNext();
                            }

                            public SampleRecord next()
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
