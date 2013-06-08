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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import ch.ethz.sis.hcscld.IFeatureGroup.FeatureGroupDataType;
import ch.ethz.sis.hcscld.ImageRunner.IExistChecker;
import ch.systemsx.cisd.base.mdarray.MDAbstractArray;
import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.base.mdarray.MDLongArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.CompoundElement;
import ch.systemsx.cisd.hdf5.CompoundType;
import ch.systemsx.cisd.hdf5.HDF5CompoundMappingHints;
import ch.systemsx.cisd.hdf5.HDF5CompoundMemberInformation;
import ch.systemsx.cisd.hdf5.HDF5CompoundType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValue;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValueMDArray;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * Dataset of cell-level features.
 * 
 * @author Bernd Rinn
 */
class CellLevelFeatureDataset extends CellLevelDataset implements ICellLevelFeatureDataset
{
    static final String FORMAT_TYPE = "CompoundArrayOrPrimitiveMatrix";

    static final int CURRENT_FORMAT_VERSION_NUMBER = 1;

    static final String VALUES_PREFIX = "Values";

    static final String DEFAULT_FEATURE_GROUP_NAME = "DEFAULT";

    static final String ALL_FEATURE_GROUP_NAME = "All";

    private final Iterable<CellLevelFeatures> EMPTY_ITERABLE = new Iterable<CellLevelFeatures>()
        {
            @Override
            public Iterator<CellLevelFeatures> iterator()
            {
                return new Iterator<CellLevelFeatures>()
                    {
                        @Override
                        public boolean hasNext()
                        {
                            return false;
                        }

                        @Override
                        public CellLevelFeatures next()
                        {
                            throw new NoSuchElementException();
                        }

                        @Override
                        public void remove()
                        {
                            throw new UnsupportedOperationException();
                        }

                    };
            }
        };

    /**
     * The storage representation of a feature group.
     */
    @CompoundType(mapAllFields = false)
    final static class FeatureGroupDescriptor implements Comparable<FeatureGroupDescriptor>
    {
        @CompoundElement(dimensions =
            { 128 })
        String id;

        String idUpperCase;

        @CompoundElement
        HDF5EnumerationValue namespaceId;

        @CompoundElement
        FeatureGroupDataType dataType;

        FeatureGroupDescriptor()
        {
        }

        FeatureGroupDescriptor(String id, HDF5EnumerationValue namespaceId,
                FeatureGroupDataType dataType)
        {
            this.id = id;
            this.idUpperCase = id.toUpperCase();
            this.namespaceId = namespaceId;
            this.dataType = dataType;
        }

        String getId()
        {
            return id;
        }

        String getNamespaceId()
        {
            return namespaceId.getValue();
        }

        @Override
        public int compareTo(FeatureGroupDescriptor o)
        {
            return idUpperCase.compareTo(o.idUpperCase);
        }
    }

    /**
     * A feature group implementation.
     */
    class FeatureGroup implements IFeatureGroup, IEnumTypeProvider
    {
        private final HDF5CompoundType<Object[]> type;

        private final String id;

        private final String idUpperCase;

        private final List<Feature> features;

        private final List<String> featureNames;

        final ObjectNamespace namespace;

        FeatureGroup(String id, ObjectNamespace namespace)
        {
            this.id = id;
            this.idUpperCase = id.toUpperCase();
            this.type = null;
            this.namespace = namespace;
            final Integer numberOfFeatures = totalNumberOfFeatures.get(namespace);
            if (numberOfFeatures == null)
            {
                throw new NullPointerException("Dataset '" + datasetCode + "' has no '" + namespace
                        + "'.");
            }
            this.featureNames = new ArrayList<String>(numberOfFeatures);
            this.features = new ArrayList<Feature>(numberOfFeatures);
            for (FeatureGroup fg : featureGroups.values())
            {
                if (namespace.equals(fg.getNamespace()) == false)
                {
                    continue;
                }
                featureNames.addAll(fg.getFeatureNames());
                features.addAll(fg.getFeatures());
            }
        }

        FeatureGroup(String idfeatureGroupId, ObjectNamespace namespace,
                HDF5CompoundType<Object[]> type)
        {
            this.id = idfeatureGroupId;
            this.idUpperCase = id.toUpperCase();
            this.namespace = namespace;
            this.type = type;
            final List<HDF5CompoundMemberInformation> members =
                    Arrays.asList(type.getCompoundMemberInformation());
            final FeaturesDefinition fdef =
                    new FeaturesDefinition(CellLevelFeatureDataset.this, namespace, members);
            this.features = fdef.getFeatures();
            final String[] memberNameArray = new String[members.size()];
            for (int i = 0; i < memberNameArray.length; ++i)
            {
                memberNameArray[i] = members.get(i).getName();
            }
            this.featureNames = Arrays.asList(memberNameArray);
        }

        @Override
        public String getId()
        {
            return id;
        }

        String getObjectPath(ImageId imageId)
        {
            return CellLevelFeatureDataset.this.getObjectPath(imageId, VALUES_PREFIX, idUpperCase);
        }

        HDF5CompoundType<Object[]> getType()
        {
            return type;
        }

        @Override
        public List<String> getFeatureNames()
        {
            return featureNames;
        }

        @Override
        public List<Feature> getFeatures()
        {
            return features;
        }

        @Override
        public ObjectNamespace getNamespace()
        {
            return namespace;
        }

        @Override
        public int getNumberOfFeatures()
        {
            return featureNames.size();
        }

        @Override
        public Iterator<ImageId> iterator()
        {
            return ImageRunner.iterator(quantityStructure, new IExistChecker()
                {
                    @Override
                    public boolean exists(ImageId imageId)
                    {
                        return hasWellFieldValues(imageId);
                    }
                });
        }

        boolean hasWellFieldValues(ImageId imageId)
        {
            final String path = getObjectPath(imageId);
            return reader.exists(path) && reader.object().getSize(path) > 0;
        }

        boolean existsIn(ImageId imageId)
        {
            return reader.exists(getObjectPath(imageId));
        }

        @Override
        public FeatureGroupDataType getDataType()
        {
            FeatureGroupDataType commonType = null;
            for (Feature f : features)
            {
                if (commonType == null)
                {
                    commonType = f.getType().getOptimalGroupType();
                } else
                {
                    if (commonType != f.getType().getOptimalGroupType())
                    {
                        commonType = FeatureGroupDataType.COMPOUND;
                    }
                }
            }
            return commonType == null ? FeatureGroupDataType.COMPOUND : commonType;
        }

        //
        //
        // IEnumTypeProvider

        @Override
        public HDF5EnumerationType tryGetEnumType()
        {
            if (type.getEnumTypeMap().size() == 1)
            {
                return null;
            } else
            {
                final HDF5EnumerationType eType = type.getEnumTypeMap().values().iterator().next();
                // Check that all map entries are the same type.
                for (HDF5EnumerationType enumType : type.getEnumTypeMap().values())
                {
                    if (enumType.equals(eType) == false)
                    {
                        return null;
                    }
                }
                return eType;
            }
        }
    }

    class NamespaceFeatureGroup extends FeatureGroup
    {
        NamespaceFeatureGroup(ObjectNamespace namespace)
        {
            super(ALL_FEATURE_GROUP_NAME, namespace);
        }

        @Override
        boolean hasWellFieldValues(ImageId imageId)
        {
            for (FeatureGroup fg : featureGroups.values())
            {
                if (namespace.equals(fg.getNamespace()))
                {
                    if (fg.hasWellFieldValues(imageId))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        boolean existsIn(ImageId imageId)
        {
            for (FeatureGroup fg : featureGroups.values())
            {
                if (namespace.equals(fg.getNamespace()))
                {
                    if (fg.existsIn(imageId))
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    final Map<String, FeatureGroup> featureGroups;

    final Map<ObjectNamespace, Integer> totalNumberOfFeatures;

    final HDF5CompoundMappingHints hintsOrNull;

    CellLevelFeatureDataset(IHDF5Reader reader, String datasetCode,
            ImageQuantityStructure geometry, HDF5CompoundMappingHints hintsOrNull,
            String formatType, int formatVersionNumber)
    {
        super(reader, datasetCode, geometry, formatVersionNumber);
        checkFormat(FORMAT_TYPE, formatType);
        this.hintsOrNull = hintsOrNull;
        this.featureGroups = readFeatureGroups();
        this.totalNumberOfFeatures = new HashMap<ObjectNamespace, Integer>();
        for (FeatureGroup fg : featureGroups.values())
        {
            Integer featuresOrNull = totalNumberOfFeatures.get(fg.getNamespace());
            int numberOfFeatures = featuresOrNull == null ? 0 : featuresOrNull;
            numberOfFeatures += fg.getNumberOfFeatures();
            totalNumberOfFeatures.put(fg.getNamespace(), numberOfFeatures);
        }
    }

    private Map<String, FeatureGroup> readFeatureGroups()
    {
        if (reader.exists(getFeatureGroupsFilename()) == false)
        {
            return new LinkedHashMap<String, FeatureGroup>(3);
        }
        final FeatureGroupDescriptor[] featureGroupDesc =
                reader.readCompoundArray(getFeatureGroupsFilename(), FeatureGroupDescriptor.class);
        final Map<String, FeatureGroup> result =
                new LinkedHashMap<String, FeatureGroup>(featureGroupDesc.length);
        for (FeatureGroupDescriptor fgd : featureGroupDesc)
        {
            final HDF5CompoundType<Object[]> type =
                    reader.compound().getNamedType(getFeatureGroupTypePath(fgd.getId()),
                            Object[].class, hintsOrNull);
            result.put(fgd.getId().toUpperCase(), new FeatureGroup(fgd.getId(),
                    getObjectNamespace(fgd.getNamespaceId()), type));
        }
        return result;
    }

    String getFeatureGroupTypePath(String id)
    {
        return getObjectPath(DATASET_TYPE_DIR, "Compound_" + id.toUpperCase());
    }

    @Override
    public CellLevelDatasetType getType()
    {
        return CellLevelDatasetType.FEATURES;
    }

    @Override
    public ICellLevelClassificationDataset toClassificationDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.CLASSIFICATION,
                CellLevelDatasetType.FEATURES);
    }

    @Override
    public ICellLevelSegmentationDataset toSegmentationDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.SEGMENTATION,
                CellLevelDatasetType.FEATURES);
    }

    @Override
    public ICellLevelTrackingDataset toTrackingDataset() throws WrongDatasetTypeException
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.TRACKING,
                CellLevelDatasetType.FEATURES);
    }

    @Override
    public ICellLevelFeatureDataset toFeatureDataset()
    {
        return this;
    }

    @Override
    public Object[] getValues(ImageId id, IFeatureGroup featureGroup, int cellId)
    {
        return reader.compound().readArrayBlockWithOffset(
                ((FeatureGroup) featureGroup).getObjectPath(id),
                ((FeatureGroup) featureGroup).getType(), 1, cellId)[0];
    }

    @Override
    public Object[][] getValues(ImageId id, IFeatureGroup featureGroup)
    {
        switch (featureGroup.getDataType())
        {
            case BOOL:
            {
                final BitSet[] array =
                        reader.bool().readBitFieldArray(
                                ((FeatureGroup) featureGroup).getObjectPath(id));
                return toObjectArray(array);

            }
            case INT8:
            {
                final MDByteArray array =
                        reader.int8().readMDArray(((FeatureGroup) featureGroup).getObjectPath(id));
                return toObjectArray(array);

            }
            case INT16:
            {
                final MDShortArray array =
                        reader.int16().readMDArray(((FeatureGroup) featureGroup).getObjectPath(id));
                return toObjectArray(array);

            }
            case INT32:
            {
                final MDIntArray array =
                        reader.int32().readMDArray(((FeatureGroup) featureGroup).getObjectPath(id));
                return toObjectArray(array);

            }
            case INT64:
            {
                final MDLongArray array =
                        reader.int64().readMDArray(((FeatureGroup) featureGroup).getObjectPath(id));
                return toObjectArray(array);

            }
            case FLOAT32:
            {
                final MDFloatArray array =
                        reader.float32().readMDArray(
                                ((FeatureGroup) featureGroup).getObjectPath(id));
                return toObjectArray(array);
            }
            case FLOAT64:
            {
                final MDDoubleArray array =
                        reader.float64().readMDArray(
                                ((FeatureGroup) featureGroup).getObjectPath(id));
                return toObjectArray(array);
            }
            case ENUM:
            {
                final HDF5EnumerationValueMDArray array =
                        reader.enumeration().readMDArray(
                                ((FeatureGroup) featureGroup).getObjectPath(id));
                return toObjectArray(array);
            }
            case COMPOUND:
            default:
            {
                return reader.compound().readArray(((FeatureGroup) featureGroup).getObjectPath(id),
                        ((FeatureGroup) featureGroup).getType());
            }
        }
    }

    private Object[][] toObjectArray(HDF5EnumerationValueMDArray array)
    {
        if (array.rank() != 2)
        {
            throw new WrongDataTypeException("Expected a float array of rank 2, found rank "
                    + array.rank() + ".");
        }
        final int dimObjects = array.dimensions()[0];
        final int dimFeatures = array.dimensions()[1];
        final Object[][] result = new Object[dimObjects][dimFeatures];
        for (int i = 0; i < dimObjects; ++i)
        {
            for (int j = 0; j < dimFeatures; ++j)
            {
                result[i][j] = array.getValue(i, j);
            }
        }
        return result;
    }

    private Object[][] toObjectArray(MDAbstractArray<?> array)
    {
        if (array.rank() != 2)
        {
            throw new WrongDataTypeException("Expected a float array of rank 2, found rank "
                    + array.rank() + ".");
        }
        final int dimObjects = array.dimensions()[0];
        final int dimFeatures = array.dimensions()[1];
        final Object[][] result = new Object[dimObjects][dimFeatures];
        for (int i = 0; i < dimObjects; ++i)
        {
            for (int j = 0; j < dimFeatures; ++j)
            {
                result[i][j] = array.getAsObject(i, j);
            }
        }
        return result;
    }

    private Object[][] toObjectArray(BitSet[] array)
    {
        final int dimObjects = array.length;
        final int dimFeatures = (dimObjects > 0) ? array[0].length() : 0;
        final Object[][] result = new Object[dimObjects][dimFeatures];
        for (int i = 0; i < dimObjects; ++i)
        {
            for (int j = 0; j < dimFeatures; ++j)
            {
                result[i][j] = array[i].get(j);
            }
        }
        return result;
    }

    @Override
    public boolean hasValues(ImageId id, IFeatureGroup featureGroup)
    {
        return reader.exists(((FeatureGroup) featureGroup).getObjectPath(id));
    }

    @Override
    public Iterable<CellLevelFeatures> getValues(final IFeatureGroup featureGroup)
    {
        return new Iterable<CellLevelFeatures>()
            {
                @Override
                public Iterator<CellLevelFeatures> iterator()
                {
                    return new Iterator<CellLevelFeatures>()
                        {
                            final Iterator<ImageId> idIterator = featureGroup.iterator();

                            CellLevelFeatures next = null;

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
                                    final Object[][] data =
                                            reader.compound()
                                                    .readArray(
                                                            ((FeatureGroup) featureGroup)
                                                                    .getObjectPath(id),
                                                            ((FeatureGroup) featureGroup).getType());
                                    next = new CellLevelFeatures(featureGroup, id, data);
                                }
                                return true;
                            }

                            @Override
                            public CellLevelFeatures next()
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
    public Object[] getValues(ImageId id, ObjectNamespace namespace, int cellId)
    {
        final int numberOfFeatures = totalNumberOfFeatures.get(namespace);
        final Object[] result = new Object[numberOfFeatures];
        int offset = 0;
        for (FeatureGroup fg : featureGroups.values())
        {
            if (namespace.equals(fg.getNamespace()) == false)
            {
                continue;
            }
            final Object[] groupValues = getValues(id, fg, cellId);
            final int numberOfFeaturesInGroup = fg.getNumberOfFeatures();
            System.arraycopy(groupValues, 0, result, offset, numberOfFeaturesInGroup);
            offset += numberOfFeaturesInGroup;
        }
        return result;
    }

    @Override
    public Object[][] getValues(ImageId id, ObjectNamespace namespace)
    {
        Object[][] result = null;
        int offset = 0;
        final int numberOfFeatures = totalNumberOfFeatures.get(namespace);
        for (FeatureGroup fg : featureGroups.values())
        {
            if (namespace.equals(fg.getNamespace()) == false)
            {
                continue;
            }
            final int numberOfFeaturesInGroup = fg.getNumberOfFeatures();
            if (fg.hasWellFieldValues(id) == false)
            {
                offset += numberOfFeaturesInGroup;
                continue;
            }
            final Object[][] groupValues = getValues(id, fg);
            if (result == null)
            {
                result = new Object[groupValues.length][numberOfFeatures];
                if (result.length == 0)
                {
                    break;
                }
            }
            for (int i = 0; i < result.length; ++i)
            {
                System.arraycopy(groupValues[i], 0, result[i], offset, numberOfFeaturesInGroup);
            }
            offset += numberOfFeaturesInGroup;
        }
        return result;
    }

    @Override
    public boolean hasValues(ImageId id, ObjectNamespace namespace)
    {
        int featureGroupsWithValues = 0;
        for (FeatureGroup fg : featureGroups.values())
        {
            if (namespace.equals(fg.getNamespace()) == false)
            {
                continue;
            }
            if (fg.hasWellFieldValues(id))
            {
                ++featureGroupsWithValues;
            }
        }
        return featureGroupsWithValues > 0;
    }

    @Override
    public Iterable<CellLevelFeatures> getValues()
    {
        final ObjectNamespace namespaceOrNull = tryGetOnlyNamespace();
        if (namespaceOrNull == null)
        {
            return EMPTY_ITERABLE;
        } else
        {
            return getValues(namespaceOrNull);
        }
    }

    @Override
    public Object[] getValues(ImageId id, int cellId) throws IllegalStateException
    {
        final ObjectNamespace namespaceOrNull = tryGetOnlyNamespace();
        if (namespaceOrNull == null)
        {
            return new Object[0];
        } else
        {
            return getValues(id, namespaceOrNull, cellId);
        }
    }

    @Override
    public Object[][] getValues(ImageId id) throws IllegalStateException
    {
        final ObjectNamespace namespaceOrNull = tryGetOnlyNamespace();
        if (namespaceOrNull == null)
        {
            return new Object[0][0];
        } else
        {
            return getValues(id, namespaceOrNull);
        }
    }

    @Override
    public boolean hasValues(ImageId id)
    {
        final ObjectNamespace namespaceOrNull = tryGetOnlyNamespace();
        if (namespaceOrNull == null)
        {
            return false;
        } else
        {
            return hasValues(id, namespaceOrNull);
        }
    }

    @Override
    public Iterable<CellLevelFeatures> getValues(final ObjectNamespace namespace)
    {
        final FeatureGroup all =
                usesDefaultFeatureGroup() ? getFirstFeatureGroup(namespace)
                        : new NamespaceFeatureGroup(namespace);
        return new Iterable<CellLevelFeatures>()
            {
                @Override
                public Iterator<CellLevelFeatures> iterator()
                {
                    return new Iterator<CellLevelFeatures>()
                        {
                            final Iterator<ImageId> idIterator = quantityStructure.iterator();

                            CellLevelFeatures next = null;

                            @Override
                            public boolean hasNext()
                            {
                                if (next == null)
                                {
                                    ImageId id;
                                    do
                                    {
                                        if (idIterator.hasNext() == false)
                                        {
                                            return false;
                                        }
                                        id = idIterator.next();
                                    } while (all.hasWellFieldValues(id) == false);
                                    final Object[][] data = getValues(id, namespace);
                                    next = new CellLevelFeatures(all, id, data);
                                }
                                return true;
                            }

                            @Override
                            public CellLevelFeatures next()
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
    public List<IFeatureGroup> getFeatureGroups()
    {
        return Collections.unmodifiableList(new ArrayList<IFeatureGroup>(featureGroups.values()));
    }

    @Override
    public IFeatureGroup getFeatureGroup(String id) throws IllegalArgumentException
    {
        final String idUpper = id.toUpperCase();
        IFeatureGroup fg = featureGroups.get(idUpper);
        if (fg == null)
        {
            throw new IllegalArgumentException("There is no feature group '" + idUpper + "'");
        }
        return fg;
    }

    String getFeatureGroupsFilename()
    {
        return getObjectPath("FeatureGroups");
    }

    FeatureGroup getFirstFeatureGroup()
    {
        return getFirstFeatureGroup(null);
    }

    FeatureGroup getFirstFeatureGroup(ObjectNamespace namespaceOrNull)
    {
        for (FeatureGroup fg : featureGroups.values())
        {
            if (namespaceOrNull == null || namespaceOrNull.equals(fg.getNamespace()))
            {
                return fg;
            }
        }
        throw new IllegalArgumentException("Unknown " + namespaceOrNull);
    }

    FeatureGroup tryGetFirstFeatureGroup(ImageId imageId, ObjectNamespace namespace)
    {
        boolean foundNamespace = false;
        for (FeatureGroup fg : featureGroups.values())
        {
            if (namespace.equals(fg.getNamespace()))
            {
                foundNamespace = true;
                if (fg.existsIn(imageId))
                {
                    return fg;
                }
            }
        }
        if (foundNamespace == false)
        {
            throw new IllegalArgumentException("Unknown " + namespace);
        }
        return null;
    }

    boolean usesDefaultFeatureGroup() throws IllegalStateException
    {
        return (featureGroups.size() == 1 && DEFAULT_FEATURE_GROUP_NAME
                .equals(getFirstFeatureGroup().getId()));
    }

}
