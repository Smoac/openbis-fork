/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.screening.shared.api;

import java.io.InputStream;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.PlateImageReference;

/**
 * Public DSS API for screening. Non-compatible changes without consultation are forbidden.
 * 
 * @author Tomasz Pylak
 */
public interface IDssServiceRpcScreening
{
    /**
     * For a given set of feature vector data sets provide the list of all available features. This
     * is just the name of the feature. If for different data sets different sets of features are
     * available, provide the union of the features of all data sets.
     */
    List<String> listAvailableFeatureNames(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * For a given set of data sets and a set of features (given by their name), provide the feature
     * matrix. Each column in that matrix is one feature, each row is one well in one data set.
     */
    List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets,
            List<String> featureNames);

    /**
     * Provide image for a given image reference (given by data set code, well position, channel and
     * tile).
     */
    InputStream loadImage(String sessionToken, PlateImageReference imageReferences);

    /**
     * For a given set of image data sets, provide all image channels that have been acquired and
     * the available (natural) image size(s).
     */
    List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets);

}
