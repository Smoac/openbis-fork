/*
 * Copyright 2011 ETH Zuerich, CISD
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

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * @author Pawel Glyzewski
 */
public class SmartQueuePersister<E> implements IQueuePersister<E>
{
    final static int QUEUE_IMPLEMENTATION_MARKER = -123456789;

    private final static int HEADER_LENGTH = 3 * 4; // 3 * sizeof(int)

    private final static int RECORD_HEADER_LENGTH = 4; // sizeof(int)

    private final static int MAX_SLICK = 100000;

    private final Queue<E> queue;

    private final File queueFile;

    private final File newQueueFile;

    private final boolean autoSync;

    private RandomAccessFile randomAccessFile;

    private int firstRecord = HEADER_LENGTH;

    private int lastRecord;

    /**
     * Returns a list of the content of the <var>queueFile</var>.
     */
    @SuppressWarnings("deprecation")
    public static <E> List<E> list(Class<E> clazz, File queueFile)
    {
        final File newQueueFile = new File(queueFile.getParentFile(), queueFile.getName() + ".new");
        List<E> result = new ArrayList<E>();
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
            if (randomAccessFile.readInt() != QUEUE_IMPLEMENTATION_MARKER)
            {
                return RecordBasedQueuePersister.list(clazz, queueFile);
            } else
            {
                final int firstRecord = randomAccessFile.readInt();
                final int lastRecord = randomAccessFile.readInt();
                load(randomAccessFile, result, firstRecord, lastRecord);
            }
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

    public SmartQueuePersister(Queue<E> queue, File queueFile)
    {
        this(queue, queueFile, false);
    }

    /**
     * Create a {@link SmartQueuePersister} for <var>queue</var>.
     * 
     * @param queue The queue to persist.
     * @param queueFile The file to persist the queue in.
     * @param autoSync If <code>true</code>, the underlying file will be synchronized after each
     *            write operation. This is safer, but costs a lot of performance.
     */
    @SuppressWarnings("deprecation")
    public SmartQueuePersister(Queue<E> queue, File queueFile, boolean autoSync)
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
            if (randomAccessFile.length() >= 4
                    && randomAccessFile.readInt() != QUEUE_IMPLEMENTATION_MARKER)
            {
                RecordBasedQueuePersister<E> oldPersister =
                        new RecordBasedQueuePersister<E>(queue, queueFile);
                oldPersister.close();
                persist();
            } else
            {

                if (randomAccessFile.length() < HEADER_LENGTH)
                {
                    writeFullHeader(randomAccessFile, firstRecord, lastRecord);
                } else
                {
                    this.firstRecord = randomAccessFile.readInt();
                    if (this.firstRecord < 0)
                    {
                        this.firstRecord = HEADER_LENGTH;
                    }
                    this.lastRecord = randomAccessFile.readInt();
                    if (this.lastRecord < 0)
                    {
                        this.lastRecord = 0;
                    }
                }
                load(randomAccessFile, queue, firstRecord, lastRecord);
                // Clean up
                if (firstRecord > 0)
                {
                    persist();
                }
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
        writeFullHeader(randomAccessFile, firstRecord, lastRecord);
    }

    private static void writeFullHeader(RandomAccessFile raf, int firstRecord, int lastRecord)
            throws IOException
    {
        raf.seek(0L);
        raf.writeInt(QUEUE_IMPLEMENTATION_MARKER);
        raf.writeInt(firstRecord);
        raf.writeInt(lastRecord);
    }

    private static <E> void load(RandomAccessFile randomAccessFile, Collection<E> collection,
            int firstRecord, int lastRecord)
    {
        long pos = firstRecord;
        while (pos < lastRecord)
        {
            try
            {
                randomAccessFile.seek(pos);
                final int len = randomAccessFile.readInt();
                pos += len + RECORD_HEADER_LENGTH;
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

    //
    // IQueuePersister
    //
    public void persist()
    {
        synchronized (queueFile)
        {
            try
            {
                randomAccessFile.close();
                fillNewQueueFile();
                if (queueFile.delete() == false)
                {
                    throw new IOException("Cannot delete file '" + queueFile.getPath() + "'");
                }
                if (newQueueFile.renameTo(queueFile) == false)
                {
                    throw new IOException("Cannot rename file '" + newQueueFile.getPath()
                            + "' to '" + queueFile.getPath() + "'");
                }
                randomAccessFile = new RandomAccessFile(queueFile, "rw");
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

    private void fillNewQueueFile() throws IOException
    {
        RandomAccessFile newRandomAccessFile = null;
        try
        {
            newRandomAccessFile = new RandomAccessFile(newQueueFile, "rw");
            firstRecord = HEADER_LENGTH;
            lastRecord = -1;
            writeFullHeader(newRandomAccessFile, firstRecord, lastRecord);
            int pos = HEADER_LENGTH;
            int elementSize = 0;
            for (E elem : queue)
            {
                pos += elementSize;
                newRandomAccessFile.seek(pos);
                final byte[] data = toByteArray(elem);
                elementSize = data.length + RECORD_HEADER_LENGTH;
                newRandomAccessFile.writeInt(data.length);
                newRandomAccessFile.write(data);
            }
            lastRecord = pos + elementSize;
            writeFullHeader(newRandomAccessFile, firstRecord, lastRecord);
        } finally
        {
            if (newRandomAccessFile != null)
            {
                newRandomAccessFile.close();
            }
        }
    }

    public void addToTail(E elem)
    {
        synchronized (queueFile)
        {
            try
            {
                randomAccessFile.seek(lastRecord);
                final byte[] data = toByteArray(elem);
                final int elementSize = data.length + RECORD_HEADER_LENGTH;
                randomAccessFile.writeInt(data.length);
                randomAccessFile.write(data);
                lastRecord += elementSize;
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
                    randomAccessFile.seek(firstRecord);
                    firstRecord += randomAccessFile.readInt() + RECORD_HEADER_LENGTH;
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
        synchronized (queueFile)
        {
            try
            {
                if (randomAccessFile.getFD().valid() == false
                        || false == randomAccessFile.getChannel().isOpen())
                {
                    throw new IllegalStateException("Cannot persist: file is closed.");
                }
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
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
        synchronized (queueFile)
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
}
