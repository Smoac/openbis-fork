/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityPropertyValue;

/**
 * The unique {@link IEntityPropertiesConverter} implementation.
 * <p>
 * This implementation caches as much as possible to avoid redundant database requests. This also
 * means that this class should not be reused. Creating a new instance each time this class is
 * needed should be preferred.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class EntityPropertiesConverter implements IEntityPropertiesConverter
{
    private final IDAOFactory daoFactory;

    private final EntityKind entityKind;

    private TableMap<String, EntityTypePE> entityTypesByCode;

    private final TableMap<String, PropertyTypePE> propertyTypesByCode =
            new TableMap<String, PropertyTypePE>(KeyExtractorFactory
                    .getPropertyTypeByCodeKeyExtractor());

    private TableMap<PropertyTypePE, EntityTypePropertyTypePE> entityTypePropertyTypesByPropertyTypes;

    public EntityPropertiesConverter(final EntityKind entityKind, final IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
        this.entityKind = entityKind;
    }

    private final EntityTypePE getEntityType(final String entityTypeCode)
    {
        if (entityTypesByCode == null)
        {
            entityTypesByCode =
                    new TableMap<String, EntityTypePE>(daoFactory.getEntityTypeDAO(entityKind)
                            .listEntityTypes(), KeyExtractorFactory
                            .getEntityTypeByCodeKeyExtractor());
        }
        final EntityTypePE entityType = entityTypesByCode.tryGet(entityTypeCode);
        if (entityType == null)
        {
            throw UserFailureException.fromTemplate(
                    "Entity type with the code '%s' does not exist!", entityTypeCode);
        }
        return entityType;
    }

    private final PropertyTypePE getPropertyType(final String propertyCode)
    {
        PropertyTypePE propertyType = propertyTypesByCode.tryGet(propertyCode);
        if (propertyType == null)
        {
            propertyType = daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyCode);
            if (propertyType == null)
            {
                throw UserFailureException.fromTemplate(
                        "Property type with the code '%s' does not exist!", propertyCode);
            }
            propertyTypesByCode.add(propertyType);
        }
        return propertyType;
    }

    private final static VocabularyTermPE tryGetVocabularyTerm(final String untypedValue,
            final PropertyTypePE propertyType)
    {
        final VocabularyPE vocabulary = propertyType.getVocabulary();
        if (vocabulary == null)
        {
            return null;
        }
        final VocabularyTermPE term = vocabulary.tryGetVocabularyTerm(untypedValue);
        if (term != null)
        {
            return term;
        }
        throw UserFailureException.fromTemplate(
                "Incorrect value '%s' for a controlled vocabulary set '%s'.", untypedValue,
                vocabulary.getCode());
    }

    private final EntityTypePropertyTypePE getEntityTypePropertyType(
            final EntityTypePE entityTypePE, final PropertyTypePE propertyType)
    {
        if (entityTypePropertyTypesByPropertyTypes == null)
        {
            entityTypePropertyTypesByPropertyTypes =
                    new TableMap<PropertyTypePE, EntityTypePropertyTypePE>(daoFactory
                            .getEntityPropertyTypeDAO(entityKind).listEntityPropertyTypes(
                                    entityTypePE),
                            new EntityTypePropertyTypeByPropertyTypeKeyExtractor());
        }
        final EntityTypePropertyTypePE entityTypePropertyType =
                entityTypePropertyTypesByPropertyTypes.tryGet(propertyType);
        if (entityTypePropertyType == null)
        {
            throw UserFailureException.fromTemplate(
                    "No assigment between property type '%s' and entity type '%s' could be found.",
                    propertyType.getCode(), entityTypePE.getCode());
        }
        return entityTypePropertyType;
    }

    public final <T extends EntityPropertyPE> List<T> convertProperties(
            final SimpleEntityProperty[] properties, final String entityTypeCode,
            final PersonPE registrator)
    {
        final EntityTypePE entityTypePE = getEntityType(entityTypeCode);
        final List<T> result = new ArrayList<T>();
        for (final SimpleEntityProperty property : properties)
        {
            final String propertyCode = property.getCode();
            final PropertyTypePE propertyType = getPropertyType(propertyCode);
            final EntityTypePropertyTypePE entityTypePropertyType =
                    getEntityTypePropertyType(entityTypePE, propertyType);
            final String untypedValueOrNull =
                    EntityPropertyValue.createFromSimple(property).tryGetUntypedValue();
            if (entityTypePropertyType.isMandatory() && untypedValueOrNull == null)
            {
                throw UserFailureException.fromTemplate("No entity property value for '%s'.",
                        propertyCode);
            }
            if (untypedValueOrNull != null)
            {
                final T entityProperty = EntityPropertyPE.createEntityProperty(entityKind);
                entityProperty.setRegistrator(registrator);
                entityProperty.setEntityTypePropertyType(entityTypePropertyType);
                final VocabularyTermPE vocabularyTerm =
                        tryGetVocabularyTerm(untypedValueOrNull, propertyType);
                entityProperty.setUntypedValue(untypedValueOrNull, vocabularyTerm);
                result.add(entityProperty);
            }
        }
        return result;
    }

    private final class EntityTypePropertyTypeByPropertyTypeKeyExtractor implements
            IKeyExtractor<PropertyTypePE, EntityTypePropertyTypePE>
    {
        public final PropertyTypePE getKey(final EntityTypePropertyTypePE e)
        {
            return e.getPropertyType();
        }
    }

}
