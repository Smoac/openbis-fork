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
import java.util.Map;

import net.lemnik.eodsql.DataSet;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.ProbabilityToFDRCalculator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.IdentifiedProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProbabilityFDRMapping;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProtein;

/**
 * @author Franz-Josef Elmer
 */
class ErrorModel
{
    private final Map<Long, ProbabilityToFDRCalculator> calculators =
            new HashMap<Long, ProbabilityToFDRCalculator>();

    private final IPhosphoNetXDAOFactory specificDAOFactory;

    ErrorModel(IPhosphoNetXDAOFactory specificDAOFactory)
    {
        this.specificDAOFactory = specificDAOFactory;
    }

    boolean passProtein(ProteinReferenceWithProtein protein, double falseDiscoveryRate)
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
            IProteinQueryDAO dao = specificDAOFactory.getProteinQueryDAO();
            DataSet<ProbabilityFDRMapping> mappings = dao.getProbabilityFDRMapping(dataSetID);
            for (ProbabilityFDRMapping probabilityFDRMapping : mappings)
            {
                double probability = probabilityFDRMapping.getProbability();
                double falseDiscoveryRate = probabilityFDRMapping.getFalseDiscoveryRate();
                calculator.add(probability, falseDiscoveryRate);
            }
            mappings.close();
            calculator.init();
            calculators.put(dataSetID, calculator);
        }
        return calculator;
    }

}
