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
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * An {@link InputStream} with support for resuming a streaming read, CRC32 checksum calculation and
 * read progress notification.
 * <p>
 * Optionally the stream can be appended with the CRC32 checksum. The read request needs to happen
 * in a separate call with a byte array that can hold at least 4 bytes.
 * 
 * @author Bernd Rinn
 */
public final class ResumingAndChecksummingInputStream extends InputStream
{

    public enum ChecksumHandling
    {
        COMPUTE, COMPUTE_AND_APPEND, DONT_COMPUTE;
    }

    private enum StreamState
    {
        TRANSFER_CONTENT, TRANSFER_CHECKSUM, EOD
    }

    private final RandomAccessFile raFile;

    private final ISimpleChecksummingProgressListener listenerOrNull;

    private CloneableCRC32 crc32OrNull;

    private final long progressChunkSize;

    private final long length;

    private final ChecksumHandling checksumHandling;

    private StreamState state = StreamState.TRANSFER_CONTENT;

    private long bytesRead;

    private long bytesReadSinceListenerCalled;

    public ResumingAndChecksummingInputStream(final File file) throws IOException
    {
        this(file, 0L, null, ChecksumHandling.COMPUTE);
    }

    public ResumingAndChecksummingInputStream(final File file,
            final ChecksumHandling checksumHandling) throws IOException
    {
        this(file, 0L, null, checksumHandling);
    }

    public ResumingAndChecksummingInputStream(final File file, final long startPos,
            final ChecksumHandling checksumHandling) throws IOException
    {
        this(file, 0L, null, startPos, checksumHandling);
    }

    public ResumingAndChecksummingInputStream(final File file, final long progressChunkSize,
            final ISimpleChecksummingProgressListener listenerOrNull,
            final ChecksumHandling checksumHandling) throws IOException
    {
        this.raFile = new RandomAccessFile(file, "r");
        this.length = raFile.length();
        this.listenerOrNull = listenerOrNull;
        this.progressChunkSize = progressChunkSize;
        this.checksumHandling = checksumHandling;
        setStartPos(0L);
    }

    public ResumingAndChecksummingInputStream(final File file, final long progressChunkSize,
            final ISimpleChecksummingProgressListener listenerOrNull, final long startPos,
            final ChecksumHandling checksumHandling) throws IOException, IllegalArgumentException
    {
        this.raFile = new RandomAccessFile(file, "r");
        this.length = raFile.length();
        this.listenerOrNull = listenerOrNull;
        this.progressChunkSize = progressChunkSize;
        this.checksumHandling = checksumHandling;
        setStartPos(startPos);
    }

    public ResumingAndChecksummingInputStream(final File file, final long progressChunkSize,
            final ISimpleChecksummingProgressListener listenerOrNull, final long startPos,
            final int startCRC32, final ChecksumHandling checksumHandling) throws IOException,
            IllegalArgumentException
    {
        this.raFile = new RandomAccessFile(file, "r");
        this.length = raFile.length();
        this.listenerOrNull = listenerOrNull;
        this.progressChunkSize = progressChunkSize;
        this.checksumHandling = checksumHandling;
        setStartPos(startPos, startCRC32);
    }

    @Override
    public void close() throws IOException
    {
        finishProgressReport();
        raFile.close();
    }

    @Override
    public int read() throws IOException
    {
        try
        {
            if (state != StreamState.TRANSFER_CONTENT)
            {
                return -1;
            }
            checkEndOfContent();
            if (state != StreamState.TRANSFER_CONTENT)
            {
                finishProgressReport();
                return -1;
            }
            final int b = raFile.read();
            if (b >= 0)
            {
                if (crc32OrNull != null)
                {
                    crc32OrNull.update(b);
                }
                updateProgress(1);
            } else
            {
                // unexpected - the file has grown shorter while being read
                finishProgressReport();
            }
            return b;
        } catch (IOException ex)
        {
            reportException(ex);
            throw ex;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        try
        {
            if (state == StreamState.EOD)
            {
                return -1;
            }
            if (len == 0)
            {
                return 0;
            }
            checkEndOfContent();
            if (state == StreamState.EOD)
            {
                finishProgressReport();
                return -1;
            }
            if (state == StreamState.TRANSFER_CHECKSUM)
            {
                IntConversionUtils.intToBytes(getCrc32Value(), b, off);
                state = StreamState.EOD;
                bytesReadSinceListenerCalled += 4;
                return 4;
            }
            final int actualLen = raFile.read(b, off, len);
            if (actualLen > 0)
            {
                if (crc32OrNull != null)
                {
                    crc32OrNull.update(b, off, actualLen);
                }
                updateProgress(actualLen);
            } else
            {
                // unexpected - the file has grown shorter while being read
                finishProgressReport();
            }
            return actualLen;
        } catch (IOException ex)
        {
            reportException(ex);
            throw ex;
        }
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }

    public int getCrc32Value()
    {
        return (crc32OrNull != null) ? crc32OrNull.getIntValue() : 0;
    }

    public long getLength()
    {
        return length;
    }

    public void setStartPos(final long startPos) throws IOException
    {
        if (startPos > length)
        {
            throw new IllegalArgumentException(String.format(
                    "Start position %s larger than file size %s.", startPos, length));
        }
        if (checksumHandling != ChecksumHandling.DONT_COMPUTE)
        {
            this.raFile.seek(0L);
            this.crc32OrNull = CRC32Utils.computeCRC32(raFile, startPos);
        } else
        {
            this.raFile.seek(startPos);
        }
        this.bytesRead = startPos;
        this.bytesReadSinceListenerCalled = 0L;
    }

    public void setStartPos(final long startPos, final int initialCrc32Value) throws IOException
    {
        if (startPos > length)
        {
            throw new IllegalArgumentException(String.format(
                    "Start position %s larger than file size %s.", startPos, length));
        }
        this.raFile.seek(startPos);
        if (checksumHandling != ChecksumHandling.DONT_COMPUTE)
        {
            this.crc32OrNull = new CloneableCRC32(initialCrc32Value);
        }
        this.bytesRead = startPos;
        this.bytesReadSinceListenerCalled = 0L;
    }
    
    private void reportException(IOException e)
    {
        if (listenerOrNull != null)
        {
            listenerOrNull.exceptionThrown(e);
        }
    }

    private void finishProgressReport()
    {
        if (bytesReadSinceListenerCalled > 0)
        {
            if (listenerOrNull != null)
            {
                listenerOrNull.update(bytesRead, getCrc32Value());
            }
            bytesReadSinceListenerCalled = 0L;
        }
    }

    private long updateProgress(long chunkSize)
    {
        final long actualChunkSize = Math.min(chunkSize, length - bytesRead);
        bytesRead += actualChunkSize;
        bytesReadSinceListenerCalled += actualChunkSize;
        if (bytesReadSinceListenerCalled >= progressChunkSize)
        {
            if (listenerOrNull != null)
            {
                listenerOrNull.update(bytesRead, getCrc32Value());
            }
            bytesReadSinceListenerCalled = 0L;
        }
        return actualChunkSize;
    }

    private void checkEndOfContent()
    {
        if (state == StreamState.TRANSFER_CONTENT && bytesRead == length)
        {
            state =
                    (checksumHandling == ChecksumHandling.COMPUTE_AND_APPEND) ? StreamState.TRANSFER_CHECKSUM
                            : StreamState.EOD;
        }
    }

}
