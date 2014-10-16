/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
public abstract class AbstractSetSampleRelatedSamplesExecutor
{

    public void set(IOperationContext context, Map<SampleCreation, SamplePE> creationsMap, Map<ISampleId, SamplePE> sampleMap)
    {
        for (SampleCreation creation : creationsMap.keySet())
        {
            SamplePE sample = creationsMap.get(creation);
            Collection<? extends ISampleId> relatedSampleIds = getRelatedSamplesIds(context, creation);

            if (relatedSampleIds != null)
            {
                Collection<SamplePE> relatedSamples = new LinkedList<SamplePE>();

                for (ISampleId relatedSampleId : relatedSampleIds)
                {
                    relatedSamples.add(sampleMap.get(relatedSampleId));
                }

                if (false == relatedSamples.isEmpty())
                {
                    setRelatedSamples(context, sample, relatedSamples);
                }
            }
        }
    }

    protected abstract Collection<? extends ISampleId> getRelatedSamplesIds(IOperationContext context, SampleCreation creation);

    protected abstract void setRelatedSamples(IOperationContext context, SamplePE sample, Collection<SamplePE> relatedSamples);

}
