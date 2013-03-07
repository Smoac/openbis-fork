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

import ch.systemsx.cisd.hdf5.HDF5TimeDurationArray;

/**
 * Base class for all writable cell level data sets.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelWritableDataset extends ICellLevelDataset
{
    /**
     * Returns the data set as {@link ICellLevelFeatureWritableDataset}, if the data set is a
     * feature data set and <code>null</code> otherwise.
     */
    @Override
    public ICellLevelFeatureWritableDataset toFeatureDataset();

    /**
     * Returns the data set as {@link ICellLevelClassificationWritableDataset}, if the data set is a
     * classification data set and <code>null</code> otherwise.
     */
    @Override
    public ICellLevelClassificationWritableDataset toClassificationDataset();

    /**
     * Returns the data set as {@link ICellLevelSegmentationWritableDataset}, if the data set is a
     * segmentation data set and <code>null</code> otherwise.
     */
    @Override
    public ICellLevelSegmentationWritableDataset toSegmentationDataset();

    /**
     * Adds a new object type to this dataset with the given <var>objectTypeId</var>.
     * 
     * @return The object type.
     * @throws UniqueViolationException if an object type with <var>id</var> already
     *             exists.
     */
    public ObjectType addObjectType(String id)
            throws UniqueViolationException;

    /**
     * Adds a new object type to this dataset with the given <var>id</var> and the
     * <var>group></var>.
     * 
     * @return The object type.
     * @throws UniqueViolationException if an object type with <var>id</var> already
     *             exists.
     */
    public ObjectType addObjectType(String id, ObjectNamespace group)
            throws UniqueViolationException;

    /**
     * Adds a new object namespace to this dataset with the given <var>id</var>.
     * 
     * @return The new object namespace.
     * @throws UniqueViolationException if a group with <var>id</var> already
     *             exists.
     */
    public ObjectNamespace addObjectNamespace(String id);

    /**
     * Add annotations for a time series image sequence. The annotation is supposed to provide the
     * timepoint for each image of the sequence, ordered by sequence index.
     */
    public void setTimeSeriesSequenceAnnotation(HDF5TimeDurationArray timeValues);

    /**
     * Add annotations for a depth scan image sequence. The annotation is supposed to provide the
     * z-value for each image of the sequence, ordered by sequence index.
     */
    public void setDepthScanSequenceAnnotation(DepthScanAnnotation zValues);

    /**
     * Add annotations for a custom image sequence. The annotation is supposed to provide a
     * description for each image of the sequence, ordered by sequence index.
     */
    public void setCustomSequenceAnnotation(String[] customDescriptions);

    /**
     * Sets the plate barcode that this dataset refers to.
     */
    public void setPlateBarcode(String plateBarcode);

    /**
     * Sets the parent dataset code of this dataset.
     */
    public void setParentDatasetCode(String parentDatasetCode);

    /**
     * Sets a custom dataset <var>annotation</var> with key <var>annoationKey</var>.
     */
    public void addDatasetAnnotation(String annotationKey, String annotation);
}
