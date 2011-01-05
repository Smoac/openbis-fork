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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.io.ByteArrayBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.ResponseContentStream;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.DatasetAcquiredImagesReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageGenerationDescription;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
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

    /**
     * Returns content of image for the specified tile in the specified size and for the requested
     * channel or with all channels merged.
     * 
     * @param datasetDirectoryProvider
     */
    public static ResponseContentStream getImageStream(ImageGenerationDescription params,
            IDatasetDirectoryProvider datasetDirectoryProvider)
    {
        Size thumbnailSizeOrNull = params.tryGetThumbnailSize();

        BufferedImage image = null;
        DatasetAcquiredImagesReference imageChannels = params.tryGetImageChannels();
        if (imageChannels != null)
        {
            image =
                    calculateBufferedImage(imageChannels, datasetDirectoryProvider,
                            thumbnailSizeOrNull);
        }

        for (DatasetAcquiredImagesReference overlayChannels : params.getOverlayChannels())
        {
            BufferedImage overlayImage =
                    calculateBufferedImage(overlayChannels, datasetDirectoryProvider,
                            thumbnailSizeOrNull);
            if (image != null)
            {
                drawOverlay(image, overlayImage);
            } else
            {
                image = overlayImage;
            }
        }
        return createResponseContentStream(image, null);
    }

    private static ResponseContentStream createResponseContentStream(BufferedImage image,
            String nameOrNull)
    {
        IContent imageContent = createPngContent(image, nameOrNull);
        return asResponseContentStream(imageContent);
    }

    private static BufferedImage calculateBufferedImage(
            DatasetAcquiredImagesReference imageChannels,
            IDatasetDirectoryProvider datasetDirectoryProvider, Size thumbnailSizeOrNull)
    {
        IImagingDatasetLoader imageAccessor =
                createImageAccessor(imageChannels, datasetDirectoryProvider);
        return calculateBufferedImage(imageChannels, imageAccessor, thumbnailSizeOrNull, true);
    }

    @Private
    static BufferedImage calculateBufferedImage(DatasetAcquiredImagesReference imageChannels,
            IImagingDatasetLoader imageAccessor, Size thumbnailSizeOrNull, boolean transform)
    {
        boolean mergeAllChannels = isMergeAllChannels(imageChannels, imageAccessor);
        List<AbsoluteImageReference> imageContents =
                fetchImageContents(imageAccessor, imageChannels, thumbnailSizeOrNull,
                        mergeAllChannels);
        return mergeChannels(imageContents, transform, mergeAllChannels);
    }

    // We want to treat the case where merged channels were requested in the same way as the case
    // where all channel names have been enumerated.
    private static boolean isMergeAllChannels(DatasetAcquiredImagesReference imageChannels,
            IImagingDatasetLoader imageAccessor)
    {
        if (imageChannels.isMergeAllChannels())
        {
            return true;
        }
        List<String> wantedChannelCodes = imageChannels.getChannelCodes();
        List<String> allChannelsCodes = imageAccessor.getImageParameters().getChannelsCodes();
        if (allChannelsCodes.size() == 1)
        {
            return false; // there is only one channel in total, single channel transformation
                          // should be used
        }
        for (String existingChannelCode : allChannelsCodes)
        {
            if (wantedChannelCodes.indexOf(existingChannelCode) == -1)
            {
                return false;
            }
        }
        return true;
    }

    private static List<AbsoluteImageReference> fetchImageContents(
            IImagingDatasetLoader imageAccessor, DatasetAcquiredImagesReference imagesReference,
            Size thumbnailSizeOrNull, boolean mergeAllChannels)
    {
        List<String> channelCodes = getChannelCodes(imagesReference, imageAccessor);
        List<AbsoluteImageReference> images = new ArrayList<AbsoluteImageReference>();
        for (String channelCode : channelCodes)
        {
            AbsoluteImageReference image =
                    getImageReference(imageAccessor, imagesReference.getChannelStackReference(),
                            channelCode, thumbnailSizeOrNull);
            images.add(image);
        }

        // Optimization for a case where all channels are on one image
        if (mergeAllChannels)
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

    private static IImagingDatasetLoader createImageAccessor(
            DatasetAcquiredImagesReference imagesReference,
            IDatasetDirectoryProvider datasetDirectoryProvider)
    {
        String datasetCode = imagesReference.getDatasetCode();
        File datasetRoot = datasetDirectoryProvider.getDatasetRoot(datasetCode);
        return HCSImageDatasetLoaderFactory.create(datasetRoot, datasetCode);
    }

    private static List<String> getChannelCodes(DatasetAcquiredImagesReference imagesReference,
            IImagingDatasetLoader imageAccessor)
    {
        if (imagesReference.isMergeAllChannels())
        {
            return imageAccessor.getImageParameters().getChannelsCodes();
        } else
        {
            return imagesReference.getChannelCodes();
        }
    }

    /**
     * Returns content of the image which is representative for the given dataset.
     */
    public static ResponseContentStream getRepresentativeImageStream(File datasetRoot,
            String datasetCode, Location wellLocationOrNull, Size thumbnailSizeOrNull)
    {
        IImagingDatasetLoader imageAccessor =
                HCSImageDatasetLoaderFactory.create(datasetRoot, datasetCode);
        List<AbsoluteImageReference> imageReferences =
                getRepresentativeImageReferences(imageAccessor, wellLocationOrNull,
                        thumbnailSizeOrNull);
        BufferedImage image = mergeChannels(imageReferences, true, true);
        String name = createFileName(datasetCode, wellLocationOrNull, thumbnailSizeOrNull);
        return createResponseContentStream(image, name);
    }

    private static String createFileName(String datasetCode, Location wellLocationOrNull,
            Size thumbnailSizeOrNull)
    {
        String name = "dataset_" + datasetCode;
        if (wellLocationOrNull != null)
        {
            name += "_row" + wellLocationOrNull.getY();
            name += "_col" + wellLocationOrNull.getX();
        }
        if (thumbnailSizeOrNull != null)
        {
            name += "_small";
        }
        name += ".png";
        return name;
    }

    private static ResponseContentStream asResponseContentStream(IContent image)
    {
        return ResponseContentStream.create(image.getInputStream(), image.getSize(),
                ImageChannelsUtils.IMAGES_CONTENT_TYPE, image.tryGetName());
    }

    /**
     * @return an image for the specified tile in the specified size and for the requested channel.
     */
    public static IContent getImage(IImagingDatasetLoader imageAccessor,
            ImageChannelStackReference channelStackReference, String chosenChannelCode,
            Size thumbnailSizeOrNull, boolean convertToPng)
    {
        String datasetCode = imageAccessor.getImageParameters().getDatasetCode();
        boolean isMergedChannels =
                ScreeningConstants.MERGED_CHANNELS.equalsIgnoreCase(chosenChannelCode);
        List<String> channelCodes = isMergedChannels ? null : Arrays.asList(chosenChannelCode);

        DatasetAcquiredImagesReference imagesReference =
                new DatasetAcquiredImagesReference(datasetCode, channelStackReference, channelCodes);

        boolean mergeAllChannels = isMergeAllChannels(imagesReference, imageAccessor);
        List<AbsoluteImageReference> imageContents =
                fetchImageContents(imageAccessor, imagesReference, thumbnailSizeOrNull,
                        mergeAllChannels);

        IContent rawContent = tryGetRawContent(convertToPng, imageContents);
        if (rawContent != null)
        {
            return rawContent;
        }
        BufferedImage image = mergeChannels(imageContents, false, mergeAllChannels);
        return createPngContent(image, null);
    }

    // optimization: maybe the original raw content is the appropriate answer?
    private static IContent tryGetRawContent(boolean convertToPng,
            List<AbsoluteImageReference> imageContents)
    {
        if (convertToPng == false && imageContents.size() == 1)
        {
            AbsoluteImageReference imageReference = imageContents.get(0);
            if (imageReference.tryGetColorComponent() == null
                    && imageReference.tryGetPage() == null && imageReference.tryGetSize() == null)
            {
                return imageReference.getContent();
            }
        }
        return null;
    }

    private static List<AbsoluteImageReference> getRepresentativeImageReferences(
            IImagingDatasetLoader imageAccessor, Location wellLocationOrNull,
            Size thumbnailSizeOrNull)
    {
        List<AbsoluteImageReference> images = new ArrayList<AbsoluteImageReference>();

        for (String chosenChannel : imageAccessor.getImageParameters().getChannelsCodes())
        {
            AbsoluteImageReference image =
                    getRepresentativeImageReference(imageAccessor, chosenChannel,
                            wellLocationOrNull, thumbnailSizeOrNull);
            images.add(image);
        }
        return images;
    }

    /**
     * @throw {@link EnvironmentFailureException} when image does not exist
     */
    private static AbsoluteImageReference getRepresentativeImageReference(
            IImagingDatasetLoader imageAccessor, String channelCode, Location wellLocationOrNull,
            Size thumbnailSizeOrNull)
    {
        AbsoluteImageReference image =
                imageAccessor.tryGetRepresentativeImage(channelCode, wellLocationOrNull,
                        thumbnailSizeOrNull);
        if (image != null)
        {
            return image;
        } else
        {
            throw EnvironmentFailureException.fromTemplate("No representative "
                    + (thumbnailSizeOrNull != null ? "thumbnail" : "image")
                    + " found for well %s and channel %s", wellLocationOrNull, channelCode);
        }
    }

    /**
     * @param allChannelsMerged sometimes we can have a single image which contain all channels
     *            merged. In this case a different transformation will be applied to it.
     */
    private static BufferedImage calculateAndTransformSingleImage(
            AbsoluteImageReference imageReference, boolean transform, boolean allChannelsMerged)
    {
        IImageTransformerFactory transformerFactory = null;
        if (transform)
        {
            transformerFactory =
                    allChannelsMerged ? imageReference.getTransformerFactoryForMergedChannels()
                            : imageReference.getTransformerFactory();
        }
        return transform(calculateSingleImage(imageReference), transformerFactory);
    }

    private static BufferedImage calculateSingleImage(AbsoluteImageReference imageReference)
    {
        long start = operationLog.isDebugEnabled() ? System.currentTimeMillis() : 0;
        BufferedImage image = loadImage(imageReference);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Load original image: " + (System.currentTimeMillis() - start));
        }

        // resized the image if necessary
        Size sizeOrNull = imageReference.tryGetSize();
        if (sizeOrNull != null && isRescalingNeeded(image, sizeOrNull))
        {
            start = operationLog.isDebugEnabled() ? System.currentTimeMillis() : 0;
            image = ImageUtil.createThumbnail(image, sizeOrNull.getWidth(), sizeOrNull.getHeight());
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

    // Do not rescale if the image is already of the right size.
    // NOTE: this may cause generating thumbnails which are larger than the original.
    // But otherwise features live image overlays could be broken.
    private static boolean isRescalingNeeded(BufferedImage image, Size size)
    {
        return image.getWidth() != size.getWidth() || image.getHeight() != size.getHeight();
    }

    private static BufferedImage loadImage(AbsoluteImageReference imageReference)
    {
        IContent content = imageReference.getContent();
        InputStream inputStream = content.getInputStream();

        // extracts the correct page if necessary
        int page = (imageReference.tryGetPage() != null) ? imageReference.tryGetPage() : 0;

        BufferedImage image = ImageUtil.loadImage(inputStream, page);
        return image;
    }

    /**
     * @param allChannelsMerged if true then we use one special transformation on the merged images
     *            instead of transforming every single image.
     */
    private static BufferedImage mergeChannels(List<AbsoluteImageReference> imageReferences,
            boolean transform, boolean allChannelsMerged)
    {
        if (imageReferences.size() == 1)
        {
            AbsoluteImageReference imageReference = imageReferences.get(0);
            return calculateAndTransformSingleImage(imageReference, transform, allChannelsMerged);
        } else
        {
            List<BufferedImage> images = calculateSingleImages(imageReferences);
            BufferedImage mergedImage = mergeImages(images, imageReferences);
            // NOTE: even if we are not merging all the channels but just few of them we use the
            // merged-channel transformation
            IImageTransformerFactory transformerFactory =
                    transform ? imageReferences.get(0).getTransformerFactoryForMergedChannels()
                            : null;
            return transform(mergedImage, transformerFactory);
        }
    }

    private static BufferedImage transform(BufferedImage input,
            IImageTransformerFactory factoryOrNull)
    {
        if (factoryOrNull == null)
        {
            return input;
        }
        return factoryOrNull.createTransformer().transform(input);
    }

    private static List<BufferedImage> calculateSingleImages(
            List<AbsoluteImageReference> imageReferences)
    {
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        for (AbsoluteImageReference imageRef : imageReferences)
        {
            images.add(calculateSingleImage(imageRef));
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
                if (equals(image.tryGetPage(), lastFound.tryGetPage()) == false
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

    private static boolean equals(Integer i1OrNull, Integer i2OrNull)
    {
        return (i1OrNull == null) ? (i2OrNull == null) : i1OrNull.equals(i2OrNull);
    }

    private static BufferedImage mergeImages(List<BufferedImage> images,
            List<AbsoluteImageReference> imageReferences)
    {
        assert images.size() > 1 : "more than 1 image expected, but found: " + images.size();
        assert images.size() == imageReferences.size() : "images.size() != imageReferences.size()";

        BufferedImage newImage = createNewImage(images.get(0));
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int mergedRGB = mergeRGBColor(images, imageReferences, x, y);
                newImage.setRGB(x, y, mergedRGB);
            }
        }
        return newImage;
    }

    private static void drawOverlay(BufferedImage image, BufferedImage overlayImage)
    {
        int width = Math.min(image.getWidth(), overlayImage.getWidth());
        int height = Math.min(image.getHeight(), overlayImage.getHeight());

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
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
        Color overlayColor = new Color(overlayRGB);

        int r = Math.max(imageColor.getRed(), overlayColor.getRed());
        int g = Math.max(imageColor.getGreen(), overlayColor.getGreen());
        int b = Math.max(imageColor.getBlue(), overlayColor.getBlue());
        return new Color(r, g, b).getRGB();
    }

    private static int mergeRGBColor(List<BufferedImage> images,
            List<AbsoluteImageReference> imageReferences, int x, int y)
    {
        int color[] = new int[]
            { 0, 0, 0 };
        for (int index = 0; index < images.size(); index++)
        {
            int rgb = images.get(index).getRGB(x, y);
            int channelIndex = imageReferences.get(index).getChannelIndex();
            for (int i : getRGBColorIndexes(channelIndex))
            {
                color[i] = Math.max(color[i], extractMaxColorIngredient(rgb));
            }
        }
        return asRGB(color);
    }

    // --------- common

    /**
     * @throw {@link EnvironmentFailureException} when image does not exist
     */
    private static AbsoluteImageReference getImageReference(IImagingDatasetLoader imageAccessor,
            ImageChannelStackReference channelStackReference, String chosenChannelCode,
            Size thumbnailSizeOrNull)
    {
        AbsoluteImageReference image =
                imageAccessor.tryGetImage(chosenChannelCode, channelStackReference,
                        thumbnailSizeOrNull);
        if (image != null)
        {
            return image;
        } else
        {
            throw EnvironmentFailureException.fromTemplate("No "
                    + (thumbnailSizeOrNull != null ? "thumbnail" : "image")
                    + " found for channel stack %s and channel %s", channelStackReference,
                    chosenChannelCode);
        }
    }

    /**
     * Transforms the given <var>bufferedImage</var> by selecting a single channel from it.
     */
    private static BufferedImage transformToChannel(BufferedImage bufferedImage,
            ColorComponent colorComponent)
    {
        BufferedImage newImage = createNewImage(bufferedImage);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int rgb = bufferedImage.getRGB(x, y);
                int channelColor = getGrayscaleAsChannel(rgb, colorComponent);
                newImage.setRGB(x, y, channelColor);
            }
        }
        return newImage;
    }

    private static BufferedImage createNewImage(RenderedImage bufferedImage)
    {
        BufferedImage newImage =
                new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
        return newImage;
    }

    // 0=B, 1=G, 2=R, 3=RG, 4=RB, 5=GB
    private static int[] getRGBColorIndexes(int channelIndex)
    {
        switch (channelIndex % 6)
        {
            case 0:
            case 1:
            case 2:
                return new int[]
                    { 2 - channelIndex };
            case 3:
                return new int[]
                    { 0, 1 };
            case 4:
                return new int[]
                    { 0, 2 };
            case 5:
                return new int[]
                    { 1, 2 };
            default:
                throw new IllegalStateException("not possible");
        }
    }

    // we assume that the color was in a grayscale
    // we reset all ingredients besides the one which should be shown
    private static int getGrayscaleAsChannel(int rgb, ColorComponent colorComponent)
    {
        return colorComponent.extractSingleComponent(rgb).getRGB();
    }

    // returns the max ingredient for the color
    private static int extractMaxColorIngredient(int rgb)
    {
        Color c = new Color(rgb);
        int maxIngredient = Math.max(Math.max(c.getBlue(), c.getGreen()), c.getRed());
        return maxIngredient;
    }

    private static int asRGB(int[] rgb)
    {
        return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
    }

    private static IContent createPngContent(BufferedImage image, String nameOrNull)
    {
        ByteArrayOutputStream output = writeBufferImageAsPng(image);
        return new ByteArrayBasedContent(output.toByteArray(), nameOrNull);
    }

    private static ByteArrayOutputStream writeBufferImageAsPng(BufferedImage image)
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            ImageIO.write(image, "png", output);
        } catch (IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate("Cannot encode image.", ex);
        }
        return output;
    }
}
