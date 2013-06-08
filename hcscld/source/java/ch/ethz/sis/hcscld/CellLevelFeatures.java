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

package ch.ethz.sis.hcscld;

/**
 * The cell level features of a given well, field and feature group.
 * 
 * @author Bernd Rinn
 */
public class CellLevelFeatures
{
    private final ImageId id;

    private final IFeatureGroup featureGroup;

    private final Object[][] data;

    CellLevelFeatures(IFeatureGroup featureGroup, ImageId id, Object[][] data)
    {
        this.featureGroup = featureGroup;
        this.id = id;
        this.data = data;
    }

    /**
     * Returns the feature group delivered.
     */
    public IFeatureGroup getFeatureGroup()
    {
        return featureGroup;
    }

    /**
     * Returns the values of the features.
     * 
     * @return The features. The first index denotes the object id, the second index denotes the
     *         feature.
     * @see IFeatureGroup#getFeatureNames() 
     */
    public Object[][] getValues()
    {
        return data;
    }

    /**
     * Returns the image id of this block.
     */
    public ImageId getImageId()
    {
        return id;
    }

}
