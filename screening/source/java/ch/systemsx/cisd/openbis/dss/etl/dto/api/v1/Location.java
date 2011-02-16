package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

/**
 * Auxiliary structure to store tile location on the well. The top left tile has coordinates (1,1).
 * 
 * @author Tomasz Pylak
 */
public class Location
{
    private final int row, column;

    /** Note: The top left tile has coordinates (1,1). */
    public Location(int row, int column)
    {
        this.row = row;
        this.column = column;
    }

    public int getRow()
    {
        return row;
    }

    public int getColumn()
    {
        return column;
    }
}