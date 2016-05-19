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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdatePropertyProgress;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.EntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author pkupczyk
 */
@Component
public class UpdateEntityPropertyExecutor implements IUpdateEntityPropertyExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    @SuppressWarnings("unused")
    private UpdateEntityPropertyExecutor()
    {
    }

    public UpdateEntityPropertyExecutor(IDAOFactory daoFactory, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this.daoFactory = daoFactory;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    @Override
    public void update(final IOperationContext context,
            final MapBatch<? extends IPropertiesHolder, ? extends IEntityInformationWithPropertiesHolder> holderToEntityMap)
    {
        final MapBatch<IEntityInformationWithPropertiesHolder, Map<String, String>> entityToPropertiesMap =
                getEntityToPropertiesMap(holderToEntityMap);

        if (entityToPropertiesMap == null || entityToPropertiesMap.isEmpty())
        {
            return;
        }

        final Map<EntityKind, EntityPropertiesConverter> converters = new HashMap<EntityKind, EntityPropertiesConverter>();

        new MapBatchProcessor<IEntityInformationWithPropertiesHolder, Map<String, String>>(context, entityToPropertiesMap)
            {
                @Override
                public void process(IEntityInformationWithPropertiesHolder propertiesHolder, Map<String, String> properties)
                {
                    EntityKind entityKind = propertiesHolder.getEntityType().getEntityKind();

                    if (converters.get(entityKind) == null)
                    {
                        EntityPropertiesConverter converter =
                                new EntityPropertiesConverter(entityKind, daoFactory, managedPropertyEvaluatorFactory);
                        converters.put(entityKind, converter);
                    }

                    update(context, propertiesHolder, properties, converters.get(entityKind));
                }

                @Override
                public IProgress createProgress(IEntityInformationWithPropertiesHolder propertiesHolder, Map<String, String> properties,
                        int objectIndex, int totalObjectCount)
                {
                    return new UpdatePropertyProgress(propertiesHolder, properties, objectIndex, totalObjectCount);
                }
            };
    }

    private MapBatch<IEntityInformationWithPropertiesHolder, Map<String, String>> getEntityToPropertiesMap(
            final MapBatch<? extends IPropertiesHolder, ? extends IEntityInformationWithPropertiesHolder> holderToEntityMap)
    {
        if (holderToEntityMap == null || holderToEntityMap.isEmpty())
        {
            return null;
        }

        Map<IEntityInformationWithPropertiesHolder, Map<String, String>> entityToPropertiesMap =
                new HashMap<IEntityInformationWithPropertiesHolder, Map<String, String>>();

        for (Map.Entry<? extends IPropertiesHolder, ? extends IEntityInformationWithPropertiesHolder> entry : holderToEntityMap.getObjects()
                .entrySet())
        {
            IPropertiesHolder holder = entry.getKey();
            IEntityInformationWithPropertiesHolder entity = entry.getValue();

            if (holder.getProperties() != null && false == holder.getProperties().isEmpty())
            {
                entityToPropertiesMap.put(entity, holder.getProperties());
            }
        }

        if (entityToPropertiesMap.isEmpty())
        {
            return null;
        }

        return new MapBatch<IEntityInformationWithPropertiesHolder, Map<String, String>>(holderToEntityMap.getBatchIndex(),
                holderToEntityMap.getFromObjectIndex(), holderToEntityMap.getToObjectIndex(), entityToPropertiesMap,
                holderToEntityMap.getTotalObjectCount());
    }

    private void update(IOperationContext context, IEntityPropertiesHolder propertiesHolder, Map<String, String> properties,
            EntityPropertiesConverter converter)
    {
        List<IEntityProperty> entityProperties = new LinkedList<IEntityProperty>();
        for (Map.Entry<String, String> entry : properties.entrySet())
        {
            entityProperties.add(EntityHelper.createNewProperty(entry.getKey(), entry.getValue()));
        }

        Set<? extends EntityPropertyPE> existingProperties = propertiesHolder.getProperties();
        Map<String, Object> existingPropertyValuesByCode = new HashMap<String, Object>();

        for (EntityPropertyPE existingProperty : existingProperties)
        {
            String propertyCode =
                    existingProperty.getEntityTypePropertyType().getPropertyType().getCode();
            existingPropertyValuesByCode.put(propertyCode, getValue(existingProperty));
        }

        Set<? extends EntityPropertyPE> convertedProperties =
                convertProperties(context, propertiesHolder.getEntityType(), existingProperties, entityProperties, converter);

        if (isEquals(existingPropertyValuesByCode, convertedProperties) == false)
        {
            propertiesHolder.setProperties(convertedProperties);
        }
    }

    private <T extends EntityPropertyPE> Set<T> convertProperties(IOperationContext context, final EntityTypePE type,
            final Set<T> existingProperties, List<IEntityProperty> properties, EntityPropertiesConverter converter)
    {
        Set<String> propertiesToUpdate = new HashSet<String>();
        if (properties != null)
        {
            for (IEntityProperty property : properties)
            {
                propertiesToUpdate.add(property.getPropertyType().getCode());
            }
        }
        return converter.updateProperties(existingProperties, type, properties,
                context.getSession().tryGetPerson(), propertiesToUpdate);
    }

    private static Object getValue(EntityPropertyPE property)
    {
        String value = property.getValue();
        if (value != null)
        {
            return value;
        }
        MaterialPE materialValue = property.getMaterialValue();
        if (materialValue != null)
        {
            return materialValue;
        }
        return property.getVocabularyTerm();
    }

    private static boolean isEquals(Map<String, Object> existingPropertyValuesByCode,
            Set<? extends EntityPropertyPE> properties)
    {
        for (EntityPropertyPE property : properties)
        {
            Object existingValue =
                    existingPropertyValuesByCode.remove(property.getEntityTypePropertyType()
                            .getPropertyType().getCode());
            if (existingValue == null || existingValue.equals(getValue(property)) == false)
            {
                return false;
            }
        }
        return existingPropertyValuesByCode.isEmpty();
    }

}
