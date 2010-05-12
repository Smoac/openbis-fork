package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

/**
 * Contains data which uniquely define a plate
 * 
 * @author Tomasz Pylak
 */
public class PlateIdentifier implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String plateCode, spaceCodeOrNull;

    public PlateIdentifier(String plateCode, String spaceCodeOrNull)
    {
        this.plateCode = plateCode;
        this.spaceCodeOrNull = spaceCodeOrNull;
    }

    /** a code of the plate */
    public String getPlateCode()
    {
        return plateCode;
    }

    /** a code of the space to which the plate belongs or null if it is a shared plate */
    public String tryGetSpaceCode()
    {
        return spaceCodeOrNull;
    }

    @Override
    public String toString()
    {
        return (spaceCodeOrNull != null ? "/" + spaceCodeOrNull : "") + "/" + plateCode;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = prime + plateCode.hashCode();
        return prime * result + ((spaceCodeOrNull == null) ? 0 : spaceCodeOrNull.hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || obj instanceof PlateIdentifier == false)
        {
            return false;
        }
        PlateIdentifier that = (PlateIdentifier) obj;
        return plateCode.equals(that.getPlateCode())
                && ((spaceCodeOrNull != null && spaceCodeOrNull.equals(that.tryGetSpaceCode())) || (spaceCodeOrNull == null && that
                        .tryGetSpaceCode() == null));
    }

}