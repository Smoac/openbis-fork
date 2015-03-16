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

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.space.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISpaceBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class DeleteSpaceExecutor implements IDeleteSpaceExecutor
{

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Override
    public void delete(IOperationContext context, List<? extends ISpaceId> spaceIds, SpaceDeletionOptions deletionOptions)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (spaceIds == null)
        {
            throw new IllegalArgumentException("Space ids cannot be null");
        }
        if (deletionOptions == null)
        {
            throw new IllegalArgumentException("Deletion options cannot be null");
        }
        if (deletionOptions.getReason() == null)
        {
            throw new IllegalArgumentException("Deletion reason cannot be null");
        }

        ISpaceBO spaceBO = businessObjectFactory.createSpaceBO(context.getSession());
        Map<ISpaceId, SpacePE> spaceMap = mapSpaceByIdExecutor.map(context, spaceIds);

        for (Map.Entry<ISpaceId, SpacePE> entry : spaceMap.entrySet())
        {
            ISpaceId spaceId = entry.getKey();
            SpacePE space = entry.getValue();

            if (false == new SimpleSpaceValidator().doValidation(context.getSession().tryGetPerson(), space))
            {
                throw new UnauthorizedObjectAccessException(spaceId);
            }

            spaceBO.deleteByTechId(new TechId(space.getId()), deletionOptions.getReason());
        }
    }

}
