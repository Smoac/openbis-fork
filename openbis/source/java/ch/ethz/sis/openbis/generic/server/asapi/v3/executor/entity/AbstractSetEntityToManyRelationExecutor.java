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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.Resource;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;

/**
 * @author pkupczyk
 */
public abstract class AbstractSetEntityToManyRelationExecutor<ENTITY_CREATION, ENTITY_PE, RELATED_ID, RELATED_PE>
{

    @Resource(name = ComponentNames.RELATIONSHIP_SERVICE)
    protected IRelationshipService relationshipService;

    public void set(IOperationContext context, Map<ENTITY_CREATION, ENTITY_PE> creationsMap, Map<RELATED_ID, RELATED_PE> relatedMap)
    {
        Collection<RELATED_PE> allSet = new HashSet<RELATED_PE>();

        for (ENTITY_CREATION creation : creationsMap.keySet())
        {
            ENTITY_PE entity = creationsMap.get(creation);
            Collection<? extends RELATED_ID> relatedIds = getRelatedIds(context, creation);

            if (relatedIds != null)
            {
                Collection<RELATED_PE> related = new LinkedList<RELATED_PE>();

                for (RELATED_ID relatedId : relatedIds)
                {
                    related.add(relatedMap.get(relatedId));
                }

                if (false == related.isEmpty())
                {
                    setRelated(context, entity, related);
                    allSet.addAll(related);
                }
            }
        }

        postSet(context, allSet);
    }

    protected void postSet(IOperationContext context, Collection<RELATED_PE> allSet)
    {
        // by default do nothing
    }

    protected abstract Collection<? extends RELATED_ID> getRelatedIds(IOperationContext context, ENTITY_CREATION creation);

    protected abstract void setRelated(IOperationContext context, ENTITY_PE entity, Collection<RELATED_PE> related);

}
