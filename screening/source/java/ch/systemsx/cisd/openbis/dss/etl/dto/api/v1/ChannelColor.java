package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

/**
 * Allowed colors in which channels can be presented.
 * 
 * @author Tomasz Pylak
 */
public enum ChannelColor
{
    BLUE(0), GREEN(1), RED(2), RED_GREEN(3), RED_BLUE(4), GREEN_BLUE(5);

    // If no mapping between channels and colors has been provided then channels get consecutive
    // colors. This field determines the order in which colors are assigned.
    // It is important for backward compatibility as well.
    private final int orderIndex;

    private ChannelColor(int orderIndex)
    {
        this.orderIndex = orderIndex;
    }

    public int getColorOrderIndex()
    {
        return orderIndex;
    }

    public static ChannelColor createFromIndex(int colorIndex)
    {
        for (ChannelColor color : values())
        {
            if (color.getColorOrderIndex() == colorIndex)
            {
                return color;
            }
        }
        throw new IllegalStateException("Invalid color index: " + colorIndex + "!");
    }
}