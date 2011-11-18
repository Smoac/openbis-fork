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
 * The interface for a feature group.
 * 
 * @author Bernd Rinn
 */
public interface IFeatureGroup extends Iterable<ImageId>
{
    /**
     * Returns the name of this feature group.
     */
    public String getName();

    /**
     * Returns the number of features in this group.
     */
    public int getNumberOfFeatures();

    /**
     * Returns the names of the features.
     */
    public List<String> getFeatureNames();

    /**
     * Returns information about each feature.
     */
    public List<Feature> getFeatures();

    /**
     * Returns the name of the types of objects that have been used to compute the features of this
     * feature group, or <code>null</code>, if not set.
     */
    public String tryGetObjectType();

}
