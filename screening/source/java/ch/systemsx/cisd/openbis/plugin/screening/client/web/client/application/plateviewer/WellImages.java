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
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Images of one well.
 * 
 * @author Tomasz Pylak
 */
public class WellImages
{
    private final int tileRowsNum;

    private final int tileColsNum;

    private final int channelsNum;

    private final String datasetCode;

    private final String downloadUrl;

    private final WellLocation location;

    public WellImages(PlateImageParameters imageParams, String downloadUrl, WellLocation location)
    {
        this.tileRowsNum = imageParams.getTileRowsNum();
        this.tileColsNum = imageParams.getTileColsNum();
        this.channelsNum = imageParams.getChannelsNum();
        this.datasetCode = imageParams.getDatasetCode();
        this.downloadUrl = downloadUrl;
        this.location = location;
    }

    public WellImages(TileImages images, WellLocation location)
    {
        this(images.getImageParameters(), images.getDownloadUrl(), location);
    }

    public String getImagePath(int channel, int tileRow, int tileCol)
    {
        return datasetCode + "/data/standard/channel" + channel + "/row" + location.getRow()
                + "/column" + location.getColumn() + "/row" + tileRow + "_column" + tileCol
                + ".tiff";
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
