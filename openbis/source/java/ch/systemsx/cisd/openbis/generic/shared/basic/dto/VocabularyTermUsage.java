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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * Stores the usage statistics for one vocabulary term and one entity kind.
 * 
 * @author Tomasz Pylak
 */
public class VocabularyTermUsage
{
    private VocabularyTerm term;

    // how many times is this term used as a value
    private int usageCounter;

    public VocabularyTermUsage(VocabularyTerm term, int usage)
    {
        this.term = term;
        this.usageCounter = usage;
    }

    public VocabularyTerm getTerm()
    {
        return term;
    }

    public int getUsageCounter()
    {
        return usageCounter;
    }
}
