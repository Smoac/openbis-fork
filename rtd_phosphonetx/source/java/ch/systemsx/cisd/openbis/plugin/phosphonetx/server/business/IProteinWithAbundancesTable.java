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

import java.util.Collection;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinWithAbundances;

/**
 * Business object for loading proteins together with their abundances.
 *
 * @author Franz-Josef Elmer
 */
public interface IProteinWithAbundancesTable
{
    /**
     * Loads proteins of all data sets registered for the specified experiment. All proteins
     * are filtered out if their false discovery rate is larger then the specifie one.
     */
    public void load(String experimentPermID, double falseDiscoveryRate);
    
    public Collection<ProteinWithAbundances> getProteinsWithAbundances();

    public Collection<Long> getSampleIDs();

}
