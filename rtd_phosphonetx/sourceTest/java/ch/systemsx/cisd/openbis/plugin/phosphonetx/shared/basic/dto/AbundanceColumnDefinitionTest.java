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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto;

import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AbundanceColumnDefinitionTest extends AssertJUnit
{
    @Test
    public void testCompareTwoDefinitonsWithoutTreatmentsAndSampleCodes()
    {
        AbundanceColumnDefinition d1 = new AbundanceColumnDefinition();
        AbundanceColumnDefinition d2 = create(null);
        
        assertEquals(0, d1.compareTo(d2));
        
        d1.setSampleCode("bla");
        
        assertEquals(0, d1.compareTo(d2));
    }
    
    @Test
    public void testCompareTwoDefinitonsWithoutTreatmentsButSampleCodes()
    {
        AbundanceColumnDefinition d1 = create("abc");
        AbundanceColumnDefinition d2 = create("abc");
        
        assertEquals(0, d1.compareTo(d2));
        
        d2.setSampleCode("def");
        
        assertEquals(true, d1.compareTo(d2) < 0);
    }
    
    @Test
    public void testCompareTwoDefinitonsWithDifferentNumberOfTreatments()
    {
        AbundanceColumnDefinition d1 = create("abc");
        d1.setSampleCode("abc");
        AbundanceColumnDefinition d2 = create("abc", new Treatment());
        d2.setSampleCode("abc");
        
        assertEquals(true, d1.compareTo(d2) < 0);
    }
    
    @Test
    public void testCompareTwoDefinitonsWithOneNonNumericalTreatment()
    {
        AbundanceColumnDefinition d1 = create("abc", treatment("light", "red"));
        AbundanceColumnDefinition d2 = create("abc", treatment("light", "blue"));
        
        assertEquals(true, d1.compareTo(d2) > 0);
    }
    
    @Test
    public void testCompareTwoDefinitonsWithOneNumericalTreatment()
    {
        AbundanceColumnDefinition d1 = create("abc", treatment("pH", "7.5"), treatment("T", "yes"));
        AbundanceColumnDefinition d2 = create("abc", treatment("pH", "9e-1"));
        
        assertEquals(true, d1.compareTo(d2) > 0);
    }
    
    @Test
    public void testCompareTwoDefinitonsWithOneNumericalTreatmentButDifferentTypes()
    {
        AbundanceColumnDefinition d1 = create("abc", treatment("K", "7.5"));
        AbundanceColumnDefinition d2 = create("abc", treatment("pH", "9e-1"));
        
        assertEquals(true, d1.compareTo(d2) < 0);
    }
    
    @Test
    public void testCompareTwoDefinitonsWithTwoNumericalTreatment()
    {
        AbundanceColumnDefinition d1 = create("abc", treatment("pH", "7.50"), treatment("T", "13"));
        AbundanceColumnDefinition d2 = create("abc", treatment("pH", "0.75e1"), treatment("T", "8"));
        
        assertEquals(true, d1.compareTo(d2) > 0);
    }
    
    private AbundanceColumnDefinition create(String sampleCode, Treatment... treatments)
    {
        AbundanceColumnDefinition definition = new AbundanceColumnDefinition();
        definition.setSampleCode(sampleCode);
        definition.setTreatments(Arrays.asList(treatments));
        return definition;
    }
    
    private Treatment treatment(String type, String value)
    {
        Treatment treatment = new Treatment();
        treatment.setType(type);
        treatment.setValue(value);
        return treatment;
    }
}
