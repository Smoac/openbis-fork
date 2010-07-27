package ch.ethz.bsse.cisd.DbDataTypeComparison;

import net.lemnik.eodsql.ResultColumn;
import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * @author Manuel Kohler
 */

class DataSetPropertyAssignmentsDTO extends AbstractHashable
{
    @ResultColumn("dataSet")
    private String dataSet;

    @ResultColumn("propertyType")
    private String propertyType;

    DataSetPropertyAssignmentsDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public DataSetPropertyAssignmentsDTO(String dataSet, String propertyType)
    {
        super();
        this.dataSet = dataSet;
        this.propertyType = propertyType;
    }

    public String getDataSet()
    {
        return dataSet;
    }

    public void setDataSet(String dataSet)
    {
        this.dataSet = dataSet;
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
