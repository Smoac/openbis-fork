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
    public ICellLevelFeatureWritableDataset toFeatureDataset();

    /**
     * Returns the data set as {@link ICellLevelClassificationWritableDataset}, if the data set is a
     * classification data set and <code>null</code> otherwise.
     */
    public ICellLevelClassificationWritableDataset toClassificationDataset();

    /**
     * Returns the data set as {@link ICellLevelSegmentationWritableDataset}, if the data set is a
     * segmentation data set and <code>null</code> otherwise.
     */
    public ICellLevelSegmentationWritableDataset toSegmentationDataset();
    
    /**
     * Add annotations for a time series image sequence. 
     */
    public void addTimeSeriesSequenceAnnotation(HDF5TimeDurationArray timeValues);
    
    /**
     * Add annotations for a depth scan image sequence. 
     */
    public void addDepthScanSequenceAnnotation(DepthScanAnnotation zValues);

    /**
     * Add annotations for a custom image sequence.
     */
    public void addCustomSequenceAnnotation(String[] customDescriptions);

}
