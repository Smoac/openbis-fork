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

/**
 * Base interface for all cell level datasets.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelDataset
{
    /**
     * Returns the code of the dataset.
     */
    public String getDatasetCode();

    /**
     * Returns the well/field geometry.
     */
    public WellFieldGeometry getGeometry();

    /**
     * Returns the type of the dataset.
     */
    public CellLevelDatasetType getType();

    /**
     * Returns the dataset as a feature dataset, if {@link #getType()} ==
     * {@link CellLevelDatasetType#FEATURES}, or <code>null</code> otherwise.
     */
    public ICellLevelFeatureDataset tryAsFeatureDataset();

    /**
     * Returns the dataset as a classification dataset, if {@link #getType()} ==
     * {@link CellLevelDatasetType#CLASSIFICATION}, or <code>null</code> otherwise.
     */
    public ICellLevelClassificationDataset tryAsClassificationDataset();

    /**
     * Returns the dataset as an image segmentation dataset, if {@link #getType()} ==
     * {@link CellLevelDatasetType#SEGMENTATION}, or <code>null</code> otherwise.
     */
    public ICellLevelSegmentationDataset tryAsSegmentationDataset();
}
