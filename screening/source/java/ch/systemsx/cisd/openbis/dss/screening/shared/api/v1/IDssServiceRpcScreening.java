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

package ch.systemsx.cisd.openbis.dss.screening.shared.api.v1;

import java.io.InputStream;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;

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
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureNames);

    /**
     * Provide images for a given list of image references (given by data set code, well position,
     * channel and tile). The result is encoded into one stream, which consist of multiple blocks in
     * a format: (<block-size><block-of-bytes>)*, where block-size is the block size in bytes
     * encoded as one long number. The number of blocks is equal to the number of specified
     * references and the order of blocks corresponds to the order of image references.
     */
    InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences);

    /**
     * For a given set of image data sets, provide all image channels that have been acquired and
     * the available (natural) image size(s).
     */
    List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets);

}
