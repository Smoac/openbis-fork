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
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IHCSImageDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDatasetDownloadServlet.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Utility classes to create an image of a specified size containing one channel or a subset of all
 * channels.
 * 
 * @author Tomasz Pylak
 */
public class ImageChannelsUtils
{

    /**
     * @return file with the image for the chosen channel or images for all channels if they should
     *         be merged.
     * @throw {@link EnvironmentFailureException} when image does not exist
     */
    public static List<AbsoluteImageReference> getImagePaths(File datasetRoot, String datasetCode,
            TileImageReference params)
    {
        IHCSImageDatasetLoader imageAccessor =
                HCSImageDatasetLoaderFactory.create(datasetRoot, datasetCode);
        try
        {
            List<AbsoluteImageReference> images = new ArrayList<AbsoluteImageReference>();

            Size thumbnailSizeOrNull = params.tryGetThumbnailSize();
            if (params.isMergeAllChannels())
            {
                for (String chosenChannel : imageAccessor.getImageParameters().getChannelsCodes())
                {
                    AbsoluteImageReference image =
                            getImage(imageAccessor, params.getChannelStack(), chosenChannel,
                                    thumbnailSizeOrNull);
                    images.add(image);
                }
            } else
            {
                AbsoluteImageReference image =
                        getImage(imageAccessor, params.getChannelStack(), params.getChannel(),
                                thumbnailSizeOrNull);
                images.add(image);
            }
            return images;
        } finally
        {
            imageAccessor.close();
        }
    }

    /**
     * Creates an images of the specified tile and with requested channels merged.<br>
     */
    public static BufferedImage mergeImageChannels(TileImageReference params,
            List<AbsoluteImageReference> imageReferences)
    {
        return mergeImageChannels(imageReferences, params.isMergeAllChannels());
    }

    /**
     * Creates an images of the specified tile and with requested channels merged.<br>
     */
    public static BufferedImage mergeImageChannels(List<AbsoluteImageReference> imageReferences,
            boolean mergeChannels)
    {
        BufferedImage resultImage;
        if (mergeChannels)
        {
            resultImage = mergeAllChannels(imageReferences);
        } else
        {
            assert imageReferences.size() == 1 : "single channel image can be generated only form one file, "
                    + "but more have been specified: " + imageReferences;
            AbsoluteImageReference imageReference = imageReferences.get(0);

            resultImage = selectSingleChannel(imageReference);
        }
        return resultImage;
    }

    /**
     * Reads the given content and selects a single channel from it.
     */
    public static BufferedImage selectSingleChannel(AbsoluteImageReference imageReference)
    {
        return selectSingleChannel(imageReference.getContent().getInputStream(), imageReference
                .tryGetColorComponent());
    }

    /**
     * Reads the given content and selects a single channel from it.
     */
    public static BufferedImage selectSingleChannel(InputStream input, ColorComponent colorComponent)
    {
        BufferedImage image = ImageUtil.loadImage(input);
        if (colorComponent == null)
        {
            // TODO 2010-06-15 Izabela Adamczyk: We have to select a single channel from the (most
            // possibly) grayscale image.
            // This image contains only one channel, but can have R, G, and B components set.
            // We select just one to make the image colorful.
            return image;
        }
        return transformToChannel(image, colorComponent);
    }

    private static BufferedImage mergeAllChannels(List<AbsoluteImageReference> imageReferences)
    {
        IContent mergedChannelsImage = tryAsOneImageWithAllChannels(imageReferences);
        if (mergedChannelsImage != null)
        {
            // all channels are on an image in one file, no pixel-level operations needed
            return ImageUtil.loadImage(mergedChannelsImage.getInputStream());
        } else
        {
            List<IContent> plainImages = tryAsPlainImages(imageReferences);
            if (plainImages == null)
            {
                throw EnvironmentFailureException.fromTemplate(
                        "Merging channels in a list of different files is not supported:  %s",
                        imageReferences);
            }
            List<BufferedImage> images = loadImages(plainImages);
            return mergeChannels(images);
        }
    }

    private static List<IContent> tryAsPlainImages(List<AbsoluteImageReference> images)
    {
        List<IContent> plainFiles = new ArrayList<IContent>();
        for (AbsoluteImageReference image : images)
        {
            if (image.tryGetColorComponent() != null || image.tryGetPage() != null)
            {
                return null;
            }
            plainFiles.add(image.getContent());
        }
        return plainFiles;
    }

    private static IContent tryAsOneImageWithAllChannels(
            List<AbsoluteImageReference> imageReferences)
    {
        IContent mergedChannelsImage = null;
        for (AbsoluteImageReference image : imageReferences)
        {
            IContent imageFile = image.getContent();
            if (mergedChannelsImage == null)
            {
                mergedChannelsImage = imageFile;
            } else
            {
                if (imageFile.getUniqueId().equals(mergedChannelsImage.getUniqueId()) == false)
                {
                    return null;
                }
            }
        }
        return mergedChannelsImage;
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

    private static List<BufferedImage> loadImages(List<IContent> imageFiles)
    {
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        for (IContent imageFile : imageFiles)
        {
            BufferedImage image = ImageUtil.loadImage(imageFile.getInputStream());
            assert image != null : "image is null";
            images.add(image);
        }
        return images;
    }

    // --------- common

    /**
     * @param chosenChannelCode starts from 1
     * @throw {@link EnvironmentFailureException} when image does not exist
     */
    public static AbsoluteImageReference getImage(IHCSImageDatasetLoader imageAccessor,
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
     * Transforms the given <var>bufferedImage</var> as
     */
    public static BufferedImage transformToChannel(BufferedImage bufferedImage,
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
}
