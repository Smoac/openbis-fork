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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
public abstract class AbstractCreateEntityExecutor<CREATION, PE, PERM_ID> implements
        ICreateEntityExecutor<CREATION, PERM_ID>
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public List<PERM_ID> create(IOperationContext context, List<CREATION> creations)
    {
        try
        {
            List<PERM_ID> permIdsAll = new LinkedList<PERM_ID>();
            Map<CREATION, PE> entitiesAll = new LinkedHashMap<CREATION, PE>();

            int batchSize = 1000;
            for (int batchStart = 0; batchStart < creations.size(); batchStart += batchSize)
            {
                List<CREATION> creationsBatch = creations.subList(batchStart, Math.min(batchStart + batchSize, creations.size()));
                createEntities(context, creationsBatch, permIdsAll, entitiesAll);
            }

            reloadEntities(context, entitiesAll);

            updateAll(context, entitiesAll);

            reloadEntities(context, entitiesAll);

            checkBusinessRules(context, entitiesAll.values());

            return permIdsAll;
        } catch (DataAccessException e)
        {
            DataAccessExceptionTranslator.throwException(e, getKind().getLabel(), getKind());
            return null;
        }
    }

    private void createEntities(IOperationContext context, List<CREATION> creationsBatch,
            List<PERM_ID> permIdsAll, Map<CREATION, PE> entitiesAll)
    {
        Map<CREATION, PE> batchMap = new LinkedHashMap<CREATION, PE>();

        daoFactory.setBatchUpdateMode(true);

        for (CREATION creation : creationsBatch)
        {
            checkData(context, creation);

            PE entity = create(context, creation);
            PERM_ID permId = createPermId(context, entity);

            permIdsAll.add(permId);
            entitiesAll.put(creation, entity);
            batchMap.put(creation, entity);
        }

        updateBatch(context, batchMap);

        for (PE entity : batchMap.values())
        {
            checkAccess(context, entity);
        }

        save(context, new ArrayList<PE>(batchMap.values()), false);

        daoFactory.setBatchUpdateMode(false);
    }

    private void reloadEntities(IOperationContext context, Map<CREATION, PE> creationToEntityMap)
    {
        daoFactory.getEventDAO().flush();
        daoFactory.getEventDAO().clear();

        Collection<Long> ids = new HashSet<Long>();

        for (PE entity : creationToEntityMap.values())
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

        for (Map.Entry<CREATION, PE> entry : creationToEntityMap.entrySet())
        {
            IIdAndCodeHolder idAndCodeHolder = (IIdAndCodeHolder) entry.getValue();
            entry.setValue(idToEntityMap.get(idAndCodeHolder.getId()));
        }
    }

    protected abstract EntityKind getKind();

    protected abstract PE create(IOperationContext context, CREATION creation);

    protected abstract PERM_ID createPermId(IOperationContext context, PE entity);

    protected abstract void checkData(IOperationContext context, CREATION creation);

    protected abstract void checkAccess(IOperationContext context, PE entity);

    protected abstract void checkBusinessRules(IOperationContext context, Collection<PE> entities);

    protected abstract void updateBatch(IOperationContext context, Map<CREATION, PE> entitiesMap);

    protected abstract void updateAll(IOperationContext context, Map<CREATION, PE> entitiesMap);

    protected abstract List<PE> list(IOperationContext context, Collection<Long> ids);

    protected abstract void save(IOperationContext context, List<PE> entities, boolean clearCache);

}
