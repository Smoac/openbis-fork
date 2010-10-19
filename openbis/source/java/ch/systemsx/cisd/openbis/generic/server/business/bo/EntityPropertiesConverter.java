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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyValueValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PropertyValidator;
import ch.systemsx.cisd.openbis.generic.server.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
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
    private static boolean CHECK_DYNAMIC_PROPERTIES_NOT_UPDATED_MANUALLY = false;

    private static boolean CREATE_DYNAMIC_PROPERTIES_PLACEHOLDERS = false;

    private static final String NO_ENTITY_PROPERTY_VALUE_FOR_S =
            "Value of mandatory property '%s' not specified.";

    private final IDAOFactory daoFactory;

    private final EntityKind entityKind;

    private TableMap<String, EntityTypePE> entityTypesByCode;

    private Map<String, Set<String>> dynamicPropertiesByEntityTypeCode =
            new HashMap<String, Set<String>>();

    private final TableMap<String, PropertyTypePE> propertyTypesByCode =
            new TableMap<String, PropertyTypePE>(
                    KeyExtractorFactory.getPropertyTypeByCodeKeyExtractor());

    private TableMap<PropertyTypePE, EntityTypePropertyTypePE> entityTypePropertyTypesByPropertyTypes;

    private final IPropertyValueValidator propertyValueValidator;

    public EntityPropertiesConverter(final EntityKind entityKind, final IDAOFactory daoFactory)
    {
        this(entityKind, daoFactory, new PropertyValidator(daoFactory));
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

    private final Set<String> getDynamicProperties(final EntityTypePE entityTypePE)
    {
        String code = entityTypePE.getCode();
        if (dynamicPropertiesByEntityTypeCode.containsKey(code) == false)
        {
            HashSet<String> set = new HashSet<String>();
            List<EntityTypePropertyTypePE> list =
                    daoFactory.getEntityPropertyTypeDAO(entityKind).listEntityPropertyTypes(
                            entityTypePE);
            for (EntityTypePropertyTypePE etpt : list)
            {
                if (etpt.isDynamic())
                {
                    set.add(etpt.getPropertyType().getCode());
                }
            }
            dynamicPropertiesByEntityTypeCode.put(code, set);
        }
        return dynamicPropertiesByEntityTypeCode.get(code);
    }

    private final EntityTypePE getEntityType(final String entityTypeCode)
    {
        if (entityTypesByCode == null)
        {
            entityTypesByCode =
                    new TableMap<String, EntityTypePE>(daoFactory.getEntityTypeDAO(entityKind)
                            .listEntityTypes(),
                            KeyExtractorFactory.getEntityTypeByCodeKeyExtractor());
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

    private MaterialPE tryGetMaterial(String value, PropertyTypePE propertyType)
    {
        if (propertyType.getType().getCode() != DataTypeCode.MATERIAL)
        {
            return null; // this is not a property of MATERIAL type
        }
        MaterialIdentifier materialIdentifier = MaterialIdentifier.tryParseIdentifier(value);
        if (materialIdentifier == null)
        {
            // identifier is valid but null
            return null;
        }
        MaterialPE material = daoFactory.getMaterialDAO().tryFindMaterial(materialIdentifier);
        if (material == null)
        {
            throw UserFailureException.fromTemplate(
                    "No material could be found for identifier '%s'.", materialIdentifier);
        }
        return material;
    }

    private final static VocabularyTermPE tryGetVocabularyTerm(final String untypedValue,
            final PropertyTypePE propertyType)
    {
        if (propertyType.getType().getCode() != DataTypeCode.CONTROLLEDVOCABULARY)
        {
            return null; // this is not a property of CONTROLLED VOCABULARY type
        }

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

    private final <T extends EntityPropertyPE> T tryConvertProperty(final PersonPE registrator,
            final EntityTypePE entityTypePE, final IEntityProperty property)
    {
        final String propertyCode = property.getPropertyType().getCode();
        final PropertyTypePE propertyType = getPropertyType(propertyCode);
        final String valueOrNull = property.tryGetAsString();
        final EntityTypePropertyTypePE entityTypePropertyTypePE =
                getEntityTypePropertyType(entityTypePE, propertyType);
        if (entityTypePropertyTypePE.isMandatory() && valueOrNull == null)
        {
            throw UserFailureException.fromTemplate(NO_ENTITY_PROPERTY_VALUE_FOR_S, propertyCode);
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
        final MaterialPE material = tryGetMaterial(value, propertyType);
        entityProperty.setUntypedValue(value, vocabularyTerm, material);
        return entityProperty;
    }

    //
    // IEntityPropertiesConverter
    //

    private final <T extends EntityPropertyPE> List<T> convertProperties(
            final List<? extends IEntityProperty> properties, final String entityTypeCode,
            final PersonPE registrator)
    {
        IEntityProperty[] propsArray = properties.toArray(new IEntityProperty[0]);
        return convertProperties(propsArray, entityTypeCode, registrator);
    }

    public final <T extends EntityPropertyPE> List<T> convertProperties(
            final IEntityProperty[] properties, final String entityTypeCode,
            final PersonPE registrator)
    {
        assert entityTypeCode != null : "Unspecified entity type code.";
        assert registrator != null : "Unspecified registrator";
        assert properties != null : "Unspecified entity properties";
        final EntityTypePE entityTypePE = getEntityType(entityTypeCode);
        Set<String> dynamicProperties = getDynamicProperties(entityTypePE);
        Set<String> propertiesToUpdate = extractPropertiesToUpdate(properties);
        checkDynamicPropertiesNotManuallyUpdated(propertiesToUpdate, dynamicProperties);
        List<IEntityProperty> newProperties =
                new ArrayList<IEntityProperty>(Arrays.asList(properties));
        addDynamicPropertiesPlaceholders(newProperties, propertiesToUpdate, dynamicProperties);
        final List<T> list = new ArrayList<T>();
        for (final IEntityProperty property : newProperties)
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

    private Set<String> extractPropertiesToUpdate(IEntityProperty[] properties)
    {
        HashSet<String> result = new HashSet<String>();
        for (IEntityProperty p : properties)
        {
            result.add(p.getPropertyType().getCode());
        }
        return result;
    }

    public <T extends EntityPropertyPE> void checkMandatoryProperties(Collection<T> properties,
            EntityTypePE entityTypePE)
    {
        assert properties != null;
        checkMandatoryProperties(
                properties,
                entityTypePE,
                daoFactory.getEntityPropertyTypeDAO(entityKind).listEntityPropertyTypes(
                        entityTypePE));
    }

    public <T extends EntityPropertyPE> void checkMandatoryProperties(Collection<T> properties,
            EntityTypePE entityTypePE, Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache)
    {
        assert properties != null;
        checkMandatoryProperties(properties, entityTypePE,
                getAssignedPropertiesForEntityType(cache, entityTypePE));

    }

    private List<EntityTypePropertyTypePE> getAssignedPropertiesForEntityType(
            Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache, EntityTypePE entityTypePE)
    {
        List<EntityTypePropertyTypePE> assignedProperties = cache.get(entityTypePE);
        if (assignedProperties == null)
        {
            assignedProperties =
                    daoFactory.getEntityPropertyTypeDAO(entityKind).listEntityPropertyTypes(
                            entityTypePE);
            cache.put(entityTypePE, assignedProperties);
        }
        return assignedProperties;
    }

    private <T extends EntityPropertyPE> void checkMandatoryProperties(Collection<T> properties,
            EntityTypePE entityTypePE, List<EntityTypePropertyTypePE> assignedProperties)
    {
        assert properties != null;
        if (assignedProperties == null || assignedProperties.size() == 0)
        {
            return;
        }
        Set<EntityTypePropertyTypePE> definedProperties = new HashSet<EntityTypePropertyTypePE>();
        for (T p : properties)
        {
            definedProperties.add(p.getEntityTypePropertyType());
        }
        for (EntityTypePropertyTypePE etpt : assignedProperties)
        {
            if (etpt.isMandatory() && (definedProperties.contains(etpt) == false))
            {
                throw UserFailureException.fromTemplate(NO_ENTITY_PROPERTY_VALUE_FOR_S, etpt
                        .getPropertyType().getCode());
            }
        }
    }

    public final String tryCreateValidatedPropertyValue(PropertyTypePE propertyType,
            EntityTypePropertyTypePE entityTypPropertyType, String value)
    {
        if (entityTypPropertyType.isMandatory() && value == null)
        {
            throw UserFailureException.fromTemplate(NO_ENTITY_PROPERTY_VALUE_FOR_S,
                    propertyType.getCode());
        }
        if (value != null)
        {
            final String validated =
                    propertyValueValidator.validatePropertyValue(propertyType, value);
            return validated;
        }
        return null;
    }

    public final <T extends EntityPropertyPE> T createValidatedProperty(
            PropertyTypePE propertyType, EntityTypePropertyTypePE entityTypPropertyType,
            final PersonPE registrator, String validatedValue)
    {
        assert validatedValue != null;
        return createEntityProperty(registrator, propertyType, entityTypPropertyType,
                validatedValue);
    }

    public <T extends EntityPropertyPE> Set<T> updateProperties(Collection<T> oldProperties,
            EntityTypePE entityType, List<IEntityProperty> newProperties, PersonPE registrator,
            Set<String> dynamicProperties)
    {
        Set<String> propertiesToUpdate = extractPropertiesToUpdate(newProperties);
        checkDynamicPropertiesNotManuallyUpdated(propertiesToUpdate, dynamicProperties);
        addDynamicPropertiesPlaceholders(newProperties, propertiesToUpdate, dynamicProperties);
        final List<T> convertedProperties =
                convertProperties(newProperties, entityType.getCode(), registrator);
        final Set<T> set = new HashSet<T>();
        for (T newProperty : convertedProperties)
        {
            T existingProperty = tryFind(oldProperties, newProperty);
            if (existingProperty != null)
            {
                existingProperty.setUntypedValue(newProperty.getValue(),
                        newProperty.getVocabularyTerm(), newProperty.getMaterialValue());
                set.add(existingProperty);
            } else
            {
                set.add(newProperty);
            }
        }
        return set;
    }

    private <P extends IEntityProperty> Set<String> extractPropertiesToUpdate(List<P> newProperties)
    {
        Set<String> propertiesToUpdate = new HashSet<String>();
        for (P np : newProperties)
        {
            propertiesToUpdate.add(np.getPropertyType().getCode());
        }
        return propertiesToUpdate;
    }

    public <T extends EntityPropertyPE> Set<T> updateProperties(Collection<T> oldProperties,
            EntityTypePE entityType, List<IEntityProperty> newProperties, PersonPE registrator,
            Set<String> propertiesToUpdate, Set<String> dynamicProperties)
    {
        checkDynamicPropertiesNotManuallyUpdated(propertiesToUpdate, dynamicProperties);
        addDynamicPropertiesPlaceholders(newProperties, propertiesToUpdate, dynamicProperties);
        // all new properties should be among propertiesToUpdate (no need to check it)
        final Set<T> set =
                updateProperties(oldProperties, entityType, newProperties, registrator,
                        dynamicProperties);
        // add old properties that are not among propertiesToUpdate (preserve those properties)
        for (T oldProperty : oldProperties)
        {
            final String oldPropertyCode =
                    oldProperty.getEntityTypePropertyType().getPropertyType().getCode();
            if (propertiesToUpdate.contains(oldPropertyCode.toLowerCase()) == false)
            {
                set.add(oldProperty);
            }
        }
        return set;
    }

    private void addDynamicPropertiesPlaceholders(List<IEntityProperty> newProperties,
            Set<String> propertiesToUpdate, Set<String> dynamicProperties)
    {
        if (CREATE_DYNAMIC_PROPERTIES_PLACEHOLDERS)
        {
            propertiesToUpdate.addAll(dynamicProperties);
            for (String dp : dynamicProperties)
            {
                final IEntityProperty entityProperty = new EntityProperty();
                entityProperty.setValue(BasicConstant.PLACEHOLDER_PROPERTY_VALUE);
                PropertyType propertyType = new PropertyType();
                propertyType.setCode(dp);
                entityProperty.setPropertyType(propertyType);
                newProperties.add(entityProperty);
            }
        }
    }

    private void checkDynamicPropertiesNotManuallyUpdated(Set<String> propertiesToUpdate,
            Set<String> dynamicProperties)
    {
        if (CHECK_DYNAMIC_PROPERTIES_NOT_UPDATED_MANUALLY)
        {
            HashSet<String> allPropertiesToUpdate = new HashSet<String>();
            for (String p : propertiesToUpdate)
            {
                allPropertiesToUpdate.add(p.toUpperCase());
            }
            HashSet<String> dynamicPropertiesToUpdate = new HashSet<String>(dynamicProperties);
            dynamicPropertiesToUpdate.retainAll(allPropertiesToUpdate);
            if (dynamicPropertiesToUpdate.isEmpty() == false)
            {
                throw new UserFailureException(String.format(
                        "Dynamic properties cannot be updated manually [%s]",
                        StringUtils.join(dynamicPropertiesToUpdate, ",")));
            }
        }
    }

    private static <T extends EntityPropertyPE> T tryFind(Collection<T> oldProperties, T p)
    {
        for (T oldProperty : oldProperties)
        {
            if (oldProperty.getEntityTypePropertyType().getPropertyType()
                    .equals(p.getEntityTypePropertyType().getPropertyType()))
            {
                return oldProperty;
            }
        }
        return null;
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
