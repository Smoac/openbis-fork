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
import ch.systemsx.cisd.hdf5.HDF5CompoundMemberInformation;
import ch.systemsx.cisd.hdf5.HDF5CompoundType;
import ch.systemsx.cisd.hdf5.HDF5DataBlock;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * Dataset of cell-level features.
 * 
 * @author Bernd Rinn
 */
class CellLevelFeatureDataset extends CellLevelDataset implements ICellLevelFeatureDataset
{
    /**
     * A feature group implementation.
     * 
     * @author Bernd Rinn
     */
    final class FeatureGroup implements IFeatureGroup
    {
        private final HDF5CompoundType<Object[]> type;

        private final String name;

        private List<HDF5CompoundMemberInformation> members;
        
        private List<String> memberNames;
        
        FeatureGroup(String name, HDF5CompoundType<Object[]> type)
        {
            this.name = name;
            this.type = type;
            this.members = Arrays.asList(type.getCompoundMemberInformation());
            final String[] memberNameArray = new String[members.size()];
            for (int i = 0; i < memberNameArray.length; ++i)
            {
                memberNameArray[i] = members.get(i).getName();
            }
            this.memberNames = Arrays.asList(memberNameArray);
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

        public List<String> getMemberNames()
        {
            return memberNames;
        }

        public List<HDF5CompoundMemberInformation> getMembers()
        {
            return members;
        }

        public Iterator<WellFieldId> iterator()
        {
            return WellFieldRunner.iterator(geometry, new IExistChecker()
                {
                    public boolean exists(WellFieldId id)
                    {
                        final String path = getObjectPath(id);
                        return reader.exists(path) && reader.getSize(path) > 0;
                    }
                });
        }
    }

    CellLevelFeatureDataset(IHDF5Reader reader, String datasetCode,
            WellFieldGeometry geometry)
    {
        super(reader, datasetCode, geometry);
    }

    public CellLevelDatasetType getType()
    {
        return CellLevelDatasetType.FEATURES;
    }

    @Override
    public ICellLevelFeatureDataset tryAsFeatureDataset()
    {
        return this;
    }

    public Object[] getFeatures(IFeatureGroup featureGroup, WellFieldId id, int cellId)
    {
        return reader.readCompoundArrayBlockWithOffset(
                ((FeatureGroup) featureGroup).getObjectPath(id),
                ((FeatureGroup) featureGroup).getType(), 1, cellId)[0];
    }

    public Object[][] getFeatures(IFeatureGroup featureGroup, WellFieldId id)
    {
        return reader.readCompoundArray(((FeatureGroup) featureGroup).getObjectPath(id),
                ((FeatureGroup) featureGroup).getType());
    }

    public Iterable<CellLevelFeatureBlock> getFeaturesNaturalBlocks(
            final IFeatureGroup featureGroup, final WellFieldId id)
    {
        final Iterable<HDF5DataBlock<Object[][]>> blockIterable =
                reader.getCompoundArrayNaturalBlocks(
                        ((FeatureGroup) featureGroup).getObjectPath(id), Object[].class);
        return new Iterable<CellLevelFeatureBlock>()
            {
                public Iterator<CellLevelFeatureBlock> iterator()
                {
                    final Iterator<HDF5DataBlock<Object[][]>> blockIterator =
                            blockIterable.iterator();
                    return new Iterator<CellLevelFeatureBlock>()
                        {
                            public boolean hasNext()
                            {
                                return blockIterator.hasNext();
                            }

                            public CellLevelFeatureBlock next()
                            {
                                return new CellLevelFeatureBlock(featureGroup, id,
                                        blockIterator.next());
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    public Iterable<CellLevelFeatures> getFeatures(final IFeatureGroup featureGroup)
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
                                        final Object[][] data = reader.readCompoundArray(
                                                ((FeatureGroup) featureGroup).getObjectPath(id),
                                                Object[].class);
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

    public Iterable<CellLevelFeatureBlock> getFeaturesNaturalBlocks(final IFeatureGroup featureGroup)
    {
        return new Iterable<CellLevelFeatureBlock>()
            {
                public Iterator<CellLevelFeatureBlock> iterator()
                {
                    return new Iterator<CellLevelFeatureBlock>()
                        {
                            final Iterator<WellFieldId> idIterator = featureGroup.iterator();

                            Iterator<HDF5DataBlock<Object[][]>> blockIterator;

                            WellFieldId id = null;

                            CellLevelFeatureBlock next = null;

                            void init()
                            {
                                if (blockIterator == null)
                                {
                                    blockIterator = tryNextBlockIterator();
                                }
                            }

                            Iterator<HDF5DataBlock<Object[][]>> tryNextBlockIterator()
                            {
                                if (idIterator.hasNext() == false)
                                {
                                    return null;
                                }
                                id = idIterator.next();
                                return reader.getCompoundArrayNaturalBlocks(
                                        ((FeatureGroup) featureGroup).getObjectPath(id),
                                        Object[].class).iterator();
                            }

                            CellLevelFeatureBlock tryFindNext()
                            {
                                init();
                                if (blockIterator == null || blockIterator.hasNext() == false)
                                {
                                    blockIterator = tryNextBlockIterator();
                                    if (blockIterator == null || blockIterator.hasNext() == false)
                                    {
                                        return null;
                                    }
                                }
                                return new CellLevelFeatureBlock(featureGroup, id,
                                        blockIterator.next());
                            }

                            public boolean hasNext()
                            {
                                if (next == null)
                                {
                                    next = tryFindNext();
                                }
                                return (next != null);
                            }

                            public CellLevelFeatureBlock next()
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
        final String[] featureGroupNames = reader.readStringArray(getFeatureGroupsFilename());
        final List<IFeatureGroup> result = new ArrayList<IFeatureGroup>(featureGroupNames.length);
        for (String name : featureGroupNames)
        {
            final HDF5CompoundType<Object[]> type =
                    reader.getNamedCompoundType(getNameInDataset(name), Object[].class);
            result.add(new FeatureGroup(name, type));
        }
        return result;
    }

    public IFeatureGroup getFeatureGroup(String name)
    {
        final HDF5CompoundType<Object[]> type =
                reader.getNamedCompoundType(getNameInDataset(name), Object[].class);
        return new FeatureGroup(name, type);
    }

    String getFeatureGroupsFilename()
    {
        return getDatasetCode() + "/featureGroups";
    }

}
