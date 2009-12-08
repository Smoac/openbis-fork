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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Describes the whole plate - images and metadata of each non-empty well.
 * 
 * @author Tomasz Pylak
 */
public class PlateContent implements IsSerializable
{
    private PlateImageParameters imageParameters;

    private List<WellMetadata> wells;

    private PlateImages imagesOrNull;

    // GWT only
    @SuppressWarnings("unused")
    private PlateContent()
    {
    }

    public PlateContent(PlateImageParameters imageParameters, List<WellMetadata> wells,
            PlateImages imagesOrNull)
    {
        this.imageParameters = imageParameters;
        this.wells = wells;
        this.imagesOrNull = imagesOrNull;
    }

    public int getRowsNum()
    {
        return imageParameters.getRowsNum();
    }

    public int getColsNum()
    {
        return imageParameters.getColsNum();
    }

    public int getTileRowsNum()
    {
        return imageParameters.getTileRowsNum();
    }

    public int getTileColsNum()
    {
        return imageParameters.getTileColsNum();
    }

    public int getChannelsNum()
    {
        return imageParameters.getChannelsNum();
    }

    public List<WellMetadata> getWells()
    {
        return wells;
    }

    /** can be null */
    public PlateImages tryGetImages()
    {
        return imagesOrNull;
    }
}
