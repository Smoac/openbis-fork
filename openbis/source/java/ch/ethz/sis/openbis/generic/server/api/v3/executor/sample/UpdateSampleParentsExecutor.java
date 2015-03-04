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

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityListUpdateValueRelationExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleParentsExecutor extends AbstractUpdateEntityListUpdateValueRelationExecutor<SampleUpdate, SamplePE, ISampleId, SamplePE>
        implements IUpdateSampleParentsExecutor
{

    @Override
    protected IdListUpdateValue<? extends ISampleId> getRelatedUpdate(IOperationContext context, SampleUpdate update)
    {
        return update.getParentIds();
    }

    @Override
    protected void check(IOperationContext context, ISampleId relatedId, SamplePE related)
    {
        if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void set(IOperationContext context, SamplePE child, Collection<SamplePE> parents)
    {
        Set<SamplePE> existingParents = new HashSet<SamplePE>(child.getParents());
        Set<SamplePE> newParents = new HashSet<SamplePE>(parents);

        for (SamplePE anExistingParent : existingParents)
        {
            if (false == newParents.contains(anExistingParent))
            {
                relationshipService.removeParentFromSample(context.getSession(), child, anExistingParent);
            }
        }

        for (SamplePE aNewParent : newParents)
        {
            if (false == existingParents.contains(aNewParent))
            {
                relationshipService.addParentToSample(context.getSession(), child, aNewParent);
            }
        }
    }

    @Override
    protected void add(IOperationContext context, SamplePE child, Collection<SamplePE> parents)
    {
        Set<SamplePE> existingParents = new HashSet<SamplePE>(child.getParents());

        for (SamplePE aParent : parents)
        {
            if (false == existingParents.contains(aParent))
            {
                relationshipService.addParentToSample(context.getSession(), child, aParent);
            }
        }
    }

    @Override
    protected void remove(IOperationContext context, SamplePE child, Collection<SamplePE> parents)
    {
        Set<SamplePE> existingParents = new HashSet<SamplePE>(child.getParents());

        for (SamplePE aParent : parents)
        {
            if (existingParents.contains(aParent))
            {
                relationshipService.removeParentFromSample(context.getSession(), child, aParent);
            }
        }
    }

}
