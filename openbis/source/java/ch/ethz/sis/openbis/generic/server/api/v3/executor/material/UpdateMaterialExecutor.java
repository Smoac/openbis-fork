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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.material;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IUpdateTagForEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.MaterialUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Jakub Straszewski
 */
@Component
public class UpdateMaterialExecutor extends AbstractUpdateEntityExecutor<MaterialUpdate, MaterialPE, IMaterialId> implements IUpdateMaterialExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapMaterialByIdExecutor mapMaterialByIdExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private IUpdateTagForEntityExecutor updateTagForEntityExecutor;

    @Autowired
    private IVerifyMaterialExecutor verifyMaterialExecutor;

    @Override
    protected IMaterialId getId(MaterialUpdate update)
    {
        return update.getMaterialId();
    }

    @Override
    protected void checkData(IOperationContext context, MaterialUpdate update)
    {
        if (update.getMaterialId() == null)
        {
            throw new UserFailureException("Sample id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IMaterialId id, MaterialPE entity)
    {
        if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, Collection<MaterialPE> entities)
    {
        verifyMaterialExecutor.verify(context, entities);
    }

    @Override
    protected void updateBatch(IOperationContext context, Map<MaterialUpdate, MaterialPE> entitiesMap)
    {
        Map<IEntityPropertiesHolder, Map<String, String>> propertyMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<MaterialUpdate, MaterialPE> entry : entitiesMap.entrySet())
        {
            MaterialUpdate update = entry.getKey();
            MaterialPE entity = entry.getValue();
            updateTagForEntityExecutor.update(context, entity, update.getTagIds());
            propertyMap.put(entity, update.getProperties());
        }

        updateEntityPropertyExecutor.update(context, propertyMap);
    }

    @Override
    protected void updateAll(IOperationContext context, Map<MaterialUpdate, MaterialPE> entitiesMap)
    {
    }

    @Override
    protected Map<IMaterialId, MaterialPE> map(IOperationContext context, Collection<IMaterialId> ids)
    {
        return mapMaterialByIdExecutor.map(context, ids);
    }

    @Override
    protected List<MaterialPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getMaterialDAO().listMaterialsById(ids);
    }

    @Override
    protected void save(IOperationContext context, List<MaterialPE> entities, boolean clearCache)
    {
        daoFactory.getMaterialDAO().createOrUpdateMaterials(entities);
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.SAMPLE.getLabel(), EntityKind.SAMPLE);
    }

}
