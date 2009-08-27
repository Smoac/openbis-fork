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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbability;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinWithAbundances;

/**
 * @author Franz-Josef Elmer
 */
class AbundanceManager
{
    private final Map<String, ProteinWithAbundances> proteins =
            new LinkedHashMap<String, ProteinWithAbundances>();
    
    private final Set<Long> sampleIDs = new LinkedHashSet<Long>();

    public void handle(ProteinReferenceWithProbability proteinReference)
    {
        ProteinWithAbundances protein = getOrCreateProtein(proteinReference);
        Long sampleID = proteinReference.getSampleID();
        if (sampleID != null)
        {
            sampleIDs.add(sampleID);
            protein.addAbundanceFor(sampleID, proteinReference.getAbundance());
        }
    }
    
    private ProteinWithAbundances getOrCreateProtein(ProteinReferenceWithProbability proteinReference)
    {
        String accessionNumber = proteinReference.getAccessionNumber();
        ProteinWithAbundances protein = proteins.get(accessionNumber);
        if (protein == null)
        {
            protein = new ProteinWithAbundances();
            protein.setId(proteinReference.getId());
            protein.setDescription(proteinReference.getDescription());
            protein.setAccessionNumber(accessionNumber);
            proteins.put(accessionNumber, protein);
        }
        return protein;
    }

    public Collection<ProteinWithAbundances> getProteinsWithAbundances()
    {
        return proteins.values();
    }

    public final Set<Long> getSampleIDs()
    {
        return sampleIDs;
    }

}
