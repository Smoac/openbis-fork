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

    private final IFeatureVectorDatasetIdentifier dataset;

    private final List<String> featureNames;

    private final List<FeatureVector> featureVectors;

    public FeatureVectorDataset(IFeatureVectorDatasetIdentifier dataset, List<String> featureNames,
            List<FeatureVector> featureVectors)
    {
        this.dataset = dataset;
        this.featureNames = featureNames;
        this.featureVectors = featureVectors;
    }

    /** identifier of the dataset containing feature vectors */
    public IDatasetIdentifier getDataset()
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
        sb.append("\n\tfeatures: ");
        for (int i = 0; i < featureNames.size(); i++)
        {
            sb.append(featureNames.get(i) + ", ");
        }
        for (int i = 0; i < featureVectors.size(); i++)
        {
            sb.append("\n\t" + featureVectors.get(i));
        }
        return sb.toString();
    }
}