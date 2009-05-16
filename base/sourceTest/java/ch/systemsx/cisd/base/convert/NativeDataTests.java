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

package ch.systemsx.cisd.base.convert;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;
import ch.systemsx.cisd.base.utilities.BuildAndEnvironmentInfo;

/**
 * Test cases for {@link NativeData}.
 * 
 * @author Bernd Rinn
 */
public class NativeDataTests
{

    @SuppressWarnings("unused")
    @DataProvider(name = "getOfs")
    private Object[][] getOfs()
    {
        return new Object[][]
            {
                { 0, 0 },
                { 0, 1 },
                { 0, 2 },
                { 0, 3 },
                { 1, 0 },
                { 1, 1 },
                { 1, 2 },
                { 1, 3 },
                { 2, 0 },
                { 2, 1 },
                { 2, 2 },
                { 2, 3 },
                { 3, 0 },
                { 3, 1 },
                { 3, 2 },
                { 3, 3 }, };
    }

    @Test(dataProvider = "getOfs")
    public void testIntToByteToInt(int sourceOfs, int targetOfs)
    {
        final int sizeOfTarget = 4;
        final int[] orignalArr = new int[]
            { -1, 17, 100000, -1000000 };
        final int[] iarr = new int[sourceOfs + orignalArr.length];
        System.arraycopy(orignalArr, 0, iarr, sourceOfs, orignalArr.length);
        final byte[] barr = new byte[iarr.length * sizeOfTarget + targetOfs];
        NativeData.copyIntToByte(iarr, sourceOfs, barr, targetOfs, orignalArr.length,
                NativeData.ByteOrder.NATIVE.ordinal());
        final int[] iarr2 = new int[(barr.length - targetOfs) / sizeOfTarget];
        NativeData.copyByteToInt(barr, targetOfs, iarr2, sourceOfs, orignalArr.length,
                NativeData.ByteOrder.NATIVE.ordinal());
        assertTrue(Arrays.equals(iarr, iarr2));
    }

    @Test(dataProvider = "getOfs")
    public void testLongToByteToLong(int sourceOfs, int targetOfs)
    {
        final int sizeOfTarget = 8;
        final long[] orignalArr = new long[]
            { -1, 17, 100000, -1000000 };
        final long[] iarr = new long[sourceOfs + orignalArr.length];
        System.arraycopy(orignalArr, 0, iarr, sourceOfs, orignalArr.length);
        final byte[] barr = new byte[iarr.length * sizeOfTarget + targetOfs];
        NativeData.copyLongToByte(iarr, sourceOfs, barr, targetOfs, orignalArr.length,
                NativeData.ByteOrder.NATIVE.ordinal());
        final long[] iarr2 = new long[(barr.length - targetOfs) / sizeOfTarget];
        NativeData.copyByteToLong(barr, targetOfs, iarr2, sourceOfs, orignalArr.length,
                NativeData.ByteOrder.NATIVE.ordinal());
        assertTrue(Arrays.equals(iarr, iarr2));
    }

    @Test(dataProvider = "getOfs")
    public void testShortToByteToShort(int sourceOfs, int targetOfs)
    {
        final int sizeOfTarget = 8;
        final short[] orignalArr = new short[]
            { -1, 17, 20000, (short) -50000 };
        final short[] iarr = new short[sourceOfs + orignalArr.length];
        System.arraycopy(orignalArr, 0, iarr, sourceOfs, orignalArr.length);
        final byte[] barr = new byte[iarr.length * sizeOfTarget + targetOfs];
        NativeData.copyShortToByte(iarr, sourceOfs, barr, targetOfs, orignalArr.length,
                NativeData.ByteOrder.NATIVE.ordinal());
        final short[] iarr2 = new short[(barr.length - targetOfs) / sizeOfTarget];
        NativeData.copyByteToShort(barr, targetOfs, iarr2, sourceOfs, orignalArr.length,
                NativeData.ByteOrder.NATIVE.ordinal());
        assertTrue(Arrays.equals(iarr, iarr2));
    }

    @Test(dataProvider = "getOfs")
    public void testFloatToByteToFloat(int sourceOfs, int targetOfs)
    {
        final int sizeOfTarget = 8;
        final float[] orignalArr = new float[]
            { -1, 17, 3.14159f, -1e6f };
        final float[] iarr = new float[sourceOfs + orignalArr.length];
        System.arraycopy(orignalArr, 0, iarr, sourceOfs, orignalArr.length);
        final byte[] barr = new byte[iarr.length * sizeOfTarget + targetOfs];
        NativeData.copyFloatToByte(iarr, sourceOfs, barr, targetOfs, orignalArr.length,
                NativeData.ByteOrder.NATIVE.ordinal());
        final float[] iarr2 = new float[(barr.length - targetOfs) / sizeOfTarget];
        NativeData.copyByteToFloat(barr, targetOfs, iarr2, sourceOfs, orignalArr.length,
                NativeData.ByteOrder.NATIVE.ordinal());
        assertTrue(Arrays.equals(iarr, iarr2));
    }

    @Test(dataProvider = "getOfs")
    public void testDoubleToByteToDouble(int sourceOfs, int targetOfs)
    {
        final int sizeOfTarget = 8;
        final double[] orignalArr = new double[]
            { -1, 17, 3.14159, -1e42 };
        final double[] iarr = new double[sourceOfs + orignalArr.length];
        System.arraycopy(orignalArr, 0, iarr, sourceOfs, orignalArr.length);
        final byte[] barr = new byte[iarr.length * sizeOfTarget + targetOfs];
        NativeData.copyDoubleToByte(iarr, sourceOfs, barr, targetOfs, orignalArr.length,
                NativeData.ByteOrder.NATIVE.ordinal());
        final double[] iarr2 = new double[(barr.length - targetOfs) / sizeOfTarget];
        NativeData.copyByteToDouble(barr, targetOfs, iarr2, sourceOfs, orignalArr.length,
                NativeData.ByteOrder.NATIVE.ordinal());
        assertTrue(Arrays.equals(iarr, iarr2));
    }

    @Test
    public void testShortEndianConversion()
    {

        final short[] values = new short[]
            { 1, 2, 4, 8, 16, 256, 512 };
        final short[] convertedValuesExpected = new short[]
            { 1 << 8, 1 << 9, 1 << 10, 1 << 11, 1 << 12, 1, 2 };
        final short[] convertedValuesFound =
                NativeData.byteToShort(NativeData.shortToByte(values, ByteOrder.BIG_ENDIAN),
                        ByteOrder.LITTLE_ENDIAN);
        assertTrue(Arrays.equals(convertedValuesExpected, convertedValuesFound));
    }

    @Test
    public void testIntEndianConversion()
    {

        final int[] values = new int[]
            { 1, 2, 4, 8, 16, 256, 1 << 16 };
        final int[] convertedValuesExpected = new int[]
            { 1 << 24, 1 << 25, 1 << 26, 1 << 27, 1 << 28, 1 << 16, 256 };
        final int[] convertedValuesFound =
                NativeData.byteToInt(NativeData.intToByte(values, ByteOrder.BIG_ENDIAN),
                        ByteOrder.LITTLE_ENDIAN);
        assertTrue(Arrays.equals(convertedValuesExpected, convertedValuesFound));
    }

    @Test
    public void testLongEndianConversion()
    {

        final long[] values = new long[]
            { 1, 2, 4, 8, 16, 256, 1L << 16, 1L << 24 };
        final long[] convertedValuesExpected = new long[]
            { 1L << 56, 1L << 57, 1L << 58, 1L << 59, 1L << 60, 1L << 48, 1L << 40, 1L << 32 };
        final long[] convertedValuesFound =
                NativeData.byteToLong(NativeData.longToByte(values, ByteOrder.BIG_ENDIAN),
                        ByteOrder.LITTLE_ENDIAN);
        assertTrue(Arrays.equals(convertedValuesExpected, convertedValuesFound));
    }

    @Test
    public void testFloatLittleEndianRoundtrip()
    {

        final float[] values = new float[]
            { 1.1f, 2.2f, 3.3f, 1e-25f, 1e25f };
        final float[] convertedValuesFound =
                NativeData.byteToFloat(NativeData.floatToByte(values, ByteOrder.LITTLE_ENDIAN),
                        ByteOrder.LITTLE_ENDIAN);
        assertTrue(Arrays.equals(values, convertedValuesFound));
    }

    @Test
    public void testFloatBigEndianRoundtrip()
    {

        final float[] values = new float[]
            { 1.1f, 2.2f, 3.3f, 1e-25f, 1e25f };
        final float[] convertedValuesFound =
                NativeData.byteToFloat(NativeData.floatToByte(values, ByteOrder.BIG_ENDIAN),
                        ByteOrder.BIG_ENDIAN);
        assertTrue(Arrays.equals(values, convertedValuesFound));
    }

    @Test
    public void testDoubleLittleEndianRoundtrip()
    {

        final double[] values = new double[]
            { 1.1f, 2.2f, 3.3f, 1e-25f, 1e25f };
        final double[] convertedValuesFound =
                NativeData.byteToDouble(NativeData.doubleToByte(values, ByteOrder.LITTLE_ENDIAN),
                        ByteOrder.LITTLE_ENDIAN);
        assertTrue(Arrays.equals(values, convertedValuesFound));
    }

    @Test
    public void testDoubleBigEndianRoundtrip()
    {

        final double[] values = new double[]
            { 1.1, 2.2, 3.3, 1e-25, 1e25 };
        final double[] convertedValuesFound =
                NativeData.byteToDouble(NativeData.doubleToByte(values, ByteOrder.BIG_ENDIAN),
                        ByteOrder.BIG_ENDIAN);
        assertTrue(Arrays.equals(values, convertedValuesFound));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNPE()
    {
        NativeData.copyByteToLong(null, 0, null, 0, 0, 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIAE()
    {
        NativeData.copyByteToLong(new byte[] {}, -1, new long[] {}, 0, 0, 0);
    }

    @Test
    public void testPlatformEndiness()
    {
        final double[] values = new double[]
            { 1.1, 2.2, 3.3, 1e-200, 1e200 };
        final double[] valuesLE =
                NativeData.byteToDouble(NativeData.doubleToByte(values, ByteOrder.LITTLE_ENDIAN),
                        ByteOrder.NATIVE);
        final double[] valuesBE =
                NativeData.byteToDouble(NativeData.doubleToByte(values, ByteOrder.BIG_ENDIAN),
                        ByteOrder.NATIVE);
        if (Arrays.equals(values, valuesLE))
        {
            System.out.println("Platform is little endian.");
            assertFalse(Arrays.equals(values, valuesBE));
        }
        if (Arrays.equals(values, valuesBE))
        {
            System.out.println("Platform is big endian.");
            assertFalse(Arrays.equals(values, valuesLE));
        }
    }

    private void afterClass()
    {
    }

    private void setUp()
    {
    }

    public static void main(String[] args) throws Throwable
    {
        System.out.println(BuildAndEnvironmentInfo.INSTANCE);
        System.out.println();
        NativeData.ensureNativeLibIsLoaded();
        final NativeDataTests test = new NativeDataTests();
        try
        {
            for (Method m : NativeDataTests.class.getMethods())
            {
                final Test testAnnotation = m.getAnnotation(Test.class);
                if (testAnnotation == null || m.getParameterTypes().length > 0)
                {
                    continue;
                }
                System.out.println("Running " + m.getName());
                test.setUp();
                try
                {
                    m.invoke(test);
                } catch (InvocationTargetException wrapperThrowable)
                {
                    final Throwable th = wrapperThrowable.getCause();
                    boolean exceptionFound = false;
                    for (Class<?> expectedExClazz : testAnnotation.expectedExceptions())
                    {
                        if (expectedExClazz == th.getClass())
                        {
                            exceptionFound = true;
                            break;
                        }
                    }
                    if (exceptionFound == false)
                    {
                        throw th;
                    }
                }
            }
            System.out.println("Tests OK!");
        } finally
        {
            test.afterClass();
        }
    }

}
