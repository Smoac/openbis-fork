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

/**
 * A multi-dimensional <code>float</code> array.
 * 
 * @author Bernd Rinn
 */
public final class MDFloatArray extends MDAbstractArray<Float>
{
    private static final long serialVersionUID = 1L;
    
    private final float[] flattenedArray;

    /**
     * Creates an empty {@link MDDoubleArray} with the <var>dimensions</var>. Convenience method if
     * <var>dimensions</var> are available as {@code long[]}.
     */
    public MDFloatArray(long[] dimensions)
    {
        this(new float[getLength(dimensions)], toInt(dimensions), false);
    }

    /**
     * Creates a {@link MDDoubleArray} from the given {@code flattenedArray} and {@code dimensions}.
     * It is checked that the arguments are compatible. Convenience method if <var>dimensions</var>
     * are available as {@code long[]}.
     */
    public MDFloatArray(float[] flattenedArray, long[] dimensions)
    {
        this(flattenedArray, toInt(dimensions), true);
    }

    /**
     * Creates a {@link MDDoubleArray} from the given <var>flattenedArray</var> and
     * <var>dimensions</var>. If <var>checkDimensions</var> is {@code true}, it is checked that the
     * arguments are compatible. Convenience method if <var>dimensions</var> are available as
     * {@code long[]}.
     */
    public MDFloatArray(float[] flattenedArray, long[] dimensions, boolean checkdimensions)
    {
        this(flattenedArray, toInt(dimensions), checkdimensions);
    }

    /**
     * Creates an empty {@link MDDoubleArray} with the <var>dimensions</var>.
     */
    public MDFloatArray(int[] dimensions)
    {
        this(new float[getLength(dimensions)], dimensions, false);
    }

    /**
     * Creates a {@link MDDoubleArray} from the given {@code flattenedArray} and {@code dimensions}.
     * It is checked that the arguments are compatible.
     */
    public MDFloatArray(float[] flattenedArray, int[] dimensions)
    {
        this(flattenedArray, dimensions, true);
    }

    /**
     * Creates a {@link MDDoubleArray} from the given <var>flattenedArray</var> and
     * <var>dimensions</var>. If <var>checkDimensions</var> is {@code true}, it is checked that the
     * arguments are compatible.
     */
    public MDFloatArray(float[] flattenedArray, int[] dimensions, boolean checkdimensions)
    {
        super(dimensions);
        assert flattenedArray != null;

        if (checkdimensions)
        {
            final int expectedLength = getLength(dimensions);
            if (flattenedArray.length != expectedLength)
            {
                throw new IllegalArgumentException("Actual array length " + flattenedArray.length
                        + " does not match expected length " + expectedLength + ".");
            }
        }
        this.flattenedArray = flattenedArray;
    }

    /**
     * Creates a {@link MDFloatArray} from the given <var>matrix</var> of rank 2. Note that the
     * values in <var>matrix</var> will be copied and thus the created {@link MDFloatArray} will be
     * independent from <var>matrix</var> after construction.
     */
    public MDFloatArray(float[][] matrix)
    {
        this(matrix, getDimensions(matrix));
    }
    
    /**
     * Creates a {@link MDFloatArray} from the given <var>matrix</var> of rank 2 and the
     * <var>dimension</var> which need to be less or equal the dimensions of <var>matrix</var>. Note
     * that the values in <var>matrix</var> will be copied and thus the created
     * {@link MDFloatArray} will be independent from <var>matrix</var> after construction.
     */
    public MDFloatArray(float[][] matrix, int[] dimensions)
    {
        super(dimensions);

        final int sizeX = dimensions[0];
        final int sizeY = dimensions[1];
        int size = 1;
        for (int i = 0; i < dimensions.length; ++i)
        {
            size *= dimensions[i];
        }
        this.flattenedArray = new float[size];
        for (int i = 0; i < sizeX; ++i)
        {
            System.arraycopy(matrix[i], 0, flattenedArray, i * sizeY, sizeY);
        }
    }

    private static int[] getDimensions(float[][] matrix)
    {
        assert matrix != null;
        
        return new int[] { matrix.length, matrix.length == 0 ? 0 : matrix[0].length };
    }

    @Override
    public int size()
    {
        return flattenedArray.length;
    }

    @Override
    public Float getAsObject(int... indices)
    {
        return get(indices);
    }

    @Override
    public void setToObject(Float value, int... indices)
    {
        set(value, indices);
    }

    /**
     * Returns the array in flattened form. Changes to the returned object will change the
     * multi-dimensional array directly.
     */
    @Override
    public float[] getAsFlatArray()
    {
        return flattenedArray;
    }

    /**
     * Returns the value of array at the position defined by <var>indices</var>.
     */
    public float get(int... indices)
    {
        return flattenedArray[computeIndex(indices)];
    }
    
    /**
     * Returns the value of a one-dimensional array at the position defined by <var>index</var>.
     * <p>
     * <b>Do not call for arrays other than one-dimensional!</b>
     */
    public float get(int index)
    {
        return flattenedArray[index];
    }

    /**
     * Returns the value of a two-dimensional array at the position defined by <var>indexX</var> and
     * <var>indexY</var>.
     * <p>
     * <b>Do not call for arrays other than two-dimensional!</b>
     */
    public float get(int indexX, int indexY)
    {
        return flattenedArray[computeIndex(indexX, indexY)];
    }

    /**
     * Returns the value of a three-dimensional array at the position defined by <var>indexX</var>,
     * <var>indexY</var> and <var>indexZ</var>.
     * <p>
     * <b>Do not call for arrays other than three-dimensional!</b>
     */
    public float get(int indexX, int indexY, int indexZ)
    {
        return flattenedArray[computeIndex(indexX, indexY, indexZ)];
    }

    /**
     * Sets the <var>value</var> of array at the position defined by <var>indices</var>.
     */
    public void set(float value, int... indices)
    {
        flattenedArray[computeIndex(indices)] = value;
    }

    /**
     * Sets the <var>value</var> of a one-dimension array at the position defined by
     * <var>index</var>.
     * <p>
     * <b>Do not call for arrays other than one-dimensional!</b>
     */
    public void set(float value, int index)
    {
        flattenedArray[index] = value;
    }

    /**
     * Sets the <var>value</var> of a two-dimensional array at the position defined by
     * <var>indexX</var> and <var>indexY</var>.
     * <p>
     * <b>Do not call for arrays other than two-dimensional!</b>
     */
    public void set(float value, int indexX, int indexY)
    {
        flattenedArray[computeIndex(indexX, indexY)] = value;
    }

    /**
     * Sets the <var>value</var> of a three-dimensional array at the position defined by
     * <var>indexX</var>, <var>indexY</var> and <var>indexZ</var>.
     * <p>
     * <b>Do not call for arrays other than three-dimensional!</b>
     */
    public void set(float value, int indexX, int indexY, int indexZ)
    {
        flattenedArray[computeIndex(indexX, indexY, indexZ)] = value;
    }

    /**
     * Creates and returns a matrix from a two-dimensional array.
     * <p>
     * <b>Do not call for arrays other than two-dimensional!</b>
     */
    public float[][] toMatrix()
    {
        final int sizeX = dimensions[0];
        final int sizeY = dimensions[1];
        final float[][] result = new float[sizeX][sizeY];
        for (int i = 0; i < sizeX; ++i)
        {
            System.arraycopy(flattenedArray, i * sizeY, result[i], 0, sizeY);
        }
        return result;
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
        result = prime * result + Arrays.hashCode(dimensions);
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
        MDFloatArray other = (MDFloatArray) obj;
        if (Arrays.equals(flattenedArray, other.flattenedArray) == false)
        {
            return false;
        }
        if (Arrays.equals(dimensions, other.dimensions) == false)
        {
            return false;
        }
        return true;
    }

}
