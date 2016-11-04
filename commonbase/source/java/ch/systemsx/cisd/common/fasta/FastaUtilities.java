/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.fasta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.utilities.Counters;

/**
 * Utility methods for FASTA files.
 *
 * @author Franz-Josef Elmer
 */
public class FastaUtilities
{
    /**
     * Nucleic acid codes as used in FASTA files (see https://en.wikipedia.org/wiki/FASTA_format).
     */
    public static final List<Character> NUCLEIC_ACID_CODES = Arrays.asList('A', 'C', 'G', 'T', 'U', 'R', 'Y',
            'K', 'M', 'S', 'W', 'B', 'D', 'H', 'V', 'N', 'X', '-');

    /**
     * Amino acid codes as used in FASTA files (see https://en.wikipedia.org/wiki/FASTA_format).
     */
    public static final List<Character> AMINO_ACID_CODES = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'Y', 'Z', 'X', '*', '-');

    private static final Set<Character> NUCLEIC_ACID_CODES_SET = new HashSet<Character>(NUCLEIC_ACID_CODES);

    private static final Set<Character> AMINO_ACID_CODES_SET = new HashSet<Character>(AMINO_ACID_CODES);

    /**
     * Determines the sequence type from the specified line of a FASTA file.
     * 
     * @param line Line from a FASTA file. Can be in lowercase.
     * @throws IllegalArgumentException if the line contains a character which isn't neither from NUCLEIC_ACID_CODES nor AMINO_ACID_CODES.
     */
    public static SequenceType determineSequenceType(String line)
    {
        Counters<Character> counters = new Counters<Character>();
        int sequenceCharacters = 0;
        for (char c : line.toUpperCase().toCharArray())
        {
            boolean isNucleicAcidCode = NUCLEIC_ACID_CODES_SET.contains(c);
            boolean isAmoniAcidCode = AMINO_ACID_CODES_SET.contains(c);
            if (isNucleicAcidCode == false && isAmoniAcidCode == false)
            {
                continue;
            }
            sequenceCharacters++;
            if (isNucleicAcidCode == false)
            {
                return SequenceType.PROT;
            }
            counters.count(c);
        }
        if (counters.getNumberOfDifferentObjectsCounted() > 6 || containsUAndT(counters))
        {
            return SequenceType.PROT;
        }
        int nonCommonNucleicAcidCodeSites = sequenceCharacters;
        for (Character c : "ACGTUN-".toCharArray())
        {
            nonCommonNucleicAcidCodeSites -= counters.getCountOf(c);
        }
        return nonCommonNucleicAcidCodeSites == 0 ? SequenceType.NUCL : SequenceType.PROT;
    }

    private static boolean containsUAndT(Counters<Character> counters)
    {
        return counters.getCountOf('T') > 0 && counters.getCountOf('U') > 0;
    }
}
