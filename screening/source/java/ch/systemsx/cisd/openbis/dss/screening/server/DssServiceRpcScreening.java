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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.api.server.RpcServiceNameServer;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.common.io.ContentProviderBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.common.io.IContentProvider;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabelUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreeningInternal;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
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
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.FeatureTableRow;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.IMetadataProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingTransformerDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.TransformerFactoryMapper;

/**
 * Implementation of the screening API interface using RPC. The instance will be created in spring
 * and published as a HTTP invoker servlet configured in service.properties.
 * 
 * @author Tomasz Pylak
 */
public class DssServiceRpcScreening extends AbstractDssServiceRpc<IDssServiceRpcScreeningInternal>
        implements IDssServiceRpcScreeningInternal
{

    /**
     * The minor version of this service.
     */
    public static final int MINOR_VERSION = 3;

    static
    {
        QueryTool.getTypeMap().put(IImageTransformerFactory.class, new TransformerFactoryMapper());
    }

    // this dao will hold one connection to the database
    private IImagingReadonlyQueryDAO dao;

    private final IImagingTransformerDAO transformerDAO;

    public DssServiceRpcScreening(String storeRootDir)
    {
        this(storeRootDir, null, QueryTool.getQuery(ServiceProvider.getDataSourceProvider()
                .getDataSource(ScreeningConstants.IMAGING_DATA_SOURCE),
                IImagingTransformerDAO.class), ServiceProvider.getOpenBISService(), true);
    }

    DssServiceRpcScreening(String storeRootDir, IImagingReadonlyQueryDAO dao,
            IImagingTransformerDAO transformerDAO, IEncapsulatedOpenBISService service,
            boolean registerAtNameService)
    {
        super(service);
        this.dao = dao;
        this.transformerDAO = transformerDAO;
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

    public IDssServiceRpcScreeningInternal createLogger(IInvocationLoggerContext context)
    {
        return new DssServiceRpcScreeningLogger(context);
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
        Set<String> datasetCodes = new HashSet<String>();
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

    private ImageDatasetMetadata extractImageMetadata(IImageDatasetIdentifier dataset,
            File datasetRoot)
    {
        IImagingDatasetLoader imageAccessor =
                createImageLoader(dataset.getDatasetCode(), datasetRoot);
        Size imageSize = getImageSize(dataset, imageAccessor);
        ImageDatasetParameters params = imageAccessor.getImageParameters();
        int tilesNumber = params.getTileColsNum() * params.getTileRowsNum();
        return new ImageDatasetMetadata(dataset, params.getChannelsCodes(),
                params.getChannelsLabels(), tilesNumber, imageSize.getWidth(),
                imageSize.getHeight());
    }

    private static Size getImageSize(IImageDatasetIdentifier dataset,
            IImagingDatasetLoader imageAccessor)
    {
        IContent imageFile = getAnyImagePath(imageAccessor, dataset);
        BufferedImage image = ImageUtil.loadImage(imageFile.getInputStream());
        Size imageSize = new Size(image.getWidth(), image.getHeight());
        return imageSize;
    }

    // TODO 2010-12-09, Tomasz Pylak: replace this code by choosing the dataset representative
    // Now it works only in HCS case.
    private static IContent getAnyImagePath(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        ImageDatasetParameters params = imageAccessor.getImageParameters();
        for (int row = 1; row <= params.tryGetRowsNum(); row++)
        {
            for (int col = 1; col <= params.tryGetColsNum(); col++)
            {
                for (int tileRow = 1; tileRow <= params.getTileRowsNum(); tileRow++)
                {
                    for (int tileCol = 1; tileCol <= params.getTileColsNum(); tileCol++)
                    {
                        for (String channelCode : params.getChannelsCodes())
                        {
                            ImageChannelStackReference channelStackReference =
                                    ImageChannelStackReference.createHCSFromLocations(new Location(
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
        WellFeatureCollection<FeatureTableRow> datasetFeatures =
                FeatureVectorLoader.fetchDatasetFeatures(Arrays.asList(dataset.getDatasetCode()),
                        featureCodes, getDAO(), createMetadataProvider());
        List<FeatureVector> featureVectors = new ArrayList<FeatureVector>();
        for (FeatureTableRow featureTableRow : datasetFeatures.getFeatures())
        {
            WellLocation wellPosition = featureTableRow.getWellLocation();
            double[] values = getFloatFeaturesAsDouble(featureTableRow);
            featureVectors.add(new FeatureVector(convert(wellPosition), values));
        }
        return new FeatureVectorDataset(dataset, datasetFeatures.getFeatureCodes(),
                datasetFeatures.getFeatureLabels(), featureVectors);
    }

    // TODO 2010-11-29, Tomasz Pylak: allow to access Vocabulary Features in the API
    private static double[] getFloatFeaturesAsDouble(FeatureTableRow featureTableRow)
    {
        FeatureValue[] featureValues = featureTableRow.getFeatureValues();
        double[] doubleValues = new double[featureValues.length];
        for (int i = 0; i < featureValues.length; ++i)
        {
            FeatureValue featureValue = featureValues[i];
            if (featureValue.isFloat())
            {
                doubleValues[i] = featureValue.asFloat();
            } else
            {
                // convert a vocabulary term to NaN
                doubleValues[i] = Double.NaN;
            }
        }
        return doubleValues;
    }

    private static WellPosition convert(WellLocation wellPosition)
    {
        return new WellPosition(wellPosition.getRow(), wellPosition.getColumn());
    }

    private List<String> normalize(List<String> names)
    {
        ArrayList<String> codes = new ArrayList<String>(names.size());
        for (String name : names)
        {
            codes.add(CodeAndLabelUtil.normalize(name));
        }
        return codes;
    }

    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken, List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureNames)
    {
        checkDatasetsAuthorizationForIDatasetIdentifier(sessionToken, datasetWellReferences);
        WellFeatureCollection<FeatureTableRow> features =
                FeatureVectorLoader.fetchWellFeatures(datasetWellReferences, featureNames, dao,
                        createMetadataProvider());
        return createFeatureVectorList(features);
    }

    private IMetadataProvider createMetadataProvider()
    {
        final IEncapsulatedOpenBISService openBISService = getOpenBISService();
        return new IMetadataProvider()
            {
                public SampleIdentifier tryGetSampleIdentifier(String samplePermId)
                {
                    return openBISService.tryToGetSampleIdentifier(samplePermId);
                }
            };
    }

    private List<FeatureVectorWithDescription> createFeatureVectorList(
            final WellFeatureCollection<FeatureTableRow> features)
    {
        final List<String> featureCodes = features.getFeatureCodes();
        final List<FeatureTableRow> featureTableRows = features.getFeatures();
        final List<FeatureVectorWithDescription> result =
                new ArrayList<FeatureVectorWithDescription>(featureTableRows.size());
        for (FeatureTableRow featureTableRow : featureTableRows)
        {
            result.add(createFeatureVector(featureTableRow, featureCodes));
        }
        return result;
    }

    private FeatureVectorWithDescription createFeatureVector(FeatureTableRow featureTableRow,
            final List<String> featureCodes)
    {
        return new FeatureVectorWithDescription(featureTableRow.getReference(), featureCodes,
                getFloatFeaturesAsDouble(featureTableRow));
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            boolean convertToPng)
    {
        return loadImages(sessionToken, imageReferences, null, convertToPng);
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            ImageSize thumbnailSizeOrNull)
    {
        return loadImages(sessionToken, imageReferences, convertToSize(thumbnailSizeOrNull), true);
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            final Size sizeOrNull, final boolean convertToPng)
    {
        checkDatasetsAuthorizationForIDatasetIdentifier(sessionToken, imageReferences);
        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);
        final List<IContent> imageContents = new ArrayList<IContent>();
        for (final PlateImageReference imageReference : imageReferences)
        {
            final IImagingDatasetLoader imageAccessor =
                    imageLoadersMap.get(imageReference.getDatasetCode());
            assert imageAccessor != null : "imageAccessor not found for: " + imageReference;
            imageContents.add(new ContentProviderBasedContent(new IContentProvider()
                {
                    public IContent getContent()
                    {
                        return tryGetImageContent(imageAccessor, imageReference, sizeOrNull,
                                convertToPng);
                    }
                }));
        }
        return new ConcatenatedContentInputStream(true, imageContents);
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences)
    {
        return loadImages(sessionToken, imageReferences, true);
    }

    public InputStream loadImages(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
    {
        String datasetCode = dataSetIdentifier.getDatasetCode();
        File rootDir = getRootDirectoryForDataSet(datasetCode);
        final IImagingDatasetLoader imageAccessor = createImageLoader(datasetCode, rootDir);
        List<PlateImageReference> imageReferences =
                createPlateImageReferences(imageAccessor, dataSetIdentifier, wellPositions, channel);
        final Size size = convertToSize(thumbnailSizeOrNull);
        List<IContent> imageContents = new ArrayList<IContent>();

        for (final PlateImageReference imageReference : imageReferences)
        {
            imageContents.add(new ContentProviderBasedContent(new IContentProvider()
                {
                    public IContent getContent()
                    {
                        return tryGetImageContent(imageAccessor, imageReference, size, true);
                    }
                }));
        }
        return new ConcatenatedContentInputStream(true, imageContents);
    }

    public List<PlateImageReference> listPlateImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions, String channel)
    {
        String datasetCode = dataSetIdentifier.getDatasetCode();
        File rootDir = getRootDirectoryForDataSet(datasetCode);
        IImagingDatasetLoader imageAccessor = createImageLoader(datasetCode, rootDir);
        return createPlateImageReferences(imageAccessor, dataSetIdentifier, wellPositions, channel);
    }

    public void saveImageTransformerFactory(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel,
            IImageTransformerFactory transformerFactory)
    {
        getOpenBISService().checkInstanceAdminAuthorization(sessionToken);
        for (IDatasetIdentifier datasetIdentifier : dataSetIdentifiers)
        {
            ImgDatasetDTO dataset = getImagingDataset(datasetIdentifier);
            if (dataset == null)
            {
                throw new UserFailureException("Unkown data set " + datasetIdentifier);
            }
            Long containerId = dataset.getContainerId();
            if (containerId == null)
            {
                saveImageTransformerFactoryForDataset(dataset.getId(), channel, transformerFactory);
            } else
            {
                ImgContainerDTO container = getDAO().getContainerById(containerId);
                saveImageTransformerFactoryForExperiment(container.getExperimentId(), channel,
                        transformerFactory);
            }
        }
        transformerDAO.commit();
    }

    private static boolean isMergedChannel(String channel)
    {
        return ScreeningConstants.MERGED_CHANNELS.equals(channel);
    }

    private void saveImageTransformerFactoryForExperiment(long experimentId, String channel,
            IImageTransformerFactory transformerFactory)
    {
        if (isMergedChannel(channel))
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("save image transformer factory " + transformerFactory
                        + " for experiment " + experimentId);
            }
            transformerDAO.saveTransformerFactoryForExperiment(experimentId, transformerFactory);
        } else
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("save image transformer factory " + transformerFactory
                        + " for experiment " + experimentId + " and channel '" + channel + "'.");
            }
            transformerDAO.saveTransformerFactoryForExperimentChannel(experimentId, channel,
                    transformerFactory);
        }
    }

    private void saveImageTransformerFactoryForDataset(long datasetId, String channel,
            IImageTransformerFactory transformerFactory)
    {
        if (isMergedChannel(channel))
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("save image transformer factory " + transformerFactory
                        + " for dataset " + datasetId);
            }
            transformerDAO.saveTransformerFactoryForDataset(datasetId, transformerFactory);
        } else
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("save image transformer factory " + transformerFactory
                        + " for dataset " + datasetId + " and channel '" + channel + "'.");
            }
            transformerDAO.saveTransformerFactoryForDatasetChannel(datasetId, channel,
                    transformerFactory);
        }
    }

    public IImageTransformerFactory getImageTransformerFactoryOrNull(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel)
    {
        if (dataSetIdentifiers.size() == 1 && isMicroscopyDataset(dataSetIdentifiers.get(0)))
        {
            return tryGetImageTransformerFactoryForDataset(dataSetIdentifiers.get(0), channel);
        }
        Set<String> experimentPermIDs = getExperimentPermIDs(sessionToken, dataSetIdentifiers);
        if (experimentPermIDs.isEmpty())
        {
            throw new UserFailureException("No data set identifers specified.");
        }
        if (experimentPermIDs.size() > 1)
        {
            throw new UserFailureException("All data sets have to belong to the same experiment: "
                    + dataSetIdentifiers);
        }
        String experimentPermID = experimentPermIDs.iterator().next();
        if (isMergedChannel(channel))
        {
            return getDAO().tryGetExperimentByPermId(experimentPermID).getImageTransformerFactory();
        } else
        {
            return getDAO().tryGetChannelForExperimentPermId(experimentPermID, channel)
                    .getImageTransformerFactory();
        }
    }

    private Set<String> getExperimentPermIDs(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers)
    {
        Set<String> experimentPermIDs = new HashSet<String>();
        for (IDatasetIdentifier dataSetIdentifier : dataSetIdentifiers)
        {
            ExternalData dataSet =
                    getOpenBISService().tryGetDataSet(sessionToken,
                            dataSetIdentifier.getDatasetCode());
            if (dataSet == null)
            {
                throw new UserFailureException("Unkown data set " + dataSetIdentifier);
            }
            experimentPermIDs.add(dataSet.getExperiment().getPermId());
        }
        return experimentPermIDs;
    }

    private IImageTransformerFactory tryGetImageTransformerFactoryForDataset(
            IDatasetIdentifier datasetIdentifier, String channel)
    {
        ImgDatasetDTO dataset = getImagingDataset(datasetIdentifier);
        if (isMergedChannel(channel))
        {
            return dataset.getImageTransformerFactory();
        } else
        {
            return getDAO().tryGetChannelForDataset(dataset.getId(), channel)
                    .getImageTransformerFactory();
        }
    }

    private boolean isMicroscopyDataset(IDatasetIdentifier datasetIdentifier)
    {
        return getImagingDataset(datasetIdentifier).getContainerId() == null;
    }

    private ImgDatasetDTO getImagingDataset(IDatasetIdentifier datasetIdentifier)
    {
        ImgDatasetDTO dataset = getDAO().tryGetDatasetByPermId(datasetIdentifier.getDatasetCode());
        if (dataset == null)
        {
            throw new UserFailureException("Unknown data set: "
                    + datasetIdentifier.getDatasetCode());
        }
        return dataset;
    }

    private List<PlateImageReference> createPlateImageReferences(
            IImagingDatasetLoader imageAccessor, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel)
    {
        ImageDatasetParameters imageParameters = imageAccessor.getImageParameters();
        int rowsNum = imageParameters.tryGetRowsNum();
        int colsNum = imageParameters.tryGetColsNum();
        List<PlateImageReference> imageReferences = new ArrayList<PlateImageReference>();
        int numberOfTiles = imageParameters.getTileRowsNum() * imageParameters.getTileColsNum();
        if (wellPositions == null || wellPositions.isEmpty())
        {
            // all wells
            for (int i = 1; i <= rowsNum; i++)
            {
                for (int j = 1; j <= colsNum; j++)
                {
                    addImageReferencesForAllTiles(imageReferences, i, j, channel,
                            dataSetIdentifier, numberOfTiles);
                }
            }
        } else
        {
            for (WellPosition wellPosition : wellPositions)
            {
                addImageReferencesForAllTiles(imageReferences, wellPosition.getWellRow(),
                        wellPosition.getWellColumn(), channel, dataSetIdentifier, numberOfTiles);
            }
        }
        return imageReferences;
    }

    private void addImageReferencesForAllTiles(List<PlateImageReference> imageReferences,
            int wellRow, int wellColumn, String channel, IDatasetIdentifier dataset,
            int numberOfTiles)
    {
        for (int i = 0; i < numberOfTiles; i++)
        {
            imageReferences.add(new PlateImageReference(wellRow, wellColumn, i, channel, dataset));
        }
    }

    // throws exception if some datasets cannot be found
    private Map<String/* image or feature vector dataset code */, IImagingDatasetLoader> getImageDatasetsMap(
            String sessionToken, List<PlateImageReference> imageReferences)
    {
        Map<String/* dataset code */, IImagingDatasetLoader> imageDatasetsMap =
                new HashMap<String, IImagingDatasetLoader>();
        for (PlateImageReference imageReference : imageReferences)
        {
            if (imageDatasetsMap.containsKey(imageReference.getDatasetCode()) == false)
            {
                ExternalData imageDataset =
                        tryFindImageDataset(sessionToken, imageReference.getDatasetCode());
                if (imageDataset != null)
                {
                    IImagingDatasetLoader imageAccessor = createImageLoader(imageDataset.getCode());
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

    private IContent tryGetImageContent(IImagingDatasetLoader imageAccessor,
            PlateImageReference imageRef, Size thumbnailSizeOrNull, boolean convertToPng)
    {
        Location wellLocation = asLocation(imageRef.getWellPosition());
        Location tileLocation =
                getTileLocation(imageRef.getTile(), imageAccessor.getImageParameters()
                        .getTileColsNum());
        try
        {
            ImageChannelStackReference channelStackReference =
                    ImageChannelStackReference.createHCSFromLocations(wellLocation, tileLocation);
            return ImageChannelsUtils.getImage(imageAccessor, channelStackReference,
                    imageRef.getChannel(), thumbnailSizeOrNull, convertToPng);
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
        return new Location(col, row);
    }

    private static Location asLocation(WellPosition wellPosition)
    {
        return new Location(wellPosition.getWellColumn(), wellPosition.getWellRow());
    }

    private Size convertToSize(ImageSize thumbnailSizeOrNull)
    {
        if (thumbnailSizeOrNull == null)
        {
            return null;
        }
        return new Size(thumbnailSizeOrNull.getWidth(), thumbnailSizeOrNull.getHeight());
    }

    private IImagingDatasetLoader createImageLoader(String datasetCode)
    {
        File datasetRoot = getRootDirectoryForDataSet(datasetCode);
        return createImageLoader(datasetCode, datasetRoot);
    }

    IImagingDatasetLoader createImageLoader(String datasetCode, File datasetRoot)
    {
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
        return dataset.getDataSetType().getCode().equals(ScreeningConstants.HCS_IMAGE_DATASET_TYPE);
    }

    private List<ImgFeatureDefDTO> getFeatureDefinitions(IDatasetIdentifier identifier)
    {
        ImgDatasetDTO dataSet = getImagingDataset(identifier);
        return getDAO().listFeatureDefsByDataSetId(dataSet.getId());
    }

    public void checkDatasetsAuthorizationForIDatasetIdentifier(String sessionToken,
            List<? extends IDatasetIdentifier> identifiers)
    {
        Set<String> dataSetCodes = new LinkedHashSet<String>();
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
