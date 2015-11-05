/*
 * Copyright 2015 ETH Zuerich, CISD
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.IObjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;

/**
 * @author pkupczyk
 */
public abstract class AbstractUpdateEntityToManyRelationExecutor<ENTITY_UPDATE, ENTITY_PE, RELATED_ID, RELATED_PE> implements
        IUpdateEntityRelationsWithCacheExecutor<ENTITY_UPDATE, ENTITY_PE, RELATED_ID, RELATED_PE>
{

    @Resource(name = ComponentNames.RELATIONSHIP_SERVICE)
    protected IRelationshipService relationshipService;

    @Override
    public void update(IOperationContext context, Map<ENTITY_UPDATE, ENTITY_PE> entitiesMap, Map<RELATED_ID, RELATED_PE> relatedMap)
    {
        for (ENTITY_UPDATE update : entitiesMap.keySet())
        {
            IdListUpdateValue<? extends RELATED_ID> listUpdate = getRelatedUpdate(context, update);

            if (listUpdate != null && listUpdate.hasActions())
            {
                ENTITY_PE entity = entitiesMap.get(update);

                for (ListUpdateAction<? extends RELATED_ID> action : listUpdate.getActions())
                {
                    Collection<RELATED_PE> relatedCollection = new LinkedList<RELATED_PE>();

                    if (action instanceof ListUpdateActionSet<?> || action instanceof ListUpdateActionAdd<?>)
                    {
                        for (RELATED_ID relatedId : action.getItems())
                        {
                            RELATED_PE related = relatedMap.get(relatedId);
                            if (related == null)
                            {
                                throw new ObjectNotFoundException((IObjectId) relatedId);
                            }
                            check(context, entity, relatedId, related);
                            relatedCollection.add(related);
                        }
                        if (action instanceof ListUpdateActionSet<?>)
                        {
                            set(context, entity, relatedCollection);
                        } else
                        {
                            add(context, entity, relatedCollection);
                        }
                    } else if (action instanceof ListUpdateActionRemove<?>)
                    {
                        for (RELATED_ID relatedId : action.getItems())
                        {
                            RELATED_PE related = relatedMap.get(relatedId);
                            if (related != null)
                            {
                                relatedCollection.add(related);
                            }
                            check(context, entity, relatedId, related);
                        }
                        remove(context, entity, relatedCollection);
                    }
                }
            }

        }
    }

    protected void set(IOperationContext context, ENTITY_PE entity, Collection<RELATED_PE> related)
    {
        Set<RELATED_PE> existingRelated = new HashSet<RELATED_PE>(getCurrentlyRelated(entity));
        Set<RELATED_PE> newRelated = new HashSet<RELATED_PE>(related);

        for (RELATED_PE anExistingRelated : existingRelated)
        {
            if (false == newRelated.contains(anExistingRelated))
            {
                remove(context, entity, anExistingRelated);
            }
        }

        for (RELATED_PE aNewRelated : newRelated)
        {
            if (false == existingRelated.contains(aNewRelated))
            {
                add(context, entity, aNewRelated);
            }
        }
    }

    protected void add(IOperationContext context, ENTITY_PE entity, Collection<RELATED_PE> related)
    {
        Set<RELATED_PE> existingRelated = new HashSet<RELATED_PE>(getCurrentlyRelated(entity));

        for (RELATED_PE aRelated : related)
        {
            if (false == existingRelated.contains(aRelated))
            {
                add(context, entity, aRelated);
            }
        }
    }

    protected void remove(IOperationContext context, ENTITY_PE entity, Collection<RELATED_PE> related)
    {
        Set<RELATED_PE> existingRelated = new HashSet<RELATED_PE>(getCurrentlyRelated(entity));

        for (RELATED_PE aRelated : related)
        {
            if (existingRelated.contains(aRelated))
            {
                remove(context, entity, aRelated);
            }
        }
    }

    protected abstract Collection<RELATED_PE> getCurrentlyRelated(ENTITY_PE entity);

    protected abstract IdListUpdateValue<? extends RELATED_ID> getRelatedUpdate(IOperationContext context, ENTITY_UPDATE update);

    protected abstract void check(IOperationContext context, ENTITY_PE entity, RELATED_ID relatedId, RELATED_PE related);

    protected abstract void add(IOperationContext context, ENTITY_PE entity, RELATED_PE related);

    protected abstract void remove(IOperationContext context, ENTITY_PE entity, RELATED_PE related);

}
