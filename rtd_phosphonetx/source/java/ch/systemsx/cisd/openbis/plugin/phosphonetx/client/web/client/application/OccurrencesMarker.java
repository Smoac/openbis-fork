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

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Occurrence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.OccurrenceUtil;

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
        List<Occurrence> sortedOccurrences = OccurrenceUtil.getCoverage(template, words);
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

}
