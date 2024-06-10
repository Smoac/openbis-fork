/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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

import java.util.*;

import javax.annotation.Resource;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSamplesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.ISearchDataSetsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.ISearchExperimentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.ISearchMaterialsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ISearchSamplesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.*;
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
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IMapPropertyAssignmentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdateRelationProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.InternalPropertyTypeAuthorization;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
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

    @Autowired
    private ISearchSamplesOperationExecutor searchSampleExecutor;

    @Autowired
    private ISearchExperimentsOperationExecutor searchExperimentExecutor;

    @Autowired
    private ISearchDataSetsOperationExecutor searchDataSetExecutor;

    @Autowired
    private ISearchMaterialsOperationExecutor searchMaterialExecutor;

    protected abstract EntityKind getEntityKind();

    @Override
    public void update(IOperationContext context, MapBatch<UPDATE, TYPE_PE> batch)
    {
        new MapBatchProcessor<UPDATE, TYPE_PE>(context, batch)
            {

                @Override
                public void process(UPDATE update, TYPE_PE typePE)
                {
                    PropertyAssignmentListUpdateValue propertyAssignments = update.getPropertyAssignments();
                    update(context, typePE, propertyAssignments);
                }

                @Override
                public IProgress createProgress(UPDATE key, TYPE_PE value, int objectIndex, int totalObjectCount)
                {
                    return new UpdateRelationProgress(key, value, "entity-type-property-type", objectIndex, totalObjectCount);
                }
            };
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
        if(typePE.isManagedInternally() && !isSystemUser(context.getSession()))
        {
            //TODO property assignment validation should be introduced during intern assignment implementation
//            validateInternalEntityTypeChanges(typePE, updates);
        }

        if (updates != null && updates.hasActions())
        {
            remove(context, typePE, updates);
            add(context, typePE, updates);
            set(context, typePE, updates);
        }
    }

    /**
     * Business logic to validate changes made to internal entity type assignments.
     * @param typePE internal entity type
     * @param updates to be performed
     */
    private void validateInternalEntityTypeChanges(TYPE_PE typePE, PropertyAssignmentListUpdateValue updates)
    {
        for (ListUpdateAction<Object> updateAction : updates.getActions())
        {
            // Remove assignment action
            if (updateAction instanceof ListUpdateActionRemove<?>)
            {
                Map<IPropertyAssignmentId, EntityTypePropertyTypePE> assignments = new HashMap<>();
                for(EntityTypePropertyTypePE etptPE : typePE.getEntityTypePropertyTypes())
                {
                    EntityTypePermId entityTypePermId = new EntityTypePermId(typePE.getPermId(), EntityKindConverter.convert(typePE.getEntityKind()));
                    PropertyTypePermId propertyTypePermId = new PropertyTypePermId(etptPE.getPropertyType().getPermId());
                    PropertyAssignmentPermId pa = new PropertyAssignmentPermId(entityTypePermId, propertyTypePermId);
                    assignments.put(pa, etptPE);
                }
                Collection<IPropertyAssignmentId> items = (Collection<IPropertyAssignmentId>) updateAction.getItems();
                for(IPropertyAssignmentId item : items) {
                    EntityTypePropertyTypePE etptPE = assignments.getOrDefault(item, null);
                    if(etptPE != null && etptPE.getRegistrator().isSystemUser() && etptPE.getPropertyType().isManagedInternally())
                    {
                        throw new UserFailureException("Only system user can delete initial internal property assignments from internal entity types");
                    }
                }
            } else if(updateAction instanceof ListUpdateActionSet<?>)
            {
                // set and add actions
                Map<IPropertyTypeId, EntityTypePropertyTypePE> propertyTypes = new HashMap<>();
                for(EntityTypePropertyTypePE etptPE : typePE.getEntityTypePropertyTypes())
                {
                    PropertyTypePermId propertyTypePermId = new PropertyTypePermId(etptPE.getPropertyType().getPermId());
                    propertyTypes.put(propertyTypePermId, etptPE);
                }
                Collection<PropertyAssignmentCreation> items = (Collection<PropertyAssignmentCreation>) updateAction.getItems();
                for(PropertyAssignmentCreation creation : items)
                {
                    IPropertyTypeId propertyTypeId = creation.getPropertyTypeId();
                    EntityTypePropertyTypePE etpt = propertyTypes.getOrDefault(propertyTypeId, null);
                    if(etpt != null)
                    {
                        // modify existing
                        if(!etpt.isMandatory() && creation.isMandatory()) {
                            throw new UserFailureException("Only system user can make properties mandatory for internal entity types! Property:" + creation.getPropertyTypeId());
                        }
                        if(!etpt.isScriptable() && creation.getPluginId() != null)
                        {
                            throw new UserFailureException("Only system user can make properties scriptable for internal entity types! Property:" + creation.getPropertyTypeId());
                        }
                    } else {
                        // add new property assignment
                        if(creation.isMandatory()) {
                            throw new UserFailureException("Only system user can make properties mandatory for internal entity types! Property:" + creation.getPropertyTypeId());
                        }
                        if(creation.getPluginId() != null) {
                            throw new UserFailureException("Only system user can make properties scriptable for internal entity types! Property:" + creation.getPropertyTypeId());
                        }
                    }
                }
            } else if(updateAction instanceof ListUpdateActionAdd<?>) {
                // add new property assignment action
                Collection<PropertyAssignmentCreation> items = (Collection<PropertyAssignmentCreation>) updateAction.getItems();
                for(PropertyAssignmentCreation creation : items)
                {
                    if(creation.isMandatory())
                    {
                        throw new UserFailureException("Only system user can make properties mandatory for internal entity types! Property:" + creation.getPropertyTypeId());
                    }
                    if(creation.getPluginId() != null)
                    {
                        throw new UserFailureException("Only system user can make properties scriptable for internal entity types! Property:" + creation.getPropertyTypeId());
                    }
                }
            }
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
            boolean forceRemovingAssignments = updates.isForceRemovingAssignments();
            removeAssignments(context, map.values(), forceRemovingAssignments);
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
            boolean forceRemovingAssignments = updates.isForceRemovingAssignments();
            findReplacementsNewCreationsAndDeleteAssignments(context, typePE, creations, replacements, newCreations,
                    forceRemovingAssignments);
            if (newCreations.isEmpty() == false)
            {
                createPropertyAssignmentsExecutor.createPropertyAssignments(context, typePE.getCode(), newCreations, getEntityKind());
            }
            if (replacements.isEmpty() == false)
            {
                for (PropertyAssignmentCreation replacement : replacements)
                {
                    NewETPTAssignment translatedAssignment = createPropertyAssignmentsExecutor.translateAssignment(context, typePE.getCode(),
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

    private void findReplacementsNewCreationsAndDeleteAssignments(IOperationContext context, TYPE_PE typePE,
            Collection<? extends PropertyAssignmentCreation> creations,
            List<PropertyAssignmentCreation> replacements, List<PropertyAssignmentCreation> newCreations,
            boolean forceRemovingAssignments)
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
            } else if (propertyTypeId == null)
            {
                throw new UserFailureException("PropertyTypeId cannot be null.");
            } else
            {
                throw new UserFailureException("Unknown type of property type id: " + propertyTypeId.getClass().getName());
            }
        }
        removeAssignments(context, currentAssignments.values(), forceRemovingAssignments);
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

    private void removeAssignments(IOperationContext context, Collection<EntityTypePropertyTypePE> etpts, boolean forceRemovingAssignments)
    {
        for (EntityTypePropertyTypePE entityTypePropertyType : etpts)
        {
            EntityTypePE entityTypePE = entityTypePropertyType.getEntityType();
            int totalCount = 0;
            if (entityTypePE instanceof SampleTypePE)
            {
                SampleSearchCriteria criteria = new SampleSearchCriteria();
                criteria.withType().withCode().thatEquals(entityTypePE.getCode());
                criteria.withProperty(entityTypePropertyType.getPropertyType().getCode());
                SampleFetchOptions fetchOptions = new SampleFetchOptions();
                fetchOptions.count(0);
                Map<IOperation, IOperationResult> results =
                        searchSampleExecutor.execute(context, List.of(new SearchSamplesOperation(criteria, fetchOptions)));
                totalCount = ((SearchSamplesOperationResult) results.values().iterator().next()).getSearchResult().getTotalCount();
            } else if (entityTypePE instanceof ExperimentTypePE)
            {
                ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
                criteria.withType().withCode().thatEquals(entityTypePE.getCode());
                criteria.withProperty(entityTypePropertyType.getPropertyType().getCode());
                ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
                fetchOptions.count(0);
                Map<IOperation, IOperationResult> results =
                        searchExperimentExecutor.execute(context, List.of(new SearchExperimentsOperation(criteria, fetchOptions)));
                totalCount = ((SearchExperimentsOperationResult) results.values().iterator().next()).getSearchResult().getTotalCount();
            } else if (entityTypePE instanceof DataSetTypePE)
            {
                DataSetSearchCriteria criteria = new DataSetSearchCriteria();
                criteria.withType().withCode().thatEquals(entityTypePE.getCode());
                criteria.withProperty(entityTypePropertyType.getPropertyType().getCode());
                DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
                fetchOptions.count(0);
                Map<IOperation, IOperationResult> results =
                        searchDataSetExecutor.execute(context, List.of(new SearchDataSetsOperation(criteria, fetchOptions)));
                totalCount = ((SearchDataSetsOperationResult) results.values().iterator().next()).getSearchResult().getTotalCount();
            } else if (entityTypePE instanceof MaterialTypePE)
            {
                MaterialSearchCriteria criteria = new MaterialSearchCriteria();
                criteria.withType().withCode().thatEquals(entityTypePE.getCode());
                criteria.withProperty(entityTypePropertyType.getPropertyType().getCode());
                MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
                fetchOptions.count(0);
                Map<IOperation, IOperationResult> results =
                        searchMaterialExecutor.execute(context, List.of(new SearchMaterialsOperation(criteria, fetchOptions)));
                totalCount = ((SearchMaterialsOperationResult) results.values().iterator().next()).getSearchResult().getTotalCount();
            } else
            {
                throw new IllegalStateException("This should never happen! entityTypePE=" + entityTypePE.getClass());
            }
            if (forceRemovingAssignments || totalCount == 0)
            {
                new InternalPropertyTypeAuthorization().canDeletePropertyAssignment(context.getSession(), entityTypePropertyType.getPropertyType(),
                        entityTypePropertyType);

                entityTypePropertyType.getEntityType().getEntityTypePropertyTypes().remove(entityTypePropertyType);
            } else
            {
                throw new UserFailureException("Can not remove property type "
                        + entityTypePropertyType.getPropertyType().getCode() + " from type "
                        + entityTypePropertyType.getEntityType().getCode() + " because "
                        + totalCount + " entites using this property. "
                        + "To force removal call getPropertyAssignments().setForceRemovingAssignments(true) "
                        + "on the entity update object.");
            }
        }
    }

    private boolean isSystemUser(Session session)
    {
        PersonPE user = session.tryGetPerson();

        if (user == null)
        {
            throw new AuthorizationFailureException("Could not check access because the current session does not have any user assigned.");
        } else
        {
            return user.isSystemUser();
        }
    }

}
