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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleRelatedSamplesExecutor implements IUpdateSampleRelatedSamplesExecutor
{

    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Autowired
    private IUpdateSampleContainerExecutor updateSampleContainerExecutor;

    @Autowired
    private IUpdateSampleContainedExecutor updateSampleContainedExecutor;

    @Autowired
    private IUpdateSampleParentsExecutor updateSampleParentsExecutor;

    @Autowired
    private IUpdateSampleChildrenExecutor updateSampleChildrenExecutor;

    @SuppressWarnings("unused")
    private UpdateSampleRelatedSamplesExecutor()
    {
    }

    public UpdateSampleRelatedSamplesExecutor(IMapSampleByIdExecutor mapSampleByIdExecutor,
            IUpdateSampleContainerExecutor updateSampleContainerExecutor, IUpdateSampleContainedExecutor updateSampleContainedExecutor,
            IUpdateSampleParentsExecutor updateSampleParentsExecutor, IUpdateSampleChildrenExecutor updateSampleChildrenExecutor)
    {
        this.mapSampleByIdExecutor = mapSampleByIdExecutor;
        this.updateSampleContainerExecutor = updateSampleContainerExecutor;
        this.updateSampleContainedExecutor = updateSampleContainedExecutor;
        this.updateSampleParentsExecutor = updateSampleParentsExecutor;
        this.updateSampleChildrenExecutor = updateSampleChildrenExecutor;
    }

    @Override
    public void update(IOperationContext context, Map<SampleUpdate, SamplePE> updateMap)
    {
        Map<ISampleId, SamplePE> samplesMap = getRelatedSamplesMap(context, updateMap.keySet());
        updateSampleContainerExecutor.update(context, updateMap, samplesMap);
        updateSampleContainedExecutor.update(context, updateMap, samplesMap);
        updateSampleParentsExecutor.update(context, updateMap, samplesMap);
        updateSampleChildrenExecutor.update(context, updateMap, samplesMap);
    }

    private Map<ISampleId, SamplePE> getRelatedSamplesMap(IOperationContext context, Collection<SampleUpdate> updates)
    {
        context.pushContextDescription("update samples - verify related entities exist");

        Collection<ISampleId> relatedSamplesIds = getRelatedSamplesIds(updates);
        Map<ISampleId, SamplePE> relatedSamplesMap = mapSampleByIdExecutor.map(context, relatedSamplesIds);

        context.popContextDescription();

        return relatedSamplesMap;
    }

    private Set<ISampleId> getRelatedSamplesIds(Collection<SampleUpdate> updates)
    {
        Set<ISampleId> ids = new HashSet<ISampleId>();
        for (SampleUpdate sampleUpdate : updates)
        {
            if (sampleUpdate.getContainerId() != null && sampleUpdate.getContainerId().isModified())
            {
                ids.add(sampleUpdate.getContainerId().getValue());
            }

            addRelatedSamplesIds(ids, sampleUpdate.getContainedIds());
            addRelatedSamplesIds(ids, sampleUpdate.getParentIds());
            addRelatedSamplesIds(ids, sampleUpdate.getChildIds());
        }
        return ids;
    }

    private void addRelatedSamplesIds(Set<ISampleId> ids, ListUpdateValue<ISampleId> listUpdate)
    {
        if (listUpdate != null && listUpdate.hasActions())
        {
            for (ListUpdateAction<ISampleId> action : listUpdate.getActions())
            {
                ids.addAll(action.getIds());
            }
        }
    }

}
