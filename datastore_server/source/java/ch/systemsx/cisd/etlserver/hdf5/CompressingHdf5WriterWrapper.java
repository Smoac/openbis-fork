/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.hdf5;

import java.util.BitSet;
import java.util.Date;
import java.util.List;

import ncsa.hdf.hdf5lib.exceptions.HDF5DatatypeInterfaceException;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5GenericStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures;
import ch.systemsx.cisd.hdf5.IHDF5SimpleWriter;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class CompressingHdf5WriterWrapper implements IHDF5SimpleWriter
{
    private final IHDF5Writer writer;

    // Store this, though I'm not yet using it
    @SuppressWarnings("unused")
    private final HDF5GenericStorageFeatures genericStorageFeatures;

    private final HDF5IntStorageFeatures intStorageFeatures;

    CompressingHdf5WriterWrapper(Hdf5Container parent, IHDF5Writer writer)
    {
        this.writer = writer;
        this.genericStorageFeatures = HDF5GenericStorageFeatures.GENERIC_DEFLATE;
        this.intStorageFeatures = HDF5IntStorageFeatures.INT_DEFLATE;
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#delete(java.lang.String)
     */
    public void delete(String objectPath)
    {
        writer.delete(objectPath);
    }

    /**
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#close()
     */
    public void close()
    {
        writer.close();
    }

    /**
     * @param objectPath
     * @param value
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeBoolean(java.lang.String, boolean)
     */
    public void writeBoolean(String objectPath, boolean value)
    {
        writer.writeBoolean(objectPath, value);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#exists(java.lang.String)
     */
    public boolean exists(String objectPath)
    {
        return writer.exists(objectPath);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#isGroup(java.lang.String)
     */
    public boolean isGroup(String objectPath)
    {
        return writer.isGroup(objectPath);
    }

    /**
     * @param objectPath
     * @param data
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeBitField(java.lang.String,
     *      java.util.BitSet)
     */
    public void writeBitField(String objectPath, BitSet data)
    {
        writer.writeBitField(objectPath, data);
    }

    /**
     * @param dataSetPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#getDataSetInformation(java.lang.String)
     */
    public HDF5DataSetInformation getDataSetInformation(String dataSetPath)
    {
        return writer.getDataSetInformation(dataSetPath);
    }

    /**
     * @param groupPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#getGroupMembers(java.lang.String)
     */
    public List<String> getGroupMembers(String groupPath)
    {
        return writer.getGroupMembers(groupPath);
    }

    /**
     * @param objectPath
     * @param data
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeByteArray(java.lang.String, byte[])
     */
    public void writeByteArray(String objectPath, byte[] data)
    {
        writer.writeByteArray(objectPath, data, intStorageFeatures);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readAsByteArray(java.lang.String)
     */
    public byte[] readAsByteArray(String objectPath)
    {
        return writer.readAsByteArray(objectPath);
    }

    /**
     * @param objectPath
     * @param value
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeInt(java.lang.String, int)
     */
    public void writeInt(String objectPath, int value)
    {
        writer.writeInt(objectPath, value);
    }

    /**
     * @param objectPath
     * @throws HDF5JavaException
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readBoolean(java.lang.String)
     */
    public boolean readBoolean(String objectPath) throws HDF5JavaException
    {
        return writer.readBoolean(objectPath);
    }

    /**
     * @param objectPath
     * @param data
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeIntArray(java.lang.String, int[])
     */
    public void writeIntArray(String objectPath, int[] data)
    {
        writer.writeIntArray(objectPath, data);
    }

    /**
     * @param objectPath
     * @throws HDF5DatatypeInterfaceException
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readBitField(java.lang.String)
     */
    public BitSet readBitField(String objectPath) throws HDF5DatatypeInterfaceException
    {
        return writer.readBitField(objectPath);
    }

    /**
     * @param objectPath
     * @param data
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeIntMatrix(java.lang.String, int[][])
     */
    public void writeIntMatrix(String objectPath, int[][] data)
    {
        writer.writeIntMatrix(objectPath, data);
    }

    /**
     * @param objectPath
     * @param value
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeLong(java.lang.String, long)
     */
    public void writeLong(String objectPath, long value)
    {
        writer.writeLong(objectPath, value);
    }

    /**
     * @param objectPath
     * @param data
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeLongArray(java.lang.String, long[])
     */
    public void writeLongArray(String objectPath, long[] data)
    {
        writer.writeLongArray(objectPath, data);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readInt(java.lang.String)
     */
    public int readInt(String objectPath)
    {
        return writer.readInt(objectPath);
    }

    /**
     * @param objectPath
     * @param data
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeLongMatrix(java.lang.String, long[][])
     */
    public void writeLongMatrix(String objectPath, long[][] data)
    {
        writer.writeLongMatrix(objectPath, data);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readIntArray(java.lang.String)
     */
    public int[] readIntArray(String objectPath)
    {
        return writer.readIntArray(objectPath);
    }

    /**
     * @param objectPath
     * @param value
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeFloat(java.lang.String, float)
     */
    public void writeFloat(String objectPath, float value)
    {
        writer.writeFloat(objectPath, value);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readIntMatrix(java.lang.String)
     */
    public int[][] readIntMatrix(String objectPath)
    {
        return writer.readIntMatrix(objectPath);
    }

    /**
     * @param objectPath
     * @param data
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeFloatArray(java.lang.String, float[])
     */
    public void writeFloatArray(String objectPath, float[] data)
    {
        writer.writeFloatArray(objectPath, data);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readLong(java.lang.String)
     */
    public long readLong(String objectPath)
    {
        return writer.readLong(objectPath);
    }

    /**
     * @param objectPath
     * @param data
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeFloatMatrix(java.lang.String, float[][])
     */
    public void writeFloatMatrix(String objectPath, float[][] data)
    {
        writer.writeFloatMatrix(objectPath, data);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readLongArray(java.lang.String)
     */
    public long[] readLongArray(String objectPath)
    {
        return writer.readLongArray(objectPath);
    }

    /**
     * @param objectPath
     * @param value
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeDouble(java.lang.String, double)
     */
    public void writeDouble(String objectPath, double value)
    {
        writer.writeDouble(objectPath, value);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readLongMatrix(java.lang.String)
     */
    public long[][] readLongMatrix(String objectPath)
    {
        return writer.readLongMatrix(objectPath);
    }

    /**
     * @param objectPath
     * @param data
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeDoubleArray(java.lang.String, double[])
     */
    public void writeDoubleArray(String objectPath, double[] data)
    {
        writer.writeDoubleArray(objectPath, data);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readFloat(java.lang.String)
     */
    public float readFloat(String objectPath)
    {
        return writer.readFloat(objectPath);
    }

    /**
     * @param objectPath
     * @param data
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeDoubleMatrix(java.lang.String, double[][])
     */
    public void writeDoubleMatrix(String objectPath, double[][] data)
    {
        writer.writeDoubleMatrix(objectPath, data);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readFloatArray(java.lang.String)
     */
    public float[] readFloatArray(String objectPath)
    {
        return writer.readFloatArray(objectPath);
    }

    /**
     * @param objectPath
     * @param date
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeDate(java.lang.String, java.util.Date)
     */
    public void writeDate(String objectPath, Date date)
    {
        writer.writeDate(objectPath, date);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readFloatMatrix(java.lang.String)
     */
    public float[][] readFloatMatrix(String objectPath)
    {
        return writer.readFloatMatrix(objectPath);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readDouble(java.lang.String)
     */
    public double readDouble(String objectPath)
    {
        return writer.readDouble(objectPath);
    }

    /**
     * @param objectPath
     * @param dates
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeDateArray(java.lang.String,
     *      java.util.Date[])
     */
    public void writeDateArray(String objectPath, Date[] dates)
    {
        writer.writeDateArray(objectPath, dates);
    }

    /**
     * @param objectPath
     * @param timeDuration
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeTimeDuration(java.lang.String, long)
     */
    public void writeTimeDuration(String objectPath, long timeDuration)
    {
        writer.writeTimeDuration(objectPath, timeDuration);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readDoubleArray(java.lang.String)
     */
    public double[] readDoubleArray(String objectPath)
    {
        return writer.readDoubleArray(objectPath);
    }

    /**
     * @param objectPath
     * @param timeDurations
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeTimeDurationArray(java.lang.String, long[])
     */
    public void writeTimeDurationArray(String objectPath, long[] timeDurations)
    {
        writer.writeTimeDurationArray(objectPath, timeDurations);
    }

    /**
     * @param objectPath
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readDoubleMatrix(java.lang.String)
     */
    public double[][] readDoubleMatrix(String objectPath)
    {
        return writer.readDoubleMatrix(objectPath);
    }

    /**
     * @param objectPath
     * @param data
     * @param maxLength
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeString(java.lang.String, java.lang.String,
     *      int)
     */
    public void writeString(String objectPath, String data, int maxLength)
    {
        writer.writeString(objectPath, data, maxLength);
    }

    /**
     * @param objectPath
     * @throws HDF5JavaException
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readDate(java.lang.String)
     */
    public Date readDate(String objectPath) throws HDF5JavaException
    {
        return writer.readDate(objectPath);
    }

    /**
     * @param objectPath
     * @param data
     * @param maxLength
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeStringArray(java.lang.String,
     *      java.lang.String[], int)
     */
    public void writeStringArray(String objectPath, String[] data, int maxLength)
    {
        writer.writeStringArray(objectPath, data, maxLength);
    }

    /**
     * @param objectPath
     * @throws HDF5JavaException
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readDateArray(java.lang.String)
     */
    public Date[] readDateArray(String objectPath) throws HDF5JavaException
    {
        return writer.readDateArray(objectPath);
    }

    /**
     * @param objectPath
     * @throws HDF5JavaException
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readTimeDuration(java.lang.String)
     */
    public long readTimeDuration(String objectPath) throws HDF5JavaException
    {
        return writer.readTimeDuration(objectPath);
    }

    /**
     * @param objectPath
     * @throws HDF5JavaException
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readTimeDurationArray(java.lang.String)
     */
    public long[] readTimeDurationArray(String objectPath) throws HDF5JavaException
    {
        return writer.readTimeDurationArray(objectPath);
    }

    /**
     * @param objectPath
     * @throws HDF5JavaException
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readString(java.lang.String)
     */
    public String readString(String objectPath) throws HDF5JavaException
    {
        return writer.readString(objectPath);
    }

    /**
     * @param objectPath
     * @throws HDF5JavaException
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#readStringArray(java.lang.String)
     */
    public String[] readStringArray(String objectPath) throws HDF5JavaException
    {
        return writer.readStringArray(objectPath);
    }
}
