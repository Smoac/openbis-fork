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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class GetEntityTypeByIdExecutor implements IGetEntityTypeByIdExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @SuppressWarnings("unused")
    private GetEntityTypeByIdExecutor()
    {
    }

    public GetEntityTypeByIdExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public EntityTypePE get(IOperationContext context, EntityKind entityKind, IEntityTypeId typeId)
    {
        if (typeId == null)
        {
            throw new UserFailureException("Unspecified entity type id.");
        }
        EntityTypePE entityType = tryGetEntityType(entityKind, typeId);
        if (entityType == null)
        {
            throw new ObjectNotFoundException(typeId);
        }
        return entityType;
    }

    private EntityTypePE tryGetEntityType(EntityKind entityKind, IEntityTypeId typeId)
    {
        if (typeId instanceof EntityTypePermId)
        {
            String experimentType = ((EntityTypePermId) typeId).getPermId();
            IEntityTypeDAO entityTypeDAO = daoFactory.getEntityTypeDAO(entityKind);
            return entityTypeDAO.tryToFindEntityTypeByCode(experimentType);
        }
        throw new NotImplementedException("Entity type id [" + typeId + "] is of unknown type: "
                + typeId.getClass().getName());
    }

}
