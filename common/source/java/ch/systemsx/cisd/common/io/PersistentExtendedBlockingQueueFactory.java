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

package ch.systemsx.cisd.common.io;

import java.io.File;
import java.io.Serializable;

import ch.systemsx.cisd.common.collection.ExtendedLinkedBlockingQueue;
import ch.systemsx.cisd.common.collection.IExtendedBlockingQueue;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * A factory class for {@link IExtendedBlockingQueue}s.
 * 
 * @author Bernd Rinn
 */
public class PersistentExtendedBlockingQueueFactory
{

    /**
     * Creates a {@link PersistentExtendedBlockingQueueDecorator} with a
     * {@link ExtendedLinkedBlockingQueue} that persists record-based.
     * 
     * @param queueFile The file to persist the queue in.
     * @param autoSync If <code>true</code>, the underlying file will be synchronized after each
     *            write operation. This is safer, but costs a lot of performance.
     */
    public static <E extends Serializable> PersistentExtendedBlockingQueueDecorator<E> createSmartQueue(
            File queueFile, boolean autoSync)
    {
        final IExtendedBlockingQueue<E> queue = new ExtendedLinkedBlockingQueue<E>();
        final IQueuePersister<E> queuePersister =
                new QueuePersister<E>(queue, queueFile, autoSync);
        return new PersistentExtendedBlockingQueueDecorator<E>(queue, queuePersister);
    }

    /**
     * Creates a {@link PersistentExtendedBlockingQueueDecorator} with a
     * {@link ExtendedLinkedBlockingQueue} that persists record-based. (Uses default record size of
     * 32 and switches off auto-sync.)
     * 
     * @param queueFile The file to persist the queue in.
     */
    public static <E extends Serializable> PersistentExtendedBlockingQueueDecorator<E> createSmartPersist(
            File queueFile)
    {
        try
        {
            return createSmartQueue(queueFile, false);
        } catch (Exception rex)
        {
            throw new EnvironmentFailureException("Could not create/restore queue file " + queueFile.getAbsolutePath(), rex);
        }
    }

}
