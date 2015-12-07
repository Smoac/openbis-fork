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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.interfaces.ICreationIdHolder;
import ch.ethz.sis.openbis.generic.as.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;

/**
 * @author pkupczyk
 */
@Component
public abstract class AbstractSetEntityMultipleRelationsExecutor<ENTITY_CREATION extends ICreationIdHolder, ENTITY_PE, ENTITY_ID extends IObjectId>
        implements ISetEntityRelationsExecutor<ENTITY_CREATION, ENTITY_PE>
{

    @SuppressWarnings("unchecked")
    @Override
    public void set(IOperationContext context, Map<ENTITY_CREATION, ENTITY_PE> creationsMap)
    {
        Map<IObjectId, ENTITY_PE> relatedMap = getRelatedMap(context, creationsMap);

        set(context, creationsMap, (Map<ENTITY_ID, ENTITY_PE>) relatedMap);
    }

    private Map<IObjectId, ENTITY_PE> getRelatedMap(IOperationContext context, Map<ENTITY_CREATION, ENTITY_PE> creationsMap)
    {
        context.pushContextDescription("load related entities");

        Set<ENTITY_ID> relatedIds = new HashSet<ENTITY_ID>();
        for (ENTITY_CREATION creation : creationsMap.keySet())
        {
            addRelatedIds(relatedIds, creation);
        }

        Map<IObjectId, ENTITY_PE> relatedMap = new HashMap<IObjectId, ENTITY_PE>();

        for (Entry<ENTITY_CREATION, ENTITY_PE> entry : creationsMap.entrySet())
        {
            ENTITY_CREATION creation = entry.getKey();
            ENTITY_PE entity = entry.getValue();

            if (creation.getCreationId() != null)
            {
                relatedMap.put(creation.getCreationId(), entity);
            }
        }

        List<ENTITY_ID> toLoadIds = new LinkedList<ENTITY_ID>();

        for (ENTITY_ID relatedId : relatedIds)
        {
            if (relatedId instanceof CreationId)
            {
                if (false == relatedMap.containsKey(relatedId))
                {
                    throw new ObjectNotFoundException(relatedId);
                }
            }
            else
            {
                toLoadIds.add(relatedId);
            }
        }

        Map<ENTITY_ID, ENTITY_PE> loadedMap = map(context, toLoadIds);
        relatedMap.putAll(loadedMap);

        for (ENTITY_ID relatedId : relatedIds)
        {
            ENTITY_PE related = relatedMap.get(relatedId);

            if (related == null)
            {
                throw new ObjectNotFoundException(relatedId);
            }

            check(context, relatedId, related);
        }

        context.popContextDescription();

        return relatedMap;
    }

    protected void addRelatedIds(Set<ENTITY_ID> relatedIds, Collection<? extends ENTITY_ID> relatedIdsToAdd)
    {
        if (relatedIdsToAdd != null)
        {
            relatedIds.addAll(relatedIdsToAdd);
        }
    }

    protected void addRelatedIds(Set<ENTITY_ID> relatedIds, ENTITY_ID relatedIdToAdd)
    {
        if (relatedIdToAdd != null)
        {
            relatedIds.add(relatedIdToAdd);
        }
    }

    protected abstract void addRelatedIds(Set<ENTITY_ID> relatedIds, ENTITY_CREATION creation);

    protected abstract Map<ENTITY_ID, ENTITY_PE> map(IOperationContext context, List<ENTITY_ID> relatedIds);

    protected abstract void check(IOperationContext context, ENTITY_ID relatedId, ENTITY_PE related);

    protected abstract void set(IOperationContext context, Map<ENTITY_CREATION, ENTITY_PE> creationsMap, Map<ENTITY_ID, ENTITY_PE> relatedMap);

}
