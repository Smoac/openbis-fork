/*
 * Copyright 2008 ETH Zuerich, CISD
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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * A decorator for a {@link List} that does the same as
 * {@link Collections#unmodifiableList(List)} but with a 'back-door'.
 * 
 * @author Bernd Rinn
 */
public class UnmodifiableListDecorator<E> extends UnmodifiableCollectionDecorator<E> implements
        List<E>
{

    private final List<E> getList()
    {
        return (List<E>) super.getDecorated();
    }
    
    private final static class UnmodifiableListIterator<E> implements ListIterator<E>
    {
        private final ListIterator<E> iter;
        
        UnmodifiableListIterator(List<E> list, int startIndex)
        {
            iter = list.listIterator(startIndex);
        }
        public boolean hasNext()
        {
            return iter.hasNext();
        }

        public boolean hasPrevious()
        {
            return iter.hasPrevious();
        }

        public E next()
        {
            return iter.next();
        }

        public int nextIndex()
        {
            return iter.nextIndex();
        }

        public E previous()
        {
            return iter.previous();
        }

        public int previousIndex()
        {
            return iter.previousIndex();
        }

        //
        // Not supported
        //

        public void add(E o)
        {
            throw new UnsupportedOperationException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public void set(E o)
        {
            throw new UnsupportedOperationException();
        }

    }
    
    public UnmodifiableListDecorator(List<E> list)
    {
        super(list);
    }

    /**
     * Returns the decorated (wrapped) list.
     */
    @Override
    public List<E> getDecorated()
    {
        return getList();
    }

    public E get(int index)
    {
        return getList().get(index);
    }

    public int indexOf(Object o)
    {
        return getList().indexOf(o);
    }

    public int lastIndexOf(Object o)
    {
        return getList().lastIndexOf(o);
    }

    public ListIterator<E> listIterator()
    {
        return new UnmodifiableListIterator<E>(getList(), 0);
    }

    public ListIterator<E> listIterator(int index)
    {
        return new UnmodifiableListIterator<E>(getList(), index);
    }

    public List<E> subList(int fromIndex, int toIndex)
    {
        return new UnmodifiableListDecorator<E>(getList().subList(fromIndex, toIndex));
    }

    //
    // Not supported
    //

    public void add(int index, E element)
    {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int index, Collection<? extends E> c)
    {
        throw new UnsupportedOperationException();
    }

    public E remove(int index)
    {
        throw new UnsupportedOperationException();
    }

    public E set(int index, E element)
    {
        throw new UnsupportedOperationException();
    }

}
