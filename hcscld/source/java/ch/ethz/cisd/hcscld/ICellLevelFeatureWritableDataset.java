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

/**
 * An interface for a writable dataset of cell-level features.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelFeatureWritableDataset extends ICellLevelWritableDataset,
        ICellLevelFeatureDataset
{
    public IFeatureGroup addFeatureGroup(String name, final List<HDF5CompoundMemberMapping> members);

    public IFeatureGroup addFeatureGroup(String name,
            final List<HDF5CompoundMemberMapping> members, int blockSize);

    public IFeatureGroup addFeatureGroup(String name, List<HDF5CompoundMemberMapping> members,
            long blockSize, int size);

    public void writeFeatureGroup(IFeatureGroup featureGroup, WellFieldId id, Object[][] features);

    public void writeFeatureGroup(IFeatureGroup featureGroup, WellFieldId id, Object[][] features,
            long blockNumber);
}
