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

package ch.systemsx.cisd.cifex.client.application.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A table of rows of type <code>E</code> with random access via a key of type <code>K</code>.
 * 
 * @author Christian Ribeaud
 */
public class TableMap
{
    private final Map map = new HashMap();

    private final IKeyExtractor extractor;

    /**
     * Creates a new instance for the specified rows and key extractor.
     * 
     * @param rows Collection of rows of type <code>E</code>.
     * @param extractor Strategy to extract a key of type <code>E</code> for an object of type <code>E</code>.
     */
    public TableMap(final Collection rows, final IKeyExtractor extractor)
    {
        assert rows != null : "Unspecified collection of rows.";
        assert extractor != null : "Unspecified key extractor.";
        this.extractor = extractor;
        for (Iterator iterator = rows.iterator(); iterator.hasNext();)
        {
            add(iterator.next());

        }
    }

    /**
     * Creates a new instance for the specified rows and key extractor.
     * 
     * @param rows Collection of rows of type <code>E</code>.
     * @param extractor Strategy to extract a key of type <code>E</code> for an object of type <code>E</code>.
     */
    public TableMap(final Object[] rows, final IKeyExtractor extractor)
    {
        assert rows != null : "Unspecified collection of rows.";
        assert extractor != null : "Unspecified key extractor.";
        this.extractor = extractor;
        for (int i = 0; i < rows.length; i++)
        {
            add(rows[i]);
        }
    }

    /**
     * Adds the specified row to this table. An already existing row with the same key as <code>row</code> will be
     * replaced by <code>row</code>.
     */
    public final void add(final Object row)
    {
        map.put(extractor.getKey(row), row);
    }

    /**
     * Gets the row for the specified key or <code>null</code> if not found.
     */
    public final Object tryToGet(final Object key)
    {
        return map.get(key);
    }

    //
    // Helper classes
    //

    /**
     * Interface defining the role of a key extractor.
     * 
     * @author Christian Ribeaud
     */
    public interface IKeyExtractor
    {
        /**
         * Returns the key of type <code>K</code> from an entity <code>E</code>.
         */
        public Object getKey(final Object e);
    }
}
