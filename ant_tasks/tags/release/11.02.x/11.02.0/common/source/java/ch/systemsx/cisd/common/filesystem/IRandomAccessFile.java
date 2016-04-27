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

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * The interface of {@link java.io.RandomAccessFile}.
 * 
 * @author Bernd Rinn
 */
public interface IRandomAccessFile extends DataInput, DataOutput, Closeable, IInputStream,
        IOutputStream
{
    //
    // RandomAccessFile
    //

    /**
     * Returns the current offset in this file.
     * 
     * @return the offset from the beginning of the file, in bytes, at which the next read or write
     *         occurs.
     * @exception IOExceptionUnchecked if an I/O error occurs.
     */
    public long getFilePointer() throws IOExceptionUnchecked;

    /**
     * Sets the file-pointer offset, measured from the beginning of this file, at which the next
     * read or write occurs. The offset may be set beyond the end of the file. Setting the offset
     * beyond the end of the file does not change the file length. The file length will change only
     * by writing after the offset has been set beyond the end of the file.
     * 
     * @param pos the offset position, measured in bytes from the beginning of the file, at which to
     *            set the file pointer.
     * @exception IOExceptionUnchecked if <code>pos</code> is less than <code>0</code> or if an I/O error
     *                occurs.
     */
    public void seek(long pos) throws IOExceptionUnchecked;

    /**
     * Returns the length of this file.
     * 
     * @return the length of this file, measured in bytes.
     * @exception IOExceptionUnchecked if an I/O error occurs.
     */
    public long length() throws IOExceptionUnchecked;

    /**
     * Sets the length of this file.
     * <p>
     * If the present length of the file as returned by the <code>length</code> method is greater
     * than the <code>newLength</code> argument then the file will be truncated. In this case, if
     * the file offset as returned by the <code>getFilePointer</code> method is greater than
     * <code>newLength</code> then after this method returns the offset will be equal to
     * <code>newLength</code>.
     * <p>
     * If the present length of the file as returned by the <code>length</code> method is smaller
     * than the <code>newLength</code> argument then the file will be extended. In this case, the
     * contents of the extended portion of the file are not defined.
     * 
     * @param newLength The desired length of the file
     * @exception IOExceptionUnchecked If an I/O error occurs
     */
    public void setLength(long newLength) throws IOExceptionUnchecked;
    
    //
    // DataInput
    //

    /**
     * @see DataInput#readFully(byte[])
     */
    public void readFully(byte b[]) throws IOExceptionUnchecked;

    /**
     * @see DataInput#readFully(byte[], int, int)
     */
    public void readFully(byte b[], int off, int len) throws IOExceptionUnchecked;

    /**
     * @see DataInput#skipBytes(int)
     */
    public int skipBytes(int n) throws IOExceptionUnchecked;

    /**
     * @see DataInput#readBoolean()
     */
    public boolean readBoolean() throws IOExceptionUnchecked;

    /**
     * @see DataInput#readByte()
     */
    public byte readByte() throws IOExceptionUnchecked;

    /**
     * @see DataInput#readUnsignedByte()
     */
    public int readUnsignedByte() throws IOExceptionUnchecked;

    /**
     * @see DataInput#readShort()
     */
    public short readShort() throws IOExceptionUnchecked;

    /**
     * @see DataInput#readUnsignedShort()
     */
    public int readUnsignedShort() throws IOExceptionUnchecked;

    /**
     * @see DataInput#readChar()
     */
    public char readChar() throws IOExceptionUnchecked;

    /**
     * @see DataInput#readInt()
     */
    public int readInt() throws IOExceptionUnchecked;

    /**
     * @see DataInput#readLong()
     */
    public long readLong() throws IOExceptionUnchecked;

    /**
     * @see DataInput#readFloat()
     */
    public float readFloat() throws IOExceptionUnchecked;

    /**
     * @see DataInput#readDouble()
     */
    public double readDouble() throws IOExceptionUnchecked;

    /**
     * @see DataInput#readLine()
     */
    public String readLine() throws IOExceptionUnchecked;

    /**
     * @see DataInput#readUTF()
     */
    public String readUTF() throws IOExceptionUnchecked;
    
    //
    // DataOutput
    //

    /**
     * @see DataOutput#write(int)
     */
    public void write(int b) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#write(byte[])
     */
    public void write(byte b[]) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#write(byte[], int, int)
     */
    public void write(byte b[], int off, int len) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#writeBoolean(boolean)
     */
    public void writeBoolean(boolean v) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#writeByte(int)
     */
    public void writeByte(int v) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#writeShort(int)
     */
    public void writeShort(int v) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#writeChar(int)
     */
    public void writeChar(int v) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#writeInt(int)
     */
    public void writeInt(int v) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#writeLong(long)
     */
    public void writeLong(long v) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#writeFloat(float)
     */
    public void writeFloat(float v) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#writeDouble(double)
     */
    public void writeDouble(double v) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#writeBytes(String)
     */
    public void writeBytes(String s) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#writeChars(String)
     */
    public void writeChars(String s) throws IOExceptionUnchecked;

    /**
     * @see DataOutput#writeUTF(String)
     */
    public void writeUTF(String str) throws IOExceptionUnchecked;
}
