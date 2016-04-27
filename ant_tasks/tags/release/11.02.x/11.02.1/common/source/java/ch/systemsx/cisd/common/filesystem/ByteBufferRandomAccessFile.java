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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * An implementation of {@link IRandomAccessFile} based on a {@link ByteBuffer}.
 * <p>
 * Does <i>not</i> implement {@link IRandomAccessFile#readLine()},
 * {@link IRandomAccessFile#readUTF()} and {@link IRandomAccessFile#writeUTF(String)}.
 * 
 * @author Bernd Rinn
 */
public class ByteBufferRandomAccessFile implements IRandomAccessFile
{

    private final ByteBuffer buf;

    public ByteBufferRandomAccessFile(ByteBuffer buf)
    {
        this.buf = buf;
    }

    public void readFully(byte[] b) throws IOExceptionUnchecked
    {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        buf.get(b, off, len);
    }

    public int skipBytes(int n) throws IOExceptionUnchecked
    {
        if (n <= 0)
        {
            return 0;
        }
        final int pos = buf.position();
        final int len = buf.limit();
        int newpos = pos + n;
        if (newpos > len)
        {
            newpos = len;
        }
        buf.position(newpos);
        return newpos - pos;
    }

    public void close() throws IOExceptionUnchecked
    {
        // NOOP
    }

    public int read() throws IOExceptionUnchecked
    {
        return buf.getInt();
    }

    public int read(byte[] b) throws IOExceptionUnchecked
    {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        final int bytesRead = Math.min(available(), len);
        buf.get(b, off, bytesRead);
        return bytesRead;
    }

    public long skip(long n) throws IOExceptionUnchecked
    {
        if (n <= 0)
        {
            return 0;
        }
        final int pos = buf.position();
        final int len = buf.limit();
        final int newpos = (int) Math.min(len, pos + n);
        buf.position(newpos);
        return (newpos - pos);
    }

    public int available() throws IOExceptionUnchecked
    {
        return buf.position() - buf.limit();
    }

    public void mark(int readlimit)
    {
        buf.mark();
    }

    public void reset() throws IOExceptionUnchecked
    {
        buf.reset();
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

    public long getFilePointer() throws IOExceptionUnchecked
    {
        return buf.position();
    }

    public void seek(long pos) throws IOExceptionUnchecked
    {
        buf.position((int) pos);
    }

    public long length() throws IOExceptionUnchecked
    {
        return buf.limit();
    }

    public void setLength(long newLength) throws IOExceptionUnchecked
    {
        buf.limit((int) newLength);
    }

    public boolean readBoolean() throws IOExceptionUnchecked
    {
        return buf.get() != 0;
    }

    public byte readByte() throws IOExceptionUnchecked
    {
        return buf.get();
    }

    public int readUnsignedByte() throws IOExceptionUnchecked
    {
        return buf.get() & 0xff;
    }

    public short readShort() throws IOExceptionUnchecked
    {
        return buf.getShort();
    }

    public int readUnsignedShort() throws IOExceptionUnchecked
    {
        return buf.getShort() & 0xffff;
    }

    public char readChar() throws IOExceptionUnchecked
    {
        return buf.getChar();
    }

    public int readInt() throws IOExceptionUnchecked
    {
        return buf.getInt();
    }

    public long readLong() throws IOExceptionUnchecked
    {
        return buf.getLong();
    }

    public float readFloat() throws IOExceptionUnchecked
    {
        return buf.getFloat();
    }

    public double readDouble() throws IOExceptionUnchecked
    {
        return buf.getDouble();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public String readLine() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    public String readUTF()
    {
        try
        {
            final byte[] strBuf = new byte[readUnsignedShort()];
            buf.get(strBuf);
            return new String(strBuf, "UTF-8");
        } catch (UnsupportedEncodingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void write(int b) throws IOExceptionUnchecked
    {
        buf.put((byte) b);
    }

    public void write(byte[] b) throws IOExceptionUnchecked
    {
        buf.put(b);
    }

    public void write(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        buf.put(b, off, len);
    }

    public void writeBoolean(boolean v) throws IOExceptionUnchecked
    {
        buf.put((byte) (v ? 1 : 0));
    }

    public void writeByte(int v) throws IOExceptionUnchecked
    {
        buf.put((byte) v);
    }

    public void writeShort(int v) throws IOExceptionUnchecked
    {
        buf.putShort((short) v);
    }

    public void writeChar(int v) throws IOExceptionUnchecked
    {
        buf.putChar((char) v);
    }

    public void writeInt(int v) throws IOExceptionUnchecked
    {
        buf.putInt(v);
    }

    public void writeLong(long v) throws IOExceptionUnchecked
    {
        buf.putLong(v);
    }

    public void writeFloat(float v) throws IOExceptionUnchecked
    {
        buf.putFloat(v);
    }

    public void writeDouble(double v) throws IOExceptionUnchecked
    {
        buf.putDouble(v);
    }

    public void writeBytes(String s) throws IOExceptionUnchecked
    {
        final int len = s.length();
        for (int i = 0; i < len; i++)
        {
            buf.put((byte) s.charAt(i));
        }
    }

    public void writeChars(String s) throws IOExceptionUnchecked
    {
        final int len = s.length();
        for (int i = 0; i < len; i++)
        {
            final int v = s.charAt(i);
            buf.put((byte) ((v >>> 8) & 0xFF));
            buf.put((byte) ((v >>> 0) & 0xFF));
        }
    }

    public void writeUTF(String str) throws UnsupportedOperationException
    {
        try
        {
            final byte[] strBuf = str.getBytes("UTF-8");
            writeShort(strBuf.length);
            write(strBuf);
        } catch (UnsupportedEncodingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
