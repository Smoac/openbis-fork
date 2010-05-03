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
 * A multi-dimensional array of generic type <code>T</code>.
 * 
 * @author Bernd Rinn
 */
public class MDArray<T> extends MDAbstractArray<T>
{

    private static final long serialVersionUID = 1L;

    private final T[] flattenedArray;

    /**
     * Creates an empty {@link MDArray} with the given <var>componentClass</var> and
     * <var>dimensions</var>. Convenience method if <var>dimensions</var> are available as {@code
     * long[]}.
     */
    public MDArray(Class<T> componentClass, long[] dimensions)
    {
        this(createArray(componentClass, getLength(dimensions)), toInt(dimensions), false);
    }

    /**
     * Creates a {@link MDArray} from the given {@code flattenedArray} and {@code dimensions}. It is
     * checked that the arguments are compatible. Convenience method if <var>dimensions</var> are
     * available as {@code long[]}.
     */
    public MDArray(T[] flattenedArray, long[] dimensions)
    {
        this(flattenedArray, toInt(dimensions), true);
    }

    /**
     * Creates a {@link MDArray} from the given <var>flattenedArray</var> and <var>dimensions</var>.
     * If <var>checkDimensions</var> is {@code true}, it is checked that the arguments are
     * compatible. Convenience method if <var>dimensions</var> are available as {@code long[]}.
     */
    public MDArray(T[] flattenedArray, long[] dimensions, boolean checkdimensions)
    {
        this(flattenedArray, toInt(dimensions), checkdimensions);
    }

    /**
     * Creates an empty {@link MDArray} with the given <var>componentClass</var> and
     * <var>dimensions</var>.
     */
    public MDArray(Class<T> componentClass, int[] dimensions)
    {
        this(createArray(componentClass, getLength(dimensions)), dimensions, false);
    }

    /**
     * Creates a {@link MDArray} from the given {@code flattenedArray} and {@code dimensions}. It is
     * checked that the arguments are compatible.
     */
    public MDArray(T[] flattenedArray, int[] dimensions)
    {
        this(flattenedArray, dimensions, true);
    }

    /**
     * Creates a {@link MDArray} from the given <var>flattenedArray</var> and <var>dimensions</var>.
     * If <var>checkDimensions</var> is {@code true}, it is checked that the arguments are
     * compatible.
     */
    public MDArray(T[] flattenedArray, int[] dimensions, boolean checkdimensions)
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

    @SuppressWarnings("unchecked")
    private static <V> V[] createArray(Class<V> componentClass, final int vectorLength)
    {
        final V[] value = (V[]) java.lang.reflect.Array.newInstance(componentClass, vectorLength);
        return value;
    }

    @Override
    public T getAsObject(int... indices)
    {
        return get(indices);
    }

    @Override
    public void setToObject(T value, int... indices)
    {
        set(indices, value);
    }

    @Override
    public int size()
    {
        return flattenedArray.length;
    }

    /**
     * Returns the array in flattened form. Changes to the returned object will change the
     * multi-dimensional array directly.
     */
    @Override
    public T[] getAsFlatArray()
    {
        return flattenedArray;
    }

    /**
     * Returns the value of array at the position defined by <var>indices</var>.
     */
    public T get(int... indices)
    {
        return flattenedArray[computeIndex(indices)];
    }

    /**
     * Returns the value of a one-dimensional array at the position defined by <var>index</var>.
     * <p>
     * <b>Do not call for arrays other than one-dimensional!</b>
     */
    public T get(int index)
    {
        return flattenedArray[index];
    }

    /**
     * Returns the value of a two-dimensional array at the position defined by <var>indexX</var> and
     * <var>indexY</var>.
     * <p>
     * <b>Do not call for arrays other than two-dimensional!</b>
     */
    public T get(int indexX, int indexY)
    {
        return flattenedArray[computeIndex(indexX, indexY)];
    }

    /**
     * Returns the value of a three-dimensional array at the position defined by <var>indexX</var>,
     * <var>indexY</var> and <var>indexZ</var>.
     * <p>
     * <b>Do not call for arrays other than three-dimensional!</b>
     */
    public T get(int indexX, int indexY, int indexZ)
    {
        return flattenedArray[computeIndex(indexX, indexY, indexZ)];
    }

    /**
     * Sets the <var>value</var> of array at the position defined by <var>indices</var>.
     */
    public void set(int[] indices, T value)
    {
        flattenedArray[computeIndex(indices)] = value;
    }

    /**
     * Sets the <var>value</var> of a one-dimension array at the position defined by
     * <var>index</var>.
     * <p>
     * <b>Do not call for arrays other than one-dimensional!</b>
     */
    public void set(T value, int index)
    {
        flattenedArray[index] = value;
    }

    /**
     * Sets the <var>value</var> of a two-dimensional array at the position defined by
     * <var>indexX</var> and <var>indexY</var>.
     * <p>
     * <b>Do not call for arrays other than two-dimensional!</b>
     */
    public void set(T value, int indexX, int indexY)
    {
        flattenedArray[computeIndex(indexX, indexY)] = value;
    }

    /**
     * Sets the <var>value</var> of a three-dimensional array at the position defined by
     * <var>indexX</var>, <var>indexY</var> and <var>indexZ</var>.
     * <p>
     * <b>Do not call for arrays other than three-dimensional!</b>
     */
    public void set(T value, int indexX, int indexY, int indexZ)
    {
        flattenedArray[computeIndex(indexX, indexY, indexZ)] = value;
    }

    /**
     * Returns the component type of this array.
     */
    @SuppressWarnings("unchecked")
    public Class<T> getComponentClass()
    {
        return (Class<T>) flattenedArray.getClass().getComponentType();
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
        final MDArray<T> other = toMDArray(obj);
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

    @SuppressWarnings("unchecked")
    private MDArray<T> toMDArray(Object obj)
    {
        return (MDArray<T>) obj;
    }

}
