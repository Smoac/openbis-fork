/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto.properties;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * Stores a set ({@link Map}) of {@link EntityTypePropertyTypePE}s and provides useful methods.
 * 
 * @author Tomasz Pylak
 */
public class EntityTypePropertyTypeContainer extends AbstractHashable
{
    /**
     * Factory method for creating {@link EntityTypePropertyTypeContainer}s from
     * {@link EntityTypePropertyTypePE} lists.
     * 
     * @param propertyTypes
     */
    public static EntityTypePropertyTypeContainer create(
            final List<EntityTypePropertyTypePE> propertyTypes)
    {
        final EntityTypePropertyTypeContainer container = new EntityTypePropertyTypeContainer();
        for (final EntityTypePropertyTypePE entityPropertyType : propertyTypes)
        {
            container.addEntityPropertyType(entityPropertyType);
        }
        return container;
    }

    /**
     * Factory method for creating {@link EntityTypePropertyTypeContainer}s from
     * {@link EntityTypePropertyTypePE} array.
     * 
     * @param schemas2
     */
    public static EntityTypePropertyTypeContainer createFromTable(
            final EntityTypePropertyTypePE[] schemas2)
    {
        if (schemas2 == null)
        {
            return null;
        }
        final EntityTypePropertyTypeContainer schema = new EntityTypePropertyTypeContainer();
        for (final EntityTypePropertyTypePE entityPropertyType : schemas2)
        {
            schema.addEntityPropertyType(entityPropertyType);
        }
        return schema;
    }

    private static UserFailureException createMandatoryException(final String name)
    {
        return UserFailureException.fromTemplate(
                "Property '%s' is mandatory and cannot be set to the empty value.", name);
    }

    private final Map<String, EntityTypePropertyTypePE> container;

    private EntityTypePropertyTypeContainer()
    {
        this.container = new LinkedHashMap<String, EntityTypePropertyTypePE>();
    }

    /**
     * Returns an array of {@link EntityTypePropertyTypePE} which typeCodes were stored in
     * container.
     */
    public EntityTypePropertyTypePE[] createTable()
    {
        final Set<String> allCodes = container.keySet();
        final EntityTypePropertyTypePE[] result = new EntityTypePropertyTypePE[allCodes.size()];
        int i = 0;
        for (final String code : allCodes)
        {
            final EntityTypePropertyTypePE orig = getPropertyType(code);
            result[i] = orig;
            i++;
        }
        return result;
    }

    /**
     * Returns an array of propertyTypeCodes stored in the container.
     */
    public String[] getAllPropertyCodes()
    {
        return container.keySet().toArray(new String[0]);
    }

    /**
     * Returns {@link EntityTypePropertyTypePE} for given <var>propertyTypeCode</var>.
     * 
     * @param propertyTypeCode
     * @throws UserFailureException if there is no {@link EntityTypePropertyTypePE} with specified
     *             <var>propertyTypeCode</var>.
     */
    public EntityTypePropertyTypePE getPropertyType(final String propertyTypeCode)
    {
        final EntityTypePropertyTypePE type = tryGetPropertyType(propertyTypeCode);
        if (type == null)
        {
            throw UserFailureException.fromTemplate("Unknown property '%s'", propertyTypeCode);
        }
        return type;
    }

    /**
     * Returns true if container contains given propertyTypeCode.
     * 
     * @param propertyTypeCode
     */
    public boolean hasPropertySchema(final String propertyTypeCode)
    {
        return container.containsKey(propertyTypeCode);
    }

    /**
     * Returns {@link EntityTypePropertyTypePE} or null for given propertyTypeCode.
     * 
     * @param propertyTypeCode
     */
    public EntityTypePropertyTypePE tryGetPropertyType(final String propertyTypeCode)
    {
        return container.get(StringUtils.upperCase(propertyTypeCode));
    }

    /**
     * Checks if value is mandatory and casts it to appropriate type.
     * 
     * @param propertyTypeCode
     * @param untypedValueOrNull
     */
    public EntityPropertyValue validateAndCreateValue(final String propertyTypeCode,
            final String untypedValueOrNull)
    {
        final String upperCaseCode = StringUtils.upperCase(propertyTypeCode);
        final EntityTypePropertyTypePE propertyType = getPropertyType(upperCaseCode);
        assert propertyType != null;
        if (propertyType.isMandatory() && untypedValueOrNull == null)
        {
            throw createMandatoryException(upperCaseCode);
        }
        return EntityPropertyValue.createFromUntyped(untypedValueOrNull, propertyType
                .getPropertyType().getType().getCode());
    }

    /**
     * Adds {@link EntityTypePropertyTypePE} to container.
     * 
     * @param entityPropertyType
     * @throws IllegalArgumentException if property was already present
     */
    private void addEntityPropertyType(final EntityTypePropertyTypePE entityPropertyType)
    {
        final String propertyTypeCode = entityPropertyType.getPropertyType().getCode();
        if (container.get(propertyTypeCode) != null)
        {
            throw new IllegalArgumentException("property already present: " + propertyTypeCode);
        }
        container.put(propertyTypeCode, entityPropertyType);
    }

    //
    // Methods introduced for testing purposes
    //

    /**
     * <i>Only to be used in unit tests.</i>
     */
    public static EntityTypePropertyTypeContainer only4TestingCreateEmpty()
    {
        return new EntityTypePropertyTypeContainer();
    }

    /**
     * <i>Only to be used in unit tests.</i>
     * <p>
     * Fails if property was already present.
     */
    @Private
    public EntityTypePropertyTypePE only4TestingAddEntityPropertyType(final long id,
            final boolean isMandatory, final Date registrationDate, final PersonPE registrator,
            final PropertyTypePE propertyType)
    {
        final EntityTypePropertyTypePE entityPropertyType = new MaterialTypePropertyTypePE();
        entityPropertyType.setId(id);
        entityPropertyType.setMandatory(isMandatory);
        entityPropertyType.setRegistrationDate(registrationDate);
        entityPropertyType.setRegistrator(registrator);
        entityPropertyType.setPropertyType(propertyType);
        addEntityPropertyType(entityPropertyType);
        return entityPropertyType;
    }

}
