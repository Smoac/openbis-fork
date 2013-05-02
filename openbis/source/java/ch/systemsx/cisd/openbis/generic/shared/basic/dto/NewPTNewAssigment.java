package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

public class NewPTNewAssigment implements Serializable
{
    private PropertyType propertyType;
    private NewETPTAssignment assignment;
    
    public PropertyType getPropertyType()
    {
        return propertyType;
    }
    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }
    public NewETPTAssignment getAssignment()
    {
        return assignment;
    }
    public void setAssignment(NewETPTAssignment assignment)
    {
        this.assignment = assignment;
    }
    
}