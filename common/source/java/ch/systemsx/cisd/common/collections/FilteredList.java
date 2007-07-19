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
import java.util.List;
import java.util.ListIterator;

/**
 * Decorates another <code>List</code> to validate that all additions match a specified <code>Validator</code>.
 * <p>
 * This list exists to provide validation for the decorated list. This class is not <code>Serializable</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class FilteredList<E> extends FilteredCollection<E> implements List<E>
{

    /**
     * Constructor that filters given <code>List</code>.
     * 
     * @param list the list to decorate. Must not be <code>null</code>
     * @param validator the <code>Validator</code> to use for validation. Must not be <code>null</code>
     */
    protected FilteredList(List<E> list, Validator<E> validator)
    {
        super(list, validator);
    }

    /**
     * Factory method to create a filtered (validating) list.
     * <p>
     * If there are any elements already in the list being decorated, they are validated.
     * </p>
     * 
     * @param list the list to decorate. Must not be <code>null</code>
     * @param validator the <code>Validator</code> to use for validation. Must not be <code>null</code>
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

    public ListIterator<E> listIterator()
    {
        return getList().listIterator();
    }

    public ListIterator<E> listIterator(int i)
    {
        return getList().listIterator(i);
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

    public void add(int index, E object)
    {
        if (isValid(object))
        {
            getList().add(index, object);
        }
    }

    public boolean addAll(int index, Collection<? extends E> collection)
    {
        return getList().addAll(index, filterCollection(collection, validator));
    }
}