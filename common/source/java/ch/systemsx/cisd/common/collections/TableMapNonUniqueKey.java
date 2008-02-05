/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.collections;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A table of rows of type <code>E</code> with random access via a key of type <code>K</code> where the key does not
 * have to be unique.
 * 
 * @author Franz-Josef Elmer
 * @author Bernd Rinn
 */
public class TableMapNonUniqueKey<K, E> implements Iterable<E>
{
    private final Map<K, Set<E>> map = new LinkedHashMap<K, Set<E>>();

    private final IKeyExtractor<K, E> extractor;

    /**
     * Creates a new instance for the specified rows and key extractor.
     * 
     * @param rows Collection of rows of type <code>E</code>.
     * @param extractor Strategy to extract a key of type <code>E</code> for an object of type <code>E</code>.
     */
    public TableMapNonUniqueKey(final Iterable<E> rows, final IKeyExtractor<K, E> extractor)
    {
        assert rows != null : "Unspecified collection of rows.";
        assert extractor != null : "Unspecified key extractor.";
        this.extractor = extractor;
        for (final E row : rows)
        {
            add(row);
        }
    }

    /**
     * Adds the specified row to this table. An already existing row with the same key as <code>row</code> will be
     * replaced by <code>row</code>.
     */
    public final void add(final E row)
    {
        final K key = extractor.getKey(row);
        Set<E> set = map.get(key); 
        if (set == null)
        {
            set = new LinkedHashSet<E>();
            map.put(key, set);
        }
        set.add(row);
    }

    /**
     * Gets the row set for the specified key or <code>null</code> if not found.
     */
    public final Set<E> tryGet(final K key)
    {
        return map.get(key);
    }

    /**
     * Creates an iterator of the rows in the order they have been added. Removing is not supported.
     */
    public final Iterator<E> iterator()
    {
        return new Iterator<E>()
            {
                private Iterator<Map.Entry<K, Set<E>>> mapSetIterator = map.entrySet().iterator();
                
                private Iterator<E> setIterator;

                private boolean setHasNext()
                {
                    return (setIterator != null) && setIterator.hasNext();
                }
                
                public boolean hasNext()
                {
                    if (setHasNext() == false)
                    {
                        if (mapSetIterator.hasNext())
                        {
                            setIterator = mapSetIterator.next().getValue().iterator();
                        }                        
                    }
                    return setHasNext();
                }

                public E next()
                {
                    if (setHasNext() == false)
                    {
                        throw new NoSuchElementException("No more elements.");
                    }
                    return setIterator.next();
                }

                public void remove()
                {
                    throw new UnsupportedOperationException("Can not remove an element.");
                }

            };
    }
}
