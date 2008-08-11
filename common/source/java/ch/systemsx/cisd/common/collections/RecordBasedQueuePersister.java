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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * An {@link IQueuePersister} that is based on records in a file. This class uses Java serialization
 * on the queue elements and thus requires queue elements to be serializable.
 * 
 * @author Bernd Rinn
 */
public class RecordBasedQueuePersister<E> implements IQueuePersister<E>
{
    final static int DEFAULT_INITIAL_RECORD_SIZE = 32;

    private final static int HEADER_LENGTH = 3 * 4; // 3 * sizeof(int)

    private final static int RECORD_HEADER_LENGTH = 4; // sizeof(int)

    private final static int MAX_SLICK = 1000;

    private final Queue<E> queue;

    private final File queueFile;

    private final File newQueueFile;

    private final boolean autoSync;

    private int recordSize;

    private RandomAccessFile randomAccessFile;

    private int firstRecord;

    private int lastRecord;

    /**
     * Returns a list of the content of the <var>queueFile</var>.
     */
    public static <E> List<E> list(Class<E> clazz, File queueFile)
    {
        final File newQueueFile = new File(queueFile.getParentFile(), queueFile.getName() + ".new");
        final List<E> result = new ArrayList<E>();
        RandomAccessFile randomAccessFile = null;
        try
        {
            if (queueFile.exists() == false && newQueueFile.exists())
            {
                randomAccessFile = new RandomAccessFile(newQueueFile, "r");
            } else
            {
                randomAccessFile = new RandomAccessFile(queueFile, "r");
            }
            final int firstRecord = randomAccessFile.readInt();
            final int lastRecord = randomAccessFile.readInt();
            final int recordSize = randomAccessFile.readInt();
            load(randomAccessFile, result, firstRecord, lastRecord, recordSize);
        } catch (IOException ex)
        {
            return Collections.emptyList();
        } finally
        {
            if (randomAccessFile != null)
            {
                try
                {
                    randomAccessFile.close();
                } catch (IOException ex)
                {
                    // Silence
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Create a {@link RecordBasedQueuePersister} for <var>queue</var>. (Uses a default initial
     * record size of 32 and switches off auto-sync.)
     * 
     * @param queue The queue to persist.
     * @param queueFile The file to persist the queue in.
     */
    public RecordBasedQueuePersister(Queue<E> queue, File queueFile)
    {
        this(queue, queueFile, DEFAULT_INITIAL_RECORD_SIZE, false);
    }

    /**
     * Create a {@link RecordBasedQueuePersister} for <var>queue</var>.
     * 
     * @param queue The queue to persist.
     * @param queueFile The file to persist the queue in.
     * @param initialRecordSize The initial size of the record. If an element of the queue is larger
     *            than this, the whole queue file has to be re-written with a larger record size.
     * @param autoSync If <code>true</code>, the underlying file will be synchronized after each
     *            write operation. This is safer, but costs a lot of performance.
     */
    public RecordBasedQueuePersister(Queue<E> queue, File queueFile, int initialRecordSize,
            boolean autoSync)
    {
        this.queue = queue;
        this.queueFile = queueFile;
        this.newQueueFile = new File(queueFile.getParentFile(), queueFile.getName() + ".new");
        this.autoSync = autoSync;
        if (queueFile.exists() == false && newQueueFile.exists())
        {
            if (newQueueFile.renameTo(queueFile) == false)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(new IOException("Cannot rename file '"
                        + newQueueFile.getPath() + "' to '" + queueFile.getPath() + "'"));
            }
        }
        try
        {
            this.randomAccessFile = new RandomAccessFile(queueFile, "rw");
            if (randomAccessFile.length() == 0)
            {
                this.recordSize = initialRecordSize;
                writeFullHeader(randomAccessFile, firstRecord, lastRecord, initialRecordSize);
            } else
            {
                this.firstRecord = randomAccessFile.readInt();
                this.lastRecord = randomAccessFile.readInt();
                this.recordSize = randomAccessFile.readInt();
            }
            load(randomAccessFile, queue, firstRecord, lastRecord, recordSize);
            // Clean up
            if (firstRecord > 0)
            {
                persist();
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private byte[] toByteArray(E o)
    {
        final ByteArrayOutputStream ba = new ByteArrayOutputStream();
        try
        {
            final ObjectOutputStream oo = new ObjectOutputStream(ba);
            oo.writeObject(o);
            oo.close();
            return ba.toByteArray();
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private static Object objFromByteArray(byte[] data)
    {
        final ByteArrayInputStream bi = new ByteArrayInputStream(data);
        try
        {
            final ObjectInputStream oi = new ObjectInputStream(bi);
            return oi.readObject();
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private void writeHeader() throws IOException
    {
        randomAccessFile.seek(0L);
        randomAccessFile.writeInt(firstRecord);
        randomAccessFile.writeInt(lastRecord);
    }

    private static void writeFullHeader(RandomAccessFile raf, int firstRecord, int lastRecord,
            int initialRecordSize) throws IOException
    {
        raf.seek(0L);
        raf.writeInt(firstRecord);
        raf.writeInt(lastRecord);
        raf.writeInt(initialRecordSize);
    }

    private static <E> void load(RandomAccessFile randomAccessFile, Collection<E> collection,
            int firstRecord, int lastRecord, int recordSize)
    {
        int pos = HEADER_LENGTH + recordSize * firstRecord;
        for (int i = firstRecord; i < lastRecord; ++i)
        {
            try
            {
                randomAccessFile.seek(pos);
                pos += recordSize;
                final int len = randomAccessFile.readInt();
                final byte[] data = new byte[len];
                randomAccessFile.read(data, 0, len);
                deserializeAndAdd(collection, data);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> void deserializeAndAdd(Collection<E> collection, final byte[] data)
    {
        collection.add((E) objFromByteArray(data));
    }

    private static int getNewRecordSize(int oldRecordSize, int elementSize)
    {
        return oldRecordSize * (elementSize / oldRecordSize + 1);
    }

    //
    // IQueuePersister
    //

    public void persist()
    {
        primPersist(recordSize);
    }

    private void primPersist(int newRecordSize)
    {
        synchronized (queueFile)
        {
            try
            {
                randomAccessFile.close();
                final RandomAccessFile newRandomAccessFile =
                        new RandomAccessFile(newQueueFile, "rw");
                firstRecord = 0;
                lastRecord = queue.size();
                writeFullHeader(newRandomAccessFile, firstRecord, lastRecord, newRecordSize);
                int pos = HEADER_LENGTH;
                for (E elem : queue)
                {
                    newRandomAccessFile.seek(pos);
                    pos += newRecordSize;
                    final byte[] data = toByteArray(elem);
                    final int elementSize = data.length + RECORD_HEADER_LENGTH;
                    if (elementSize > newRecordSize)
                    {
                        primPersist(getNewRecordSize(newRecordSize, elementSize));
                        return;
                    }
                    newRandomAccessFile.writeInt(data.length);
                    newRandomAccessFile.write(data);
                }
                randomAccessFile = newRandomAccessFile;
                recordSize = newRecordSize;
                if (queueFile.delete() == false)
                {
                    throw new IOException("Cannot delete file '" + queueFile.getPath() + "'");
                }
                if (newQueueFile.renameTo(queueFile) == false)
                {
                    throw new IOException("Cannot rename file '" + newQueueFile.getPath()
                            + "' to '" + queueFile.getPath() + "'");
                }
                if (autoSync)
                {
                    sync();
                }
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    public void addToTail(E elem)
    {
        synchronized (queueFile)
        {
            try
            {
                randomAccessFile.seek(HEADER_LENGTH + lastRecord * recordSize);
                final byte[] data = toByteArray(elem);
                final int elementSize = data.length + RECORD_HEADER_LENGTH;
                if (elementSize > recordSize)
                {
                    primPersist(getNewRecordSize(recordSize, elementSize));
                    return;
                }
                randomAccessFile.writeInt(data.length);
                randomAccessFile.write(data);
                ++lastRecord;
                writeHeader();
                if (autoSync)
                {
                    sync();
                }
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    public void removeFromHead(E elem)
    {
        synchronized (queueFile)
        {
            try
            {
                if (firstRecord > MAX_SLICK)
                {
                    persist();
                } else
                {
                    ++firstRecord;
                    writeHeader();
                    if (autoSync)
                    {
                        sync();
                    }
                }
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    public void check() throws IllegalStateException
    {
        try
        {
            if (randomAccessFile.getFD().valid() == false)
            {
                throw new IllegalStateException("Cannot persist: file is closed.");
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void close()
    {
        synchronized (queueFile)
        {
            try
            {
                randomAccessFile.close();
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    public void sync()
    {
        try
        {
            randomAccessFile.getFD().sync();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
