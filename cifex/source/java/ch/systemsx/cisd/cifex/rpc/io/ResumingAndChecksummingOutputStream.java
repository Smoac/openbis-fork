/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * An {@link OutputStream} with support for resuming a streaming write, CRC32 checksum calculation
 * and write progress notification.
 * 
 * @author Bernd Rinn
 */
public final class ResumingAndChecksummingOutputStream extends OutputStream
{

    private final RandomAccessFile raFile;

    private final CloneableCRC32 crc32;

    private final IWriteProgressListener listenerOrNull;

    private final long progressChunkSize;

    private long bytesWritten;

    private long bytesWrittenSinceListenerCalled;

    public interface IWriteProgressListener
    {
        /**
         * Indicates that <var>bytesWritten</var> bytes have been written and that the content of
         * all bytes written up to now have a ckecksum of <var>crc32Value</var>.
         */
        void update(long bytesWritten, int crc32Value);
    }

    public ResumingAndChecksummingOutputStream(final File file) throws IOException
    {
        this(file, 0L, null);
    }
    
    public ResumingAndChecksummingOutputStream(final File file, final long progressChunkSize,
            final IWriteProgressListener listenerOrNull) throws IOException
    {
        this.raFile = new RandomAccessFile(file, "rw");
        if (this.raFile.length() > 0)
        {
            this.raFile.setLength(0L);
        }
        this.listenerOrNull = listenerOrNull;
        this.progressChunkSize = progressChunkSize;
        this.crc32 = new CloneableCRC32();
        this.bytesWritten = 0L;
        this.bytesWrittenSinceListenerCalled = 0L;
    }

    public ResumingAndChecksummingOutputStream(final File file, final long progressChunkSize,
            final IWriteProgressListener listenerOrNull, final long startPos, final int startCRC32)
            throws IOException, IllegalArgumentException
    {
        this.raFile = new RandomAccessFile(file, "rw");
        if (startPos > raFile.length())
        {
            throw new IllegalArgumentException(String.format(
                    "Start position %s larger than file size %s.", startPos, raFile.length()));
        }
        this.raFile.seek(startPos);
        if (this.raFile.length() > startPos)
        {
            this.raFile.setLength(startPos);
        }
        this.listenerOrNull = listenerOrNull;
        this.progressChunkSize = progressChunkSize;
        this.crc32 = new CloneableCRC32(startCRC32);
        this.bytesWritten = startPos;
        this.bytesWrittenSinceListenerCalled = 0L;
    }

    public ResumingAndChecksummingOutputStream(final File file, final long progressChunkSize,
            final IWriteProgressListener listenerOrNull, final long startPos)
            throws IOException, IllegalArgumentException
    {
        this.raFile = new RandomAccessFile(file, "rw");
        if (startPos > raFile.length())
        {
            throw new IllegalArgumentException(String.format(
                    "Start position %s larger than file size %s.", startPos, raFile.length()));
        }
        if (this.raFile.length() > startPos)
        {
            this.raFile.setLength(startPos);
        }
        this.listenerOrNull = listenerOrNull;
        this.progressChunkSize = progressChunkSize;
        this.crc32 = CRC32Utils.computeCRC32(raFile, startPos);
        this.bytesWritten = startPos;
        this.bytesWrittenSinceListenerCalled = 0L;
    }

    @Override
    public void close() throws IOException
    {
        if (bytesWrittenSinceListenerCalled > 0)
        {
            if (listenerOrNull != null)
            {
                listenerOrNull.update(bytesWritten, crc32.getIntValue());
            }
            bytesWrittenSinceListenerCalled = 0L;
        }
        raFile.close();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        crc32.update(b, off, len);
        updateProgress(len);
        raFile.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        crc32.update(b);
        updateProgress(b.length);
        raFile.write(b);
    }

    @Override
    public void write(int b) throws IOException
    {
        crc32.update(b);
        updateProgress(1);
        raFile.write(b);
    }
    
    public int getCrc32Value()
    {
        return crc32.getIntValue();
    }
    
    public long getByteCount()
    {
        return bytesWritten;
    }

    private void updateProgress(long chunkSize)
    {
        bytesWritten += chunkSize;
        bytesWrittenSinceListenerCalled += chunkSize;
        if (bytesWrittenSinceListenerCalled >= progressChunkSize)
        {
            if (listenerOrNull != null)
            {
                listenerOrNull.update(bytesWritten, crc32.getIntValue());
            }
            bytesWrittenSinceListenerCalled = 0L;
        }
    }

}
