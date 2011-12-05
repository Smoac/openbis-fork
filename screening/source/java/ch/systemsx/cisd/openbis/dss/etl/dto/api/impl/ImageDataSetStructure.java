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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.util.List;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ChannelColorComponent;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageStorageConfiguraton;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ToStringUtil;

/**
 * Information about images needed in HCS/Microscopy. Does not contain information about datasets
 * entities and their metadata.
 * 
 * @author Tomasz Pylak
 */
public class ImageDataSetStructure
{
    private List<ImageFileInfo> images;

    private List<Channel> channels;

    private List<ChannelColorComponent> channelColorComponentsOrNull;

    private int tileRowsNumber, tileColumnsNumber;

    private ImageStorageConfiguraton imageStorageConfiguratonOrNull;

    public int getTileRowsNumber()
    {
        return tileRowsNumber;
    }

    public int getTileColumnsNumber()
    {
        return tileColumnsNumber;
    }

    public List<ImageFileInfo> getImages()
    {
        return images;
    }

    public List<Channel> getChannels()
    {
        return channels;
    }

    public List<ChannelColorComponent> getChannelColorComponents()
    {
        return channelColorComponentsOrNull;
    }

    /**
     * @return image storage configuration for this dataset or null if the global configuration of
     *         the storage processor should be used.
     */
    public ImageStorageConfiguraton getImageStorageConfiguraton()
    {
        return imageStorageConfiguratonOrNull;
    }

    public boolean areThumbnailsGenerated()
    {
        return getImageStorageConfiguraton().getThumbnailsStorageFormat().size() > 0;
    }

    // ------ setters

    /** Sets location of the tile (a.k.a. filed or side) on the 'well matrix'. */
    public void setTileGeometry(int tileRowsNumber, int tileColumnsNumber)
    {
        this.tileRowsNumber = tileRowsNumber;
        this.tileColumnsNumber = tileColumnsNumber;
    }

    /** Sets detailed description of the images in the dataset. */
    public void setImages(List<ImageFileInfo> images)
    {
        this.images = images;
    }

    /** Sets all channels available in the dataset. */
    public void setChannels(List<Channel> channels)
    {
        this.channels = channels;
    }

    /**
     * Use this method if channels are encoded in color components of one image (or in other words:
     * each image contains merged channels). For each channel you have to specify the corresponding
     * color component of the image.
     */
    public void setChannels(List<Channel> channels,
            List<ChannelColorComponent> channelColorComponents)
    {
        if (channels.size() != channelColorComponents.size())
        {
            throw new IllegalArgumentException(
                    "There should be exactly one color component for each channel!");
        }
        this.channels = channels;
        this.channelColorComponentsOrNull = channelColorComponents;
    }

    /**
     * Allows to configure various image storage parameters. Set to null if the configuration of the
     * storage processor should be used.
     */
    public void setImageStorageConfiguraton(ImageStorageConfiguraton imageStorageConfiguratonOrNull)
    {
        this.imageStorageConfiguratonOrNull = imageStorageConfiguratonOrNull;
    }

    /** are all necessary fields filled? */
    public boolean isValid()
    {
        return tileRowsNumber > 0 && tileColumnsNumber > 0 && channels != null && images != null;
    }

    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        ToStringUtil.appendNameAndObject(buffer, "config", imageStorageConfiguratonOrNull);
        ToStringUtil.appendNameAndObject(buffer, "tile", tileRowsNumber + "x" + tileColumnsNumber);
        ToStringUtil.appendNameAndObject(buffer, "channels",
                CollectionUtils.abbreviate(channels, -1));
        ToStringUtil.appendNameAndObject(buffer, "number of images", images.size());
        return buffer.toString();
    }

}