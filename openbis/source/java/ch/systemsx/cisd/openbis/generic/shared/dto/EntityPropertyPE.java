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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.builtin.StringEncodingDateBridge;

import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator.SupportedDatePattern;

/**
 * Persistence entity representing entity property.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
@MappedSuperclass
@ClassBridge(index = Index.YES, store = Store.YES, impl = EntityPropertyPE.EntityPropertySearchBridge.class)
public abstract class EntityPropertyPE extends HibernateAbstractRegistrationHolder implements
        IUntypedValueSetter, IEntityPropertyHolder
{
    private static final long serialVersionUID = IServer.VERSION;

    protected IEntityPropertiesHolder entity;

    /**
     * The value of this entity property.
     * <p>
     * Like in the database, no value is set if <code>value</code> is from controlled vocabulary.
     * </p>
     */
    private String value;

    /**
     * The vocabulary term.
     * <p>
     * Not <code>null</code> if <code>value</code> is from controlled vocabulary.
     * </p>
     */
    private VocabularyTermPE vocabularyTerm;

    /**
     * If the property is of MATERIAL, this field is not <code>null</code> and {@link #value} and {@link #vocabularyTerm} fields are set to
     * <code>null</code>.
     */
    private MaterialPE material;

    protected transient Long id;

    protected EntityTypePropertyTypePE entityTypePropertyType;

    /**
     * Person who modified this entity.
     * <p>
     * This is specified at update time.
     * </p>
     */
    private PersonPE author;

    private Date modificationDate;
    
    /**
     * This bridge allows to save in the search index not only the value of property, but also the corresponding property code.
     */
    public static class EntityPropertySearchBridge implements FieldBridge
    {

        private static SimpleDateFormat dateFormat;

        static
        {
            dateFormat = new SimpleDateFormat(SupportedDatePattern.CANONICAL_DATE_PATTERN.getPattern());
        }

        @Override
        public void set(
                String name, 
                Object/* EntityPropertyPE */value,
                Document/* Lucene document */document, 
                LuceneOptions luceneOptions)
        {
            EntityPropertyPE entityProperty = (EntityPropertyPE) value;

            String fieldValue = entityProperty.tryGetUntypedValue();
            String fieldPrefix = name;
            String fieldFullName = fieldPrefix + getPropertyFieldName(entityProperty);

            if (DataTypeCode.TIMESTAMP.equals(entityProperty.getEntityTypePropertyType().getPropertyType().getType().getCode()))
            {
                try
                {
                    Date date = dateFormat.parse(fieldValue);
                    StringEncodingDateBridge dateBridge = new StringEncodingDateBridge(Resolution.SECOND);
                    fieldValue = dateBridge.objectToString(date);
                } catch (ParseException e)
                {
                    // leave the original value
                }
            } else if(DataTypeCode.INTEGER.equals(entityProperty.getEntityTypePropertyType().getPropertyType().getType().getCode()) ||
                    DataTypeCode.REAL.equals(entityProperty.getEntityTypePropertyType().getPropertyType().getType().getCode())) {
                fieldValue = DetailedSearchCriterion.getNumberForLucene(fieldValue);
            }

            Field field = new Field(fieldFullName, fieldValue, luceneOptions.getStore(), luceneOptions.getIndex());
            field.setBoost(luceneOptions.getBoost());
            document.add(field);
        }

        private String getPropertyFieldName(EntityPropertyPE entityProperty)
        {
            PropertyTypePE propertyType =
                    entityProperty.getEntityTypePropertyType().getPropertyType();
            return propertyType.getCode();
        }
    }

    public <T extends EntityTypePropertyTypePE> void setEntityTypePropertyType(
            final T entityTypePropertyType)
    {
        this.entityTypePropertyType = entityTypePropertyType;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    public void setValue(final String value)
    {
        this.value = value;
    }

    @Column(name = ColumnNames.VALUE_COLUMN)
    public String getValue()
    {
        return value;
    }

    public void setVocabularyTerm(final VocabularyTermPE vt)
    {
        this.vocabularyTerm = vt;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.VOCABULARY_TERM_COLUMN)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_VOCABULARY_TERM)
    public VocabularyTermPE getVocabularyTerm()
    {
        return vocabularyTerm;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.MATERIAL_PROP_COLUMN)
    public MaterialPE getMaterialValue()
    {
        return material;
    }

    public void setMaterialValue(MaterialPE material)
    {
        this.material = material;
    }

    //
    // IUntypedValueSetter
    //

    @Override
    public final void setUntypedValue(final String valueOrNull,
            final VocabularyTermPE vocabularyTermOrNull, MaterialPE materialOrNull)
    {
        assert valueOrNull != null || vocabularyTermOrNull != null || materialOrNull != null : "Either value, vocabulary term or material should not be null.";
        setVocabularyTerm(null);
        setMaterialValue(null);
        setValue(null);
        if (vocabularyTermOrNull != null)
        {
            assert materialOrNull == null;
            setVocabularyTerm(vocabularyTermOrNull);
        } else if (materialOrNull != null)
        {
            setMaterialValue(materialOrNull);
        } else
        {
            setValue(valueOrNull);
        }
    }

    @Version
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PERSON_AUTHOR_COLUMN, nullable = false, updatable = true)
    public PersonPE getAuthor()
    {
        return author;
    }

    public void setAuthor(PersonPE author)
    {
        this.author = author;
    }

    /**
     * Sets the <var>entity</var> of this property.
     * <p>
     * <i>Do not use directly, instead, call {@link IEntityPropertiesHolder#addProperty(EntityPropertyPE)} with <code>this</code> object!</i>
     */
    void setEntity(final IEntityPropertiesHolder entity)
    {
        this.entity = entity;
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
        builder.append("entityTypePropertyType", getEntityTypePropertyType());
        builder.append("value", tryGetUntypedValue());
        return builder.toString();
    }

    //
    // IEntityProperty
    //

    @Override
    public final String tryGetUntypedValue()
    {
        if (getVocabularyTerm() != null)
        {
            final String labelOrNull = getVocabularyTerm().getLabel();
            return getVocabularyTerm().getCode()
                    + (labelOrNull != null ? " " + getVocabularyTerm().getLabel() : "");
        } else if (getMaterialValue() != null)
        {
            return createMaterialIdentifier(getMaterialValue()).print();
        } else
        {
            return getValue();
        }
    }

    private static MaterialIdentifier createMaterialIdentifier(MaterialPE material)
    {
        return new MaterialIdentifier(material.getCode(), material.getMaterialType().getCode());
    }

    /**
     * Creates an {@link EntityPropertyPE} from given <var>entityKind</var>.
     */
    public final static <T extends EntityPropertyPE> T createEntityProperty(
            final EntityKind entityKind)
    {
        assert entityKind != null : "Unspecified entity kind";
        return ClassUtils.createInstance(entityKind.<T> getEntityPropertyClass());
    }

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getEntityTypePropertyType(), "etpt");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EntityPropertyPE == false)
        {
            return false;
        }
        final EntityPropertyPE that = (EntityPropertyPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getEntity(), that.getEntity());
        builder.append(getEntityTypePropertyType(), that.getEntityTypePropertyType());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getEntity());
        builder.append(getEntityTypePropertyType());
        return builder.toHashCode();
    }

}
