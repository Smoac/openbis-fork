/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.etl.IContentRepository;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageEnrichedDTO;
import ch.systemsx.sybit.imageviewer.data.ImageViewerParameters;
import ch.systemsx.sybit.imageviewer.data.MinMaxInChannel;
import ch.systemsx.sybit.imageviewer.transformers.IJImageTransformerFactory;

/**
 * @author Tomasz Pylak
 */
public class WellImageIntensityTransformerProcessingPlugin extends
        AbstractSpotImagesTransformerProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    // Colors which are smaller or larger will be ignored when the rescaling parameters are
    // calculated.
    // If the encountered minimum and maximum is equal to the spcified, no rescaling happens.
    private final int minRescaledColor, maxRescaledColor;

    public WellImageIntensityTransformerProcessingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        this.minRescaledColor = PropertyUtils.getInt(properties, "min-color", 2);
        this.maxRescaledColor = PropertyUtils.getInt(properties, "max-color", 16000);
    }

    private static class ImagePixelsRange
    {
        private final int min, max;

        public ImagePixelsRange(int min, int max)
        {
            this.min = min;
            this.max = max;
        }

        public int getMin()
        {
            return min;
        }

        public int getMax()
        {
            return max;
        }

        public ImagePixelsRange createOverlap(ImagePixelsRange rangeOrNull)
        {
            if (rangeOrNull == null)
            {
                return this;
            }
            int newMin = Math.min(min, rangeOrNull.getMin());
            int newMax = Math.max(max, rangeOrNull.getMax());
            return new ImagePixelsRange(newMin, newMax);
        }

        @Override
        public String toString()
        {
            return "range [" + min + ", " + max + "]";
        }
    }

    @Override
    protected IImageTransformerFactory tryCalculateTransformation(
            List<ImgImageEnrichedDTO> spotImages, IContentRepository contentRepository)
    {
        if (spotImages.size() < 2)
        {
            return null;
        }
        ImagePixelsRange range = null;
        int i = 1;
        for (ImgImageEnrichedDTO image : spotImages)
        {
            IContent content = contentRepository.getContent(image.getFilePath());
            BufferedImage bufferedImage = ImageUtil.loadImage(content, image.getPage());
            ImagePixelsRange imageRange = calculatePixelsRange(bufferedImage);

            // debug
            System.out.println(imageRange + " - " + i++ + " - " + image.getFilePath());

            range = imageRange.createOverlap(range);
        }
        assert range != null;
        // debug
        System.out.println("Final range: " + range);
        return tryCreateRescaleTransformationFactory(range);
    }

    private IImageTransformerFactory tryCreateRescaleTransformationFactory(ImagePixelsRange range)
    {
        ImageViewerParameters params = new ImageViewerParameters();
        params.setMin(range.getMin());
        params.setMax(range.getMax());
        params.setLutOperation("");
        params.setMinMaxInChannels(new HashMap<Integer, MinMaxInChannel>());
        params.setSlice(1);
        return new IJImageTransformerFactory(params);
    }

    private ImagePixelsRange calculatePixelsRange(BufferedImage bufferedImage)
    {
        int bucketSize = 2000;
        int buckets[] = new int[17000 / bucketSize + 1];
        long sum = 0;
        System.out.println("component bits: " + bufferedImage.getColorModel().getComponentSize(0));
        System.out.println("number of components: "
                + bufferedImage.getColorModel().getNumColorComponents());

        int minColor = maxRescaledColor;
        int maxColor = minRescaledColor;
        // first pass - calsulate min and max color
        for (int x = 0; x < bufferedImage.getWidth(); x++)
        {
            for (int y = 0; y < bufferedImage.getHeight(); y++)
            {
                int dominantColorComponent = getDominantColorComponent(bufferedImage, x, y);
                buckets[dominantColorComponent / bucketSize]++;
                sum += dominantColorComponent;

                if (dominantColorComponent >= minRescaledColor
                        && dominantColorComponent <= maxRescaledColor)
                {
                    if (dominantColorComponent > maxColor)
                    {
                        maxColor = dominantColorComponent;
                    } else if (dominantColorComponent < minColor)
                    {
                        minColor = dominantColorComponent;
                    }
                }
            }
        }
        int max = 0;
        for (int i = 0; i < buckets.length; i++)
        {
            if (buckets[i] > buckets[max])
            {
                max = i;
            }
            System.out.println("bucket " + i + ": " + buckets[i]);
        }
        System.out.println("Max bucket " + max + ": " + buckets[max] + ".\t Sum: " + sum);
        return new ImagePixelsRange(minColor, maxColor);
    }

    // private static int getDominantColorComponent(BufferedImage bufferedImage, int x, int y)
    // {
    // int rgb = bufferedImage.getRGB(x, y);
    // Color color = new Color(rgb);
    // int max = Math.max(color.getRed(), color.getBlue());
    // return Math.max(max, color.getGreen());
    // }

    private static int getDominantColorComponent(BufferedImage bufferedImage, int x, int y)
    {
        return bufferedImage.getRaster().getSample(x, y, 0);
    }

}
