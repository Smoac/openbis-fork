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

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImages;

/**
 * Images of one well.
 * 
 * @author Tomasz Pylak
 */
public class WellImages
{
    private String[/* channel */][/* tile number */] imagePaths;

    private final int tileRowsNum;

    private final int tileColsNum;

    private final int channelsNum;

    private final String downloadUrl;

    public WellImages(PlateImageParameters imageParams, String downloadUrl)
    {
        int tilesNum = imageParams.getTileRowsNum() * imageParams.getTileColsNum();
        this.tileRowsNum = imageParams.getTileRowsNum();
        this.tileColsNum = imageParams.getTileColsNum();
        this.channelsNum = imageParams.getChannelsNum();
        this.downloadUrl = downloadUrl;
        this.imagePaths = new String[imageParams.getChannelsNum()][tilesNum];
    }

    public WellImages(TileImages images)
    {
        this(images.getImageParameters(), images.getDownloadUrl());
        for (TileImage image : images.getImages())
        {
            addImage(image);
        }
    }

    public void addImage(TileImage image)
    {
        int channelIx = image.getChannel() - 1;
        int tileIx = image.getTile() - 1;
        assert imagePaths != null && imagePaths[channelIx][tileIx] == null : "duplicated image for channelIx = "
                + channelIx + ", tileIx = " + tileIx;
        imagePaths[channelIx][tileIx] = image.getImagePath();
    }

    public String tryGetImagePath(int channel, int tile)
    {
        return imagePaths[channel - 1][tile - 1];
    }

    public int getTileRowsNum()
    {
        return tileRowsNum;
    }

    public int getTileColsNum()
    {
        return tileColsNum;
    }

    public int getChannelsNum()
    {
        return channelsNum;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }
}
