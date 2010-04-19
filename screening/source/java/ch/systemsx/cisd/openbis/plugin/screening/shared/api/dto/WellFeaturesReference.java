package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

import java.io.Serializable;

/**
 * Identifies one well in a dataset containing feature vectors.
 * 
 * @author Tomasz Pylak
 */
public class WellFeaturesReference implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final WellPosition wellPosition;

    private final IDatasetIdentifier featureVectorDataset;

    public WellFeaturesReference(WellPosition wellPosition, IDatasetIdentifier featureVectorDataset)
    {
        this.wellPosition = wellPosition;
        this.featureVectorDataset = featureVectorDataset;
    }

    /** well position on the plate */
    public WellPosition getWellPosition()
    {
        return wellPosition;
    }

    /** identifier of the dataset containing feature vectors */
    public IDatasetIdentifier getFeatureVectorDataset()
    {
        return featureVectorDataset;
    }
}