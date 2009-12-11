/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.plateviewer;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Stores information about images and metadata of one well.
 * 
 * @author Tomasz Pylak
 */
class WellData
{
    private String[/* channel */][/* tile number */] imagePaths;

    private WellMetadata metadata;

    public WellData(PlateContent plateContent)
    {
        PlateImages images = plateContent.tryGetImages();
        if (images != null)
        {
            this.imagePaths = initImagePathArray(images.getImageParameters());
        } else
        {
            this.imagePaths = null;
        }
    }

    private String[][] initImagePathArray(PlateImageParameters params)
    {
        int tilesNum = params.getTileRowsNum() * params.getTileColsNum();
        return new String[params.getChannelsNum()][tilesNum];
    }

    public void addImage(PlateImage image)
    {
        int channelIx = image.getChannel() - 1;
        int tileIx = image.getTile() - 1;
        assert imagePaths != null && imagePaths[channelIx][tileIx] == null : "duplicated image for channelIx = "
                + channelIx + ", tileIx = " + tileIx;
        imagePaths[channelIx][tileIx] = image.getImagePath();
    }

    public void setMetadata(WellMetadata well)
    {
        this.metadata = well;
    }

    public String tryGetImagePath(int channel, int tile)
    {
        return imagePaths != null ? imagePaths[channel - 1][tile - 1] : null;
    }

    public WellMetadata tryGetMetadata()
    {
        return metadata;
    }

    public String getWellContentDescription()
    {
        if (metadata != null)
        {
            return metadata.getWellSample().getSubCode();
        } else
        {
            return "?";
        }
    }

    public String getWellSubcode()
    {
        if (metadata != null)
        {
            return metadata.getWellSample().getSubCode();
        } else
        {
            return "?";
        }
    }
}