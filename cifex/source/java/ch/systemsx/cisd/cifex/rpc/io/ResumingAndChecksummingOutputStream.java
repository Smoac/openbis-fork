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

    private final ISimpleChecksummingProgressListener listenerOrNull;

    private final long progressChunkSize;

    private long bytesWritten;

    private long bytesWrittenSinceListenerCalled;

    public ResumingAndChecksummingOutputStream(final File file) throws IOException
    {
        this(file, 0L, null);
    }

    public ResumingAndChecksummingOutputStream(final File file, final long progressChunkSize,
            final ISimpleChecksummingProgressListener listenerOrNull) throws IOException
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
            final ISimpleChecksummingProgressListener listenerOrNull, final long startPos,
            final int startCRC32) throws IOException, IllegalArgumentException
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
            final ISimpleChecksummingProgressListener listenerOrNull, final long startPos)
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
        try
        {
            raFile.close();
            finishProgressReport();
        } catch (IOException ex)
        {
            reportException(ex);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        try
        {
            crc32.update(b, off, len);
            updateProgress(len);
            raFile.write(b, off, len);
        } catch (IOException ex)
        {
            reportException(ex);
        }
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        try
        {
            crc32.update(b);
            updateProgress(b.length);
            raFile.write(b);
        } catch (IOException ex)
        {
            reportException(ex);
        }
    }

    @Override
    public void write(int b) throws IOException
    {
        try
        {
            crc32.update(b);
            updateProgress(1);
            raFile.write(b);
        } catch (IOException ex)
        {
            reportException(ex);
        }
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

    private void finishProgressReport()
    {
        if (bytesWrittenSinceListenerCalled > 0)
        {
            if (listenerOrNull != null)
            {
                listenerOrNull.update(bytesWritten, crc32.getIntValue());
            }
            bytesWrittenSinceListenerCalled = 0L;
        }
    }

    private void reportException(IOException e)
    {
        if (listenerOrNull != null)
        {
            listenerOrNull.exceptionThrown(e);
        }
    }

}
