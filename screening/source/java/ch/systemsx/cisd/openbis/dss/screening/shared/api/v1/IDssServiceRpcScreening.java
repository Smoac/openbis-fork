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

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.api.MinimalMinorVersion;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.AuthorizationGuard;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.DataSetAccessGuard;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * Public DSS API for screening. Since version 1.2 features are no longer identified by a name but
 * by a code. Previous client code still works but all name will be normalized internally.
 * Normalized means that the original code arguments turn to upper case and any symbol which isn't
 * from A-Z or 0-9 is replaced by an underscore character. {@link FeatureVectorDataset} will provide
 * feature codes and feature labels.
 * 
 * @author Tomasz Pylak
 */
// Non-compatible changes without consultation are forbidden.
public interface IDssServiceRpcScreening extends IRpcService
{
    /**
     * The major version of this service.
     */
    public static final int MAJOR_VERSION = 1;

    /**
     * For a given set of feature vector data sets provide the list of all available features. This
     * is just the name of the feature. If for different data sets different sets of features are
     * available, provide the union of the features of all data sets.
     */
    @Deprecated
    @DataSetAccessGuard
    public List<String> listAvailableFeatureNames(
            String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * For a given set of feature vector data sets provide the list of all available features. This
     * is just the code of the feature. If for different data sets different sets of features are
     * available, provide the union of the features of all data sets.
     */
    @MinimalMinorVersion(2)
    @DataSetAccessGuard
    public List<String> listAvailableFeatureCodes(
            String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<? extends IFeatureVectorDatasetIdentifier> featureDatasets);

    /**
     * Conceptually, for a given list of data well references (i.e. specified wells on specified
     * feature vector data sets) and a set of features (given by their name) provide the feature
     * matrix. In this matrix, each column is one feature, each row is one well in one data set.
     * <p>
     * Physically, the result is delivered as a list of feature vector datasets. Each entry in this
     * list corresponds to one well in one dataset.
     * 
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the
     *         <var>featureDatasets</var>.
     */
    @DataSetAccessGuard
    public List<FeatureVectorDataset> loadFeatures(
            String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<FeatureVectorDatasetReference> featureDatasets,
            List<String> featureCodes);

    /**
     * Conceptually, for a given list of dataset well references (i.e. specified wells on specified
     * feature vector data sets) and a set of features (given by their name) provide the feature
     * matrix. In this matrix, each column is one feature, each row is one well in one data set.
     * <p>
     * Physically, the result is delivered as a list of feature vectors. Each entry in this list
     * corresponds to one well in one dataset.
     * 
     * @return The list of {@link FeatureVectorWithDescription}s, each element corresponds to one of
     *         the <var>datasetWellReferences</var>. <b>Note that the order of the returned is
     *         <i>not</i> guaranteed to be the same as the order of the list
     *         <var>datasetWellReferences</var>. Use
     *         {@link FeatureVectorWithDescription#getDatasetWellReference()} to find the
     *         corresponding dataset / well.</b>
     * @since 1.1
     */
    @MinimalMinorVersion(1)
    @DataSetAccessGuard
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureCodes);

    /**
     * Provide images for a given list of image references (given by data set code, well position,
     * channel and tile). The result is encoded into one stream, which consist of multiple blocks in
     * a format: (<block-size><block-of-bytes>)*, where block-size is the block size in bytes
     * encoded as one long number. The number of blocks is equal to the number of specified
     * references and the order of blocks corresponds to the order of image references. If
     * <code>convertToPng==true</code>, the images will be converted to PNG format before being
     * shipped, otherwise they will be shipped in the format that they are stored on the server.
     * 
     * @since 1.3
     */
    @MinimalMinorVersion(3)
    @DataSetAccessGuard
    public InputStream loadImages(
            String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<PlateImageReference> imageReferences,
            boolean convertToPng);

    /**
     * Provide images for a given list of image references (given by data set code, well position,
     * channel and tile). The result is encoded into one stream, which consist of multiple blocks in
     * a format: (<block-size><block-of-bytes>)*, where block-size is the block size in bytes
     * encoded as one long number. The number of blocks is equal to the number of specified
     * references and the order of blocks corresponds to the order of image references. The images
     * will be converted to PNG format before being shipped.
     */
    @DataSetAccessGuard
    public InputStream loadImages(
            String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<PlateImageReference> imageReferences);

    /**
     * Provide images for a specified data set, a list of well positions (empty list means all
     * wells), a channel, and an optional thumb nail size. Images of all tiles are delivered. If
     * thumb nail size isn't specified the original image is delivered otherwise a thumb nail image
     * with same aspect ratio as the original image but which fits into specified size will be
     * delivered.
     * <p>
     * The result is encoded into one stream, which consist of multiple blocks in a format:
     * (<block-size><block-of-bytes>)*, where block-size is the block size in bytes encoded as one
     * long number. The number of blocks is equal to the number of specified references and the
     * order of blocks corresponds to the order of image references. The images will be converted to
     * PNG format before being shipped.
     * 
     * @since 1.4
     */
    @MinimalMinorVersion(4)
    @DataSetAccessGuard
    public InputStream loadImages(
            String sessionToken,
            @AuthorizationGuard(guardClass = SingleDataSetIdentifierPredicate.class) IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull);

    /**
     * For a given set of image data sets, provide all image channels that have been acquired and
     * the available (natural) image size(s).
     */
    @DataSetAccessGuard
    public List<ImageDatasetMetadata> listImageMetadata(
            String sessionToken,
            @AuthorizationGuard(guardClass = DatasetIdentifierPredicate.class) List<? extends IImageDatasetIdentifier> imageDatasets);

}
