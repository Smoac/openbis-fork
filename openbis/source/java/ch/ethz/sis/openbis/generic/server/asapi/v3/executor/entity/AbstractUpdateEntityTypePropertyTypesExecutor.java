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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.IEntityTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.PropertyAssignmentListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IMapPropertyAssignmentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

/**
 * @author Franz-Josef Elmer
 */
@Component
public abstract class AbstractUpdateEntityTypePropertyTypesExecutor<UPDATE extends IEntityTypeUpdate, TYPE_PE extends EntityTypePE, ETPT_PE extends EntityTypePropertyTypePE>
        implements IUpdateEntityTypePropertyTypesExecutor<UPDATE, TYPE_PE>
{
    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    protected ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private CreatePropertyAssignmentsExecutor createPropertyAssignmentsExecutor;
    
    @Autowired
    private IMapPropertyAssignmentByIdExecutor mapPropertyAssignmentByIdExecutor;
    
    protected abstract EntityKind getEntityKind();

    @Override
    public void update(IOperationContext context, MapBatch<UPDATE, TYPE_PE> batch)
    {
        Set<Entry<UPDATE, TYPE_PE>> entrySet = batch.getObjects().entrySet();
        for (Entry<UPDATE, TYPE_PE> entry : entrySet)
        {
            UPDATE update = entry.getKey();
            TYPE_PE typePE = entry.getValue();
            PropertyAssignmentListUpdateValue propertyAssignments = update.getPropertyAssignments();
            update(context, typePE, propertyAssignments);
        }
    }
    
    private void update(IOperationContext context, TYPE_PE typePE, PropertyAssignmentListUpdateValue updates)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (typePE == null)
        {
            throw new IllegalArgumentException("Entity type cannot be null");
        }

        if (updates != null && updates.hasActions())
        {
            remove(context, typePE, updates);
            add(context, typePE, updates);
            set(context, typePE, updates);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void remove(IOperationContext context, TYPE_PE typePE, PropertyAssignmentListUpdateValue updates)
    {
        Set<IPropertyAssignmentId> removed = new HashSet<>();
        for (ListUpdateAction<Object> updateAction : updates.getActions())
        {
            if (updateAction instanceof ListUpdateActionRemove<?>)
            {
                removed.addAll((Collection<IPropertyAssignmentId>) updateAction.getItems());
            }
        }
        if (removed.isEmpty() == false)
        {
            Map<IPropertyAssignmentId, EntityTypePropertyTypePE> map = mapPropertyAssignmentByIdExecutor.map(context, removed);
            removeAssignments(map.values());
        }
    }

    @SuppressWarnings("unchecked")
    private void add(IOperationContext context, TYPE_PE typePE, PropertyAssignmentListUpdateValue updates)
    {
        Set<PropertyAssignmentCreation> added = new HashSet<>();
        for (ListUpdateAction<Object> updateAction : updates.getActions())
        {
            if (updateAction instanceof ListUpdateActionAdd<?>)
            {
                added.addAll((Collection<PropertyAssignmentCreation>) updateAction.getItems());
            }
        }
        if (added.isEmpty() == false)
        {
            createPropertyAssignmentsExecutor.createPropertyAssignments(context, typePE.getCode(), added, getEntityKind());
        }
    }

    @SuppressWarnings("unchecked")
    private void set(IOperationContext context, TYPE_PE typePE, PropertyAssignmentListUpdateValue updates)
    {
        ListUpdateActionSet<PropertyAssignmentCreation> lastSet = null;

        for (ListUpdateAction<?> action : updates.getActions())
        {
            if (action instanceof ListUpdateActionSet<?>)
            {
                lastSet = (ListUpdateActionSet<PropertyAssignmentCreation>) action;
            }
        }
        if (lastSet != null)
        {
            Collection<? extends PropertyAssignmentCreation> creations = lastSet.getItems();
            List<PropertyAssignmentCreation> replacements = new ArrayList<>();
            List<PropertyAssignmentCreation> newCreations = new ArrayList<>();
            findReplacementsNewCreationsAndDeleteAssignments(typePE, creations, replacements, newCreations);
            if (newCreations.isEmpty() == false)
            {
                createPropertyAssignmentsExecutor.createPropertyAssignments(context, typePE.getCode(), newCreations, getEntityKind());
            }
            if (replacements.isEmpty() == false)
            {
                for (PropertyAssignmentCreation replacement : replacements)
                {
                    NewETPTAssignment translatedAssignment 
                            = createPropertyAssignmentsExecutor.translateAssignment(context, typePE.getCode(), 
                                    getEntityKind(), replacement);
                    IEntityTypePropertyTypeBO etptBO =
                            businessObjectFactory.createEntityTypePropertyTypeBO(context.getSession(),
                                    DtoConverters.convertEntityKind(getEntityKind()));
                    etptBO.loadAssignment(translatedAssignment.getPropertyTypeCode(),
                            translatedAssignment.getEntityTypeCode());
                    etptBO.updateLoadedAssignment(translatedAssignment);
                }
            }
        }
    }

    private void findReplacementsNewCreationsAndDeleteAssignments(TYPE_PE typePE, Collection<? extends PropertyAssignmentCreation> creations,
            List<PropertyAssignmentCreation> replacements, List<PropertyAssignmentCreation> newCreations)
    {
        Map<String, EntityTypePropertyTypePE> currentAssignments = getCurrentAssignments(typePE);
        for (PropertyAssignmentCreation propertyAssignmentCreation : creations)
        {
            IPropertyTypeId propertyTypeId = propertyAssignmentCreation.getPropertyTypeId();
            if (propertyTypeId instanceof PropertyTypePermId)
            {
                String propertyTypeCode = ((PropertyTypePermId) propertyTypeId).getPermId();
                if (currentAssignments.remove(propertyTypeCode) != null)
                {
                    replacements.add(propertyAssignmentCreation);
                } else
                {
                    newCreations.add(propertyAssignmentCreation);
                }
            }
        }
        removeAssignments(currentAssignments.values());
    }

    private Map<String, EntityTypePropertyTypePE> getCurrentAssignments(TYPE_PE typePE)
    {
        Collection<? extends EntityTypePropertyTypePE> entityTypePropertyTypes = typePE.getEntityTypePropertyTypes();
        Map<String, EntityTypePropertyTypePE> etptByPropertyTypeCode = new HashMap<>();
        for (EntityTypePropertyTypePE entityTypePropertyTypePE : entityTypePropertyTypes)
        {
            String code = entityTypePropertyTypePE.getPropertyType().getCode();
            etptByPropertyTypeCode.put(code, entityTypePropertyTypePE);
        }
        return etptByPropertyTypeCode;
    }
    
    private void removeAssignments(Collection<EntityTypePropertyTypePE> etpts)
    {
        for (EntityTypePropertyTypePE entityTypePropertyType : etpts)
        {
            entityTypePropertyType.getEntityType().getEntityTypePropertyTypes().remove(entityTypePropertyType);
        }
    }


}
