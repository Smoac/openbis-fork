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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
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

    private final List<String> channelsNames;

    private final String datasetCode;

    private final String datastoreCode;

    private final String downloadUrl;

    private final WellLocation location;

    // has timepoints or depth stack?
    private final boolean isMultidimensional;

    public WellImages(PlateImageParameters imageParams, String datastoreCode, String downloadUrl,
            WellLocation location)
    {
        this.tileRowsNum = imageParams.getTileRowsNum();
        this.tileColsNum = imageParams.getTileColsNum();
        this.channelsNames = imageParams.getChannelsNames();
        this.isMultidimensional = imageParams.isMultidimensional();
        this.datasetCode = imageParams.getDatasetCode();
        this.datastoreCode = datastoreCode;
        this.downloadUrl = downloadUrl;
        this.location = location;
    }

    public WellImages(DatasetImagesReference images, WellLocation location)
    {
        this(images.getImageParameters(), images.getDatastoreCode(), images.getDownloadUrl(),
                location);
    }

    public int getTileRowsNum()
    {
        return tileRowsNum;
    }

    public int getTileColsNum()
    {
        return tileColsNum;
    }

    public List<String> getChannelsNames()
    {
        return channelsNames;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public WellLocation getWellLocation()
    {
        return location;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public String getDatastoreCode()
    {
        return datastoreCode;
    }

    public boolean isMultidimensional()
    {
        return isMultidimensional;
    }
}
