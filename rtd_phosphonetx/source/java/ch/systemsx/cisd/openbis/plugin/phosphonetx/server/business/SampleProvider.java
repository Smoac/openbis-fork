/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 *
 * @author Franz-Josef Elmer
 */
class SampleProvider implements ISampleProvider
{
    private final Session session;
    private final IBusinessObjectFactory boFactory;
    
    private Map<String, Sample> samplesByPermIDs;
    
    SampleProvider(Session session, IBusinessObjectFactory boFactory)
    {
        this.session = session;
        this.boFactory = boFactory;
    }
    
    public void loadByExperimentID(TechId experimentID)
    {
        samplesByPermIDs = new HashMap<String, Sample>();
        ListSampleCriteria criteria = ListSampleCriteria.createForExperiment(experimentID);
        ISampleLister lister = boFactory.createSampleLister(session);
        ListOrSearchSampleCriteria criteria2 = new ListOrSearchSampleCriteria(criteria);
        criteria2.setEnrichDependentSamplesWithProperties(true);
        gatherSamplesAndAncestorsRecursively(lister, criteria2);
    }

    private void gatherSamplesAndAncestorsRecursively(ISampleLister lister,
            ListOrSearchSampleCriteria criteria)
    {
        List<Sample> list = lister.list(criteria);
        Set<Long> sampleIDs = new LinkedHashSet<Long>();
        for (Sample sample : list)
        {
            samplesByPermIDs.put(sample.getPermId(), sample);
            sampleIDs.add(sample.getId());
        }
        if (sampleIDs.isEmpty())
        {
            return;
        }
        ListSampleCriteria criteria2 =
            ListSampleCriteria.createForChildren(sampleIDs);
        ListOrSearchSampleCriteria criteria3 = new ListOrSearchSampleCriteria(criteria2);
        criteria3.setEnrichDependentSamplesWithProperties(true);
        gatherSamplesAndAncestorsRecursively(lister, criteria3);
    }

    public Sample getSample(String permID)
    {
        Sample sample = samplesByPermIDs.get(permID);
        if (sample == null)
        {
            throw new UserFailureException(
                    "No sample with following perm ID registered in openBIS: " + permID);
        }
        return sample;
    }
}
