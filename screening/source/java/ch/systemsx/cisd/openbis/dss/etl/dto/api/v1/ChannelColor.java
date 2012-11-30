package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

/**
 * This class is obsolete, and should not be used. Use
 * {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor} instead
 * 
 * @author Jakub Straszewski
 */
public enum ChannelColor
{
    BLUE(0), GREEN(1), RED(2), RED_GREEN(3), RED_BLUE(4), GREEN_BLUE(5);

    private static final int MAX_COLOR = calcMaxColorIndex();

    // If no mapping between channels and colors has been provided then channels get consecutive
    // colors. This field determines the order in which colors are assigned.
    // It is important for backward compatibility as well.
    private final int orderIndex;

    private ChannelColor(int orderIndex)
    {
        this.orderIndex = orderIndex;
    }

    private static int calcMaxColorIndex()
    {
        int max = 0;
        for (ChannelColor color : values())
        {
            max = Math.max(max, color.getColorOrderIndex());
        }
        return max;
    }

    public int getColorOrderIndex()
    {
        return orderIndex;
    }

    public static ChannelColor createFromIndex(int colorIndex)
    {
        for (ChannelColor color : values())
        {
            if (color.getColorOrderIndex() == colorIndex % (MAX_COLOR + 1))
            {
                return color;
            }
        }
        throw new IllegalStateException("Invalid color index: " + colorIndex + "!");
    }

    public ChannelColorRGB getRGB()
    {
        switch (this)
        {
            case RED:
                return new ChannelColorRGB(255, 0, 0);
            case GREEN:
                return new ChannelColorRGB(0, 255, 0);
            case BLUE:
                return new ChannelColorRGB(0, 0, 255);
            case RED_GREEN:
                return new ChannelColorRGB(255, 255, 0);
            case GREEN_BLUE:
                return new ChannelColorRGB(0, 255, 255);
            case RED_BLUE:
                return new ChannelColorRGB(255, 0, 255);
            default:
                throw new IllegalStateException("unhandled enum " + this);
        }
    }

    public ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor getIndependentChannelColor()
    {
        switch (this)
        {
            case RED:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.RED;
            case GREEN:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.GREEN;
            case BLUE:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.BLUE;
            case RED_GREEN:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.RED_GREEN;
            case GREEN_BLUE:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.GREEN_BLUE;
            case RED_BLUE:
                return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor.RED_BLUE;
            default:
                throw new IllegalStateException("unhandled enum " + this);
        }
    }
}