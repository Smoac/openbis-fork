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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import ch.ethz.cisd.hcscld.WellFieldRunner.IExistChecker;
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
    static final String DEFAULT_FEATURE_GROUP_NAME = "default";

    static final String ALL_FEATURE_GROUP_NAME = "all";

    /**
     * A feature group implementation.
     * 
     * @author Bernd Rinn
     */
    final class FeatureGroup implements IFeatureGroup
    {
        private final HDF5CompoundType<Object[]> type;

        private final String name;

        private final List<Feature> features;

        private final List<String> featureNames;

        FeatureGroup(String name)
        {
            this.name = name;
            this.type = null;
            this.featureNames = new ArrayList<String>(totalNumberOfFeatures);
            this.features = new ArrayList<Feature>(totalNumberOfFeatures);
            for (FeatureGroup fg : featureGroups)
            {
                featureNames.addAll(fg.getFeatureNames());
                features.addAll(fg.getFeatures());
            }
        }

        FeatureGroup(String name, HDF5CompoundType<Object[]> type)
        {
            this.name = name;
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

        public String getName()
        {
            return name;
        }

        String getObjectPath(WellFieldId id)
        {
            return CellLevelFeatureDataset.this.getObjectPath(id) + "/" + name;
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

        public int getNumberOfFeatures()
        {
            return featureNames.size();
        }

        public Iterator<WellFieldId> iterator()
        {
            return WellFieldRunner.iterator(geometry, new IExistChecker()
                {
                    public boolean exists(WellFieldId id)
                    {
                        return hasWellFieldValues(id);
                    }
                });
        }

        boolean hasWellFieldValues(WellFieldId id)
        {
            final String path = getObjectPath(id);
            return reader.exists(path) && reader.getSize(path) > 0;
        }

        boolean existsIn(WellFieldId id)
        {
            return reader.exists(getObjectPath(id));
        }
    }

    final List<FeatureGroup> featureGroups;

    final int totalNumberOfFeatures;

    final HDF5CompoundMappingHints hintsOrNull;

    CellLevelFeatureDataset(IHDF5Reader reader, String datasetCode, WellFieldGeometry geometry,
            HDF5CompoundMappingHints hintsOrNull)
    {
        super(reader, datasetCode, geometry);
        this.hintsOrNull = hintsOrNull;
        this.featureGroups = readFeatureGroups();
        int numberOfFeatures = 0;
        for (FeatureGroup fg : featureGroups)
        {
            numberOfFeatures += fg.getNumberOfFeatures();
        }
        this.totalNumberOfFeatures = numberOfFeatures;
    }

    void addFeatureGroupToInternalList(FeatureGroup newFeatureGroup)
    {
        featureGroups.add(newFeatureGroup);
    }

    private List<FeatureGroup> readFeatureGroups()
    {
        if (reader.exists(getFeatureGroupsFilename()) == false)
        {
            return new ArrayList<CellLevelFeatureDataset.FeatureGroup>(3);
        }
        final String[] featureGroupNames = reader.readStringArray(getFeatureGroupsFilename());
        final List<FeatureGroup> result = new ArrayList<FeatureGroup>(featureGroupNames.length);
        for (String name : featureGroupNames)
        {
            final HDF5CompoundType<Object[]> type =
                    reader.getNamedCompoundType(getNameInDataset(name), Object[].class, hintsOrNull);
            result.add(new FeatureGroup(name, type));
        }
        return result;
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

    public Object[] getValues(WellFieldId id, IFeatureGroup featureGroup, int cellId)
    {
        return reader.readCompoundArrayBlockWithOffset(
                ((FeatureGroup) featureGroup).getObjectPath(id),
                ((FeatureGroup) featureGroup).getType(), 1, cellId)[0];
    }

    public Object[][] getValues(WellFieldId id, IFeatureGroup featureGroup)
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
                            final Iterator<WellFieldId> idIterator = featureGroup.iterator();

                            CellLevelFeatures next = null;

                            public boolean hasNext()
                            {
                                if (next == null)
                                {
                                    if (idIterator.hasNext() == false)
                                    {
                                        return false;
                                    }
                                    final WellFieldId id = idIterator.next();
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

    public Object[] getValues(WellFieldId id, int cellId)
    {
        final Object[] result = new Object[totalNumberOfFeatures];
        int offset = 0;
        for (FeatureGroup fg : featureGroups)
        {
            final Object[] groupValues = getValues(id, fg, cellId);
            final int numberOfFeaturesInGroup = fg.getNumberOfFeatures();
            System.arraycopy(groupValues, 0, result, offset, numberOfFeaturesInGroup);
            offset += numberOfFeaturesInGroup;
        }
        return result;
    }

    public Object[][] getValues(WellFieldId id)
    {
        Object[][] result = null;
        int offset = 0;
        for (FeatureGroup fg : featureGroups)
        {
            final int numberOfFeaturesInGroup = fg.getNumberOfFeatures();
            if (fg.hasWellFieldValues(id) == false)
            {
                offset += numberOfFeaturesInGroup;
                continue;
            }
            final Object[][] groupValues = getValues(id, fg);
            if (result == null)
            {
                result = new Object[groupValues.length][totalNumberOfFeatures];
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
        final FeatureGroup all =
                new FeatureGroup(usesDefaultFeatureGroup() ? DEFAULT_FEATURE_GROUP_NAME
                        : ALL_FEATURE_GROUP_NAME);
        return new Iterable<CellLevelFeatures>()
            {
                public Iterator<CellLevelFeatures> iterator()
                {
                    return new Iterator<CellLevelFeatures>()
                        {
                            final Iterator<WellFieldId> idIterator = geometry.iterator();

                            CellLevelFeatures next = null;

                            public boolean hasNext()
                            {
                                if (next == null)
                                {
                                    if (idIterator.hasNext() == false)
                                    {
                                        return false;
                                    }
                                    final WellFieldId id = idIterator.next();
                                    final Object[][] data = getValues(id);
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

    @SuppressWarnings("unchecked")
    public List<IFeatureGroup> getFeatureGroups()
    {
        return getFeatureGroupsUntyped();
    }

    @SuppressWarnings("rawtypes")
    private List getFeatureGroupsUntyped()
    {
        return featureGroups;
    }

    public IFeatureGroup getFeatureGroup(String name)
    {
        final HDF5CompoundType<Object[]> type =
                reader.getNamedCompoundType(getNameInDataset(name), Object[].class, hintsOrNull);
        return new FeatureGroup(name, type);
    }

    String getFeatureGroupsFilename()
    {
        return getDatasetCode() + "/featureGroups";
    }

    boolean usesDefaultFeatureGroup() throws IllegalStateException
    {
        return (featureGroups.size() == 1 && DEFAULT_FEATURE_GROUP_NAME.equals(featureGroups.get(0)
                .getName()));
    }

}
