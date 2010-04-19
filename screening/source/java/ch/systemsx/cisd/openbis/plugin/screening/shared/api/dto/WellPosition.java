package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

/**
 * Point to the well on a plate, contains row and column number.
 * 
 * @author Tomasz Pylak
 */
public class WellPosition
{
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
}