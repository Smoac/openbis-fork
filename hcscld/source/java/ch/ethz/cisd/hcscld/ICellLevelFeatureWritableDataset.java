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
 * An interface for a writable dataset of cell-level features.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelFeatureWritableDataset extends ICellLevelWritableDataset,
        ICellLevelFeatureDataset
{
    /**
     * Creates an (empty) object to define the features of a feature group.
     */
    public IFeaturesDefinition createFeaturesDefinition();

    /**
     * Writes the values of the feature group to the dataset.
     * 
     * @param id The well and field to write the features for.
     * @param featureGroup The feature group to write.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the <var>featureGroup</var>.
     */
    public void writeFeatures(WellFieldId id, IFeatureGroup featureGroup, Object[][] featureValues);

    /**
     * Writes the values of the features to the dataset.
     * <p>
     * <i>This method requires that {@link IFeaturesDefinition#create()} has been called before.</i>
     * 
     * @param id The well and field to write the features for.
     * @param featureValues The feature values to write. The first index is the object id, the
     *            second index is the feature index as defined by the default feature group.
     */
    public void writeFeatures(WellFieldId id, Object[][] featureValues);

}
