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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.plugin.IMapPluginByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IMapPropertyTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class CreateSampleTypesExecutor extends AbstractCreateEntityExecutor<SampleTypeCreation, SampleTypePE, EntityTypePermId>
        implements ICreateSampleTypeExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    private ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private ISampleTypeAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapPropertyTypeByIdExecutor mapPropertyTypeByIdExecutor;

    @Autowired
    private IMapPluginByIdExecutor mapPluginByIdExecutor;

    @Override
    protected List<SampleTypePE> createEntities(final IOperationContext context, CollectionBatch<SampleTypeCreation> typeCreations)
    {
        final List<SampleTypePE> typePEs = new LinkedList<SampleTypePE>();

        new CollectionBatchProcessor<SampleTypeCreation>(context, typeCreations)
            {
                @Override
                public void process(SampleTypeCreation typeCreation)
                {
                    SampleTypePE typePE = createSampleType(context, typeCreation);
                    typePEs.add(typePE);

                    if (typeCreation.getPropertyAssignments() != null)
                    {
                        List<PropertyAssignmentCreation> assignmentCreations = new ArrayList<PropertyAssignmentCreation>();
                        assignmentCreations.addAll(typeCreation.getPropertyAssignments());

                        Collections.sort(assignmentCreations, new Comparator<PropertyAssignmentCreation>()
                            {
                                @Override
                                public int compare(PropertyAssignmentCreation o1, PropertyAssignmentCreation o2)
                                {
                                    int ordinal1 = o1.getOrdinal() != null ? o1.getOrdinal() : Integer.MAX_VALUE;
                                    int ordinal2 = o2.getOrdinal() != null ? o2.getOrdinal() : Integer.MAX_VALUE;
                                    return Integer.compare(ordinal1, ordinal2);
                                }
                            });

                        for (PropertyAssignmentCreation assignmentCreation : assignmentCreations)
                        {
                            createPropertyAssignment(context, typeCreation, assignmentCreation);
                        }
                    }
                }

                @Override
                public IProgress createProgress(SampleTypeCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };

        return typePEs;
    }

    private SampleTypePE createSampleType(IOperationContext context, SampleTypeCreation typeCreation)
    {
        SampleType type = new SampleType();
        type.setCode(typeCreation.getCode());
        type.setAutoGeneratedCode(typeCreation.isAutoGeneratedCode());
        type.setGeneratedCodePrefix(typeCreation.getGeneratedCodePrefix());
        type.setSubcodeUnique(typeCreation.isSubcodeUnique());
        type.setDescription(typeCreation.getDescription());
        type.setListable(typeCreation.isListable());
        type.setShowContainer(typeCreation.isShowContainer());
        type.setShowParents(typeCreation.isShowParents());
        type.setShowParentMetadata(typeCreation.isShowParentMetadata());

        if (typeCreation.getValidationPluginId() != null)
        {
            ScriptPE pluginPE = findPlugin(context, typeCreation.getValidationPluginId());

            if (false == ScriptType.ENTITY_VALIDATION.equals(pluginPE.getScriptType()))
            {
                throw new UserFailureException("Sample type validation plugin has to be of type '" + ScriptType.ENTITY_VALIDATION
                        + "'. The specified plugin with id '" + typeCreation.getValidationPluginId() + "' is of type '" + pluginPE.getScriptType()
                        + "'.");
            }

            if (pluginPE.getEntityKind() != null
                    && false == pluginPE.getEntityKind().equals(ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.SAMPLE))
            {
                throw new UserFailureException("Sample type validation plugin has entity kind set to '" + pluginPE.getEntityKind()
                        + "'. Expected a plugin where entity kind is either '" + ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.SAMPLE
                        + "' or null.");
            }

            Script plugin = new Script();
            plugin.setName(pluginPE.getName());
            type.setValidationScript(plugin);
        }

        IEntityTypeBO typeBO = businessObjectFactory.createEntityTypeBO(context.getSession());
        typeBO.define(type);
        typeBO.save();

        return (SampleTypePE) daoFactory.getEntityTypeDAO(EntityKind.SAMPLE).tryToFindEntityTypeByCode(typeCreation.getCode());
    }

    private void createPropertyAssignment(IOperationContext context, SampleTypeCreation typeCreation, PropertyAssignmentCreation assignmentCreation)
    {
        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setEntityKind(ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.SAMPLE);

        PropertyTypePE propertyTypePE = findPropertyType(context, assignmentCreation.getPropertyTypeId());
        assignment.setPropertyTypeCode(propertyTypePE.getCode());

        assignment.setEntityTypeCode(typeCreation.getCode());
        assignment.setMandatory(assignmentCreation.isMandatory());
        assignment.setDefaultValue(assignmentCreation.getInitialValueForExistingEntities());
        assignment.setSection(assignmentCreation.getSection());

        if (assignmentCreation.getOrdinal() != null)
        {
            assignment.setOrdinal((long) assignmentCreation.getOrdinal() - 1);
        }

        if (assignmentCreation.getPluginId() != null)
        {
            ScriptPE pluginPE = findPlugin(context, assignmentCreation.getPluginId());

            if (false == ScriptType.DYNAMIC_PROPERTY.equals(pluginPE.getScriptType())
                    && false == ScriptType.MANAGED_PROPERTY.equals(pluginPE.getScriptType()))
            {
                throw new UserFailureException(
                        "Property assignment plugin has to be of type '" + ScriptType.DYNAMIC_PROPERTY + "' or '" + ScriptType.MANAGED_PROPERTY
                                + "'. The specified plugin with id '" + assignmentCreation.getPluginId() + "' is of type '" + pluginPE.getScriptType()
                                + "'.");
            }

            if (pluginPE.getEntityKind() != null
                    && false == pluginPE.getEntityKind().equals(ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.SAMPLE))
            {
                throw new UserFailureException("Property assignment plugin has entity kind set to '" + pluginPE.getEntityKind()
                        + "'. Expected a plugin where entity kind is either '" + ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.SAMPLE
                        + "' or null.");
            }

            assignment.setScriptName(pluginPE.getName());
            assignment.setDynamic(ScriptType.DYNAMIC_PROPERTY.equals(pluginPE.getScriptType()));
            assignment.setManaged(ScriptType.MANAGED_PROPERTY.equals(pluginPE.getScriptType()));
        }

        assignment.setShownInEditView(assignmentCreation.isShowInEditView());
        assignment.setShowRawValue(assignmentCreation.isShowRawValueInForms());

        IEntityTypePropertyTypeBO propertyBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(context.getSession(), EntityKind.SAMPLE);
        propertyBO.createAssignment(assignment);
    }

    private ScriptPE findPlugin(IOperationContext context, IPluginId pluginId)
    {
        Map<IPluginId, ScriptPE> pluginPEMap =
                mapPluginByIdExecutor.map(context, Arrays.asList(pluginId));
        ScriptPE pluginPE = pluginPEMap.get(pluginId);

        if (pluginPE == null)
        {
            throw new ObjectNotFoundException(pluginId);
        }

        return pluginPE;
    }

    private PropertyTypePE findPropertyType(IOperationContext context, IPropertyTypeId propertyTypeId)
    {
        Map<IPropertyTypeId, PropertyTypePE> propertyTypePEMap =
                mapPropertyTypeByIdExecutor.map(context, Arrays.asList(propertyTypeId));
        PropertyTypePE propertyTypePE = propertyTypePEMap.get(propertyTypeId);

        if (propertyTypePE == null)
        {
            throw new ObjectNotFoundException(propertyTypeId);
        }

        return propertyTypePE;
    }

    @Override
    protected EntityTypePermId createPermId(IOperationContext context, SampleTypePE entity)
    {
        return new EntityTypePermId(entity.getCode());
    }

    @Override
    protected void checkData(IOperationContext context, SampleTypeCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }
        if (creation.getGeneratedCodePrefix() == null)
        {
            throw new UserFailureException("GeneratedCodePrefix cannot be null.");
        }

        if (creation.getPropertyAssignments() != null)
        {
            for (PropertyAssignmentCreation assignmentCreation : creation.getPropertyAssignments())
            {
                if (assignmentCreation.getPropertyTypeId() == null)
                {
                    throw new UserFailureException("PropertyTypeId cannot be null.");
                }
                if (assignmentCreation.getOrdinal() != null && assignmentCreation.getOrdinal() <= 0)
                {
                    throw new UserFailureException("Ordinal cannot be <= 0.");
                }
            }
        }
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canCreate(context);
    }

    @Override
    protected void checkAccess(IOperationContext context, SampleTypePE entity)
    {
        authorizationExecutor.canCreate(context, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<SampleTypeCreation, SampleTypePE> batch)
    {
        // nothing to do
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<SampleTypeCreation, SampleTypePE> batch)
    {
        // nothing to do
    }

    @Override
    protected List<SampleTypePE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getEntityTypeDAO(EntityKind.SAMPLE).listEntityTypes();
    }

    @Override
    protected void save(IOperationContext context, List<SampleTypePE> entities, boolean clearCache)
    {
        // nothing to do
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "sampleType", null);
    }

}
