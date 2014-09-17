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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.image.IntensityRescaling.Channel;
import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;
import ch.systemsx.cisd.common.image.IntensityRescaling.Pixels;
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
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.AutoRescaleIntensityImageTransformerFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.IntensityRangeImageTransformerFactory;
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
    
    private static interface IColorTransformation
    {
        int transform(int rgb);
    }

    private static interface IImageCalculator
    {
        public BufferedImage create(AbsoluteImageReference imageContent);
    }

    private static class ImageWithReference
    {
        private BufferedImage image;

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
        
        public void setImage(BufferedImage image)
        {
            this.image = image;
        }

        public AbsoluteImageReference getReference()
        {
            return reference;
        }
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
     * channel or with all channels merged. This method is called by the servlets which delivers images.
     * 
     * @param params
     * @param contentProvider
     */
    public static ResponseContentStream getImageStream(ImageGenerationDescription params,
            IHierarchicalContentProvider contentProvider)
    {
        BufferedImage image = calculateImage(params, contentProvider);
        image = drawOverlays(image, params, contentProvider);
        if (image == null)
        {
            throw new UserFailureException("No image is available for parameters: " + params);
        }
        return createResponseContentStream(image, null);
    }
    
    private static BufferedImage calculateImage(ImageGenerationDescription params, 
            IHierarchicalContentProvider contentProvider)
    {
        DatasetAcquiredImagesReference imageChannels = params.tryGetImageChannels();
        if (imageChannels == null)
        {
            return null;
        }
        RequestedImageSize imageSize = new RequestedImageSize(params.tryGetThumbnailSize(), false);
        String transformationCode = params.tryGetSingleChannelTransformationCode();
        Map<String, String> transformationsPerChannel = params.tryGetTransformationsPerChannel();
        ImageLoadingHelper imageLoadingHelper = new ImageLoadingHelper(imageChannels, contentProvider, imageSize, transformationCode);
        boolean mergeAllChannels = imageLoadingHelper.isMergeAllChannels(imageChannels);
        ImageTransformationParams transformationInfo = new ImageTransformationParams(true, mergeAllChannels,
                        transformationCode, transformationsPerChannel);
        List<AbsoluteImageReference> imageContents =
                imageLoadingHelper.fetchImageContents(imageChannels, mergeAllChannels, false, transformationInfo);
        return calculateBufferedImage(imageContents, transformationInfo);
    }

    private static BufferedImage drawOverlays(BufferedImage imageOrNull, ImageGenerationDescription params, 
            IHierarchicalContentProvider contentProvider)
    {
        RequestedImageSize overlaySize = calcOverlaySize(imageOrNull, params.tryGetThumbnailSize());
        BufferedImage imageWithOverlays = imageOrNull;
        for (DatasetAcquiredImagesReference overlayChannels : params.getOverlayChannels())
        {
            // NOTE: never merges the overlays, draws each channel separately (merging looses
            // transparency and is slower)
            String transformationCode = params.tryGetSingleChannelTransformationCode();
            List<ImageWithReference> overlayImages = getSingleImagesSkipNonExisting(overlayChannels, 
                    overlaySize, transformationCode, contentProvider);
            for (ImageWithReference overlayImage : overlayImages)
            {
                if (imageWithOverlays != null)
                {
                    drawOverlay(imageWithOverlays, overlayImage);
                } else
                {
                    imageWithOverlays = overlayImage.getBufferedImage();
                }
            }
        }
        return imageWithOverlays;
    }

    private static List<ImageWithReference> getSingleImagesSkipNonExisting(
            DatasetAcquiredImagesReference imagesReference, RequestedImageSize imageSize,
            String singleChannelTransformationCodeOrNull,
            IHierarchicalContentProvider contentProvider)
    {
        ImageLoadingHelper imageLoadingHelper = new ImageLoadingHelper(imagesReference,
                contentProvider, imageSize, singleChannelTransformationCodeOrNull);
        boolean mergeAllChannels = imageLoadingHelper.isMergeAllChannels(imagesReference);
        final ImageTransformationParams transformationInfo =
                new ImageTransformationParams(true, mergeAllChannels, null, new HashMap<String, String>());
        List<AbsoluteImageReference> imageContents =
                imageLoadingHelper.fetchImageContents(imagesReference, mergeAllChannels, true,
                        transformationInfo);
        return calculateSingleImages(imageContents, new IImageCalculator()
            {
                @Override
                public BufferedImage create(AbsoluteImageReference imageContent)
                {
                    return calculateAndTransformSingleImageForDisplay(imageContent, transformationInfo, 0f);
                }
            });
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

    /**
     * Returns content of the image which is representative for the given dataset.
     */
    public static ResponseContentStream getRepresentativeImageStream(
            IHierarchicalContent dataSetRoot, String datasetCode, Location wellLocationOrNull,
            Size imageSizeLimitOrNull, String singleChannelTransformationCodeOrNull)
    {
        IImagingDatasetLoader imageAccessor = HCSImageDatasetLoaderFactory.create(dataSetRoot, datasetCode);
        IImagingLoaderStrategy loaderStrategy = ImagingLoaderStrategyFactory.createImageLoaderStrategy(imageAccessor);
        ImageLoadingHelper imageLoadingHelper = 
                new ImageLoadingHelper(loaderStrategy, imageSizeLimitOrNull, singleChannelTransformationCodeOrNull);
        List<AbsoluteImageReference> imageReferences = imageLoadingHelper.getRepresentativeImageReferences(wellLocationOrNull);
        ImageTransformationParams transformationParams = new ImageTransformationParams(true, true,
                        null, new HashMap<String, String>());
        BufferedImage image = calculateBufferedImage(imageReferences, transformationParams);
        String name = createFileName(datasetCode, wellLocationOrNull, imageSizeLimitOrNull);
        return createResponseContentStream(image, name);
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

        ImageLoadingHelper imageLoadingHelper =
                new ImageLoadingHelper(imageLoaderStrategy, imageSizeLimitOrNull,
                        singleChannelImageTransformationCodeOrNull);
        boolean mergeAllChannels = imageLoadingHelper.isMergeAllChannels(imagesReference);
        ImageTransformationParams transformationInfo =
                new ImageTransformationParams(transform, mergeAllChannels,
                        singleChannelImageTransformationCodeOrNull, new HashMap<String, String>());

        List<AbsoluteImageReference> imageContents =
                imageLoadingHelper.fetchImageContents(imagesReference, mergeAllChannels, false,
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
        image = ImageUtil.convertForDisplayIfNecessary(image, threshold);
        Channel channel = ImageUtil.getRepresentativeChannelIfEffectiveGray(image);
        if (channel != null)
        {
            final ChannelColorRGB channelColor = imageReference.getChannelColor();
            image = transformColor(image, createColorTransformation(channel, channelColor));
        }
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
            boolean enlarge = requestedSize.enlargeIfNecessary();
            boolean highQuality8Bit = requestedSize.isHighQualityRescalingRequired();
            image = ImageUtil.rescale(image, size.getWidth(), size.getHeight(), enlarge, highQuality8Bit);
            if (highQuality8Bit == false)
            {
                image = ImageUtil.convertForDisplayIfNecessary(image, null);
            }
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
                operationLog.debug("Select single channel: " + (System.currentTimeMillis() - start));
            }
        }
        return image;
    }

    private static IColorTransformation createColorTransformation(final Channel channel, final ChannelColorRGB channelColor)
    {
        return new IColorTransformation()
            {
                
                @Override
                public int transform(int rgb)
                {
                    int gray = (rgb >> channel.getShift()) & 0xff;
                    int red = scale(gray, channelColor.getR());
                    int green = scale(gray, channelColor.getG());
                    int blue = scale(gray, channelColor.getB());
                    return (red << 16) + (green << 8) + blue;
                }
                
                private int scale(int value, int maxValue)
                {
                    return maxValue == 0 ? 0 : Math.min(255, (value * 255) / maxValue);
                }
            };
    }

    @Private
    static BufferedImage calculateBufferedImage(List<AbsoluteImageReference> imageReferences,
            ImageTransformationParams transformationInfo)
    {
        AbsoluteImageReference singleImageReference = imageReferences.get(0);
        if (imageReferences.size() == 1)
        {
            return calculateAndTransformSingleImageForDisplay(singleImageReference, transformationInfo, null);
        } else
        {
            IImageTransformerFactory mergedChannelTransformationOrNull =
                    singleImageReference.getImageTransformationFactories().tryGetForMerged();
            return mergeChannels(imageReferences, transformationInfo, mergedChannelTransformationOrNull);
        }
    }

    private static BufferedImage mergeChannels(List<AbsoluteImageReference> imageReferences,
            ImageTransformationParams transformationInfo,
            IImageTransformerFactory mergedChannelTransformationOrNull)
    {
        // We do not transform single images here.
        IImageCalculator calculator = createCalculator(transformationInfo);
        List<ImageWithReference> images = calculateSingleImages(imageReferences, calculator);
        for (int i = 0; i < images.size(); i++)
        {
            ImageWithReference imageWithReference = images.get(i);
            BufferedImage image = imageWithReference.getBufferedImage();
            AbsoluteImageReference imageReference = imageWithReference.getReference();
            Channel channel = ImageUtil.getRepresentativeChannelIfEffectiveGray(image);
            if (channel != null)
            {
                final ChannelColorRGB channelColor = imageReference.getChannelColor();
                image = transformColor(image, createColorTransformation(channel, channelColor));
                imageWithReference.setImage(image);
            }
        }
        
        BufferedImage mergedImage = mergeImages(images);
        
        // non-user transformation - apply color range fix after mixing
        Map<String, String> transMap = transformationInfo.tryGetTransformationCodeForChannels();
        IImageTransformerFactory channelTransformation = mergedChannelTransformationOrNull;
        if ((transMap == null || transMap.size() == 0) && channelTransformation == null) 
        {
            Levels levels = IntensityRescaling.computeLevels(mergedImage, 30);
            int minLevel = levels.getMinLevel();
            int maxLevel = levels.getMaxLevel();
            channelTransformation = new IntensityRangeImageTransformerFactory(minLevel, maxLevel);
        }
        
        // NOTE: even if we are not merging all the channels but just few of them we use the
        // merged-channel transformation
        if (transformationInfo.isApplyNonImageLevelTransformation())
        {
            mergedImage = applyImageTransformation(mergedImage, channelTransformation);
        }
        return mergedImage;
    }

    private static IImageCalculator createCalculator(final ImageTransformationParams transformationInfo)
    {
        return new IImageCalculator()
            {
                @Override
                public BufferedImage create(AbsoluteImageReference imageContent)
                {
                    boolean applyNonImageLevelTransformation = false;
                    String transformationCode = null;
                    if (transformationInfo != null)
                    {
                        String channelCode = imageContent.tryGetChannelCode();
                        transformationCode = transformationInfo.tryGetTransformationCodeForChannel(channelCode);
                        if (transformationCode != null)
                        {
                            applyNonImageLevelTransformation = transformationInfo.isApplyNonImageLevelTransformation();
                        }
                    }
                    ImageTransformationParams info = new ImageTransformationParams(applyNonImageLevelTransformation,
                            false, transformationCode, null);
                    return calculateAndTransformSingleImageForDisplay(imageContent, info, null);
                }
            };
    }

	private static BufferedImage transform(BufferedImage image,
            AbsoluteImageReference imageReference, ImageTransformationParams transformationInfo)
    {
        BufferedImage resultImage = image;
        ImageTransfomationFactories transformations = imageReference.getImageTransformationFactories();
        // image level transformation is applied always, as it cannot be applied or changed in
        // external image viewer

        resultImage = applyImageTransformation(resultImage, transformations.tryGetForImage());

        if (transformationInfo.isApplyNonImageLevelTransformation() == false)
        {
            return resultImage;
        }
        IImageTransformerFactory channelLevelTransformationOrNull = null;
        if (transformationInfo.isUseMergedChannelsTransformation())
        {
            channelLevelTransformationOrNull = transformations.tryGetForMerged();
        } else
        {
            String transformationCode = transformationInfo.tryGetSingleChannelTransformationCode();
            String channelTransformationCode = transformationCode;
            if (transformationCode == null)
            {
                channelTransformationCode = transformations.tryGetDefaultTransformationCode();
            }
            if (channelTransformationCode != null
                    && (false == channelTransformationCode.equals(imageReference
                            .tryGetSingleChannelTransformationCode())))
            {
                channelLevelTransformationOrNull = transformations.tryGetForChannel(transformationCode);
            }
            if (channelLevelTransformationOrNull == null)
            {
                channelLevelTransformationOrNull = new AutoRescaleIntensityImageTransformerFactory(
                                ImageUtil.DEFAULT_IMAGE_OPTIMAL_RESCALING_FACTOR);
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
        IImageTransformer transformer = transformerFactoryOrNull.createTransformer();
        BufferedImage transformImage = transformer.transform(image);
        return transformImage;
    }

    private static List<ImageWithReference> calculateSingleImages(List<AbsoluteImageReference> imageContents, 
            IImageCalculator imageCalculator)
    {
        List<ImageWithReference> images = new ArrayList<ImageWithReference>();
        for (AbsoluteImageReference imageContent : imageContents)
        {
            BufferedImage image = imageCalculator.create(imageContent);
            images.add(new ImageWithReference(image, imageContent));
        }
        return images;
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

    /**
     * Transforms the given <var>bufferedImage</var> by selecting a single channel from it.
     */
    public static BufferedImage transformToChannel(BufferedImage bufferedImage, final ColorComponent colorComponent)
    {
        return transformColor(bufferedImage, new IColorTransformation()
            {
                @Override
                public int transform(int rgb)
                {
                    // We reset all ingredients besides the one which is specified by color component.
                    // The result is the rgb value with only one component which is non-zero.
                    return colorComponent.extractSingleComponent(rgb).getRGB();
                }
            });
    }
    
    public static BufferedImage transformColor(BufferedImage bufferedImage, IColorTransformation transformation)
    {
        Pixels pixels = new Pixels(bufferedImage);
        int width = pixels.getWidth();
        int height = pixels.getHeight();
        int[][] pixelData = pixels.getPixelData();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++)
        {
            int offset = y * width;
            for (int x = 0; x < width; x++)
            {
                int pixelIndex = offset + x;
                int rgb = 0;
                for (int i = 0; i < 3; i++)
                {
                    int band = Math.min(i, pixelData.length - 1);
                    rgb = (rgb << 8) + (pixelData[band][pixelIndex] & 0xff);
                }
                newImage.setRGB(x, y, transformation.transform(rgb));
            }
        }
        return newImage;
    }
    
    private static IHierarchicalContentNode createPngContent(BufferedImage image, String nameOrNull)
    {
        final byte[] output = ImageUtil.imageToPngFast(image);
        return new ByteArrayBasedContentNode(output, nameOrNull);
    }

}
