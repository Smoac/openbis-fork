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

    private final List<String> channelCodes;

    private final List<String> channelLabels;

    private final int tilesNumber;

    private final int tilesRows;

    private final int tilesCols;

    private final int width;

    private final int height;

    private final int thumbnailWidth;

    private final int thumbnailHeight;

    public ImageDatasetMetadata(IImageDatasetIdentifier dataset, List<String> channelCodes,
            List<String> channelLabels, int tilesRows, int tilesCols, int width, int height,
            int thumbnailWidth, int thumbnailHeight)
    {
        this.imageDataset = dataset;
        this.channelNames = channelCodes;
        this.channelCodes = channelCodes;
        this.channelLabels = channelLabels;
        this.channelsNumber = channelNames.size();
        this.tilesRows = tilesRows;
        this.tilesCols = tilesCols;
        this.tilesNumber = tilesRows * tilesCols;
        this.width = width;
        this.height = height;
        this.thumbnailHeight = thumbnailHeight;
        this.thumbnailWidth = thumbnailWidth;
    }

    /**
     * Identifier of this image dataset.
     */
    public IImageDatasetIdentifier getImageDataset()
    {
        return imageDataset;
    }

    /**
     * Number of channels (wavelengths) in which images have been acquired for this dataset.
     */
    public int getNumberOfChannels()
    {
        return channelsNumber;
    }

    /**
     * Names of channels in which images have been acquired for this dataset.
     */
    @Deprecated
    public List<String> getChannelNames()
    {
        return channelNames;
    }

    /**
     * Returns channel codes.
     * <p>
     * <i>Note: If channel codes are unspecified channel names are returned. This will be the case
     * if a serialized instance of a previous of this class will be deserialized.</i>
     */
    public List<String> getChannelCodes()
    {
        return channelCodes == null ? channelNames : channelCodes;
    }

    /**
     * Returns channel labels.
     * <p>
     * <i>Note: If channel labels are unspecified channel names are returned. This will be the case
     * if a serialized instance of a previous of this class will be deserialized.</i>
     */
    public List<String> getChannelLabels()
    {
        return channelLabels == null ? channelNames : channelLabels;
    }

    /**
     * Number of rows of image tiles (or "fields") available for each well.
     * <p>
     * <i>Note: Will be 0 if the server does not support API version 1.6</i>
     * 
     * @since 1.6
     */
    public int getTilesRows()
    {
        return tilesRows;
    }

    /**
     * Number of columns of image tiles (or "fields") available for each well.
     * <p>
     * <i>Note: Will be 0 if the server does not support API version 1.6</i>
     * 
     * @since 1.6
     */
    public int getTilesCols()
    {
        return tilesCols;
    }

    /**
     * Number of image tiles (or "fields") available for each well.
     */
    public int getNumberOfTiles()
    {
        return tilesNumber;
    }

    /**
     * Width of the images in this dataset.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Height of the images in this dataset.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * The width of the thumbnail images, or 0 if no thumbnails are available for this data set.
     * 
     * @since 1.6
     */
    public int getThumbnailWidth()
    {
        return thumbnailWidth;
    }

    /**
     * The height of the thumbnail images, or 0 if no thumbnails are available for this data set.
     * 
     * @since 1.6
     */
    public int getThumbnailHeight()
    {
        return thumbnailHeight;
    }

    /**
     * <code>true</code>, if this data set has thumbnails, <code>false</code> otherwise.
     * 
     * @since 1.6
     */
    public boolean hasThumbnails()
    {
        return thumbnailHeight != 0 && thumbnailWidth != 0;
    }

    @Override
    public String toString()
    {
        String thumbnailsDesc =
                hasThumbnails() ? ". Thumbnail resolution: " + thumbnailWidth + "x"
                        + thumbnailHeight + "." : "";
        return "Dataset " + imageDataset + " has [" + getChannelCodes() + "] channels, "
                + tilesNumber + " tiles. Image resolution: " + width + "x" + height
                + thumbnailsDesc;
    }
}