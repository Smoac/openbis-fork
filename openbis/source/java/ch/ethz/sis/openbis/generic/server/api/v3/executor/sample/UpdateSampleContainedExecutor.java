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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleContainedExecutor extends AbstractUpdateSampleRelatedSamplesExecutor implements IUpdateSampleContainedExecutor
{

    @Resource(name = ComponentNames.RELATIONSHIP_SERVICE)
    private IRelationshipService relationshipService;

    @Override
    protected IdListUpdateValue<? extends ISampleId> getRelatedSamplesUpdate(IOperationContext context, SampleUpdate update)
    {
        return update.getContainedIds();
    }

    @Override
    protected void setRelatedSamples(IOperationContext context, SamplePE container, Collection<SamplePE> contained)
    {
        Set<SamplePE> existingContained = new HashSet<SamplePE>(container.getContained());
        Set<SamplePE> newContained = new HashSet<SamplePE>(contained);

        for (SamplePE anExistingContained : existingContained)
        {
            if (false == newContained.contains(existingContained))
            {
                removeFromContainer(relationshipService, context, anExistingContained, container);
            }
        }

        for (SamplePE aNewContained : newContained)
        {
            if (false == existingContained.contains(aNewContained))
            {
                assignToContainer(relationshipService, context, aNewContained, container);
            }
        }
    }

    @Override
    protected void addRelatedSamples(IOperationContext context, SamplePE container, Collection<SamplePE> contained)
    {
        for (SamplePE aContained : contained)
        {
            assignToContainer(relationshipService, context, aContained, container);
        }
    }

    @Override
    protected void removeRelatedSamples(IOperationContext context, SamplePE container, Collection<SamplePE> contained)
    {
        for (SamplePE aContained : contained)
        {
            removeFromContainer(relationshipService, context, aContained, container);
        }
    }

    static void assignToContainer(IRelationshipService service, IOperationContext context, SamplePE sample, SamplePE container)
    {
        SamplePE previousContainer = sample.getContainer();

        if (previousContainer != null)
        {
            if (previousContainer.equals(container))
            {
                // nothing to do
                return;
            } else
            {
                removeFromContainer(service, context, sample, previousContainer);
            }
        }

        Set<SamplePE> contained = new HashSet<SamplePE>(container.getContained());
        contained.add(sample);
        container.setContained(new ArrayList<SamplePE>(contained));

        service.assignSampleToContainer(context.getSession(), sample, container);
    }

    static void removeFromContainer(IRelationshipService service, IOperationContext context, SamplePE sample, SamplePE container)
    {
        SamplePE previousContainer = sample.getContainer();

        if (previousContainer != null && previousContainer.equals(container))
        {
            Set<SamplePE> contained = new HashSet<SamplePE>(container.getContained());
            contained.remove(sample);
            container.setContained(new ArrayList<SamplePE>(contained));

            service.removeSampleFromContainer(context.getSession(), sample);
        }
    }

}
