package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

public class NewETNewPTAssigments implements Serializable
{
    private EntityType entity;
    
    private List<NewPTNewAssigment> assigments;

    public EntityType getEntity()
    {
        return entity;
    }

    public void setEntity(EntityType entity)
    {
        this.entity = entity;
    }

    public List<NewPTNewAssigment> getAssigments()
    {
        return assigments;
    }

    public void setAssigments(List<NewPTNewAssigment> assigments)
    {
        this.assigments = assigments;
    }
    
}
