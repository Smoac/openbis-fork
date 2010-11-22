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

package ch.systemsx.cisd.openbis.dss.screening.server;

import java.io.InputStream;
import java.util.List;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreeningInternal;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
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
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DssServiceRpcScreeningLogger extends AbstractServerLogger implements
        IDssServiceRpcScreeningInternal
{

    DssServiceRpcScreeningLogger(IInvocationLoggerContext context)
    {
        super(null, context);
    }

    public int getMajorVersion()
    {
        return 0;
    }

    public int getMinorVersion()
    {
        return 0;
    }

    public List<String> listAvailableFeatureNames(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        logAccess(sessionToken, "load_available_feature_codes", "DATASET_REFERENCES(%s)",
                featureDatasets);
        return null;
    }

    public List<String> listAvailableFeatureCodes(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        logAccess(sessionToken, "load_available_feature_names", "DATASET_REFERENCES(%s)",
                featureDatasets);
        return null;
    }

    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureCodes)
    {
        logAccess(sessionToken, "load_features", "DATASET_REFERENCES(%s) FEATURES(%s)",
                featureDatasets, featureCodes);
        return null;
    }

    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken, List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureCodes)
    {
        logAccess(sessionToken, "load_feature_for_dataset_well_references",
                "WELL_REFERENCES(%s) FEATURES(%s)", datasetWellReferences, featureCodes);
        return null;
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            boolean convertToPng)
    {
        logAccess(sessionToken, "load_images", "IMAGE_REFERENCES(%s) CONVERT(%s)", imageReferences,
                convertToPng);
        return null;
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences)
    {
        logAccess(sessionToken, "load_images", "IMAGE_REFERENCES(%s)", imageReferences);
        return null;
    }

    public InputStream loadImages(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
    {
        logAccess(sessionToken, "load_images", "DATA_SET(%s) CHANNEL(%s) IMAGE_SIZE(%s)",
                dataSetIdentifier, channel, thumbnailSizeOrNull);
        return null;
    }

    public void saveImageTransformerFactory(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel,
            IImageTransformerFactory transformerFactory)
    {
        logTracking(sessionToken, "save_image_transformer_factory",
                "DATA_SETS(%s) CHANNEL(%s) FACTORY(%s)", dataSetIdentifiers, channel,
                transformerFactory);
    }

    public IImageTransformerFactory getImageTransformerFactoryOrNull(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel)
    {
        logAccess(sessionToken, "get_image_transformer_factory", "DATA_SETS(%s) CHANNEL(%s)",
                dataSetIdentifiers, channel);
        return null;
    }

    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        logAccess(sessionToken, "load_image_metadata", "DATA_SETS(%s)", imageDatasets);
        return null;
   }

    public void checkDatasetsAuthorizationForIDatasetIdentifier(String sessionToken,
            List<? extends IDatasetIdentifier> featureDatasets)
    {
        // server already logs
    }

}
