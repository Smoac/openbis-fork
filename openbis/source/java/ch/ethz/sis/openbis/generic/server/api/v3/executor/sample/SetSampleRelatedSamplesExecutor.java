/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.CreationId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SetSampleRelatedSamplesExecutor implements ISetSampleRelatedSamplesExecutor
{

    @Autowired
    private IListSampleTechIdByIdExecutor listSampleTechIdByIdExecutor;

    @Autowired
    private ISetSampleContainerExecutor setSampleContainerExecutor;

    @Autowired
    private ISetSampleContainedExecutor setSampleContainedExecutor;

    @Autowired
    private ISetSampleParentsExecutor setSampleParentsExecutor;

    @Autowired
    private ISetSampleChildrenExecutor setSampleChildrenExecutor;

    @SuppressWarnings("unused")
    private SetSampleRelatedSamplesExecutor()
    {
    }

    public SetSampleRelatedSamplesExecutor(IListSampleTechIdByIdExecutor listSampleTechIdByIdExecutor,
            ISetSampleContainerExecutor setSampleContainerExecutor, ISetSampleContainedExecutor setSampleContainedExecutor,
            ISetSampleParentsExecutor setSampleParentsExecutor, ISetSampleChildrenExecutor setSampleChildrenExecutor)
    {
        this.listSampleTechIdByIdExecutor = listSampleTechIdByIdExecutor;
        this.setSampleContainerExecutor = setSampleContainerExecutor;
        this.setSampleContainedExecutor = setSampleContainedExecutor;
        this.setSampleParentsExecutor = setSampleParentsExecutor;
        this.setSampleChildrenExecutor = setSampleChildrenExecutor;
    }

    @Override
    public void set(IOperationContext context, Map<SampleCreation, SamplePE> creationsMap)
    {
        Map<ISampleId, Long> relatedSamplesMap = getRelatedSamplesMap(context, creationsMap);

        setSampleContainerExecutor.set(context, creationsMap, relatedSamplesMap);
        setSampleContainedExecutor.set(context, creationsMap, relatedSamplesMap);
        setSampleParentsExecutor.set(context, creationsMap, relatedSamplesMap);
        setSampleChildrenExecutor.set(context, creationsMap, relatedSamplesMap);
    }

    private Map<ISampleId, Long> getRelatedSamplesMap(IOperationContext context, Map<SampleCreation, SamplePE> createdSamples)
    {
        context.pushContextDescription("register samples - verify relationships");

        Collection<ISampleId> relatedSamples = getRelatedSamplesIds(createdSamples.keySet());
        HashMap<ISampleId, Long> sampleIdToTechIdMap = new HashMap<ISampleId, Long>();

        for (Entry<SampleCreation, SamplePE> entry : createdSamples.entrySet())
        {
            SampleCreation sampleCreation = entry.getKey();
            SamplePE sample = entry.getValue();

            if (sampleCreation.getCreationId() != null)
            {
                sampleIdToTechIdMap.put(sampleCreation.getCreationId(), sample.getId());
            }
        }

        List<ISampleId> knownSampleIds = new LinkedList<ISampleId>();

        for (ISampleId relatedSample : relatedSamples)
        {
            if (relatedSample instanceof CreationId)
            {
                if (false == sampleIdToTechIdMap.containsKey(relatedSample))
                {
                    throw new UserFailureException("Unknown sample creation id: " + relatedSample);
                }
            }
            else
            {
                knownSampleIds.add(relatedSample);
            }
        }
        Collection<Long> techIds = listSampleTechIdByIdExecutor.list(context, knownSampleIds);

        assert knownSampleIds.size() == techIds.size();

        Iterator<ISampleId> it1 = knownSampleIds.iterator();
        Iterator<Long> it2 = techIds.iterator();

        while (it1.hasNext())
        {
            sampleIdToTechIdMap.put(it1.next(), it2.next());
        }

        context.popContextDescription();

        return sampleIdToTechIdMap;
    }

    private Set<ISampleId> getRelatedSamplesIds(Collection<SampleCreation> sampleCreations)
    {
        Set<ISampleId> relatedSamples = new HashSet<ISampleId>();
        for (SampleCreation sampleCreation : sampleCreations)
        {
            if (sampleCreation.getContainerId() != null)
            {
                relatedSamples.add(sampleCreation.getContainerId());
            }
            if (sampleCreation.getContainedIds() != null)
            {
                relatedSamples.addAll(sampleCreation.getContainedIds());
            }
            if (sampleCreation.getChildIds() != null)
            {
                relatedSamples.addAll(sampleCreation.getChildIds());
            }
            if (sampleCreation.getParentIds() != null)
            {
                relatedSamples.addAll(sampleCreation.getParentIds());
            }
        }
        return relatedSamples;
    }
}
