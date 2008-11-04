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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Relation between a {@link EntityTypePE} of something and a {@link PropertyTypePE}.
 * <p>
 * This represents an entry in <code>{entity}_type_property_types</code> table.
 * </p>
 * 
 * @author Franz-Josef Elmer
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
@MappedSuperclass
public abstract class EntityTypePropertyTypePE extends HibernateAbstractRegistrationHolder
        implements IEntityTypePropertyType
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private boolean mandatory;

    private boolean managedInternally;

    protected transient Long id;

    protected EntityTypePE entityType;

    private PropertyTypePE propertyType;

    final public static <T extends EntityTypePropertyTypePE> T createEntityTypePropertyType(
            final EntityKind entityKind)
    {
        return ClassUtils.createInstance(entityKind.<T> getEntityTypePropertyTypeAssignmentClass());
    }

    @NotNull(message = ValidationMessages.PROPERTY_TYPE_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PROPERTY_TYPE_COLUMN, updatable = false)
    public PropertyTypePE getPropertyType()
    {
        return propertyType;
    }

    public void setPropertyType(final PropertyTypePE propertyType)
    {
        this.propertyType = propertyType;
    }

    @NotNull
    @Column(name = ColumnNames.IS_MANDATORY, updatable = true)
    public boolean isMandatory()
    {
        return mandatory;
    }

    public void setMandatory(final boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    @NotNull
    @Column(name = ColumnNames.IS_MANAGED_INTERNALLY)
    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    public void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    // needed by Hibernate, must match the mapped getter name
    void setEntityTypeInternal(EntityTypePE entityType)
    {
        this.entityType = entityType;
    }

    public void setEntityType(final EntityTypePE entityType)
    {
        setEntityTypeInternal(entityType);
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("managedInternally", isManagedInternally());
        builder.append("mandatory", isMandatory());
        builder.append("propertyType", getPropertyType());
        builder.append("entityType", getEntityType());
        return builder.toString();
    }

    @Override
    public final boolean equals(final Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public final int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
