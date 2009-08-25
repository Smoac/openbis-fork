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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinWithAbundances;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ListProteinOriginalDataProvider implements IOriginalDataProvider<ProteinInfo>
{
    private final IPhosphoNetXServer server;
    private final String sessionToken;
    private final TechId experimentID;
    private final double falseDiscoveryRate;
    private final AggregateFunction aggregateFunction;

    ListProteinOriginalDataProvider(IPhosphoNetXServer server, String sessionToken,
            TechId experimentID, double falseDiscoveryRate, AggregateFunction function)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.experimentID = experimentID;
        this.falseDiscoveryRate = falseDiscoveryRate;
        this.aggregateFunction = function;
    }
    
    public List<ProteinInfo> getOriginalData() throws UserFailureException
    {
        Collection<ProteinWithAbundances> proteins =
                server.listProteinsByExperiment(sessionToken, experimentID, falseDiscoveryRate);
        List<ProteinInfo> infos = new ArrayList<ProteinInfo>(proteins.size());
        for (ProteinWithAbundances protein : proteins)
        {
            ProteinInfo proteinInfo = new ProteinInfo();
            proteinInfo.setId(new TechId(protein.getId()));
            proteinInfo.setUniprotID(protein.getUniprotID());
            proteinInfo.setDescription(protein.getDescription());
            proteinInfo.setExperimentID(experimentID);
            Map<Long, Double> abundances = new HashMap<Long, Double>();
            Set<Long> sampleIDs = protein.getSampleIDs();
            for (Long id : sampleIDs)
            {
                double[] abundanceValues = protein.getAbundancesForSample(id);
                if (abundanceValues.length > 0)
                {
                    abundances.put(id, aggregateFunction.aggregate(abundanceValues));
                }
            }
            proteinInfo.setAbundances(abundances);
            infos.add(proteinInfo);
        }
        return infos;
    }

}
