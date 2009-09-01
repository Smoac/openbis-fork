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

import java.util.Collection;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.MockDataSet;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProbabilityFDRMapping;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinWithAbundances;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinWithAbundancesTableTest extends AbstractServerTestCase
{
    private static final Double ABUNDANCE = new Double(47.11);
    private static final long PROTEIN_ID = 41L;
    private static final String SAMPLE_PERM_ID = "s47-11";
    private static final long SAMPLE_ID = 4711;
    private static final long DATA_SET_ID = 42L;
    private static final String EXPERIMENT_ID = "abc-234";
    private static final double FALSE_DISCOVERY_RATE = 0.25;
    private static final String ACCESSION_NUMBER = "ABC123";
    
    private IPhosphoNetXDAOFactory specificDAOFactory;
    private IProteinQueryDAO proteinDAO;
    private ProteinWithAbundancesTable table;
    
    @Override
    @BeforeMethod
    public void setUp()
    {
        super.setUp();
        specificDAOFactory = context.mock(IPhosphoNetXDAOFactory.class);
        proteinDAO = context.mock(IProteinQueryDAO.class);
        context.checking(new Expectations()
            {
                {
                    allowing(specificDAOFactory).getProteinQueryDAO();
                    will(returnValue(proteinDAO));
                }
            });
        table = new ProteinWithAbundancesTable(daoFactory, specificDAOFactory, SESSION);
    }
    
    @Test
    public void testLoadLeadingToAnEmptyTable()
    {
        final MockDataSet<ProteinReferenceWithProbability> dataSet =
                new MockDataSet<ProteinReferenceWithProbability>();
        context.checking(new Expectations()
            {
                {
                    one(proteinDAO).listProteinsByExperiment(EXPERIMENT_ID);
                    will(returnValue(dataSet));
                }
            });
        
        table.load(EXPERIMENT_ID, FALSE_DISCOVERY_RATE);
        
        assertEquals(0, table.getSampleIDs().size());
        assertEquals(0, table.getProteinsWithAbundances().size());
        
        assertEquals(true, dataSet.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }

    @Test
    public void testLoad()
    {
        final MockDataSet<ProteinReferenceWithProbability> dataSet =
            new MockDataSet<ProteinReferenceWithProbability>();
        ProteinReferenceWithProbability proteinReference = new ProteinReferenceWithProbability();
        proteinReference.setDataSetID(DATA_SET_ID);
        proteinReference.setSamplePermID(SAMPLE_PERM_ID);
        proteinReference.setAccessionNumber(ACCESSION_NUMBER);
        proteinReference.setId(PROTEIN_ID);
        proteinReference.setAbundance(ABUNDANCE);
        dataSet.add(proteinReference);
        proteinReference = new ProteinReferenceWithProbability();
        proteinReference.setProbability(1);
        proteinReference.setDataSetID(DATA_SET_ID);
        dataSet.add(proteinReference);
        final MockDataSet<ProbabilityFDRMapping> mappings = new MockDataSet<ProbabilityFDRMapping>();
        mappings.add(new ProbabilityFDRMapping());
        ProbabilityFDRMapping mapping = new ProbabilityFDRMapping();
        mapping.setProbability(1);
        mapping.setFalseDiscoveryRate(1);
        mappings.add(mapping);
        context.checking(new Expectations()
            {
                {
                    one(proteinDAO).listProteinsByExperiment(EXPERIMENT_ID);
                    will(returnValue(dataSet));

                    one(proteinDAO).getProbabilityFDRMapping(DATA_SET_ID);
                    will(returnValue(mappings));

                    one(sampleDAO).tryToFindByPermID(SAMPLE_PERM_ID);
                    SamplePE samplePE = new SamplePE();
                    samplePE.setId(SAMPLE_ID);
                    will(returnValue(samplePE));
                }
            });
        
        table.load(EXPERIMENT_ID, FALSE_DISCOVERY_RATE);
        
        Collection<Long> sampleIDs = table.getSampleIDs();
        assertEquals(1, sampleIDs.size());
        Long sampleID = sampleIDs.iterator().next();
        assertEquals(SAMPLE_ID, sampleID.longValue());
        Collection<ProteinWithAbundances> proteins = table.getProteinsWithAbundances();
        assertEquals(1, proteins.size());
        ProteinWithAbundances protein = proteins.iterator().next();
        assertEquals(PROTEIN_ID, protein.getId());
        assertEquals(ACCESSION_NUMBER, protein.getAccessionNumber());
        assertEquals(sampleIDs, protein.getSampleIDs());
        double[] abundances = protein.getAbundancesForSample(sampleID);
        assertEquals(1, abundances.length);
        assertEquals(ABUNDANCE, abundances[0]);
        
        assertEquals(true, dataSet.hasCloseBeenInvoked());
        assertEquals(true, mappings.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }
    
}
