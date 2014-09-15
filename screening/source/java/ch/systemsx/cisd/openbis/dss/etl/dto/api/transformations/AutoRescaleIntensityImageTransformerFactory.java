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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.image.IntensityRescaling.Channel;
import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Transformation performed by
 * {@link IntensityRescaling#rescaleIntensityLevelTo8Bits(BufferedImage, Levels)} where levels are
 * computed automatically by {@link IntensityRescaling#computeLevels(BufferedImage, float)}.
 * <p>
 * Warning: The serialized version of this class can be stored in the database for each image.
 * Moving this class to a different package or changing it in a backward incompatible way would make
 * all the saved transformations invalid.
 * 
 * @author Tomasz Pylak
 */
@JsonObject("AutoRescaleIntensityImageTransformerFactory")
public class AutoRescaleIntensityImageTransformerFactory implements IImageTransformerFactory
{
    private static final long serialVersionUID = 1L;

    private final float threshold;

    public AutoRescaleIntensityImageTransformerFactory(float threshold)
    {
        this.threshold = threshold;
    }

    @Override
    public IImageTransformer createTransformer()
    {
        return new IImageTransformer()
            {
                @Override
                public BufferedImage transform(BufferedImage image)
                {
                    if (IntensityRescaling.isNotGrayscale(image))
                    {
                        Channel channel = ImageUtil.getRepresentativeChannelIfEffectiveGray(image);
                        if (channel == null)
                        {
                            return image;
                        }
                        BufferedImage grayScaleImage = toGrayScale(image, channel);
                        Levels levels = IntensityRescaling.computeLevels(grayScaleImage, threshold);
                        return IntensityRescaling.rescaleIntensityLevelTo8Bits(image, levels);
                    }
                    Levels levels = IntensityRescaling.computeLevels(image, threshold);
                    return IntensityRescaling.rescaleIntensityLevelTo8Bits(new IntensityRescaling.GrayscalePixels(image), levels);
                }
            };
    }

    private BufferedImage toGrayScale(BufferedImage image, Channel channel)
    {
        BufferedImage gray =
                new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = gray.getRaster();

        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                int value = (image.getRGB(x, y) >> channel.getShift()) & 0xff;
                raster.setPixel(x, y, new int[]
                    { value });
            }
        }
        return gray;
    }
}
