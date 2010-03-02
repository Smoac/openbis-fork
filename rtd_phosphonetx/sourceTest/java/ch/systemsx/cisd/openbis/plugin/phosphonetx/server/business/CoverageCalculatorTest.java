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

import java.util.Arrays;
import java.util.Collections;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithPeptideSequence;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CoverageCalculatorTest extends AssertJUnit
{   
    @Test
    public void testCalculate()
    {
        ProteinReferenceWithPeptideSequence p1a = protein(1, 101, "abcd", "a");
        ProteinReferenceWithPeptideSequence p1b = protein(1, 101, "abcd", "c");
        ProteinReferenceWithPeptideSequence p2a = protein(2, 102, "aabbccdd", "bc");
        ProteinReferenceWithPeptideSequence p2b = protein(2, 103, "314159", "14");
        ProteinReferenceWithPeptideSequence p2c = protein(2, 103, "314159", "5");
        CoverageCalculator caluclator = new CoverageCalculator(Arrays.asList(p1a, p2a, p2b));
        caluclator.handlePeptideSequences(Arrays.asList(p1a, p1b, p2a, p2b, p2c));
        
        assertEquals(50.0, caluclator.calculateCoverageFor(1));
        assertEquals(37.5, caluclator.calculateCoverageFor(2));
    }
    
    @Test
    public void testCalculateForUnknwonID()
    {
        CoverageCalculator caluclator =
                new CoverageCalculator(Collections
                        .<ProteinReferenceWithPeptideSequence> emptyList());
        try
        {
            caluclator.calculateCoverageFor(42);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("No coverage info found for protein reference ID 42", ex.getMessage());
        }
    }

    private ProteinReferenceWithPeptideSequence protein(long id, long seqID, String protein, String peptide)
    {
        ProteinReferenceWithPeptideSequence pp = new ProteinReferenceWithPeptideSequence();
        pp.setId(id);
        pp.setSequenceID(seqID);
        pp.setProteinSequence(protein);
        pp.setPeptideSequence(peptide);
        return pp;
    }
}
