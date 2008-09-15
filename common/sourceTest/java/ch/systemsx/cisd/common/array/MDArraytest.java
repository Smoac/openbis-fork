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

package ch.systemsx.cisd.common.array;

import java.util.Arrays;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for {@link MDArray}.
 *
 * @author Bernd Rinn
 */
public class MDArraytest
{
    
    static class TestMDArray extends MDArray<Void>
    {
        protected TestMDArray(int[] shape)
        {
            super(shape);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Void getAsObject(int[] indices)
        {
            return null;
        }

        @Override
        public void setToObject(int[] indices, Void value)
        {
        }

        @Override
        public int size()
        {
            return 0;
        }
    }

    @Test
    public void testGetLength()
    {
        assertEquals(0, MDArray.getLength(new int[] { 0 }));
        assertEquals(1, MDArray.getLength(new int[] { 1 }));
        assertEquals(15, MDArray.getLength(new int[] { 5, 3 }));
        assertEquals(1, MDArray.getLength(new int[] { 1, 1, 1 }));
        assertEquals(8, MDArray.getLength(new int[] { 2, 2, 2 }));
        assertEquals(2, MDArray.getLength(new int[] { 1, 1, 2 }));
        assertEquals(2, MDArray.getLength(new int[] { 1, 2, 1 }));
        assertEquals(2, MDArray.getLength(new int[] { 2, 1, 1 }));
        assertEquals(50, MDArray.getLength(new int[] { 10, 1, 5 }));
        assertEquals(50, MDArray.getLength(new long[] { 10, 1, 5 }));
    }
    
    @Test
    public void testToInt()
    {
        assertTrue(Arrays.equals(new int[] { 1, 2, 3 }, MDArray.toInt(new long[] { 1, 2, 3 })));
        assertTrue(Arrays.equals(new int[] { }, MDArray.toInt(new long[] { })));
    }
    
    @Test
    public void testComputeIndex()
    {
        TestMDArray array;
        array = new TestMDArray(new int[] { 33 });
        assertEquals(17, array.computeIndex(new int[] { 17 }));
        array = new TestMDArray(new int[] { 100, 10 });
        assertEquals(10 * 42 + 17, array.computeIndex(new int[] { 42, 17 }));
        array = new TestMDArray(new int[] { 2, 7, 3 });
        assertEquals(3 * 7 * 1 + 3 * 2 + 3, array.computeIndex(new int[] { 1, 2, 3 }));
    }
}
