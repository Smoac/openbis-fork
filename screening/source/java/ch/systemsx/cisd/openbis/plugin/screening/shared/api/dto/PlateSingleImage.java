package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Contains a stream with a single image from a plate and information from which well, channel and
 * tile it comes.
 * 
 * @author Tomasz Pylak
 */
public class PlateSingleImage implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final PlateImageReference imageReference;

    private final InputStream image;

    public PlateSingleImage(PlateImageReference imageReference, InputStream image)
    {
        this.imageReference = imageReference;
        this.image = image;
    }

    /** position of the well to which the image belongs */
    public WellPosition getWellPosition()
    {
        return imageReference.getWellPosition();
    }

    /**
     * tile (aka field) number, each well can be separated into many tiles, for each tile one image
     * is acquired.
     */
    public int getTile()
    {
        return imageReference.getTile();
    }

    /** Index of the channel. Starts from 1. */
    public int getChannel()
    {
        return imageReference.getChannel();
    }

    /** stream with a png image */
    public InputStream getImage()
    {
        return image;
    }
}