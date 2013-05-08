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

    public void updateOrdinalToDBOrder()
    {
        // Update Ordinal - Internal/External List
        for (int i = 0; i < entity.getAssignedPropertyTypes().size(); i++)
        {
            entity.getAssignedPropertyTypes().get(i).setOrdinal((long) i);
            assigments.get(i).getAssignment().setOrdinal((long) i);
        }
    }

    public void updateOrdinalToGridOrder()
    {
        // Update Ordinal - Internal/External List
        for (int i = 0; i < entity.getAssignedPropertyTypes().size(); i++)
        {
            entity.getAssignedPropertyTypes().get(i).setOrdinal((long) i + 1);
            assigments.get(i).getAssignment().setOrdinal((long) i + 1);
        }
    }

    public void refreshOrderAdd(NewPTNewAssigment newAssigment) throws Exception
    {
        if (isAssigmentFound(newAssigment))
        {
            throw new Exception("A property can't be assigned twice.");
        }

        // Update Ordinal - Internal/External List
        updateOrdinalToDBOrder();
        int insertPos = newAssigment.getAssignment().getOrdinal().intValue();

        //
        // Update Lists - This Reorder the items due to an insert
        //

        // Internal List
        EntityTypePropertyType<?> newEtpt = getEntityTypePropertyType(entity.getEntityKind(), newAssigment);
        switch (entity.getEntityKind())
        {
            case SAMPLE:
                SampleType sampleType = (SampleType) entity;
                sampleType.getAssignedPropertyTypes().add(insertPos, (SampleTypePropertyType) newEtpt);
                break;
            case EXPERIMENT:
                ExperimentType experimentType = (ExperimentType) entity;
                experimentType.getAssignedPropertyTypes().add(insertPos, (ExperimentTypePropertyType) newEtpt);
                break;
            case DATA_SET:
                DataSetType datasetType = (DataSetType) entity;
                datasetType.getAssignedPropertyTypes().add(insertPos, (DataSetTypePropertyType) newEtpt);
                break;
            case MATERIAL:
                MaterialType materialType = (MaterialType) entity;
                materialType.getAssignedPropertyTypes().add(insertPos, (MaterialTypePropertyType) newEtpt);
                break;
        }

        // External List
        assigments.add(insertPos, newAssigment);

        // Update Ordinal - Internal/External List
        updateOrdinalToGridOrder();
    }

    public void refreshOrderDelete(String code)
    {
        // Update Ordinal - Internal/External List
        updateOrdinalToDBOrder();

        //
        // Delete Code - Internal/External List
        //
        for (int i = 0; i < entity.getAssignedPropertyTypes().size(); i++)
        {
            if (entity.getAssignedPropertyTypes().get(i).getPropertyType().getCode().equals(code))
            {
                entity.getAssignedPropertyTypes().remove(i);
                assigments.remove(i);
                break;
            }
        }

        // Update Ordinal - Internal/External List
        updateOrdinalToGridOrder();
    }

    public boolean isAssigmentFound(NewPTNewAssigment assigment)
    {
        for (NewPTNewAssigment assigmentFound : assigments)
        {
            if (assigmentFound.getPropertyType().getCode().equals(assigment.getPropertyType().getCode()))
            {
                return true;
            }
        }
        return false;
    }

    public static EntityTypePropertyType<?> getEntityTypePropertyType(EntityKind kind, NewPTNewAssigment propertyTypeAsg)
    {
        EntityTypePropertyType<?> etpt = null;
        switch (kind)
        {
            case SAMPLE:
                etpt = new SampleTypePropertyType();
                break;
            case EXPERIMENT:
                etpt = new ExperimentTypePropertyType();
                break;
            case DATA_SET:
                etpt = new DataSetTypePropertyType();
                break;
            case MATERIAL:
                etpt = new MaterialTypePropertyType();
                break;
        }
        etpt.setPropertyType(propertyTypeAsg.getPropertyType());
        etpt.setOrdinal(propertyTypeAsg.getAssignment().getOrdinal());
        etpt.setSection(propertyTypeAsg.getAssignment().getSection());
        etpt.setMandatory(propertyTypeAsg.getAssignment().isMandatory());
        etpt.setDynamic(propertyTypeAsg.getAssignment().isDynamic());
        etpt.setManaged(propertyTypeAsg.getAssignment().isManaged());
        etpt.setShownInEditView(propertyTypeAsg.getAssignment().isShownInEditView());
        etpt.setShowRawValue(propertyTypeAsg.getAssignment().getShowRawValue());
        etpt.setModificationDate(propertyTypeAsg.getAssignment().getModificationDate());

        if (propertyTypeAsg.getAssignment().getScriptName() != null)
        {
            Script scriptNew = new Script();
            scriptNew.setName(propertyTypeAsg.getAssignment().getScriptName());
            etpt.setScript(scriptNew);
        }

        return etpt;
    }
}
