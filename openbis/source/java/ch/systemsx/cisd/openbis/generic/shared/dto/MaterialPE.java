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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * A <i>Persistent Entity</i> which is a material.
 * 
 * @author Franz-Josef Elmer
 */
@Entity
@Table(name = TableNames.MATERIALS_TABLE, uniqueConstraints = @UniqueConstraint(columnNames =
    { ColumnNames.CODE_COLUMN, ColumnNames.MATERIAL_TYPE_COLUMN,
            ColumnNames.DATABASE_INSTANCE_COLUMN }))
public class MaterialPE extends HibernateAbstractRegistratrationHolder implements IIdAndCodeHolder,
        Comparable<MaterialPE>, IEntityPropertiesHolder<MaterialPropertyPE>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private transient Long id;

    private MaterialTypePE materialType;

    private String code;

    private DatabaseInstancePE databaseInstance;

    private List<MaterialPropertyPE> properties = MaterialPropertyPE.EMPTY_LIST;

    public static final MaterialPE[] EMPTY_ARRAY = new MaterialPE[0];

    private MaterialPE inhibitorOf;

    public MaterialPE()
    {
    }

    @Id
    @SequenceGenerator(name = SequenceNames.MATERIAL_SEQUENCE, sequenceName = SequenceNames.MATERIAL_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.MATERIAL_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.MATERIAL_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.MATERIAL_TYPE_COLUMN, updatable = false)
    public MaterialTypePE getMaterialType()
    {
        return materialType;
    }

    public void setMaterialType(final MaterialTypePE materialType)
    {
        this.materialType = materialType;
    }

    @Column(name = ColumnNames.CODE_COLUMN)
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getCode()
    {
        return code;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.DATABASE_INSTANCE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATABASE_INSTANCE_COLUMN, updatable = false)
    public DatabaseInstancePE getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.INHIBITOR_OF_COLUMN)
    public MaterialPE getInhibitorOf()
    {
        return inhibitorOf;
    }

    public void setInhibitorOf(final MaterialPE inhibitorOf)
    {
        this.inhibitorOf = inhibitorOf;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<MaterialPropertyPE> getMaterialProperties()
    {
        return properties;
    }

    private void setMaterialProperties(final List<MaterialPropertyPE> properties)
    {
        this.properties = properties;
    }

    //
    // IEntityPropertiesHolder
    //

    @Transient
    public List<MaterialPropertyPE> getProperties()
    {
        return getMaterialProperties();
    }

    public void setProperties(final List<MaterialPropertyPE> properties)
    {
        for (final MaterialPropertyPE materialProperty : properties)
        {
            materialProperty.setEntity(this);
        }
        setMaterialProperties(properties);
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof MaterialPE == false)
        {
            return false;
        }
        final MaterialPE that = (MaterialPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getMaterialType(), that.getMaterialType());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getMaterialType());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        builder.append("materialType", getMaterialType());
        return builder.toString();
    }

    //
    // Comparable
    //

    public final int compareTo(final MaterialPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

}
