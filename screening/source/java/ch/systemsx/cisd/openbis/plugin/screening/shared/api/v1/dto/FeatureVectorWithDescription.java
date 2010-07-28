package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Feature vectors of one well in one feature vector dataset.
 * 
 * @author Bernd Rinn
 */
public class FeatureVectorWithDescription extends FeatureVector implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final FeatureVectorDatasetWellReference datasetWellReference;

    private final List<String> featureNames;

    public FeatureVectorWithDescription(FeatureVectorDatasetWellReference dataset,
            List<String> featureNames, double[] values)
    {
        super(dataset.getWellPosition(), values);
        this.datasetWellReference = dataset;
        this.featureNames = featureNames;
    }

    /**
     * Identifier of the dataset and well of this feature vector.
     */
    public FeatureVectorDatasetWellReference getDatasetWellReference()
    {
        return datasetWellReference;
    }

    /**
     * Names (and implicitly order) of the features present in each feature vector.
     */
    public List<String> getFeatureNames()
    {
        return featureNames;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("datasetCode: " + datasetWellReference.getDatasetCode());
        sb.append(", storeUrl: " + datasetWellReference.getDatastoreServerUrl());
        sb.append("\n\tfeatures: " + featureNames);
        sb.append("\n");
        sb.append(super.toString());
        return sb.toString();
    }

}