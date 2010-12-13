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

    private final int row, col;

    public WellIdentifier(PlateIdentifier plateIdentifier, String permId, int row, int col)
    {
        super(permId);
        this.plateIdentifier = plateIdentifier;
        this.row = row;
        this.col = col;
    }

    public PlateIdentifier getPlateIdentifier()
    {
        return plateIdentifier;
    }

    public int getRow()
    {
        return row;
    }

    public int getCol()
    {
        return col;
    }

    @Override
    public String toString()
    {
        return plateIdentifier + "/" + getPermId();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + col;
        result = prime * result + ((plateIdentifier == null) ? 0 : plateIdentifier.hashCode());
        result = prime * result + row;
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
        if (col != other.col)
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
        if (row != other.row)
        {
            return false;
        }
        return true;
    }

}