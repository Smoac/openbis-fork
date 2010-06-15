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

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IHCSDatasetLoader;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ColorComponent;
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
     * @return file with the image for the chosen channel or images for all channels if they should
     *         be merged.
     * @throw {@link EnvironmentFailureException} when image does not exist
     */
    public static List<AbsoluteImageReference> getImagePaths(File datasetRoot, String datasetCode,
            TileImageReference params)
    {
        IHCSDatasetLoader imageAccessor = HCSDatasetLoaderFactory.create(datasetRoot, datasetCode);
        Location wellLocation = params.getWellLocation();
        Location tileLocation = params.getTileLocation();
        List<AbsoluteImageReference> images = new ArrayList<AbsoluteImageReference>();

        Size thumbnailSizeOrNull = params.tryGetThumbnailSize();
        if (params.isMergeAllChannels())
        {
            for (String chosenChannel : imageAccessor.getChannelsNames())
            {
                AbsoluteImageReference image =
                        getImage(imageAccessor, wellLocation, tileLocation, chosenChannel,
                                thumbnailSizeOrNull);
                images.add(image);
            }
        } else
        {
            AbsoluteImageReference image =
                    getImage(imageAccessor, wellLocation, tileLocation, params.getChannel(),
                            thumbnailSizeOrNull);
            images.add(image);
        }
        imageAccessor.close();

        return images;
    }

    /**
     * Creates an images of the specified tile and with requested channels merged.<br>
     */
    public static BufferedImage mergeImageChannels(TileImageReference params,
            List<AbsoluteImageReference> imageReferences)
    {
        BufferedImage resultImage;
        if (params.isMergeAllChannels())
        {
            resultImage = mergeAllChannels(imageReferences);
        } else
        {
            assert imageReferences.size() == 1 : "single channel image can be generated only form one file, "
                    + "but more have been specified: " + imageReferences;
            AbsoluteImageReference imageReference = imageReferences.get(0);

            resultImage = selectSingleChannel(params, imageReference);
        }
        return resultImage;
    }

    private static BufferedImage selectSingleChannel(TileImageReference params,
            AbsoluteImageReference imageReference)
    {
        BufferedImage image = ImageUtil.loadImage(imageReference.getContent().getInputStream());
        ColorComponent colorComponent = imageReference.tryGetColorComponent();
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
                if (imageFile.equals(mergedChannelsImage) == false)
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

    // NOTE: we handle only 3 channels until we know that more channels can be used and
    // what kind of color manipulation makes sense
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

    private static List<BufferedImage> loadImages(List<IContent> imageFiles)
    {
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        for (IContent imageFile : imageFiles)
        {
            BufferedImage image = ImageUtil.loadImage(imageFile.getInputStream());
            images.add(image);
        }
        return images;
    }

    // --------- common

    /**
     * @param chosenChannel starts from 1
     * @throw {@link EnvironmentFailureException} when image does not exist
     */
    public static AbsoluteImageReference getImage(IHCSDatasetLoader imageAccessor,
            Location wellLocation, Location tileLocation, String chosenChannel,
            Size thumbnailSizeOrNull)
    {
        AbsoluteImageReference image =
                imageAccessor.tryGetImage(chosenChannel, wellLocation, tileLocation,
                        thumbnailSizeOrNull);
        if (image != null)
        {
            return image;
        } else
        {
            throw EnvironmentFailureException.fromTemplate("No "
                    + (thumbnailSizeOrNull != null ? "thumbnail" : "image")
                    + " found for well %s, tile %s and channel %d", wellLocation, tileLocation,
                    chosenChannel);
        }
    }

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

    private static int asRGB(int[] rgb)
    {
        return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
    }
}
