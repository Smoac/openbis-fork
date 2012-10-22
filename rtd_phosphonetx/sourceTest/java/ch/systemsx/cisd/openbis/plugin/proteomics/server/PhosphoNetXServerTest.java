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

package ch.systemsx.cisd.openbis.plugin.proteomics.server;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.common.eodsql.MockDataSet;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IAbundanceColumnDefinitionTable;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IProteinDetailsBO;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IProteinRelatedSampleTable;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.ISampleProvider;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinDetails;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinRelatedSample;

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
    
    private IPhosphoNetXDAOFactory phosphoNetXDAOFactory;
    private IBusinessObjectFactory boFactory;
    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;
    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;
    private IProteinQueryDAO proteinDAO;
    private PhosphoNetXServer server;
    private IAbundanceColumnDefinitionTable abundanceColumnDefinitionTable;
    private ISampleProvider sampleProvider;
    private IProteinDetailsBO proteinDetailsBO;
    private IProteinRelatedSampleTable proteinRelatedSampleTable;
    
    @Override
    @BeforeMethod
    public void setUp()
    {
        super.setUp();
        phosphoNetXDAOFactory = context.mock(IPhosphoNetXDAOFactory.class);
        boFactory = context.mock(IBusinessObjectFactory.class);
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
        proteinDAO = context.mock(IProteinQueryDAO.class);
        abundanceColumnDefinitionTable = context.mock(IAbundanceColumnDefinitionTable.class);
        proteinDetailsBO = context.mock(IProteinDetailsBO.class);
        proteinRelatedSampleTable = context.mock(IProteinRelatedSampleTable.class);
        sampleProvider = context.mock(ISampleProvider.class);
        server =
                new PhosphoNetXServer(sessionManager, daoFactory, propertiesBatchManager,
                        phosphoNetXDAOFactory, boFactory, sampleTypeSlaveServerPlugin,
                        dataSetTypeSlaveServerPlugin);
        context.checking(new Expectations()
            {
                {
                    allowing(boFactory).createProteinDetailsBO(session);
                    will(returnValue(proteinDetailsBO));
                    
                    allowing(boFactory).createProteinRelatedSampleTable(session);
                    will(returnValue(proteinRelatedSampleTable));
                }
            });
    }

    @Test
    public void testGetAbundanceColumnDefinitionsForProteinByExperiment()
    {
        prepareGetSession();
        final MockDataSet<String> mockDataSet = new MockDataSet<String>();
        mockDataSet.add(SAMPLE_PERM_ID);
        final List<AbundanceColumnDefinition> result = Arrays.asList();
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).getByTechId(EXPERIMENT_ID);
                    ExperimentPE experimentPE = new ExperimentPE();
                    experimentPE.setPermId(EXPERIMENT_PERM_ID);
                    will(returnValue(experimentPE));
                    
                    allowing(phosphoNetXDAOFactory).getProteinQueryDAO(EXPERIMENT_ID);
                    will(returnValue(proteinDAO));
                    
                    one(proteinDAO).listAbundanceRelatedSamplePermIDsByExperiment(EXPERIMENT_PERM_ID);
                    will(returnValue(mockDataSet));
                    
                    one(boFactory).createAbundanceColumnDefinitionTable(session);
                    will(returnValue(abundanceColumnDefinitionTable));
                    
                    one(boFactory).createSampleProvider(session);
                    will(returnValue(sampleProvider));
                    
                    one(sampleProvider).loadByExperimentID(EXPERIMENT_ID);
                    
                    one(sampleProvider).getSample(SAMPLE_PERM_ID);
                    Sample sample = new Sample();
                    sample.setPermId(SAMPLE_PERM_ID);
                    will(returnValue(sample));
                    
                    one(abundanceColumnDefinitionTable).add(sample);
                    
                    one(abundanceColumnDefinitionTable).getSortedAndAggregatedDefinitions("PH");
                    will(returnValue(result));
                }
            });
        
        List<AbundanceColumnDefinition> definitions =
            server.getAbundanceColumnDefinitionsForProteinByExperiment(SESSION_TOKEN,
                    EXPERIMENT_ID, "PH");
        assertSame(result, definitions);
        assertEquals(true, mockDataSet.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListProteinRelatedSamplesByProtein()
    {
        prepareGetSession();
        final TechId experimentID = new TechId(42);
        final TechId proteinReferenceID = new TechId(4711);
        final List<Object> result = Arrays.asList();
        context.checking(new Expectations()
            {
                {
                    one(proteinDetailsBO).loadByExperimentAndReference(experimentID,
                            proteinReferenceID);
                    one(proteinDetailsBO).getDetailsOrNull();
                    ProteinDetails details = new ProteinDetails();
                    String sequence = "abcdefabcab";
                    details.setSequence(sequence);
                    will(returnValue(details));

                    one(proteinRelatedSampleTable).load(experimentID, proteinReferenceID, sequence);
                    one(proteinRelatedSampleTable).getSamples();
                    will(returnValue(result));
                }
            });

        List<ProteinRelatedSample> list =
                server.listProteinRelatedSamplesByProtein(SESSION_TOKEN, experimentID,
                        proteinReferenceID);
        
        assertSame(result, list);
        context.assertIsSatisfied();
    }
    
}
