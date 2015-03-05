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

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityFieldUpdateValueRelationExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleContainerExecutor extends AbstractUpdateEntityFieldUpdateValueRelationExecutor<SampleUpdate, SamplePE, ISampleId, SamplePE>
        implements IUpdateSampleContainerExecutor
{

    @Override
    protected ISampleId getRelatedId(SamplePE related)
    {
        return new SampleIdentifier(related.getIdentifier());
    }

    @Override
    protected SamplePE getCurrentlyRelated(SamplePE entity)
    {
        return entity.getContainer();
    }

    @Override
    protected FieldUpdateValue<ISampleId> getRelatedUpdate(SampleUpdate update)
    {
        return update.getContainerId();
    }

    @Override
    protected Map<ISampleId, SamplePE> map(IOperationContext context, List<ISampleId> relatedIds)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void check(IOperationContext context, SamplePE entity, ISampleId relatedId, SamplePE related)
    {
        if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void update(IOperationContext context, SamplePE entity, SamplePE related)
    {
        if (related == null)
        {
            UpdateSampleContainedExecutor.remove(relationshipService, context, entity.getContainer(), entity);
        } else
        {
            UpdateSampleContainedExecutor.add(relationshipService, context, related, entity);
        }
    }

}
