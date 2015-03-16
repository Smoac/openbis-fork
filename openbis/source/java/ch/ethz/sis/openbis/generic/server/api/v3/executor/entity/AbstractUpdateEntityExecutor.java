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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.IObjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IIdAndCodeHolder;

/**
 * @author pkupczyk
 */
public abstract class AbstractUpdateEntityExecutor<UPDATE, PE, ID> implements IUpdateEntityExecutor<UPDATE>
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public void update(IOperationContext context, List<UPDATE> updates)
    {
        try
        {
            Map<UPDATE, PE> entitiesAll = new LinkedHashMap<UPDATE, PE>();

            int batchSize = 1000;
            for (int batchStart = 0; batchStart < updates.size(); batchStart += batchSize)
            {
                List<UPDATE> updatesBatch = updates.subList(batchStart, Math.min(batchStart + batchSize, updates.size()));
                updateEntities(context, updatesBatch, entitiesAll);
            }

            reloadEntities(context, entitiesAll);

            updateAll(context, entitiesAll);

            reloadEntities(context, entitiesAll);

            checkBusinessRules(context, entitiesAll.values());

        } catch (DataAccessException e)
        {
            handleException(e);
        }
    }

    private Map<UPDATE, PE> getEntitiesMap(IOperationContext context, List<UPDATE> updates)
    {

        for (UPDATE update : updates)
        {
            checkData(context, update);
        }

        Collection<ID> entityIds = CollectionUtils.collect(updates, new Transformer<UPDATE, ID>()
            {
                @Override
                public ID transform(UPDATE update)
                {
                    return getId(update);
                }
            });

        Map<ID, PE> entityMap = map(context, entityIds);

        for (ID entityId : entityIds)
        {
            PE entity = entityMap.get(entityId);

            if (entity == null)
            {
                throw new ObjectNotFoundException((IObjectId) entityId);
            }

            checkAccess(context, entityId, entity);
        }

        Map<UPDATE, PE> result = new HashMap<UPDATE, PE>();

        for (UPDATE update : updates)
        {
            ID id = getId(update);
            result.put(update, entityMap.get(id));
        }

        return result;
    }

    private void updateEntities(IOperationContext context, List<UPDATE> updatesBatch, Map<UPDATE, PE> entitiesAll)
    {
        Map<UPDATE, PE> batchMap = getEntitiesMap(context, updatesBatch);
        entitiesAll.putAll(batchMap);

        daoFactory.setBatchUpdateMode(true);

        updateBatch(context, batchMap);

        save(context, new ArrayList<PE>(batchMap.values()), false);

        daoFactory.setBatchUpdateMode(false);
    }

    private void reloadEntities(IOperationContext context, Map<UPDATE, PE> updateToEntityMap)
    {
        Collection<Long> ids = new HashSet<Long>();

        for (PE entity : updateToEntityMap.values())
        {
            IIdAndCodeHolder idAndCodeHolder = (IIdAndCodeHolder) entity;
            ids.add(idAndCodeHolder.getId());
        }

        List<PE> entities = list(context, ids);

        Map<Long, PE> idToEntityMap = new HashMap<Long, PE>();

        for (PE entity : entities)
        {
            IIdAndCodeHolder idAndCodeHolder = (IIdAndCodeHolder) entity;
            idToEntityMap.put(idAndCodeHolder.getId(), entity);
        }

        for (Map.Entry<UPDATE, PE> entry : updateToEntityMap.entrySet())
        {
            IIdAndCodeHolder idAndCodeHolder = (IIdAndCodeHolder) entry.getValue();
            entry.setValue(idToEntityMap.get(idAndCodeHolder.getId()));
        }
    }

    protected abstract ID getId(UPDATE update);

    protected abstract void checkData(IOperationContext context, UPDATE update);

    protected abstract void checkAccess(IOperationContext context, ID id, PE entity);

    protected abstract void checkBusinessRules(IOperationContext context, Collection<PE> entities);

    protected abstract void updateBatch(IOperationContext context, Map<UPDATE, PE> entitiesMap);

    protected abstract void updateAll(IOperationContext context, Map<UPDATE, PE> entitiesMap);

    protected abstract Map<ID, PE> map(IOperationContext context, Collection<ID> ids);

    protected abstract List<PE> list(IOperationContext context, Collection<Long> ids);

    protected abstract void save(IOperationContext context, List<PE> entities, boolean clearCache);

    protected abstract void handleException(DataAccessException e);

}
