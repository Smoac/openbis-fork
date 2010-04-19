package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

import java.io.Serializable;

/**
 * Description of one dataset.
 * 
 * @author Tomasz Pylak
 */
public class Dataset implements IDatasetIdentifier, Serializable
{
    private static final long serialVersionUID = 1L;

    private final String datasetCode;

    private final String datastoreCode;

    private final IPlateIdentifier plate;

    public Dataset(String datasetCode, String datastoreCode, IPlateIdentifier plate)
    {
        this.datasetCode = datasetCode;
        this.datastoreCode = datastoreCode;
        this.plate = plate;
    }

    /** a code of the dataset */
    public String getDatasetCode()
    {
        return datasetCode;
    }

    /** a code which points to the datastore server on which the dataset is accessible */
    public String getDatastoreCode()
    {
        return datastoreCode;
    }

    /** a pointer to a plate to which the dataset belongs */
    public IPlateIdentifier getPlate()
    {
        return plate;
    }

    @Override
    public String toString()
    {
        return datasetCode;
    }
}