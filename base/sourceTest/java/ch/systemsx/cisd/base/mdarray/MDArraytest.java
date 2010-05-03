/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.base.mdarray;

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.mdarray.MDAbstractArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for {@link MDAbstractArray}.
 * 
 * @author Bernd Rinn
 */
public class MDArraytest
{

    static class TestMDArray extends MDAbstractArray<Void>
    {
        private static final long serialVersionUID = 1L;

        protected TestMDArray(int[] shape)
        {
            super(shape);
        }

        @Override
        public Void getAsObject(int... indices)
        {
            return null;
        }

        @Override
        public void setToObject(Void value, int... indices)
        {
        }

        @Override
        public int size()
        {
            return 0;
        }

        @Override
        public Object getAsFlatArray()
        {
            return null;
        }
    }

    @Test
    public void testGetLength()
    {
        assertEquals(0, MDAbstractArray.getLength(new int[]
            { 0 }));
        assertEquals(1, MDAbstractArray.getLength(new int[]
            { 1 }));
        assertEquals(15, MDAbstractArray.getLength(new int[]
            { 5, 3 }));
        assertEquals(1, MDAbstractArray.getLength(new int[]
            { 1, 1, 1 }));
        assertEquals(8, MDAbstractArray.getLength(new int[]
            { 2, 2, 2 }));
        assertEquals(2, MDAbstractArray.getLength(new int[]
            { 1, 1, 2 }));
        assertEquals(2, MDAbstractArray.getLength(new int[]
            { 1, 2, 1 }));
        assertEquals(2, MDAbstractArray.getLength(new int[]
            { 2, 1, 1 }));
        assertEquals(50, MDAbstractArray.getLength(new int[]
            { 10, 1, 5 }));
        assertEquals(50, MDAbstractArray.getLength(new long[]
            { 10, 1, 5 }));
    }

    @Test
    public void testToInt()
    {
        assertTrue(Arrays.equals(new int[]
            { 1, 2, 3 }, MDAbstractArray.toInt(new long[]
            { 1, 2, 3 })));
        assertTrue(Arrays.equals(new int[] {}, MDAbstractArray.toInt(new long[] {})));
    }

    @Test
    public void testComputeIndex()
    {
        TestMDArray array;
        array = new TestMDArray(new int[]
            { 33 });
        assertEquals(17, array.computeIndex(new int[]
            { 17 }));
        array = new TestMDArray(new int[]
            { 100, 10 });
        assertEquals(10 * 42 + 17, array.computeIndex(new int[]
            { 42, 17 }));
        array = new TestMDArray(new int[]
            { 2, 7, 3 });
        assertEquals(3 * 7 * 1 + 3 * 2 + 3, array.computeIndex(new int[]
            { 1, 2, 3 }));
    }

    @Test
    public void testComputeIndex2D()
    {
        TestMDArray array;
        array = new TestMDArray(new int[]
            { 100, 10 });
        assertEquals(array.computeIndex(new int[]
            { 5, 8, }), array.computeIndex(5, 8));
        assertEquals(array.computeIndex(new int[]
            { 9, 1, }), array.computeIndex(9, 1));
        array = new TestMDArray(new int[]
            { 101, 11 });
        assertEquals(array.computeIndex(new int[]
            { 5, 8, }), array.computeIndex(5, 8));
        assertEquals(array.computeIndex(new int[]
            { 9, 1, }), array.computeIndex(9, 1));
    }

    @Test
    public void testComputeIndex3()
    {
        TestMDArray array;
        array = new TestMDArray(new int[]
            { 100, 10, 17 });
        assertEquals(array.computeIndex(new int[]
            { 5, 8, 16 }), array.computeIndex(5, 8, 16));
        assertEquals(array.computeIndex(new int[]
            { 9, 1, 5 }), array.computeIndex(9, 1, 5));
        array = new TestMDArray(new int[]
            { 101, 11, 3 });
        assertEquals(array.computeIndex(new int[]
            { 5, 8, 0 }), array.computeIndex(5, 8, 0));
        assertEquals(array.computeIndex(new int[]
            { 9, 1, 2 }), array.computeIndex(9, 1, 2));
    }

    @Test
    public void testMDFloatArrayMatrix()
    {
        final float[][] matrix1 = new float[][]
            {
                { 1f, 2f, 3f, 4f },
                { 5f, 6f, 7f, 8f },
                { 9f, 10f, 11f, 12f } };
        final MDFloatArray array = new MDFloatArray(matrix1);
        assertEquals(2, array.rank());
        assertEquals(12, array.size());
        assertEquals(3, array.size(0));
        assertEquals(4, array.size(1));
        assertEquals(7f, array.get(1, 2));
        final float[][] matrix2 = array.toMatrix();
        assertEquals(matrix1.length, matrix2.length);
        for (int i = 0; i < matrix1.length; ++i)
        {
            assertTrue(Arrays.equals(matrix1[i], matrix2[i]));
        }
    }
}
