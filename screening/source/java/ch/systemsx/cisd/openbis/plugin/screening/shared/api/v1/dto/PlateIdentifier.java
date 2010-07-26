package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

/**
 * Contains data which uniquely define a plate.
 * 
 * @author Tomasz Pylak
 */
public class PlateIdentifier extends PermanentIdentifier
{
    private static final long serialVersionUID = 1L;

    private final String plateCode, spaceCodeOrNull;

    public PlateIdentifier(String plateCode, String spaceCodeOrNull)
    {
        this(plateCode, spaceCodeOrNull, null);
    }

    public PlateIdentifier(String plateCode, String spaceCodeOrNull, String permId)
    {
        super(permId);
        this.plateCode = plateCode;
        this.spaceCodeOrNull = spaceCodeOrNull;
    }

    /**
     * A code of the plate.
     */
    public String getPlateCode()
    {
        return plateCode;
    }

    /**
     * A code of the space to which the plate belongs or <code>null</code> if it is a shared plate.
     */
    public String tryGetSpaceCode()
    {
        return spaceCodeOrNull;
    }

    /**
     * Returns the augmented (full) code of this plate.
     */
    public String getAugmentedCode()
    {
        if (spaceCodeOrNull != null)
        {
            return "/" + spaceCodeOrNull + "/" + plateCode;
        } else
        {
            return "/" + plateCode;
        }
    }

}