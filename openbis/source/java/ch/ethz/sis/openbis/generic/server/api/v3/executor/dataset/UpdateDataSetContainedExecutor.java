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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.Collection;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityListUpdateValueRelationExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetContainedExecutor extends AbstractUpdateEntityListUpdateValueRelationExecutor<DataSetUpdate, DataPE, IDataSetId, DataPE>
        implements IUpdateDataSetContainedExecutor
{

    @Override
    protected Collection<DataPE> getCurrentlyRelated(DataPE entity)
    {
        return entity.getContainedDataSets();
    }

    @Override
    protected IdListUpdateValue<? extends IDataSetId> getRelatedUpdate(IOperationContext context, DataSetUpdate update)
    {
        return update.getContainedIds();
    }

    @Override
    protected void check(IOperationContext context, IDataSetId relatedId, DataPE related)
    {
        if (false == new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(context.getSession().tryGetPerson(), related))
        {
            throw new UnauthorizedObjectAccessException(relatedId);
        }
    }

    @Override
    protected void add(IOperationContext context, DataPE entity, DataPE related)
    {
        relationshipService.assignDataSetToContainer(context.getSession(), related, entity);
    }

    @Override
    protected void remove(IOperationContext context, DataPE entity, DataPE related)
    {
        relationshipService.removeDataSetFromContainer(context.getSession(), related, entity);
    }

}
