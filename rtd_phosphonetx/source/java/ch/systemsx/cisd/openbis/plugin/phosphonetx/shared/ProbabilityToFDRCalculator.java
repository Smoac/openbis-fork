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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class ProbabilityToFDRCalculator
{
    private static final class MappingEntry implements Comparable<ProbabilityToFDRCalculator.MappingEntry>
    {
        private final double probability;

        private final double fdr;

        MappingEntry(double probability, double fdr)
        {
            this.probability = probability;
            this.fdr = fdr;
        }

        public int compareTo(ProbabilityToFDRCalculator.MappingEntry that)
        {
            return probability < that.probability ? -1 : (probability > that.probability ? 1
                    : 0);
        }

        @Override
        public String toString()
        {
            return probability + " = " + fdr;
        }
    }

    private final List<ProbabilityToFDRCalculator.MappingEntry> mappingEntries = new ArrayList<ProbabilityToFDRCalculator.MappingEntry>();

    public void add(double probability, double falseDiscoveryRate)
    {
        mappingEntries.add(new MappingEntry(probability, falseDiscoveryRate));
    }

    public void init()
    {
        Collections.sort(mappingEntries);
    }

    public double calculateFDR(double probability)
    {
        int index = Collections.binarySearch(mappingEntries, new MappingEntry(probability, 0));
        if (index >= 0)
        {
            return mappingEntries.get(index).fdr;
        }
        // calculate by linear interpolation
        int index1 = -index - 1;
        int index0 = index1 - 1;
        assert index0 >= 0;
        ProbabilityToFDRCalculator.MappingEntry m0 = mappingEntries.get(index0);
        ProbabilityToFDRCalculator.MappingEntry m1 = mappingEntries.get(index1);
        double scale = (m1.fdr - m0.fdr) / (m1.probability - m0.probability);
        return m0.fdr + scale * (probability - m0.probability);
    }
}