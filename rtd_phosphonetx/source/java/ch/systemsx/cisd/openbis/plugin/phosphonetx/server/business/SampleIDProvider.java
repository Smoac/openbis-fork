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

import java.util.LinkedHashMap;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Implementation based on {@link ISampleDAO}.
 *
 * @author Franz-Josef Elmer
 */
class SampleIDProvider implements ISampleIDProvider
{
    private final ISampleDAO sampleDAO;
    private final Map<String, SamplePE> samples = new LinkedHashMap<String, SamplePE>();

    SampleIDProvider(ISampleDAO sampleDAO)
    {
        this.sampleDAO = sampleDAO;
    }
    
    public long getSampleIDOrParentSampleID(String samplePermID)
    {
        return HibernateUtils.getId(getSampleOrParentSample(samplePermID));
    }

    public SamplePE getSampleOrParentSample(String samplePermID)
    {
        SamplePE sample = samples.get(samplePermID);
        if (sample == null)
        {
            sample = sampleDAO.tryToFindByPermID(samplePermID);
            if (sample == null)
            {
                throw new UserFailureException("No sample found for permID " + samplePermID);
            }
            SamplePE parentSample = sample.getGeneratedFrom();
            if (parentSample != null)
            {
                sample = parentSample;
            }
            samples.put(samplePermID, sample);
        }
        return sample;
    }
}
