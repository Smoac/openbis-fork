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
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.validator.Length;

import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Persistence entity representing entity property.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
@MappedSuperclass
@ClassBridge(index = Index.TOKENIZED, store = Store.YES, impl = EntityPropertyPE.EntityPropertySearchBridge.class)
public abstract class EntityPropertyPE extends HibernateAbstractRegistrationHolder implements
        IUntypedValueSetter, IEntityProperty
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

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

    protected transient Long id;

    protected EntityTypePropertyTypePE entityTypePropertyType;

    /**
     * This bridge allows to save in the search index not only the value of property, but also the
     * corresponding property code.
     */
    public static class EntityPropertySearchBridge implements FieldBridge
    {

        private static final String PROPERTY_FIELD_PREFIX = "property: ";

        public void set(String name, Object/* EntityPropertyPE */value,
                Document/* Lucene document */document,
                org.apache.lucene.document.Field.Store store,
                org.apache.lucene.document.Field.Index index, Float boost)
        {
            EntityPropertyPE entityProperty = (EntityPropertyPE) value;
            String fieldValue = entityProperty.tryGetUntypedValue();
            String fieldName = PROPERTY_FIELD_PREFIX + getPropertyFieldName(entityProperty);
            Field field = new Field(fieldName, fieldValue, store, index);
            if (boost != null)
            {
                field.setBoost(boost);
            }
            document.add(field);
        }

        private String getPropertyFieldName(EntityPropertyPE entityProperty)
        {
            return entityProperty.getEntityTypePropertyType().getPropertyType().getLabel();
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

    /**
     * Sets the entity that holds this property. Needs to be of the correct sub-class of
     * {@link IIdAndCodeHolder}.
     * <p>
     * <i>Note: Consider using the <code>addProperty()</code> method of the holder instead of
     * using this method!</i>
     */
    abstract public void setHolder(final IIdAndCodeHolder entity);

    @Column(name = ColumnNames.VALUE_COLUMN)
    @Length(max = 1024, message = ValidationMessages.VALUE_LENGTH_MESSAGE)
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
    public VocabularyTermPE getVocabularyTerm()
    {
        return vocabularyTerm;
    }

    //
    // IUntypedValueSetter
    //

    public final void setUntypedValue(final String valueOrNull,
            final VocabularyTermPE vocabularyTermOrNull)
    {
        assert valueOrNull != null || vocabularyTermOrNull != null : "Either value or vocabulary term should not be null.";
        if (vocabularyTermOrNull != null)
        {
            setVocabularyTerm(vocabularyTermOrNull);
        } else
        {
            setValue(valueOrNull);
        }
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

    public final String tryGetUntypedValue()
    {
        return getVocabularyTerm() == null ? getValue() : getVocabularyTerm().getCode();
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
        EqualsHashUtils.assertDefined(getEntity(), "entity");
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
