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

import java.util.List;

import ch.systemsx.cisd.hdf5.HDF5CompoundMemberMapping;
import ch.systemsx.cisd.hdf5.HDF5CompoundType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * A writable dataset for cell-level features.
 * 
 * @author Bernd Rinn
 */
public class CellLevelFeatureWritableDataset extends CellLevelFeatureDataset
        implements ICellLevelFeatureWritableDataset
{
    private final CellLevelBaseWritableDataset base;
    
    CellLevelFeatureWritableDataset(final IHDF5Writer writer, final String datasetCode,
            final WellFieldGeometry geometry, final HDF5EnumerationType hdf5KindEnum)
    {
        super(writer, datasetCode, geometry);
        this.base =
                new CellLevelBaseWritableDataset(writer, datasetCode, geometry, hdf5KindEnum,
                        CellLevelDatasetType.FEATURES);
        writer.createStringArray(getFeatureGroupsFilename(), 100, 0, 1);
    }

    @Override
    public ICellLevelFeatureWritableDataset tryAsFeatureDataset()
    {
        return this;
    }

    @Override
    public ICellLevelClassificationWritableDataset tryAsClassificationDataset()
    {
        return null;
    }

    public HDF5EnumerationType addEnum(String name, List<String> values)
    {
        return base.addEnum(name, values);
    }

    public HDF5EnumerationType addEnum(Class<? extends Enum<?>> enumClass)
    {
        return base.addEnum(enumClass);
    }

    public IFeatureGroup addFeatureGroup(final String name,
            final List<HDF5CompoundMemberMapping> members)
    {
        return addFeatureGroup(name, members, 0L, -1);
    }

    public IFeatureGroup addFeatureGroup(final String name,
            final List<HDF5CompoundMemberMapping> members, final int blockSize)
    {
        return addFeatureGroup(name, members, 0L, blockSize);
    }

    public IFeatureGroup addFeatureGroup(final String name,
            final List<HDF5CompoundMemberMapping> members, final long size, final int blockSize)
    {
        final HDF5CompoundType<Object[]> type =
                base.writer.getCompoundType(getNameInDataset(name), Object[].class,
                        members.toArray(new HDF5CompoundMemberMapping[members.size()]));
        final FeatureGroup featureGroup = new FeatureGroup(name, type);
        final String featureGroupsFile = getFeatureGroupsFilename();
        base.writer.writeStringArrayBlock(featureGroupsFile, new String[]
            { name }, base.writer.getNumberOfElements(featureGroupsFile));
        if (blockSize > 0)
        {
            base.run(new IWellFieldRunnable()
                {
                    public void run(WellFieldId id, Object state)
                    {
                        base.writer.createCompoundArray(featureGroup.getObjectPath(id), type, size,
                                blockSize);
                    }
                }, null);
        }
        return featureGroup;
    }

    public void writeFeatureGroup(IFeatureGroup featureGroup, WellFieldId id, Object[][] features)
    {
        final FeatureGroup fg = (FeatureGroup) featureGroup;
        base.writer.writeCompoundArray(fg.getObjectPath(id), fg.getType(), features);
    }

    public void writeFeatureGroup(IFeatureGroup featureGroup, WellFieldId id, Object[][] features,
            long blockNumber)
    {
        final FeatureGroup fg = (FeatureGroup) featureGroup;
        base.writer.writeCompoundArrayBlock(fg.getObjectPath(id), fg.getType(), features,
                blockNumber);
    }

}
