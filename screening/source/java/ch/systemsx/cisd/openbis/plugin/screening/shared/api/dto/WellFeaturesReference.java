package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

/**
 * Identifies one well in a dataset containing feature vectors.
 * 
 * @author Tomasz Pylak
 */
public class WellFeaturesReference
{
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