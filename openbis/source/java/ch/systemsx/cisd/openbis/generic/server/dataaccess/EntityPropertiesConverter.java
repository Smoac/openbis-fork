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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.collection.TableMap.UniqueKeyViolationStrategy;
import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;

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
    private static final IKeyExtractor<PropertyTypePE, ExtendedEntityTypePropertyType> EXTENDED_ETPT_KEY_EXTRACTOR =
            new IKeyExtractor<PropertyTypePE, ExtendedEntityTypePropertyType>()
                {
                    @Override
                    public PropertyTypePE getKey(ExtendedEntityTypePropertyType etpt)
                    {
                        return etpt.getEntityTypePropertyTypePE().getPropertyType();
                    }
                };

    private static final String NO_ENTITY_PROPERTY_VALUE_FOR_S =
            "Value of mandatory property '%s' not specified.";

    private final IDAOFactory daoFactory;

    private final EntityKind entityKind;

    private TableMap<String, EntityTypePE> entityTypesByCode;

    private Map<String, Set<String>> dynamicPropertiesByEntityTypeCode =
            new HashMap<String, Set<String>>();

    private Map<String, Set<String>> managedPropertiesByEntityTypeCode =
            new HashMap<String, Set<String>>();

    private final TableMap<String, PropertyTypePE> propertyTypesByCode =
            new TableMap<String, PropertyTypePE>(
                    KeyExtractorFactory.getPropertyTypeByCodeKeyExtractor());

    private Map<String /* Entity type code */, TableMap<PropertyTypePE, ExtendedEntityTypePropertyType>> entityTypePropertyTypesByEntityTypeAndPropertyType =
            new HashMap<String, TableMap<PropertyTypePE, ExtendedEntityTypePropertyType>>();

    private final ComplexPropertyValueHelper complexPropertyValueHelper;

    private final IPropertyValueValidator propertyValueValidator;

    private final IPropertyPlaceholderCreator placeholderCreator;

    public EntityPropertiesConverter(final EntityKind entityKind, final IDAOFactory daoFactory)
    {
        this(entityKind, daoFactory, new PropertyValidator(), new PlaceholderPropertyCreator());
    }

    @Private
    EntityPropertiesConverter(final EntityKind entityKind, final IDAOFactory daoFactory,
            final IPropertyValueValidator propertyValueValidator,
            IPropertyPlaceholderCreator placeholderCreator)
    {
        assert entityKind != null : "Unspecified entity kind.";
        assert daoFactory != null : "Unspecified DAO factory.";
        assert propertyValueValidator != null : "Unspecified property value validator.";

        this.daoFactory = daoFactory;
        this.entityKind = entityKind;
        this.propertyValueValidator = propertyValueValidator;
        this.placeholderCreator = placeholderCreator;
        this.complexPropertyValueHelper = new ComplexPropertyValueHelper(daoFactory, null);
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

    private final Set<String> getManagedProperties(final EntityTypePE entityTypePE)
    {
        String code = entityTypePE.getCode();
        if (managedPropertiesByEntityTypeCode.containsKey(code) == false)
        {
            HashSet<String> set = new HashSet<String>();
            List<EntityTypePropertyTypePE> list =
                    daoFactory.getEntityPropertyTypeDAO(entityKind).listEntityPropertyTypes(
                            entityTypePE);
            for (EntityTypePropertyTypePE etpt : list)
            {
                if (etpt.isManaged())
                {
                    set.add(etpt.getPropertyType().getCode());
                }
            }
            managedPropertiesByEntityTypeCode.put(code, set);
        }
        return managedPropertiesByEntityTypeCode.get(code);
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

    private final ExtendedEntityTypePropertyType getEntityTypePropertyType(
            final EntityTypePE entityTypePE, final PropertyTypePE propertyType)
    {
        String entityTypeCode = entityTypePE.getCode();
        TableMap<PropertyTypePE, ExtendedEntityTypePropertyType> map =
                entityTypePropertyTypesByEntityTypeAndPropertyType.get(entityTypeCode);
        if (map == null)
        {
            List<ExtendedEntityTypePropertyType> entityTypePropertyTypes =
                    getEntityTypePropertyTypes(entityTypePE);
            map =
                    new TableMap<PropertyTypePE, ExtendedEntityTypePropertyType>(
                            entityTypePropertyTypes, EXTENDED_ETPT_KEY_EXTRACTOR,
                            UniqueKeyViolationStrategy.KEEP_FIRST);
            entityTypePropertyTypesByEntityTypeAndPropertyType.put(entityTypeCode, map);
        }

        final ExtendedEntityTypePropertyType entityTypePropertyType = map.tryGet(propertyType);

        if (entityTypePropertyType == null)
        {
            throw UserFailureException.fromTemplate(
                    "No assigment between property type '%s' and entity type '%s' could be found.",
                    propertyType.getCode(), entityTypeCode);
        }
        return entityTypePropertyType;
    }

    private List<ExtendedEntityTypePropertyType> getEntityTypePropertyTypes(
            EntityTypePE entityTypePE)
    {
        IEntityPropertyTypeDAO entityPropertyTypeDAO =
                daoFactory.getEntityPropertyTypeDAO(entityKind);
        List<EntityTypePropertyTypePE> entityPropertyTypes =
                entityPropertyTypeDAO.listEntityPropertyTypes(entityTypePE);
        List<ExtendedEntityTypePropertyType> result =
                new ArrayList<ExtendedEntityTypePropertyType>();
        for (EntityTypePropertyTypePE entityTypePropertyTypePE : entityPropertyTypes)
        {
            result.add(new ExtendedEntityTypePropertyType(entityTypePropertyTypePE));
        }
        return result;
    }

    private final <T extends EntityPropertyPE> T tryConvertProperty(final PersonPE registrator,
            final EntityTypePE entityTypePE, final IEntityProperty property)
    {
        final String propertyCode = property.getPropertyType().getCode();
        final PropertyTypePE propertyType = getPropertyType(propertyCode);
        final String valueOrNull = property.tryGetAsString();
        ExtendedEntityTypePropertyType extendedETPT =
                getEntityTypePropertyType(entityTypePE, propertyType);
        final EntityTypePropertyTypePE entityTypePropertyTypePE =
                extendedETPT.getEntityTypePropertyTypePE();
        if (entityTypePropertyTypePE.isMandatory() && valueOrNull == null)
        {
            throw UserFailureException.fromTemplate(NO_ENTITY_PROPERTY_VALUE_FOR_S, propertyCode);
        }
        if (valueOrNull != null)
        {
            final String validated =
                    propertyValueValidator.validatePropertyValue(propertyType, valueOrNull);

            return createEntityProperty(registrator, propertyType, entityTypePropertyTypePE,
                    extendedETPT.translate(validated));
        }
        return null;
    }

    private final <T extends EntityPropertyPE> T createEntityProperty(final PersonPE registrator,
            final PropertyTypePE propertyType,
            final EntityTypePropertyTypePE entityTypePropertyType, final String value)
    {
        final T entityProperty = EntityPropertyPE.createEntityProperty(entityKind);
        entityProperty.setRegistrator(registrator);
        entityProperty.setAuthor(registrator);
        entityProperty.setEntityTypePropertyType(entityTypePropertyType);
        setPropertyValue(entityProperty, propertyType, value);
        return entityProperty;
    }

    //
    // IEntityPropertiesConverter
    //

    @Override
    public final <T extends EntityPropertyPE> List<T> convertProperties(
            final IEntityProperty[] properties, final String entityTypeCode,
            final PersonPE registrator)
    {
        return convertProperties(properties, entityTypeCode, registrator, true, true);
    }

    private final <T extends EntityPropertyPE> List<T> convertProperties(
            final IEntityProperty[] properties, final String entityTypeCode,
            final PersonPE registrator, final boolean createManagedPropertiesPlaceholders,
            boolean createDynamicPropertiesPlaceholders)
    {
        assert entityTypeCode != null : "Unspecified entity type code.";
        assert registrator != null : "Unspecified registrator";
        assert properties != null : "Unspecified entity properties";
        final EntityTypePE entityTypePE = getEntityType(entityTypeCode);
        Set<String> dynamicProperties = getDynamicProperties(entityTypePE);
        Set<String> managedProperties = getManagedProperties(entityTypePE);
        Set<IEntityProperty> definedProperties =
                new LinkedHashSet<IEntityProperty>(Arrays.asList(properties));
        if (createDynamicPropertiesPlaceholders)
        {
            placeholderCreator.addDynamicPropertiesPlaceholders(definedProperties,
                    dynamicProperties);
        }
        if (createManagedPropertiesPlaceholders)
        {
            placeholderCreator.addManagedPropertiesPlaceholders(definedProperties,
                    managedProperties);
        }
        final List<T> list = new ArrayList<T>();
        for (final IEntityProperty property : definedProperties)
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public final <T extends EntityPropertyPE> T createValidatedProperty(
            PropertyTypePE propertyType, EntityTypePropertyTypePE entityTypPropertyType,
            final PersonPE registrator, String validatedValue)
    {
        assert validatedValue != null;
        return createEntityProperty(registrator, propertyType, entityTypPropertyType,
                validatedValue);
    }

    @Override
    public final <T extends EntityPropertyPE> void setPropertyValue(final T entityProperty,
            final PropertyTypePE propertyType, final String validatedValue)
    {
        assert validatedValue != null;
        if (validatedValue.startsWith(BasicConstant.ERROR_PROPERTY_PREFIX))
        {
            // save errors as strings
            entityProperty.setUntypedValue(validatedValue, null, null);
        } else
        {
            final VocabularyTermPE vocabularyTerm =
                    complexPropertyValueHelper.tryGetVocabularyTerm(validatedValue, propertyType);
            final MaterialPE material =
                    complexPropertyValueHelper.tryGetMaterial(validatedValue, propertyType);
            entityProperty.setUntypedValue(validatedValue, vocabularyTerm, material);
        }
    }

    @Override
    public <T extends EntityPropertyPE> Set<T> updateProperties(Collection<T> oldProperties,
            EntityTypePE entityType, List<IEntityProperty> newProperties, PersonPE author)
    {
        final List<T> convertedProperties =
                convertPropertiesForUpdate(newProperties, entityType.getCode(), author);
        final Set<T> set = new HashSet<T>();
        for (T newProperty : convertedProperties)
        {
            PropertyTypePE propertyType = newProperty.getEntityTypePropertyType().getPropertyType();
            T existingProperty = tryFind(oldProperties, propertyType);
            if (existingProperty != null)
            {
                existingProperty.setUntypedValue(newProperty.getValue(),
                        newProperty.getVocabularyTerm(), newProperty.getMaterialValue());
                existingProperty.setAuthor(author);
                set.add(existingProperty);
            } else
            {
                set.add(newProperty);
            }
        }
        return set;
    }

    /**
     * Update the value of a managed property, assuming the managedProperty already has the updated
     * value.
     */
    @Override
    public <T extends EntityPropertyPE> Set<T> updateManagedProperty(Collection<T> oldProperties,
            EntityTypePE entityType, IManagedProperty managedProperty, PersonPE author)
    {

        final Set<T> set = new HashSet<T>();

        // Add all existing properties
        set.addAll(oldProperties);

        // Update the managed property we want to update
        T existingProperty = tryFind(oldProperties, managedProperty.getPropertyTypeCode());
        if (existingProperty != null)
        {
            existingProperty.setUntypedValue(managedProperty.getValue(), null, null);
            existingProperty.setAuthor(author);
        }
        return set;
    }

    private final <T extends EntityPropertyPE> List<T> convertPropertiesForUpdate(
            final List<? extends IEntityProperty> properties, final String entityTypeCode,
            final PersonPE registrator)
    {
        IEntityProperty[] propsArray = properties.toArray(new IEntityProperty[0]);
        return convertProperties(propsArray, entityTypeCode, registrator, false, false);
    }

    @Override
    public <T extends EntityPropertyPE> Set<T> updateProperties(Collection<T> oldProperties,
            EntityTypePE entityType, List<IEntityProperty> newProperties, PersonPE author,
            Set<String> propertiesToUpdate)
    {
        // all new properties should be among propertiesToUpdate (no need to check it)
        final Set<T> set = updateProperties(oldProperties, entityType, newProperties, author);
        // add old properties that are not among propertiesToUpdate (preserve those properties)
        for (T oldProperty : oldProperties)
        {
            final String oldPropertyCode =
                    oldProperty.getEntityTypePropertyType().getPropertyType().getCode();
            // TODO 2011-02-16, Piotr Buczek: improve case sensitivity checks
            if (propertiesToUpdate.contains(oldPropertyCode.toLowerCase()) == false
                    && propertiesToUpdate.contains(oldPropertyCode) == false)
            {
                set.add(oldProperty);
            }
        }
        return set;
    }

    private static <T extends EntityPropertyPE> T tryFind(Collection<T> oldProperties,
            PropertyTypePE propertyType)
    {
        for (T oldProperty : oldProperties)
        {
            if (oldProperty.getEntityTypePropertyType().getPropertyType().equals(propertyType))
            {
                return oldProperty;
            }
        }
        return null;
    }

    private static <T extends EntityPropertyPE> T tryFind(Collection<T> oldProperties,
            String propertyTypeCode)
    {
        for (T oldProperty : oldProperties)
        {
            if (oldProperty.getEntityTypePropertyType().getPropertyType().getCode()
                    .equals(propertyTypeCode))
            {
                return oldProperty;
            }
        }
        return null;
    }

    //
    // Helper classes
    //

    private static final class ExtendedEntityTypePropertyType
    {
        private final EntityTypePropertyTypePE entityTypePropertyTypePE;

        private final List<IManagedInputWidgetDescription> inputWidgetDescriptions;

        private ManagedPropertyEvaluator evaluator;

        ExtendedEntityTypePropertyType(EntityTypePropertyTypePE entityTypePropertyTypePE)
        {
            this.entityTypePropertyTypePE = entityTypePropertyTypePE;
            if (entityTypePropertyTypePE.isManaged())
            {
                String script = entityTypePropertyTypePE.getScript().getScript();
                evaluator = ManagedPropertyEvaluatorFactory.createManagedPropertyEvaluator(script);
                inputWidgetDescriptions = evaluator.getInputWidgetDescriptions();
            } else
            {
                inputWidgetDescriptions = Collections.emptyList();
            }
        }

        public EntityTypePropertyTypePE getEntityTypePropertyTypePE()
        {
            return entityTypePropertyTypePE;
        }

        @SuppressWarnings("unchecked")
        String translate(String propertyValue)
        {
            if (inputWidgetDescriptions.isEmpty() || propertyValue == null
                    || propertyValue.startsWith(BasicConstant.ERROR_PROPERTY_PREFIX))
            {
                return propertyValue;
            }
            try
            {
                List<?> readValue = new ObjectMapper().readValue(propertyValue, List.class);
                ManagedProperty managedProperty = new ManagedProperty();
                for (Object row : readValue)
                {
                    if (row instanceof Map == false)
                    {
                        continue;
                    }
                    evaluator.updateFromBatchInput(managedProperty, (Map<String, String>) row);
                }
                return managedProperty.getValue();
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

    }

    public interface IHibernateSessionProvider
    {
        public Session getSession();
    }

    public final static class ComplexPropertyValueHelper
    {

        private final IDAOFactory daoFactory;

        // WORKAROUND This information needs to be exposed to force MaterialDAO to use given session
        // for requests coming from DynamicPropertyEvaluator. Otherwise each call to tryGetMaterial
        // creates a new DB connection that are not closed.
        private final IHibernateSessionProvider customSessionProviderOrNull;

        /**
         * @param customSessionProviderOrNull Provider of custom session that should be used for
         *            accessing DB instead of default one. If null the standard way of getting the
         *            session should be used.
         */
        public ComplexPropertyValueHelper(IDAOFactory daoFactory,
                IHibernateSessionProvider customSessionProviderOrNull)
        {
            this.daoFactory = daoFactory;
            this.customSessionProviderOrNull = customSessionProviderOrNull;
        }

        public MaterialPE tryGetMaterial(String value, PropertyTypePE propertyType)
        {
            if (propertyType.getType().getCode() != DataTypeCode.MATERIAL)
            {
                return null; // this is not a property of MATERIAL type
            }
            MaterialIdentifier materialIdentifier = MaterialIdentifier.tryParseIdentifier(value);
            if (materialIdentifier == null)
            {
                MaterialTypePE fixedMaterialType = propertyType.getMaterialType();
                // if the material type of the property is fixed, then we accept when only material
                // code is specified and its type is skipped (we know what the type should be)
                if (fixedMaterialType != null && StringUtils.isBlank(value) == false)
                {
                    materialIdentifier = new MaterialIdentifier(value, fixedMaterialType.getCode());
                } else
                {
                    // identifier is invalid or null
                    return null;
                }
            }

            final MaterialPE material;
            if (customSessionProviderOrNull != null)
            {
                material =
                        daoFactory.getMaterialDAO().tryFindMaterial(
                                customSessionProviderOrNull.getSession(), materialIdentifier);
            } else
            {
                material = daoFactory.getMaterialDAO().tryFindMaterial(materialIdentifier);
            }

            if (material == null)
            {
                throw UserFailureException.fromTemplate(
                        "No material could be found for identifier '%s'.", materialIdentifier);
            }
            return material;
        }

        public VocabularyTermPE tryGetVocabularyTerm(final String value,
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
            final VocabularyTermPE term = vocabulary.tryGetVocabularyTerm(value);
            if (term != null)
            {
                return term;
            }
            throw UserFailureException.fromTemplate(
                    "Incorrect value '%s' for a controlled vocabulary set '%s'.", value,
                    vocabulary.getCode());
        }
    }

}
