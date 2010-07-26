package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

/**
 * Identifies one image acquired in a screening context.
 * 
 * @author Tomasz Pylak
 */
public class PlateImageReference extends DatasetIdentifier
{
    private static final long serialVersionUID = 1L;

    private final WellPosition wellPosition;

    private final int tile;

    private final String channel;

    /**
     * @param dataset if image dataset is specified, image will be fetched from it. If a feature
     *            vector dataset is specified, a connected image dataset will be found and image
     *            will be fetched from it.
     */
    public PlateImageReference(int wellRow, int wellColumn, int tile, String channel,
            IDatasetIdentifier dataset)
    {
        super(dataset.getDatasetCode(), dataset.getDatastoreServerUrl());
        this.wellPosition = new WellPosition(wellRow, wellColumn);
        this.tile = tile;
        this.channel = channel.toUpperCase();
    }

    /** well position on the plate */
    public WellPosition getWellPosition()
    {
        return wellPosition;
    }

    /** a sequential number of an image tile, starts from 0 */
    public int getTile()
    {
        return tile;
    }

    /**
     * channel name
     */
    public String getChannel()
    {
        return channel;
    }

    @Override
    public String toString()
    {
        return "Image for [dataset " + getDatasetCode() + ", well " + wellPosition + ", channel "
                + channel + ", tile " + tile + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + super.hashCode();
        result = prime * result + channel.hashCode();
        result = prime * result + tile;
        result = prime * result + wellPosition.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        if (super.equals(obj) == false)
        {
            return false;
        }

        final PlateImageReference other = (PlateImageReference) obj;
        if (channel.equals(other.channel) == false)
        {
            return false;
        }
        if (tile != other.tile)
        {
            return false;
        }
        if (wellPosition.equals(other.wellPosition) == false)
        {
            return false;
        }
        return true;
    }

}