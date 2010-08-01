package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Date;

/**
 * Description of one plate-based screening dataset.
 * 
 * @author Tomasz Pylak
 */
public class DatasetReference extends DatasetIdentifier implements Serializable
{
    private static final long serialVersionUID = 1L;

    private PlateIdentifier plate;

    private ExperimentIdentifier experimentIdentifier;

    private Geometry plateGeometry;

    private Date registrationDate;

    @Deprecated
    public DatasetReference(String datasetCode, String datastoreServerUrl, PlateIdentifier plate)
    {
        this(datasetCode, datastoreServerUrl, plate, null, null);
    }

    @Deprecated
    public DatasetReference(String datasetCode, String datastoreServerUrl, PlateIdentifier plate,
            Geometry plateGeometry, Date registrationDate)
    {
        this(datasetCode, datastoreServerUrl, plate, null, plateGeometry, registrationDate);
    }

    public DatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plateWithExperiment, ExperimentIdentifier experiment,
            Geometry plateGeometry, Date registrationDate)
    {
        super(datasetCode, datastoreServerUrl);
        this.plate = plateWithExperiment;
        this.experimentIdentifier = (experiment == null) ? createFakeExperiment(plate) : experiment;
        this.plateGeometry = plateGeometry;
        this.registrationDate = registrationDate;
    }

    private static ExperimentIdentifier createFakeExperiment(PlateIdentifier plate)
    {
        return new ExperimentIdentifier("?", "?", (plate == null) ? "?" : plate.tryGetSpaceCode(),
                "?");
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

    /**
     * Returns the identifier of the plate that this dataset belongs to.
     */
    public PlateIdentifier getPlate()
    {
        return plate;
    }

    /**
     * Returns the identifier of the experiment that this dataset belongs to.
     * 
     * @since 1.2
     */
    public ExperimentIdentifier getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    // Special method for customizing Java deserialization.
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        // Kick-off the default serialization procedure.
        in.defaultReadObject();
        // V1.0 and V1.1 didn't have the experimentIdentifier, so it may be null here.
        if (experimentIdentifier == null)
        {
            experimentIdentifier = createFakeExperiment(plate);
        }
    }

    @Override
    public String toString()
    {
        return super.toString() + " (plate: " + plate + ")";
    }
}