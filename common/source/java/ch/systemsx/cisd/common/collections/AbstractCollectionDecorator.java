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

/**
 * Decorates another <code>Collection</code> to provide additional behaviour.
 * <p>
 * Each method call made on this <code>Collection</code> is forwarded to the decorated
 * <code>Collection</code>. This class is used as a framework on which to build extensions. The
 * main advantage of decoration is that one decorator can wrap any implementation of
 * <code>Collection</code>, whereas sub-classing requires a new class to be written for each
 * implementation.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractCollectionDecorator<E> implements Collection<E>
{

    /** The collection being decorated. */
    private final Collection<E> collection;

    /**
     * Constructor that wraps (not copies).
     * 
     * @param coll the collection to decorate, must not be <code>null</code>.
     */
    protected AbstractCollectionDecorator(final Collection<E> coll)
    {
        assert coll != null;
        this.collection = coll;
    }

    /**
     * Gets the collection being decorated.
     * 
     * @return the decorated collection
     */
    protected Collection<E> getCollection()
    {
        return collection;
    }

    //
    // Collection
    //

    public boolean add(final E object)
    {
        return collection.add(object);
    }

    public boolean addAll(final Collection<? extends E> coll)
    {
        return collection.addAll(coll);
    }

    public void clear()
    {
        collection.clear();
    }

    public boolean contains(final Object object)
    {
        return collection.contains(object);
    }

    public boolean isEmpty()
    {
        return collection.isEmpty();
    }

    public Iterator<E> iterator()
    {
        return collection.iterator();
    }

    public boolean remove(final Object object)
    {
        return collection.remove(object);
    }

    public int size()
    {
        return collection.size();
    }

    public Object[] toArray()
    {
        return collection.toArray();
    }

    public <T> T[] toArray(final T[] object)
    {
        return collection.toArray(object);
    }

    public boolean containsAll(final Collection<?> coll)
    {
        return collection.containsAll(coll);
    }

    public boolean removeAll(final Collection<?> coll)
    {
        return collection.removeAll(coll);
    }

    public boolean retainAll(final Collection<?> coll)
    {
        return collection.retainAll(coll);
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object object)
    {
        if (object == this)
        {
            return true;
        }
        return collection.equals(object);
    }

    @Override
    public final int hashCode()
    {
        return collection.hashCode();
    }

    @Override
    public final String toString()
    {
        return collection.toString();
    }
}
