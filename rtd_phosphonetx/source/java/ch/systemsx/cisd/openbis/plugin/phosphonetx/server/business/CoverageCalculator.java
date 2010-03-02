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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Occurrence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.OccurrenceUtil;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithPeptideSequence;

/**
 * Calculates the coverage (in %) of a protein sequence by the set of peptides which identified it.
 *
 * @author Franz-Josef Elmer
 */
class CoverageCalculator
{
    private static final class Coverage
    {
        private final Map<Long, Set<String>> peptidesOfProteins = new HashMap<Long, Set<String>>();
        private final Map<Long, String> aminoAcidSequences = new HashMap<Long, String>();
        
        public double calculateCoverage()
        {
            double sum = 0;
            if (peptidesOfProteins.size() > 1)
            {
                System.out.println("SEQ:"+peptidesOfProteins.size());
            }
            for (Map.Entry<Long, Set<String>> entry : peptidesOfProteins.entrySet())
            {
                String proteinSequence = aminoAcidSequences.get(entry.getKey());
                assert proteinSequence != null : "Unknown sequence ID: " + entry.getKey();
                Set<String> peptideSequences = entry.getValue();
                List<Occurrence> list = OccurrenceUtil.getCoverage(proteinSequence, peptideSequences);
                int sumPeptides = 0;
                for (Occurrence occurrence : list)
                {
                    sumPeptides += occurrence.getWord().length();
                }
                sum += sumPeptides / (double) proteinSequence.length();
            }
            return 100 * sum / peptidesOfProteins.size();
        }

        public void register(Long sequenceID, String aminoAcidSequence)
        {
            aminoAcidSequences.put(sequenceID, aminoAcidSequence);
        }
        
        public void handle(Long sequenceID, String peptideSequence)
        {
            Set<String> peptides = peptidesOfProteins.get(sequenceID);
            if (peptides == null)
            {
                peptides = new LinkedHashSet<String>();
                peptidesOfProteins.put(sequenceID, peptides);
            }
            peptides.add(peptideSequence);
        }
    }
    
    private final Map<Long, Coverage> coverageMap = new HashMap<Long, Coverage>();
    
    /**
     * Creates an instance for the specified collection of protein references with peptide
     * sequence.
     */
    CoverageCalculator(Iterable<ProteinReferenceWithPeptideSequence> proteins)
    {
        for (ProteinReferenceWithPeptideSequence protein : proteins)
        {
            Long id = protein.getId();
            Coverage coverage = coverageMap.get(id);
            if (coverage == null)
            {
                coverage = new Coverage();
                coverageMap.put(id, coverage);
            }
            coverage.register(protein.getSequenceID(), protein.getProteinSequence());
        }
    }
    
    void handlePeptideSequences(Iterable<ProteinReferenceWithPeptideSequence> proteins)
    {
        for (ProteinReferenceWithPeptideSequence protein : proteins)
        {
            Long id = protein.getId();
            Coverage coverage = coverageMap.get(id);
            assert coverage != null : "Unknown protein reference ID: " + id;
            coverage.handle(protein.getSequenceID(), protein.getPeptideSequence());
        }
        
    }

    /**
     * Calculate the coverage for the specified protein reference.
     */
    double calculateCoverageFor(long proteinReferenceID)
    {
        Coverage coverage = coverageMap.get(proteinReferenceID);
        if (coverage == null)
        {
            throw new IllegalArgumentException("No coverage info found for protein reference ID "
                    + proteinReferenceID);
        }
        return coverage.calculateCoverage();
    }
}
