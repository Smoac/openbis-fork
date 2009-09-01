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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinWithAbundances;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AbundanceManagerTest extends AssertJUnit
{

    @Test
    public void testNoProteinReferenceHandled()
    {
        AbundanceManager abundanceManager = new AbundanceManager();
        assertEquals(0, abundanceManager.getSampleIDs().size());
        assertEquals(0, abundanceManager.getProteinsWithAbundances().size());
    }
    
    @Test
    public void testHandleTwoProteinReferencesButOnlyOneHasAnAbundance()
    {
        AbundanceManager abundanceManager = new AbundanceManager();
        ProteinReferenceWithProbability protein1 = new ProteinReferenceWithProbability();
        protein1.setId(1);
        protein1.setAccessionNumber("abc1");
        protein1.setDescription("abc one");
        protein1.setSampleID(101L);
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
        assertEquals(101, p1.getSampleIDs().iterator().next().longValue());
        assertEquals(0, p1.getAbundancesForSample(12345678).length);
        assertEquals(1, p1.getAbundancesForSample(101).length);
        assertEquals(1.5, p1.getAbundancesForSample(101)[0]);
        ProteinWithAbundances p2 = iterator.next();
        assertEquals(2, p2.getId());
        assertEquals("abc two", p2.getDescription());
        assertEquals("abc2", p2.getAccessionNumber());
        assertEquals(0, p2.getSampleIDs().size());
    }
    
    @Test
    public void testHandleProteinReferencesWithManyAbundancesForTwoSamples()
    {
        AbundanceManager abundanceManager = new AbundanceManager();
        abundanceManager.handle(createProteinReference(101, 1.5));
        abundanceManager.handle(createProteinReference(101, 2.25));
        abundanceManager.handle(createProteinReference(102, 42));
        abundanceManager.handle(createProteinReference(102, 4.75));
        abundanceManager.handle(createProteinReference(102, 7.5));
        
        assertEquals(2, abundanceManager.getSampleIDs().size());
        Collection<ProteinWithAbundances> proteinsWithAbundances = abundanceManager.getProteinsWithAbundances();
        assertEquals(1, proteinsWithAbundances.size());
        ProteinWithAbundances protein = proteinsWithAbundances.iterator().next();
        assertEquals(1, protein.getId());
        assertEquals("abc one", protein.getDescription());
        assertEquals("abc1", protein.getAccessionNumber());
        assertEquals(2, protein.getSampleIDs().size());
        Iterator<Long> iterator = protein.getSampleIDs().iterator();
        assertEquals(101, iterator.next().longValue());
        assertEquals(102, iterator.next().longValue());
        assertEquals(2, protein.getAbundancesForSample(101).length);
        assertEquals(1.5, protein.getAbundancesForSample(101)[0]);
        assertEquals(2.25, protein.getAbundancesForSample(101)[1]);
        assertEquals(3, protein.getAbundancesForSample(102).length);
        assertEquals(42.0, protein.getAbundancesForSample(102)[0]);
        assertEquals(4.75, protein.getAbundancesForSample(102)[1]);
        assertEquals(7.5, protein.getAbundancesForSample(102)[2]);
    }

    private ProteinReferenceWithProbability createProteinReference(long sampleID, double abundance)
    {
        ProteinReferenceWithProbability protein1 = new ProteinReferenceWithProbability();
        protein1.setId(1);
        protein1.setAccessionNumber("abc1");
        protein1.setDescription("abc one");
        protein1.setSampleID(sampleID);
        protein1.setAbundance(abundance);
        return protein1;
    }
}
