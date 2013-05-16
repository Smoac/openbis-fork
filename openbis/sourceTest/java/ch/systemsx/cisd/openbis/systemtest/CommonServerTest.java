/*
 * Copyright 2013 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;

/**
 * @author Franz-Josef Elmer
 */
public class CommonServerTest extends SystemTestCase
{
    @Test
    public void testGetSampleWithAssignedPropertyTypesAndProperties()
    {
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(1)).getParent();

        assertEquals("/CISD/CL1", sample.getIdentifier());
        EntityType entityType = sample.getEntityType();
        assertEquals("CONTROL_LAYOUT", entityType.getCode());
        assertAssignedPropertyTypes("[$PLATE_GEOMETRY*, DESCRIPTION]", entityType);
        assertProperties("[$PLATE_GEOMETRY: 384_WELLS_16X24, DESCRIPTION: test control layout]",
                sample);
    }

    @Test
    public void testGetExperimentWithAssignedPropertyTypesAndProperties()
    {
        Experiment experiment = commonServer.getExperimentInfo(systemSessionToken, new TechId(2));

        assertEquals("/CISD/NEMO/EXP1", experiment.getIdentifier());
        EntityType entityType = experiment.getEntityType();
        assertEquals("SIRNA_HCS", entityType.getCode());
        assertAssignedPropertyTypes("[DESCRIPTION*, GENDER, PURCHASE_DATE]", entityType);
        assertProperties("[DESCRIPTION: A simple experiment, GENDER: MALE]", experiment);
    }

    @Test
    public void testGetExperimentWithAttachments()
    {
        Experiment experiment = commonServer.getExperimentInfo(systemSessionToken, new TechId(2));

        assertEquals("/CISD/NEMO/EXP1", experiment.getIdentifier());
        List<Attachment> attachments = experiment.getAttachments();
        assertEquals("exampleExperiments.txt", attachments.get(0).getFileName());
        assertEquals(4, attachments.size());
    }

    @Test
    public void testGetDataSetWithAssignedPropertyTypesAndProperties()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(14));

        assertEquals("20110509092359990-11", dataSet.getCode());
        DataSetType dataSetType = dataSet.getDataSetType();
        assertEquals("HCS_IMAGE", dataSetType.getCode());
        assertAssignedPropertyTypes("[ANY_MATERIAL, BACTERIUM, COMMENT*, GENDER]", dataSetType);
        assertEquals("[COMMENT: non-virtual comment]", dataSet.getProperties().toString());
        assertEquals("/CISD/DEFAULT/EXP-REUSE", dataSet.getExperiment().getIdentifier());
    }

    @Test
    public void testGetContainerDataSetWithContainedDataSets()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(13));

        assertEquals("20110509092359990-10", dataSet.getCode());
        assertEquals(true, dataSet.isContainer());
        ContainerDataSet containerDataSet = dataSet.tryGetAsContainerDataSet();
        List<AbstractExternalData> containedDataSets = containerDataSet.getContainedDataSets();
        assertEntities("[20110509092359990-11, 20110509092359990-12]", containedDataSets);
    }

    @Test
    public void testGetDataSetWithChildrenAndParents()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(10));

        assertEquals("20081105092259900-0", dataSet.getCode());
        assertEntities("[20081105092359990-2]", dataSet.getChildren());
        assertEntities("[20081105092259000-9]", new ArrayList<AbstractExternalData>(dataSet.getParents()));
    }

    @Test
    public void testGetDataSetWithSample()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(5));

        assertEquals("20081105092159111-1", dataSet.getCode());
        assertEquals("/CISD/CP-TEST-1", dataSet.getSampleIdentifier());
    }

    private void assertAssignedPropertyTypes(String expected, EntityType entityType)
    {
        List<? extends EntityTypePropertyType<?>> propTypes = entityType.getAssignedPropertyTypes();
        List<String> propertyCodes = new ArrayList<String>();
        for (EntityTypePropertyType<?> entityTypePropertyType : propTypes)
        {
            String code = entityTypePropertyType.getPropertyType().getCode();
            if (entityTypePropertyType.isMandatory())
            {
                code = code + "*";
            }
            propertyCodes.add(code);
        }
        Collections.sort(propertyCodes);
        assertEquals(expected, propertyCodes.toString());
    }

    @Test
    public void testRegisterAndAssignPropertyType()
    {
        // Entity Type
        final String entityTypeCode = "TEST_ENTITY_TYPE_CODE_" + System.currentTimeMillis();
        final EntityKind entityKind = EntityKind.EXPERIMENT;

        ExperimentType entityType = new ExperimentType();
        entityType.setCode(entityTypeCode);

        // Property Type
        final String propertyTypeCode = "TEST_PROPERTY_TYPE_CODE_" + System.currentTimeMillis();
        final String propertyTypeLabel = "TEST_PROPERTY_TYPE_LABEL_" + System.currentTimeMillis();
        final String propertyTypeDescription = "TEST_PROPERTY_TYPE_DESCRIPTION_" + System.currentTimeMillis();
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.INTEGER);

        PropertyType newPropertyType = new PropertyType();
        newPropertyType.setCode(propertyTypeCode);
        newPropertyType.setLabel(propertyTypeLabel);
        newPropertyType.setDescription(propertyTypeDescription);
        newPropertyType.setDataType(dataType);

        // Assigment
        NewETPTAssignment newETPTAssigment = new NewETPTAssignment();
        newETPTAssigment.setEntityKind(entityKind);
        newETPTAssigment.setPropertyTypeCode(propertyTypeCode);
        newETPTAssigment.setEntityTypeCode(entityTypeCode);
        newETPTAssigment.setOrdinal(0L);

        // Call
        commonServer.registerExperimentType(systemSessionToken, entityType);
        commonServer.registerAndAssignPropertyType(systemSessionToken, newPropertyType, newETPTAssigment);

        // Validation
        List<EntityTypePropertyType<?>> listAssigments = commonServer.listEntityTypePropertyTypes(systemSessionToken, entityType);

        assertEquals(newPropertyType, listAssigments.get(0).getPropertyType());
    }

    @Test
    public void testRegisterEntitytypeAndAssignPropertyTypes() throws Exception
    {
        for (EntityKind entityKind : EntityKind.values())
        {
            // Entity Type
            final String entityTypeCode = "TEST_ENTITY_TYPE_CODE_" + entityKind.name();
            EntityType entityType = null;

            switch (entityKind)
            {
                case EXPERIMENT:
                    entityType = new ExperimentType();
                    ((ExperimentType) entityType).setExperimentTypePropertyTypes(new ArrayList<ExperimentTypePropertyType>());
                    break;
                case DATA_SET:
                    entityType = new DataSetType();
                    ((DataSetType) entityType).setDataSetKind(DataSetKind.PHYSICAL);
                    ((DataSetType) entityType).setDataSetTypePropertyTypes(new ArrayList<DataSetTypePropertyType>());
                    break;
                case MATERIAL:
                    entityType = new MaterialType();
                    ((MaterialType) entityType).setMaterialTypePropertyTypes(new ArrayList<MaterialTypePropertyType>());
                    break;
                case SAMPLE:
                    entityType = new SampleType();
                    ((SampleType) entityType).setGeneratedCodePrefix("TEST");
                    ((SampleType) entityType).setSampleTypePropertyTypes(new ArrayList<SampleTypePropertyType>());
                    break;
            }
            entityType.setCode(entityTypeCode);

            // Complete Assignments Object
            NewETNewPTAssigments assigments = new NewETNewPTAssigments();
            assigments.setEntity(entityType);
            assigments.setAssigments(new ArrayList<NewPTNewAssigment>());

            Random random = new Random();
            final int numberOfProperties = random.nextInt(20) + 1;
            for (int i = 0; i < numberOfProperties; i++)
            {
                // Property Type
                final String propertyTypeCode = "TEST_PROPERTY_TYPE_CODE_" + random.nextInt();
                final String propertyTypeLabel = "TEST_PROPERTY_TYPE_LABEL_" + random.nextInt();
                final String propertyTypeDescription = "TEST_PROPERTY_TYPE_DESCRIPTION_" + random.nextInt();
                final DataType dataType = new DataType();
                dataType.setCode(DataTypeCode.INTEGER);

                PropertyType newPropertyType = new PropertyType();
                newPropertyType.setCode(propertyTypeCode);
                newPropertyType.setLabel(propertyTypeLabel);
                newPropertyType.setDescription(propertyTypeDescription);
                newPropertyType.setDataType(dataType);

                // New Assignments
                NewETPTAssignment newETPTAssigment = new NewETPTAssignment();
                newETPTAssigment.setEntityKind(entityKind);
                newETPTAssigment.setPropertyTypeCode(propertyTypeCode);
                newETPTAssigment.setEntityTypeCode(entityTypeCode);
                newETPTAssigment.setOrdinal((long) assigments.getAssigments().size());

                NewPTNewAssigment newPTNewAssigment = new NewPTNewAssigment();
                newPTNewAssigment.setExistingPropertyType(false);
                newPTNewAssigment.setPropertyType(newPropertyType);
                newPTNewAssigment.setAssignment(newETPTAssigment);

                assigments.refreshOrderAdd(newPTNewAssigment);
            }

            // Call
            commonServer.registerEntitytypeAndAssignPropertyTypes(systemSessionToken, assigments);

            // Validation
            List<EntityTypePropertyType<?>> listAssigments = commonServer.listEntityTypePropertyTypes(systemSessionToken, entityType);
            for (int i = 0; i < assigments.getAssigments().size(); i++)
            {
                assertEquals(assigments.getAssigments().get(i).getPropertyType(), listAssigments.get(i).getPropertyType());
            }
        }
    }

    @Test
    public void testUpdateEntitytypeAndPropertyTypes() throws Exception
    {
        testRegisterEntitytypeAndAssignPropertyTypes();
        for (EntityKind entityKind : EntityKind.values())
        {
            // Existing Entity Type
            final String entityTypeCode = "TEST_ENTITY_TYPE_CODE_" + entityKind.name();
            List<? extends EntityType> types = null;
            EntityType entityType = null;

            switch (entityKind)
            {
                case EXPERIMENT:
                    types = commonServer.listExperimentTypes(systemSessionToken);
                    break;
                case DATA_SET:
                    types = commonServer.listDataSetTypes(systemSessionToken);
                    break;
                case MATERIAL:
                    types = commonServer.listMaterialTypes(systemSessionToken);
                    break;
                case SAMPLE:
                    types = commonServer.listSampleTypes(systemSessionToken);
                    break;
            }

            for (EntityType type : types)
            {
                if (type.getCode().equals(entityTypeCode))
                {
                    entityType = type;
                }
            }

            // Existing Properties
            List<EntityTypePropertyType<?>> listAssigmentsOld = commonServer.listEntityTypePropertyTypes(systemSessionToken, entityType);
            List listAssigmentsOldHack = listAssigmentsOld;
            switch (entityKind)
            {
                case EXPERIMENT:
                    ((ExperimentType) entityType).setExperimentTypePropertyTypes(listAssigmentsOldHack);
                    break;
                case DATA_SET:
                    ((DataSetType) entityType).setDataSetTypePropertyTypes(listAssigmentsOldHack);
                    break;
                case MATERIAL:
                    ((MaterialType) entityType).setMaterialTypePropertyTypes(listAssigmentsOldHack);
                    break;
                case SAMPLE:
                    ((SampleType) entityType).setSampleTypePropertyTypes(listAssigmentsOldHack);
                    break;
            }

            // Complete Assignments Object
            NewETNewPTAssigments assigments = new NewETNewPTAssigments();
            assigments.setEntity(entityType);
            assigments.setAssigments(new ArrayList<NewPTNewAssigment>());

            // Existing Assignments
            for (int i = 0; i < entityType.getAssignedPropertyTypes().size(); i++)
            {
                NewETPTAssignment oldETPTAssigment = new NewETPTAssignment();
                oldETPTAssigment.setEntityKind(entityType.getAssignedPropertyTypes().get(i).getEntityKind());
                oldETPTAssigment.setPropertyTypeCode(entityType.getAssignedPropertyTypes().get(i).getPropertyType().getCode());
                oldETPTAssigment.setEntityTypeCode(entityType.getAssignedPropertyTypes().get(i).getEntityType().getCode());
                oldETPTAssigment.setOrdinal((long) entityType.getAssignedPropertyTypes().get(i).getOrdinal());
                oldETPTAssigment.setModificationDate(entityType.getAssignedPropertyTypes().get(i).getModificationDate());

                NewPTNewAssigment oldPTNewAssigment = new NewPTNewAssigment();
                oldPTNewAssigment.setExistingPropertyType(true);
                oldPTNewAssigment.setPropertyType(entityType.getAssignedPropertyTypes().get(i).getPropertyType());
                oldPTNewAssigment.setAssignment(oldETPTAssigment);

                assigments.getAssigments().add(oldPTNewAssigment);
            }

            // New Assignments
            Random random = new Random();
            final int numberOfProperties = random.nextInt(30) + 1;
            for (int i = 0; i < numberOfProperties; i++)
            {
                switch (random.nextInt(3))
                {
                    case 0: // Insert
                        final String propertyTypeCode = "TEST_PROPERTY_TYPE_CODE_" + random.nextInt();
                        final String propertyTypeLabel = "TEST_PROPERTY_TYPE_LABEL_" + random.nextInt();
                        final String propertyTypeDescription = "TEST_PROPERTY_TYPE_DESCRIPTION_" + random.nextInt();
                        final DataType dataType = new DataType();
                        dataType.setCode(DataTypeCode.INTEGER);

                        PropertyType newPropertyType = new PropertyType();
                        newPropertyType.setCode(propertyTypeCode);
                        newPropertyType.setLabel(propertyTypeLabel);
                        newPropertyType.setDescription(propertyTypeDescription);
                        newPropertyType.setDataType(dataType);

                        NewETPTAssignment newETPTAssigment = new NewETPTAssignment();
                        newETPTAssigment.setEntityKind(entityKind);
                        newETPTAssigment.setPropertyTypeCode(propertyTypeCode);
                        newETPTAssigment.setEntityTypeCode(entityTypeCode);
                        if (assigments.getAssigments().size() > 0)
                        {
                            newETPTAssigment.setOrdinal((long) random.nextInt(assigments.getAssigments().size()));
                        } else
                        {
                            newETPTAssigment.setOrdinal(0L);
                        }
                        newETPTAssigment.setModificationDate(new Date());

                        NewPTNewAssigment newPTNewassignment = new NewPTNewAssigment();
                        newPTNewassignment.setExistingPropertyType(false);
                        newPTNewassignment.setPropertyType(newPropertyType);
                        newPTNewassignment.setAssignment(newETPTAssigment);

                        assigments.refreshOrderAdd(newPTNewassignment);
                        break;
                    case 1: // Modification
                        if (assigments.getAssigments().size() > 0)
                        {
                            int posToModify = random.nextInt(assigments.getAssigments().size()); // Random Position
                            NewETPTAssignment toModify = assigments.getAssigments().get(posToModify).getAssignment();
                            int toNewPos = random.nextInt(assigments.getAssigments().size()); // Random Position
                            toModify.setOrdinal((long) toNewPos);
                            assigments.refreshOrderUpdate(toModify);
                        }
                        break;
                    case 2: // Delete
                        if (assigments.getAssigments().size() > 0)
                        {
                            int posToDelete = random.nextInt(assigments.getAssigments().size()); // Random Position
                            assigments.refreshOrderDelete(assigments.getAssigments().get(posToDelete).getPropertyType().getCode());
                        }
                        break;
                }
            }

            // Call
            commonServer.updateEntitytypeAndPropertyTypes(systemSessionToken, assigments);

            // Validation
            List<EntityTypePropertyType<?>> listAssigments = commonServer.listEntityTypePropertyTypes(systemSessionToken, entityType);

            assertEquals(assigments.getAssigments().size(), listAssigments.size());
            for (int i = 0; i < assigments.getAssigments().size(); i++)
            {
                assertEquals(assigments.getAssigments().get(i).getPropertyType(), listAssigments.get(i).getPropertyType());
            }
        }
    }

}
