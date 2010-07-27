package ch.ethz.bsse.cisd.DbDataTypeComparison;

import net.lemnik.eodsql.ResultColumn;
import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * @author Manuel Kohler
 */

class ExperimentPropertyAssignmentsDTO extends AbstractHashable
{
    @ResultColumn("experimentType")
    private String experimentType;

    @ResultColumn("propertyType")
    private String propertyType;

    ExperimentPropertyAssignmentsDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ExperimentPropertyAssignmentsDTO(String experimentType, String propertyType)
    {
        super();
        this.experimentType = experimentType;
        this.propertyType = propertyType;
    }

    public String getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(String experimentType)
    {
        this.experimentType = experimentType;
    }

    public String getPropertyType()
    {
        return propertyType;
    }

    public void setPropertyType(String propertyType)
    {
        this.propertyType = propertyType;
    }

}
