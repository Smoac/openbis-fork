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
import java.util.Map;

/**
 * A table of rows of type <code>E</code> with random access via a key of type <code>K</code>.
 * 
 * @author Franz-Josef Elmer
 */
public class TableMap<K, E> implements Iterable<E>
{
    /** Strategy on how to handle unique key constraint violations. */
    public enum UniqueKeyViolationStrategy
    {
        KEEP_FIRST, KEEP_LAST, ERROR
    }

    /**
     * Exception indicating a violation of the unique key constraint.
     */
    public static class UniqueKeyViolationException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        UniqueKeyViolationException(String msg)
        {
            super(msg);
        }
    }

    private final Map<K, E> map = new LinkedHashMap<K, E>();

    private final IKeyExtractor<K, E> extractor;

    private final UniqueKeyViolationStrategy uniqueKeyViolationStrategy;

    /**
     * Creates a new instance for the specified rows and key extractor.
     * 
     * @param rows Collection of rows of type <code>E</code>.
     * @param extractor Strategy to extract a key of type <code>E</code> for an object of type <code>E</code>.
     * @throws UniqueKeyViolationException If the keys of <var>rows</var> are not unique and a
     *             <var>uniqueKeyViolationStrategy</var> of <code>ERROR</code> has been chosen.
     */
    public TableMap(final Iterable<E> rows, final IKeyExtractor<K, E> extractor)
    {
        this(rows, extractor, UniqueKeyViolationStrategy.ERROR);
    }
    
    /**
     * Creates a new instance for the specified rows and key extractor.
     * 
     * @param rows Collection of rows of type <code>E</code>.
     * @param extractor Strategy to extract a key of type <code>E</code> for an object of type <code>E</code>.
     * @param uniqueKeyViolationStrategy Strategy to react on unique key violations. 
     * @throws UniqueKeyViolationException If the keys of <var>rows</var> are not unique and a
     *             <var>uniqueKeyViolationStrategy</var> of <code>ERROR</code> has been chosen.
     */
    public TableMap(final Iterable<E> rows, final IKeyExtractor<K, E> extractor,
            final UniqueKeyViolationStrategy uniqueKeyViolationStrategy)
    {
        assert rows != null : "Unspecified collection of rows.";
        assert extractor != null : "Unspecified key extractor.";
        assert uniqueKeyViolationStrategy != null : "Unispecified unique key violation strategy.";
        this.extractor = extractor;
        this.uniqueKeyViolationStrategy = uniqueKeyViolationStrategy;
        for (final E row : rows)
        {
            add(row);
        }
    }

    /**
     * Adds the specified row to this table. An already existing row with the same key as <code>row</code> will be
     * replaced by <code>row</code>.
     * 
     * @throws UniqueKeyViolationException If the key of <var>row</var> is already in the map and a unique key
     *             violation strategy of <code>ERROR</code> has been chosen.
     */
    public final void add(final E row)
    {
        final K key = extractor.getKey(row);
        if (map.get(key) != null)
        {
            switch (uniqueKeyViolationStrategy)
            {
                case KEEP_FIRST:
                    break;
                case KEEP_LAST:
                    map.put(key, row);
                    break;
                case ERROR:
                    throw new IllegalStateException();
            }
        }
        map.put(key, row);
    }

    /**
     * Gets the row for the specified key or <code>null</code> if not found.
     */
    public final E tryGet(final K key)
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
                private Iterator<Map.Entry<K, E>> iterator = map.entrySet().iterator();

                public boolean hasNext()
                {
                    return iterator.hasNext();
                }

                public E next()
                {
                    return iterator.next().getValue();
                }

                public void remove()
                {
                    throw new UnsupportedOperationException("Can not remove an element.");
                }

            };
    }
}
