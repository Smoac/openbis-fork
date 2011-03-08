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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.etl.IContentRepository;
import ch.systemsx.cisd.openbis.dss.etl.dynamix.IntensityRangeReductionFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageEnrichedDTO;

/**
 * Rescales colors so that all images of one well have comparable brightness. Used by DynamiX
 * project.
 * <p>
 * Currently openBIS converts 16 bit grayscale images to 8 bit. This class has to revert this
 * conversion and apply the correct one (taking all images of a well into account). We cannot avoid
 * double convertions before openBIS supports 16bit images better.
 * <p>
 * Limitations:<br>
 * 1) works on the first color component of the images (perfect for e.g. grayscale images).<br>
 * 2) Can process only tiff images which can be read by JAI library.
 * 
 * @author Tomasz Pylak
 */
public class DynamixWellBrightnessEqualizerProcessingPlugin extends
        AbstractSpotImagesTransformerProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    // Colors which are smaller or larger will be ignored when the rescaling parameters are
    // calculated.
    // If the encountered minimum and maximum is equal to the spcified, no rescaling happens.
    private final int minRescaledColor, maxRescaledColor;

    public DynamixWellBrightnessEqualizerProcessingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        this.minRescaledColor = PropertyUtils.getInt(properties, "min-color", 1);
        this.maxRescaledColor = PropertyUtils.getInt(properties, "max-color", 16384 - 1);
    }

    /** Describes the minimal and maximal brightness of the pixel on the image. */
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
            return "[" + min + ", " + max + "]";
        }
    }

    @Override
    protected IImageTransformerFactoryProvider getTransformationProvider(
            List<ImgImageEnrichedDTO> spotImages, IContentRepository contentRepository)
    {
        if (spotImages.size() < 2)
        {
            return NO_TRANSFORMATION_PROVIDER;
        }
        final Map<ImgImageEnrichedDTO, ImagePixelsRange> rangeMap =
                new HashMap<ImgImageEnrichedDTO, ImagePixelsRange>();
        for (ImgImageEnrichedDTO image : spotImages)
        {
            BufferedImage bufferedImage = loadImage(contentRepository, image);
            ImagePixelsRange imageRange = calculatePixelsRange(bufferedImage);
            rangeMap.put(image, imageRange);
        }
        return createImageTransformerFactoryProvider(rangeMap);
    }

    private static ImagePixelsRange calculateOverlapRange(Collection<ImagePixelsRange> ranges)
    {
        ImagePixelsRange globalRange = null;
        for (ImagePixelsRange imageRange : ranges)
        {
            globalRange = imageRange.createOverlap(globalRange);
        }
        return globalRange;
    }

    private ImagePixelsRange calculatePixelsRange(BufferedImage bufferedImage)
    {
        int minColor = maxRescaledColor;
        int maxColor = minRescaledColor;
        for (int x = 0; x < bufferedImage.getWidth(); x++)
        {
            for (int y = 0; y < bufferedImage.getHeight(); y++)
            {
                int dominantColorComponent = bufferedImage.getRaster().getSample(x, y, 0);
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
        return new ImagePixelsRange(minColor, maxColor);
    }

    // --- DynamiX specific part ----------------

    private static BufferedImage loadImage(IContentRepository contentRepository,
            ImgImageEnrichedDTO image)
    {
        IContent content = contentRepository.getContent(image.getFilePath());
        return ImageUtil.loadJavaAdvancedImagingTiff(content.getInputStream(), image.getPage(),
                true);
    }

    private static IImageTransformerFactoryProvider createImageTransformerFactoryProvider(
            final Map<ImgImageEnrichedDTO, ImagePixelsRange> rangeMap)
    {
        final ImagePixelsRange globalRange = calculateOverlapRange(rangeMap.values());
        return new IImageTransformerFactoryProvider()
            {
                public IImageTransformerFactory tryGetTransformationFactory(
                        ImgImageEnrichedDTO image)
                {
                    ImagePixelsRange imageRange = rangeMap.get(image);
                    ImagePixelsRange rescaledRange = rescaleRange(imageRange, globalRange);
                    operationLog.info(image.getFilePath() + " global: " + globalRange + ", local: "
                            + imageRange + ", rescaled: " + rescaledRange);
                    return new IntensityRangeReductionFactory(rescaledRange.getMin(),
                            rescaledRange.getMax());
                }
            };
    }

    /**
     * We should have converted 'globalRange' to [0,255] but instead a smaller range 'imageRange'
     * has been converted to [0,255]. We have to squeeze 'imageRange' to a smaller range [a,b] (a >=
     * 0, b <= 255) into which it would be mapped if globalRange would be used at the beginning.
     */
    private static ImagePixelsRange rescaleRange(ImagePixelsRange imageRange,
            ImagePixelsRange globalRange)
    {
        double globalRangeLength = globalRange.getMax() - globalRange.getMin();
        int min = (int) (255 * (imageRange.getMin() - globalRange.getMin()) / globalRangeLength);
        int max = (int) (255 * (imageRange.getMax() - globalRange.getMin()) / globalRangeLength);
        return new ImagePixelsRange(min, max);
    }

}
