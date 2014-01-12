/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.hcscld;

import java.util.Iterator;
import java.util.List;

import ch.systemsx.cisd.base.convert.NativeData;
import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;
import ch.systemsx.cisd.base.mdarray.MDAbstractArray;
import ch.systemsx.cisd.base.mdarray.MDArray;
import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType.EnumStorageForm;
import ch.systemsx.cisd.hdf5.hdf5lib.HDFNativeData;

/**
 * A class that represents a two dimensional matrix of HDF5 enumeration features. The first index
 * (row) represents the object, the second index (column) represents the feature.
 * 
 * @author Bernd Rinn
 */
public class HDF5EnumerationFeatureMatrix implements Iterable<MDArray<String>.ArrayEntry>
{
    private final EnumerationType[] types;

    private final int maxNumberOfTerms;

    private final int maxNumberOfBits;

    private EnumStorageForm storageForm;

    private MDByteArray bArrayOrNull;

    private MDShortArray sArrayOrNull;

    private MDIntArray iArrayOrNull;

    private static void check(EnumerationType[] types, MDAbstractArray<?> ordinalArray)
            throws IllegalArgumentException
    {
        if (ordinalArray.rank() != 2)
        {
            throw new IllegalArgumentException(
                    "HDF5EnumerationFeatureArray needs to be 2 dimensional.");
        }
        if (types.length != ordinalArray.dimensions()[1])
        {
            throw new IllegalArgumentException(
                    "Mismatch between number of feature columns and number of enumeration types.");
        }
    }

    /**
     * An interface representing a feature column.
     */
    public interface IFeatureColumn<T extends Enum<T>>
    {
        /**
         * Returns the feature value for the given <var>objectIndex</var>.
         */
        public T get(int objectIndex);

        /**
         * Returns the feature value for the given <var>objectIndex</var> as a string.
         */
        public String getAsString(int objectIndex);

        /**
         * Sets the feature value for <var>objectIndex</var> to <var>value</var>.
         */
        public void set(T value, int objectIndex);
    }

    /**
     * Creates an enumeration feature matrix.
     * 
     * @param types The enumeration type of this value.
     * @param ordinalArray The array of ordinal values in the <var>type</var>. Has to be one of
     *            {@link MDByteArray}, {@link MDShortArray} or {@link MDIntArray}.
     * @throws IllegalArgumentException If any of the ordinals in the <var>ordinalArray</var> is
     *             outside of the range of allowed values of the <var>type</var>.
     */
    public HDF5EnumerationFeatureMatrix(EnumerationType[] types, MDAbstractArray<?> ordinalArray)
            throws IllegalArgumentException
    {
        check(types, ordinalArray);
        this.types = types;
        this.maxNumberOfTerms = getMaxSize(types);
        this.maxNumberOfBits = getNumberOfBits(maxNumberOfTerms);
        if (ordinalArray instanceof MDByteArray)
        {
            final MDByteArray bArray = (MDByteArray) ordinalArray;
            setOrdinalArray(bArray);
        } else if (ordinalArray instanceof MDShortArray)
        {
            final MDShortArray sArray = (MDShortArray) ordinalArray;
            setOrdinalArray(sArray);
        } else if (ordinalArray instanceof MDIntArray)
        {
            final MDIntArray iArray = (MDIntArray) ordinalArray;
            setOrdinalArray(iArray);
        } else if (ordinalArray instanceof MDArray)
        {
            final MDArray<?> concreteArray = (MDArray<?>) ordinalArray;
            if (concreteArray.getAsFlatArray().getClass().getComponentType() == String.class)
            {
                @SuppressWarnings("unchecked")
                final MDArray<String> sArray = (MDArray<String>) concreteArray;
                map(sArray);
            } else if (concreteArray.getAsFlatArray().getClass().getComponentType().isEnum())
            {
                @SuppressWarnings("unchecked")
                final MDArray<Enum<?>> eArray = (MDArray<Enum<?>>) concreteArray;
                map(toString(eArray));
            } else
            {
                throw new IllegalArgumentException("Array has illegal component type "
                        + concreteArray.getAsFlatArray().getClass().getComponentType()
                                .getCanonicalName());
            }
        } else
        {
            throw new IllegalArgumentException("Array is of illegal type "
                    + ordinalArray.getClass().getCanonicalName());
        }
    }

    /**
     * Creates an empty matrix.
     */
    public HDF5EnumerationFeatureMatrix()
    {
        this.types = null;
        this.maxNumberOfTerms = 0;
        this.maxNumberOfBits = 0;
        this.storageForm = EnumStorageForm.BYTE;
        this.bArrayOrNull = new MDByteArray(new int[2]);
    }

    /**
     * Creates an enumeration feature matrix.
     * 
     * @param types The enumeration type of this value.
     * @param numberOfObjects The number of objects in this enumeration feature matrix.
     */
    public HDF5EnumerationFeatureMatrix(EnumerationType[] types, int numberOfObjects)
            throws IllegalArgumentException
    {
        this.types = types;
        this.maxNumberOfTerms = getMaxSize(types);
        this.maxNumberOfBits = getNumberOfBits(maxNumberOfTerms);
        if (storageForm == EnumStorageForm.BYTE)
        {
            setOrdinalArray(new MDByteArray(new int[] { numberOfObjects, types.length } ));
        } else if (storageForm == EnumStorageForm.SHORT)
        {
            setOrdinalArray(new MDShortArray(new int[] { numberOfObjects, types.length } ));
        } else
        {
            setOrdinalArray(new MDIntArray(new int[] { numberOfObjects, types.length } ));
        }
    }

    /**
     * Creates an enumeration feature matrix.
     * 
     * @param types The enumeration type of this value.
     * @param ordinalArray The array of ordinal values in the <var>type</var>.
     * @throws IllegalArgumentException If any of the ordinals in the <var>ordinalArray</var> is
     *             outside of the range of allowed values of the <var>type</var>.
     */
    public HDF5EnumerationFeatureMatrix(EnumerationType[] types, MDByteArray ordinalArray)
            throws IllegalArgumentException
    {
        check(types, ordinalArray);
        this.types = types;
        this.maxNumberOfTerms = getMaxSize(types);
        this.maxNumberOfBits = getNumberOfBits(maxNumberOfTerms);
        setOrdinalArray(ordinalArray);
    }

    /**
     * Creates an enumeration feature matrix.
     * 
     * @param types The enumeration type of this value.
     * @param ordinalArray The array of ordinal values in the <var>type</var>.
     * @throws IllegalArgumentException If any of the ordinals in the <var>ordinalArray</var> is
     *             outside of the range of allowed values of the <var>type</var>.
     */
    public HDF5EnumerationFeatureMatrix(EnumerationType[] types, MDShortArray ordinalArray)
            throws IllegalArgumentException
    {
        check(types, ordinalArray);
        this.types = types;
        this.maxNumberOfTerms = getMaxSize(types);
        this.maxNumberOfBits = getNumberOfBits(maxNumberOfTerms);
        setOrdinalArray(ordinalArray);
    }

    /**
     * Creates an enumeration feature matrix.
     * 
     * @param types The enumeration type of this value.
     * @param ordinalArray The array of ordinal values in the <var>type</var>.
     * @throws IllegalArgumentException If any of the ordinals in the <var>ordinalArray</var> is
     *             outside of the range of allowed values of the <var>type</var>.
     */
    public HDF5EnumerationFeatureMatrix(EnumerationType[] types, MDIntArray ordinalArray)
            throws IllegalArgumentException
    {
        check(types, ordinalArray);
        this.types = types;
        this.maxNumberOfTerms = getMaxSize(types);
        this.maxNumberOfBits = getNumberOfBits(maxNumberOfTerms);
        setOrdinalArray(ordinalArray);
    }

    private static MDArray<String> toString(MDArray<Enum<?>> valueArray)
    {
        final Enum<?>[] flatEnumArray = valueArray.getAsFlatArray();
        final MDArray<String> result = new MDArray<String>(String.class, valueArray.dimensions());
        final String[] flatStringArray = result.getAsFlatArray();
        for (int i = 0; i < flatEnumArray.length; ++i)
        {
            flatStringArray[i] = flatEnumArray[i].name();
        }
        return result;
    }

    private static int getMaxSize(EnumerationType[] types)
    {
        int maxSize = 0;
        for (EnumerationType type : types)
        {
            maxSize = Math.max(maxSize, type.getValues().size());
        }
        return maxSize;
    }

    private static byte getNumberOfBits(int numberOfTerms)
    {
        final int n = (numberOfTerms > 0) ? numberOfTerms - 1 : 0;
        // Binary search - decision tree (5 tests, rarely 6)
        return (byte) (n < 1 << 15 ? (n < 1 << 7 ? (n < 1 << 3 ? (n < 1 << 1 ? (n < 1 << 0 ? (n < 0 ? 32
                : 0)
                : 1)
                : (n < 1 << 2 ? 2 : 3))
                : (n < 1 << 5 ? (n < 1 << 4 ? 4 : 5) : (n < 1 << 6 ? 6 : 7)))
                : (n < 1 << 11 ? (n < 1 << 9 ? (n < 1 << 8 ? 8 : 9) : (n < 1 << 10 ? 10 : 11))
                        : (n < 1 << 13 ? (n < 1 << 12 ? 12 : 13) : (n < 1 << 14 ? 14 : 15))))
                : (n < 1 << 23 ? (n < 1 << 19 ? (n < 1 << 17 ? (n < 1 << 16 ? 16 : 17)
                        : (n < 1 << 18 ? 18 : 19)) : (n < 1 << 21 ? (n < 1 << 20 ? 20 : 21)
                        : (n < 1 << 22 ? 22 : 23)))
                        : (n < 1 << 27 ? (n < 1 << 25 ? (n < 1 << 24 ? 24 : 25) : (n < 1 << 26 ? 26
                                : 27)) : (n < 1 << 29 ? (n < 1 << 28 ? 28 : 29) : (n < 1 << 30 ? 30
                                : 31)))));
    }

    private void map(MDArray<String> array) throws IllegalArgumentException
    {
        final String[] flatArray = array.getAsFlatArray();
        if (maxNumberOfTerms < Byte.MAX_VALUE)
        {
            storageForm = EnumStorageForm.BYTE;
            bArrayOrNull = new MDByteArray(array.dimensions());
            final byte[] flatBArray = bArrayOrNull.getAsFlatArray();
            for (int i = 0; i < flatArray.length; ++i)
            {
                final Integer indexOrNull =
                        types[i % types.length].tryGetIndexForValue(flatArray[i]);
                if (indexOrNull == null)
                {
                    throw new IllegalArgumentException("Value '" + flatArray[i]
                            + "' is not allowed for type '" + types[i % types.length].getName()
                            + "'.");
                }
                flatBArray[i] = indexOrNull.byteValue();
            }
            sArrayOrNull = null;
            iArrayOrNull = null;
        } else if (maxNumberOfTerms < Short.MAX_VALUE)
        {
            storageForm = EnumStorageForm.SHORT;
            bArrayOrNull = null;
            sArrayOrNull = new MDShortArray(array.dimensions());
            final short[] flatSArray = sArrayOrNull.getAsFlatArray();
            for (int i = 0; i < flatArray.length; ++i)
            {
                final Integer indexOrNull =
                        types[i % types.length].tryGetIndexForValue(flatArray[i]);
                if (indexOrNull == null)
                {
                    throw new IllegalArgumentException("Value '" + flatArray[i]
                            + "' is not allowed for type '" + types[i % types.length].getName()
                            + "'.");
                }
                flatSArray[i] = indexOrNull.shortValue();
            }
            iArrayOrNull = null;
        } else
        {
            storageForm = EnumStorageForm.INT;
            bArrayOrNull = null;
            sArrayOrNull = null;
            iArrayOrNull = new MDIntArray(array.dimensions());
            final int[] flatIArray = iArrayOrNull.getAsFlatArray();
            for (int i = 0; i < flatIArray.length; ++i)
            {
                final Integer indexOrNull =
                        types[i % types.length].tryGetIndexForValue(flatArray[i]);
                if (indexOrNull == null)
                {
                    throw new IllegalArgumentException("Value '" + flatArray[i]
                            + "' is not allowed for type '" + types[i % types.length].getName()
                            + "'.");
                }
                flatIArray[i] = indexOrNull.intValue();
            }
        }
    }

    private void setOrdinalArray(MDByteArray array)
    {
        if (maxNumberOfTerms < Byte.MAX_VALUE)
        {
            storageForm = EnumStorageForm.BYTE;
            bArrayOrNull = array;
            checkOrdinalArray(bArrayOrNull);
            sArrayOrNull = null;
            iArrayOrNull = null;
        } else if (maxNumberOfTerms < Short.MAX_VALUE)
        {
            storageForm = EnumStorageForm.SHORT;
            bArrayOrNull = null;
            sArrayOrNull = toShortArray(array);
            checkOrdinalArray(sArrayOrNull);
            iArrayOrNull = null;
        } else
        {
            storageForm = EnumStorageForm.INT;
            bArrayOrNull = null;
            sArrayOrNull = null;
            iArrayOrNull = toIntArray(array);
            checkOrdinalArray(iArrayOrNull);
        }
    }

    private void setOrdinalArray(MDShortArray array) throws IllegalArgumentException
    {
        if (maxNumberOfTerms < Byte.MAX_VALUE)
        {
            storageForm = EnumStorageForm.BYTE;
            bArrayOrNull = toByteArray(array);
            checkOrdinalArray(bArrayOrNull);
            sArrayOrNull = null;
            iArrayOrNull = null;
        } else if (maxNumberOfTerms < Short.MAX_VALUE)
        {
            storageForm = EnumStorageForm.SHORT;
            bArrayOrNull = null;
            sArrayOrNull = array;
            checkOrdinalArray(sArrayOrNull);
            iArrayOrNull = null;
        } else
        {
            storageForm = EnumStorageForm.INT;
            bArrayOrNull = null;
            sArrayOrNull = null;
            iArrayOrNull = toIntArray(array);
            checkOrdinalArray(iArrayOrNull);
        }
    }

    private void setOrdinalArray(MDIntArray array) throws IllegalArgumentException
    {
        if (maxNumberOfTerms < Byte.MAX_VALUE)
        {
            storageForm = EnumStorageForm.BYTE;
            bArrayOrNull = toByteArray(array);
            checkOrdinalArray(bArrayOrNull);
            sArrayOrNull = null;
            iArrayOrNull = null;
        } else if (maxNumberOfTerms < Short.MAX_VALUE)
        {
            storageForm = EnumStorageForm.SHORT;
            bArrayOrNull = null;
            sArrayOrNull = toShortArray(array);
            checkOrdinalArray(sArrayOrNull);
            iArrayOrNull = null;
        } else
        {
            storageForm = EnumStorageForm.INT;
            bArrayOrNull = null;
            sArrayOrNull = null;
            iArrayOrNull = array;
            checkOrdinalArray(iArrayOrNull);
        }
    }

    private MDByteArray toByteArray(MDShortArray array) throws IllegalArgumentException
    {
        final short[] flatSourceArray = array.getAsFlatArray();
        final MDByteArray bArray = new MDByteArray(array.dimensions());
        final byte[] flatTargetArray = bArray.getAsFlatArray();
        for (int i = 0; i < flatSourceArray.length; ++i)
        {
            flatTargetArray[i] = (byte) flatSourceArray[i];
            if (flatTargetArray[i] != flatSourceArray[i])
            {
                throw new IllegalArgumentException("Value " + flatSourceArray[i]
                        + " cannot be stored in byte array");
            }
        }
        return bArray;
    }

    private MDByteArray toByteArray(MDIntArray array) throws IllegalArgumentException
    {
        final int[] flatSourceArray = array.getAsFlatArray();
        final MDByteArray bArray = new MDByteArray(array.dimensions());
        final byte[] flatTargetArray = bArray.getAsFlatArray();
        for (int i = 0; i < flatSourceArray.length; ++i)
        {
            flatTargetArray[i] = (byte) flatSourceArray[i];
            if (flatTargetArray[i] != flatSourceArray[i])
            {
                throw new IllegalArgumentException("Value " + flatSourceArray[i]
                        + " cannot be stored in byte array");
            }
        }
        return bArray;
    }

    private MDShortArray toShortArray(MDByteArray array)
    {
        final byte[] flatSourceArray = array.getAsFlatArray();
        final MDShortArray sArray = new MDShortArray(array.dimensions());
        final short[] flatTargetArray = sArray.getAsFlatArray();
        for (int i = 0; i < flatSourceArray.length; ++i)
        {
            flatTargetArray[i] = flatSourceArray[i];
        }
        return sArray;
    }

    private MDShortArray toShortArray(MDIntArray array) throws IllegalArgumentException
    {
        final int[] flatSourceArray = array.getAsFlatArray();
        final MDShortArray sArray = new MDShortArray(array.dimensions());
        final short[] flatTargetArray = sArray.getAsFlatArray();
        for (int i = 0; i < flatSourceArray.length; ++i)
        {
            flatTargetArray[i] = (short) flatSourceArray[i];
            if (flatSourceArray[i] != flatTargetArray[i])
            {
                throw new IllegalArgumentException("Value " + flatSourceArray[i]
                        + " cannot be stored in short array");
            }
        }
        return sArray;
    }

    private MDIntArray toIntArray(MDByteArray array)
    {
        final byte[] flatSourceArray = array.getAsFlatArray();
        final MDIntArray iArray = new MDIntArray(array.dimensions());
        final int[] flatTargetArray = iArray.getAsFlatArray();
        for (int i = 0; i < flatSourceArray.length; ++i)
        {
            flatTargetArray[i] = flatSourceArray[i];
        }
        return iArray;
    }

    private MDIntArray toIntArray(MDShortArray array)
    {
        final short[] flatSourceArray = array.getAsFlatArray();
        final MDIntArray iArray = new MDIntArray(array.dimensions());
        final int[] flatTargetArray = iArray.getAsFlatArray();
        for (int i = 0; i < flatSourceArray.length; ++i)
        {
            flatTargetArray[i] = flatSourceArray[i];
        }
        return iArray;
    }

    private void checkOrdinalArray(MDByteArray array) throws IllegalArgumentException
    {
        final byte[] flatArray = array.getAsFlatArray();
        for (int i = 0; i < flatArray.length; ++i)
        {
            final EnumerationType type = types[i % types.length];
            final int numberOfTerms = type.getValues().size();
            if (flatArray[i] < 0 || flatArray[i] >= numberOfTerms)
            {
                throw new IllegalArgumentException("valueIndex " + flatArray[i]
                        + " out of allowed range [0.." + (numberOfTerms - 1) + "] of type '"
                        + type.getName() + "'.");
            }
        }
    }

    private void checkOrdinalArray(MDShortArray array) throws IllegalArgumentException
    {
        final short[] flatArray = array.getAsFlatArray();
        for (int i = 0; i < flatArray.length; ++i)
        {
            final EnumerationType type = types[i % types.length];
            final int numberOfTerms = type.getValues().size();
            if (flatArray[i] < 0 || flatArray[i] >= numberOfTerms)
            {
                throw new IllegalArgumentException("valueIndex " + flatArray[i]
                        + " out of allowed range [0.." + (numberOfTerms - 1) + "] of type '"
                        + type.getName() + "'.");
            }
        }
    }

    private void checkOrdinalArray(MDIntArray array) throws IllegalArgumentException
    {
        final int[] flatArray = array.getAsFlatArray();
        for (int i = 0; i < flatArray.length; ++i)
        {
            final EnumerationType type = types[i % types.length];
            final int numberOfTerms = type.getValues().size();
            if (flatArray[i] < 0 || flatArray[i] >= numberOfTerms)
            {
                throw new IllegalArgumentException("valueIndex " + flatArray[i]
                        + " out of allowed range [0.." + (numberOfTerms - 1) + "] of type '"
                        + type.getName() + "'.");
            }
        }
    }

    EnumStorageForm getStorageForm()
    {
        return storageForm;
    }

    /**
     * Returns the <var>types</var> of this enumeration array.
     */
    public EnumerationType[] getTypes()
    {
        return types.clone();
    }

    /**
     * Returns the number of elements of this enumeration array.
     */
    public int size()
    {
        return getOrdinalValues().size();
    }

    /**
     * Returns the extent of this enum array along its <var>dim</var>-th axis.
     */
    public int size(int dim)
    {
        assert dim < dimensions().length;

        return dimensions()[dim];
    }

    /**
     * Returns the rank of this multi-dimensional enumeration array.
     */
    public int rank()
    {
        return dimensions().length;
    }

    /**
     * Returns the dimensions of this enumeration array.
     */
    public int[] dimensions()
    {
        return getOrdinalValues().dimensions();
    }

    /**
     * Returns the dimensions of this enumeration array as a long.
     */
    public long[] longDimensions()
    {
        return getOrdinalValues().longDimensions();
    }

    /**
     * Returns a view of the feature column <var>featureIndex</var> using <var>enumClass</var> to
     * represent its values.
     */
    public <T extends Enum<T>> IFeatureColumn<T> getFeatureView(final Class<T> enumClass,
            final int featureIndex)
    {
        checkCompatible(enumClass, featureIndex);
        return new IFeatureColumn<T>()
            {
                @Override
                public T get(int objectIndex)
                {
                    return Enum.valueOf(enumClass, getAsString(objectIndex));
                }

                @Override
                public String getAsString(int objectIndex)
                {
                    return HDF5EnumerationFeatureMatrix.this.getValue(objectIndex, featureIndex);
                }

                @Override
                public void set(T value, int objectIndex)
                {
                    HDF5EnumerationFeatureMatrix.this.setValue(value.name(), objectIndex,
                            featureIndex);
                }
            };
    }

    int getMaxNumberOfBits()
    {
        return maxNumberOfBits;
    }

    private <T extends Enum<T>> void checkCompatible(final Class<T> enumClass,
            final int featureIndex) throws IllegalArgumentException
    {
        final EnumerationType type = types[featureIndex % types.length];
        final List<String> typeConstants = type.getValues();
        final T[] enumConsts = enumClass.getEnumConstants();
        if (typeConstants.size() != enumConsts.length)
        {
            throw new IllegalArgumentException("Enum class " + enumClass.getName()
                    + " does not match enum type " + type.getName());
        }
        for (T c : enumConsts)
        {
            if (type.tryGetIndexForValue(c.name()) == null)
            {
                throw new IllegalArgumentException("Enum class " + enumClass.getName()
                        + " does not match enum type " + type.getName());
            }
        }
    }

    /**
     * Returns the ordinal value for the <var>arrayIndex</var>.
     * 
     * @param arrayIndex The index in the array to get the ordinal for.
     */
    private int getOrdinal(int arrayIndex)
    {
        if (bArrayOrNull != null)
        {
            return bArrayOrNull.get(arrayIndex);
        } else if (sArrayOrNull != null)
        {
            return sArrayOrNull.get(arrayIndex);
        } else
        {
            return iArrayOrNull.get(arrayIndex);
        }
    }

    /**
     * Returns the ordinal value for the given indices.
     * 
     * @param objectIndex The index of the object row.
     * @param featureIndex The y index for the feature column.
     */
    public int getOrdinal(int objectIndex, int featureIndex)
    {
        if (bArrayOrNull != null)
        {
            return bArrayOrNull.get(objectIndex, featureIndex);
        } else if (sArrayOrNull != null)
        {
            return sArrayOrNull.get(objectIndex, featureIndex);
        } else
        {
            return iArrayOrNull.get(objectIndex, featureIndex);
        }
    }

    /**
     * Sets the ordinal value for the given indices.
     * 
     * @param ordinalValue The new ordinal value.
     * @param objectIndex The index of the object row.
     * @param featureIndex The y index for the feature column.
     */
    public void setOrdinal(int ordinalValue, int objectIndex, int featureIndex)
    {
        if (bArrayOrNull != null)
        {
            bArrayOrNull.set((byte) ordinalValue, objectIndex, featureIndex);
        } else if (sArrayOrNull != null)
        {
            sArrayOrNull.set((short) ordinalValue, objectIndex, featureIndex);
        } else
        {
            iArrayOrNull.set(ordinalValue, objectIndex, featureIndex);
        }
    }

    /**
     * Returns the string value for <var>arrayIndex</var>.
     * 
     * @param objectIndex The index of the object (row).
     * @param featureIndex The index of the feature (column).
     */
    public String getValue(int objectIndex, int featureIndex)
    {
        return types[featureIndex % types.length].getValues().get(
                getOrdinal(objectIndex, featureIndex));
    }

    /**
     * Sets the string value for <var>arrayIndex</var>.
     * 
     * @param value The new feature value.
     * @param objectIndex The index of the object (row).
     * @param featureIndex The index of the feature (column).
     */
    public void setValue(String value, int objectIndex, int featureIndex)
    {
        final EnumerationType type = types[featureIndex % types.length];
        final Integer ordinal = type.tryGetIndexForValue(value);
        if (ordinal == null)
        {
            throw new IllegalArgumentException("Illegal feature value '" + value + "' for type "
                    + type.getName());
        }
        setOrdinal(ordinal, objectIndex, featureIndex);
    }

    /**
     * Returns the value as Enum of type <var>enumClass</var>.
     * 
     * @param enumClass The class to return the value as.
     * @param objectIndex The index of the object (row).
     * @param featureIndex The index of the feature (column).
     */
    public <T extends Enum<T>> T getValue(Class<T> enumClass, int objectIndex, int featureIndex)
    {
        return Enum.valueOf(enumClass, getValue(objectIndex, featureIndex));
    }

    /**
     * Returns the string values for all elements of this array.
     */
    public MDArray<String> toStringArray()
    {
        final int len = size();
        final MDArray<String> values = new MDArray<String>(String.class, dimensions());
        final String[] flatValues = values.getAsFlatArray();
        for (int i = 0; i < len; ++i)
        {
            flatValues[i] = types[i % types.length].getValues().get(getOrdinal(i));
        }
        return values;
    }

    /**
     * Returns the ordinal values for all elements of this array.
     */
    public MDAbstractArray<?> getOrdinalValues()
    {
        switch (getStorageForm())
        {
            case BYTE:
                return bArrayOrNull;
            case SHORT:
                return sArrayOrNull;
            case INT:
                return iArrayOrNull;
        }
        throw new Error("Illegal storage form.");
    }

    byte[] toStorageForm()
    {
        switch (getStorageForm())
        {
            case BYTE:
                return bArrayOrNull.getAsFlatArray();
            case SHORT:
                return NativeData.shortToByte(sArrayOrNull.getAsFlatArray(), ByteOrder.NATIVE);
            case INT:
                return NativeData.intToByte(iArrayOrNull.getAsFlatArray(), ByteOrder.NATIVE);
        }
        throw new Error("Illegal storage form (" + getStorageForm() + ".)");
    }

    static HDF5EnumerationFeatureMatrix fromStorageForm(EnumerationType[] enumTypes, byte[] data,
            int offset, int[] dimensions, int len)
    {
        final EnumStorageForm sform = getStorageForm(enumTypes);
        switch (sform)
        {
            case BYTE:
                final byte[] subArray = new byte[len];
                System.arraycopy(data, offset, subArray, 0, len);
                return new HDF5EnumerationFeatureMatrix(enumTypes, new MDByteArray(subArray,
                        dimensions));
            case SHORT:
                return new HDF5EnumerationFeatureMatrix(enumTypes, new MDShortArray(
                        HDFNativeData.byteToShort(data, offset, len), dimensions));
            case INT:
                return new HDF5EnumerationFeatureMatrix(enumTypes, new MDIntArray(
                        HDFNativeData.byteToInt(data, offset, len), dimensions));
        }
        throw new Error("Illegal storage form (" + sform + ".)");
    }

    static EnumStorageForm getStorageForm(EnumerationType[] enumTypes)
    {
        EnumStorageForm sform = EnumStorageForm.BYTE;
        for (EnumerationType type : enumTypes)
        {
            if (type.getStorageForm().getStorageSize() > sform.getStorageSize())
            {
                sform = type.getStorageForm();
            }
        }
        return sform;
    }

    //
    // Iterable
    //

    @Override
    public Iterator<MDArray<String>.ArrayEntry> iterator()
    {
        return toStringArray().iterator();
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return toStringArray().toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bArrayOrNull == null) ? 0 : bArrayOrNull.hashCode());
        result = prime * result + ((iArrayOrNull == null) ? 0 : iArrayOrNull.hashCode());
        result = prime * result + ((sArrayOrNull == null) ? 0 : sArrayOrNull.hashCode());
        result = prime * result + ((storageForm == null) ? 0 : storageForm.hashCode());
        result = prime * result + ((types == null) ? 0 : types.hashCode());
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
        HDF5EnumerationFeatureMatrix other = (HDF5EnumerationFeatureMatrix) obj;
        if (bArrayOrNull == null)
        {
            if (other.bArrayOrNull != null)
            {
                return false;
            }
        } else if (false == bArrayOrNull.equals(other.bArrayOrNull))
        {
            return false;
        }
        if (iArrayOrNull == null)
        {
            if (other.iArrayOrNull != null)
            {
                return false;
            }
        } else if (false == iArrayOrNull.equals(other.iArrayOrNull))
        {
            return false;
        }
        if (sArrayOrNull == null)
        {
            if (other.sArrayOrNull != null)
            {
                return false;
            }
        } else if (false == sArrayOrNull.equals(other.sArrayOrNull))
        {
            return false;
        }
        if (storageForm != other.storageForm)
        {
            return false;
        }
        if (types == null)
        {
            if (other.types != null)
            {
                return false;
            }
        } else if (false == types.equals(other.types))
        {
            return false;
        }
        return true;
    }

}
