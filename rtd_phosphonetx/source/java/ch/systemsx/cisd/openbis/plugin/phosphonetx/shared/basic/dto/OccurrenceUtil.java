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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Helper class for calculating peptide on protein coverage.
 *
 * @author Franz-Josef Elmer
 */
public class OccurrenceUtil
{
    /**
     * Returns a list of non-overlapping sequences built by the specified peptides covering the
     * specified protein.
     */
    public static List<Occurrence> getCoverage(String protein, Collection<String> peptides)
    {
        ArrayList<Occurrence> list = new ArrayList<Occurrence>();
        List<Occurrence> sortedList = calcSortedOccurrences(protein, peptides);
        if (sortedList.isEmpty() == false)
        {
            Occurrence current = sortedList.get(0);
            for (int i = 1; i < sortedList.size(); i++)
            {
                String currentPeptide = current.getWord();
                Occurrence next = sortedList.get(i);
                int diff = next.getStartIndex() - current.getStartIndex();
                if (current.getWord().length() < diff)
                {
                    list.add(current);
                    current = next;
                } else if (current.getEndIndex() < next.getEndIndex())
                {
                    String mergedPeptides = currentPeptide.substring(0, diff) + next.getWord();
                    current = new Occurrence(mergedPeptides, current.getStartIndex());
                }
            }
            list.add(current);
        }
        return list;
    }

    // calculates a list of all words occurences, sorts it be starting position
    private static List<Occurrence> calcSortedOccurrences(String protein, Collection<String> peptides)
    {
        List<Occurrence> result = new ArrayList<Occurrence>();
        for (String word : peptides)
        {
            int startIndex = 0;
            while (true)
            {
                int occurrenceIndex = protein.indexOf(word, startIndex);
                if (occurrenceIndex == -1)
                {
                    break;
                }
                result.add(new Occurrence(word, occurrenceIndex));
                startIndex = occurrenceIndex + 1; // maybe the word overlaps with itself?
            }
        }
        Collections.sort(result);
        return result;
    }

}
