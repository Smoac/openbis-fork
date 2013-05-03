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
    

    public void refreshOrder() {
//        //
//        // Get List
//        //
//        List<? extends EntityTypePropertyType<?>> list = entity.getAssignedPropertyTypes();
//        
//        switch (entity.getEntityKind())
//        {
//            case SAMPLE:
//                SampleType sampleType = (SampleType) entity;
//                if(entity.getAssignedPropertyTypes() == null) {
//                    sampleType.setSampleTypePropertyTypes(new ArrayList<SampleTypePropertyType>());
//                }
//                entity.getAssignedPropertyTypes().clear();
//                break;
//            case EXPERIMENT:
//                ExperimentType experimentType = (ExperimentType) entity;
//                if(entity.getAssignedPropertyTypes() == null) {
//                    experimentType.setExperimentTypePropertyTypes(new ArrayList<ExperimentTypePropertyType>());
//                }
//                entity.getAssignedPropertyTypes().clear();
//                break;
//            case DATA_SET:
//                DataSetType datasetType = (DataSetType) entity;
//                if(entity.getAssignedPropertyTypes() == null) {
//                    datasetType.setDataSetTypePropertyTypes(new ArrayList<DataSetTypePropertyType>());
//                }
//                entity.getAssignedPropertyTypes().clear();
//                break;
//            case MATERIAL:
//                MaterialType materialType = (MaterialType) entity;
//                if(entity.getAssignedPropertyTypes() == null) {
//                    materialType.setMaterialTypePropertyTypes(new ArrayList<MaterialTypePropertyType>());
//                }
//                entity.getAssignedPropertyTypes().clear();
//                break;
//        }
//        
//        //
//        // Update List
//        //
//        for(int i = 0; i < assigments.size(); i++) {
//            NewPTNewAssigment assigment = assigments.get(i);
//            int insertPos = -1;
//            EntityTypePropertyType<?> etpt = getEntityTypePropertyType(entity.getEntityKind(), assigment);
//            
//            if(assigment.getAssignment().getOrdinal() == 0) {
//                insertPos = 0;
//            } else if (assigment.getAssignment().getOrdinal() != 0) {
//                insertPos = i;
//            }
//            etpt.setOrdinal(insertPos+1L);
//            
//            switch (entity.getEntityKind())
//            {
//                case SAMPLE:
//                    SampleType sampleType = (SampleType) entity;
//                    sampleType.getAssignedPropertyTypes().add(insertPos, (SampleTypePropertyType) etpt);
//                    break;
//                case EXPERIMENT:
//                    ExperimentType experimentType = (ExperimentType) entity;
//                    experimentType.getAssignedPropertyTypes().add(insertPos, (ExperimentTypePropertyType) etpt);
//                    break;
//                case DATA_SET:
//                    DataSetType datasetType = (DataSetType) entity;
//                    datasetType.getAssignedPropertyTypes().add(insertPos, (DataSetTypePropertyType) etpt);
//                    break;
//                case MATERIAL:
//                    MaterialType materialType = (MaterialType) entity;
//                    materialType.getAssignedPropertyTypes().add(insertPos, (MaterialTypePropertyType) etpt);
//                    break;
//            }
//        }
    }
    
    public static EntityTypePropertyType<?> getEntityTypePropertyType(EntityKind kind, NewPTNewAssigment propertyTypeAsg) {
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
        etpt.setOrdinal(propertyTypeAsg.getAssignment().getOrdinal()); //TO-DO Sort list by order
        etpt.setSection(propertyTypeAsg.getAssignment().getSection());
        etpt.setMandatory(propertyTypeAsg.getAssignment().isMandatory());
        etpt.setDynamic(propertyTypeAsg.getAssignment().isDynamic());
        etpt.setManaged(propertyTypeAsg.getAssignment().isManaged());
        etpt.setShownInEditView(propertyTypeAsg.getAssignment().isShownInEditView());
        etpt.setShowRawValue(propertyTypeAsg.getAssignment().getShowRawValue());
        
        if(propertyTypeAsg.getAssignment().getScriptName() != null) {
            Script scriptNew = new Script();
            scriptNew.setName(propertyTypeAsg.getAssignment().getScriptName());
            etpt.setScript(scriptNew);
        }
        
        return etpt;
    }
}
