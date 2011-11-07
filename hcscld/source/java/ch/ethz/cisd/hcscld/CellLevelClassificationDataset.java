/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.cisd.hcscld;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.ethz.cisd.hcscld.ImageRunner.IExistChecker;
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

    public CellLevelDatasetType getType()
    {
        return CellLevelDatasetType.CLASSIFICATION;
    }

    public ICellLevelFeatureDataset toFeatureDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.FEATURES,
                CellLevelDatasetType.CLASSIFICATION);
    }

    public ICellLevelSegmentationDataset toSegmentationDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.SEGMENTATION,
                CellLevelDatasetType.CLASSIFICATION);
    }

    public ICellLevelClassificationDataset toClassificationDataset()
    {
        return this;
    }

    public String getClassification(ImageId id, int cellId)
    {
        return reader.readEnumArrayBlockWithOffset(getObjectPath(id), 1, cellId).getValue(0);
    }

    public <T extends Enum<T>> T getClassification(ImageId id, Class<T> enumClass, int cellId)
    {
        return Enum.valueOf(enumClass,
                reader.readEnumArrayBlockWithOffset(getObjectPath(id), 1, cellId).getValue(0));
    }

    public int getClassificationOrdinal(ImageId id, int cellId)
    {
        return reader.readEnumArrayBlockWithOffset(getObjectPath(id), 1, cellId).getOrdinal(0);
    }

    public String[] getClassifications(ImageId id)
    {
        return reader.readEnumArrayAsString(getObjectPath(id));
    }

    public <T extends Enum<T>> T[] getClassifications(ImageId id, Class<T> enumClass)
    {
        final String[] clsStr = getClassifications(id);
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

    public int[] getClassificationsOrdinal(ImageId id)
    {
        final HDF5EnumerationValueArray array = reader.readEnumArray(getObjectPath(id));
        final int[] ordinals = new int[array.getLength()];
        for (int i = 0; i < ordinals.length; ++i)
        {
            ordinals[i] = array.getOrdinal(i);
        }
        return ordinals;
    }

    public Iterable<CellLevelClassificationsString> getClassifications()
    {
        return new Iterable<CellLevelClassificationsString>()
            {
                public Iterator<CellLevelClassificationsString> iterator()
                {
                    return new Iterator<CellLevelClassificationsString>()
                        {
                            final Iterator<ImageId> idIterator = ImageRunner.iterator(
                                    quantityStructure, new IExistChecker()
                                        {
                                            public boolean exists(ImageId id)
                                            {
                                                return reader.exists(getObjectPath(id));
                                            }
                                        });

                            CellLevelClassificationsString next = null;

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
                                                    getClassifications(id));
                                }
                                return true;
                            }

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

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    public <T extends Enum<T>> Iterable<CellLevelClassificationsEnum<T>> getClassifications(
            final Class<T> enumClass)
    {
        return new Iterable<CellLevelClassificationsEnum<T>>()
            {
                public Iterator<CellLevelClassificationsEnum<T>> iterator()
                {
                    return new Iterator<CellLevelClassificationsEnum<T>>()
                        {
                            final Iterator<ImageId> idIterator = ImageRunner.iterator(
                                    quantityStructure, new IExistChecker()
                                        {
                                            public boolean exists(ImageId id)
                                            {
                                                return reader.exists(getObjectPath(id));
                                            }
                                        });

                            CellLevelClassificationsEnum<T> next = null;

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
                                                    getClassifications(id, enumClass));
                                }
                                return true;
                            }

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

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    public Iterable<CellLevelClassificationsOrdinal> getClassificationsOrdinal()
    {
        return new Iterable<CellLevelClassificationsOrdinal>()
            {
                public Iterator<CellLevelClassificationsOrdinal> iterator()
                {
                    return new Iterator<CellLevelClassificationsOrdinal>()
                        {
                            final Iterator<ImageId> idIterator = ImageRunner.iterator(
                                    quantityStructure, new IExistChecker()
                                        {
                                            public boolean exists(ImageId id)
                                            {
                                                return reader.exists(getObjectPath(id));
                                            }
                                        });

                            CellLevelClassificationsOrdinal next = null;

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
                                            new CellLevelClassificationsOrdinal(id,
                                                    getClassificationsOrdinal(id));
                                }
                                return true;
                            }

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

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

}
