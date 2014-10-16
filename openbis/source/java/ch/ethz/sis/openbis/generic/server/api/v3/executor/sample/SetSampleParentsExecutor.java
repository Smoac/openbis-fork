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
import java.util.HashSet;
import java.util.Set;

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
public class SetSampleParentsExecutor extends AbstractSetSampleRelatedSamplesExecutor implements ISetSampleParentsExecutor
{

    @Resource(name = ComponentNames.RELATIONSHIP_SERVICE)
    private IRelationshipService relationshipService;

    @Override
    protected Collection<? extends ISampleId> getRelatedSamplesIds(IOperationContext context, SampleCreation creation)
    {
        return creation.getParentIds();
    }

    @Override
    protected void setRelatedSamples(IOperationContext context, SamplePE child, Collection<SamplePE> parents)
    {
        Set<SamplePE> existingParents = new HashSet<SamplePE>(child.getParents());

        context.pushContextDescription("set parents for sample " + child.getCode());

        for (SamplePE parent : parents)
        {
            if (false == existingParents.contains(parent))
            {
                relationshipService.addParentToSample(context.getSession(), child, parent);
                existingParents.add(parent);
            }
        }

        context.popContextDescription();
    }
}
