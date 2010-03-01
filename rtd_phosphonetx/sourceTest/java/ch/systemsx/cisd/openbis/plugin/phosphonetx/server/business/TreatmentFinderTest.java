/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.TreatmentFinder.TREATMENT_TYPE_CODE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.TreatmentFinder.TREATMENT_VALUE_CODE;

import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Treatment;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TreatmentFinderTest extends AssertJUnit
{
    @Test
    public void testFindingTreatmentsInSample()
    {
        Sample sample = createSample();
        sample.getProperties().add(create("BLABLA", "blub", DataTypeCode.MULTILINE_VARCHAR));
        addTreatment(sample, "", "pH", "7");
        addTreatment(sample, "1", "PLASMA", "20");
        addTreatment(sample, "2", "VIRUS", "HIV", DataTypeCode.MATERIAL);
        
        List<Treatment> treatments = new TreatmentFinder().findTreatmentsOf(sample);
        
        assertEquals(3, treatments.size());
        assertTreatment("7", "pH", treatments.get(0));
        assertTreatment("20", "PLASMA", treatments.get(1));
        assertTreatment("HIV", "VIRUS", EntityDataType.MATERIAL, treatments.get(2));
    }

    @Test
    public void testFindingTreatmentsInAncestorsOverriddenInDescendants()
    {
        Sample sample = createSample();
        Sample parentSample = createSample();
        sample.setGeneratedFrom(parentSample);
        Sample grandParentSample = createSample();
        parentSample.setGeneratedFrom(grandParentSample);
        addTreatment(grandParentSample, "1", "PLASMA", "35");
        parentSample.getProperties().add(create("BLABLA", "blub", DataTypeCode.MULTILINE_VARCHAR));
        addTreatment(parentSample, "", "pH", "7");
        addTreatment(sample, "", "PLASMA", "20");
        
        List<Treatment> treatments = new TreatmentFinder().findTreatmentsOf(sample);
        
        assertEquals(2, treatments.size());
        assertTreatment("7", "pH", treatments.get(0));
        assertTreatment("20", "PLASMA", treatments.get(1));
    }
    
    @Test
    public void testFindingTreatmentsWhereTreatmentTypeIsNotAVocabulary()
    {
        Sample sample = createSample();
        sample.getProperties().add(create(TREATMENT_TYPE_CODE, "pH", DataTypeCode.VARCHAR));

        try
        {
            new TreatmentFinder().findTreatmentsOf(sample);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Data type of property type '" + TREATMENT_TYPE_CODE
                    + "' must be a vocabulary.", e.getMessage());
        }
    }
    
    @Test
    public void testFindingTreatmentsWhereOnlyTreatmentTypeIsDefined()
    {
        Sample sample = createSample();
        sample.getProperties().add(create(TREATMENT_TYPE_CODE, "pH", DataTypeCode.CONTROLLEDVOCABULARY));
        
        List<Treatment> treatments = new TreatmentFinder().findTreatmentsOf(sample);
        
        assertEquals(1, treatments.size());
        assertTreatment("", "pH", treatments.get(0));
    }
    
    @Test
    public void testFindingTreatmentsWhereOnlyTreatmentValueIsDefined()
    {
        Sample sample = createSample();
        sample.getProperties().add(create(TREATMENT_VALUE_CODE, "HIV", DataTypeCode.MATERIAL));
        
        List<Treatment> treatments = new TreatmentFinder().findTreatmentsOf(sample);
        
        assertEquals(1, treatments.size());
        assertTreatment("HIV", "", EntityDataType.MATERIAL, treatments.get(0));
    }
    
    private void assertTreatment(String expectedValue, String expectedType, Treatment treatment)
    {
        assertTreatment(expectedValue, expectedType, EntityDataType.VARCHAR, treatment);
    }

    private void assertTreatment(String expectedValue, String expectedType,
            EntityDataType expectedDataType, Treatment treatment)
    {
        assertEquals("Actual treatment: " + treatment, expectedValue, treatment.getValue());
        assertEquals("Actual treatment: " + treatment, expectedDataType.toString(), treatment
                .getValueType());
        assertEquals("Actual treatment: " + treatment, expectedType, treatment.getType());
        assertEquals("Actual treatment: " + treatment, expectedValue + " " + expectedType,
                treatment.getLabel());
    }
    
    private void addTreatment(Sample sample, String treatmentCodePostfix, String treatmentType,
            String treatmentValue)
    {
        addTreatment(sample, treatmentCodePostfix, treatmentType, treatmentValue,
                DataTypeCode.VARCHAR);
    }

    private void addTreatment(Sample sample, String treatmentCodePostfix, String treatmentType,
            String treatmentValue, DataTypeCode valueType)
    {
        List<IEntityProperty> properties = sample.getProperties();
        properties.add(create(TREATMENT_TYPE_CODE + treatmentCodePostfix, treatmentType,
                DataTypeCode.CONTROLLEDVOCABULARY));
        properties.add(create(TREATMENT_VALUE_CODE + treatmentCodePostfix, treatmentValue,
                valueType));
    }

    private IEntityProperty create(String code, String value, DataTypeCode type)
    {
        EntityProperty sampleProperty = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(code);
        DataType dataType = new DataType();
        dataType.setCode(type);
        propertyType.setDataType(dataType);
        sampleProperty.setPropertyType(propertyType);
        switch (type)
        {
            case CONTROLLEDVOCABULARY:
                VocabularyTerm term = new VocabularyTerm();
                if (Character.isUpperCase(value.charAt(0)))
                {
                    term.setCode(value);
                } else
                {
                    term.setCode(value.toUpperCase());
                    term.setLabel(value);
                }
                sampleProperty.setVocabularyTerm(term);
                break;
            case MATERIAL:
                Material material = new Material();
                material.setCode(value);
                sampleProperty.setMaterial(material);
                break;
            default:
                sampleProperty.setValue(value);
        }
        return sampleProperty;
    }
    
    private Sample createSample()
    {
        Sample sample = new Sample();
        sample.setProperties(new ArrayList<IEntityProperty>());
        return sample;
    }
}
