/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update.PropertyTypeUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdateRelationProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdatePropertyTypeExecutor
        extends AbstractUpdateEntityExecutor<PropertyTypeUpdate, PropertyTypePE, IPropertyTypeId, PropertyTypePermId>
        implements IUpdatePropertyTypeExecutor
{
    @Autowired
    private IDAOFactory daoFactory;
    
    @Autowired
    private IMapPropertyTypeByIdExecutor mapPropertyTypeByIdExecutor;
    
    @Autowired
    private IPropertyTypeAuthorizationExecutor authorizationExecutor;

    @Override
    protected IPropertyTypeId getId(PropertyTypeUpdate update)
    {
        return update.getTypeId();
    }

    @Override
    protected PropertyTypePermId getPermId(PropertyTypePE entity)
    {
        return new PropertyTypePermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, PropertyTypeUpdate update)
    {
        if (update.getTypeId() == null)
        {
            throw new UserFailureException("Property type id cannot be null.");
        }
        if (update.getLabel().isModified() && StringUtils.isEmpty(update.getLabel().getValue()))
        {
            throw new UserFailureException("Label cannot be empty.");
        }
        if (update.getDescription().isModified() && StringUtils.isEmpty(update.getDescription().getValue()))
        {
            throw new UserFailureException("Description cannot be empty.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IPropertyTypeId id, PropertyTypePE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<PropertyTypeUpdate, PropertyTypePE> batch)
    {
        new MapBatchProcessor<PropertyTypeUpdate, PropertyTypePE>(context, batch)
            {
                @Override
                public void process(PropertyTypeUpdate update, PropertyTypePE propertyType)
                {
                    propertyType.setDescription(getNewValue(update.getDescription(), propertyType.getDescription()));
                    propertyType.setLabel(getNewValue(update.getLabel(), propertyType.getLabel()));
                    String dataType = propertyType.getType().getCode().name();
                    CreatePropertyTypeExecutor.validateSchemaAndDataType(dataType, update.getSchema().getValue());
                    propertyType.setSchema(getNewValue(update.getSchema(), propertyType.getSchema()));
                    CreatePropertyTypeExecutor.validateTransformationAndDataType(dataType, update.getTransformation().getValue());
                    propertyType.setTransformation(getNewValue(update.getTransformation(), propertyType.getTransformation()));
                }

                @Override
                public IProgress createProgress(PropertyTypeUpdate key, PropertyTypePE value, int objectIndex, int totalObjectCount)
                {
                    return new UpdateRelationProgress(key, value, "property type", objectIndex, totalObjectCount);
                }
            };
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<PropertyTypeUpdate, PropertyTypePE> batch)
    {
    }

    @Override
    protected Map<IPropertyTypeId, PropertyTypePE> map(IOperationContext context, Collection<IPropertyTypeId> ids)
    {
        return mapPropertyTypeByIdExecutor.map(context, ids);
    }

    @Override
    protected List<PropertyTypePE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getPropertyTypeDAO().listAllEntities();
    }

    @Override
    protected void save(IOperationContext context, List<PropertyTypePE> entities, boolean clearCache)
    {
        for (PropertyTypePE propertyType : entities)
        {
            daoFactory.getPropertyTypeDAO().validateAndSaveUpdatedEntity(propertyType);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "property types", null);
    }

}
