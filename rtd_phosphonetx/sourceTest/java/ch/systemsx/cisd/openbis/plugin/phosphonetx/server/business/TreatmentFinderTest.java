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

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
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
        SamplePE sample = new SamplePE();
        sample.addProperty(create("BLABLA", "blub", EntityDataType.MULTILINE_VARCHAR));
        addTreatment(sample, "", "pH", "7");
        addTreatment(sample, "1", "PLASMA", "20");
        addTreatment(sample, "2", "VIRUS", "HIV", EntityDataType.MATERIAL);
        
        List<Treatment> treatments = new TreatmentFinder().findTreatmentsOf(sample);
        
        assertEquals(3, treatments.size());
        assertTreatment("7", "pH", treatments.get(0));
        assertTreatment("20", "PLASMA", treatments.get(1));
        assertTreatment("HIV", "VIRUS", EntityDataType.MATERIAL, treatments.get(2));
    }

    @Test
    public void testFindingTreatmentsInAncestorsOverriddenInDescendants()
    {
        SamplePE sample = new SamplePE();
        SamplePE parentSample = new SamplePE();
        sample.setGeneratedFrom(parentSample);
        SamplePE grandParentSample = new SamplePE();
        parentSample.setGeneratedFrom(grandParentSample);
        addTreatment(grandParentSample, "1", "PLASMA", "35");
        parentSample.addProperty(create("BLABLA", "blub", EntityDataType.MULTILINE_VARCHAR));
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
        SamplePE sample = new SamplePE();
        sample.addProperty(create(TREATMENT_TYPE_CODE, "pH", EntityDataType.VARCHAR));

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
        SamplePE sample = new SamplePE();
        sample.addProperty(create(TREATMENT_TYPE_CODE, "pH", EntityDataType.CONTROLLEDVOCABULARY));
        
        List<Treatment> treatments = new TreatmentFinder().findTreatmentsOf(sample);
        
        assertEquals(1, treatments.size());
        assertTreatment("", "pH", treatments.get(0));
    }
    
    @Test
    public void testFindingTreatmentsWhereOnlyTreatmentValueIsDefined()
    {
        SamplePE sample = new SamplePE();
        sample.addProperty(create(TREATMENT_VALUE_CODE, "HIV", EntityDataType.MATERIAL));
        
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
    
    private void addTreatment(SamplePE sample, String treatmentCodePostfix, String treatmentType,
            String treatmentValue)
    {
        addTreatment(sample, treatmentCodePostfix, treatmentType, treatmentValue,
                EntityDataType.VARCHAR);
    }

    private void addTreatment(SamplePE sample, String treatmentCodePostfix, String treatmentType,
            String treatmentValue, EntityDataType valueType)
    {
        sample.addProperty(create(TREATMENT_TYPE_CODE + treatmentCodePostfix, treatmentType,
                EntityDataType.CONTROLLEDVOCABULARY));
        sample.addProperty(create(TREATMENT_VALUE_CODE + treatmentCodePostfix, treatmentValue,
                valueType));
    }

    private SamplePropertyPE create(String code, String value, EntityDataType type)
    {
        SamplePropertyPE sampleProperty = new SamplePropertyPE();
        SampleTypePropertyTypePE sampleTypePropertyType = new SampleTypePropertyTypePE();
        sampleTypePropertyType.setEntityType(new SampleTypePE());
        PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setSimpleCode(code);
        DataTypePE dataType = new DataTypePE();
        dataType.setCode(type);
        propertyType.setType(dataType);
        sampleTypePropertyType.setPropertyType(propertyType);
        sampleProperty.setEntityTypePropertyType(sampleTypePropertyType);
        switch (type)
        {
            case CONTROLLEDVOCABULARY:
                VocabularyTermPE term = new VocabularyTermPE();
                if (Character.isUpperCase(value.charAt(0)))
                {
                    term.setCode(value);
                } else
                {
                    term.setCode(value.toUpperCase());
                    term.setLabel(value);
                }
                sampleProperty.setUntypedValue(null, term, null);
                break;
            case MATERIAL:
                MaterialPE material = new MaterialPE();
                material.setCode(value);
                sampleProperty.setUntypedValue(null, null, material);
                break;
            default:
                sampleProperty.setUntypedValue(value, null, null);
        }
        return sampleProperty;
    }
}
