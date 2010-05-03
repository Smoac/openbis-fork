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

import java.io.Serializable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;

/**
 * Base class of a multi-dimensional array. The <var>dimensions</var> of an array are provided
 * separately from the data as a <code>int[]</code>.
 * 
 * @author Bernd Rinn
 */
public abstract class MDAbstractArray<T> implements Serializable
{

    private static final long serialVersionUID = 1L;
    
    protected final int[] dimensions;

    protected MDAbstractArray(int[] dimensions)
    {
        assert dimensions != null;

        this.dimensions = dimensions;
    }

    /**
     * Returns the rank of the array.
     */
    public int rank()
    {
        return dimensions.length;
    }

    /**
     * Returns the extent of the array along its <var>dim</var>-th axis.
     */
    public int size(int dim)
    {
        assert dim < dimensions.length;

        return dimensions[dim];
    }

    /**
     * Returns a copy of the dimensions of the multi-dimensional array.
     */
    public int[] dimensions()
    {
        return dimensions.clone();
    }

    /**
     * Returns a copy of the dimensions of the multi-dimensional array as <code>long[]</code>.
     */
    public long[] longDimensions()
    {
        final long[] dimensionsCopy = new long[dimensions.length];
        for (int i = 0; i < dimensionsCopy.length; ++i)
        {
            dimensionsCopy[i] = dimensions[i];
        }
        return dimensionsCopy;
    }

    /**
     * Returns the number of elements in the array.
     */
    public abstract int size();

    /**
     * Return an object which has the same value as the element of the array specified by
     * <var>indices</var>.
     */
    public abstract T getAsObject(int... indices);

    /**
     * Sets the element of the array specified by <var>indices</var> to the particular
     * <var>value</var>.
     */
    public abstract void setToObject(T value, int... indices);

    /**
     * Returns the array in flattened form. Changes to the returned object will change the
     * multi-dimensional array directly.
     */
    public abstract Object getAsFlatArray();

    /**
     * Computes the linear index for the multi-dimensional <var>indices</var> provided.
     */
    protected int computeIndex(int... indices)
    {
        assert indices != null;
        assert indices.length == dimensions.length;

        int index = indices[0];
        for (int i = 1; i < indices.length; ++i)
        {
            index = index * dimensions[i] + indices[i];
        }
        return index;
    }

    /**
     * Computes the linear index for the two-dimensional (<var>indexX, indexY</var>) provided.
     */
    protected int computeIndex(int indexX, int indexY)
    {
        assert 2 == dimensions.length;

        return dimensions[1] * indexX + indexY;
    }

    /**
     * Computes the linear index for the three-dimensional (<var>indexX, indexY, indexZ</var>)
     * provided.
     */
    protected int computeIndex(int indexX, int indexY, int indexZ)
    {
        assert 3 == dimensions.length;

        return dimensions[2] * (dimensions[1] * indexX + indexY) + indexZ;
    }

    /**
     * Converts the <var>dimensions</var> from <code>long[]</code> to <code>int[]</code>.
     */
    public static int[] toInt(final long[] dimensions)
    {
        assert dimensions != null;

        final int[] result = new int[dimensions.length];
        for (int i = 0; i < result.length; ++i)
        {
            result[i] = (int) dimensions[i];
            if (result[i] != dimensions[i])
            {
                throw new IllegalArgumentException("Dimension " + i + "  is too large ("
                        + dimensions[i] + ")");
            }
        }
        return result;
    }

    /**
     * Converts the <var>dimensions</var> from <code>int[]</code> to <code>long[]</code>.
     */
    public static long[] toLong(final int[] dimensions)
    {
        assert dimensions != null;

        final long[] result = new long[dimensions.length];
        for (int i = 0; i < result.length; ++i)
        {
            result[i] = dimensions[i];
        }
        return result;
    }

    /**
     * Returns the one-dimensional length of the multi-dimensional array defined by
     * <var>dimensions</var>.
     * 
     * @throws IllegalArgumentException If <var>dimensions</var> overflow the <code>int</code> type.
     */
    public static int getLength(final int[] dimensions)
    {
        assert dimensions != null;

        if (dimensions.length == 0)
        {
            return 0;
        }
        long length = dimensions[0];
        for (int i = 1; i < dimensions.length; ++i)
        {
            length *= dimensions[i];
        }
        int intLength = (int) length;
        if (length != intLength)
        {
            throw new IllegalArgumentException("Length is too large (" + length + ")");
        }
        return intLength;
    }

    /**
     * Returns the one-dimensional length of the multi-dimensional array defined by
     * <var>dimensions</var>.
     * 
     * @throws IllegalArgumentException If <var>dimensions</var> overflow the <code>int</code> type.
     */
    public static int getLength(final long[] dimensions)
    {
        assert dimensions != null;

        if (dimensions.length == 0) // NULL data space needs to be treated differently
        {
            return 0;
        }
        long length = dimensions[0];
        for (int i = 1; i < dimensions.length; ++i)
        {
            length *= dimensions[i];
        }
        int intLength = (int) length;
        if (length != intLength)
        {
            throw new IllegalArgumentException("Length is too large (" + length + ")");
        }
        return intLength;
    }

    //
    // Object
    //
    
    @Override
    public String toString()
    {
        final int length = getLength(dimensions);
        final StringBuilder b = new StringBuilder();
        b.append(ClassUtils.getShortCanonicalName(this.getClass()));
        b.append('(');
        b.append(ArrayUtils.toString(dimensions));
        b.append(')');
        if (length <= 100)
        {
            b.append(": ");
            b.append(ArrayUtils.toString(getAsFlatArray()));
        }
        return b.toString();
    }

}
