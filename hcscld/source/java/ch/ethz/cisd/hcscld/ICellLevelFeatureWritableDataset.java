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
    /**
     * Adds a new feature group to this dataset.
     * 
     * @param name The name of the feature group.
     * @param features The features defined as HDF5 compound members. The order of the features in
     *            this list establish an order and thus an index for each feature.
     * @return The new feature group.
     */
    public IFeatureGroup addFeatureGroup(String name, final List<HDF5CompoundMemberMapping> features);

    /**
     * Adds a new feature group to this dataset.
     * 
     * @param name The name of the feature group.
     * @param features The features defined as HDF5 compound members. The order of the features in
     *            this list establish an order and thus an index for each feature.
     * @param blockSize The block size of the storage form (in feature records).
     * @return The new feature group.
     */
    public IFeatureGroup addFeatureGroup(String name,
            final List<HDF5CompoundMemberMapping> features, int blockSize);

    /**
     * Adds a new feature group to this dataset.
     * 
     * @param name The name of the feature group.
     * @param features The features defined as HDF5 compound members. The order of the features in
     *            this list establish an order and thus an index for each feature.
     * @param blockSize The block size (in feature records) of the storage form.
     * @param size The size (in feature records) of the feature group on the storage.
     * @return The new feature group.
     */
    public IFeatureGroup addFeatureGroup(String name, List<HDF5CompoundMemberMapping> features,
            long blockSize, int size);

    /**
     * Writes the values of the feature group to the storage form.
     * 
     * @param featureGroup The feature group to write.
     * @param id The well and field to write the features for.
     * @param features The feature values to write. The first index is the object id, the second
     *            index is the feature index as defined by {@link #addFeatureGroup(String, List)}.
     */
    public void writeFeatureGroup(IFeatureGroup featureGroup, WellFieldId id, Object[][] features);

    /**
     * Writes the values of one block of the feature group to the storage form.
     * 
     * @param featureGroup The feature group to write.
     * @param id The well and field to write the features for.
     * @param features The feature values to write. The first index is the object id, the second
     *            index is the feature index as defined by {@link #addFeatureGroup(String, List)}.
     * @param blockNumber The number of the block to write.
     */
    public void writeFeatureGroup(IFeatureGroup featureGroup, WellFieldId id, Object[][] features,
            long blockNumber);
}
