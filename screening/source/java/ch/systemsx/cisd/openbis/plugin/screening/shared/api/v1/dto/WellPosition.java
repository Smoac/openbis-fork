package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

/**
 * Point to the well on a plate, contains row and column number.
 * 
 * @author Tomasz Pylak
 */
public class WellPosition implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final int wellRow, wellColumn;

    public WellPosition(int wellRow, int wellColumn)
    {
        this.wellRow = wellRow;
        this.wellColumn = wellColumn;
    }

    /** well row, starts from 1 */
    public int getWellRow()
    {
        return wellRow;
    }

    /** well column, starts from 1 */
    public int getWellColumn()
    {
        return wellColumn;
    }

    @Override
    public String toString()
    {
        return "[" + wellRow + ", " + wellColumn + "]";
    }

    @Override
    public int hashCode()
    {
        int result = 313 + wellColumn;
        result = 313 * result + wellRow;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WellPosition other = (WellPosition) obj;
        if (wellColumn != other.wellColumn)
            return false;
        if (wellRow != other.wellRow)
            return false;
        return true;
    }

}