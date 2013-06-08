/*
 * Copyright 2011-2013 ETH Zuerich, Scientific IT Services
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

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.ethz.sis.hcscld.ImageRunner.IExistChecker;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValueArray;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * Implementation of {@link ICellLevelClassificationDataset}.
 * 
 * @author Bernd Rinn
 */
class CellLevelClassificationDataset extends CellLevelDataset implements
        ICellLevelClassificationDataset
{
    static final String FORMAT_TYPE = "EnumArray";

    static final int CURRENT_FORMAT_VERSION_NUMBER = 1;

    CellLevelClassificationDataset(IHDF5Reader reader, String datasetCode,
            ImageQuantityStructure geometry, String formatType, int formatVersionNumber)
    {
        super(reader, datasetCode, geometry, formatVersionNumber);
        checkFormat(FORMAT_TYPE, formatType);
    }

    @Override
    public CellLevelDatasetType getType()
    {
        return CellLevelDatasetType.CLASSIFICATION;
    }

    @Override
    public ICellLevelFeatureDataset toFeatureDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.FEATURES,
                CellLevelDatasetType.CLASSIFICATION);
    }

    @Override
    public ICellLevelSegmentationDataset toSegmentationDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.SEGMENTATION,
                CellLevelDatasetType.CLASSIFICATION);
    }

    @Override
    public ICellLevelTrackingDataset toTrackingDataset() throws WrongDatasetTypeException
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.TRACKING,
                CellLevelDatasetType.CLASSIFICATION);
    }

    @Override
    public ICellLevelClassificationDataset toClassificationDataset()
    {
        return this;
    }

    @Override
    public String getClassification(ImageId id, ObjectNamespace namespace, int cellId)
    {
        return reader.enumeration().readArrayBlockWithOffset(getClassPath(id, namespace), 1, cellId)
                .getValue(0);
    }

    @Override
    public <T extends Enum<T>> T getClassification(ImageId id, Class<T> enumClass,
            ObjectNamespace namespace, int cellId)
    {
        return Enum
                .valueOf(
                        enumClass,
                        reader.enumeration().readArrayBlockWithOffset(getClassPath(id, namespace),
                                1, cellId).getValue(0));
    }

    @Override
    public int getClassificationOrdinal(ImageId id, ObjectNamespace namespace, int cellId)
    {
        return reader.enumeration().readArrayBlockWithOffset(getClassPath(id, namespace), 1, cellId)
                .getOrdinal(0);
    }

    @Override
    public String[] getClassifications(ImageId id, ObjectNamespace namespace)
    {
        return reader.enumeration().readArray(getClassPath(id, namespace)).toStringArray();
    }

    String getClassPath(ImageId id, ObjectNamespace namespace)
    {
        return getObjectPath(id, "Classes", namespace.getId());
    }

    @Override
    public boolean hasClassifications(ImageId id, ObjectNamespace namespace)
    {
        return reader.exists(getClassPath(id, namespace));
    }

    @Override
    public <T extends Enum<T>> T[] getClassifications(ImageId id, Class<T> enumClass,
            ObjectNamespace namespace)
    {
        final String[] clsStr = getClassifications(id, namespace);
        try
        {
            @SuppressWarnings("unchecked")
            final T[] clsEnum = (T[]) Array.newInstance(enumClass, clsStr.length);
            int i = 0;
            for (String s : clsStr)
            {
                clsEnum[i++] = Enum.valueOf(enumClass, s);
            }
            return clsEnum;
        } catch (Exception ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    @Override
    public int[] getClassificationsOrdinals(ImageId id, ObjectNamespace namespace)
    {
        final HDF5EnumerationValueArray array =
                reader.enumeration().readArray(getClassPath(id, namespace));
        final int[] ordinals = new int[array.getLength()];
        for (int i = 0; i < ordinals.length; ++i)
        {
            ordinals[i] = array.getOrdinal(i);
        }
        return ordinals;
    }

    @Override
    public Iterable<CellLevelClassificationsString> getClassifications(
            final ObjectNamespace namespace)
    {
        return new Iterable<CellLevelClassificationsString>()
            {
                @Override
                public Iterator<CellLevelClassificationsString> iterator()
                {
                    return new Iterator<CellLevelClassificationsString>()
                        {
                            final Iterator<ImageId> idIterator = ImageRunner.iterator(
                                    quantityStructure, new IExistChecker()
                                        {
                                            @Override
                                            public boolean exists(ImageId id)
                                            {
                                                return reader.exists(getClassPath(id, namespace));
                                            }
                                        });

                            CellLevelClassificationsString next = null;

                            @Override
                            public boolean hasNext()
                            {
                                if (next == null)
                                {
                                    if (idIterator.hasNext() == false)
                                    {
                                        return false;
                                    }
                                    final ImageId id = idIterator.next();
                                    next =
                                            new CellLevelClassificationsString(id,
                                                    getClassifications(id, namespace));
                                }
                                return true;
                            }

                            @Override
                            public CellLevelClassificationsString next()
                            {
                                try
                                {
                                    if (hasNext() == false)
                                    {
                                        throw new NoSuchElementException();
                                    }
                                    return next;
                                } finally
                                {
                                    next = null;
                                }
                            }

                            @Override
                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    @Override
    public <T extends Enum<T>> Iterable<CellLevelClassificationsEnum<T>> getClassifications(
            final Class<T> enumClass, final ObjectNamespace namespace)
    {
        return new Iterable<CellLevelClassificationsEnum<T>>()
            {
                @Override
                public Iterator<CellLevelClassificationsEnum<T>> iterator()
                {
                    return new Iterator<CellLevelClassificationsEnum<T>>()
                        {
                            final Iterator<ImageId> idIterator = ImageRunner.iterator(
                                    quantityStructure, new IExistChecker()
                                        {
                                            @Override
                                            public boolean exists(ImageId id)
                                            {
                                                return reader.exists(getClassPath(id, namespace));
                                            }
                                        });

                            CellLevelClassificationsEnum<T> next = null;

                            @Override
                            public boolean hasNext()
                            {
                                if (next == null)
                                {
                                    if (idIterator.hasNext() == false)
                                    {
                                        return false;
                                    }
                                    final ImageId id = idIterator.next();
                                    next =
                                            new CellLevelClassificationsEnum<T>(id,
                                                    getClassifications(id, enumClass, namespace));
                                }
                                return true;
                            }

                            @Override
                            public CellLevelClassificationsEnum<T> next()
                            {
                                try
                                {
                                    if (hasNext() == false)
                                    {
                                        throw new NoSuchElementException();
                                    }
                                    return next;
                                } finally
                                {
                                    next = null;
                                }
                            }

                            @Override
                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    @Override
    public Iterable<CellLevelClassificationsOrdinal> getClassificationsOrdinals(
            final ObjectNamespace namespace)
    {
        return new Iterable<CellLevelClassificationsOrdinal>()
            {
                @Override
                public Iterator<CellLevelClassificationsOrdinal> iterator()
                {
                    return new Iterator<CellLevelClassificationsOrdinal>()
                        {
                            final Iterator<ImageId> idIterator = ImageRunner.iterator(
                                    quantityStructure, new IExistChecker()
                                        {
                                            @Override
                                            public boolean exists(ImageId id)
                                            {
                                                return reader.exists(getClassPath(id, namespace));
                                            }
                                        });

                            CellLevelClassificationsOrdinal next = null;

                            @Override
                            public boolean hasNext()
                            {
                                if (next == null)
                                {
                                    if (idIterator.hasNext() == false)
                                    {
                                        return false;
                                    }
                                    final ImageId id = idIterator.next();
                                    next =
                                            new CellLevelClassificationsOrdinal(
                                                    id,
                                                    getClassificationsOrdinals(id, namespace));
                                }
                                return true;
                            }

                            @Override
                            public CellLevelClassificationsOrdinal next()
                            {
                                try
                                {
                                    if (hasNext() == false)
                                    {
                                        throw new NoSuchElementException();
                                    }
                                    return next;
                                } finally
                                {
                                    next = null;
                                }
                            }

                            @Override
                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    @Override
    public String getClassification(ImageId imageId, int cellId) throws IllegalStateException
    {
        return getClassification(imageId, getOnlyNamespace(), cellId);
    }

    @Override
    public <T extends Enum<T>> T getClassification(ImageId id, Class<T> enumClass, int cellId)
            throws IllegalStateException
    {
        return getClassification(id,  enumClass, getOnlyNamespace(), cellId);
    }

    @Override
    public int getClassificationOrdinal(ImageId imageId, int cellId) throws IllegalStateException
    {
        return getClassificationOrdinal(imageId, getOnlyNamespace(), cellId);
    }

    @Override
    public String[] getClassifications(ImageId imageId) throws IllegalStateException
    {
        return getClassifications(imageId, getOnlyNamespace());
    }

    @Override
    public <T extends Enum<T>> T[] getClassifications(ImageId imageId, Class<T> enumClass)
            throws IllegalStateException
    {
        return getClassifications(imageId, enumClass, getOnlyNamespace());
    }

    @Override
    public int[] getClassificationsOrdinals(ImageId imageId) throws IllegalStateException
    {
        return getClassificationsOrdinals(imageId, getOnlyNamespace());
    }

    @Override
    public boolean hasClassifications(ImageId id) throws IllegalStateException
    {
        return hasClassifications(id, getOnlyNamespace());
    }

    @Override
    public Iterable<CellLevelClassificationsString> getClassifications()
            throws IllegalStateException
    {
        return getClassifications(getOnlyNamespace());
    }

    @Override
    public <T extends Enum<T>> Iterable<CellLevelClassificationsEnum<T>> getClassifications(
            Class<T> enumClass) throws IllegalStateException
    {
        return getClassifications(enumClass, getOnlyNamespace());
    }

    @Override
    public Iterable<CellLevelClassificationsOrdinal> getClassificationsOrdinals()
            throws IllegalStateException
    {
        return getClassificationsOrdinals(getOnlyNamespace());
    }

}
