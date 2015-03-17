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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.IMapProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.IObjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;

/**
 * @author pkupczyk
 */
@Component
public abstract class AbstractSetEntityRelationExecutor<ENTITY_CREATION, ENTITY_PE, RELATED_ID, RELATED_PE> implements
        ISetEntityRelationsExecutor<ENTITY_CREATION, ENTITY_PE>
{

    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

    @Override
    public void set(IOperationContext context, Map<ENTITY_CREATION, ENTITY_PE> entitiesMap)
    {
        List<RELATED_ID> relatedIds = new LinkedList<RELATED_ID>();

        for (ENTITY_CREATION creation : entitiesMap.keySet())
        {
            RELATED_ID relatedId = getRelatedId(creation);

            if (relatedId != null)
            {
                relatedIds.add(relatedId);
            }
        }

        Map<RELATED_ID, RELATED_PE> relatedMap = map(context, relatedIds);

        for (Map.Entry<ENTITY_CREATION, ENTITY_PE> entry : entitiesMap.entrySet())
        {
            ENTITY_CREATION creation = entry.getKey();
            ENTITY_PE entity = entry.getValue();
            RELATED_ID relatedId = getRelatedId(creation);

            if (relatedId == null)
            {
                check(context, entity, null, null);
            } else
            {
                RELATED_PE related = relatedMap.get(relatedId);

                if (related == null)
                {
                    throw new ObjectNotFoundException((IObjectId) relatedId);
                }

                check(context, entity, relatedId, related);
                set(context, entity, related);
            }
        }
    }

    protected abstract RELATED_ID getRelatedId(ENTITY_CREATION creation);

    protected abstract Map<RELATED_ID, RELATED_PE> map(IOperationContext context, List<RELATED_ID> relatedIds);

    protected abstract void check(IOperationContext context, ENTITY_PE entity, RELATED_ID relatedId, RELATED_PE related);

    protected abstract void set(IOperationContext context, ENTITY_PE entity, RELATED_PE related);

}
