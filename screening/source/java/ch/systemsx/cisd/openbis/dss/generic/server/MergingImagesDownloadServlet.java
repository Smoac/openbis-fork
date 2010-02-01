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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.bds.hcs.HCSDatasetLoader;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Allows to download screening images in a chosen size for a specified channels or with all
 * channels merged.<br>
 * Assumes that originally there is one image for each channel and no image with all the channels
 * merged exist.
 * 
 * @author Tomasz Pylak
 */
public class MergingImagesDownloadServlet extends AbstractImagesDownloadServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected final ResponseContentStream createImageResponse(RequestParams params, File datasetRoot)
            throws IOException, EnvironmentFailureException /* if image does not exist */
    {
        List<File> imageFiles = getImagePaths(datasetRoot, params);
        BufferedImage image = mergeImages(imageFiles, params);
        File singleFileOrNull = imageFiles.size() == 1 ? imageFiles.get(0) : null;
        return createResponseContentStream(image, singleFileOrNull);
    }

    /** throws {@link EnvironmentFailureException} when image does not exist */
    private static List<File> getImagePaths(File datasetRoot, RequestParams params)
    {
        HCSDatasetLoader imageAccessor = new HCSDatasetLoader(datasetRoot);
        List<File> paths = new ArrayList<File>();

        if (params.isMergeAllChannels())
        {
            for (int chosenChannel = 1; chosenChannel <= params.getChannel(); chosenChannel++)
            {
                File path = getPath(imageAccessor, params, chosenChannel);
                paths.add(path);
            }
        } else
        {
            File path = getPath(imageAccessor, params, params.getChannel());
            paths.add(path);
        }
        imageAccessor.close();

        return paths;
    }

    private static BufferedImage mergeImages(List<File> imageFiles, RequestParams params)
    {
        List<BufferedImage> images = loadImages(imageFiles, params);

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

    private static List<BufferedImage> loadImages(List<File> imageFiles, RequestParams params)
    {
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        for (File imageFile : imageFiles)
        {
            BufferedImage image = ImageUtil.loadImage(imageFile);
            images.add(image);
        }
        return images;
    }

}
