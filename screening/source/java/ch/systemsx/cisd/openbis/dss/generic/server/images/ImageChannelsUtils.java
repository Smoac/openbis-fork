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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.bds.hcs.HCSDatasetLoader;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDatasetDownloadServlet.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Utility classes to create an image of a specified size containing one channel or a subset of all
 * channels.
 * 
 * @author Tomasz Pylak
 */
public class ImageChannelsUtils
{

    /**
     * Creates an images of the specified tile and with requested channels merged.<br>
     * Assumes that originally all channels are merged on one image.
     */
    public static BufferedImage mergeImageChannels(TileImageReference params, File imageFile)
    {
        BufferedImage image = ImageUtil.loadImage(imageFile);
        if (params.isMergeAllChannels() == false)
        {
            image = transformToChannel(image, params.getChannel());
        }
        image = asThumbnailIfRequested(params, image);
        return image;
    }

    /**
     * @return file with the image for the chosen channel or images for all channels if they should
     *         be merged.
     * @throw {@link EnvironmentFailureException} when image does not exist
     */
    public static List<File> getImagePaths(File datasetRoot, TileImageReference params)
    {
        HCSDatasetLoader imageAccessor = new HCSDatasetLoader(datasetRoot);
        Location wellLocation = params.getWellLocation();
        Location tileLocation = params.getTileLocation();
        List<File> paths = new ArrayList<File>();

        if (params.isMergeAllChannels())
        {
            for (int chosenChannel = 1; chosenChannel <= params.getChannel(); chosenChannel++)
            {
                File path = getImagePath(imageAccessor, wellLocation, tileLocation, chosenChannel);
                paths.add(path);
            }
        } else
        {
            File path =
                    getImagePath(imageAccessor, wellLocation, tileLocation, params.getChannel());
            paths.add(path);
        }
        imageAccessor.close();

        return paths;
    }

    /**
     * Creates an images of the specified tile and with requested channels merged.<br>
     * Assumes that different channels are on separate images.
     */
    public static BufferedImage mergeImageChannels(TileImageReference params, List<File> imageFiles)
    {
        List<BufferedImage> images = loadImages(imageFiles);

        BufferedImage resultImage;
        if (images.size() == 1)
        {
            resultImage = transformToChannel(images.get(0), params.getChannel());
        } else
        {
            resultImage = mergeChannels(images);
        }
        resultImage = asThumbnailIfRequested(params, resultImage);
        return resultImage;
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

    // NOTE: we handle only 3 channels until we know that more channels can be used and
    // what
    // kind of color manipulation makes sense
    private static int mergeRGBColor(List<BufferedImage> images, int x, int y)
    {
        int color[] = new int[]
            { 0, 0, 0 };
        for (int channel = 1; channel <= Math.min(3, images.size()); channel++)
        {
            int rgb = images.get(channel - 1).getRGB(x, y);
            color[getRGBColorIndex(channel)] = extractChannelColorIngredient(rgb, channel);
        }
        int mergedRGB = asRGB(color);
        return mergedRGB;
    }

    private static List<BufferedImage> loadImages(List<File> imageFiles)
    {
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        for (File imageFile : imageFiles)
        {
            BufferedImage image = ImageUtil.loadImage(imageFile);
            images.add(image);
        }
        return images;
    }

    // --------- common

    /**
     * @param chosenChannel starts from 1
     * @throw {@link EnvironmentFailureException} when image does not exist
     */
    public static File getImagePath(HCSDatasetLoader imageAccessor, Location wellLocation,
            Location tileLocation, int chosenChannel)
    {
        String imagePath =
                imageAccessor.tryGetStandardNodeAt(chosenChannel, wellLocation, tileLocation);
        if (imagePath != null)
        {
            return new File(imagePath);
        } else
        {
            throw EnvironmentFailureException.fromTemplate(
                    "No image found for well %s, tile %s and channel %d", wellLocation,
                    tileLocation, chosenChannel);
        }
    }

    private static BufferedImage asThumbnailIfRequested(TileImageReference params,
            BufferedImage image)
    {
        Size thumbnailSizeOrNull = params.tryGetThumbnailSize();
        if (thumbnailSizeOrNull != null)
        {
            int width = thumbnailSizeOrNull.getWidth();
            int height = thumbnailSizeOrNull.getHeight();
            return ImageUtil.createThumbnail(image, width, height);
        } else
        {
            return image;
        }
    }

    private static BufferedImage transformToChannel(BufferedImage bufferedImage, int channelNumber)
    {
        BufferedImage newImage = createNewImage(bufferedImage);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int rgb = bufferedImage.getRGB(x, y);
                int channelColor = getGrayscaleAsChannel(rgb, channelNumber);
                newImage.setRGB(x, y, channelColor);
            }
        }
        return newImage;
    }

    private static BufferedImage createNewImage(BufferedImage bufferedImage)
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

    private static int getGrayscaleAsChannel(int rgb, int channelNumber)
    {
        // NOTE: we handle only 3 channels until we know that more channels can be used and what
        // kind of color manipulation makes sense
        if (channelNumber <= 3)
        {
            // we assume that the color was in a grayscale
            // we reset all ingredients besides the one which should be shown
            int newColor[] = new int[]
                { 0, 0, 0 };
            newColor[getRGBColorIndex(channelNumber)] =
                    extractChannelColorIngredient(rgb, channelNumber);
            return asRGB(newColor);
        } else
        {
            return rgb;
        }
    }

    // returns the ingredient for the specified channel
    private static int extractChannelColorIngredient(int rgb, int channelNumber)
    {
        Color c = new Color(rgb);
        int channelColors[] = new int[]
            { c.getBlue(), c.getGreen(), c.getRed() };
        return channelColors[channelNumber - 1];
    }

    private static int asRGB(int[] rgb)
    {
        return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
    }
}
