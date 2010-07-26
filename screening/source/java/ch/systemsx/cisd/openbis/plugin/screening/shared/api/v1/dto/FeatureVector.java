package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Feature vector for one well.
 * 
 * @author Tomasz Pylak
 */
public class FeatureVector implements Serializable
{
    private static final long serialVersionUID = 1L;

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

    @Override
    public String toString()
    {
        return "wellPosition: " + wellPosition + ", values: " + Arrays.toString(values);
    }
}