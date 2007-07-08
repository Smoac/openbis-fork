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

/**
 * Decorates another <code>Collection</code> to validate that additions match a specified <code>Validator</code>.
 * <p>
 * This collection exists to provide validation for the decorated collection.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class FilteredCollection<E> extends AbstractCollectionDecorator<E>
{

    /** The validator to use. */
    protected final Validator<E> validator;

    /**
     * Factory method to create a filtered (validating) collection.
     * <p>
     * If there are any elements already in the collection being decorated, they are not validated.
     * </p>
     * 
     * @param coll the collection to decorate, must not be <code>null</code>
     * @param validator the predicate to use for validation, must not be <code>null</code>
     * @return a new predicated collection
     */
    public static <E> Collection<E> decorate(Collection<E> coll, Validator<E> validator)
    {
        return new FilteredCollection<E>(coll, validator);
    }

    /**
     * Constructor that wraps (not copies) given <code>Collection</code>.
     * 
     * @param collection the collection to decorate, must not be <code>null</code>.
     * @param validator the predicate to use for validation, must not be <code>null</code>.
     */
    protected FilteredCollection(Collection<E> collection, Validator<E> validator)
    {
        super(collection);
        assert validator != null;

        this.validator = validator;
    }

    /**
     * Validates the object being added.
     * 
     * @param object the object being added
     */
    protected boolean isValid(E object)
    {
        return validator.isValid(object);
    }

    //
    // AbstractCollectionDecorator
    //

    @Override
    public final boolean add(E object)
    {
        if (isValid(object))
        {
            return getCollection().add(object);
        }
        return false;
    }

    @Override
    public final boolean addAll(Collection<? extends E> collection)
    {
        boolean changed = false;
        for (E e : collection)
        {
            if (add(e) && changed == false)
            {
                changed = true;
            }
        }
        return changed;
    }
}
