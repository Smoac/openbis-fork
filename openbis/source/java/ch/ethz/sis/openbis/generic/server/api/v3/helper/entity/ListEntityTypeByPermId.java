/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.helper.entity;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
public class ListEntityTypeByPermId implements IListObjectById<EntityTypePermId, EntityTypePE>
{

    private IEntityTypeDAO entityTypeDAO;

    public ListEntityTypeByPermId(IDAOFactory daoFactory, EntityKind entityKind)
    {
        this.entityTypeDAO = daoFactory.getEntityTypeDAO(entityKind);
    }

    @Override
    public Class<EntityTypePermId> getIdClass()
    {
        return EntityTypePermId.class;
    }

    @Override
    public EntityTypePermId createId(EntityTypePE entityType)
    {
        return new EntityTypePermId(entityType.getCode());
    }

    @Override
    public List<EntityTypePE> listByIds(List<EntityTypePermId> ids)
    {
        List<String> permIds = new LinkedList<String>();

        for (EntityTypePermId id : ids)
        {
            permIds.add(id.getPermId());
        }

        return entityTypeDAO.tryToFindEntityTypeByCodes(permIds);
    }

}
