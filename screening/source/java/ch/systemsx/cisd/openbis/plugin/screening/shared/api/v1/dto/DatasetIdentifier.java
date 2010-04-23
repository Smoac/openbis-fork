package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

/**
 * Contains data which uniquely define a dataset.
 * 
 * @author Tomasz Pylak
 */
public class DatasetIdentifier implements Serializable, IDatasetIdentifier
{
    private static final long serialVersionUID = 1L;

    private final String datasetCode;

    // a.k.a. downloadURL
    private final String datastoreServerUrl;

    public DatasetIdentifier(String datasetCode, String datastoreServerUrl)
    {
        this.datasetCode = datasetCode;
        this.datastoreServerUrl = datastoreServerUrl;
    }

    /** a code of the dataset */
    public String getDatasetCode()
    {
        return datasetCode;
    }

    public String getDatastoreServerUrl()
    {
        return datastoreServerUrl;
    }

    @Override
    public String toString()
    {
        return datasetCode;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || obj instanceof DatasetIdentifier == false)
        {
            return false;
        }
        DatasetIdentifier that = (DatasetIdentifier) obj;
        return datasetCode.equals(that.datasetCode);
    }

    @Override
    public int hashCode()
    {
        return datasetCode.hashCode();
    }
}