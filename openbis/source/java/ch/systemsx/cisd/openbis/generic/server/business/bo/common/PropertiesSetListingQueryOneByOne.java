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

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Iterator;

import net.lemnik.eodsql.DataIterator;

import ch.rinn.restrictions.Friend;

/**
 * An implementation of {@link IEntityPropertySetListingQuery} that gets the entity properties one
 * by one.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
    { IEntityPropertySetListingQuery.class, IEntityPropertyListingQuery.class })
class PropertiesSetListingQueryOneByOne implements IEntityPropertySetListingQuery
{
    private final IEntityPropertyListingQuery query;

    public PropertiesSetListingQueryOneByOne(final IEntityPropertyListingQuery query)
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
                    final LongIterator outerIt = entityIDs.iterator();
                    return new Iterator<GenericEntityPropertyRecord>()
                        {
                            DataIterator<GenericEntityPropertyRecord> innerIt = null;

                            public boolean hasNext()
                            {
                                while ((innerIt == null || innerIt.hasNext() == false)
                                        && outerIt.hasNext())
                                {
                                    innerIt =
                                            query
                                                    .getEntityPropertyGenericValues(outerIt
                                                            .nextLong());
                                }
                                return (innerIt != null && innerIt.isClosed() == false);
                            }

                            public GenericEntityPropertyRecord next()
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

    public Iterable<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
            final LongSet entityIDs)
    {
        return new Iterable<MaterialEntityPropertyRecord>()
            {
                public Iterator<MaterialEntityPropertyRecord> iterator()
                {
                    final LongIterator outerIt = entityIDs.iterator();
                    return new Iterator<MaterialEntityPropertyRecord>()
                        {
                            DataIterator<MaterialEntityPropertyRecord> innerIt = null;

                            public boolean hasNext()
                            {
                                while ((innerIt == null || innerIt.hasNext() == false)
                                        && outerIt.hasNext())
                                {
                                    innerIt =
                                            query.getEntityPropertyMaterialValues(outerIt
                                                    .nextLong());
                                }
                                return (innerIt != null && innerIt.isClosed() == false);
                            }

                            public MaterialEntityPropertyRecord next()
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

    public Iterable<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
            final LongSet entityIDs)
    {
        return new Iterable<VocabularyTermRecord>()
            {
                public Iterator<VocabularyTermRecord> iterator()
                {
                    final LongIterator outerIt = entityIDs.iterator();
                    return new Iterator<VocabularyTermRecord>()
                        {
                            DataIterator<VocabularyTermRecord> innerIt = null;

                            public boolean hasNext()
                            {
                                while ((innerIt == null || innerIt.hasNext() == false)
                                        && outerIt.hasNext())
                                {
                                    innerIt =
                                            query.getEntityPropertyVocabularyTermValues(outerIt
                                                    .nextLong());
                                }
                                return (innerIt != null && innerIt.isClosed() == false);
                            }

                            public VocabularyTermRecord next()
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
}
