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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinAbundance;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinWithAbundances;

/**
 * @author Franz-Josef Elmer
 */
class AbundanceManager
{
    private final List<ProteinWithAbundances> proteins = new ArrayList<ProteinWithAbundances>();
    
    private final ISampleProvider sampleProvider;
    
    private final Set<Long> sampleIDs = new TreeSet<Long>();

    AbundanceManager(ISampleProvider sampleProvider)
    {
        this.sampleProvider = sampleProvider;
    }
    
    public void handle(ProteinReferenceWithProtein proteinReference, List<ProteinAbundance> listOrNull)
    {
        String accessionNumber = proteinReference.getAccessionNumber();
        ProteinWithAbundances protein = new ProteinWithAbundances();
        protein.setCoverage(proteinReference.getCoverage());
        protein.setId(proteinReference.getId());
        protein.setDescription(proteinReference.getDescription());
        protein.setAccessionNumber(accessionNumber);
        proteins.add(protein);
        if (listOrNull != null)
        {
            for (ProteinAbundance proteinAbundance : listOrNull)
            {
                String samplePermID = proteinAbundance.getSamplePermID();
                Long sampleID = getSampleIDOrParentSampleID(samplePermID);
                sampleIDs.add(sampleID);
                protein.addAbundanceFor(sampleID, proteinAbundance.getAbundance());
            }
        }
    }
    
    private Long getSampleIDOrParentSampleID(String samplePermID)
    {
        Sample sample = sampleProvider.getSample(samplePermID);
        Sample parent = sample.getGeneratedFrom();
        return parent == null ? sample.getId() : parent.getId();
    }

    public Collection<ProteinWithAbundances> getProteinsWithAbundances()
    {
        return proteins;
    }

    public final Collection<Long> getSampleIDs()
    {
        return sampleIDs;
    }


}
