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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class is able to find all occurrences of the given set of words in the provided template and
 * mark the beginning and end of those words. Cases when occurrences overlap are handles properly.
 * 
 * @author Tomasz Pylak
 */
public class OccurrencesMarker
{
    private final String startMarker;

    private final String endMarker;

    /** Produces an HTML code whih all occurrences properly marked */
    public static String markOccurrencesWithHtml(String template, List<String> words)
    {
        String start = "(";
        String end = ")";
        List<String> markedTemplates = new OccurrencesMarker(start, end).mark(template, words);
        StringBuffer sb = new StringBuffer();
        for (String markedTemplate : markedTemplates)
        {
            markedTemplate.replaceAll(start, "<font color='red'>");
            markedTemplate.replaceAll(end, "</font>");
            sb.append(markedTemplate);
            sb.append("<BR>");
        }
        return sb.toString();
    }

    public OccurrencesMarker(String startMarker, String endMarker)
    {
        this.startMarker = startMarker;
        this.endMarker = endMarker;
    }

    /**
     * If the words occurrences do not overlap in the template, the returned list has only one
     * element. Otherwise there are several elements, each contains non-overlapping marks. skips
     * words which do not occur in the template.
     */
    public List<String> mark(String template, List<String> words)
    {
        List<Occurrence> occurrences = calcSortedOccurrences(template, words);
        if (hasOverlapping(occurrences))
        {
            return markOverlapping(template, occurrences);
        } else
        {
            return Arrays.asList(markNonoverlapping(template, occurrences));
        }
    }

    // describes one occurence of the word in a template
    private static class Occurrence implements Comparable<Occurrence>
    {
        private final String word;

        private final int startIndex;

        // helper flag for additional processing
        private boolean visited;

        public Occurrence(String word, int startIndex)
        {
            this.word = word;
            this.startIndex = startIndex;
        }

        public String getWord()
        {
            return word;
        }

        public int getStartIndex()
        {
            return startIndex;
        }

        public int getEndIndex()
        {
            return startIndex + word.length() - 1;
        }

        public boolean isVisited()
        {
            return visited;
        }

        public void setVisited(boolean visited)
        {
            this.visited = visited;
        }

        public int compareTo(Occurrence o)
        {
            return getStartIndex() - o.getStartIndex();
        }
    }

    private List<String> markOverlapping(String template, List<Occurrence> sortedOccurrences)
    {
        List<List<Occurrence>> distinctOccurrencesList = splitToDistinctGroups(sortedOccurrences);
        List<String> result = new ArrayList<String>();
        for (List<Occurrence> distinctOccurrences : distinctOccurrencesList)
        {
            String marked = markNonoverlapping(template, distinctOccurrences);
            result.add(marked);
        }
        return result;
    }

    // Splits all occurrences into many groups in such a way, that occurrences in one group do not
    // overlap with each other.
    private static List<List<Occurrence>> splitToDistinctGroups(List<Occurrence> sortedOccurrences)
    {
        setVisitedFlag(sortedOccurrences, false);
        List<List<Occurrence>> result = new ArrayList<List<Occurrence>>();
        int unvisited = sortedOccurrences.size();
        while (unvisited > 0)
        {
            List<Occurrence> distinctOccurrences =
                    chooseDistinctUnvisitedOccurrences(sortedOccurrences);
            result.add(distinctOccurrences);

            setVisitedFlag(distinctOccurrences, true);
            unvisited -= distinctOccurrences.size();
        }
        return result;
    }

    private static List<Occurrence> chooseDistinctUnvisitedOccurrences(
            List<Occurrence> sortedOccurrences)
    {
        List<Occurrence> distinctOccurrences = new ArrayList<Occurrence>();
        int lastIncludedCharIndex = -1;
        for (Occurrence occurrence : sortedOccurrences)
        {
            if (occurrence.isVisited() == false)
            {
                if (occurrence.getStartIndex() > lastIncludedCharIndex)
                {
                    distinctOccurrences.add(occurrence);
                    lastIncludedCharIndex = occurrence.getEndIndex();
                }
            }
        }
        return distinctOccurrences;
    }

    private static void setVisitedFlag(List<Occurrence> occurrences, boolean visited)
    {
        for (Occurrence occurrence : occurrences)
        {
            occurrence.setVisited(visited);
        }
    }

    // marks all occurrences in the template, assuming that all occurrences do not
    // overlap with each other and are sorted by the start position
    private String markNonoverlapping(String template, List<Occurrence> sortedOccurrences)
    {
        StringBuffer sb = new StringBuffer();
        int nextUnprocessedCharIndex = 0;
        for (Occurrence occurrence : sortedOccurrences)
        {
            int occuranceIndex = occurrence.getStartIndex();
            sb.append(template.substring(nextUnprocessedCharIndex, occuranceIndex));
            sb.append(startMarker);
            sb.append(occurrence.getWord());
            sb.append(endMarker);
            nextUnprocessedCharIndex = occurrence.getEndIndex() + 1;
        }
        sb.append(template.substring(nextUnprocessedCharIndex));
        return sb.toString();
    }

    // true if two occurrences of the same word in the template overlap (have a common part)
    private static boolean hasOverlapping(List<Occurrence> occurrences)
    {
        int prevEndIndex = -1;
        for (Occurrence occurrence : occurrences)
        {
            if (occurrence.getStartIndex() <= prevEndIndex)
            {
                return true;
            }
            prevEndIndex = occurrence.getEndIndex();
        }
        return false;
    }

    // calculates a list of all words occurances, sorts it be starting position
    private static List<Occurrence> calcSortedOccurrences(String template, List<String> words)
    {
        List<Occurrence> result = new ArrayList<Occurrence>();
        for (String word : words)
        {
            result.addAll(calcOccurrences(template, word));
        }
        Collections.sort(result);
        return result;
    }

    private static List<Occurrence> calcOccurrences(String template, String word)
    {
        List<Occurrence> result = new ArrayList<Occurrence>();
        int startIndex = 0;
        while (true)
        {
            int occurrenceIndex = template.indexOf(word, startIndex);
            if (occurrenceIndex == -1)
            {
                break;
            }
            result.add(new Occurrence(word, occurrenceIndex));
            startIndex = occurrenceIndex + 1; // maybe the word overlaps with itself?
        }
        return result;
    }

}
