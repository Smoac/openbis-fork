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

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import net.lemnik.eodsql.DataSet;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.MockDataSet;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProbabilityFDRMapping;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinAbundance;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProtein;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinInfoTableTest extends AbstractServerTestCase
{
    private static final double COVERAGE = 0.5;
    private static final String PERM_ID_PREFIX = "abc-";
    private static final long SAMPLE_ID_3 = 211L;
    private static final long SAMPLE_ID_2 = 102L;
    private static final long SAMPLE_ID_1 = 101L;
    private static final Double ABUNDANCE = new Double(47.11);
    private static final long PROTEIN_ID = 4141L;
    private static final long PROTEIN_REFERENCE_ID = 41L;
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
    private ISampleProvider sampleProvider;
    
    @Override
    @BeforeMethod
    public void setUp()
    {
        super.setUp();
        specificDAOFactory = context.mock(IPhosphoNetXDAOFactory.class);
        proteinDAO = context.mock(IProteinQueryDAO.class);
        sampleProvider = context.mock(ISampleProvider.class);
        context.checking(new Expectations()
            {
                {
                    allowing(specificDAOFactory).getProteinQueryDAO();
                    will(returnValue(proteinDAO));
                }
            });
        table = new ProteinInfoTable(daoFactory, specificDAOFactory, SESSION, sampleProvider);
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
        MockDataSet<ProteinReferenceWithProtein> proteinReferences = new MockDataSet<ProteinReferenceWithProtein>();
        MockDataSet<ProteinAbundance> proteinAbundances = new MockDataSet<ProteinAbundance>();
        prepareLoadDataSet(proteinReferences, proteinAbundances);
        
        table.load(Arrays.<AbundanceColumnDefinition> asList(), EXPERIMENT_ID,
                FALSE_DISCOVERY_RATE, AggregateFunction.MEAN, false);
        
        assertEquals(0, table.getProteinInfos().size());
        
        assertEquals(true, proteinReferences.hasCloseBeenInvoked());
        assertEquals(true, proteinAbundances.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }

    @Test
    public void testSimpleLoad()
    {
        MockDataSet<ProteinReferenceWithProtein> proteinReferences =
            new MockDataSet<ProteinReferenceWithProtein>();
        MockDataSet<ProteinAbundance> abundances = new MockDataSet<ProteinAbundance>();
        ProteinReferenceWithProtein proteinReference = new ProteinReferenceWithProtein();
        proteinReference.setDataSetID(DATA_SET_ID);
        proteinReference.setProteinID(PROTEIN_ID);
        proteinReference.setAccessionNumber(ACCESSION_NUMBER);
        proteinReference.setId(PROTEIN_REFERENCE_ID);
        proteinReferences.add(proteinReference);
        ProteinAbundance proteinAbundance = new ProteinAbundance();
        proteinAbundance.setId(PROTEIN_ID);
        proteinAbundance.setAbundance(ABUNDANCE);
        proteinAbundance.setSampleID(SAMPLE_PERM_ID);
        abundances.add(proteinAbundance);
        proteinReference = new ProteinReferenceWithProtein();
        proteinReference.setProteinID(PROTEIN_ID);
        proteinReference.setId(PROTEIN_REFERENCE_ID);
        proteinReference.setProbability(1);
        proteinReference.setDataSetID(DATA_SET_ID);
        proteinReferences.add(proteinReference);
        final MockDataSet<ProbabilityFDRMapping> mappings = new MockDataSet<ProbabilityFDRMapping>();
        mappings.add(new ProbabilityFDRMapping());
        ProbabilityFDRMapping mapping = new ProbabilityFDRMapping();
        mapping.setProbability(1);
        mapping.setFalseDiscoveryRate(1);
        mappings.add(mapping);
        prepareLoadDataSet(proteinReferences, abundances);
        context.checking(new Expectations()
            {
                {
                    one(proteinDAO).getProbabilityFDRMapping(DATA_SET_ID);
                    will(returnValue(mappings));

                    one(sampleProvider).getSample(SAMPLE_PERM_ID);
                    Sample sample = new Sample();
                    Sample parent = new Sample();
                    parent.setId(SAMPLE_ID);
                    sample.setGeneratedFrom(parent);
                    will(returnValue(sample));
                }
            });
        
        table.load(Arrays.<AbundanceColumnDefinition> asList(), EXPERIMENT_ID,
                FALSE_DISCOVERY_RATE, AggregateFunction.MEAN, false);
        
        Collection<ProteinInfo> proteins = table.getProteinInfos();
        assertEquals(1, proteins.size());
        ProteinInfo protein = proteins.iterator().next();
        assertEquals(PROTEIN_REFERENCE_ID, protein.getId().getId().longValue());
        assertEquals(ACCESSION_NUMBER, protein.getAccessionNumber());
        
        assertEquals(true, proteinReferences.hasCloseBeenInvoked());
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
        final MockDataSet<ProteinReferenceWithProtein> proteinReferences =
            new MockDataSet<ProteinReferenceWithProtein>();
        ProteinReferenceWithProtein proteinReference = new ProteinReferenceWithProtein();
        proteinReference.setProteinID(PROTEIN_ID);
        proteinReference.setDataSetID(DATA_SET_ID);
        proteinReference.setAccessionNumber(ACCESSION_NUMBER);
        proteinReference.setId(PROTEIN_REFERENCE_ID);
        proteinReference.setCoverage(COVERAGE);
        proteinReferences.add(proteinReference);
        MockDataSet<ProteinAbundance> proteinAbundances = new MockDataSet<ProteinAbundance>();
        proteinAbundances.add(createProteinAbundance(SAMPLE_ID_1, 1));
        proteinAbundances.add(createProteinAbundance(SAMPLE_ID_1, 2));
        proteinAbundances.add(createProteinAbundance(SAMPLE_ID_2, 3));
        proteinAbundances.add(createProteinAbundance(SAMPLE_ID_3, 20));
        final MockDataSet<ProbabilityFDRMapping> mappings = new MockDataSet<ProbabilityFDRMapping>();
        mappings.add(new ProbabilityFDRMapping());
        ProbabilityFDRMapping mapping = new ProbabilityFDRMapping();
        mapping.setProbability(1);
        mapping.setFalseDiscoveryRate(1);
        mappings.add(mapping);
        prepareLoadDataSet(proteinReferences, proteinAbundances);
        context.checking(new Expectations()
            {
                {
                    one(proteinDAO).getProbabilityFDRMapping(DATA_SET_ID);
                    will(returnValue(mappings));

                    for (long id : new long[] {SAMPLE_ID_1, SAMPLE_ID_2, SAMPLE_ID_3})
                    {
                        atLeast(1).of(sampleProvider).getSample(PERM_ID_PREFIX + id);
                        Sample sample = new Sample();
                        sample.setId(id);
                        will(returnValue(sample));
                    }
                }
            });
        
        table.load(definitions, EXPERIMENT_ID,
                FALSE_DISCOVERY_RATE, AggregateFunction.MEAN, aggregateOriginal);
        
        Collection<ProteinInfo> proteins = table.getProteinInfos();
        assertEquals(1, proteins.size());
        ProteinInfo protein = proteins.iterator().next();
        assertEquals(PROTEIN_REFERENCE_ID, protein.getId().getId().longValue());
        assertEquals(ACCESSION_NUMBER, protein.getAccessionNumber());
        Map<Long, Double> abundances = protein.getAbundances();
        assertEquals(2, abundances.size());
        assertEquals(expectedAbundance, abundances.get(SAMPLE_ID_1 * 37 + SAMPLE_ID_2).doubleValue());
        assertEquals(20.0, abundances.get(SAMPLE_ID_3).doubleValue());
        assertEquals(100 * COVERAGE, protein.getCoverage());
        
        assertEquals(true, proteinReferences.hasCloseBeenInvoked());
        assertEquals(true, mappings.hasCloseBeenInvoked());
        context.assertIsSatisfied();
    }
    
    private void prepareLoadDataSet(
            final MockDataSet<ProteinReferenceWithProtein> proteinReferences,
            final DataSet<ProteinAbundance> proteinAbundances)
    {
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).getByTechId(EXPERIMENT_ID);
                    ExperimentPE experimentPE = new ExperimentPE();
                    experimentPE.setPermId(EXPERIMENT_PERM_ID);
                    will(returnValue(experimentPE));

                    one(proteinDAO).listProteinReferencesByExperiment(EXPERIMENT_PERM_ID);
                    will(returnValue(proteinReferences));
                    
                    LongSet proteinIDs = new LongArraySet();
                    for (ProteinReferenceWithProtein p : proteinReferences)
                    {
                        proteinIDs.add(p.getProteinID());
                    }
                    one(proteinDAO).listProteinWithAbundanceByExperiment(proteinIDs);
                    will(returnValue(proteinAbundances));
                }
            });
    }
    
    private ProteinAbundance createProteinAbundance(long sampleID, double abundance)
    {
        ProteinAbundance proteinAbundance = new ProteinAbundance();
        proteinAbundance.setSampleID(PERM_ID_PREFIX + sampleID);
        proteinAbundance.setId(PROTEIN_ID);
        proteinAbundance.setAbundance(abundance);
        return proteinAbundance;
    }

}
