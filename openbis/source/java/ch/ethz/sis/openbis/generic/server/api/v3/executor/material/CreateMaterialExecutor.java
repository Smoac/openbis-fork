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

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.MaterialCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Jakub Straszewski
 */
@Component
public class CreateMaterialExecutor extends AbstractCreateEntityExecutor<MaterialCreation, MaterialPE, MaterialPermId> implements
        ICreateMaterialExecutor
{

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISetMaterialTypeExecutor setMaterialTypeExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @Autowired
    private IVerifyMaterialExecutor verifyMaterialExecutor;

    @Override
    protected MaterialPE create(IOperationContext context, MaterialCreation creation)
    {
        MaterialPE material = new MaterialPE();
        material.setCode(creation.getCode());
        material.setRegistrator(context.getSession().tryGetPerson());
        return material;
    }

    @Override
    protected MaterialPermId createPermId(IOperationContext context, MaterialPE entity)
    {
        return new MaterialPermId(entity.getCode(), entity.getEntityType().getCode());
    }

    @Override
    protected void checkData(IOperationContext context, MaterialCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }

        SpaceIdentifierFactory.assertValidCode(creation.getCode());
    }

    @Override
    protected void checkAccess(IOperationContext context, MaterialPE entity)
    {
        // nothing to do
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, Collection<MaterialPE> entities)
    {
        verifyMaterialExecutor.verify(context, entities);
    }

    @Override
    protected void updateBatch(IOperationContext context, Map<MaterialCreation, MaterialPE> entitiesMap)
    {
        setMaterialTypeExecutor.set(context, entitiesMap);

        Map<IEntityPropertiesHolder, Map<String, String>> propertyMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<MaterialCreation, MaterialPE> entry : entitiesMap.entrySet())
        {
            propertyMap.put(entry.getValue(), entry.getKey().getProperties());
        }
        updateEntityPropertyExecutor.update(context, propertyMap);
    }

    @Override
    protected void updateAll(IOperationContext context, Map<MaterialCreation, MaterialPE> entitiesMap)
    {
        Map<IEntityWithMetaprojects, Collection<? extends ITagId>> tagMap = new HashMap<IEntityWithMetaprojects, Collection<? extends ITagId>>();

        for (Map.Entry<MaterialCreation, MaterialPE> entry : entitiesMap.entrySet())
        {
            MaterialCreation creation = entry.getKey();
            MaterialPE entity = entry.getValue();
            tagMap.put(entity, creation.getTagIds());
        }

        addTagToEntityExecutor.add(context, tagMap);
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
        DataAccessExceptionTranslator.throwException(e, EntityKind.MATERIAL.getLabel(), EntityKind.MATERIAL);
    }

}
