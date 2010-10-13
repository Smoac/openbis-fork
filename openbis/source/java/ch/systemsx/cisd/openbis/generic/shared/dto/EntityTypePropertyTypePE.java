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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
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
        implements IEntityTypePropertyType, Comparable<EntityTypePropertyTypePE>
{
    private static final long serialVersionUID = IServer.VERSION;

    private boolean mandatory;

    private boolean managedInternally;

    private Long ordinal;

    private String section;

    protected transient Long id;

    protected EntityTypePE entityType;

    protected Set<EntityPropertyPE> propertyValues;

    private PropertyTypePE propertyType;

    private ScriptPE script;

    private boolean dynamic;

    final public static <T extends EntityTypePropertyTypePE> T createEntityTypePropertyType(
            final EntityKind entityKind)
    {
        return ClassUtils.createInstance(entityKind.<T> getEntityTypePropertyTypeAssignmentClass());
    }

    @NotNull(message = ValidationMessages.PROPERTY_TYPE_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PROPERTY_TYPE_COLUMN, updatable = false)
    private PropertyTypePE getPropertyTypeInternal()
    {
        return propertyType;
    }

    @Transient
    public PropertyTypePE getPropertyType()
    {
        return getPropertyTypeInternal();
    }

    void setPropertyTypeInternal(final PropertyTypePE propertyType)
    {
        this.propertyType = propertyType;
    }

    abstract public void setPropertyType(final PropertyTypePE propertyType);

    @SuppressWarnings("unused")
    // for Hibernate only
    private void setPropertyValues(final Set<EntityPropertyPE> propertyValues)
    {
        this.propertyValues = propertyValues;
    }

    @NotNull
    @Column(name = ColumnNames.IS_DYNAMIC)
    public boolean isDynamic()
    {
        return dynamic;
    }

    public void setDynamic(final boolean dynamic)
    {
        this.dynamic = dynamic;
    }

    public void setScript(final ScriptPE script)
    {
        this.script = script;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.SCRIPT_ID_COLUMN)
    public ScriptPE getScript()
    {
        return script;
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

    @Column(name = ColumnNames.ORDINAL_COLUMN)
    @NotNull(message = ValidationMessages.ORDINAL_NOT_NULL_MESSAGE)
    public Long getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(Long ordinal)
    {
        this.ordinal = ordinal;
    }

    @Column(name = ColumnNames.SECTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.SECTION_LENGTH_MESSAGE)
    public String getSection()
    {
        return section;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    // needed by Hibernate, must match the mapped getter name
    void setEntityTypeInternal(final EntityTypePE entityType)
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
        builder.append("ordinal", getOrdinal());
        builder.append("section", getSection());
        builder.append("dynamic", isDynamic());
        return builder.toString();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EntityTypePropertyTypePE == false)
        {
            return false;
        }
        final EntityTypePropertyTypePE that = (EntityTypePropertyTypePE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getPropertyType(), that.getPropertyType());
        builder.append(getEntityType(), that.getEntityType());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getPropertyType());
        builder.append(getEntityType());
        return builder.toHashCode();
    }

    //
    // Comparable
    //

    public int compareTo(EntityTypePropertyTypePE o)
    {
        return this.getOrdinal().compareTo(o.getOrdinal());
    }

}
