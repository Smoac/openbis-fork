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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.api.server.RpcServiceNameServer;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IHCSImageDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.FeatureTableBuilder;
import ch.systemsx.cisd.openbis.dss.generic.server.FeatureTableRow;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDatasetDownloadServlet.Size;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabel;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;

/**
 * Implementation of the screening API interface using RPC. The instance will be created in spring
 * and published as a HTTP invoker servlet configured in service.properties.
 * 
 * @author Tomasz Pylak
 */
public class DssServiceRpcScreening extends AbstractDssServiceRpc implements
        IDssServiceRpcScreening
{

    /**
     * The minor version of this service.
     */
    public static final int MINOR_VERSION = 2;

    // this dao will hold one connection to the database
    private IImagingReadonlyQueryDAO dao;

    public DssServiceRpcScreening(String storeRootDir)
    {
        this(storeRootDir, null, ServiceProvider.getOpenBISService(), true);
    }

    DssServiceRpcScreening(String storeRootDir, IImagingReadonlyQueryDAO dao,
            IEncapsulatedOpenBISService service, boolean registerAtNameService)
    {
        super(service);
        this.dao = dao;
        setStoreDirectory(new File(storeRootDir));
        if (registerAtNameService)
        {
            // Register the service with the name server
            RpcServiceInterfaceVersionDTO ifaceVersion =
                    new RpcServiceInterfaceVersionDTO("screening-dss",
                            "/rmi-datastore-server-screening-api-v1", getMajorVersion(),
                            getMinorVersion());
            HttpInvokerServiceExporter nameServiceExporter =
                    ServiceProvider.getRpcNameServiceExporter();
            RpcServiceNameServer nameServer =
                    (RpcServiceNameServer) nameServiceExporter.getService();
            nameServer.addSupportedInterfaceVersion(ifaceVersion);

            operationLog.info("[rpc] Started DSS RPC screening service V1.");
        }
    }

    // ------------------ impl -----------------

    public List<String> listAvailableFeatureNames(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return listAvailableFeatureCodes(sessionToken, featureDatasets);
    }

    public List<String> listAvailableFeatureCodes(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        checkDatasetsAuthorizationForIDatasetIdentifier(sessionToken, featureDatasets);
        List<String> result = new ArrayList<String>(); // keep the order
        for (IFeatureVectorDatasetIdentifier identifier : featureDatasets)
        {
            // add only new feature names
            List<ImgFeatureDefDTO> featureDefinitions = getFeatureDefinitions(identifier);
            for (ImgFeatureDefDTO featureDefinition : featureDefinitions)
            {
                String featureCode = featureDefinition.getCode();
                if (result.contains(featureCode) == false)
                {
                    result.add(featureCode);
                }
            }
        }
        return result;
    }

    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        checkDatasetsAuthorizationForIDatasetIdentifier(sessionToken, imageDatasets);
        ArrayList<String> datasetCodes = new ArrayList<String>();
        for (IImageDatasetIdentifier dataset : imageDatasets)
        {
            datasetCodes.add(dataset.getDatasetCode());
        }
        Map<String, File> datasetRoots =
                checkAccessAndGetRootDirectories(sessionToken, datasetCodes);
        List<ImageDatasetMetadata> result = new ArrayList<ImageDatasetMetadata>();
        for (IImageDatasetIdentifier dataset : imageDatasets)
        {
            File rootDirectoryOrNull = datasetRoots.get(dataset.getDatasetCode());
            if (rootDirectoryOrNull != null)
            {
                result.add(extractImageMetadata(dataset, rootDirectoryOrNull));
            }
        }
        return result;
    }

    private static ImageDatasetMetadata extractImageMetadata(IImageDatasetIdentifier dataset,
            File datasetRoot)
    {
        IHCSImageDatasetLoader imageAccessor =
                HCSImageDatasetLoaderFactory.create(datasetRoot, dataset.getDatasetCode());
        Size imageSize = getImageSize(dataset, imageAccessor);
        PlateImageParameters params = imageAccessor.getImageParameters();
        int tilesNumber = params.getTileColsNum() * params.getTileRowsNum();
        return new ImageDatasetMetadata(dataset, params.getChannelsCodes(), params
                .getChannelsLabels(), tilesNumber, imageSize.getWidth(), imageSize.getHeight());
    }

    private static Size getImageSize(IImageDatasetIdentifier dataset,
            IHCSImageDatasetLoader imageAccessor)
    {
        IContent imageFile = getAnyImagePath(imageAccessor, dataset);
        BufferedImage image = ImageUtil.loadImage(imageFile.getInputStream());
        Size imageSize = new Size(image.getWidth(), image.getHeight());
        return imageSize;
    }

    private static IContent getAnyImagePath(IHCSImageDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        PlateImageParameters params = imageAccessor.getImageParameters();
        for (int row = 1; row <= params.getRowsNum(); row++)
        {
            for (int col = 1; col <= params.getColsNum(); col++)
            {
                for (int tileRow = 1; tileRow <= params.getTileRowsNum(); tileRow++)
                {
                    for (int tileCol = 1; tileCol <= params.getTileColsNum(); tileCol++)
                    {
                        for (String channelCode : params.getChannelsCodes())
                        {
                            ImageChannelStackReference channelStackReference =
                                    ImageChannelStackReference.createFromLocations(new Location(
                                            col, row), Location.tryCreateLocationFromRowAndColumn(
                                            tileRow, tileCol));
                            AbsoluteImageReference image =
                                    imageAccessor.tryGetImage(channelCode, channelStackReference,
                                            null);
                            if (image != null)
                            {
                                return image.getContent();
                            }
                        }
                    }
                }
            }
        }
        throw new IllegalStateException("Cannot find any image in a dataset: " + dataset);
    }

    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureNames)
    {
        checkDatasetsAuthorizationForIDatasetIdentifier(sessionToken, featureDatasets);
        List<String> codes = normalize(featureNames);
        List<FeatureVectorDataset> result = new ArrayList<FeatureVectorDataset>();
        for (FeatureVectorDatasetReference dataset : featureDatasets)
        {
            result.add(createFeatureVectorDataset(sessionToken, dataset, codes));
        }
        return result;
    }

    private FeatureVectorDataset createFeatureVectorDataset(String sessionToken,
            FeatureVectorDatasetReference dataset, List<String> featureCodes)
    {
        FeatureTableBuilder builder =
                new FeatureTableBuilder(featureCodes, getDAO(), getOpenBISService());
        builder.addFeatureVectorsOfDataSet(dataset.getDatasetCode());
        List<FeatureTableRow> featureTableRows = builder.createFeatureTableRows();
        List<FeatureVector> featureVectors = new ArrayList<FeatureVector>();
        for (FeatureTableRow featureTableRow : featureTableRows)
        {
            WellPosition wellPosition = featureTableRow.getWellPosition();
            double[] values = featureTableRow.getFeatureValuesAsDouble();
            featureVectors.add(new FeatureVector(wellPosition, values));
        }
        List<String> codes = getCodes(builder);
        List<String> labels = getLabels(builder);
        return new FeatureVectorDataset(dataset, codes, labels, featureVectors);
    }

    private List<String> normalize(List<String> names)
    {
        ArrayList<String> codes = new ArrayList<String>(names.size());
        for (String name : names)
        {
            codes.add(CodeAndLabel.normalize(name));
        }
        return codes;
    }

    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken, List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureNames)
    {
        checkDatasetsAuthorizationForIDatasetIdentifier(sessionToken, datasetWellReferences);
        final FeatureTableBuilder builder =
                createFeatureTableBuilder(datasetWellReferences, normalize(featureNames));
        return createFeatureVectorList(builder);
    }

    private List<FeatureVectorWithDescription> createFeatureVectorList(
            final FeatureTableBuilder builder)
    {
        final List<String> featureCodes = getCodes(builder);
        final List<FeatureTableRow> featureTableRows = builder.createFeatureTableRows();
        final List<FeatureVectorWithDescription> result =
                new ArrayList<FeatureVectorWithDescription>(featureTableRows.size());
        for (FeatureTableRow featureTableRow : featureTableRows)
        {
            result.add(createFeatureVector(featureTableRow, featureCodes));
        }
        return result;
    }

    private List<String> getCodes(FeatureTableBuilder builder)
    {
        List<CodeAndLabel> featureCodesAndLabels = builder.getCodesAndLabels();
        List<String> codes = new ArrayList<String>();
        for (CodeAndLabel codeAndTitle : featureCodesAndLabels)
        {
            codes.add(codeAndTitle.getCode());
        }
        return codes;
    }

    private List<String> getLabels(FeatureTableBuilder builder)
    {
        List<CodeAndLabel> featureCodesAndLabels = builder.getCodesAndLabels();
        List<String> labels = new ArrayList<String>();
        for (CodeAndLabel codeAndTitle : featureCodesAndLabels)
        {
            labels.add(codeAndTitle.getLabel());
        }
        return labels;
    }

    private FeatureVectorWithDescription createFeatureVector(FeatureTableRow featureTableRow,
            final List<String> featureCodes)
    {
        return new FeatureVectorWithDescription(featureTableRow.getReference(), featureCodes,
                featureTableRow.getFeatureValuesAsDouble());
    }

    private FeatureTableBuilder createFeatureTableBuilder(
            List<FeatureVectorDatasetWellReference> plateWellReferences, List<String> featureCodes)
    {
        final FeatureTableBuilder builder =
                new FeatureTableBuilder(featureCodes, getDAO(), getOpenBISService());
        for (FeatureVectorDatasetWellReference datasetWellReference : plateWellReferences)
        {
            builder.addFeatureVectorsOfDataSet(datasetWellReference);
        }
        return builder;
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences)
    {
        checkDatasetsAuthorizationForIDatasetIdentifier(sessionToken, imageReferences);
        final Map<String, IHCSImageDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);
        final List<IContent> imageFiles = new ArrayList<IContent>();
        for (PlateImageReference imageReference : imageReferences)
        {
            final IHCSImageDatasetLoader imageAccessor =
                    imageLoadersMap.get(imageReference.getDatasetCode());
            assert imageAccessor != null : "imageAccessor not found for: " + imageReference;
            final AbsoluteImageReference imageRef = tryGetImage(imageAccessor, imageReference);
            imageFiles.add((imageRef == null) ? null : imageRef.getContent());
        }
        return new ConcatenatedContentInputStream(true, imageFiles);
    }

    // throws exception if some datasets cannot be found
    private Map<String/* image or feature vector dataset code */, IHCSImageDatasetLoader> getImageDatasetsMap(
            String sessionToken, List<PlateImageReference> imageReferences)
    {
        Map<String/* dataset code */, IHCSImageDatasetLoader> imageDatasetsMap =
                new HashMap<String, IHCSImageDatasetLoader>();
        for (PlateImageReference imageReference : imageReferences)
        {
            if (imageDatasetsMap.containsKey(imageReference.getDatasetCode()) == false)
            {
                ExternalData imageDataset =
                        tryFindImageDataset(sessionToken, imageReference.getDatasetCode());
                if (imageDataset != null)
                {
                    IHCSImageDatasetLoader imageAccessor =
                            createImageLoader(imageDataset.getCode());
                    imageDatasetsMap.put(imageReference.getDatasetCode(), imageAccessor);
                } else
                {
                    throw UserFailureException.fromTemplate(
                            "Cannot find an image dataset for the reference: %s", imageReference);
                }
            }
        }
        return imageDatasetsMap;
    }

    private AbsoluteImageReference tryGetImage(IHCSImageDatasetLoader imageAccessor,
            PlateImageReference imageRef)
    {
        Location wellLocation = asLocation(imageRef.getWellPosition());
        Location tileLocation =
                getTileLocation(imageRef.getTile(), imageAccessor.getImageParameters()
                        .getTileColsNum());
        try
        {
            ImageChannelStackReference channelStackReference =
                    ImageChannelStackReference.createFromLocations(wellLocation, tileLocation);
            return ImageChannelsUtils.getImage(imageAccessor, channelStackReference, imageRef
                    .getChannel(), null);
        } catch (EnvironmentFailureException e)
        {
            operationLog.error("Error reading image.", e);
            return null; // no image found
        }
    }

    // tile - start from 0
    private static Location getTileLocation(int tile, int tileColumnsNum)
    {
        int row = (tile / tileColumnsNum) + 1;
        int col = (tile % tileColumnsNum) + 1;
        return new Location(row, col);
    }

    private static Location asLocation(WellPosition wellPosition)
    {
        return new Location(wellPosition.getWellColumn(), wellPosition.getWellRow());
    }

    private IHCSImageDatasetLoader createImageLoader(String datasetCode)
    {
        File datasetRoot = getRootDirectoryForDataSet(datasetCode);
        return HCSImageDatasetLoaderFactory.create(datasetRoot, datasetCode);
    }

    private ExternalData tryFindImageDataset(String sessionToken, String datasetCode)
    {
        ExternalData dataset = tryGetDataSet(sessionToken, datasetCode);
        if (dataset == null)
        {
            throw new IllegalArgumentException("Dataset " + datasetCode + " cannot be found.");
        }
        if (isImageDataset(dataset))
        {
            return dataset;
        }
        // it may be the feature dataset
        Collection<ExternalData> parents = dataset.getParents();
        if (parents.size() > 1)
        {
            throw new IllegalArgumentException("Dataset " + datasetCode
                    + " should have at most 1 parent, but has: " + parents.size());
        }
        if (parents.size() == 1)
        {
            ExternalData parent = parents.iterator().next();
            if (isImageDataset(parent))
            {
                return parent;
            } else
            {
                return null;
            }
        } else
        {
            return null;
        }
    }

    private boolean isImageDataset(ExternalData dataset)
    {
        return dataset.getDataSetType().getCode().equals(ScreeningConstants.IMAGE_DATASET_TYPE);
    }

    private List<ImgFeatureDefDTO> getFeatureDefinitions(IDatasetIdentifier identifier)
    {
        ImgDatasetDTO dataSet = getDAO().tryGetDatasetByPermId(identifier.getDatasetCode());
        if (dataSet == null)
        {
            throw new UserFailureException("Unknown data set: " + identifier.getDatasetCode());
        }
        return getDAO().listFeatureDefsByDataSetId(dataSet.getId());
    }

    private void checkDatasetsAuthorizationForIDatasetIdentifier(String sessionToken,
            List<? extends IDatasetIdentifier> identifiers)
    {
        List<String> dataSetCodes = new ArrayList<String>();
        for (IDatasetIdentifier identifier : identifiers)
        {
            dataSetCodes.add(identifier.getDatasetCode());
        }
        checkDatasetsAuthorization(sessionToken, dataSetCodes);
    }

    private IImagingReadonlyQueryDAO getDAO()
    {
        synchronized (this)
        {
            if (dao == null)
            {
                dao = DssScreeningUtils.getQuery();
            }
        }
        return dao;
    }

    public int getMajorVersion()
    {
        return MAJOR_VERSION;
    }

    public int getMinorVersion()
    {
        return MINOR_VERSION;
    }

}
