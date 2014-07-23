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

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
public abstract class AbstractSetSampleRelatedSamplesExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    protected AbstractSetSampleRelatedSamplesExecutor()
    {
    }

    public AbstractSetSampleRelatedSamplesExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    public void set(IOperationContext context, Map<SampleCreation, SamplePE> creationsMap, Map<ISampleId, Long> techIdMap)
    {
        for (SampleCreation creation : creationsMap.keySet())
        {
            SamplePE sample = creationsMap.get(creation);
            Collection<? extends ISampleId> sampleIds = getRelatedSamplesIds(context, creation);

            if (sampleIds != null)
            {
                Collection<Long> sampleTechIds = new LinkedList<Long>();

                for (ISampleId sampleId : sampleIds)
                {
                    sampleTechIds.add(techIdMap.get(sampleId));
                }

                setRelatedSamples(context, sample, sampleTechIds);
            }
        }
    }

    protected abstract Collection<? extends ISampleId> getRelatedSamplesIds(IOperationContext context, SampleCreation creation);

    protected abstract void setRelatedSamples(IOperationContext context, SamplePE sample, Collection<Long> relatedSamplesTechIds);

    public IDAOFactory getDaoFactory()
    {
        return daoFactory;
    }

}
