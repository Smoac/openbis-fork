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

import java.io.File;
import java.io.Serializable;

/**
 * A factory class for {@link IExtendedBlockingQueue}s.
 * 
 * @author Bernd Rinn
 */
public class ExtendedBlockingQueueFactory
{

    /**
     * Creates a {@link PersistentExtendedBlockingQueueDecorator} with a
     * {@link ExtendedLinkedBlockingQueue} that persists record-based.
     * 
     * @param queueFile The file to persist the queue in.
     * @param initialRecordSize The initial size of the record. If an element of the queue is larger
     *            than this, the whole queue file has to be re-written with a larger record size.
     * @param autoSync If <code>true</code>, the underlying file will be synchronized after each
     *            write operation. This is safer, but costs a lot of performance.
     */
    public static <E extends Serializable> PersistentExtendedBlockingQueueDecorator<E> createPersistRecordBased(
            File queueFile, int initialRecordSize, boolean autoSync)
    {
        final IExtendedBlockingQueue<E> queue = new ExtendedLinkedBlockingQueue<E>();
        final IQueuePersister<E> queuePersister =
                new RecordBasedQueuePersister<E>(queue, queueFile, initialRecordSize, autoSync);
        return new PersistentExtendedBlockingQueueDecorator<E>(queue, queuePersister);
    }

    /**
     * Creates a {@link PersistentExtendedBlockingQueueDecorator} with a
     * {@link ExtendedLinkedBlockingQueue} that persists record-based. (Switches off auto-sync.)
     * 
     * @param queueFile The file to persist the queue in.
     * @param initialRecordSize The initial size of the record. If an element of the queue is larger
     *            than this, the whole queue file has to be re-written with a larger record size.
     */
    public static <E extends Serializable> PersistentExtendedBlockingQueueDecorator<E> createPersistRecordBased(
            File queueFile, int initialRecordSize)
    {
        return createPersistRecordBased(queueFile, initialRecordSize, false);
    }

    /**
     * Creates a {@link PersistentExtendedBlockingQueueDecorator} with a
     * {@link ExtendedLinkedBlockingQueue} that persists record-based. (Uses default record size of
     * 32 and switches off auto-sync.)
     * 
     * @param queueFile The file to persist the queue in.
     */
    public static <E extends Serializable> PersistentExtendedBlockingQueueDecorator<E> createPersistRecordBased(
            File queueFile)
    {
        return createPersistRecordBased(queueFile,
                RecordBasedQueuePersister.DEFAULT_INITIAL_RECORD_SIZE, false);
    }

}
