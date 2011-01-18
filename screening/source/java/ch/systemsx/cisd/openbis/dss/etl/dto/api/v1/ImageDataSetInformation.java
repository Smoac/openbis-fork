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

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Extends {@link DataSetInformation} with information about images needed in HCS/Microscopy.
 * 
 * @author Tomasz Pylak
 */
public class ImageDataSetInformation extends DataSetInformation
{
    private static final long serialVersionUID = IServer.VERSION;

    private List<ImageFileInfo> images;

    private List<Channel> channels;

    private int tileRowsNumber, tileColumnsNumber;

    public int getTileRowsNumber()
    {
        return tileRowsNumber;
    }

    public int getTileColumnsNumber()
    {
        return tileColumnsNumber;
    }

    public void setTileGeometry(int tileRowsNumber, int tileColumnsNumber)
    {
        this.tileRowsNumber = tileRowsNumber;
        this.tileColumnsNumber = tileColumnsNumber;
    }

    public List<ImageFileInfo> getImages()
    {
        return images;
    }

    public void setImages(List<ImageFileInfo> images)
    {
        this.images = images;
    }

    public List<Channel> getChannels()
    {
        return channels;
    }

    public void setChannels(List<Channel> channels)
    {
        this.channels = channels;
    }

    @Override
    public String toString()
    {
        return "[ dataset code = " + getDataSetCode() + ", tile Rows Number: " + tileRowsNumber
                + ", tile Columns Number: " + tileColumnsNumber + ", channels: "
                + CollectionUtils.abbreviate(channels, -1) + ", images: "
                + CollectionUtils.abbreviate(images, 20);
    }

}
