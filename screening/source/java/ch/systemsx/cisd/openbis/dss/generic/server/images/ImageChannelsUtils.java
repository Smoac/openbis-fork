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

package ch.systemsx.cisd.openbis.dss.generic.server.images;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.image.MixColors;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.common.io.ByteArrayBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.etl.IImagingLoaderStrategy;
import ch.systemsx.cisd.openbis.dss.etl.ImagingLoaderStrategyFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageTransfomationFactories;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorRGB;
import ch.systemsx.cisd.openbis.dss.generic.server.ResponseContentStream;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.DatasetAcquiredImagesReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageGenerationDescription;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageTransformationParams;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Utility classes to create an image of a specified size containing one channel or a subset of all
 * channels.
 * 
 * @author Tomasz Pylak
 */
public class ImageChannelsUtils
{
    static protected final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ImageChannelsUtils.class);

    // MIME type of the images which are produced by this class
    public static final String IMAGES_CONTENT_TYPE = "image/png";

    public static interface IDatasetDirectoryProvider
    {
        /** directory where dataset can be found in DSS store */
        File getDatasetRoot(String datasetCode);
    }

    private final IImagingLoaderStrategy imageLoaderStrategy;

    private final RequestedImageSize imageSizeLimit;

    private final String singleChannelTransformationCodeOrNull;

    @Private
    ImageChannelsUtils(IImagingLoaderStrategy imageLoaderStrategy,
            RequestedImageSize imageSizeLimit, String singleChannelTransformationCodeOrNull)
    {
        this.imageLoaderStrategy = imageLoaderStrategy;
        this.imageSizeLimit = imageSizeLimit;
        this.singleChannelTransformationCodeOrNull = singleChannelTransformationCodeOrNull;
    }

    @Private
    ImageChannelsUtils(IImagingLoaderStrategy imageLoaderStrategy, Size imageSizeLimitOrNull,
            String singleChannelTransformationCodeOrNull)
    {
        this(imageLoaderStrategy, new RequestedImageSize(imageSizeLimitOrNull, false),
                singleChannelTransformationCodeOrNull);
    }

    /**
     * Get the row content of a given existing representation format
     */
    static IHierarchicalContentNode tryGetRawContentOfExistingThumbnail(
            ImageGenerationDescription params, ImageRepresentationFormat format)
    {
        String sessionToken = params.getSessionId();
        String dataSetCode = params.tryGetImageChannels().getDatasetCode();
        ImageChannelStackReference channelStackRef =
                params.tryGetImageChannels().getChannelStackReference();
        String transformation = params.tryGetSingleChannelTransformationCode();
        String channel = params.tryGetImageChannels().getChannelCodes(null).get(0);

        IHierarchicalContentProvider contentProvider =
                ServiceProvider.getHierarchicalContentProvider();
        OpenBISSessionHolder sessionTokenHolder = new OpenBISSessionHolder();
        sessionTokenHolder.setSessionToken(sessionToken);
        contentProvider = contentProvider.cloneFor(sessionTokenHolder);

        IHierarchicalContent content = contentProvider.asContent(dataSetCode);

        IImagingDatasetLoader loader = HCSImageDatasetLoaderFactory.tryCreate(content, dataSetCode);

        if (format.isOriginal())
        {
            return loader.tryGetImage(channel, channelStackRef,
                    new RequestedImageSize(null, false, false), transformation).tryGetRawContent();
        } else
        {
            return loader.tryGetThumbnail(channel, channelStackRef,
                    new RequestedImageSize(params.tryGetThumbnailSize(), false, false),
                    transformation).tryGetRawContent();
        }
    }

    /**
     * Returns content of image for the specified tile in the specified size and for the requested
     * channel or with all channels merged.
     * 
     * @param params
     * @param contentProvider
     */
    public static ResponseContentStream getImageStream(ImageGenerationDescription params,
            IHierarchicalContentProvider contentProvider)
    {
        Size thumbnailSizeOrNull = params.tryGetThumbnailSize();

        ImageRepresentationFormat existingRepresentationFormat =
                RepresentationUtil.tryGetRepresentationFormat(params);

        if (existingRepresentationFormat != null)
        {
            IHierarchicalContentNode content =
                    tryGetRawContentOfExistingThumbnail(params, existingRepresentationFormat);
            if (content != null)
            {
                return asResponseContentStream(content);
            }
        }

        BufferedImage image = null;
        DatasetAcquiredImagesReference imageChannels = params.tryGetImageChannels();
        if (imageChannels != null)
        {
            RequestedImageSize imageSize = new RequestedImageSize(thumbnailSizeOrNull, false);
            image =
                    calculateBufferedImage(imageChannels,
                            params.tryGetSingleChannelTransformationCode(),
                            params.tryGetTransformationsPerChannel(), contentProvider, imageSize);
        }

        RequestedImageSize overlaySize = calcOverlaySize(image, thumbnailSizeOrNull);
        for (DatasetAcquiredImagesReference overlayChannels : params.getOverlayChannels())
        {
            // NOTE: never merges the overlays, draws each channel separately (merging looses
            // transparency and is slower)
            List<ImageWithReference> overlayImages =
                    getSingleImagesSkipNonExisting(overlayChannels, overlaySize,
                            params.tryGetSingleChannelTransformationCode(), contentProvider);
            for (ImageWithReference overlayImage : overlayImages)
            {
                if (image != null)
                {
                    drawOverlay(image, overlayImage);
                } else
                {
                    image = overlayImage.getBufferedImage();
                }
            }
        }
        if (image == null)
        {
            throw new UserFailureException("No image is available for parameters: " + params);
        }
        return createResponseContentStream(image, null);
    }

    private static List<ImageWithReference> getSingleImagesSkipNonExisting(
            DatasetAcquiredImagesReference imagesReference, RequestedImageSize imageSize,
            String singleChannelTransformationCodeOrNull,
            IHierarchicalContentProvider contentProvider)
    {
        ImageChannelsUtils utils =
                createImageChannelsUtils(imagesReference, contentProvider, imageSize,
                        singleChannelTransformationCodeOrNull);
        boolean mergeAllChannels = utils.isMergeAllChannels(imagesReference);
        ImageTransformationParams transformationInfo =
                new ImageTransformationParams(true, mergeAllChannels, null,
                        new HashMap<String, String>());
        List<AbsoluteImageReference> imageContents =
                utils.fetchImageContents(imagesReference, mergeAllChannels, true,
                        transformationInfo);
        return calculateSingleImagesForDisplay(imageContents, transformationInfo, 0.0f, null);
    }

    private static RequestedImageSize getSize(BufferedImage img, boolean highQuality)
    {
        return new RequestedImageSize(new Size(img.getWidth(), img.getHeight()), true, highQuality);
    }

    private static RequestedImageSize calcOverlaySize(BufferedImage imageOrNull,
            Size thumbnailSizeOrNull)
    {
        if (thumbnailSizeOrNull == null)
        {
            // thumbnais are not used, so use the original size even if it does not match
            return RequestedImageSize.createOriginal();
        } else
        {
            // we want higher quality only if thumbnail overlays are generated
            boolean highQuality = true;
            if (imageOrNull != null)
            {
                // all overlays have to be of the same size as the basic image
                return getSize(imageOrNull, highQuality);
            } else
            {
                // if there is no basic image yet, enlarging too small images is not necessary
                return new RequestedImageSize(thumbnailSizeOrNull, false, highQuality);
            }
        }
    }

    private static ResponseContentStream createResponseContentStream(BufferedImage image,
            String nameOrNull)
    {
        IHierarchicalContentNode imageContent = createPngContent(image, nameOrNull);
        return asResponseContentStream(imageContent);
    }

    private static BufferedImage calculateBufferedImage(
            DatasetAcquiredImagesReference imageChannels,
            String singleChannelTransformationCodeOrNull,
            Map<String, String> transformationsPerChannels,
            IHierarchicalContentProvider contentProvider, RequestedImageSize imageSizeLimit)
    {
        ImageChannelsUtils imageChannelsUtils =
                createImageChannelsUtils(imageChannels, contentProvider, imageSizeLimit,
                        singleChannelTransformationCodeOrNull);
        boolean useMergedChannelsTransformation =
                imageChannelsUtils.isMergeAllChannels(imageChannels);
        ImageTransformationParams transformationInfo =
                new ImageTransformationParams(true, useMergedChannelsTransformation,
                        singleChannelTransformationCodeOrNull, transformationsPerChannels);

        return imageChannelsUtils.calculateBufferedImage(imageChannels, transformationInfo);
    }

    private static ImageChannelsUtils createImageChannelsUtils(
            DatasetAcquiredImagesReference imageChannels,
            IHierarchicalContentProvider contentProvider, RequestedImageSize imageSizeLimit,
            String singleChannelTransformationCodeOrNull)
    {
        IImagingDatasetLoader imageAccessor = createImageAccessor(imageChannels, contentProvider);
        return new ImageChannelsUtils(
                ImagingLoaderStrategyFactory.createImageLoaderStrategy(imageAccessor),
                imageSizeLimit, singleChannelTransformationCodeOrNull);
    }

    @Private
    BufferedImage calculateBufferedImage(DatasetAcquiredImagesReference imageChannels,
            ImageTransformationParams transformationInfo)
    {
        boolean mergeAllChannels = isMergeAllChannels(imageChannels);
        List<AbsoluteImageReference> imageContents =
                fetchImageContents(imageChannels, mergeAllChannels, false, transformationInfo);
        return calculateBufferedImage(imageContents, transformationInfo);
    }

    private boolean isMergeAllChannels(DatasetAcquiredImagesReference imageChannels)
    {
        return imageChannels.isMergeAllChannels(getAllChannelCodes());
    }

    /**
     * @param skipNonExisting if true references to non-existing images are ignored, otherwise an
     *            exception is thrown
     * @param mergeAllChannels true if all existing channel images should be merged
     * @param transformationInfo
     */
    private List<AbsoluteImageReference> fetchImageContents(
            DatasetAcquiredImagesReference imagesReference, boolean mergeAllChannels,
            boolean skipNonExisting, ImageTransformationParams transformationInfo)
    {
        List<String> channelCodes = imagesReference.getChannelCodes(getAllChannelCodes());
        List<AbsoluteImageReference> images = new ArrayList<AbsoluteImageReference>();
        for (String channelCode : channelCodes)
        {
            ImageChannelStackReference channelStackReference =
                    imagesReference.getChannelStackReference();
            AbsoluteImageReference image =
                    imageLoaderStrategy.tryGetImage(channelCode, channelStackReference,
                            imageSizeLimit, singleChannelTransformationCodeOrNull);
            if (image == null && skipNonExisting == false)
            {
                throw createImageNotFoundException(channelStackReference, channelCode);
            }
            if (image != null)
            {
                images.add(image);
            }
        }

        // Optimization for a case where all channels are on one image
        if (mergeAllChannels
                && (false == shouldApplySingleChannelsTransformations(transformationInfo)))
        {
            AbsoluteImageReference allChannelsImageReference =
                    tryCreateAllChannelsImageReference(images);
            if (allChannelsImageReference != null)
            {
                images.clear();
                images.add(allChannelsImageReference);
            }
        }
        return images;
    }

    private boolean shouldApplySingleChannelsTransformations(
            ImageTransformationParams transformationInfo)
    {
        if (transformationInfo == null
                || transformationInfo.tryGetTransformationCodeForChannels() == null
                || transformationInfo.tryGetTransformationCodeForChannels().size() == 0)
        {
            return false;
        }

        return true;
    }

    private static IImagingDatasetLoader createImageAccessor(
            DatasetAcquiredImagesReference imagesReference,
            IHierarchicalContentProvider contentProvider)
    {
        String datasetCode = imagesReference.getDatasetCode();
        IHierarchicalContent dataSetRoot = contentProvider.asContent(datasetCode);
        return createDatasetLoader(dataSetRoot, datasetCode);
    }

    /**
     * Returns content of the image which is representative for the given dataset.
     */
    public static ResponseContentStream getRepresentativeImageStream(
            IHierarchicalContent dataSetRoot, String datasetCode, Location wellLocationOrNull,
            Size imageSizeLimitOrNull, String singleChannelTransformationCodeOrNull)
    {
        IImagingDatasetLoader imageAccessor = createDatasetLoader(dataSetRoot, datasetCode);
        List<AbsoluteImageReference> imageReferences =
                new ImageChannelsUtils(
                        ImagingLoaderStrategyFactory.createImageLoaderStrategy(imageAccessor),
                        imageSizeLimitOrNull, singleChannelTransformationCodeOrNull)
                        .getRepresentativeImageReferences(wellLocationOrNull);
        BufferedImage image =
                calculateBufferedImage(imageReferences, new ImageTransformationParams(true, true,
                        null, new HashMap<String, String>()));
        String name = createFileName(datasetCode, wellLocationOrNull, imageSizeLimitOrNull);
        return createResponseContentStream(image, name);
    }

    private static IImagingDatasetLoader createDatasetLoader(IHierarchicalContent dataSetRoot,
            String datasetCode)
    {
        IImagingDatasetLoader loader =
                HCSImageDatasetLoaderFactory.tryCreate(dataSetRoot, datasetCode);
        if (loader == null)
        {
            throw new IllegalStateException(String.format(
                    "Dataset '%s' not found in the imaging database.", datasetCode));
        }
        return loader;
    }

    private static String createFileName(String datasetCode, Location wellLocationOrNull,
            Size imageSizeLimitOrNull)
    {
        String name = "dataset_" + datasetCode;
        if (wellLocationOrNull != null)
        {
            name += "_row" + wellLocationOrNull.getY();
            name += "_col" + wellLocationOrNull.getX();
        }
        if (imageSizeLimitOrNull != null)
        {
            name += "_small";
        }
        name += ".png";
        return name;
    }

    private static ResponseContentStream asResponseContentStream(
            IHierarchicalContentNode imageContent)
    {
        return ResponseContentStream.create(imageContent.getInputStream(),
                imageContent.getFileLength(), ImageChannelsUtils.IMAGES_CONTENT_TYPE,
                imageContent.getName());
    }

    /**
     * @return an image for the specified tile in the specified size and for the requested channel.
     */
    public static IHierarchicalContentNode getImage(IImagingLoaderStrategy imageLoaderStrategy,
            ImageChannelStackReference channelStackReference, String chosenChannelCode,
            Size imageSizeLimitOrNull, String singleChannelImageTransformationCodeOrNull,
            boolean convertToPng, boolean transform)
    {
        DatasetAcquiredImagesReference imagesReference =
                createDatasetAcquiredImagesReference(imageLoaderStrategy, channelStackReference,
                        chosenChannelCode == null ? ScreeningConstants.MERGED_CHANNELS
                                : chosenChannelCode);

        ImageChannelsUtils imageChannelsUtils =
                new ImageChannelsUtils(imageLoaderStrategy, imageSizeLimitOrNull,
                        singleChannelImageTransformationCodeOrNull);
        boolean mergeAllChannels = imageChannelsUtils.isMergeAllChannels(imagesReference);
        ImageTransformationParams transformationInfo =
                new ImageTransformationParams(transform, mergeAllChannels,
                        singleChannelImageTransformationCodeOrNull, new HashMap<String, String>());

        List<AbsoluteImageReference> imageContents =
                imageChannelsUtils.fetchImageContents(imagesReference, mergeAllChannels, false,
                        transformationInfo);

        IHierarchicalContentNode contentNode = tryGetRawContent(convertToPng, imageContents);
        if (contentNode != null)
        {
            return contentNode;
        }
        BufferedImage image = calculateBufferedImage(imageContents, transformationInfo);
        return createPngContent(image, null);
    }

    private static DatasetAcquiredImagesReference createDatasetAcquiredImagesReference(
            IImagingLoaderStrategy imageLoaderStrategy,
            ImageChannelStackReference channelStackReference, String chosenChannelCode)
    {
        String datasetCode = imageLoaderStrategy.getImageParameters().getDatasetCode();
        boolean isMergedChannels =
                ScreeningConstants.MERGED_CHANNELS.equalsIgnoreCase(chosenChannelCode);
        if (isMergedChannels)
        {
            return DatasetAcquiredImagesReference.createForMergedChannels(datasetCode,
                    channelStackReference);
        } else
        {
            return DatasetAcquiredImagesReference.createForSingleChannel(datasetCode,
                    channelStackReference, chosenChannelCode);
        }
    }

    // optimization: if there is exactly one image reference, maybe its original raw content is the
    // appropriate answer?
    private static IHierarchicalContentNode tryGetRawContent(boolean convertToPng,
            List<AbsoluteImageReference> imageContents)
    {
        if (imageContents.size() == 1 && convertToPng == false)
        {
            AbsoluteImageReference imageReference = imageContents.get(0);
            return imageReference.tryGetRawContentForOriginalImage();
        }
        return null;
    }

    private List<AbsoluteImageReference> getRepresentativeImageReferences(
            Location wellLocationOrNull)
    {
        List<AbsoluteImageReference> images = new ArrayList<AbsoluteImageReference>();

        for (String chosenChannel : getAllChannelCodes())
        {
            AbsoluteImageReference image =
                    getRepresentativeImageReference(chosenChannel, wellLocationOrNull);
            images.add(image);
        }
        return images;
    }

    private List<String> getAllChannelCodes()
    {
        return imageLoaderStrategy.getImageParameters().getChannelsCodes();
    }

    /**
     * @throw {@link EnvironmentFailureException} when image does not exist
     */
    private AbsoluteImageReference getRepresentativeImageReference(String channelCode,
            Location wellLocationOrNull)
    {
        AbsoluteImageReference image =
                imageLoaderStrategy.tryGetRepresentativeImage(channelCode, wellLocationOrNull,
                        imageSizeLimit, singleChannelTransformationCodeOrNull);
        if (image != null)
        {
            return image;
        } else
        {
            throw EnvironmentFailureException.fromTemplate(
                    "No representative "
                            + (imageSizeLimit.isThumbnailRequired() ? "thumbnail" : "image")
                            + " found for well %s and channel %s", wellLocationOrNull, channelCode);
        }
    }

    /**
     * @param threshold
     * @param useMergedChannelsTransformation sometimes we can have a single image which contain all
     *            channels merged. In this case a different transformation will be applied to it.
     */
    private static BufferedImage calculateAndTransformSingleImageForDisplay(
            AbsoluteImageReference imageReference, ImageTransformationParams transformationInfo,
            Float threshold)
    {
        BufferedImage image = calculateSingleImage(imageReference);
        image = transform(image, imageReference, transformationInfo);
        image =
                threshold == null ? ImageUtil.convertForDisplayIfNecessary(image) : ImageUtil
                        .convertForDisplayIfNecessary(image, threshold);
        return image;
    }

    private static BufferedImage calculateSingleImage(AbsoluteImageReference imageReference)
    {
        long start = operationLog.isDebugEnabled() ? System.currentTimeMillis() : 0;
        BufferedImage image = imageReference.getUnchangedImage();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Load original image: " + (System.currentTimeMillis() - start));
        }

        // resized the image if necessary
        RequestedImageSize requestedSize = imageReference.getRequestedSize();

        Size size = requestedSize.tryGetThumbnailSize();
        if (size != null)
        {
            start = operationLog.isDebugEnabled() ? System.currentTimeMillis() : 0;
            image =
                    ImageUtil.rescale(image, size.getWidth(), size.getHeight(),
                            requestedSize.enlargeIfNecessary(),
                            requestedSize.isHighQualityRescalingRequired());
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Create thumbnail: " + (System.currentTimeMillis() - start));
            }
        }

        // choose color component if necessary
        final ColorComponent colorComponentOrNull = imageReference.tryGetColorComponent();
        if (colorComponentOrNull != null)
        {
            start = operationLog.isDebugEnabled() ? System.currentTimeMillis() : 0;
            image = transformToChannel(image, colorComponentOrNull);
            if (operationLog.isDebugEnabled())
            {
                operationLog
                        .debug("Select single channel: " + (System.currentTimeMillis() - start));
            }
        }
        return image;
    }

    /**
     * @param allChannelsMerged if true then we use one special transformation on the merged images
     *            instead of transforming every single image.
     */
    private static BufferedImage calculateBufferedImage(
            List<AbsoluteImageReference> imageReferences,
            ImageTransformationParams transformationInfo)
    {
        AbsoluteImageReference singleImageReference = imageReferences.get(0);
        if (imageReferences.size() == 1)
        {
            return calculateAndTransformSingleImageForDisplay(singleImageReference,
                    transformationInfo, null);
        } else
        {
            IImageTransformerFactory mergedChannelTransformationOrNull =
                    singleImageReference.getImageTransfomationFactories().tryGetForMerged();
            return mergeChannels(imageReferences, transformationInfo,
                    mergedChannelTransformationOrNull);
        }
    }

    private static BufferedImage mergeChannels(List<AbsoluteImageReference> imageReferences,
            ImageTransformationParams transformationInfo,
            IImageTransformerFactory mergedChannelTransformationOrNull)
    {
        // We do not transform single images here.
        List<ImageWithReference> images =
                calculateSingleImagesForDisplay(imageReferences, null, null, transformationInfo);
        BufferedImage mergedImage = mergeImages(images);
        // NOTE: even if we are not merging all the channels but just few of them we use the
        // merged-channel transformation
        if (transformationInfo.isApplyNonImageLevelTransformation())
        {
            mergedImage = applyImageTransformation(mergedImage, mergedChannelTransformationOrNull);
        }
        return mergedImage;
    }

    private static BufferedImage transform(BufferedImage image,
            AbsoluteImageReference imageReference, ImageTransformationParams transformationInfo)
    {
        BufferedImage resultImage = image;
        ImageTransfomationFactories transfomations =
                imageReference.getImageTransfomationFactories();
        // image level transformation is applied always, as it cannot be applied or changed in
        // external image viewer
        resultImage = applyImageTransformation(resultImage, transfomations.tryGetForImage());

        if (transformationInfo.isApplyNonImageLevelTransformation() == false)
        {
            return resultImage;
        }
        IImageTransformerFactory channelLevelTransformationOrNull = null;
        if (transformationInfo.isUseMergedChannelsTransformation())
        {
            channelLevelTransformationOrNull = transfomations.tryGetForMerged();
        } else
        {
            String channelTransformationCode =
                    transformationInfo.tryGetSingleChannelTransformationCode() == null ? transfomations
                            .tryGetDefaultTransformationCode() : transformationInfo
                            .tryGetSingleChannelTransformationCode();

            if (channelTransformationCode != null
                    && (false == channelTransformationCode.equals(imageReference
                            .tryGetSingleChannelTransformationCode())))
            {
                channelLevelTransformationOrNull =
                        transfomations.tryGetForChannel(transformationInfo
                                .tryGetSingleChannelTransformationCode());
            }
        }

        return applyImageTransformation(resultImage, channelLevelTransformationOrNull);
    }

    private static BufferedImage applyImageTransformation(BufferedImage image,
            IImageTransformerFactory transformerFactoryOrNull)
    {
        if (transformerFactoryOrNull == null)
        {
            return image;
        }
        return transformerFactoryOrNull.createTransformer().transform(image);
    }

    private static class ImageWithReference
    {
        private final BufferedImage image;

        private final AbsoluteImageReference reference;

        public ImageWithReference(BufferedImage image, AbsoluteImageReference reference)
        {
            this.image = image;
            this.reference = reference;
        }

        public BufferedImage getBufferedImage()
        {
            return image;
        }

        public AbsoluteImageReference getReference()
        {
            return reference;
        }
    }

    /**
     * @param transformationInfoOrNull if null all transformations (including image-level) will be
     *            skipped
     * @param transformationInfo
     */
    private static List<ImageWithReference> calculateSingleImagesForDisplay(
            List<AbsoluteImageReference> imageReferences,
            ImageTransformationParams transformationInfoOrNull, Float threshold,
            ImageTransformationParams transformationInfoForMergingOrNull)
    {
        List<ImageWithReference> images = new ArrayList<ImageWithReference>();
        for (AbsoluteImageReference imageRef : imageReferences)
        {
            BufferedImage image;
            if (transformationInfoOrNull != null)
            {
                image =
                        calculateAndTransformSingleImageForDisplay(imageRef,
                                transformationInfoOrNull, threshold);
            } else if (transformationInfoForMergingOrNull != null
                    && null != transformationInfoForMergingOrNull
                            .tryGetTransformationCodeForChannel(imageRef.tryGetChannelCode()))
            {
                String transformationCode =
                        transformationInfoForMergingOrNull
                                .tryGetTransformationCodeForChannel(imageRef.tryGetChannelCode());
                image =
                        calculateAndTransformSingleImageForDisplay(
                                imageRef,
                                new ImageTransformationParams(transformationInfoForMergingOrNull
                                        .isApplyNonImageLevelTransformation(), false,
                                        transformationCode, null), threshold);
            } else
            {
                // NOTE: here we skip image level transformations as well
                image = calculateSingleImage(imageRef);
                image =
                        threshold == null ? ImageUtil.convertForDisplayIfNecessary(image)
                                : ImageUtil.convertForDisplayIfNecessary(image, threshold);
            }
            images.add(new ImageWithReference(image, imageRef));
        }
        return images;
    }

    // Checks if all images differ only at the color component level and stem from the same page
    // of the same file. If that's the case any image from the collection contains the merged
    // channels image (if we erase the color component).
    private static AbsoluteImageReference tryCreateAllChannelsImageReference(
            List<AbsoluteImageReference> imageReferences)
    {
        AbsoluteImageReference lastFound = null;
        for (AbsoluteImageReference image : imageReferences)
        {
            if (lastFound == null)
            {
                lastFound = image;
            } else
            {
                if (equals(image.tryGetImageID(), lastFound.tryGetImageID()) == false
                        || image.getUniqueId().equals(lastFound.getUniqueId()) == false)
                {
                    return null;
                }
            }
        }
        if (lastFound != null)
        {
            return lastFound.createWithoutColorComponent();
        } else
        {
            return null;
        }
    }

    private static boolean equals(String i1OrNull, String i2OrNull)
    {
        return (i1OrNull == null) ? (i2OrNull == null) : i1OrNull.equals(i2OrNull);
    }

    // this method always returns RGB images, even if the input was in grayscale
    private static BufferedImage mergeImages(List<ImageWithReference> images)
    {
        BufferedImage[] bufferedImages = new BufferedImage[images.size()];
        Color[] colors = new Color[images.size()];
        for (int i = 0; i < images.size(); i++)
        {
            ImageWithReference image = images.get(i);
            bufferedImages[i] = image.getBufferedImage();
            colors[i] = getColor(image);
        }

        ColorComponent[] colorComponents = tryExtractColorComponent(images);
        if (colorComponents != null)
        {
            return ColorComponentImageChannelMerger.mergeByExtractingComponents(bufferedImages,
                    colorComponents);
        } else
        {
            return MixColors.mixImages(bufferedImages, colors, false, 0);
        }
    }

    private static Color getColor(ImageWithReference image)
    {
        return getColor(image.getReference().getChannelColor());
    }

    private static Color getColor(ChannelColorRGB color)
    {
        return new Color(color.getR(), color.getG(), color.getB());
    }

    private static ColorComponent[] tryExtractColorComponent(List<ImageWithReference> images)
    {
        ColorComponent[] components = new ColorComponent[images.size()];
        int i = 0;
        for (ImageWithReference image : images)
        {
            ColorComponent colorComponent = image.getReference().tryGetColorComponent();
            if (colorComponent == null)
            {
                if (i == 0)
                {
                    return null;
                } else
                {
                    throw new IllegalStateException(
                            "Some images have color component set and some have it unset.");
                }
            }
            components[i++] = colorComponent;
        }
        return components;
    }

    private static void drawOverlay(BufferedImage image, ImageWithReference overlayImage)
    {
        BufferedImage overlayBufferedImage = overlayImage.getBufferedImage();
        if (supportsTransparency(overlayImage))
        {
            drawTransparentOverlayFast(image, overlayBufferedImage);
        } else
        {
            drawOverlaySlow(image, overlayBufferedImage);
        }
    }

    private static void drawTransparentOverlayFast(BufferedImage image,
            BufferedImage overlayBufferedImage)
    {
        Graphics2D graphics = image.createGraphics();
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
        graphics.setComposite(ac);
        graphics.drawImage(overlayBufferedImage, null, null);
    }

    /**
     * Draws overlay by computing the maximal color components (r,g,b) for each pixel. In this way
     * both transparent and black pixels are not drawn. Useful when overlays are saved in a format
     * which does not support transparency.
     */
    private static void drawOverlaySlow(BufferedImage image, BufferedImage overlayImage)
    {
        int width = Math.min(image.getWidth(), overlayImage.getWidth());
        int height = Math.min(image.getHeight(), overlayImage.getHeight());

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int imageRGB = image.getRGB(x, y);
                int overlayRGB = overlayImage.getRGB(x, y);
                int overlayedRGB = overlayRGBColor(imageRGB, overlayRGB);
                image.setRGB(x, y, overlayedRGB);
            }
        }
    }

    // creates a color with a maximum value of each component
    private static int overlayRGBColor(int imageRGB, int overlayRGB)
    {
        Color imageColor = new Color(imageRGB);
        Color overlayColor = new Color(overlayRGB, true);

        if (overlayColor.getAlpha() == 0)
        {
            return imageRGB; // overlay is transparent, return the original pixel
        } else
        {
            int r = Math.max(imageColor.getRed(), overlayColor.getRed());
            int g = Math.max(imageColor.getGreen(), overlayColor.getGreen());
            int b = Math.max(imageColor.getBlue(), overlayColor.getBlue());
            return new Color(r, g, b).getRGB();
        }
    }

    private static boolean supportsTransparency(ImageWithReference image)
    {
        return image.getBufferedImage().getColorModel().hasAlpha();
    }

    // --------- common

    private EnvironmentFailureException createImageNotFoundException(
            ImageChannelStackReference channelStackReference, String chosenChannelCode)
    {
        return EnvironmentFailureException.fromTemplate(
                "No " + (imageSizeLimit.isThumbnailRequired() ? "thumbnail" : "image")
                        + " found for channel stack %s and channel %s", channelStackReference,
                chosenChannelCode);
    }

    /**
     * Transforms the given <var>bufferedImage</var> by selecting a single channel from it.
     */
    public static BufferedImage transformToChannel(BufferedImage bufferedImage,
            ColorComponent colorComponent)
    {
        BufferedImage newImage = createNewRGBImage(bufferedImage);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int rgb = bufferedImage.getRGB(x, y);
                int channelColor = extractSingleComponent(rgb, colorComponent);
                newImage.setRGB(x, y, channelColor);
            }
        }
        return newImage;
    }

    // We reset all ingredients besides the one which is specified by color component.
    // The result is the rgb value with only one component which is non-zero.
    private static int extractSingleComponent(int rgb, ColorComponent colorComponent)
    {
        return colorComponent.extractSingleComponent(rgb).getRGB();
    }

    // NOTE: drawing on this image will not preserve transparency - but we do not need it and the
    // image is smaller
    private static BufferedImage createNewRGBImage(RenderedImage bufferedImage)
    {
        BufferedImage newImage =
                new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
        return newImage;
    }

    private static IHierarchicalContentNode createPngContent(BufferedImage image, String nameOrNull)
    {
        final byte[] output = ImageUtil.imageToPngFast(image);
        return new ByteArrayBasedContentNode(output, nameOrNull);
    }

}
