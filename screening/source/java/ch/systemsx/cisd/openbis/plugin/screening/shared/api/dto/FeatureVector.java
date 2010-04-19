package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

/**
 * Feature vector for one well.
 * 
 * @author Tomasz Pylak
 */
public class FeatureVector
{
    private final WellPosition wellPosition;

    private final double[] values;

    public FeatureVector(WellPosition well, double[] values)
    {
        this.wellPosition = well;
        this.values = values;
    }

    /** well position on a plate */
    public WellPosition getWellPosition()
    {
        return wellPosition;
    }

    /** feature vector values */
    public double[] getValues()
    {
        return values;
    }
}