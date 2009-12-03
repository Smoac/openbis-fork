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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.DataSet;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProbabilityFDRMapping;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;

/**
 * @author Franz-Josef Elmer
 */
class ErrorModel
{
    private static final class ProbabilityToFDRCalculator
    {
        private static final class MappingEntry implements Comparable<MappingEntry>
        {
            private final double probability;

            private final double fdr;

            MappingEntry(double probability, double fdr)
            {
                this.probability = probability;
                this.fdr = fdr;
            }

            public int compareTo(MappingEntry that)
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

        private final List<MappingEntry> mappingEntries = new ArrayList<MappingEntry>();

        void add(double probability, double falseDiscoveryRate)
        {
            mappingEntries.add(new MappingEntry(probability, falseDiscoveryRate));
        }

        void init()
        {
            Collections.sort(mappingEntries);
        }

        double calculateFDR(double probability)
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
            MappingEntry m0 = mappingEntries.get(index0);
            MappingEntry m1 = mappingEntries.get(index1);
            double scale = (m1.fdr - m0.fdr) / (m1.probability - m0.probability);
            return m0.fdr + scale * (probability - m0.probability);
        }
    }

    private final Map<Long, ProbabilityToFDRCalculator> calculators =
            new HashMap<Long, ProbabilityToFDRCalculator>();

    private final IPhosphoNetXDAOFactory specificDAOFactory;

    ErrorModel(IPhosphoNetXDAOFactory specificDAOFactory)
    {
        this.specificDAOFactory = specificDAOFactory;
    }

    boolean passProtein(ProteinReferenceWithProbability protein, double falseDiscoveryRate)
    {
        ProbabilityToFDRCalculator calculator = getCalculator(protein.getDataSetID());
        return calculator.calculateFDR(protein.getProbability()) <= falseDiscoveryRate;
    }

    void setFalseDiscoveryRateFor(IdentifiedProtein protein)
    {
        long dataSetID = protein.getDataSetID();
        double probability = protein.getProbability();
        protein.setFalseDiscoveryRate(calculateFalsDiscoveryRate(dataSetID, probability));
    }

    double calculateFalsDiscoveryRate(long dataSetID, double probability)
    {
        ProbabilityToFDRCalculator calculator = getCalculator(dataSetID);
        double fdr = calculator.calculateFDR(probability);
        return fdr;
    }

    private ProbabilityToFDRCalculator getCalculator(long dataSetID)
    {
        ProbabilityToFDRCalculator calculator = calculators.get(dataSetID);
        if (calculator == null)
        {
            calculator = new ProbabilityToFDRCalculator();
            final IProteinQueryDAO dao = specificDAOFactory.getProteinQueryDAOFromPool();
            try
            {
                final DataSet<ProbabilityFDRMapping> mappings =
                        dao.getProbabilityFDRMapping(dataSetID);
                try
                {
                    for (ProbabilityFDRMapping probabilityFDRMapping : mappings)
                    {
                        double probability = probabilityFDRMapping.getProbability();
                        double falseDiscoveryRate = probabilityFDRMapping.getFalseDiscoveryRate();
                        calculator.add(probability, falseDiscoveryRate);
                    }
                } finally
                {
                    mappings.close();
                }
            } finally
            {
                specificDAOFactory.returnProteinQueryDAOToPool(dao);
            }
            calculator.init();
            calculators.put(dataSetID, calculator);
        }
        return calculator;
    }

}
