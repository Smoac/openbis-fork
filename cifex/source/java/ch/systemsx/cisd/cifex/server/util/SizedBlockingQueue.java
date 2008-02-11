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

package ch.systemsx.cisd.cifex.server.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A small <code>ArrayBlockingQueue</code> extension
 * 
 * @author Christian Ribeaud
 */
public final class SizedBlockingQueue<E> extends ArrayBlockingQueue<E>
{

    private static final long serialVersionUID = 1L;

    public SizedBlockingQueue(final int capacity)
    {
        super(capacity);
        assert capacity > 0 : "Capacity must be > 0.";
    }

    private final void ensureCapacity()
    {
        if (remainingCapacity() == 0)
        {
            remove();
        }
    }

    //
    // ArrayBlockingQueue
    //

    @Override
    public final void put(final E o) throws InterruptedException
    {
        synchronized (this)
        {
            ensureCapacity();
            super.put(o);
        }
    }

    @Override
    public final boolean add(final E o)
    {
        synchronized (this)
        {
            ensureCapacity();
            return super.add(o);
        }
    }

    @Override
    public final boolean offer(final E o)
    {
        synchronized (this)
        {
            ensureCapacity();
            return super.offer(o);
        }
    }

    @Override
    public final boolean offer(final E o, final long timeout, final TimeUnit unit) throws InterruptedException
    {
        throw new UnsupportedOperationException();
    }
}
