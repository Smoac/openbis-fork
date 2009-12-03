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

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IAbundanceColumnDefinitionTable;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IBusinessObjectFactory;
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
    
    private IPhosphoNetXDAOFactory phosphoNetXDAOFactory;
    private IBusinessObjectFactory boFactory;
    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;
    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;
    private IProteinQueryDAO proteinDAO;
    private PhosphoNetXServer server;
    private IAbundanceColumnDefinitionTable abundanceColumnDefinitionTable;
    
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
        server =
                new PhosphoNetXServer(sessionManager, daoFactory, phosphoNetXDAOFactory, boFactory,
                        sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
        context.checking(new Expectations()
            {
                {
                    allowing(phosphoNetXDAOFactory).getProteinQueryDAOFromPool();
                    will(returnValue(proteinDAO));
                    allowing(phosphoNetXDAOFactory).returnProteinQueryDAOToPool(proteinDAO);
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
                    
                    one(proteinDAO).listAbundanceRelatedSamplePermIDsByExperiment(EXPERIMENT_PERM_ID);
                    will(returnValue(mockDataSet));
                    
                    one(boFactory).createAbundanceColumnDefinitionTable(SESSION);
                    will(returnValue(abundanceColumnDefinitionTable));
                    
                    one(abundanceColumnDefinitionTable).add(SAMPLE_PERM_ID);
                    
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
    
}
