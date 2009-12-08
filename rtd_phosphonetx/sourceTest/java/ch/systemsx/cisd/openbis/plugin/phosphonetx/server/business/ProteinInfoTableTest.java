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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.MockDataSet;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProbabilityFDRMapping;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithPeptideSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinInfoTableTest extends AbstractServerTestCase
{
    private static final String PERM_ID_PREFIX = "abc-";
    private static final long SAMPLE_ID_3 = 211L;
    private static final long SAMPLE_ID_2 = 102L;
    private static final long SAMPLE_ID_1 = 101L;
    private static final Double ABUNDANCE = new Double(47.11);
    private static final long PROTEIN_ID = 41L;
    private static final String SAMPLE_PERM_ID = "s47-11";
    private static final long SAMPLE_ID = 4711;
    private static final long DATA_SET_ID = 42L;
    private static final TechId EXPERIMENT_ID = new TechId(234L);
    private static final String EXPERIMENT_PERM_ID = "abc-234";
    private static final double FALSE_DISCOVERY_RATE = 0.25;
    private static final String ACCESSION_NUMBER = "ABC123";
    
    private IPhosphoNetXDAOFactory specificDAOFactory;
    private IProteinQueryDAO proteinDAO;
    private ProteinInfoTable table;
    private ArrayList<AbundanceColumnDefinition> definitions;
    
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
        table = new ProteinInfoTable(daoFactory, specificDAOFactory, SESSION);
        definitions = new ArrayList<AbundanceColumnDefinition>();
        definitions.add(create(SAMPLE_ID_1, SAMPLE_ID_2));
        definitions.add(create(SAMPLE_ID_3));
    }
    
    private AbundanceColumnDefinition create(long... ids)
    {
        AbundanceColumnDefinition definition = new AbundanceColumnDefinition();
        for (long id : ids)
        {
            definition.addSampleID(id);
        }
        return definition;
    }
    
    @Test
    public void testLoadLeadingToAnEmptyTable()
    {
        final MockDataSet<ProteinReferenceWithProbability> dataSet =
                new MockDataSet<ProteinReferenceWithProbability>();
        prepareLoadDataSet(dataSet);
        
        table.load(Arrays.<AbundanceColumnDefinition> asList(), EXPERIMENT_ID,
                FALSE_DISCOVERY_RATE, AggregateFunction.MEAN, false);
        
        assertEquals(0, table.getProteinInfos().size());
        
        assertEquals(true, dataSet.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }

    @Test
    public void testSimpleLoad()
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
        prepareLoadDataSet(dataSet);
        context.checking(new Expectations()
            {
                {
                    one(proteinDAO).getProbabilityFDRMapping(DATA_SET_ID);
                    will(returnValue(mappings));

                    one(sampleDAO).tryToFindByPermID(SAMPLE_PERM_ID);
                    SamplePE samplePE = new SamplePE();
                    samplePE.setId(SAMPLE_ID);
                    will(returnValue(samplePE));
                }
            });
        
        table.load(Arrays.<AbundanceColumnDefinition> asList(), EXPERIMENT_ID,
                FALSE_DISCOVERY_RATE, AggregateFunction.MEAN, false);
        
        Collection<ProteinInfo> proteins = table.getProteinInfos();
        assertEquals(1, proteins.size());
        ProteinInfo protein = proteins.iterator().next();
        assertEquals(PROTEIN_ID, protein.getId().getId().longValue());
        assertEquals(ACCESSION_NUMBER, protein.getAccessionNumber());
        
        assertEquals(true, dataSet.hasCloseBeenInvoked());
        assertEquals(true, mappings.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLoadWithAggregationOnOriginal()
    {
        checkAggregationType(2.0, true);
    }

    @Test
    public void testLoadWithAggregationOnAggregation()
    {
        checkAggregationType(2.25, false);
    }
    
    private void checkAggregationType(double expectedAbundance, boolean aggregateOriginal)
    {
        final MockDataSet<ProteinReferenceWithProbability> dataSet =
            new MockDataSet<ProteinReferenceWithProbability>();
        dataSet.add(createProtein(SAMPLE_ID_1, 1));
        dataSet.add(createProtein(SAMPLE_ID_1, 2));
        dataSet.add(createProtein(SAMPLE_ID_2, 3));
        dataSet.add(createProtein(SAMPLE_ID_3, 20));
        final MockDataSet<ProbabilityFDRMapping> mappings = new MockDataSet<ProbabilityFDRMapping>();
        mappings.add(new ProbabilityFDRMapping());
        ProbabilityFDRMapping mapping = new ProbabilityFDRMapping();
        mapping.setProbability(1);
        mapping.setFalseDiscoveryRate(1);
        mappings.add(mapping);
        prepareLoadDataSet(dataSet);
        context.checking(new Expectations()
            {
                {
                    one(proteinDAO).getProbabilityFDRMapping(DATA_SET_ID);
                    will(returnValue(mappings));

                    for (long id : new long[] {SAMPLE_ID_1, SAMPLE_ID_2, SAMPLE_ID_3})
                    {
                        one(sampleDAO).tryToFindByPermID(PERM_ID_PREFIX + id);
                        SamplePE samplePE = new SamplePE();
                        samplePE.setId(id);
                        will(returnValue(samplePE));
                    }
                }
            });
        
        table.load(definitions, EXPERIMENT_ID,
                FALSE_DISCOVERY_RATE, AggregateFunction.MEAN, aggregateOriginal);
        
        Collection<ProteinInfo> proteins = table.getProteinInfos();
        assertEquals(1, proteins.size());
        ProteinInfo protein = proteins.iterator().next();
        assertEquals(PROTEIN_ID, protein.getId().getId().longValue());
        assertEquals(ACCESSION_NUMBER, protein.getAccessionNumber());
        Map<Long, Double> abundances = protein.getAbundances();
        assertEquals(2, abundances.size());
        assertEquals(expectedAbundance, abundances.get(SAMPLE_ID_1 * 37 + SAMPLE_ID_2).doubleValue());
        assertEquals(20.0, abundances.get(SAMPLE_ID_3).doubleValue());
        assertEquals(50.0, protein.getCoverage());
        
        assertEquals(true, dataSet.hasCloseBeenInvoked());
        assertEquals(true, mappings.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }
    
    private void prepareLoadDataSet(final MockDataSet<ProteinReferenceWithProbability> dataSet)
    {
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).getByTechId(EXPERIMENT_ID);
                    ExperimentPE experimentPE = new ExperimentPE();
                    experimentPE.setPermId(EXPERIMENT_PERM_ID);
                    will(returnValue(experimentPE));

                    one(proteinDAO).listProteinsByExperiment(EXPERIMENT_PERM_ID);
                    will(returnValue(dataSet));
                    
                    one(proteinDAO).listProteinsWithPeptidesByExperiment(EXPERIMENT_PERM_ID);
                    MockDataSet<ProteinReferenceWithPeptideSequence> dataSet2 =
                            new MockDataSet<ProteinReferenceWithPeptideSequence>();
                    ProteinReferenceWithPeptideSequence protein = new ProteinReferenceWithPeptideSequence();
                    protein.setId(PROTEIN_ID);
                    protein.setPeptideSequence("ABCDEF");
                    protein.setProteinSequence("ABCDEFblabla");
                    dataSet2.add(protein);
                    will(returnValue(dataSet2));
                }
            });
    }
    
    private ProteinReferenceWithProbability createProtein(long sampleID, double abundance)
    {
        ProteinReferenceWithProbability proteinReference = new ProteinReferenceWithProbability();
        proteinReference.setDataSetID(DATA_SET_ID);
        proteinReference.setSamplePermID(PERM_ID_PREFIX + sampleID);
        proteinReference.setAccessionNumber(ACCESSION_NUMBER);
        proteinReference.setId(PROTEIN_ID);
        proteinReference.setAbundance(abundance);
        return proteinReference;
    }

}
