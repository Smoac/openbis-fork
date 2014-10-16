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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleChildrenExecutor extends AbstractUpdateSampleRelatedSamplesExecutor implements IUpdateSampleChildrenExecutor
{

    @Resource(name = ComponentNames.RELATIONSHIP_SERVICE)
    private IRelationshipService relationshipService;

    @Override
    protected ListUpdateValue<? extends ISampleId> getRelatedSamplesUpdate(IOperationContext context, SampleUpdate update)
    {
        return update.getChildIds();
    }

    @Override
    protected void setRelatedSamples(IOperationContext context, SamplePE parent, Collection<SamplePE> children)
    {
        Set<SamplePE> existingChildren = new HashSet<SamplePE>(parent.getChildren());
        Set<SamplePE> newChildren = new HashSet<SamplePE>(children);

        for (SamplePE anExistingChild : existingChildren)
        {
            if (false == newChildren.contains(anExistingChild))
            {
                relationshipService.removeParentFromSample(context.getSession(), anExistingChild, parent);
            }
        }

        for (SamplePE aNewChild : newChildren)
        {
            if (false == existingChildren.contains(aNewChild))
            {
                relationshipService.addParentToSample(context.getSession(), aNewChild, parent);
            }
        }
    }

    @Override
    protected void addRelatedSamples(IOperationContext context, SamplePE parent, Collection<SamplePE> children)
    {
        Set<SamplePE> existingChildren = new HashSet<SamplePE>(parent.getChildren());

        for (SamplePE aChild : children)
        {
            if (false == existingChildren.contains(aChild))
            {
                relationshipService.addParentToSample(context.getSession(), aChild, parent);
            }
        }
    }

    @Override
    protected void removeRelatedSamples(IOperationContext context, SamplePE parent, Collection<SamplePE> children)
    {
        Set<SamplePE> existingChildren = new HashSet<SamplePE>(parent.getChildren());

        for (SamplePE aChild : children)
        {
            if (existingChildren.contains(aChild))
            {
                relationshipService.removeParentFromSample(context.getSession(), aChild, parent);
            }
        }
    }

}
