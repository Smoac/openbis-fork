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

package ch.systemsx.cisd.common.filesystem;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * The file implementation of {@link IRandomAccessFile}.
 * 
 * @author Bernd Rinn
 */
public class RandomAccessFileImpl implements IRandomAccessFile
{

    private final RandomAccessFile raf;

    public RandomAccessFileImpl(RandomAccessFile raf)
    {
        this.raf = raf;
    }

    public final FileDescriptor getFD() throws IOException
    {
        return raf.getFD();
    }

    public final FileChannel getChannel()
    {
        return raf.getChannel();
    }

    public int read() throws IOExceptionUnchecked
    {
        try
        {
            return raf.read();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public int read(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        try
        {
            return raf.read(b, off, len);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public int read(byte[] b) throws IOExceptionUnchecked
    {
        try
        {
            return raf.read(b);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void readFully(byte[] b) throws IOExceptionUnchecked
    {
        try
        {
            raf.readFully(b);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void readFully(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        try
        {
            raf.readFully(b, off, len);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public int skipBytes(int n) throws IOExceptionUnchecked
    {
        try
        {
            return raf.skipBytes(n);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void write(int b) throws IOExceptionUnchecked
    {
        try
        {
            raf.write(b);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void write(byte[] b) throws IOExceptionUnchecked
    {
        try
        {
            raf.write(b);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void write(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        try
        {
            raf.write(b, off, len);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public long getFilePointer() throws IOExceptionUnchecked
    {
        try
        {
            return raf.getFilePointer();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void seek(long pos) throws IOExceptionUnchecked
    {
        try
        {
            raf.seek(pos);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public long length() throws IOExceptionUnchecked
    {
        try
        {
            return raf.length();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void setLength(long newLength) throws IOExceptionUnchecked
    {
        try
        {
            raf.setLength(newLength);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void close() throws IOExceptionUnchecked
    {
        try
        {
            raf.close();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final boolean readBoolean() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readBoolean();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final byte readByte() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readByte();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final int readUnsignedByte() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readUnsignedByte();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final short readShort() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readShort();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final int readUnsignedShort() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readUnsignedShort();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final char readChar() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readChar();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final int readInt() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readInt();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final long readLong() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readLong();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final float readFloat() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readFloat();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final double readDouble() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readDouble();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final String readLine() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readLine();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final String readUTF() throws IOExceptionUnchecked
    {
        try
        {
            return raf.readUTF();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void writeBoolean(boolean v) throws IOExceptionUnchecked
    {
        try
        {
            raf.writeBoolean(v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void writeByte(int v) throws IOExceptionUnchecked
    {
        try
        {
            raf.writeByte(v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void writeShort(int v) throws IOExceptionUnchecked
    {
        try
        {
            raf.writeShort(v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void writeChar(int v) throws IOExceptionUnchecked
    {
        try
        {
            raf.writeChar(v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void writeInt(int v) throws IOExceptionUnchecked
    {
        try
        {
            raf.writeInt(v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void writeLong(long v) throws IOExceptionUnchecked
    {
        try
        {
            raf.writeLong(v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void writeFloat(float v) throws IOExceptionUnchecked
    {
        try
        {
            raf.writeFloat(v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void writeDouble(double v) throws IOExceptionUnchecked
    {
        try
        {
            raf.writeDouble(v);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void writeBytes(String s) throws IOExceptionUnchecked
    {
        try
        {
            raf.writeBytes(s);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void writeChars(String s) throws IOExceptionUnchecked
    {
        try
        {
            raf.writeChars(s);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public final void writeUTF(String str) throws IOExceptionUnchecked
    {
        try
        {
            raf.writeUTF(str);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    //
    // Object
    //

    @Override
    public int hashCode()
    {
        return raf.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return raf.equals(obj);
    }

    @Override
    public String toString()
    {
        return raf.toString();
    }

    public long skip(long n) throws IOExceptionUnchecked
    {
        if (n <= 0)
        {
            return 0;
        }
        try
        {
            final long pos = raf.getFilePointer();
            final long len = raf.length();
            final long newpos = Math.min(len, pos + n);
            raf.seek(newpos);
            return (int) (newpos - pos);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public int available() throws IOExceptionUnchecked
    {
        try
        {
            return (int) Math.min(raf.length() - raf.getFilePointer(), Integer.MAX_VALUE);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private long markedPosition = -1;
    
    public void mark(int readlimit)
    {
        try
        {
            markedPosition = raf.getFilePointer();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void reset() throws IOExceptionUnchecked
    {
        if (markedPosition == -1)
        {
            throw new IOExceptionUnchecked(new IOException("mark() not called"));
        }
        try
        {
            raf.seek(markedPosition);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public boolean markSupported()
    {
        return true;
    }

    public void flush() throws IOExceptionUnchecked
    {
        // NOOP
    }

    public void synchronize() throws IOExceptionUnchecked
    {
        // NOOP
    }

}
