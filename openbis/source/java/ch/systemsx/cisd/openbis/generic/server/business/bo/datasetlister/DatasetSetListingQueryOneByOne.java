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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Iterator;

import ch.rinn.restrictions.Friend;

/**
 * An implementation of {@link IDatasetSetListingQuery} that gets the datasets one by one.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = IDatasetListingQuery.class)
class DatasetSetListingQueryOneByOne implements IDatasetSetListingQuery
{
    private final IDatasetListingQuery query;

    public DatasetSetListingQueryOneByOne(final IDatasetListingQuery query)
    {
        this.query = query;
    }

    public Iterable<DatasetRecord> getDatasets(final LongSet sampleIds)
    {
        return new Iterable<DatasetRecord>()
            {
                public Iterator<DatasetRecord> iterator()
                {
                    final LongIterator it = sampleIds.iterator();
                    return new Iterator<DatasetRecord>()
                        {
                            public boolean hasNext()
                            {
                                return it.hasNext();
                            }

                            public DatasetRecord next()
                            {
                                return query.getDataset(it.nextLong());
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
