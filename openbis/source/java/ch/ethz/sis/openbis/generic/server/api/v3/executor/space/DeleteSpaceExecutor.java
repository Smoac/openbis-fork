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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.space;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.as.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISpaceBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class DeleteSpaceExecutor extends AbstractDeleteEntityExecutor<Void, ISpaceId, SpacePE, SpaceDeletionOptions> implements IDeleteSpaceExecutor
{

    @Autowired
    IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Override
    protected Map<ISpaceId, SpacePE> map(IOperationContext context, List<? extends ISpaceId> entityIds)
    {
        return mapSpaceByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, ISpaceId entityId, SpacePE entity)
    {
        if (false == new SimpleSpaceValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(entityId);
        }
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, SpacePE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<SpacePE> spaces, SpaceDeletionOptions deletionOptions)
    {
        ISpaceBO spaceBO = businessObjectFactory.createSpaceBO(context.getSession());
        for (SpacePE space : spaces)
        {
            spaceBO.deleteByTechId(new TechId(space.getId()), deletionOptions.getReason());
        }
        return null;
    }

}
