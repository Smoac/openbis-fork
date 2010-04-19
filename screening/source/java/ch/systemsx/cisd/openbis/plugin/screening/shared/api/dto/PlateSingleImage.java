package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

import java.io.InputStream;

/**
 * Contains a stream with a single image from a plate and information from which well, channel and
 * tile it comes.
 * 
 * @author Tomasz Pylak
 */
public class PlateSingleImage
{
    private final WellPosition wellPosition;

    private final int tile;

    private final int channel;

    private final InputStream image;

    public PlateSingleImage(WellPosition wellPosition, int tile, int channel, InputStream image)
    {
        this.wellPosition = wellPosition;
        this.tile = tile;
        this.channel = channel;
        this.image = image;
    }

    /** position of the well to which the image belongs */
    public WellPosition getWellPosition()
    {
        return wellPosition;
    }

    /**
     * tile (a.k.a. field) number. Each well can be separated into many tiles, for each tile one
     * image is acquired.
     */
    public int getTile()
    {
        return tile;
    }

    /** Index of the channel. Starts from 1. */
    public int getChannel()
    {
        return channel;
    }

    /** stream with a png image */
    public InputStream getImage()
    {
        return image;
    }
}