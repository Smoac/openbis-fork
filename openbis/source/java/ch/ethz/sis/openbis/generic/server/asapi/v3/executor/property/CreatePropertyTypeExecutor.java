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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary.IMapVocabularyByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.util.XmlUtils;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class CreatePropertyTypeExecutor
        extends AbstractCreateEntityExecutor<PropertyTypeCreation, PropertyTypePE, PropertyTypePermId>
        implements ICreatePropertyTypeExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapVocabularyByIdExecutor mapVocabularyByIdExecutor;

    @Autowired
    private IMapEntityTypeByIdExecutor mapMaterialTypeByIdExecutor;

    @Autowired
    private IPropertyTypeAuthorizationExecutor authorizationExecutor;

    @Override
    protected IObjectId getId(PropertyTypePE entity)
    {
        return new PropertyTypePermId(entity.getCode());
    }

    @Override
    protected PropertyTypePermId createPermId(IOperationContext context, PropertyTypePE entity)
    {
        return new PropertyTypePermId(entity.getCode());
    }

    @Override
    protected void checkData(IOperationContext context, PropertyTypeCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }
        if (StringUtils.isEmpty(creation.getLabel()))
        {
            throw new UserFailureException("Label cannot be empty.");
        }
        if (StringUtils.isEmpty(creation.getDescription()))
        {
            throw new UserFailureException("Description cannot be empty.");
        }
        DataType dataType = creation.getDataType();
        if (dataType == null)
        {
            throw new UserFailureException("Data type not specified.");
        }
        if (dataType == DataType.CONTROLLEDVOCABULARY && creation.getVocabularyId() == null)
        {
            throw new UserFailureException("Data type has been specified as " 
                    + DataType.CONTROLLEDVOCABULARY + " but vocabulary id is missing.");
        }
        if (creation.getVocabularyId() != null && dataType != DataType.CONTROLLEDVOCABULARY)
        {
            throw new UserFailureException("Vocabulary id has been specified but data type is " + dataType + ".");
        }
        IEntityTypeId materialTypeId = creation.getMaterialTypeId();
        if (materialTypeId != null)
        {
            if (dataType != DataType.MATERIAL)
            {
                throw new UserFailureException("Material type id has been specified but data type is " + dataType + ".");
            }
            if (materialTypeId instanceof EntityTypePermId)
            {
                EntityTypePermId permId = (EntityTypePermId) materialTypeId;
                if (permId.getEntityKind() != EntityKind.MATERIAL)
                {
                    throw new UserFailureException("Specified entity type id (" + materialTypeId + ") is not a " 
                            + EntityKind.MATERIAL + " type.");
                }
            }
        }
        XmlUtils.validateXML(creation.getSchema(), "XML Schema", XmlUtils.XML_SCHEMA_XSD_FILE_RESOURCE);
        XmlUtils.validateXML(creation.getTransformation(), "XSLT", XmlUtils.XSLT_XSD_FILE_RESOURCE);
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canCreate(context);
    }

    @Override
    protected void checkAccess(IOperationContext context, PropertyTypePE entity)
    {
    }

    @Override
    protected List<PropertyTypePE> createEntities(IOperationContext context, CollectionBatch<PropertyTypeCreation> batch)
    {
        List<DataTypePE> dataTypes = daoFactory.getPropertyTypeDAO().listDataTypes();
        Map<String, DataTypePE> map = dataTypes.stream().collect(
                Collectors.toMap(t -> t.getCode().toString(), Function.identity()));
        List<PropertyTypePE> propertyTypes = new ArrayList<>();
        PersonPE person = context.getSession().tryGetPerson();
        new CollectionBatchProcessor<PropertyTypeCreation>(context, batch)
            {
                @Override
                public void process(PropertyTypeCreation creation)
                {
                    PropertyTypePE propertyType = new PropertyTypePE();
                    propertyType.setCode(creation.getCode());
                    propertyType.setDescription(creation.getDescription());
                    propertyType.setLabel(creation.getLabel());
                    propertyType.setType(map.get(creation.getDataType().toString()));
                    propertyType.setInternalNamespace(Boolean.TRUE.equals(creation.isInternalNameSpace()));
                    propertyType.setManagedInternally(Boolean.TRUE.equals(creation.isManagedInternally()));
                    propertyType.setRegistrator(person);
                    propertyType.setSchema(creation.getSchema());
                    propertyType.setTransformation(creation.getTransformation());
                    propertyTypes.add(propertyType);
                }

                @Override
                public IProgress createProgress(PropertyTypeCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };
        return propertyTypes;
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<PropertyTypeCreation, PropertyTypePE> batch)
    {
        injectVocabularies(context, batch);
        injectMaterialTypes(context, batch);
    }

    private void injectVocabularies(IOperationContext context, MapBatch<PropertyTypeCreation, PropertyTypePE> batch)
    {
        Set<IVocabularyId> vocabularyIds = extract(batch, PropertyTypeCreation::getVocabularyId);
        Map<IVocabularyId, VocabularyPE> vocabulariesById = mapVocabularyByIdExecutor.map(context, vocabularyIds);
        for (Entry<PropertyTypeCreation, PropertyTypePE> entry : batch.getObjects().entrySet())
        {
            PropertyTypeCreation creation = entry.getKey();
            PropertyTypePE propertyType = entry.getValue();
            IVocabularyId vocabularyId = creation.getVocabularyId();
            if (vocabularyId != null)
            {
                propertyType.setVocabulary(vocabulariesById.get(vocabularyId));
            }
        }
    }

    private void injectMaterialTypes(IOperationContext context, MapBatch<PropertyTypeCreation, PropertyTypePE> batch)
    {
        Set<IEntityTypeId> typeIds = extract(batch, PropertyTypeCreation::getMaterialTypeId);
        Map<IEntityTypeId, EntityTypePE> materialTypesById = mapMaterialTypeByIdExecutor.map(context, 
                ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL, typeIds);
        for (Entry<PropertyTypeCreation, PropertyTypePE> entry : batch.getObjects().entrySet())
        {
            PropertyTypeCreation creation = entry.getKey();
            PropertyTypePE propertyType = entry.getValue();
            IEntityTypeId materialTypeId = creation.getMaterialTypeId();
            if (materialTypeId != null)
            {
                propertyType.setMaterialType((MaterialTypePE) materialTypesById.get(materialTypeId));
            }
        }
    }
    
    private <T> Set<T> extract(MapBatch<PropertyTypeCreation, PropertyTypePE> batch, Function<PropertyTypeCreation, T> mapper)
    {
        return batch.getObjects().keySet().stream()
                .filter(c -> mapper.apply(c) != null).map(mapper).collect(Collectors.toSet());
    }
    
    @Override
    protected List<PropertyTypePE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getPropertyTypeDAO().listAllPropertyTypes();
    }

    @Override
    protected void save(IOperationContext context, List<PropertyTypePE> entities, boolean clearCache)
    {
        for (PropertyTypePE propertyType : entities)
        {
            daoFactory.getPropertyTypeDAO().createPropertyType(propertyType);
        }
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<PropertyTypeCreation, PropertyTypePE> batch)
    {
    }
    
    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "property type", null);
    }

}
