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


import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;

/**
 * Transformation performed by
 * {@link IntensityRescaling#rescaleIntensityLevelTo8Bits(BufferedImage, Levels)}.
 * <p>
 * Warning: The serialized version of this class can be stored in the database for each image.
 * Moving this class to a different package or changing it in a backward incompatible way would make
 * all the saved transformations invalid.
 * 
 * @author Tomasz Pylak
 */
@JsonObject("IntensityRangeImageTransformerFactory")
public class IntensityRangeImageTransformerFactory implements IImageTransformerFactory
{
    private static final long serialVersionUID = 1L;

    private final int blackPointIntensity;

    private final int whitePointIntensity;

    public IntensityRangeImageTransformerFactory(int blackPointIntensity, int whitePointIntensity)
    {
        this.blackPointIntensity = blackPointIntensity;
        this.whitePointIntensity = whitePointIntensity;
    }

    public int getBlackPointIntensity()
    {
        return blackPointIntensity;
    }

    public int getWhitePointIntensity()
    {
        return whitePointIntensity;
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
                        return image;
                    }
                    Levels levels = new Levels(blackPointIntensity, whitePointIntensity);
                    return IntensityRescaling.rescaleIntensityLevelTo8Bits(image, levels);
                }
            };
    }

}
