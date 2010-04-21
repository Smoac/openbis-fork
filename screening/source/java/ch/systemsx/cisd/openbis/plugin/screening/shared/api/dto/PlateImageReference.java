package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

import java.io.Serializable;

/**
 * Identifies one well in a dataset containing images.
 * 
 * @author Tomasz Pylak
 */
public class PlateImageReference extends DatasetIdentifier implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final WellPosition wellPosition;

    private final int tile;

    private final int channel;

    public PlateImageReference(int wellRow, int wellColumn, int tile, int channel,
            IDatasetIdentifier dataset)
    {
        super(dataset.getDatasetCode(), dataset.getDatastoreServerUrl());
        this.wellPosition = new WellPosition(wellRow, wellColumn);
        this.tile = tile;
        this.channel = channel;
    }

    /** well position on the plate */
    public WellPosition getWellPosition()
    {
        return wellPosition;
    }

    /** a sequential number of an image tile, starts from 1 */
    public int getTile()
    {
        return tile;
    }

    /** index of a channel in which an image has been taken, starts from 1 */
    public int getChannel()
    {
        return channel;
    }

    @Override
    public String toString()
    {
        return "Image for [dataset " + getDatasetCode() + ", well " + wellPosition + ", channel "
                + channel + ", tile " + tile + "]";
    }
}