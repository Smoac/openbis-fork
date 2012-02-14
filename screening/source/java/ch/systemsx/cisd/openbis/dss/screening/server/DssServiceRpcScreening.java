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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.api.server.RpcServiceNameServer;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.common.io.HierarchicalContentNodeBasedHierarchicalContentNode;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.etl.IImagingLoaderStrategy;
import ch.systemsx.cisd.openbis.dss.etl.ImagingLoaderStrategyFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.IStreamRepository;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.screening.server.logic.ImageRepresentationFormatFinder;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.LoadImageConfiguration;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.AbstractFormatSelectionCriterion;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageRepresentationFormatSelectionCriterion;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MicroscopyImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.FeatureTableRow;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.IMetadataProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.AbstractImgIdentifiable;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingTransformerDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAnalysisDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageTransformationDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;

/**
 * Implementation of the screening API interface using RPC. The instance will be created in spring
 * and published as a HTTP invoker servlet configured in service.properties.
 * 
 * @author Tomasz Pylak
 */
public class DssServiceRpcScreening extends AbstractDssServiceRpc<IDssServiceRpcScreening>
        implements IDssServiceRpcScreening
{

    /**
     * The minor version of this service.
     */
    public static final int MINOR_VERSION = 10;

    // this dao will hold one connection to the database
    private IImagingReadonlyQueryDAO dao;

    private final IImagingTransformerDAO transformerDAO;

    /**
     * The designated constructor.
     */
    public DssServiceRpcScreening(String storeRootDir)
    {
        this(storeRootDir, null, DssScreeningUtils.createImagingTransformerDAO(), ServiceProvider
                .getOpenBISService(), null, null, null, true);
    }

    DssServiceRpcScreening(String storeRootDir, IImagingReadonlyQueryDAO dao,
            IImagingTransformerDAO transformerDAO, IEncapsulatedOpenBISService service,
            IStreamRepository streamRepository, IShareIdManager shareIdManager,
            IHierarchicalContentProvider contentProvider, boolean registerAtNameService)
    {
        super(service, streamRepository, shareIdManager, contentProvider);
        this.dao = dao;
        this.transformerDAO = transformerDAO;
        setStoreDirectory(new File(storeRootDir));
        if (registerAtNameService)
        {
            // Register the service with the name server
            RpcServiceInterfaceVersionDTO ifaceVersion =
                    new RpcServiceInterfaceVersionDTO(IDssServiceRpcScreening.SERVICE_NAME,
                            IDssServiceRpcScreening.SERVICE_URL, getMajorVersion(),
                            getMinorVersion());
            RpcServiceInterfaceVersionDTO jsonVersion =
                    new RpcServiceInterfaceVersionDTO(IDssServiceRpcScreening.SERVICE_NAME,
                            IDssServiceRpcScreening.JSON_SERVICE_URL, getMajorVersion(),
                            getMinorVersion());

            HttpInvokerServiceExporter nameServiceExporter =
                    ServiceProvider.getRpcNameServiceExporter();
            RpcServiceNameServer nameServer =
                    (RpcServiceNameServer) nameServiceExporter.getService();
            nameServer.addSupportedInterfaceVersion(ifaceVersion);
            nameServer.addSupportedInterfaceVersion(jsonVersion);

            operationLog.info("[rpc] Started DSS RPC screening service V1.");
        }
    }

    // ------------------ impl -----------------

    public List<String> listAvailableFeatureNames(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return listAvailableFeatureCodes(sessionToken, featureDatasets);
    }

    public IDssServiceRpcScreening createLogger(IInvocationLoggerContext context)
    {
        return new DssServiceRpcScreeningLogger(context);
    }

    public List<String> listAvailableFeatureCodes(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        List<ImgFeatureDefDTO> featureDefinitions = getFeatureDefinitions(featureDatasets);

        // add only new feature names
        List<String> result = new ArrayList<String>(); // keep the order
        for (ImgFeatureDefDTO featureDefinition : featureDefinitions)
        {
            String featureCode = featureDefinition.getCode();
            if (result.contains(featureCode) == false)
            {
                result.add(featureCode);
            }
        }
        return result;
    }

    public List<FeatureInformation> listAvailableFeatures(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        List<ImgFeatureDefDTO> featureDefinitions = getFeatureDefinitions(featureDatasets);

        // add only new feature names
        List<FeatureInformation> result = new ArrayList<FeatureInformation>(); // keep the order
        for (ImgFeatureDefDTO featureDefinition : featureDefinitions)
        {
            FeatureInformation description =
                    new FeatureInformation(featureDefinition.getCode(),
                            featureDefinition.getLabel(), featureDefinition.getDescription());
            if (result.contains(description) == false)
            {
                result.add(description);
            }
        }
        return result;
    }

    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        IShareIdManager shareIdManager = getShareIdManager();
        for (IImageDatasetIdentifier dataset : imageDatasets)
        {
            String datasetCode = dataset.getDatasetCode();
            shareIdManager.lock(datasetCode);
        }
        try
        {
            List<ImageDatasetMetadata> result = new ArrayList<ImageDatasetMetadata>();
            for (IImageDatasetIdentifier dataset : imageDatasets)
            {
                final IHierarchicalContent content =
                        getHierarchicalContent(dataset.getDatasetCode());
                try
                {
                    result.add(extractImageMetadata(dataset, content));
                } finally
                {
                    content.close();
                }
            }
            return result;
        } finally
        {
            shareIdManager.releaseLocks();
        }
    }

    private ImageDatasetMetadata extractImageMetadata(IImageDatasetIdentifier dataset,
            IHierarchicalContent content)
    {
        final IImagingDatasetLoader imageAccessor =
                createImageLoader(dataset.getDatasetCode(), content);
        final Size imageSize = getOriginalImageSize(dataset, imageAccessor);
        final Size thumbnailSize = getThumbnailImageSize(dataset, imageAccessor);
        final ImageDatasetParameters params = imageAccessor.getImageParameters();
        return new ImageDatasetMetadata(dataset, toPublicChannels(params.getInternalChannels()),
                params.getTileRowsNum(), params.getTileColsNum(), imageSize.getWidth(),
                imageSize.getHeight(), thumbnailSize.getWidth(), thumbnailSize.getHeight());
    }

    private List<ImageChannel> toPublicChannels(List<InternalImageChannel> internalChannels)
    {
        final List<ImageChannel> publicChannels =
                new ArrayList<ImageChannel>(internalChannels.size());
        for (InternalImageChannel channel : internalChannels)
        {
            publicChannels.add(new ImageChannel(channel.getCode(), channel.getLabel(), channel
                    .tryGetDescription(), channel.tryGetWavelength(),
                    toPublicImageTransformationInfos(channel.getAvailableImageTransformations())));
        }
        return publicChannels;

    }

    private List<ImageTransformationInfo> toPublicImageTransformationInfos(
            List<InternalImageTransformationInfo> internalTrafos)
    {
        final List<ImageTransformationInfo> publicTrafos =
                new ArrayList<ImageTransformationInfo>(internalTrafos.size());
        for (InternalImageTransformationInfo info : internalTrafos)
        {
            publicTrafos.add(new ImageTransformationInfo(info.getCode(), info.getLabel(), info
                    .getDescription(), info.isDefault()));
        }
        return publicTrafos;
    }

    private static Size getOriginalImageSize(IImageDatasetIdentifier dataset,
            IImagingDatasetLoader imageAccessor)
    {
        BufferedImage image = getAnyImage(imageAccessor, dataset);
        Size imageSize = new Size(image.getWidth(), image.getHeight());
        return imageSize;
    }

    private static Size getThumbnailImageSize(IImageDatasetIdentifier dataset,
            IImagingDatasetLoader imageAccessor)
    {
        BufferedImage image = getAnyThumbnailImage(imageAccessor, dataset);
        if (image != null)
        {
            return new Size(image.getWidth(), image.getHeight());
        } else
        {
            return Size.NULL_SIZE;
        }
    }

    private static BufferedImage getAnyImage(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        if (imageAccessor.getImageParameters().tryGetRowsNum() == null)
        {
            return getAnyMicroscopyImage(imageAccessor, dataset);
        } else
        {
            return getAnyHCSImage(imageAccessor, dataset);
        }
    }

    private static BufferedImage getAnyThumbnailImage(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        if (imageAccessor.getImageParameters().tryGetRowsNum() == null)
        {
            return getAnyMicroscopyThumbnail(imageAccessor, dataset);
        } else
        {
            return getAnyHCSThumbnail(imageAccessor, dataset);
        }
    }

    private static BufferedImage getAnyMicroscopyImage(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        ImageDatasetParameters params = imageAccessor.getImageParameters();
        RequestedImageSize originalOrThumbnail = RequestedImageSize.createOriginal();
        for (String channelCode : params.getChannelsCodes())
        {
            AbsoluteImageReference image =
                    imageAccessor.tryGetRepresentativeImage(channelCode, null, originalOrThumbnail);
            if (image != null)
            {
                return image.getUnchangedImage();
            }
        }
        throw new IllegalStateException("Cannot find any image in a dataset: " + dataset);
    }

    private static BufferedImage getAnyMicroscopyThumbnail(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        ImageDatasetParameters params = imageAccessor.getImageParameters();
        for (String channelCode : params.getChannelsCodes())
        {
            AbsoluteImageReference image =
                    imageAccessor.tryGetRepresentativeThumbnail(channelCode, null, null);
            if (image != null)
            {
                return image.getUnchangedImage();
            }
        }
        return null;
    }

    private static BufferedImage getAnyHCSThumbnail(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        AbsoluteImageReference image = imageAccessor.tryFindAnyThumbnail();
        if (image != null)
        {
            return image.getUnchangedImage();
        }

        return null;
    }

    private static BufferedImage getAnyHCSImage(IImagingDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        AbsoluteImageReference image = imageAccessor.tryFindAnyOriginalImage();

        if (image != null)
        {
            return image.getUnchangedImage();
        }

        throw new IllegalStateException("Cannot find any image in a dataset: " + dataset);
    }

    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureNames)
    {
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
            codes.add(CodeNormalizer.normalize(name));
        }
        return codes;
    }

    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken, List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureNames)
    {
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
        return loadImages(sessionToken, imageReferences, null, null, convertToPng);
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            ImageSize thumbnailSizeOrNull)
    {
        return loadImages(sessionToken, imageReferences, tryAsSize(thumbnailSizeOrNull), null, true);
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            LoadImageConfiguration configuration)
    {
        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);
        return loadImages(imageReferences, tryAsSize(configuration.getDesiredImageSize()),
                configuration.getSingleChannelImageTransformationCode(),
                configuration.isDesiredImageFormatPng(),
                configuration.isOpenBisImageTransformationApplied(), imageLoadersMap);
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            final ImageRepresentationFormat format)
    {
        for (PlateImageReference plateImageReference : imageReferences)
        {
            if (plateImageReference.getDatasetCode().equals(format.getDataSetCode()) == false)
            {
                throw new UserFailureException(
                        "At least for one plate image reference the image representation format "
                                + "is unknown: Plate image reference: " + plateImageReference
                                + ", format: " + format);
            }
        }
        IImageRepresentationFormatSelectionCriterion criterion =
                new AbstractFormatSelectionCriterion()
                    {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected boolean accept(ImageRepresentationFormat availableFormat)
                        {
                            return format.getId() == availableFormat.getId();
                        }
                    };
        return loadImages(sessionToken, imageReferences, criterion);
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria)
    {
        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);
        Map<String, ImgImageDatasetDTO> imageDataSetMap =
                createDataSetCodeToImageDataSetMap(imageReferences);
        ImageRepresentationFormatFinder finder = new ImageRepresentationFormatFinder(criteria);
        Map<String, ImageRepresentationFormat> dataSetToImageReferenceFormatMap =
                new HashMap<String, ImageRepresentationFormat>();
        for (Entry<String, ImgImageDatasetDTO> entry : imageDataSetMap.entrySet())
        {
            String dataSetCode = entry.getKey();
            List<ImageRepresentationFormat> filteredFormats =
                    finder.find(getImageRepresentationFormats(entry.getValue()));
            if (filteredFormats.isEmpty())
            {
                throw new UserFailureException(
                        "No image representation format fitting criteria found for data set "
                                + dataSetCode + ".");
            }
            if (filteredFormats.size() > 1)
            {
                throw new UserFailureException(
                        "To many image representation formats fitting criteria for data set "
                                + dataSetCode + ": " + filteredFormats);
            }
            dataSetToImageReferenceFormatMap.put(dataSetCode, filteredFormats.get(0));
        }
        List<IHierarchicalContentNode> imageContents = new ArrayList<IHierarchicalContentNode>();
        for (PlateImageReference imageReference : imageReferences)
        {
            String datasetCode = imageReference.getDatasetCode();
            IImagingDatasetLoader loader = imageLoadersMap.get(datasetCode);
            ImageRepresentationFormat format = dataSetToImageReferenceFormatMap.get(datasetCode);
            Size size = new Size(format.getWidth(), format.getHeight());
            addImageContentTo(imageContents, loader, imageReference, size, null, false, false);
        }
        return new ConcatenatedContentInputStream(true, imageContents);
    }

    private InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            final Size sizeOrNull, String singleChannelImageTransformationCodeOrNull,
            final boolean convertToPng)
    {
        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);
        return loadImages(imageReferences, sizeOrNull, singleChannelImageTransformationCodeOrNull,
                convertToPng, false, imageLoadersMap);
    }

    private InputStream loadImages(List<PlateImageReference> imageReferences,
            final Size sizeOrNull, final String singleChannelImageTransformationCodeOrNull,
            final boolean convertToPng, final boolean transform,
            final Map<String, IImagingDatasetLoader> imageLoadersMap)
    {
        final List<IHierarchicalContentNode> imageContents =
                new ArrayList<IHierarchicalContentNode>();
        for (final PlateImageReference imageReference : imageReferences)
        {
            final IImagingDatasetLoader imageAccessor =
                    imageLoadersMap.get(imageReference.getDatasetCode());
            assert imageAccessor != null : "imageAccessor not found for: " + imageReference;

            addImageContentTo(imageContents, imageAccessor, imageReference, sizeOrNull,
                    singleChannelImageTransformationCodeOrNull, convertToPng, transform);
        }

        return new ConcatenatedContentInputStream(true, imageContents);
    }

    private void addImageContentTo(final List<IHierarchicalContentNode> imageContents,
            final IImagingDatasetLoader imageAccessor, final PlateImageReference imageReference,
            final Size sizeOrNull, final String singleChannelImageTransformationCodeOrNull,
            final boolean convertToPng, final boolean transform)
    {
        final IImagingLoaderStrategy imageLoaderStrategy =
                ImagingLoaderStrategyFactory.createImageLoaderStrategy(imageAccessor);

        final ImageChannelStackReference channelStackRef =
                getImageChannelStackReference(imageAccessor, imageReference);
        final String channelCode = imageReference.getChannel();

        imageContents.add(new HierarchicalContentNodeBasedHierarchicalContentNode(
                tryGetImageContent(imageLoaderStrategy, channelStackRef, channelCode, sizeOrNull,
                        singleChannelImageTransformationCodeOrNull, convertToPng, transform)));
    }

    private InputStream loadThumbnailImages(List<PlateImageReference> imageReferences,
            final Map<String, IImagingDatasetLoader> imageLoadersMap)
    {
        final List<IHierarchicalContentNode> imageContents =
                new ArrayList<IHierarchicalContentNode>();
        for (final PlateImageReference imageReference : imageReferences)
        {
            final IImagingDatasetLoader imageAccessor =
                    imageLoadersMap.get(imageReference.getDatasetCode());
            assert imageAccessor != null : "imageAccessor not found for: " + imageReference;

            final IImagingLoaderStrategy imageLoaderStrategy =
                    ImagingLoaderStrategyFactory.createThumbnailLoaderStrategy(imageAccessor);

            final ImageChannelStackReference channelStackRef =
                    getImageChannelStackReference(imageAccessor, imageReference);
            final String channelCode = imageReference.getChannel();

            imageContents.add(new HierarchicalContentNodeBasedHierarchicalContentNode(
                    tryGetImageContent(imageLoaderStrategy, channelStackRef, channelCode, null,
                            null, false, false)));
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
        final IImagingDatasetLoader imageAccessor = createImageLoader(dataSetIdentifier);

        final List<PlateImageReference> imageReferences =
                createPlateImageReferences(imageAccessor, dataSetIdentifier, wellPositions, channel);
        final Size size = tryAsSize(thumbnailSizeOrNull);

        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                new HashMap<String, IImagingDatasetLoader>();
        imageLoadersMap.put(dataSetIdentifier.getDatasetCode(), imageAccessor);

        return loadImages(imageReferences, size, null, true, false, imageLoadersMap);
    }

    public InputStream loadThumbnailImages(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        final Map<String, IImagingDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);

        return loadThumbnailImages(imageReferences, imageLoadersMap);
    }

    public InputStream loadImages(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            String channel, ImageSize thumbnailSizeOrNull)
    {
        final IImagingDatasetLoader imageAccessor = createImageLoader(dataSetIdentifier);
        List<MicroscopyImageReference> imageReferences =
                listImageReferences(dataSetIdentifier, channel, imageAccessor);
        final Size sizeOrNull = tryAsSize(thumbnailSizeOrNull);

        final List<IHierarchicalContentNode> imageContents =
                new ArrayList<IHierarchicalContentNode>();
        for (final MicroscopyImageReference imageReference : imageReferences)
        {
            final ImageChannelStackReference channelStackRef =
                    getImageChannelStackReference(imageAccessor, imageReference);
            final String channelCode = imageReference.getChannel();

            imageContents.add(new HierarchicalContentNodeBasedHierarchicalContentNode(
                    tryGetImageContent(
                            ImagingLoaderStrategyFactory.createImageLoaderStrategy(imageAccessor),
                            channelStackRef, channelCode, sizeOrNull, null, true, false)));
        }
        return new ConcatenatedContentInputStream(true, imageContents);
    }

    public InputStream loadThumbnailImages(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        final IImagingDatasetLoader imageAccessor = createImageLoader(dataSetIdentifier);
        assert imageAccessor != null : "imageAccessor not found for: " + dataSetIdentifier;
        final List<MicroscopyImageReference> imageReferences =
                listImageReferences(dataSetIdentifier, channels, imageAccessor);
        final List<IHierarchicalContentNode> imageContents =
                new ArrayList<IHierarchicalContentNode>();
        for (final MicroscopyImageReference imageReference : imageReferences)
        {
            final IImagingLoaderStrategy imageLoaderStrategy =
                    ImagingLoaderStrategyFactory.createThumbnailLoaderStrategy(imageAccessor);

            final ImageChannelStackReference channelStackRef =
                    getImageChannelStackReference(imageAccessor, imageReference);
            final String channelCode = imageReference.getChannel();

            imageContents.add(new HierarchicalContentNodeBasedHierarchicalContentNode(
                    tryGetImageContent(imageLoaderStrategy, channelStackRef, channelCode, null,
                            null, false, false)));
        }
        return new ConcatenatedContentInputStream(true, imageContents);
    }

    public List<MicroscopyImageReference> listImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, String channel)
    {
        IImagingDatasetLoader imageAccessor = createImageLoader(dataSetIdentifier);
        return listImageReferences(dataSetIdentifier, channel, imageAccessor);
    }

    public List<MicroscopyImageReference> listImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        IImagingDatasetLoader imageAccessor = createImageLoader(dataSetIdentifier);
        return listImageReferences(dataSetIdentifier, channels, imageAccessor);
    }

    private List<MicroscopyImageReference> listImageReferences(
            IDatasetIdentifier dataSetIdentifier, String channel,
            IImagingDatasetLoader imageAccessor)
    {
        int numberOfTiles = getNumberOfTiles(imageAccessor);
        List<MicroscopyImageReference> imageReferences = new ArrayList<MicroscopyImageReference>();
        for (int i = 0; i < numberOfTiles; i++)
        {
            imageReferences.add(new MicroscopyImageReference(i, channel, dataSetIdentifier));
        }
        return imageReferences;
    }

    private List<MicroscopyImageReference> listImageReferences(
            IDatasetIdentifier dataSetIdentifier, List<String> channels,
            IImagingDatasetLoader imageAccessor)
    {
        final int numberOfTiles = getNumberOfTiles(imageAccessor);
        final List<MicroscopyImageReference> imageReferences =
                new ArrayList<MicroscopyImageReference>(numberOfTiles * channels.size());
        for (int i = 0; i < numberOfTiles; i++)
        {
            for (String channel : channels)
            {
                imageReferences.add(new MicroscopyImageReference(i, channel, dataSetIdentifier));
            }
        }
        return imageReferences;
    }

    public List<PlateImageReference> listPlateImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions, String channel)
    {
        IImagingDatasetLoader imageAccessor = createImageLoader(dataSetIdentifier);
        return createPlateImageReferences(imageAccessor, dataSetIdentifier, wellPositions, channel);
    }

    public List<PlateImageReference> listPlateImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions,
            List<String> channels)
    {
        IImagingDatasetLoader imageAccessor = createImageLoader(dataSetIdentifier);
        return createPlateImageReferences(imageAccessor, dataSetIdentifier, wellPositions, channels);
    }

    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            String sessionToken, List<? extends IDatasetIdentifier> imageDatasets)
    {
        ArrayList<DatasetImageRepresentationFormats> result =
                new ArrayList<DatasetImageRepresentationFormats>(imageDatasets.size());

        Map<String, ImgImageDatasetDTO> imageDataSetMap =
                createDataSetCodeToImageDataSetMap(imageDatasets);

        // Not an efficient way to execute this query, but it reuses the queries at our disposal
        for (IDatasetIdentifier imageDataset : imageDatasets)
        {
            String dataSetCode = imageDataset.getPermId();
            ImgImageDatasetDTO primImageDataSet = imageDataSetMap.get(dataSetCode);
            if (null == primImageDataSet)
            {
                List<ImageRepresentationFormat> emptyList = Collections.emptyList();
                DatasetImageRepresentationFormats datasetResult =
                        new DatasetImageRepresentationFormats(imageDataset, emptyList);
                result.add(datasetResult);
            } else
            {
                List<ImageRepresentationFormat> formats =
                        getImageRepresentationFormats(primImageDataSet);
                DatasetImageRepresentationFormats datasetResult =
                        new DatasetImageRepresentationFormats(imageDataset, formats);
                result.add(datasetResult);
            }
        }
        return result;
    }

    private List<ImageRepresentationFormat> getImageRepresentationFormats(
            ImgImageDatasetDTO imageDataSet)
    {
        List<ImgImageZoomLevelDTO> zoomLevels = getDAO().listImageZoomLevels(imageDataSet.getId());
        return convertZoomLevelsToRepresentationFormats(imageDataSet.getPermId(), zoomLevels);
    }

    private Map<String, ImgImageDatasetDTO> createDataSetCodeToImageDataSetMap(
            List<? extends IDatasetIdentifier> imageDatasets)
    {
        Set<String> permIds = new HashSet<String>();
        for (IDatasetIdentifier identifier : imageDatasets)
        {
            permIds.add(identifier.getPermId());
        }
        List<ImgImageDatasetDTO> primImageDatasets =
                getDAO().listImageDatasetsByPermId(permIds.toArray(new String[permIds.size()]));

        // Convert this to a hash map for faster indexing
        HashMap<String, ImgImageDatasetDTO> imageDataSetMap =
                new HashMap<String, ImgImageDatasetDTO>();
        for (ImgImageDatasetDTO primImageDataSet : primImageDatasets)
        {
            imageDataSetMap.put(primImageDataSet.getPermId(), primImageDataSet);
        }
        return imageDataSetMap;
    }

    private List<ImageRepresentationFormat> convertZoomLevelsToRepresentationFormats(
            String dataSetCode, List<ImgImageZoomLevelDTO> zoomLevels)
    {
        ArrayList<ImageRepresentationFormat> results = new ArrayList<ImageRepresentationFormat>();
        for (ImgImageZoomLevelDTO zoomLevel : zoomLevels)
        {
            ImageRepresentationFormat result =
                    new ImageRepresentationFormat(dataSetCode, zoomLevel.getId(),
                            zoomLevel.getIsOriginal(), zoomLevel.getWidth(), zoomLevel.getHeight(),
                            zoomLevel.getColorDepth(), zoomLevel.getFileType());
            results.add(result);
        }

        return results;
    }

    private IImagingDatasetLoader createImageLoader(IDatasetIdentifier dataSetIdentifier)
    {
        final String datasetCode = dataSetIdentifier.getDatasetCode();
        return createImageLoader(datasetCode);
    }

    public void saveImageTransformerFactory(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel,
            IImageTransformerFactory transformerFactory)
    {
        for (IDatasetIdentifier datasetIdentifier : dataSetIdentifiers)
        {
            ImgImageDatasetDTO dataset = getImagingImageDataset(datasetIdentifier);
            if (dataset == null)
            {
                throw new UserFailureException("Unkown data set " + datasetIdentifier);
            }
            if (dao.hasDatasetChannels(dataset.getPermId()))
            {
                saveImageTransformerFactoryForDataset(dataset.getId(), channel, transformerFactory);
            } else
            {
                Long containerId = dataset.getContainerId();
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

    static final String IMAGE_VIEWER_TRANSFORMATION_CODE = "_CUSTOM";

    private static final String IMAGE_VIEWER_TRANSFORMATION_LABEL = "Custom";

    private static final String IMAGE_VIEWER_TRANSFORMATION_DESCRIPTION =
            "Custom image transformation defined with the Color Adjustment tool.";

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
            Long channelId = transformerDAO.getExperimentChannelId(experimentId, channel);
            createOrUpdateImageViewerTransformation(channelId, transformerFactory);
        }
    }

    private void createOrUpdateImageViewerTransformation(long channelId,
            IImageTransformerFactory transformerFactory)
    {
        Long transformationIdOrNull =
                transformerDAO.tryGetImageTransformationId(channelId,
                        IMAGE_VIEWER_TRANSFORMATION_CODE);
        if (transformationIdOrNull == null)
        {
            transformerDAO.addImageTransformation(createImageViewerTransformation(channelId,
                    transformerFactory));
        } else
        {
            if (transformerFactory != null)
            {
                transformerDAO.updateImageTransformerFactory(transformationIdOrNull,
                        transformerFactory);
            } else
            {
                transformerDAO.removeImageTransformation(transformationIdOrNull);
            }
        }
    }

    private ImgImageTransformationDTO createImageViewerTransformation(long channelId,
            IImageTransformerFactory transformerFactory)
    {
        return new ImgImageTransformationDTO(IMAGE_VIEWER_TRANSFORMATION_CODE,
                IMAGE_VIEWER_TRANSFORMATION_LABEL, IMAGE_VIEWER_TRANSFORMATION_DESCRIPTION, false,
                channelId, transformerFactory, true);
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
            transformerDAO.saveTransformerFactoryForImageDataset(datasetId, transformerFactory);
        } else
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("save image transformer factory " + transformerFactory
                        + " for dataset " + datasetId + " and channel '" + channel + "'.");
            }
            long channelId = transformerDAO.getDatasetChannelId(datasetId, channel);
            createOrUpdateImageViewerTransformation(channelId, transformerFactory);
        }
    }

    public IImageTransformerFactory getImageTransformerFactoryOrNull(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel)
    {
        if (dataSetIdentifiers.size() == 1)
        {
            IDatasetIdentifier datasetIdentifier = dataSetIdentifiers.get(0);
            if (getDAO().hasDatasetChannels(datasetIdentifier.getPermId()))
            {
                return tryGetImageTransformerFactoryForDataset(datasetIdentifier, channel);
            }
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
        return tryGetImageTransformerFactoryForExperiment(experimentPermID, channel);
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
        ImgImageDatasetDTO dataset = getImagingImageDataset(datasetIdentifier);
        if (isMergedChannel(channel))
        {
            return dataset.tryGetImageTransformerFactory();
        } else
        {
            long channelId = getDAO().getDatasetChannelId(dataset.getId(), channel);
            return tryGetImageViewerTransformation(channelId);
        }
    }

    private IImageTransformerFactory tryGetImageViewerTransformation(long channelId)
    {
        ImgImageTransformationDTO transformation =
                getDAO().tryGetImageTransformation(channelId, IMAGE_VIEWER_TRANSFORMATION_CODE);
        if (transformation == null)
        {
            return null;
        }
        return transformation.getImageTransformerFactory();
    }

    private IImageTransformerFactory tryGetImageTransformerFactoryForExperiment(
            String experimentPermID, String channel)
    {
        if (isMergedChannel(channel))
        {
            return getDAO().tryGetExperimentByPermId(experimentPermID)
                    .tryGetImageTransformerFactory();
        } else
        {
            ImgChannelDTO channelDTO =
                    getDAO().tryGetChannelForExperimentPermId(experimentPermID, channel);
            assert channelDTO != null : String.format("No channel '%s' for experiment '%s'.",
                    channel, experimentPermID);
            return tryGetImageViewerTransformation(channelDTO.getId());
        }
    }

    private List<ImgAnalysisDatasetDTO> getAnalysisDatasets(
            List<? extends IDatasetIdentifier> datasetIdents)
    {
        String[] permIds = extractPermIds(datasetIdents);
        List<ImgAnalysisDatasetDTO> datasets = getDAO().listAnalysisDatasetsByPermId(permIds);
        if (datasets.size() != datasetIdents.size())
        {
            Set<String> missing = new HashSet<String>(Arrays.asList(permIds));
            for (ImgAnalysisDatasetDTO dataset : datasets)
            {
                missing.remove(dataset.getPermId());
            }
            throw new UserFailureException("Following datasets could not be found: " + missing);
        }
        return datasets;
    }

    private static <T extends AbstractImgIdentifiable> long[] extractIds(List<T> dataSets)
    {
        long[] ids = new long[dataSets.size()];
        for (int i = 0; i < ids.length; i++)
        {
            ids[i] = dataSets.get(i).getId();
        }
        return ids;
    }

    private static String[] extractPermIds(List<? extends IDatasetIdentifier> datasets)
    {
        String[] permIds = new String[datasets.size()];
        for (int i = 0; i < permIds.length; i++)
        {
            permIds[i] = datasets.get(i).getDatasetCode();
        }
        return permIds;
    }

    private ImgImageDatasetDTO getImagingImageDataset(IDatasetIdentifier datasetIdentifier)
    {
        ImgImageDatasetDTO dataset =
                getDAO().tryGetImageDatasetByPermId(datasetIdentifier.getDatasetCode());
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
        int numberOfTiles = getNumberOfTiles(imageAccessor);
        List<PlateImageReference> imageReferences = new ArrayList<PlateImageReference>();
        if (wellPositions == null || wellPositions.isEmpty())
        {
            for (int i = 0; i < numberOfTiles; i++)
            {
                imageReferences.add(new PlateImageReference(i, channel, null, dataSetIdentifier));
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

    private List<PlateImageReference> createPlateImageReferences(
            IImagingDatasetLoader imageAccessor, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, List<String> channels)
    {
        int numberOfTiles = getNumberOfTiles(imageAccessor);
        List<PlateImageReference> imageReferences = new ArrayList<PlateImageReference>();
        if (wellPositions == null || wellPositions.isEmpty())
        {
            for (String channel : channels)
            {
                for (int i = 0; i < numberOfTiles; i++)
                {
                    imageReferences
                            .add(new PlateImageReference(i, channel, null, dataSetIdentifier));
                }
            }
        } else
        {
            for (WellPosition wellPosition : wellPositions)
            {
                for (String channel : channels)
                {
                    addImageReferencesForAllTiles(imageReferences, wellPosition.getWellRow(),
                            wellPosition.getWellColumn(), channel, dataSetIdentifier, numberOfTiles);
                }
            }
        }
        return imageReferences;
    }

    private int getNumberOfTiles(IImagingDatasetLoader imageAccessor)
    {
        ImageDatasetParameters imageParameters = imageAccessor.getImageParameters();
        int numberOfTiles = imageParameters.getTileRowsNum() * imageParameters.getTileColsNum();
        return numberOfTiles;
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

    private IHierarchicalContentNode tryGetImageContent(IImagingLoaderStrategy imageLoaderStrategy,
            final ImageChannelStackReference channelStackReference, String channelCode,
            Size thumbnailSizeOrNull, String singleChannelImageTransformationCodeOrNull,
            boolean convertToPng, boolean transform)
    {
        try
        {
            return ImageChannelsUtils.getImage(imageLoaderStrategy, channelStackReference,
                    channelCode, thumbnailSizeOrNull, singleChannelImageTransformationCodeOrNull,
                    convertToPng, transform);
        } catch (EnvironmentFailureException e)
        {
            operationLog.error("Error reading image.", e);
            return null; // no image found
        }
    }

    private ImageChannelStackReference getImageChannelStackReference(
            IImagingDatasetLoader imageAccessor, MicroscopyImageReference imageReference)
    {
        return getMicroscopyImageChannelStackReference(imageAccessor, imageReference.getTile());
    }

    private static ImageChannelStackReference getImageChannelStackReference(
            IImagingDatasetLoader imageAccessor, PlateImageReference imageRef)
    {
        int tile = imageRef.getTile();
        if (imageRef.getWellPosition() != null)
        {
            Location tileLocation = getTileLocation(imageAccessor, tile);
            Location wellLocation = asLocation(imageRef.getWellPosition());
            return ImageChannelStackReference.createHCSFromLocations(wellLocation, tileLocation);
        } else
        {
            return getMicroscopyImageChannelStackReference(imageAccessor, tile);
        }
    }

    private static ImageChannelStackReference getMicroscopyImageChannelStackReference(
            IImagingDatasetLoader imageAccessor, int tileIx)
    {
        Location tileLocation = getTileLocation(imageAccessor, tileIx);
        return ImageChannelStackReference.createMicroscopyFromLocations(tileLocation);
    }

    private static Location getTileLocation(IImagingDatasetLoader imageAccessor, int tile)
    {
        return getTileLocation(tile, imageAccessor.getImageParameters().getTileColsNum());
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

    private Size tryAsSize(ImageSize thumbnailSizeOrNull)
    {
        if (thumbnailSizeOrNull == null)
        {
            return null;
        }
        return new Size(thumbnailSizeOrNull.getWidth(), thumbnailSizeOrNull.getHeight());
    }

    private IImagingDatasetLoader createImageLoader(String datasetCode)
    {
        final IHierarchicalContent content = getHierarchicalContent(datasetCode);
        return createImageLoader(datasetCode, content);
    }

    IImagingDatasetLoader createImageLoader(String dataSetCode, IHierarchicalContent content)
    {
        IImagingDatasetLoader loader = HCSImageDatasetLoaderFactory.tryCreate(content, dataSetCode);
        if (loader == null)
        {
            throw new IllegalStateException(String.format(
                    "Dataset '%s' not found in the imaging database.", dataSetCode));
        }
        return loader;
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
        String datasetTypeCode = dataset.getDataSetType().getCode();
        return datasetTypeCode.matches(ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN)
                || datasetTypeCode
                        .matches(ScreeningConstants.ANY_MICROSCOPY_IMAGE_DATASET_TYPE_PATTERN);
    }

    private List<ImgFeatureDefDTO> getFeatureDefinitions(
            List<? extends IDatasetIdentifier> featureDatasets)
    {
        List<ImgAnalysisDatasetDTO> dataSets = getAnalysisDatasets(featureDatasets);
        return getDAO().listFeatureDefsByDataSetIds(extractIds(dataSets));
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
