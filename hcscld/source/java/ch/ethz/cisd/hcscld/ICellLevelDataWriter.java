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

/**
 * An interface for writing cell-level data.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelDataWriter extends ICellLevelDataReader
{
    /**
     * Adds a new feature data set.
     * 
     * @param datasetCode The code of the new data set.
     * @param geometry The geometry of the new data set.
     */
    public ICellLevelFeatureWritableDataset addFeatureDataset(String datasetCode,
            WellFieldGeometry geometry);

    /**
     * Adds a new classification data set.
     * 
     * @param datasetCode The code of the new data set.
     * @param geometry The geometry of the new data set.
     * @param enumType The type of the enumeration that represents the classification results.
     */
    public ICellLevelClassificationWritableDataset addClassificationDataset(String datasetCode,
            WellFieldGeometry geometry, Class<? extends Enum<?>> enumType);

    /**
     * Adds a new classification data set.
     * 
     * @param datasetCode The code of the new data set.
     * @param geometry The geometry of the new data set.
     * @param options The options that represents the classification results.
     */
    public ICellLevelClassificationWritableDataset addClassificationDataset(String datasetCode,
            WellFieldGeometry geometry, List<String> options);

    /**
     * Adds a new image segmentation data set.
     * 
     * @param datasetCode The code of the new data set.
     * @param segmentedObjectTypeName A name for the type of object that was segmented for (e.g.
     *            "cell")
     * @param geometry The geometry of the new data set.
     * @param imageGeometry The geometry of the images that have segmented.
     * @param storeEdgeMasks Store the edge masks (compute if necessary) so that they can be
     *            retrieved faster.
     */
    public ICellLevelSegmentationWritableDataset addSegmentationDataset(String datasetCode,
            String segmentedObjectTypeName, WellFieldGeometry geometry,
            ImageGeometry imageGeometry, boolean storeEdgeMasks);
}
