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
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AbundanceColumnDefinitionTableTest extends AbstractServerTestCase
{
    private IPhosphoNetXDAOFactory phosphoNetXDAOFactory;
    private AbundanceColumnDefinitionTable table;
    
    @Override
    @BeforeMethod
    public void setUp()
    {
        super.setUp();
        phosphoNetXDAOFactory = context.mock(IPhosphoNetXDAOFactory.class);
        table = new AbundanceColumnDefinitionTable(daoFactory, phosphoNetXDAOFactory, SESSION);
    }
    
    @Test
    public void testNoAbundanceColumns()
    {
        List<AbundanceColumnDefinition> definitions = table.getSortedAndAggregatedDefinitions(null);
        
        assertEquals(0, definitions.size());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testOneAbundanceColumnWithoutTreatment()
    {
        Sample s1 = createSample(42);
        
        table.add(s1);
        List<AbundanceColumnDefinition> definitions = table.getSortedAndAggregatedDefinitions(null);
        
        assertEquals(1, definitions.size());
        assertEquals(42l, definitions.get(0).getID());
        assertEquals("code-42", definitions.get(0).getSampleCode());
        assertEquals(0, definitions.get(0).getTreatments().size());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testOneAbundanceColumnWithOneTreatment()
    {
        Sample s1 = createSample(42);
        addTreatment(s1, "", "abc");
        
        table.add(s1);
        List<AbundanceColumnDefinition> definitions = table.getSortedAndAggregatedDefinitions(null);
        
        assertEquals(1, definitions.size());
        assertEquals(42l, definitions.get(0).getID());
        assertEquals("code-42", definitions.get(0).getSampleCode());
        assertEquals(1, definitions.get(0).getTreatments().size());
        assertEquals("pH", definitions.get(0).getTreatments().get(0).getType());
        assertEquals("PH", definitions.get(0).getTreatments().get(0).getTypeCode());
        assertEquals("abc", definitions.get(0).getTreatments().get(0).getValue());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAbundanceColumnsForTwoSamplesWithSameParent()
    {
        Sample s1 = createSample(42);
        Sample s2 = createSample(43);
        Sample parent = createSample(4711);
        s1.setGeneratedFrom(parent);
        s2.setGeneratedFrom(parent);
        addTreatment(parent, "", "abc");
        
        table.add(s1);
        table.add(s2);
        List<AbundanceColumnDefinition> definitions = table.getSortedAndAggregatedDefinitions(null);
        
        assertEquals(1, definitions.size());
        assertEquals(4711l, definitions.get(0).getID());
        assertEquals("code-4711", definitions.get(0).getSampleCode());
        assertEquals(1, definitions.get(0).getTreatments().size());
        assertEquals("pH", definitions.get(0).getTreatments().get(0).getType());
        assertEquals("PH", definitions.get(0).getTreatments().get(0).getTypeCode());
        assertEquals("abc", definitions.get(0).getTreatments().get(0).getValue());
        
        context.assertIsSatisfied();
    }
    
    private Sample createSample(long sampleID)
    {
        Sample sample = new Sample();
        sample.setId(sampleID);
        sample.setCode("code-" + sampleID);
        sample.setPermId("abc-" + sampleID);
        sample.setProperties(Collections.<IEntityProperty>emptyList());
        return sample;
    }
    
    private void addTreatment(Sample sample, String type, String value)
    {
        List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        EntityProperty sampleProperty = new EntityProperty();
        sampleProperty.setPropertyType(createPropertyType(TREATMENT_TYPE_CODE + type));
        VocabularyTerm term = new VocabularyTerm();
        term.setCode("PH");
        term.setLabel("pH");
        sampleProperty.setVocabularyTerm(term);
        properties.add(sampleProperty);

        sampleProperty = new EntityProperty();
        sampleProperty.setPropertyType(createPropertyType(TREATMENT_VALUE_CODE + type));
        sampleProperty.setValue(value);
        properties.add(sampleProperty);
        
        sample.setProperties(properties);
    }

    private PropertyType createPropertyType(String code)
    {
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(code);
        DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.VARCHAR);
        propertyType.setDataType(dataType);
        return propertyType;
    }
   
}
