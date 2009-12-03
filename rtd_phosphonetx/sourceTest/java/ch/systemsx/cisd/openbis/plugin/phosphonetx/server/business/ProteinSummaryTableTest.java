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

import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.ProteinSummaryTable.FDR_LEVELS;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.MockDataSet;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProbabilityFDRMapping;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbabilityAndPeptide;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinSummaryTableTest extends AbstractServerTestCase
{
    private static final TechId EXPERIMENT_ID = new TechId(234L);

    private static final String EXPERIMENT_PERM_ID = "abc-234";

    private static final long DATA_SET_ID = 42L;

    private IPhosphoNetXDAOFactory specificDAOFactory;

    private IProteinQueryDAO proteinDAO;

    private ProteinSummaryTable table;

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
                    allowing(specificDAOFactory).getProteinQueryDAOFromPool();
                    will(returnValue(proteinDAO));
                    allowing(specificDAOFactory).returnProteinQueryDAOToPool(proteinDAO);
                }
            });
        table = new ProteinSummaryTable(daoFactory, specificDAOFactory, SESSION);
    }

    @Test
    public void testLoadEmptyData()
    {
        prepare();

        table.load(EXPERIMENT_ID);

        List<ProteinSummary> summaries = table.getProteinSummaries();
        for (ProteinSummary proteinSummary : summaries)
        {
            assertEquals(0, proteinSummary.getProteinCount());
            assertEquals(0, proteinSummary.getPeptideCount());
        }
        assertEquals(FDR_LEVELS.length, summaries.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadData()
    {
        ProteinReferenceWithProbabilityAndPeptide i1 = createItem(0.5, 123, "ABCD");
        ProteinReferenceWithProbabilityAndPeptide i2 = createItem(0.5, 123, "DEF");
        ProteinReferenceWithProbabilityAndPeptide i3 = createItem(0.75, 123, "DEF");
        ProteinReferenceWithProbabilityAndPeptide i4 = createItem(0.75, 456, "ABC");
        ProteinReferenceWithProbabilityAndPeptide i5 = createItem(1, 456, "DEF");
        ProteinReferenceWithProbabilityAndPeptide i6 = createItem(1, 456, "XYZ");
        prepare(i1, i2, i3, i4, i5, i6);
        context.checking(new Expectations()
            {
                {
                    allowing(proteinDAO).getProbabilityFDRMapping(DATA_SET_ID);
                    MockDataSet<ProbabilityFDRMapping> dataSet =
                            new MockDataSet<ProbabilityFDRMapping>();
                    dataSet.add(createMappingItem(0.5, FDR_LEVELS[3]));
                    dataSet.add(createMappingItem(0.75, FDR_LEVELS[2]));
                    dataSet.add(createMappingItem(1, FDR_LEVELS[0]));
                    will(returnValue(dataSet));
                }

            });
        table.load(EXPERIMENT_ID);

        List<ProteinSummary> summaries = table.getProteinSummaries();
        assertSummary(0, 1, 2, summaries);
        assertSummary(1, 1, 2, summaries);
        assertSummary(2, 2, 3, summaries);
        assertSummary(3, 2, 4, summaries);
        assertSummary(4, 2, 4, summaries);
        assertEquals(FDR_LEVELS.length, summaries.size());
        context.assertIsSatisfied();
    }

    private void assertSummary(int index, int expectedProteinCount, int expectedPeptideCount,
            List<ProteinSummary> summaries)
    {
        ProteinSummary proteinSummary = summaries.get(index);
        assertEquals(FDR_LEVELS[index], proteinSummary.getFDR(), 1e-6);
        assertEquals(expectedProteinCount, proteinSummary.getProteinCount());
        assertEquals(expectedPeptideCount, proteinSummary.getPeptideCount());
    }

    private ProbabilityFDRMapping createMappingItem(double probability, double falseDiscoveryRate)
    {
        ProbabilityFDRMapping m1 = new ProbabilityFDRMapping();
        m1.setProbability(probability);
        m1.setFalseDiscoveryRate(falseDiscoveryRate);
        return m1;
    }

    private void prepare(final ProteinReferenceWithProbabilityAndPeptide... items)
    {
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).getByTechId(EXPERIMENT_ID);
                    ExperimentPE experimentPE = new ExperimentPE();
                    experimentPE.setPermId(EXPERIMENT_PERM_ID);
                    will(returnValue(experimentPE));

                    one(proteinDAO).listProteinsWithProbabilityAndPeptidesByExperiment(
                            EXPERIMENT_PERM_ID);
                    MockDataSet<ProteinReferenceWithProbabilityAndPeptide> dataSet =
                            new MockDataSet<ProteinReferenceWithProbabilityAndPeptide>();
                    dataSet.addAll(Arrays.asList(items));
                    will(returnValue(dataSet));
                }
            });
    }

    private ProteinReferenceWithProbabilityAndPeptide createItem(double probability, long id,
            String peptideSequence)
    {
        ProteinReferenceWithProbabilityAndPeptide item =
                new ProteinReferenceWithProbabilityAndPeptide();
        item.setDataSetID(DATA_SET_ID);
        item.setProbability(probability);
        item.setId(id);
        item.setPeptideSequence(peptideSequence);
        return item;
    }

}
