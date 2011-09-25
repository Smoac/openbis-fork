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
     * Returns all available feature groups.
     */
    public List<IFeatureGroup> getFeatureGroups();

    /**
     * Returns the feature group with the given <var>name</var>.
     */
    public IFeatureGroup getFeatureGroup(String name);

    /**
     * Returns all feature values for the given <var>cellId</var> of field <var>id</var>.
     */
    public Object[] getValues(WellFieldId id, int cellId);

    /**
     * Returns all feature values for all cells of field <var>id</var>.
     */
    public Object[][] getValues(WellFieldId id);

    /**
     * Returns all feature values for all cells in all wells.
     */
    public Iterable<CellLevelFeatures> getValues();

    /**
     * Returns the feature values for the given <var>cellId</var> of field <var>id</var> in the
     * given <var>featureGroup</var>.
     */
    public Object[] getValues(WellFieldId id, IFeatureGroup featureGroup, int cellId);

    /**
     * Returns the feature values for all cells of field <var>id</var> in the given
     * <var>featureGroup</var>.
     * 
     * @return The feature values; the first index is the cell id, the second index is the feature
     *         index.
     */
    public Object[][] getValues(WellFieldId id, IFeatureGroup featureGroup);

    /**
     * Returns the feature values for all cells in all wells in the given <var>featureGroup<var>.
     */
    public Iterable<CellLevelFeatures> getValues(IFeatureGroup featureGroup);

}
