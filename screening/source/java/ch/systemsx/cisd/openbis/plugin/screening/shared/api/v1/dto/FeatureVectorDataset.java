package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Feature vectors of one dataset.
 * 
 * @author Tomasz Pylak
 */
public class FeatureVectorDataset implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final FeatureVectorDatasetReference dataset;

    private final List<String> featureNames;

    private final List<FeatureVector> featureVectors;

    public FeatureVectorDataset(FeatureVectorDatasetReference dataset, List<String> featureNames,
            List<FeatureVector> featureVectors)
    {
        this.dataset = dataset;
        this.featureNames = featureNames;
        this.featureVectors = featureVectors;
    }

    /** identifier of the dataset containing feature vectors */
    public FeatureVectorDatasetReference getDataset()
    {
        return dataset;
    }

    /** names of features present in each feature vector */
    public List<String> getFeatureNames()
    {
        return featureNames;
    }

    /** all feature vectors for a dataset */
    public List<FeatureVector> getFeatureVectors()
    {
        return featureVectors;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("datasetCode: " + dataset.getDatasetCode());
        sb.append(", storeUrl: " + dataset.getDatastoreServerUrl());
        sb.append("\n\tfeatures: " + featureNames);
        for (int i = 0; i < featureVectors.size(); i++)
        {
            sb.append("\n\t" + featureVectors.get(i));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || obj instanceof FeatureVectorDataset == false)
        {
            return false;
        }
        FeatureVectorDataset that = (FeatureVectorDataset) obj;
        return dataset.getDatasetCode().equals(that.getDataset().getDatasetCode());
    }

    @Override
    public int hashCode()
    {
        return dataset.getDatasetCode().hashCode();
    }
}