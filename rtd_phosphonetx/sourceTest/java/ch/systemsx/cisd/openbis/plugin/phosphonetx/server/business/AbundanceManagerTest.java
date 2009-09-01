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
import java.util.Iterator;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinWithAbundances;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AbundanceManagerTest extends AbstractServerTestCase
{
    private static final String PERM_ID1 = "s101";
    private static final String PERM_ID2 = "s102";
    private static final long SAMPLE_ID_A = 42;
    private static final long SAMPLE_ID_B = 4711;
    
    private AbundanceManager abundanceManager;
    private ISampleIDProvider sampleIDProvider;

    @Override
    @BeforeMethod
    public void setUp()
    {
        super.setUp();
        
        sampleIDProvider = context.mock(ISampleIDProvider.class);
        abundanceManager = new AbundanceManager(sampleIDProvider);
    }

    @Test
    public void testNoProteinReferenceHandled()
    {
        assertEquals(0, abundanceManager.getSampleIDs().size());
        assertEquals(0, abundanceManager.getProteinsWithAbundances().size());
    }
    
    @Test
    public void testHandleTwoProteinReferencesButOnlyOneHasAnAbundance()
    {
        prepareSampleIDProvider();
        ProteinReferenceWithProbability protein1 = new ProteinReferenceWithProbability();
        protein1.setId(1);
        protein1.setAccessionNumber("abc1");
        protein1.setDescription("abc one");
        protein1.setSamplePermID(PERM_ID1);
        protein1.setAbundance(1.5);
        abundanceManager.handle(protein1);
        ProteinReferenceWithProbability protein2 = new ProteinReferenceWithProbability();
        protein2.setId(2);
        protein2.setAccessionNumber("abc2");
        protein2.setDescription("abc two");
        abundanceManager.handle(protein2);
        
        assertEquals(1, abundanceManager.getSampleIDs().size());
        Collection<ProteinWithAbundances> proteinsWithAbundances = abundanceManager.getProteinsWithAbundances();
        assertEquals(2, proteinsWithAbundances.size());
        Iterator<ProteinWithAbundances> iterator = proteinsWithAbundances.iterator();
        ProteinWithAbundances p1 = iterator.next();
        assertEquals(1, p1.getId());
        assertEquals("abc one", p1.getDescription());
        assertEquals("abc1", p1.getAccessionNumber());
        assertEquals(1, p1.getSampleIDs().size());
        assertEquals(SAMPLE_ID_A, p1.getSampleIDs().iterator().next().longValue());
        assertEquals(0, p1.getAbundancesForSample(12345678).length);
        assertEquals(1, p1.getAbundancesForSample(SAMPLE_ID_A).length);
        assertEquals(1.5, p1.getAbundancesForSample(SAMPLE_ID_A)[0]);
        ProteinWithAbundances p2 = iterator.next();
        assertEquals(2, p2.getId());
        assertEquals("abc two", p2.getDescription());
        assertEquals("abc2", p2.getAccessionNumber());
        assertEquals(0, p2.getSampleIDs().size());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testHandleProteinReferencesWithManyAbundancesForTwoSamples()
    {
        prepareSampleIDProvider();
        abundanceManager.handle(createProteinReference(PERM_ID1, 1.5));
        abundanceManager.handle(createProteinReference(PERM_ID1, 2.25));
        abundanceManager.handle(createProteinReference(PERM_ID2, 42));
        abundanceManager.handle(createProteinReference(PERM_ID2, 4.75));
        abundanceManager.handle(createProteinReference(PERM_ID2, 7.5));
        
        assertEquals(2, abundanceManager.getSampleIDs().size());
        Collection<ProteinWithAbundances> proteinsWithAbundances = abundanceManager.getProteinsWithAbundances();
        assertEquals(1, proteinsWithAbundances.size());
        ProteinWithAbundances protein = proteinsWithAbundances.iterator().next();
        assertEquals(1, protein.getId());
        assertEquals("abc one", protein.getDescription());
        assertEquals("abc1", protein.getAccessionNumber());
        assertEquals(2, protein.getSampleIDs().size());
        Iterator<Long> iterator = protein.getSampleIDs().iterator();
        assertEquals(SAMPLE_ID_A, iterator.next().longValue());
        assertEquals(SAMPLE_ID_B, iterator.next().longValue());
        assertEquals(2, protein.getAbundancesForSample(SAMPLE_ID_A).length);
        assertEquals(1.5, protein.getAbundancesForSample(SAMPLE_ID_A)[0]);
        assertEquals(2.25, protein.getAbundancesForSample(SAMPLE_ID_A)[1]);
        assertEquals(3, protein.getAbundancesForSample(SAMPLE_ID_B).length);
        assertEquals(42.0, protein.getAbundancesForSample(SAMPLE_ID_B)[0]);
        assertEquals(4.75, protein.getAbundancesForSample(SAMPLE_ID_B)[1]);
        assertEquals(7.5, protein.getAbundancesForSample(SAMPLE_ID_B)[2]);
        
        context.assertIsSatisfied();
    }

    private void prepareSampleIDProvider()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(sampleIDProvider).getSampleIDOrParentSampleID(PERM_ID1);
                    will(returnValue(SAMPLE_ID_A));
                    allowing(sampleIDProvider).getSampleIDOrParentSampleID(PERM_ID2);
                    will(returnValue(SAMPLE_ID_B));
                }
            });
    }

    private ProteinReferenceWithProbability createProteinReference(String samplePermID, double abundance)
    {
        ProteinReferenceWithProbability protein1 = new ProteinReferenceWithProbability();
        protein1.setId(1);
        protein1.setAccessionNumber("abc1");
        protein1.setDescription("abc one");
        protein1.setSamplePermID(samplePermID);
        protein1.setAbundance(abundance);
        return protein1;
    }
}
