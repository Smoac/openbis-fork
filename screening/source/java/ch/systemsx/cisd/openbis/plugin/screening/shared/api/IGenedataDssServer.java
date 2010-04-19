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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ScreenerDatasetPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ScreenerWellPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.PlateSingleImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.WellFeaturesReference;

/**
 * @author Tomasz Pylak
 */
public interface IGenedataDssServer
{
    /**
     * For a given set of feature vector data sets provide the list of all available features. This
     * is just the name of the feature. If for different data sets different sets of features are
     * available, provide the union of the features of all data sets.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    List<String> listAvailableFeatureNames(
            String sessionToken,
            @AuthorizationGuard(guardClass = ScreenerDatasetPredicate.class) List<IDatasetIdentifier> featureDatasets);

    /**
     * For a given set of data sets and a set of features (given by their name), provide the feature
     * matrix. Each column in that matrix is one feature, each row is one well in one data set.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    List<FeatureVectorDataset> loadFeatures(
            String sessionToken,
            @AuthorizationGuard(guardClass = ScreenerDatasetPredicate.class) List<IDatasetIdentifier> featureDatasets,
            List<String> featureNames);

    /**
     * For a given set of wells (given by data set code / well position), provide all images for all
     * channels and tiles. To be more precise: find the image parent data set for each feature
     * vector data set given by data set code and then provide the images for a sub set of the wells
     * of this image data set. Do this for all feature vector data sets.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    List<PlateSingleImage> loadWellImages(
            String sessionToken,
            @AuthorizationGuard(guardClass = ScreenerWellPredicate.class) List<WellFeaturesReference> wells);

    /**
     * For a given set of image data sets, provide all image channels that have been acquired and
     * the available (natural) image size(s).
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    List<ImageDatasetMetadata> listImageMetadata(
            String sessionToken,
            @AuthorizationGuard(guardClass = ScreenerDatasetPredicate.class) List<IDatasetIdentifier> imageDatasets);

}
