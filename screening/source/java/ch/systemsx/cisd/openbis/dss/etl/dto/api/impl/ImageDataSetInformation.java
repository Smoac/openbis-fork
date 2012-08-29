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

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.BasicDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ChannelColorComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Extends {@link DataSetInformation} with information about images needed in HCS/Microscopy.
 * 
 * @author Tomasz Pylak
 */
public class ImageDataSetInformation extends BasicDataSetInformation
{
    private static final long serialVersionUID = IServer.VERSION;

    private File incomingDirectory;

    /**
     * Path to the incoming folder with images, relative to the dataset directory. E.g. if the
     * incoming folder name is X and the transaction's dataset registration code put it inside
     * 'original' folder, then this path points to "original/X'.
     */
    private File datasetRelativeImagesFolderPath;

    private ImageDataSetStructure imageDataSetStructure;

    private ThumbnailsInfo thumbnailsInfos;

    private String containerDatasetPermId;

    private boolean registerAsOverviewImageDataSet;

    private boolean generateOverviewImagesFromRegisteredImages;

    private int maximumImageWidth;

    private int maximumImageHeight;

    public File getIncomingDirectory()
    {
        return incomingDirectory;
    }

    public String getContainerDatasetPermId()
    {
        return containerDatasetPermId;
    }

    /** @returns null if no thumbnails will be stored */
    public ThumbnailsInfo getThumbnailsInfos()
    {
        return thumbnailsInfos;
    }

    public ImageDataSetStructure getImageDataSetStructure()
    {
        return imageDataSetStructure;
    }

    public File getDatasetRelativeImagesFolderPath()
    {
        return datasetRelativeImagesFolderPath;
    }

    /** Sets the folder where all original images are located initially. */
    public void setIncomingDirectory(File incomingDirectory)
    {
        this.incomingDirectory = incomingDirectory;
    }

    public void setDatasetRelativeImagesFolderPath(File datasetRelativeImagesFolderPath)
    {
        this.datasetRelativeImagesFolderPath = datasetRelativeImagesFolderPath;
    }

    public void setContainerDatasetPermId(String containerDatasetPermId)
    {
        this.containerDatasetPermId = containerDatasetPermId;
    }

    public void setThumbnailsInfo(ThumbnailsInfo thumbnailsInfo)
    {
        this.thumbnailsInfos = thumbnailsInfo;
    }

    public void setImageDataSetStructure(ImageDataSetStructure imageDataSetStructure)
    {
        this.imageDataSetStructure = imageDataSetStructure;
    }

    // -------

    public int getMaximumImageWidth()
    {
        return maximumImageWidth;
    }

    public void setMaximumImageWidth(int maximumImageWidth)
    {
        this.maximumImageWidth = maximumImageWidth;
    }

    public int getMaximumImageHeight()
    {
        return maximumImageHeight;
    }

    public void setMaximumImageHeight(int maximumImageHeight)
    {
        this.maximumImageHeight = maximumImageHeight;
    }

    public List<Channel> getChannels()
    {
        return imageDataSetStructure.getChannels();
    }

    public List<ChannelColorComponent> getChannelColorComponents()
    {
        return imageDataSetStructure.getChannelColorComponents();
    }

    public void setChannels(List<Channel> channels)
    {
        imageDataSetStructure.setChannels(channels);
    }

    public void setChannels(List<Channel> channels,
            List<ChannelColorComponent> channelColorComponents)
    {
        imageDataSetStructure.setChannels(channels, channelColorComponents);
    }

    public boolean getRegisterAsOverviewImageDataSet()
    {
        return registerAsOverviewImageDataSet;
    }

    public void setRegisterAsOverviewImageDataSet(boolean registerAsOverviewImageDataSet)
    {
        this.registerAsOverviewImageDataSet = registerAsOverviewImageDataSet;
    }

    public boolean isGenerateOverviewImagesFromRegisteredImages()
    {
        return generateOverviewImagesFromRegisteredImages;
    }

    public void setGenerateOverviewImagesFromRegisteredImages(
            boolean generateOverviewImagesFromRegisteredImages)
    {
        this.generateOverviewImagesFromRegisteredImages =
                generateOverviewImagesFromRegisteredImages;
    }

    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder(super.toString());
        appendNameAndObject(buffer, "images structure", imageDataSetStructure);
        appendNameAndObject(buffer, "container dataset", containerDatasetPermId);
        appendNameAndObject(buffer, "original dataset", this.getDataSetCode());
        if (maximumImageHeight > 0 && maximumImageWidth > 0)
        {
            appendNameAndObject(buffer, "bounding box", maximumImageWidth + "x"
                    + maximumImageHeight);
        }
        if (getThumbnailsInfos() != null)
        {
            appendNameAndObject(buffer, "thumbnail", getThumbnailsInfos());
        } else
        {
            appendNameAndObject(buffer, "thumbnail", "none");
        }
        return buffer.toString();
    }

}
