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

    public Channel(String code, String label)
    {
        assert code != null : "code is null";
        assert label != null : "label is null";
        this.label = label;
        this.code = code;
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

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setWavelength(Integer wavelength)
    {
        this.wavelength = wavelength;
    }
}