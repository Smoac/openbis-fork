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
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Decorates another <code>List</code> to validate that all additions match a specified <code>Validator</code>.
 * <p>
 * This list exists to provide validation for the decorated list. This class is not <code>Serializable</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
// TODO 2007-07-11, Franz-Josef Elmer: This class violates the contract of List because iterator() creates a filtered 
// iterator which does not returns size() elements
public final class FilteredList<E> extends FilteredCollection<E> implements List<E>
{

    /**
     * Constructor that wraps (not copies) given <code>List</code>.
     * 
     * @param list the list to decorate, must not be <code>null</code>
     * @param validator the predicate to use for validation, must not be <code>null</code>
     */
    protected FilteredList(List<E> list, Validator<E> validator)
    {
        super(list, validator);
    }

    /**
     * Factory method to create a filtered (validating) list.
     * <p>
     * If there are any elements already in the list being decorated, they are not validated.
     * </p>
     * 
     * @param list the list to decorate, must not be <code>null</code>
     * @param validator the predicate to use for validation, must not be <code>null</code>
     */
    public static <E> List<E> decorate(List<E> list, Validator<E> validator)
    {
        return new FilteredList<E>(list, validator);
    }

    /**
     * Gets the list being decorated.
     * 
     * @return the decorated list
     */
    protected List<E> getList()
    {
        return (List<E>) getCollection();
    }

    //
    // List
    //

    public E get(int index)
    {
        return getList().get(index);
    }

    public int indexOf(Object object)
    {
        return getList().indexOf(object);
    }

    public int lastIndexOf(Object object)
    {
        return getList().lastIndexOf(object);
    }

    public E remove(int index)
    {
        return getList().remove(index);
    }

    public void add(int index, E object)
    {
        if (isValid(object))
        {
            getList().add(index, object);
        }
    }

    public boolean addAll(int index, Collection<? extends E> collection)
    {
        for (Iterator<? extends E> iter = collection.iterator(); iter.hasNext();)
        {
            E element = iter.next();
            if (isValid(element) == false)
            {
                iter.remove();
            }
        }
        return getList().addAll(index, collection);
    }
    
    public ListIterator<E> listIterator()
    {
        return listIterator(0);
    }

    public ListIterator<E> listIterator(int i)
    {
        return new FilteredListIterator(getList().listIterator(i));
    }

    public E set(int index, E object)
    {
        if (isValid(object))
        {
            return getList().set(index, object);
        }
        return null;
    }

    public List<E> subList(int fromIndex, int toIndex)
    {
        return new FilteredList<E>(getList().subList(fromIndex, toIndex), validator);
    }

     @Override
    public Iterator<E> iterator()
    {
        return listIterator();
    }

    /**
     * Inner class <code>Iterator</code> for the <code>FilteredList</code>.
     * 
     * @author Christian Ribeaud
     */
    protected class FilteredListIterator extends AbstractListIteratorDecorator<E>
    {
        /** The next object in the iteration */
        private E nextObject;

        /** Whether the next object has been calculated yet. */
        private boolean nextObjectSet = false;

        /**
         * Constructs a new <code>FilterIterator</code>.
         * 
         * @param iterator the iterator to use.
         */
        public FilteredListIterator(ListIterator<E> iterator)
        {
            super(iterator);
        }

        /**
         * Set <code>nextObject</code> to the next object. If there are no more objects then returns
         * <code>false</code>. Otherwise, returns <code>true</code>.
         */
        private boolean setNextObject()
        {
            ListIterator<E> iterator = getListIterator();
            while (iterator.hasNext())
            {
                E object = iterator.next();
                if (validator.isValid(object))
                {
                    nextObject = object;
                    nextObjectSet = true;
                    return true;
                }
            }
            return false;
        }

        //
        // AbstractListIteratorDecorator
        //

        /**
         * Returns <code>true</code> if the underlying iterator contains an object that matches the
         * <code>Validator</code>.
         * 
         * @return <code>true</code> if there is another object that matches the <code>Validator</code>.
         */
        @Override
        public final boolean hasNext()
        {
            if (nextObjectSet)
            {
                return true;
            } else
            {
                return setNextObject();
            }
        }

        @Override
        public E next()
        {
            if (nextObjectSet == false)
            {
                if (setNextObject() == false)
                {
                    throw new NoSuchElementException();
                }
            }
            nextObjectSet = false;
            return nextObject;
        }

        @Override
        public void remove()
        {
            if (nextObjectSet)
            {
                throw new IllegalStateException("remove() cannot be called");
            }
            listIterator().remove();
        }

    }
}
