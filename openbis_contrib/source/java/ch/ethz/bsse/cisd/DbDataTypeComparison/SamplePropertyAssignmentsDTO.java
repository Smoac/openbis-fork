package ch.ethz.bsse.cisd.DbDataTypeComparison;

import net.lemnik.eodsql.ResultColumn;
import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * @author Manuel Kohler
 */
class SamplePropertyAssignmentsDTO extends AbstractHashable
{
    @ResultColumn("sampleType")
    private String sampleType;

    @ResultColumn("propertyType")
    private String propertyType;

    SamplePropertyAssignmentsDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public SamplePropertyAssignmentsDTO(String sampleType, String propertyType)
    {
        super();
        this.sampleType = sampleType;
        this.propertyType = propertyType;
    }

    public String getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(String sampleType)
    {
        this.sampleType = sampleType;
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
