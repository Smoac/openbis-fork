package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

/**
 * Contains data which uniquely define a plate.
 * 
 * @author Piotr Buczek
 */
public class WellIdentifier extends PermanentIdentifier
{
    private static final long serialVersionUID = 1L;

    private final PlateIdentifier plateIdentifier;

    private final WellPosition wellPosition;

    public WellIdentifier(PlateIdentifier plateIdentifier, WellPosition wellPosition, String permId)
    {
        super(permId);
        this.wellPosition = wellPosition;
        this.plateIdentifier = plateIdentifier;
    }

    public PlateIdentifier getPlateIdentifier()
    {
        return plateIdentifier;
    }

    public WellPosition getWellPosition()
    {
        return wellPosition;
    }

    @Override
    public String toString()
    {
        return getPermId() + " " + getWellPosition() + ", plate: " + plateIdentifier;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((wellPosition == null) ? 0 : wellPosition.hashCode());
        result = prime * result + ((plateIdentifier == null) ? 0 : plateIdentifier.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (!(obj instanceof WellIdentifier))
        {
            return false;
        }
        WellIdentifier other = (WellIdentifier) obj;
        if (wellPosition == null)
        {
            if (other.wellPosition != null)
            {
                return false;
            }
        } else if (!wellPosition.equals(other.wellPosition))
        {
            return false;
        }
        if (plateIdentifier == null)
        {
            if (other.plateIdentifier != null)
            {
                return false;
            }
        } else if (!plateIdentifier.equals(other.plateIdentifier))
        {
            return false;
        }
        return true;
    }

}