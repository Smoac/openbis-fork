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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import ch.ethz.cisd.hcscld.FeatureGroupNamespace.FeatureNamespaceKind;
import ch.ethz.cisd.hcscld.ImageRunner.IExistChecker;
import ch.systemsx.cisd.hdf5.CompoundElement;
import ch.systemsx.cisd.hdf5.HDF5CompoundMappingHints;
import ch.systemsx.cisd.hdf5.HDF5CompoundMemberInformation;
import ch.systemsx.cisd.hdf5.HDF5CompoundType;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * Dataset of cell-level features.
 * 
 * @author Bernd Rinn
 */
class CellLevelFeatureDataset extends CellLevelDataset implements ICellLevelFeatureDataset
{
    static final String FORMAT_TYPE = "CompoundArray";

    static final int CURRENT_FORMAT_VERSION_NUMBER = 1;

    static final String VALUES_PREFIX = "Values";

    static final String DEFAULT_FEATURE_GROUP_NAME = "DEFAULT";

    static final String ALL_FEATURE_GROUP_NAME = "ALL";

    /**
     * The storage representation of a feature group.
     */
    final static class FeatureGroupDescriptor
    {
        @CompoundElement(dimensions =
            { 100 })
        String id;

        @CompoundElement(dimensions =
            { 100 })
        String namespaceId;

        FeatureNamespaceKind namespaceKind;

        FeatureGroupDescriptor()
        {
        }

        FeatureGroupDescriptor(String id, FeatureGroupNamespace namespace)
        {
            this.id = id;
            this.namespaceId = namespace.getId();
            this.namespaceKind = namespace.getKind();
        }

        String getId()
        {
            return id;
        }

        FeatureGroupNamespace getNamespace()
        {
            return new FeatureGroupNamespace(namespaceId, namespaceKind);
        }

        String getNamespaceId()
        {
            return namespaceId;
        }

        FeatureNamespaceKind getNamespaceKind()
        {
            return namespaceKind;
        }
    }

    /**
     * A feature group implementation.
     */
    final class FeatureGroup implements IFeatureGroup
    {
        private final HDF5CompoundType<Object[]> type;

        private final FeatureGroupNamespace namespace;

        private final String id;

        private final List<Feature> features;

        private final List<String> featureNames;

        FeatureGroup(String id, FeatureGroupNamespace namespace)
        {
            this.id = id;
            this.type = null;
            this.namespace = namespace;
            final int numberOfFeatures = totalNumberOfFeatures.get(namespace);
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

        FeatureGroup(String idfeatureGroupId, FeatureGroupNamespace namespace,
                HDF5CompoundType<Object[]> type)
        {
            this.id = idfeatureGroupId;
            this.namespace = namespace;
            this.type = type;
            final List<HDF5CompoundMemberInformation> members =
                    Arrays.asList(type.getCompoundMemberInformation());
            final FeaturesDefinition fdef =
                    new FeaturesDefinition(CellLevelFeatureDataset.this, members);
            this.features = fdef.getFeatures();
            final String[] memberNameArray = new String[members.size()];
            for (int i = 0; i < memberNameArray.length; ++i)
            {
                memberNameArray[i] = members.get(i).getName();
            }
            this.featureNames = Arrays.asList(memberNameArray);
        }

        public String getId()
        {
            return id;
        }

        String getObjectPath(ImageId imageId)
        {
            return CellLevelFeatureDataset.this.getObjectPath(imageId, VALUES_PREFIX, id);
        }

        HDF5CompoundType<Object[]> getType()
        {
            return type;
        }

        public List<String> getFeatureNames()
        {
            return featureNames;
        }

        public List<Feature> getFeatures()
        {
            return features;
        }

        public FeatureGroupNamespace getNamespace()
        {
            return namespace;
        }

        public int getNumberOfFeatures()
        {
            return featureNames.size();
        }

        public Iterator<ImageId> iterator()
        {
            return ImageRunner.iterator(quantityStructure, new IExistChecker()
                {
                    public boolean exists(ImageId imageId)
                    {
                        return hasWellFieldValues(imageId);
                    }
                });
        }

        boolean hasWellFieldValues(ImageId imageId)
        {
            final String path = getObjectPath(imageId);
            return reader.exists(path) && reader.getSize(path) > 0;
        }

        boolean existsIn(ImageId imageId)
        {
            return reader.exists(getObjectPath(imageId));
        }
    }

    final Map<String, FeatureGroup> featureGroups;

    final Map<String, FeatureGroupNamespace> featureGroupNamespaces;

    final Map<FeatureGroupNamespace, Integer> totalNumberOfFeatures;

    final HDF5CompoundMappingHints hintsOrNull;

    CellLevelFeatureDataset(IHDF5Reader reader, String datasetCode,
            ImageQuantityStructure geometry, HDF5CompoundMappingHints hintsOrNull,
            String formatType, int formatVersionNumber)
    {
        super(reader, datasetCode, geometry, formatVersionNumber);
        checkFormat(FORMAT_TYPE, formatType);
        this.hintsOrNull = hintsOrNull;
        this.featureGroups = readFeatureGroups();
        this.featureGroupNamespaces = extractNamespacesFromFeatureGroups();
        this.totalNumberOfFeatures = new HashMap<FeatureGroupNamespace, Integer>();
        for (FeatureGroup fg : featureGroups.values())
        {
            Integer featuresOrNull = totalNumberOfFeatures.get(fg.getNamespace());
            int numberOfFeatures = featuresOrNull == null ? 0 : featuresOrNull;
            numberOfFeatures += fg.getNumberOfFeatures();
            totalNumberOfFeatures.put(fg.getNamespace(), numberOfFeatures);
        }
    }

    void addFeatureGroupToInternalList(FeatureGroup newFeatureGroup)
    {
        featureGroups.put(newFeatureGroup.getId(), newFeatureGroup);
        if (featureGroupNamespaces.containsKey(newFeatureGroup.getNamespace().getId()) == false)
        {
            featureGroupNamespaces.put(newFeatureGroup.getNamespace().getId(),
                    newFeatureGroup.getNamespace());
        }
    }

    private Map<String, FeatureGroup> readFeatureGroups()
    {
        if (reader.exists(getFeatureGroupsFilename()) == false)
        {
            return new LinkedHashMap<String, FeatureGroup>(3);
        }
        final FeatureGroupDescriptor[] featureGroupCpds =
                reader.readCompoundArray(getFeatureGroupsFilename(), FeatureGroupDescriptor.class);
        final Map<String, FeatureGroup> result =
                new LinkedHashMap<String, FeatureGroup>(featureGroupCpds.length);
        for (FeatureGroupDescriptor fg : featureGroupCpds)
        {
            final HDF5CompoundType<Object[]> type =
                    reader.getNamedCompoundType(getDataTypeName(fg.getId()), Object[].class,
                            hintsOrNull);
            result.put(fg.getId(), new FeatureGroup(fg.getId(), fg.getNamespace(), type));
        }
        return result;
    }

    private Map<String, FeatureGroupNamespace> extractNamespacesFromFeatureGroups()
    {
        final Map<String, FeatureGroupNamespace> namespaces =
                new LinkedHashMap<String, FeatureGroupNamespace>();
        for (IFeatureGroup fg : featureGroups.values())
        {
            if (namespaces.containsKey(fg.getNamespace().getId()) == false)
            {
                namespaces.put(fg.getNamespace().getId(), fg.getNamespace());
            }
        }
        return namespaces;
    }

    public CellLevelDatasetType getType()
    {
        return CellLevelDatasetType.FEATURES;
    }

    public ICellLevelClassificationDataset toClassificationDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.CLASSIFICATION,
                CellLevelDatasetType.FEATURES);
    }

    public ICellLevelSegmentationDataset toSegmentationDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.SEGMENTATION,
                CellLevelDatasetType.FEATURES);
    }

    public ICellLevelFeatureDataset toFeatureDataset()
    {
        return this;
    }

    public Object[] getValues(ImageId id, IFeatureGroup featureGroup, int cellId)
    {
        return reader.readCompoundArrayBlockWithOffset(
                ((FeatureGroup) featureGroup).getObjectPath(id),
                ((FeatureGroup) featureGroup).getType(), 1, cellId)[0];
    }

    public Object[][] getValues(ImageId id, IFeatureGroup featureGroup)
    {
        return reader.readCompoundArray(((FeatureGroup) featureGroup).getObjectPath(id),
                ((FeatureGroup) featureGroup).getType());
    }

    public Iterable<CellLevelFeatures> getValues(final IFeatureGroup featureGroup)
    {
        return new Iterable<CellLevelFeatures>()
            {
                public Iterator<CellLevelFeatures> iterator()
                {
                    return new Iterator<CellLevelFeatures>()
                        {
                            final Iterator<ImageId> idIterator = featureGroup.iterator();

                            CellLevelFeatures next = null;

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
                                            reader.readCompoundArray(
                                                    ((FeatureGroup) featureGroup).getObjectPath(id),
                                                    ((FeatureGroup) featureGroup).getType());
                                    next = new CellLevelFeatures(featureGroup, id, data);
                                }
                                return true;
                            }

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

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    public Object[] getValues(ImageId id, FeatureGroupNamespace namespace, int cellId)
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

    public Object[][] getValues(ImageId id, FeatureGroupNamespace namespace)
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

    public Iterable<CellLevelFeatures> getValues()
    {
        return getValues(getOnlyNamespace());
    }

    public Object[] getValues(ImageId id, int cellId) throws IllegalStateException
    {
        return getValues(id, getOnlyNamespace(), cellId);
    }

    public Object[][] getValues(ImageId id) throws IllegalStateException
    {
        return getValues(id, getOnlyNamespace());
    }

    public Iterable<CellLevelFeatures> getValues(final FeatureGroupNamespace namespace)
    {
        final FeatureGroup all =
                usesDefaultFeatureGroup() ? getFirstFeatureGroup(namespace) : new FeatureGroup(
                        ALL_FEATURE_GROUP_NAME, namespace);
        return new Iterable<CellLevelFeatures>()
            {
                public Iterator<CellLevelFeatures> iterator()
                {
                    return new Iterator<CellLevelFeatures>()
                        {
                            final Iterator<ImageId> idIterator = quantityStructure.iterator();

                            CellLevelFeatures next = null;

                            public boolean hasNext()
                            {
                                if (next == null)
                                {
                                    if (idIterator.hasNext() == false)
                                    {
                                        return false;
                                    }
                                    final ImageId id = idIterator.next();
                                    final Object[][] data = getValues(id, namespace);
                                    next = new CellLevelFeatures(all, id, data);
                                }
                                return true;
                            }

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

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    public List<IFeatureGroup> getFeatureGroups()
    {
        return Collections.unmodifiableList(new ArrayList<IFeatureGroup>(featureGroups.values()));
    }

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

    public List<FeatureGroupNamespace> getNamespaces()
    {
        return Collections.unmodifiableList(new ArrayList<FeatureGroupNamespace>(
                featureGroupNamespaces.values()));
    }

    public FeatureGroupNamespace getNamespace(String id) throws IllegalArgumentException
    {
        final String idUpper = id.toUpperCase();
        FeatureGroupNamespace ns = featureGroupNamespaces.get(idUpper);
        if (ns == null)
        {
            throw new IllegalArgumentException("There is no feature group name space '" + idUpper
                    + "'");
        }
        return ns;
    }

    String getFeatureGroupsFilename()
    {
        return getObjectPath("featureGroups");
    }

    FeatureGroup getFirstFeatureGroup()
    {
        return getFirstFeatureGroup(null);
    }

    FeatureGroup getFirstFeatureGroup(FeatureGroupNamespace namespaceOrNull)
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

    FeatureGroup tryGetFirstFeatureGroup(ImageId imageId, FeatureGroupNamespace namespace)
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

    FeatureGroupNamespace getOnlyNamespace()
    {
        FeatureGroupNamespace namespace = null;
        for (FeatureGroup fg : featureGroups.values())
        {
            if (namespace == null)
            {
                namespace = fg.getNamespace();
            }
            if (namespace.equals(fg.getNamespace()) == false)
            {
                throw new IllegalStateException(
                        "getOnlyNamespace() may not be called on datasets with multiple feature group namespaces.");
            }
        }
        return namespace;
    }

    boolean usesDefaultFeatureGroup() throws IllegalStateException
    {
        return (featureGroups.size() == 1 && DEFAULT_FEATURE_GROUP_NAME
                .equals(getFirstFeatureGroup().getId()));
    }

}
