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
import java.util.Collections;
import java.util.List;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.shared.EntityProperty;
import ch.systemsx.cisd.openbis.generic.client.shared.EntityType;
import ch.systemsx.cisd.openbis.generic.client.shared.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The unique {@link IEntityPropertiesConverter} implementation.
 * <p>
 * This implementation caches as much as possible to avoid redundant database requests. This also
 * means that this class should not be reused. Creating a new instance each time this class is
 * needed should be preferred.
 * </p>
 * 
 * @author Christian Ribeaud
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

    private final IPropertyValueValidator propertyValueValidator;

    public EntityPropertiesConverter(final EntityKind entityKind, final IDAOFactory daoFactory)
    {
        this(entityKind, daoFactory, new PropertyValidator());
    }

    @Private
    EntityPropertiesConverter(final EntityKind entityKind, final IDAOFactory daoFactory,
            final IPropertyValueValidator propertyValueValidator)
    {
        assert entityKind != null : "Unspecified entity kind.";
        assert daoFactory != null : "Unspecified DAO factory.";
        assert propertyValueValidator != null : "Unspecified property value validator.";

        this.daoFactory = daoFactory;
        this.entityKind = entityKind;
        this.propertyValueValidator = propertyValueValidator;
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
        final EntityTypePE entityType = entityTypesByCode.tryGet(entityTypeCode.toUpperCase());
        if (entityType == null)
        {
            throw UserFailureException.fromTemplate("Entity type with code '%s' does not exist!",
                    entityTypeCode);
        }
        return entityType;
    }

    private final PropertyTypePE getPropertyType(final String propertyCode)
    {
        PropertyTypePE propertyType = propertyTypesByCode.tryGet(propertyCode.toUpperCase());
        if (propertyType == null)
        {
            propertyType = daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyCode);
            if (propertyType == null)
            {
                throw UserFailureException.fromTemplate(
                        "Property type with code '%s' does not exist!", propertyCode);
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
                            EntityTypePropertyTypeByPropertyTypeKeyExtractor.INSTANCE);
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

    private final <T extends EntityPropertyPE, ET extends EntityType, ETPT extends EntityTypePropertyType<ET>> T tryConvertProperty(
            final PersonPE registrator, final EntityTypePE entityTypePE,
            final EntityProperty<ET, ETPT> property)
    {
        final ETPT entityTypePropertyType = property.getEntityTypePropertyType();
        final String propertyCode = entityTypePropertyType.getPropertyType().getCode();
        final PropertyTypePE propertyType = getPropertyType(propertyCode);
        final String valueOrNull = property.getValue();
        final EntityTypePropertyTypePE entityTypePropertyTypePE =
                getEntityTypePropertyType(entityTypePE, propertyType);
        if (entityTypePropertyTypePE.isMandatory() && valueOrNull == null)
        {
            throw UserFailureException.fromTemplate("No entity property value for '%s'.",
                    propertyCode);
        }
        if (valueOrNull != null)
        {
            final String validated =
                    propertyValueValidator.validatePropertyValue(propertyType, valueOrNull);
            return createEntityProperty(registrator, propertyType, entityTypePropertyTypePE,
                    validated);
        }
        return null;
    }

    private final <T extends EntityPropertyPE> T createEntityProperty(final PersonPE registrator,
            final PropertyTypePE propertyType,
            final EntityTypePropertyTypePE entityTypePropertyType, final String value)
    {
        final T entityProperty = EntityPropertyPE.createEntityProperty(entityKind);
        entityProperty.setRegistrator(registrator);
        entityProperty.setEntityTypePropertyType(entityTypePropertyType);
        final VocabularyTermPE vocabularyTerm = tryGetVocabularyTerm(value, propertyType);
        entityProperty.setUntypedValue(value, vocabularyTerm);
        return entityProperty;
    }

    //
    // IEntityPropertiesConverter
    //

    public final <T extends EntityPropertyPE, ET extends EntityType, ETPT extends EntityTypePropertyType<ET>> List<T> convertProperties(
            final EntityProperty<ET, ETPT>[] properties, final String entityTypeCode,
            final PersonPE registrator)
    {
        assert entityTypeCode != null : "Unspecified entity type code.";
        assert registrator != null : "Unspecified registrator";
        assert properties != null : "Unspecified entity properties";
        if (properties.length == 0)
        {
            return Collections.emptyList();
        }
        final EntityTypePE entityTypePE = getEntityType(entityTypeCode);
        final List<T> list = new ArrayList<T>();
        for (final EntityProperty<ET, ETPT> property : properties)
        {
            final T convertedPropertyOrNull =
                    tryConvertProperty(registrator, entityTypePE, property);
            if (convertedPropertyOrNull != null)
            {
                list.add(convertedPropertyOrNull);
            }
        }
        return list;
    }

    //
    // Helper classes
    //

    private final static class EntityTypePropertyTypeByPropertyTypeKeyExtractor implements
            IKeyExtractor<PropertyTypePE, EntityTypePropertyTypePE>
    {

        static final EntityTypePropertyTypeByPropertyTypeKeyExtractor INSTANCE =
                new EntityTypePropertyTypeByPropertyTypeKeyExtractor();

        private EntityTypePropertyTypeByPropertyTypeKeyExtractor()
        {
            // Can not be instantiated.
        }

        //
        // IKeyExtractor
        //

        public final PropertyTypePE getKey(final EntityTypePropertyTypePE e)
        {
            return e.getPropertyType();
        }
    }

}
