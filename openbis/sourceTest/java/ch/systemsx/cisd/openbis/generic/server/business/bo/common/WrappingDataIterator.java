/*
 * Copyright 2017 ETH Zuerich, SIS
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

import java.util.Collection;
import java.util.Iterator;

import net.lemnik.eodsql.DataIterator;

public class WrappingDataIterator<E> implements DataIterator<E>
{
    private final Collection<E> collection;

    private final Iterator<E> iterator;

    public WrappingDataIterator(Collection<E> collection)
    {
        this.collection = collection;
        iterator = collection.iterator();
    }

    @Override
    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    @Override
    public E next()
    {
        return iterator.next();
    }

    @Override
    public void remove()
    {
        iterator.remove();
    }

    @Override
    public Iterator<E> iterator()
    {
        return collection.iterator();
    }

    @Override
    public void close()
    {
    }

    @Override
    public boolean isClosed()
    {
        return false;
    }
}