package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

import java.io.Serializable;

/**
 * Metadata information about images in a specified dataset.
 * 
 * @author Tomasz Pylak
 */
public class ImageDatasetMetadata implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final IDatasetIdentifier imageDataset;

    private final int channelsNumber;

    private final int width;

    private final int height;

    public ImageDatasetMetadata(IDatasetIdentifier dataset, int channelsNumber, int width,
            int height)
    {
        this.imageDataset = dataset;
        this.channelsNumber = channelsNumber;
        this.width = width;
        this.height = height;
    }

    /** identifier of a dataset which contains images described in this class */
    public IDatasetIdentifier getImageDataset()
    {
        return imageDataset;
    }

    /**
     * number of channels in which images have been acquired for the described dataset
     */
    public int getChannelsNumber()
    {
        return channelsNumber;
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
}