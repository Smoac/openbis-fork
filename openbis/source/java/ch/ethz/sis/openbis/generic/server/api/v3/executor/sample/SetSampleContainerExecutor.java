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

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SetSampleContainerExecutor implements ISetSampleContainerExecutor
{

    @Resource(name = ComponentNames.RELATIONSHIP_SERVICE)
    private IRelationshipService relationshipService;

    @Override
    public void set(IOperationContext context, Map<SampleCreation, SamplePE> creationsMap, Map<ISampleId, SamplePE> sampleMap)
    {
        for (SampleCreation creation : creationsMap.keySet())
        {
            context.pushContextDescription("set container for sample " + creation.getCode());

            SamplePE sample = creationsMap.get(creation);
            ISampleId containerId = creation.getContainerId();
            if (containerId != null)
            {
                SamplePE container = sampleMap.get(containerId);
                relationshipService.assignSampleToContainer(context.getSession(), sample, container);
            }

            context.popContextDescription();
        }
    }

}
