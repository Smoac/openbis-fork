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

    /**
     * Creates a {@link PlateIdentifier} from the given <var>augmentedCode</code>.
     * 
     * @param augmentedCode The <var>augmentedCode</code> in the form
     *            <code>/SPACE/PROJECT/EXPERIMENT</code>
     * @return A plate identifier corresponding to <var>augmentedCode</code>. Note that this plate
     *         identifier has no perm id set.
     * @throws IllegalArgumentException If the <var>augmentedCode</code> is not in one of the forms
     *             <code>/SPACE/PLATE', /PLATE or PLATE</code>.
     */
    public static PlateIdentifier createFromAugmentedCode(String augmentedCode)
            throws IllegalArgumentException
    {
        final String[] splitted = augmentedCode.split("/");
        if (splitted.length == 1) // Sample in home space
        {
            return new PlateIdentifier(splitted[0], null);
        }
        if (splitted.length == 2 && splitted[0].length() == 0) // Shared sample
        {
            return new PlateIdentifier(splitted[0], "");
        }
        if (splitted.length != 3 || splitted[0].length() != 0)
        {
            throw new IllegalArgumentException("Augmented code '" + augmentedCode
                    + "' needs to be of the form '/SPACE/PLATE', '/PLATE' or 'PLATE'.");
        }
        return new PlateIdentifier(splitted[2], splitted[1]);
    }

    /**
     * Creates a {@link PlateIdentifier} from the given <var>permId</code>.
     * 
     * @param permId The <var>permId</code>
     * @return A plate identifier corresponding to <var>permId</code>. Note that this plate
     *         identifier has no code or space set.
     * @throws IllegalArgumentException If the <var>augmentedCode</code> is not in one of the forms
     *             <code>/SPACE/PLATE', /PLATE or PLATE</code>.
     */
    public static PlateIdentifier createFromPermId(String permId) throws IllegalArgumentException
    {
        return new PlateIdentifier(null, null, permId);
    }

    /**
     * An empty <var>spaceCode</var> is interpreted as the home space, a <code>null</code>
     * <var>spaceCode</code> is interpreted as 'no space', i.e. identifies a shared sample.
     */
    protected PlateIdentifier(String plateCode, String spaceCodeOrNull)
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
            if (isSharedPlate())
            {
                return "/" + plateCode;
            } else
            {
                return "/" + spaceCodeOrNull + "/" + plateCode;
            }
        } else
        {
            return plateCode;
        }
    }

    public boolean isSharedPlate()
    {
        return "".equals(spaceCodeOrNull);
    }

    @Override
    public String toString()
    {
        if (getPermId() == null)
        {
            return getAugmentedCode();
        } else
        {
            return getAugmentedCode() + " [" + getPermId() + "]";
        }
    }

}