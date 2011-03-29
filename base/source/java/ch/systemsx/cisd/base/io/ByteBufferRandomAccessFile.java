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

package ch.systemsx.cisd.base.io;

import java.io.EOFException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.systemsx.cisd.base.convert.NativeData;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * An implementation of {@link IRandomAccessFile} based on a {@link ByteBuffer}.
 * <p>
 * Does <i>not</i> implement {@link IRandomAccessFile#readLine()}.
 * 
 * @author Bernd Rinn
 */
public class ByteBufferRandomAccessFile implements IRandomAccessFile
{

    private final ByteBuffer buf;

    private void addToLength(int newItemLen)
    {
        final int rem = buf.remaining();
        if (newItemLen > rem)
        {
            buf.limit(buf.limit() + (newItemLen - rem));
        }
    }

    /**
     * Creates a {@link IRandomAccessFile} wrapper for the given <var>buf</var>.
     * 
     * @param buf The buffer to wrap.
     * @param initialLength The initially set length (corresponds to the {@link ByteBuffer#limit()}
     *            ).
     */
    public ByteBufferRandomAccessFile(ByteBuffer buf, int initialLength)
    {
        this(buf);
        setLength(initialLength);
    }

    /**
     * Creates a {@link IRandomAccessFile} wrapper for the given <var>buf</var>. Does not change the
     * {@link ByteBuffer#limit()} of <var>buf</var>.
     * 
     * @param buf The buffer to wrap.
     */
    public ByteBufferRandomAccessFile(ByteBuffer buf)
    {
        this.buf = buf;
    }

    /**
     * Creates a {@link IRandomAccessFile} wrapper for the given <var>array</var>.
     * 
     * @param array The byte array to wrap.
     * @param initialLength The initially set length.
     */
    public ByteBufferRandomAccessFile(byte[] array, int initialLength)
    {
        this(array);
        setLength(initialLength);
    }

    /**
     * Creates a {@link IRandomAccessFile} wrapper for the given <var>array</var>. The initial
     * {@link ByteBuffer#limit()} will be <code>array.length</code>.
     * 
     * @param array The byte array to wrap.
     */
    public ByteBufferRandomAccessFile(byte[] array)
    {
        this(ByteBuffer.wrap(array));
    }

    /**
     * Creates a {@link IRandomAccessFile} wrapper for a {@link ByteBuffer} with
     * <var>capacity</var>. The initial {@link ByteBuffer#limit()} will be <code>0</code>.
     * 
     * @param capacity The maximal size of the {@link ByteBuffer}.
     */
    public ByteBufferRandomAccessFile(int capacity)
    {
        this(ByteBuffer.allocate(capacity));
        setLength(0);
    }

    public ByteOrder getByteOrder()
    {
        return buf.order();
    }

    public void setByteOrder(ByteOrder byteOrder)
    {
        buf.order(byteOrder);
    }

    public void readFully(byte[] b) throws IOExceptionUnchecked
    {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        if (available0() == -1)
        {
            throw new IOExceptionUnchecked(new EOFException());
        } else
        {
            buf.get(b, off, len);
        }
    }

    public int skipBytes(int n) throws IOExceptionUnchecked
    {
        if (n <= 0)
        {
            return 0;
        }
        final int pos = buf.position();
        final int len = buf.limit();
        final int newpos = Math.min(len, pos + n);
        buf.position(newpos);
        return (newpos - pos);
    }

    public void close() throws IOExceptionUnchecked
    {
        // NOOP
    }

    public int read() throws IOExceptionUnchecked
    {
        return buf.get() & 0xff;
    }

    public int read(byte[] b) throws IOExceptionUnchecked
    {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        final int bytesRead = Math.min(available0(), len);
        if (bytesRead < 0)
        {
            return bytesRead;
        }
        buf.get(b, off, bytesRead);
        return bytesRead;
    }

    public long skip(long n) throws IOExceptionUnchecked
    {
        if (n > Integer.MAX_VALUE)
        {
            throw new IndexOutOfBoundsException();
        }
        return skipBytes((int) n);
    }
    
    private int available0() throws IOExceptionUnchecked
    {
        return (buf.remaining() == 0) ? -1 : buf.remaining();
    }

    public int available() throws IOExceptionUnchecked
    {
        return buf.remaining();
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
        addToLength(1);
        buf.put((byte) b);
    }

    public void write(byte[] b) throws IOExceptionUnchecked
    {
        addToLength(b.length);
        buf.put(b);
    }

    public void write(byte[] b, int off, int len) throws IOExceptionUnchecked
    {
        addToLength(len);
        buf.put(b, off, len);
    }

    public void writeBoolean(boolean v) throws IOExceptionUnchecked
    {
        addToLength(1);
        buf.put((byte) (v ? 1 : 0));
    }

    public void writeByte(int v) throws IOExceptionUnchecked
    {
        write((byte) v);
    }

    public void writeShort(int v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.SHORT_SIZE);
        buf.putShort((short) v);
    }

    public void writeChar(int v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.CHAR_SIZE);
        buf.putChar((char) v);
    }

    public void writeInt(int v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.INT_SIZE);
        buf.putInt(v);
    }

    public void writeLong(long v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.LONG_SIZE);
        buf.putLong(v);
    }

    public void writeFloat(float v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.FLOAT_SIZE);
        buf.putFloat(v);
    }

    public void writeDouble(double v) throws IOExceptionUnchecked
    {
        addToLength(NativeData.DOUBLE_SIZE);
        buf.putDouble(v);
    }

    public void writeBytes(String s) throws IOExceptionUnchecked
    {
        final int len = s.length();
        addToLength(len);
        for (int i = 0; i < len; i++)
        {
            buf.put((byte) s.charAt(i));
        }
    }

    public void writeChars(String s) throws IOExceptionUnchecked
    {
        final int len = s.length();
        addToLength(NativeData.CHAR_SIZE * len);
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
            addToLength(NativeData.SHORT_SIZE + strBuf.length);
            writeShort(strBuf.length);
            write(strBuf);
        } catch (UnsupportedEncodingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
