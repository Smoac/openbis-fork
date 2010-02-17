/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Reference to a single image acquired for a plate.
 * 
 * @author Tomasz Pylak
 */
public class PlateSingleImageReference implements IsSerializable, Serializable
{
    private static final long serialVersionUID = 1L;

    private String datasetCode;

    private int wellRow;

    private int wellCol;

    private int tile;

    private int channel;

    // url to fetch the image from DSS using HTTP, the hostname and servlet name is not included
    private String imageUrl;

    // path in the DSS store
    private String imagePath;

    public int getWellRow()
    {
        return wellRow;
    }

    public void setWellRow(int wellRow)
    {
        this.wellRow = wellRow;
    }

    public int getWellCol()
    {
        return wellCol;
    }

    public void setWellCol(int wellCol)
    {
        this.wellCol = wellCol;
    }

    public int getTile()
    {
        return tile;
    }

    public void setTile(int tile)
    {
        this.tile = tile;
    }

    public int getChannel()
    {
        return channel;
    }

    public void setChannel(int channel)
    {
        this.channel = channel;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public void setDatasetCode(String datasetCode)
    {
        this.datasetCode = datasetCode;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

}
