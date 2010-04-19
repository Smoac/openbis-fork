package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

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

    private final IDatasetIdentifier dataset;

    private final List<String> featureNames;

    private final List<FeatureVector> featureVectors;

    public FeatureVectorDataset(IDatasetIdentifier dataset, List<String> featureNames,
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
}