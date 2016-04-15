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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.Progress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

/**
 * @author pkupczyk
 */
@Component
public abstract class AbstractSetEntityMultipleRelationsExecutor<ENTITY_CREATION, ENTITY_PE, RELATED_ID extends IObjectId, RELATED_PE>
        implements ISetEntityRelationsExecutor<ENTITY_CREATION, ENTITY_PE>
{

    @Override
    public void set(IOperationContext context, Map<ENTITY_CREATION, ENTITY_PE> creationsMap)
    {
        Map<RELATED_ID, RELATED_PE> relatedMap = getRelatedMap(context, creationsMap);

        set(context, creationsMap, relatedMap);
    }

    private Map<RELATED_ID, RELATED_PE> getRelatedMap(IOperationContext context, Map<ENTITY_CREATION, ENTITY_PE> creationsMap)
    {
        context.pushProgress(new Progress("load related entities"));

        Set<RELATED_ID> relatedIds = new HashSet<RELATED_ID>();
        for (Entry<ENTITY_CREATION, ENTITY_PE> entry : creationsMap.entrySet())
        {
            addRelatedIds(relatedIds, entry.getKey(), entry.getValue());
        }

        Map<RELATED_ID, RELATED_PE> relatedMap = new HashMap<RELATED_ID, RELATED_PE>();

        for (Entry<ENTITY_CREATION, ENTITY_PE> entry : creationsMap.entrySet())
        {
            ENTITY_CREATION creation = entry.getKey();
            ENTITY_PE entity = entry.getValue();

            addRelated(relatedMap, creation, entity);
        }

        List<RELATED_ID> toLoadIds = new LinkedList<RELATED_ID>();

        for (RELATED_ID relatedId : relatedIds)
        {
            if (relatedId instanceof CreationId)
            {
                if (false == relatedMap.containsKey(relatedId))
                {
                    throw new ObjectNotFoundException(relatedId);
                }
            } else
            {
                toLoadIds.add(relatedId);
            }
        }

        Map<RELATED_ID, RELATED_PE> loadedMap = map(context, toLoadIds);
        relatedMap.putAll(loadedMap);

        for (RELATED_ID relatedId : relatedIds)
        {
            RELATED_PE related = relatedMap.get(relatedId);

            if (related == null)
            {
                throw new ObjectNotFoundException(relatedId);
            }

            check(context, relatedId, related);
        }

        context.popProgress();

        return relatedMap;
    }

    protected void addRelatedIds(Set<RELATED_ID> relatedIds, Collection<? extends RELATED_ID> relatedIdsToAdd)
    {
        if (relatedIdsToAdd != null)
        {
            relatedIds.addAll(relatedIdsToAdd);
        }
    }

    protected void addRelatedIds(Set<RELATED_ID> relatedIds, RELATED_ID relatedIdToAdd)
    {
        if (relatedIdToAdd != null)
        {
            relatedIds.add(relatedIdToAdd);
        }
    }

    protected void addRelated(Map<RELATED_ID, RELATED_PE> relatedMap, RELATED_ID relatedId, RELATED_PE related)
    {
        if (relatedId != null && related != null)
        {
            relatedMap.put(relatedId, related);
        }
    }

    protected abstract void addRelatedIds(Set<RELATED_ID> relatedIds, ENTITY_CREATION creation, ENTITY_PE entity);

    protected abstract void addRelated(Map<RELATED_ID, RELATED_PE> relatedMap, ENTITY_CREATION creation, ENTITY_PE entity);

    protected abstract Map<RELATED_ID, RELATED_PE> map(IOperationContext context, List<RELATED_ID> relatedIds);

    protected abstract void check(IOperationContext context, RELATED_ID relatedId, RELATED_PE related);

    protected abstract void set(IOperationContext context, Map<ENTITY_CREATION, ENTITY_PE> creationsMap, Map<RELATED_ID, RELATED_PE> relatedMap);

}
