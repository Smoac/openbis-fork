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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TableMap<K, E> implements Iterable<E>
{
    private final Map<K, E> map = new LinkedHashMap<K, E>();
    private final IKeyExtractor<K, E> extractor;
    
    public TableMap(Collection<E> rows, IKeyExtractor<K, E> extractor)
    {
        this.extractor = extractor;
        for (E row : rows)
        {
            add(row);
        }
    }
    
    public void add(E row)
    {
        map.put(extractor.getKey(row), row);
    }
    
    public E get(K key)
    {
        return map.get(key);
    }

    public Iterator<E> iterator()
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
