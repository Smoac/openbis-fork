package ch.ethz.bsse.cisd.DbDataTypeComparison;

import net.lemnik.eodsql.ResultColumn;
import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * @author Manuel Kohler
 */

class MaterialPropertyAssignmentDTO extends AbstractHashable
{

    @ResultColumn("materialType")
    private String materialType;

    @ResultColumn("propertyType")
    private String propertyType;

    MaterialPropertyAssignmentDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public MaterialPropertyAssignmentDTO(String materialType, String propertyType)
    {
        super();
        this.materialType = materialType;
        this.propertyType = propertyType;
    }

    public String getMaterialType()
    {
        return materialType;
    }

    public void setMaterialType(String materialType)
    {
        this.materialType = materialType;
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
