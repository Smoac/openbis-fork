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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.TreatmentFinder.TREATMENT_TYPE_CODE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.TreatmentFinder.TREATMENT_VALUE_CODE;

import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.ISampleIDProvider;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class PhosphoNetXServerTest extends AbstractServerTestCase
{
    private static final TechId EXPERIMENT_ID = new TechId(42L);
    private static final String EXPERIMENT_PERM_ID = "e123-45";
    private static final String SAMPLE_PERM_ID = "s34-56";
    private static final long SAMPLE_ID = 4711;
    private static final String SAMPLE_CODE = "S42";
    
    private IPhosphoNetXDAOFactory phosphoNetXDAOFactory;
    private IBusinessObjectFactory boFactory;
    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;
    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;
    private IProteinQueryDAO proteinDAO;
    private PhosphoNetXServer server;
    private ISampleIDProvider sampleIDProvider;
    
    @Override
    @BeforeMethod
    public void setUp()
    {
        super.setUp();
        phosphoNetXDAOFactory = context.mock(IPhosphoNetXDAOFactory.class);
        boFactory = context.mock(IBusinessObjectFactory.class);
        sampleIDProvider = context.mock(ISampleIDProvider.class);
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
        proteinDAO = context.mock(IProteinQueryDAO.class);
        
        server =
                new PhosphoNetXServer(sessionManager, daoFactory, phosphoNetXDAOFactory, boFactory,
                        sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
        context.checking(new Expectations()
            {
                {
                    allowing(phosphoNetXDAOFactory).getProteinQueryDAO();
                    will(returnValue(proteinDAO));
                }
            });
    }

    @Test
    public void testGetNoAbundanceColumnDefinitions()
    {
        prepareGetSession();
        prepareGetExperimentPermID();
        MockDataSet<String> mockDataSet = new MockDataSet<String>();
        prepareListAbundanceRelatedSamples(mockDataSet);
        
        List<AbundanceColumnDefinition> definitions =
            server.getAbundanceColumnDefinitionsForProteinByExperiment(SESSION_TOKEN,
                    EXPERIMENT_ID);
        assertEquals(0, definitions.size());
        assertEquals(true, mockDataSet.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetOneAbundanceColumnDefinitionWithoutTreatments()
    {
        prepareGetSession();
        prepareGetExperimentPermID();
        MockDataSet<String> mockDataSet = new MockDataSet<String>();
        mockDataSet.add(SAMPLE_PERM_ID);
        prepareListAbundanceRelatedSamples(mockDataSet);
        prepareFindSample(createSample());
        
        List<AbundanceColumnDefinition> definitions =
            server.getAbundanceColumnDefinitionsForProteinByExperiment(SESSION_TOKEN,
                    EXPERIMENT_ID);
        assertEquals(1, definitions.size());
        assertEquals(SAMPLE_CODE, definitions.get(0).getSampleCode());
        assertEquals(0, definitions.get(0).getTreatments().size());
        
        assertEquals(true, mockDataSet.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetOneAbundanceColumnDefinitionWithOneTreatment()
    {
        prepareGetSession();
        prepareGetExperimentPermID();
        MockDataSet<String> mockDataSet = new MockDataSet<String>();
        mockDataSet.add(SAMPLE_PERM_ID);
        prepareListAbundanceRelatedSamples(mockDataSet);
        final SamplePE samplePE = createSample();
        addTreatment(samplePE, "", "abc");
        prepareFindSample(samplePE);
        
        List<AbundanceColumnDefinition> definitions =
            server.getAbundanceColumnDefinitionsForProteinByExperiment(SESSION_TOKEN,
                    EXPERIMENT_ID);
        assertEquals(1, definitions.size());
        assertEquals(SAMPLE_CODE, definitions.get(0).getSampleCode());
        assertEquals(1, definitions.get(0).getTreatments().size());
        assertEquals("abc", definitions.get(0).getTreatments().get(0).getValue());
        assertEquals("PH", definitions.get(0).getTreatments().get(0).getType());
        
        assertEquals(true, mockDataSet.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetAbundanceColumnDefinitionForTwoSamplesWithSameParent()
    {
        prepareGetSession();
        prepareGetExperimentPermID();
        MockDataSet<String> mockDataSet = new MockDataSet<String>();
        mockDataSet.add(SAMPLE_PERM_ID);
        mockDataSet.add(SAMPLE_PERM_ID + "a");
        prepareListAbundanceRelatedSamples(mockDataSet);
        final SamplePE samplePE = createSample();
        addTreatment(samplePE, "", "abc");
        prepareFindSample(samplePE);
        
        List<AbundanceColumnDefinition> definitions =
            server.getAbundanceColumnDefinitionsForProteinByExperiment(SESSION_TOKEN,
                    EXPERIMENT_ID);
        assertEquals(1, definitions.size());
        assertEquals(SAMPLE_CODE, definitions.get(0).getSampleCode());
        assertEquals(1, definitions.get(0).getTreatments().size());
        assertEquals("abc", definitions.get(0).getTreatments().get(0).getValue());
        assertEquals("PH", definitions.get(0).getTreatments().get(0).getType());
        
        assertEquals(true, mockDataSet.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }

    private void prepareFindSample(final SamplePE samplePE)
    {
        context.checking(new Expectations()
        {
            {
                one(sampleDAO).getByTechId(new TechId(SAMPLE_ID));
                will(returnValue(samplePE));
            }
        });
    }
    
    private void prepareListAbundanceRelatedSamples(final MockDataSet<String> mockDataSet)
    {
        context.checking(new Expectations()
            {
                {
                    one(proteinDAO).listAbundanceRelatedSamplePermIDsByExperiment(EXPERIMENT_PERM_ID);
                    will(returnValue(mockDataSet));
                    
                    one(boFactory).createSampleIDProvider(SESSION);
                    will(returnValue(sampleIDProvider));
                    
                    for (String samplePermID : mockDataSet)
                    {
                        one(sampleIDProvider).getSampleIDOrParentSampleID(samplePermID);
                        will(returnValue(SAMPLE_ID));
                    }
                }
            });
    }
    
    private void prepareGetExperimentPermID()
    {
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).getByTechId(EXPERIMENT_ID);
                    ExperimentPE experimentPE = new ExperimentPE();
                    experimentPE.setPermId(EXPERIMENT_PERM_ID);
                    will(returnValue(experimentPE));
                }
            });
    }
    
    private SamplePE createSample()
    {
        SamplePE samplePE = new SamplePE();
        samplePE.setCode(SAMPLE_CODE);
        return samplePE;
    }
    
    private void addTreatment(SamplePE sample, String type, String value)
    {
        SamplePropertyPE sampleProperty = new SamplePropertyPE();
        sampleProperty.setEntityTypePropertyType(createSTPT(TREATMENT_TYPE_CODE + type));
        VocabularyTermPE term = new VocabularyTermPE();
        term.setCode("PH");
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
}
