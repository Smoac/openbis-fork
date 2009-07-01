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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.MockDataSet;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProbabilityFDRMapping;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ErrorModelTest extends AssertJUnit
{
    private static final double TOL = 1e-6;
    private static final long DATA_SET1_ID = 42;
    private static final long DATA_SET2_ID = 43;
    
    private Mockery context;
    private IPhosphoNetXDAOFactory specificDAOFactory;
    private IProteinQueryDAO proteinQueryDAO;
    private ErrorModel errorModel;
    
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        specificDAOFactory = context.mock(IPhosphoNetXDAOFactory.class);
        proteinQueryDAO = context.mock(IProteinQueryDAO.class);
        errorModel = new ErrorModel(specificDAOFactory);
        context.checking(new Expectations()
            {
                {
                    allowing(specificDAOFactory).getProteinQueryDAO();
                    will(returnValue(proteinQueryDAO));
                    
                    atMost(1).of(proteinQueryDAO).getProbabilityFDRMapping(DATA_SET1_ID);
                    MockDataSet<ProbabilityFDRMapping> dataSet1 = new MockDataSet<ProbabilityFDRMapping>();
                    createEntry(dataSet1, 0.4, 0.9);
                    createEntry(dataSet1, 0.5, 0.95);
                    will(returnValue(dataSet1));
                    
                    atMost(1).of(proteinQueryDAO).getProbabilityFDRMapping(DATA_SET2_ID);
                    MockDataSet<ProbabilityFDRMapping> dataSet2 = new MockDataSet<ProbabilityFDRMapping>();
                    createEntry(dataSet2, 1, 1);
                    createEntry(dataSet2, 0.4, 0.9);
                    createEntry(dataSet2, 0.1, 0.7);
                    createEntry(dataSet2, 0.0, 0.6);
                    will(returnValue(dataSet2));
                }
            });
    }
    
    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void testExact()
    {
        IdentifiedProtein protein = create(DATA_SET1_ID, 0.4);
        errorModel.setFalseDiscoveryRateFor(protein);
        
        assertEquals(0.9, protein.getFalseDiscoveryRate(), TOL);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testInterpolation()
    {
        IdentifiedProtein protein = create(DATA_SET1_ID, 0.42);
        errorModel.setFalseDiscoveryRateFor(protein);
        
        assertEquals(0.91, protein.getFalseDiscoveryRate(), TOL);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testCachedMapping()
    {
        IdentifiedProtein protein = create(DATA_SET1_ID, 0.42);
        errorModel.setFalseDiscoveryRateFor(protein);
        
        assertEquals(0.91, protein.getFalseDiscoveryRate(), TOL);
        
        protein = create(DATA_SET2_ID, 0.25);
        errorModel.setFalseDiscoveryRateFor(protein);
        
        assertEquals(0.8, protein.getFalseDiscoveryRate(), TOL);
        
        protein = create(DATA_SET1_ID, 0.5);
        errorModel.setFalseDiscoveryRateFor(protein);
        
        assertEquals(0.95, protein.getFalseDiscoveryRate(), TOL);
        
        context.assertIsSatisfied();
    }
    
    private IdentifiedProtein create(long dataSetID, double probability)
    {
        IdentifiedProtein protein = new IdentifiedProtein();
        protein.setDataSetID(dataSetID);
        protein.setProbability(probability);
        return protein;
    }
    
    private void createEntry(MockDataSet<ProbabilityFDRMapping> dataSet, double propability,
            double falseDiscoveryRate)
    {
        ProbabilityFDRMapping mapping = new ProbabilityFDRMapping();
        mapping.setProbability(propability);
        mapping.setFalseDiscoveryRate(falseDiscoveryRate);
        dataSet.add(mapping);
    }
}
