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

package ch.systemsx.cisd.base.convert;

import static ch.systemsx.cisd.base.convert.NativeData.FLOAT_SIZE;
import static ch.systemsx.cisd.base.convert.NativeData.DOUBLE_SIZE;
import static ch.systemsx.cisd.base.convert.NativeData.INT_SIZE;
import static ch.systemsx.cisd.base.convert.NativeData.LONG_SIZE;
import static ch.systemsx.cisd.base.convert.NativeData.SHORT_SIZE;

import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;

/**
 * A utility class that supports encoding and decoding of arrays of primitive number types to byte
 * arrays such that the characteristics of the number type (float or integer, byte order, element
 * size) and the dimensions are known and can be checked for correctness when converted back to the
 * number type.
 * 
 * @author Bernd Rinn
 */
public class NativeTaggedArray
{

    private final static NativeData.ByteOrder NATIVE_BYTE_ORDER = NativeData.getNativeByteOrder();

    private static final int MAGIC_SIZE = 3;

    private static final int RANK_SIZE = 1;

    private static final int RANK_INDEX = 3;

    private static final int LENGTH_SIZE = 4;

    private static final int LENGTH_INDEX = 4;

    private static final int RANK_1 = 1;

    //
    // Float
    //

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(float[] data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(float[] data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetFloatEncoding(byteOrder, (byte) FLOAT_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final int headerSize = MAGIC_SIZE + RANK_SIZE + 1 * LENGTH_SIZE;
        final byte[] byteArr = new byte[headerSize + FLOAT_SIZE * data.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = RANK_1;
        NativeData.copyIntToByte(new int[]
            { data.length }, 0, byteArr, LENGTH_INDEX, 1, byteOrder);
        NativeData.copyFloatToByte(data, 0, byteArr, headerSize, data.length, byteOrder);
        return byteArr;
    }

    /**
     * Returns the tagged array <var>data</var> as a float array or <code>null</code>, if
     * <var>data</var> is not a tagged 1D float array.
     */
    public static float[] tryToFloatArray1D(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isInteger() || encoding.getSizeInBytes() != FLOAT_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        if (rank != 1)
        {
            return null;
        }
        final int[] dimensions = new int[1];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, 1, encoding.getByteOrder());
        if (dimensions[0] * FLOAT_SIZE + LENGTH_INDEX + LENGTH_SIZE != data.length)
        {
            return null;
        }
        final float[] floatData = new float[dimensions[0]];
        NativeData.copyByteToFloat(data, LENGTH_INDEX + LENGTH_SIZE, floatData, 0,
                floatData.length, encoding.getByteOrder());
        return floatData;
    }

    //
    // Double
    //

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(double[] data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(double[] data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetFloatEncoding(byteOrder, (byte) DOUBLE_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final int headerSize = MAGIC_SIZE + RANK_SIZE + 1 * LENGTH_SIZE;
        final byte[] byteArr = new byte[headerSize + DOUBLE_SIZE * data.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = RANK_1;
        NativeData.copyIntToByte(new int[]
            { data.length }, 0, byteArr, LENGTH_INDEX, 1, byteOrder);
        NativeData.copyDoubleToByte(data, 0, byteArr, headerSize, data.length, byteOrder);
        return byteArr;
    }

    /**
     * Returns the tagged array <var>data</var> as a double array or <code>null</code>, if
     * <var>data</var> is not a tagged 1D double array.
     */
    public static double[] tryToDoubleArray1D(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isInteger() || encoding.getSizeInBytes() != DOUBLE_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        if (rank != 1)
        {
            return null;
        }
        final int[] dimensions = new int[1];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, 1, encoding.getByteOrder());
        if (dimensions[0] * DOUBLE_SIZE + LENGTH_INDEX + LENGTH_SIZE != data.length)
        {
            return null;
        }
        final double[] doubleData = new double[dimensions[0]];
        NativeData.copyByteToDouble(data, LENGTH_INDEX + LENGTH_SIZE, doubleData, 0,
                doubleData.length, encoding.getByteOrder());
        return doubleData;
    }

    //
    // Short
    //

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(short[] data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(short[] data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetIntEncoding(byteOrder, (byte) SHORT_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final int headerSize = MAGIC_SIZE + RANK_SIZE + 1 * LENGTH_SIZE;
        final byte[] byteArr = new byte[headerSize + SHORT_SIZE * data.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = RANK_1;
        NativeData.copyIntToByte(new int[]
            { data.length }, 0, byteArr, LENGTH_INDEX, 1, byteOrder);
        NativeData.copyShortToByte(data, 0, byteArr, headerSize, data.length, byteOrder);
        return byteArr;
    }

    /**
     * Returns the tagged array <var>data</var> as a short array or <code>null</code>, if
     * <var>data</var> is not a tagged 1D short array.
     */
    public static short[] tryToShortArray1D(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isFloatingPoint()
                || encoding.getSizeInBytes() != SHORT_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        if (rank != 1)
        {
            return null;
        }
        final int[] dimensions = new int[1];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, 1, encoding.getByteOrder());
        if (dimensions[0] * SHORT_SIZE + LENGTH_INDEX + LENGTH_SIZE != data.length)
        {
            return null;
        }
        final short[] shortData = new short[dimensions[0]];
        NativeData.copyByteToShort(data, LENGTH_INDEX + LENGTH_SIZE, shortData, 0,
                shortData.length, encoding.getByteOrder());
        return shortData;
    }

    //
    // Int
    //

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(int[] data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(int[] data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetIntEncoding(byteOrder, (byte) INT_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final int headerSize = MAGIC_SIZE + RANK_SIZE + 1 * LENGTH_SIZE;
        final byte[] byteArr = new byte[headerSize + INT_SIZE * data.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = RANK_1;
        NativeData.copyIntToByte(new int[]
            { data.length }, 0, byteArr, LENGTH_INDEX, 1, byteOrder);
        NativeData.copyIntToByte(data, 0, byteArr, headerSize, data.length, byteOrder);
        return byteArr;
    }

    /**
     * Returns the tagged array <var>data</var> as an int array or <code>null</code>, if
     * <var>data</var> is not a tagged 1D int array.
     */
    public static int[] tryToIntArray1D(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isFloatingPoint() || encoding.getSizeInBytes() != INT_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        if (rank != 1)
        {
            return null;
        }
        final int[] dimensions = new int[1];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, 1, encoding.getByteOrder());
        if (dimensions[0] * INT_SIZE + LENGTH_INDEX + LENGTH_SIZE != data.length)
        {
            return null;
        }
        final int[] intData = new int[dimensions[0]];
        NativeData.copyByteToInt(data, LENGTH_INDEX + LENGTH_SIZE, intData, 0, intData.length,
                encoding.getByteOrder());
        return intData;
    }

    //
    // Long
    //

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(long[] data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(long[] data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetIntEncoding(byteOrder, (byte) LONG_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final int headerSize = MAGIC_SIZE + RANK_SIZE + 1 * LENGTH_SIZE;
        final byte[] byteArr = new byte[headerSize + LONG_SIZE * data.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = RANK_1;
        NativeData.copyIntToByte(new int[]
            { data.length }, 0, byteArr, LENGTH_INDEX, 1, byteOrder);
        NativeData.copyLongToByte(data, 0, byteArr, headerSize, data.length, byteOrder);
        return byteArr;
    }

    /**
     * Returns the tagged array <var>data</var> as a long array or <code>null</code>, if
     * <var>data</var> is not a tagged 1D long array.
     */
    public static long[] tryToLongArray1D(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isFloatingPoint()
                || encoding.getSizeInBytes() != LONG_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        if (rank != 1)
        {
            return null;
        }
        final int[] dimensions = new int[1];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, 1, encoding.getByteOrder());
        if (dimensions[0] * LONG_SIZE + LENGTH_INDEX + LENGTH_SIZE != data.length)
        {
            return null;
        }
        final long[] intData = new long[dimensions[0]];
        NativeData.copyByteToLong(data, LENGTH_INDEX + LENGTH_SIZE, intData, 0, intData.length,
                encoding.getByteOrder());
        return intData;
    }

}
