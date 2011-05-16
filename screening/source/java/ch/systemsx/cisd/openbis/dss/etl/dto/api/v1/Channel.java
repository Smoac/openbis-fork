package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * A channel in which the image has been acquired.
 * <p>
 * Each channel has its <code>code</code> which uniquely identifies it in one experiment or dataset.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class Channel extends AbstractHashable
{
    private final String code;

    private final String label;

    private String description;

    private Integer wavelength;

    private ChannelColor channelColorOrNull;

    /**
     * Constructs a channel with a specified code and label. The channel will be presented in a
     * default color.
     */
    public Channel(String code, String label)
    {
        this(code, label, null);
    }

    /**
     * Constructs a channel with a specified code and label. The channel will be presented in a
     * specified color.
     */
    public Channel(String code, String label, ChannelColor channelColorOrNull)
    {
        assert code != null : "code is null";
        assert label != null : "label is null";
        this.label = label;
        this.code = code;
        this.channelColorOrNull = channelColorOrNull;
    }

    public String getCode()
    {
        return code;
    }

    public String tryGetDescription()
    {
        return description;
    }

    public Integer tryGetWavelength()
    {
        return wavelength;
    }

    public String getLabel()
    {
        return label;
    }

    /**
     * @return color for the specified channel which will be used to display merged channels images.
     *         null only during dataset registration wheb default color should be used, afterwards
     *         never null.
     */
    public ChannelColor tryGetChannelColor()
    {
        return channelColorOrNull;
    }

    /** Sets the description of the channel.Optional. */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /** Sets the wavelength of the channel.Optional. */
    public void setWavelength(Integer wavelength)
    {
        this.wavelength = wavelength;
    }

    /** Sets the color in which this channel will be displayed. */
    public void setChannelColor(ChannelColor channelColor)
    {
        this.channelColorOrNull = channelColor;
    }

}