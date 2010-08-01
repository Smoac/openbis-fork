package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Feature vectors of one well in one feature vector dataset.
 * 
 * @since 1.1
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
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((datasetWellReference == null) ? 0 : datasetWellReference.hashCode());
        result = prime * result + ((featureNames == null) ? 0 : featureNames.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FeatureVectorWithDescription other = (FeatureVectorWithDescription) obj;
        if (datasetWellReference == null)
        {
            if (other.datasetWellReference != null)
                return false;
        } else if (!datasetWellReference.equals(other.datasetWellReference))
            return false;
        if (featureNames == null)
        {
            if (other.featureNames != null)
                return false;
        } else if (!featureNames.equals(other.featureNames))
            return false;
        return true;
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