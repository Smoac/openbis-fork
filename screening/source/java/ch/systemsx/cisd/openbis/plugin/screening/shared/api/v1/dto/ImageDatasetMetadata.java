package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Metadata information about images in a specified dataset.
 * 
 * @author Tomasz Pylak
 */
public class ImageDatasetMetadata implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final IImageDatasetIdentifier imageDataset;

    private final int channelsNumber;

    private final List<String> channelNames;

    private final int tilesNumber;

    private final int width;

    private final int height;

    public ImageDatasetMetadata(IImageDatasetIdentifier dataset, List<String> channelNames,
            int tilesNumber, int width, int height)
    {
        this.imageDataset = dataset;
        this.channelNames = channelNames;
        this.channelsNumber = channelNames.size();
        this.tilesNumber = tilesNumber;
        this.width = width;
        this.height = height;
    }

    /** identifier of an image dataset which contains images described in this class */
    public IImageDatasetIdentifier getImageDataset()
    {
        return imageDataset;
    }

    /**
     * number of channels in which images have been acquired for the described dataset
     */
    public int getNumberOfChannels()
    {
        return channelsNumber;
    }

    /**
     * names of channels in which images have been acquired for the described dataset
     */
    public List<String> getChannelNames()
    {
        return channelNames;
    }

    /**
     * number of image tiles (aka fields) into which each well is splited
     */
    public int getNumberOfTiles()
    {
        return tilesNumber;
    }

    /** width of all the images in the described dataset */
    public int getWidth()
    {
        return width;
    }

    /** height of all the images in the described dataset */
    public int getHeight()
    {
        return height;
    }

    @Override
    public String toString()
    {
        return "Dataset " + imageDataset + " has [" + channelNames + "] channels, " + tilesNumber
                + " tiles. Images resolution: " + width + "x" + height;
    }
}