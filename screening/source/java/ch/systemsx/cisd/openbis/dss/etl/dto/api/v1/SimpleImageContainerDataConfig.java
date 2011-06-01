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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import java.util.List;

/**
 * Subclass of {@link SimpleImageDataConfig} for container image files which creates
 * {@link ImageMetadata} based on {@link ImageIdentifier} using a simple mapping.
 *
 * @author Franz-Josef Elmer
 */
public class SimpleImageContainerDataConfig extends SimpleImageDataConfig
{

    @Override
    public ImageMetadata[] extractImageMetadata(String imagePath,
            List<ImageIdentifier> imageIdentifiers)
    {
        ImageMetadata[] metaData = new ImageMetadata[imageIdentifiers.size()];
        for (int i = 0, n = imageIdentifiers.size(); i < n; i++)
        {
            ImageIdentifier imageIdentifier = imageIdentifiers.get(i);
            ImageMetadata imageMetadata = new ImageMetadata();
            imageMetadata.setImageIdentifier(imageIdentifier);
            imageMetadata.setChannelCode("CHANNEL-" + (imageIdentifier.getColorChannelIndex() + 1));
            imageMetadata.setDepth(new Float(imageIdentifier.getFocalPlaneIndex()));
            imageMetadata.setTimepoint(new Float(imageIdentifier.getTimeSeriesIndex()));
            imageMetadata.setSeriesNumber(imageIdentifier.getSeriesIndex());
            imageMetadata.setTileNumber(i + 1);
            imageMetadata.setWell("A1");
            metaData[i] = imageMetadata;
        }
        return metaData;
    }

}
