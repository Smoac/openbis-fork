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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import ch.systemsx.cisd.common.filesystem.ICloseable;
import ch.systemsx.cisd.common.filesystem.ISynchronizable;

/**
 * A decorator of a {@link IExtendedBlockingQueue} that keeps the current state of the queue current
 * with a file specified in the constructor.
 * 
 * @author Bernd Rinn
 */
public class PersistentExtendedBlockingQueueDecorator<E extends Serializable> implements
        IExtendedBlockingQueue<E>, ICloseable, ISynchronizable
{

    private final IExtendedBlockingQueue<E> delegate;

    private final IQueuePersister<E> persister;

    public PersistentExtendedBlockingQueueDecorator(IExtendedBlockingQueue<E> delegate,
            IQueuePersister<E> persister)
    {
        this.delegate = delegate;
        this.persister = persister;
    }

    //
    // Closeable
    //

    public void close()
    {
        persister.close();
    }

    //
    // ISynchronizable
    //

    public void synchronize()
    {
        persister.sync();
    }

    //
    // IExtendedBlockingQueue
    //

    public boolean contains(Object o)
    {
        return delegate.contains(o);
    }

    public boolean containsAll(Collection<?> c)
    {
        return delegate.containsAll(c);
    }

    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }

    public int size()
    {
        return delegate.size();
    }

    public int remainingCapacity()
    {
        return delegate.remainingCapacity();
    }

    public Object[] toArray()
    {
        return delegate.toArray();
    }

    public <T> T[] toArray(T[] a)
    {
        return delegate.toArray(a);
    }

    public E element()
    {
        return delegate.element();
    }

    public E peek()
    {
        return delegate.peek();
    }

    public E peekWait() throws InterruptedException
    {
        return delegate.peekWait();
    }

    public E peekWait(long timeout, TimeUnit unit) throws InterruptedException
    {
        return delegate.peekWait(timeout, unit);
    }

    public E poll()
    {
        persister.check();
        final E elementOrNull = delegate.poll();
        if (elementOrNull != null)
        {
            persister.removeFromHead(elementOrNull);
        }
        return elementOrNull;
    }

    public E remove()
    {
        persister.check();
        final E element = delegate.remove();
        persister.removeFromHead(element);
        return element;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException
    {
        persister.check();
        final E elementOrNull = delegate.poll(timeout, unit);
        if (elementOrNull != null)
        {
            persister.removeFromHead(elementOrNull);
        }
        return elementOrNull;
    }

    public E take() throws InterruptedException
    {
        persister.check();
        final E element = delegate.take();
        persister.removeFromHead(element);
        return element;
    }

    public boolean add(E o)
    {
        persister.check();
        final boolean ok = delegate.add(o);
        if (ok)
        {
            persister.addToTail(o);
        }
        return ok;
    }

    public boolean offer(E o)
    {
        persister.check();
        final boolean ok = delegate.offer(o);
        if (ok)
        {
            persister.addToTail(o);
        }
        return ok;
    }

    public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException
    {
        persister.check();
        final boolean ok = delegate.offer(o, timeout, unit);
        if (ok)
        {
            persister.addToTail(o);
        }
        return ok;
    }

    public void put(E o) throws InterruptedException
    {
        persister.check();
        delegate.put(o);
        persister.addToTail(o);
    }

    public boolean addAll(Collection<? extends E> c)
    {
        persister.check();
        final boolean ok = delegate.addAll(c);
        if (ok)
        {
            persister.persist();
        }
        return ok;
    }

    public boolean remove(Object o)
    {
        persister.check();
        final boolean ok = delegate.remove(o);
        if (ok)
        {
            persister.persist();
        }
        return ok;
    }

    public boolean removeAll(Collection<?> c)
    {
        persister.check();
        final boolean ok = delegate.removeAll(c);
        if (ok)
        {
            persister.persist();
        }
        return ok;
    }

    public boolean retainAll(Collection<?> c)
    {
        persister.check();
        final boolean ok = delegate.retainAll(c);
        if (ok)
        {
            persister.persist();
        }
        return ok;
    }

    public int drainTo(Collection<? super E> c, int maxElements)
    {
        persister.check();
        final int elementsDrained = delegate.drainTo(c, maxElements);
        if (elementsDrained > 0)
        {
            persister.persist();
        }
        return elementsDrained;
    }

    public int drainTo(Collection<? super E> c)
    {
        persister.check();
        final int elementsDrained = delegate.drainTo(c);
        if (elementsDrained > 0)
        {
            persister.persist();
        }
        return elementsDrained;
    }

    public void clear()
    {
        persister.check();
        delegate.clear();
        persister.persist();
    }

    public Iterator<E> iterator()
    {
        return new Iterator<E>()
            {
                private final Iterator<E> delegateIterator = delegate.iterator();

                public boolean hasNext()
                {
                    return delegateIterator.hasNext();
                }

                public E next()
                {
                    return delegateIterator.next();
                }

                public void remove()
                {
                    persister.check();
                    delegateIterator.remove();
                    persister.persist();
                }

            };
    }

    //
    // Object
    //

    @Override
    public boolean equals(Object o)
    {
        return delegate.equals(o);
    }

    @Override
    public int hashCode()
    {
        return delegate.hashCode();
    }

}
