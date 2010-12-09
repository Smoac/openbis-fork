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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.io.ByteArrayBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
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

    
    /**
     * Returns content of image for the specified tile in the specified size and for the requested
     * channel or with all channels merged.
     */
    public static IContent getImage(IImagingDatasetLoader imageAccessor, TileImageReference params)
    {
        return getImage(imageAccessor, params, true, true);
    }
    
    private static IContent getImage(IImagingDatasetLoader imageAccessor,
            TileImageReference params, boolean transform, boolean convertToPng)
    {
        List<AbsoluteImageReference> images = getImageReferences(imageAccessor, params);
        if (images.size() > 1)
        {
            return mergeAllChannels(images, transform, convertToPng);
        } else
        {
            AbsoluteImageReference imageReference = images.get(0);
            IImageTransformerFactory transformerFactory =
                    transform ? imageReference.getTransformerFactory() : null;
            return calculateSingleImageContent(imageReference, transformerFactory, convertToPng);
        }
    }

    /**
     * @return an image for the specified tile in the specified size and for the requested channel.
     */
    public static IContent getImage(IImagingDatasetLoader imageAccessor,
            ImageChannelStackReference channelStackReference, String chosenChannelCode,
            Size thumbnailSizeOrNull, boolean convertToPng)
    {
        TileImageReference tileImageReference = new TileImageReference();
        tileImageReference.setChannelStack(channelStackReference);
        tileImageReference.setChannel(chosenChannelCode);
        tileImageReference.setThumbnailSizeOrNull(thumbnailSizeOrNull);
        tileImageReference.setMergeAllChannels(ScreeningConstants.MERGED_CHANNELS.equalsIgnoreCase(chosenChannelCode));
        return getImage(imageAccessor, tileImageReference, false, convertToPng);
    }

    private static List<AbsoluteImageReference> getImageReferences(
            IImagingDatasetLoader imageAccessor, TileImageReference params)
    {
        List<AbsoluteImageReference> images = new ArrayList<AbsoluteImageReference>();

        Size thumbnailSizeOrNull = params.tryGetThumbnailSize();
        if (params.isMergeAllChannels())
        {
            for (String chosenChannel : imageAccessor.getImageParameters().getChannelsCodes())
            {
                AbsoluteImageReference image =
                        getImageReference(imageAccessor, params.getChannelStack(), chosenChannel,
                                thumbnailSizeOrNull);
                images.add(image);
            }
        } else
        {
            AbsoluteImageReference image =
                    getImageReference(imageAccessor, params.getChannelStack(), params.getChannel(),
                            thumbnailSizeOrNull);
            images.add(image);
        }
        return images;
    }

    private static IContent calculateSingleImageContent(AbsoluteImageReference imageReference,
            IImageTransformerFactory transformerFactoryOrNull, boolean convertToPng)
    {
        final IContent content;
        if (convertToPng || imageReference.tryGetColorComponent() != null)
        {
            final BufferedImage image = transform(calculateSingleImage(imageReference), transformerFactoryOrNull);

            long start = operationLog.isDebugEnabled() ? System.currentTimeMillis() : 0;
            content = createPngContent(image, imageReference.getContent().tryGetName());
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("save as png: " + (System.currentTimeMillis() - start));
            }
        } else
        {
            if (transformerFactoryOrNull != null)
            {
                BufferedImage img = transform(loadImage(imageReference), transformerFactoryOrNull);
                content = createPngContent(img, imageReference.getContent().tryGetName());
            } else
            {
                content = imageReference.getContent();
            }
        }

        return content;
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
        if (sizeOrNull != null)
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
                operationLog.debug("Select single channel: " + (System.currentTimeMillis() - start));
            }
        }
        return image;
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

    private static IContent mergeAllChannels(List<AbsoluteImageReference> imageReferences, boolean transform, boolean convertToPng)
    {
        AbsoluteImageReference allChannelsImageReference =
                tryCreateAllChannelsImageReference(imageReferences);
        if (allChannelsImageReference != null)
        {
            // all channels are on an image in one file, no pixel-level operations needed
            IImageTransformerFactory transformerFactory =
                    transform ? allChannelsImageReference.getTransformerFactoryForMergedChannels()
                            : null;
            return calculateSingleImageContent(allChannelsImageReference, transformerFactory,
                    convertToPng);
        } else
        {
            List<BufferedImage> images = calculateSingleImages(imageReferences);
            BufferedImage mergedImage = mergeChannels(images);
            IImageTransformerFactory transformerFactory = transform ? 
                    imageReferences.get(0).getTransformerFactoryForMergedChannels() : null;
            return createPngContent(transform(mergedImage, transformerFactory), null);
        }
    }
    
    private static BufferedImage transform(BufferedImage input, IImageTransformerFactory factoryOrNull)
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

    private static BufferedImage mergeChannels(List<BufferedImage> images)
    {
        assert images.size() > 1 : "more than 1 image expected, but found: " + images.size();
        BufferedImage newImage = createNewImage(images.get(0));
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int mergedRGB = mergeRGBColor(images, x, y);
                newImage.setRGB(x, y, mergedRGB);
            }
        }
        return newImage;
    }

    private static int mergeRGBColor(List<BufferedImage> images, int x, int y)
    {
        int color[] = new int[]
            { 0, 0, 0 };
        // standard merge of first 3 channels
        for (int channel = 1; channel <= Math.min(3, images.size()); channel++)
        {
            int rgb = images.get(channel - 1).getRGB(x, y);
            color[getRGBColorIndex(channel)] = extractChannelColorIngredient(rgb, channel);
        }
        // 4th=RG, 5th=RB, 6th=GB
        for (int channel = 4; channel <= images.size(); channel++)
        {
            int rgb = images.get(channel - 1).getRGB(x, y);
            for (int i : getRGBColorIndexes(channel))
            {
                color[i] = Math.max(color[i], extractMaxColorIngredient(rgb));
            }
        }
        int mergedRGB = asRGB(color);
        return mergedRGB;
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

    private static int getRGBColorIndex(int channel)
    {
        assert channel <= 3 : "to many channels: " + channel;
        return 3 - channel;
    }

    private static int[] getRGBColorIndexes(int channel)
    {
        assert channel <= 6 : "to many channels: " + channel;
        if (channel == 4)
        {
            return new int[]
                { 1, 2 };
        } else if (channel == 5)
        {
            return new int[]
                { 1, 3 };
        } else
        {
            return new int[]
                { 2, 3 };
        }
    }

    // we assume that the color was in a grayscale
    // we reset all ingredients besides the one which should be shown
    private static int getGrayscaleAsChannel(int rgb, ColorComponent colorComponent)
    {
        return colorComponent.extractSingleComponent(rgb).getRGB();
    }

    // returns the ingredient for the specified channel
    private static int extractChannelColorIngredient(int rgb, int channelNumber)
    {
        Color c = new Color(rgb);
        int channelColors[] = new int[]
            { c.getBlue(), c.getGreen(), c.getRed() };
        return channelColors[channelNumber - 1];
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
