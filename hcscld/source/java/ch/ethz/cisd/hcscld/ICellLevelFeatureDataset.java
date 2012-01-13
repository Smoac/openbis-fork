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
 * An interface for feature datasets on the cell level.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelFeatureDataset extends ICellLevelDataset
{
    /**
     * Returns all available feature groups. This list is read-only.
     */
    public List<IFeatureGroup> getFeatureGroups();

    /**
     * Returns the feature group with the given <var>id</var>.
     * 
     * @throws IllegalArgumentException If a feature group with that id doesn't exist.
     */
    public IFeatureGroup getFeatureGroup(String id)  throws IllegalArgumentException;

    /**
     * Returns all feature group name spaces in this dataset.
     */
    public List<FeatureGroupNamespace> getNamespaces();

    /**
     * Returns the name space with the given <var>id</var>.
     * 
     * @throws IllegalArgumentException If a feature group name space with that id doesn't exist.
     */
    public FeatureGroupNamespace getNamespace(String id)  throws IllegalArgumentException;

    /**
     * Returns all feature values for the given <var>cellId</var> of field <var>id</var>.
     */
    public Object[] getValues(ImageId id, FeatureGroupNamespace namespace, int cellId);

    /**
     * Returns all feature values for all cells of field <var>id</var>.
     */
    public Object[][] getValues(ImageId id, FeatureGroupNamespace namespace);

    /**
     * Returns all feature values for all cells in all wells in the given <var>namespace</var>.
     */
    public Iterable<CellLevelFeatures> getValues(FeatureGroupNamespace namespace);

    /**
     * Returns all feature values for the given <var>cellId</var> of field <var>id</var>.
     * 
     * @throws IllegalStateException If the dataset has more than one feature group name space.
     */
    public Object[] getValues(ImageId id, int cellId) throws IllegalStateException;

    /**
     * Returns all feature values for all cells of field <var>id</var>.
     * 
     * @throws IllegalStateException If the dataset has more than one feature group name space.
     */
    public Object[][] getValues(ImageId id) throws IllegalStateException;

    /**
     * Returns all feature values for all cells in all wells.
     * 
     * @throws IllegalStateException If the dataset has more than one feature group name space.
     */
    public Iterable<CellLevelFeatures> getValues() throws IllegalStateException;

    /**
     * Returns the feature values for the given <var>cellId</var> of field <var>id</var> in the
     * given <var>featureGroup</var>.
     */
    public Object[] getValues(ImageId id, IFeatureGroup featureGroup, int cellId);

    /**
     * Returns the feature values for all cells of field <var>id</var> in the given
     * <var>featureGroup</var>.
     * 
     * @return The feature values; the first index is the cell id, the second index is the feature
     *         index.
     */
    public Object[][] getValues(ImageId id, IFeatureGroup featureGroup);

    /**
     * Returns the feature values for all cells in all wells in the given <var>featureGroup<var>.
     */
    public Iterable<CellLevelFeatures> getValues(IFeatureGroup featureGroup);

}
