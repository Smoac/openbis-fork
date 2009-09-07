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

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
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
    private ISampleIDProvider sampleIDProvider;
    private AbundanceColumnDefinitionTable table;
    
    @Override
    @BeforeMethod
    public void setUp()
    {
        super.setUp();
        phosphoNetXDAOFactory = context.mock(IPhosphoNetXDAOFactory.class);
        sampleIDProvider = context.mock(ISampleIDProvider.class);
        table =
                new AbundanceColumnDefinitionTable(daoFactory, phosphoNetXDAOFactory,
                        sampleIDProvider, SESSION);
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
        SamplePE s1 = createSample(42);
        prepareGetSample(s1);
        
        table.add(s1.getPermId());
        List<AbundanceColumnDefinition> definitions = table.getSortedAndAggregatedDefinitions(null);
        
        assertEquals(1, definitions.size());
        assertEquals(42l, definitions.get(0).getSampleID());
        assertEquals("code-42", definitions.get(0).getSampleCode());
        assertEquals(0, definitions.get(0).getTreatments().size());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testOneAbundanceColumnWithOneTreatment()
    {
        SamplePE s1 = createSample(42);
        addTreatment(s1, "", "abc");
        prepareGetSample(s1);
        
        table.add(s1.getPermId());
        List<AbundanceColumnDefinition> definitions = table.getSortedAndAggregatedDefinitions(null);
        
        assertEquals(1, definitions.size());
        assertEquals(42l, definitions.get(0).getSampleID());
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
        SamplePE s1 = createSample(42);
        addTreatment(s1, "", "abc");
        prepareGetSample(s1);
        prepareGetSample("abc2", s1.getId(), null);
        
        table.add(s1.getPermId());
        table.add("abc2");
        List<AbundanceColumnDefinition> definitions = table.getSortedAndAggregatedDefinitions(null);
        
        assertEquals(1, definitions.size());
        assertEquals(42l, definitions.get(0).getSampleID());
        assertEquals("code-42", definitions.get(0).getSampleCode());
        assertEquals(1, definitions.get(0).getTreatments().size());
        assertEquals("pH", definitions.get(0).getTreatments().get(0).getType());
        assertEquals("PH", definitions.get(0).getTreatments().get(0).getTypeCode());
        assertEquals("abc", definitions.get(0).getTreatments().get(0).getValue());
        
        context.assertIsSatisfied();
    }
    
    private SamplePE createSample(long sampleID)
    {
        SamplePE samplePE = new SamplePE();
        samplePE.setId(sampleID);
        samplePE.setCode("code-" + sampleID);
        samplePE.setPermId("abc-" + sampleID);
        return samplePE;
    }
    
    private void addTreatment(SamplePE sample, String type, String value)
    {
        SamplePropertyPE sampleProperty = new SamplePropertyPE();
        sampleProperty.setEntityTypePropertyType(createSTPT(TREATMENT_TYPE_CODE + type));
        VocabularyTermPE term = new VocabularyTermPE();
        term.setCode("PH");
        term.setLabel("pH");
        sampleProperty.setUntypedValue(null, term, null);
        sample.addProperty(sampleProperty);

        sampleProperty = new SamplePropertyPE();
        sampleProperty.setEntityTypePropertyType(createSTPT(TREATMENT_VALUE_CODE + type));
        sampleProperty.setUntypedValue(value, null, null);
        sample.addProperty(sampleProperty);
    }

    private SampleTypePropertyTypePE createSTPT(String code)
    {
        SampleTypePropertyTypePE stpt = new SampleTypePropertyTypePE();
        PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setSimpleCode(code);
        DataTypePE dataType = new DataTypePE();
        dataType.setCode(EntityDataType.VARCHAR);
        propertyType.setType(dataType);
        stpt.setPropertyType(propertyType);
        return stpt;
    }
   
    private void prepareGetSample(final SamplePE sample)
    {
        prepareGetSample(sample.getPermId(), sample.getId(), sample);
    }
    
    private void prepareGetSample(final String samplePermID, final long sampleID, final SamplePE sampleOrNull)
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleIDProvider).getSampleIDOrParentSampleID(samplePermID);
                    will(returnValue(sampleID));
                    
                    if (sampleOrNull != null)
                    {
                        one(sampleDAO).getByTechId(new TechId(sampleID));
                        will(returnValue(sampleOrNull));
                    }
                }
            });
    }
}
