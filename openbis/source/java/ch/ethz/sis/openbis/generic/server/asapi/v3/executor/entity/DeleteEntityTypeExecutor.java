/*
 * Copyright 2017 ETH Zuerich, SIS
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.delete.EntityTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IDataSetTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IExperimentTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IMaterialTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ISampleTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class DeleteEntityTypeExecutor
        extends AbstractDeleteEntityExecutor<Void, IEntityTypeId, EntityTypePE, EntityTypeDeletionOptions>
        implements IDeleteEntityTypeExecutor
{
    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;
    
    @Autowired
    private IDataSetTypeAuthorizationExecutor dataSetTypeAuthorizationExecutor;
    
    @Autowired
    private IExperimentTypeAuthorizationExecutor experimentTypeAuthorizationExecutor;
    
    @Autowired
    private IMaterialTypeAuthorizationExecutor materialTypeAuthorizationExecutor;
    
    @Autowired
    private ISampleTypeAuthorizationExecutor sampleTypeAuthorizationExecutor;
    
    @Override
    protected Map<IEntityTypeId, EntityTypePE> map(IOperationContext context, List<? extends IEntityTypeId> entityTypeIds)
    {
        Map<IEntityTypeId, EntityTypePE> result = new HashMap<>();
        Map<EntityKind, List<IEntityTypeId>> typeIdsByKind = splitByEntityKind(entityTypeIds);
        for (Entry<EntityKind, List<IEntityTypeId>> entry : typeIdsByKind.entrySet())
        {
            result.putAll(mapEntityTypeByIdExecutor.map(context, entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private Map<EntityKind, List<IEntityTypeId>> splitByEntityKind(List<? extends IEntityTypeId> entityTypeIds)
    {
        Map<EntityKind, List<IEntityTypeId>> typeIdsByKind = new HashMap<>();
        for (IEntityTypeId entityTypeId : entityTypeIds)
        {
            if (entityTypeId instanceof EntityTypePermId)
            {
                EntityTypePermId permId = (EntityTypePermId) entityTypeId;
                EntityKind entityKind = EntityKindConverter.convert(permId.getEntityKind());
                if (entityKind == null)
                {
                    throw new UserFailureException("Entity type id with unspecified entity kind: " + entityTypeId);
                }
                List<IEntityTypeId> ids = typeIdsByKind.get(entityKind);
                if (ids == null)
                {
                    ids = new ArrayList<>();
                    typeIdsByKind.put(entityKind, ids);
                }
                ids.add(entityTypeId);
            } else
            {
                throw new UserFailureException("Unknown entity type id type: " + entityTypeId.getClass().getName());
            }
        }
        return typeIdsByKind;
    }

    @Override
    protected void checkAccess(IOperationContext context, IEntityTypeId entityId, EntityTypePE entity)
    {
        switch (entity.getEntityKind())
        {
            case DATA_SET:
                dataSetTypeAuthorizationExecutor.canDelete(context);
                break;
            case EXPERIMENT:
                experimentTypeAuthorizationExecutor.canDelete(context);
                break;
            case MATERIAL:
                materialTypeAuthorizationExecutor.canDelete(context);
                break;
            case SAMPLE:
                sampleTypeAuthorizationExecutor.canDelete(context);
                break;
        }
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, EntityTypePE entity)
    {
    }

    @Override
    protected Void delete(IOperationContext context, Collection<EntityTypePE> entities, EntityTypeDeletionOptions deletionOptions)
    {
        for (EntityTypePE entityType : entities)
        {
            daoFactory.getEntityTypeDAO(entityType.getEntityKind()).delete(entityType);
        }
        return null;
    }

}
