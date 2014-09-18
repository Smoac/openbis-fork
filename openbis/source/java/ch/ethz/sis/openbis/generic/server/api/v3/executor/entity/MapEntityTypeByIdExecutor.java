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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.entity;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.collection.ListTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.id.entitytype.IEntityTypeIdTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.IListerById;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.ListerById;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.entitytype.ListerByEntityTypePermIdId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class MapEntityTypeByIdExecutor implements IMapEntityTypeByIdExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @SuppressWarnings("unused")
    private MapEntityTypeByIdExecutor()
    {
    }

    public MapEntityTypeByIdExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public Map<IEntityTypeId, EntityTypePE> map(IOperationContext context, EntityKind entityKind, Collection<? extends IEntityTypeId> entityTypeIds)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.entitytype.IEntityTypeId> entityTypeIdsCore =
                new ListTranslator().translate(entityTypeIds, new IEntityTypeIdTranslator());

        @SuppressWarnings("rawtypes")
        List<IListerById> listers = new LinkedList<IListerById>();
        listers.add(new ListerByEntityTypePermIdId(daoFactory, entityKind));

        List<EntityTypePE> list = new ListerById(listers).list(entityTypeIdsCore);
        Map<IEntityTypeId, EntityTypePE> map = new LinkedHashMap<IEntityTypeId, EntityTypePE>();
        int index = 0;

        for (IEntityTypeId entityTypeId : entityTypeIds)
        {
            map.put(entityTypeId, list.get(index));
            index++;
        }

        return map;
    }

}
