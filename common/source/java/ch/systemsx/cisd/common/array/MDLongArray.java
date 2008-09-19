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

/**
 * A multi-dimensional <code>long</code> array.
 * 
 * @author Bernd Rinn
 */
public final class MDLongArray extends MDArray<Long>
{
    private final long[] flattenedArray;

    public MDLongArray(long[] shape)
    {
        this(new long[getLength(shape)], shape, true);
    }

    public MDLongArray(long[] flattenedArray, long[] shape)
    {
        this(flattenedArray, shape, true);
    }

    public MDLongArray(long[] flattenedArray, long[] shape, boolean checkshape)
    {
        this(flattenedArray, MDArray.toInt(shape), checkshape);
    }

    public MDLongArray(int[] shape)
    {
        this(new long[getLength(shape)], shape, true);
    }

    public MDLongArray(long[] flattenedArray, int[] shape)
    {
        this(flattenedArray, shape, true);
    }

    public MDLongArray(long[] flattenedArray, int[] shape, boolean checkshape)
    {
        super(shape);
        assert flattenedArray != null;

        if (checkshape)
        {
            final int expectedLength = getLength(shape);
            if (flattenedArray.length != expectedLength)
            {
                throw new IllegalArgumentException("Actual array length " + flattenedArray.length
                        + " does not match expected length " + expectedLength + ".");
            }
        }
        this.flattenedArray = flattenedArray;
    }

    @Override
    public int size()
    {
        return flattenedArray.length;
    }

    @Override
    public Long getAsObject(int[] indices)
    {
        return getValue(indices);
    }

    @Override
    public void setToObject(int[] indices, Long value)
    {
        setValue(indices, value);
    }

    /**
     * Returns the array in flattened form. Changes to the returned object will change the
     * multi-dimensional array directly.
     */
    public long[] getAsFlatArray()
    {
        return flattenedArray;
    }

    /**
     * Returns the value of array at the position defined by <var>indices</var>.
     */
    public long getValue(int[] indices)
    {
        return flattenedArray[computeIndex(indices)];
    }
    
    /**
     * Sets the <var>value</var> of array at the position defined by <var>indices</var>.
     */
    public void setValue(int[] indices, long value)
    {
        flattenedArray[computeIndex(indices)] = value;
    }

    //
    // Object
    //
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(flattenedArray);
        result = prime * result + Arrays.hashCode(shape);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        MDLongArray other = (MDLongArray) obj;
        if (Arrays.equals(flattenedArray, other.flattenedArray) == false)
        {
            return false;
        }
        if (Arrays.equals(shape, other.shape) == false)
        {
            return false;
        }
        return true;
    }

}
