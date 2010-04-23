package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

/**
 * Description of one dataset.
 * 
 * @author Tomasz Pylak
 */
public class DatasetReference extends DatasetIdentifier implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final PlateIdentifier plate;

    public DatasetReference(String datasetCode, String datastoreServerUrl, PlateIdentifier plate)
    {
        super(datasetCode, datastoreServerUrl);
        this.plate = plate;
    }

    /** a pointer to a plate to which the dataset belongs */
    public PlateIdentifier getPlate()
    {
        return plate;
    }

    @Override
    public String toString()
    {
        return super.toString() + " (plate: " + plate + ")";
    }
}