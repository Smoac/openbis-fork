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

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.common.collections.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A <i>Persistent Entity</i> which is a material.
 * 
 * @author Franz-Josef Elmer
 */
@Entity
@Table(name = TableNames.MATERIALS_TABLE, uniqueConstraints = @UniqueConstraint(columnNames =
    { ColumnNames.CODE_COLUMN, ColumnNames.MATERIAL_TYPE_COLUMN,
            ColumnNames.DATABASE_INSTANCE_COLUMN }))
@Indexed(index = "MaterialPE")
public class MaterialPE implements IIdAndCodeHolder, Comparable<MaterialPE>,
        IEntityInformationWithPropertiesHolder, Serializable, IMatchingEntity
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final MaterialPE[] EMPTY_ARRAY = new MaterialPE[0];

    private transient Long id;

    private MaterialTypePE materialType;

    private String code;

    private DatabaseInstancePE databaseInstance;

    private Set<MaterialPropertyPE> properties = new HashSet<MaterialPropertyPE>();

    /**
     * NOTE: Materials do not have permanent ids stored in the database.
     * 
     * @return a material permanent id for the given code and typeCode
     */
    public static String createPermId(String code, String materialTypeCode)
    {
        return code + " (" + materialTypeCode + ")";
    }

    public MaterialPE()
    {
    }

    /**
     * Person who registered this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private PersonPE registrator;

    /**
     * Registration date of this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private Date registrationDate;

    private Date modificationDate;

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    @Field(name = SearchFieldConstants.REGISTRATION_DATE, index = Index.UN_TOKENIZED, store = Store.NO)
    @DateBridge(resolution = Resolution.SECOND)
    public Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @Override
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_REGISTERER_COLUMN, updatable = false)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_REGISTRATOR)
    public PersonPE getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
    }

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.MATERIAL_SEQUENCE, sequenceName = SequenceNames.MATERIAL_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.MATERIAL_SEQUENCE)
    @DocumentId(name = SearchFieldConstants.ID)
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
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_ENTITY_TYPE)
    public MaterialTypePE getMaterialType()
    {
        return materialType;
    }

    public void setMaterialType(final MaterialTypePE materialType)
    {
        this.materialType = materialType;
    }

    @Override
    @Column(name = ColumnNames.CODE_COLUMN)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Field(index = Index.TOKENIZED, store = Store.YES, name = SearchFieldConstants.CODE)
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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity", orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_PROPERTIES)
    @Fetch(FetchMode.SUBSELECT)
    private Set<MaterialPropertyPE> getMaterialProperties()
    {
        return properties;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setMaterialProperties(final Set<MaterialPropertyPE> properties)
    {
        this.properties = properties;
    }

    //
    // IEntityPropertiesHolder
    //

    @Override
    @Transient
    public Set<MaterialPropertyPE> getProperties()
    {

        return new UnmodifiableSetDecorator<MaterialPropertyPE>(getMaterialProperties());
    }

    /**
     * Returns <code>true</code>, if and only if the properties have been initialized.
     */
    @Override
    @Transient
    public boolean isPropertiesInitialized()
    {
        return HibernateUtils.isInitialized(getMaterialProperties());
    }

    @Override
    public void setProperties(final Set<? extends EntityPropertyPE> properties)
    {
        getMaterialProperties().clear();
        for (final EntityPropertyPE untypedProperty : properties)
        {
            MaterialPropertyPE materialProperty = (MaterialPropertyPE) untypedProperty;
            final MaterialPE parent = materialProperty.getEntity();
            if (parent != null)
            {
                parent.getMaterialProperties().remove(materialProperty);
            }
            addProperty(materialProperty);
        }
    }

    @Override
    public void addProperty(final EntityPropertyPE property)
    {
        property.setEntity(this);
        getMaterialProperties().add((MaterialPropertyPE) property);
    }

    @Override
    public void removeProperty(final EntityPropertyPE property)
    {
        getMaterialProperties().remove(property);
        property.setEntity(null);
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getCode(), "code");
        EqualsHashUtils.assertDefined(getMaterialType(), "type");
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

    @Version
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    @Field(name = SearchFieldConstants.MODIFICATION_DATE, index = Index.UN_TOKENIZED, store = Store.NO)
    @DateBridge(resolution = Resolution.SECOND)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final MaterialPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

    //
    // IMatchingEntity
    //

    @Transient
    @Field(index = Index.NO, store = Store.YES, name = SearchFieldConstants.IDENTIFIER)
    public final String getIdentifier()
    {
        return getCode();
    }

    @Override
    @Transient
    public final EntityTypePE getEntityType()
    {
        return getMaterialType();
    }

    @Override
    @Transient
    public final EntityKind getEntityKind()
    {
        return EntityKind.MATERIAL;
    }

    @Override
    @Transient
    public String getPermId()
    {
        return createPermId(code, materialType.getCode());
    }

}
