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

import java.util.Iterator;

import net.lemnik.eodsql.DataIterator;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.CoVoSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.GenericSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.MaterialSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.SampleRowVO;

/**
 * An implementation of {@link ISampleSetListingQuery} that gets the samples one by one.
 * 
 * @author Bernd Rinn
 */
public class SampleSetListingQueryOneByOne implements ISampleSetListingQuery
{
    private final ISampleListingQuery query;
    
    public SampleSetListingQueryOneByOne(final ISampleListingQuery query)
    {
        this.query = query;
    }

    public Iterable<GenericSamplePropertyVO> getSamplePropertyGenericValues(final LongSet sampleIds)
    {
        return new Iterable<GenericSamplePropertyVO>()
            {
                public Iterator<GenericSamplePropertyVO> iterator()
                {
                    final LongIterator outerIt = sampleIds.iterator();
                    return new Iterator<GenericSamplePropertyVO>()
                        {
                            DataIterator<GenericSamplePropertyVO> innerIt = null;

                            public boolean hasNext()
                            {
                                while ((innerIt == null || innerIt.hasNext() == false)
                                        && outerIt.hasNext())
                                {
                                    innerIt =
                                            query
                                                    .getSamplePropertyGenericValues(outerIt
                                                            .nextLong());
                                }
                                return (innerIt != null && innerIt.isClosed() == false);
                            }

                            public GenericSamplePropertyVO next()
                            {
                                return innerIt.next();
                            }

                            public void remove() throws UnsupportedOperationException
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
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
                    final LongIterator outerIt = sampleIds.iterator();
                    return new Iterator<MaterialSamplePropertyVO>()
                        {
                            DataIterator<MaterialSamplePropertyVO> innerIt = null;

                            public boolean hasNext()
                            {
                                while ((innerIt == null || innerIt.hasNext() == false)
                                        && outerIt.hasNext())
                                {
                                    innerIt =
                                            query.getSamplePropertyMaterialValues(outerIt
                                                    .nextLong());
                                }
                                return (innerIt != null && innerIt.isClosed() == false);
                            }

                            public MaterialSamplePropertyVO next()
                            {
                                return innerIt.next();
                            }

                            public void remove() throws UnsupportedOperationException
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
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
                    final LongIterator outerIt = sampleIds.iterator();
                    return new Iterator<CoVoSamplePropertyVO>()
                        {
                            DataIterator<CoVoSamplePropertyVO> innerIt = null;

                            public boolean hasNext()
                            {
                                while ((innerIt == null || innerIt.hasNext() == false)
                                        && outerIt.hasNext())
                                {
                                    innerIt =
                                            query.getSamplePropertyVocabularyTermValues(outerIt
                                                    .nextLong());
                                }
                                return (innerIt != null && innerIt.isClosed() == false);
                            }

                            public CoVoSamplePropertyVO next()
                            {
                                return innerIt.next();
                            }

                            public void remove() throws UnsupportedOperationException
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    public Iterable<SampleRowVO> getSamples(final LongSet sampleIds)
    {
        return new Iterable<SampleRowVO>()
            {
                public Iterator<SampleRowVO> iterator()
                {
                    final LongIterator it = sampleIds.iterator();
                    return new Iterator<SampleRowVO>()
                        {
                            public boolean hasNext()
                            {
                                return it.hasNext();
                            }

                            public SampleRowVO next()
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
