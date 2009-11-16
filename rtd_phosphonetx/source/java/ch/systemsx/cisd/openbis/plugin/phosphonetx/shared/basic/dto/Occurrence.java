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

/**
 * The occurrence of a word in a text. Occurrences are comparable by their start indexes.
 * 
 * @author Tomasz Pylak
 */
public class Occurrence implements Comparable<Occurrence>
{
    private final String word;

    private final int startIndex;

    /**
     * Creates a new instance of the specified word which appears at the specified start index.
     */
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