package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Description of one dataset.
 * 
 * @author Tomasz Pylak
 */
public class DatasetReference extends DatasetIdentifier implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final PlateIdentifier plate;

    private final Geometry plateGeometry;

    private final Date registrationDate;

    @Deprecated
    public DatasetReference(String datasetCode, String datastoreServerUrl, PlateIdentifier plate)
    {
        this(datasetCode, datastoreServerUrl, plate, null, null);
    }

    public DatasetReference(String datasetCode, String datastoreServerUrl, PlateIdentifier plate,
            Geometry plateGeometry, Date registrationDate)
    {
        super(datasetCode, datastoreServerUrl);
        this.plate = plate;
        this.plateGeometry = plateGeometry;
        this.registrationDate = registrationDate;
    }
    
    /**
     * Returns the plate geometry.
     */
    public final Geometry getPlateGeometry()
    {
        return plateGeometry;
    }

    /**
     * Returns the registration date.
     */
    public final Date getRegistrationDate()
    {
        return registrationDate;
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