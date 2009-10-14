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
import java.util.Collections;
import java.util.List;

/**
 * This class is able to find all occurrences of the given set of words in the provided template and
 * mark the beginning and end of those words. Note that when occurrences overlap the overlaps are
 * merged and the border between them is not visualized.
 * 
 * @author Tomasz Pylak
 */
public class OccurrencesMarker
{
    private final char startMarker;

    private final char endMarker;

    /** Produces an HTML code whih all occurrences properly marked */
    public static String markOccurrencesWithHtml(String template, List<String> words,
            int lineLength, int blockLength)
    {
        char start = '(';
        char end = ')';
        OccurrencesMarker marker = new OccurrencesMarker(start, end);
        String markedTemplate = marker.mark(template, words);
        markedTemplate = marker.breakLines(markedTemplate, lineLength, "<br>", blockLength, " ");
        markedTemplate = marker.replaceTags(markedTemplate, "<font color='red'>", "</font>");
        return markedTemplate;
    }

    // @Private
    String replaceTags(String text, String startTag, String endTag)
    {
        String newText = text;
        newText = newText.replaceAll("\\" + startMarker, startTag);
        newText = newText.replaceAll("\\" + endMarker, endTag);
        return newText;
    }

    /** split lines, so that each line has lineLength characters at most */
    String breakLines(String text, int lineLength, String endOfLine, int blockLength,
            String endOfBlock)
    {
        StringBuffer sb = new StringBuffer();
        String textToBreak = text;
        while (true)
        {
            int breakIndex = calcBreakBeforeIndex(textToBreak, lineLength);
            if (breakIndex == -1 || breakIndex == textToBreak.length())
            {
                break;
            }
            String line = textToBreak.substring(0, breakIndex);
            line = separateBlocks(line, lineLength, blockLength, endOfBlock);
            sb.append(line);
            sb.append(endOfLine);
            textToBreak = textToBreak.substring(breakIndex);
        }
        sb.append(separateBlocks(textToBreak, lineLength, blockLength, endOfBlock));
        return sb.toString();
    }

    private String separateBlocks(String line, int lineLength, int blockLength, String endOfBlock)
    {
        if (blockLength < lineLength)
        {
            return separateBlocks(line, blockLength, endOfBlock);
        } else
        {
            return line;
        }
    }

    private String separateBlocks(String text, int blockLength, String endOfBlock)
    {
        // use the same code as for breaking into lines, ensure that no blocks will be separated
        return breakLines(text, blockLength, endOfBlock, blockLength + 1, "");
    }

    // -1 if the text is shorter (ignoring the markers) then the line length
    private int calcBreakBeforeIndex(String textToBreak, int lineLength)
    {
        int counter = 0;
        for (int i = 0; i < textToBreak.length(); i++)
        {
            char ch = textToBreak.charAt(i);
            if (isMarker(ch) == false)
            {
                counter++;
            }
            if (counter == lineLength)
            {
                return i + 1;
            }
        }
        return -1;
    }

    private boolean isMarker(char ch)
    {
        return ch == startMarker || ch == endMarker;
    }

    public OccurrencesMarker(char startMarker, char endMarker)
    {
        this.startMarker = startMarker;
        this.endMarker = endMarker;
    }

    /**
     * All letters which belong to the matching words are marked. If some words overlap, the markers
     * are merged. E.g. for the template 'xabcx' and words 'ab' and 'bc', the result will be
     * 'x(abc)x'.
     * <p>
     * Words which do not occur in the template are ignored.
     * </p>
     */
    public String mark(String template, List<String> words)
    {
        List<Occurrence> occurrences = calcSortedOccurrences(template, words);
        if (hasOverlapping(occurrences))
        {
            return markOverlapping(template, occurrences);
        } else
        {
            return markNonoverlapping(template, occurrences);
        }
    }

    // describes one occurence of the word in a template
    private static class Occurrence implements Comparable<Occurrence>
    {
        private final String word;

        private final int startIndex;

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

        public int compareTo(Occurrence o)
        {
            return getStartIndex() - o.getStartIndex();
        }

        @Override
        public String toString()
        {
            return "[" + word + "@" + startIndex + "]";
        }
    }

    private String markOverlapping(String template, List<Occurrence> sortedOccurrences)
    {
        List<Occurrence> mergedOccurrences = mergeOverlaps(template, sortedOccurrences);
        return markNonoverlapping(template, mergedOccurrences);
    }

    private List<Occurrence> mergeOverlaps(String template, List<Occurrence> sortedOccurrences)
    {
        List<Occurrence> result = new ArrayList<Occurrence>();
        if (sortedOccurrences.size() == 0)
        {
            return result;
        }
        int startIndex = -1;
        int endIndex = -1;
        for (Occurrence occurrence : sortedOccurrences)
        {
            if (occurrence.getStartIndex() <= endIndex)
            { // overlap
                endIndex = Math.max(endIndex, occurrence.getEndIndex());
            } else
            { // current word does not overlap with the words browsed before
                if (startIndex != -1)
                { // create a new word from the words browsed before
                    Occurrence newOccurrence = createOccurence(template, startIndex, endIndex);
                    result.add(newOccurrence);
                }
                startIndex = occurrence.getStartIndex();
                endIndex = occurrence.getEndIndex();
            }
        }
        Occurrence newOccurrence = createOccurence(template, startIndex, endIndex);
        result.add(newOccurrence);
        return result;
    }

    private static Occurrence createOccurence(String template, int startIndex, int endIndex)
    {
        assert startIndex != -1 : "start index should be initialized";
        assert endIndex != -1 : "end index should be initialized";
        String mergedWord = template.substring(startIndex, endIndex + 1);
        Occurrence newOccurrence = new Occurrence(mergedWord, startIndex);
        return newOccurrence;
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
            prevEndIndex = Math.max(prevEndIndex, occurrence.getEndIndex());
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
