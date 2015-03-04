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
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityMultipleRelationsExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleRelatedSamplesExecutor extends AbstractUpdateEntityMultipleRelationsExecutor<SampleUpdate, SamplePE, ISampleId, SamplePE>
        implements IUpdateSampleRelatedSamplesExecutor
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

    @Override
    protected void addRelatedIds(Set<ISampleId> relatedIds, SampleUpdate update)
    {
        addRelatedIds(relatedIds, update.getContainerId());
        addRelatedIds(relatedIds, update.getContainedIds());
        addRelatedIds(relatedIds, update.getParentIds());
        addRelatedIds(relatedIds, update.getChildIds());
    }

    @Override
    protected Map<ISampleId, SamplePE> map(IOperationContext context, Collection<ISampleId> relatedIds)
    {
        return mapSampleByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void update(IOperationContext context, Map<SampleUpdate, SamplePE> entitiesMap, Map<ISampleId, SamplePE> relatedMap)
    {
        updateSampleContainerExecutor.update(context, entitiesMap, relatedMap);
        updateSampleContainedExecutor.update(context, entitiesMap, relatedMap);
        updateSampleParentsExecutor.update(context, entitiesMap, relatedMap);
        updateSampleChildrenExecutor.update(context, entitiesMap, relatedMap);
    }

}
