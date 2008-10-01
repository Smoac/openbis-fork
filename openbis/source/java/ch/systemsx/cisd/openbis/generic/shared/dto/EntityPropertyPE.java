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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;

import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Persistence entity representing entity property.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
@MappedSuperclass
public abstract class EntityPropertyPE extends HibernateAbstractRegistratrationHolder implements
        IRegistratorHolder, IUntypedValueSetter, IEntityProperty
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

    protected IIdAndCodeHolder entity;

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

    public void setEntity(final IIdAndCodeHolder entity)
    {
        this.entity = entity;
    }

    @Column(name = ColumnNames.VALUE)
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
    @JoinColumn(name = ColumnNames.VOCABULARY_TERM_COLUMN, updatable = false)
    public VocabularyTermPE getVocabularyTerm()
    {
        return vocabularyTerm;
    }

    //
    // IUntypedValueSetter
    //

    // TODO 2008-08-29, Christian Ribeaud: How to validate this? A custom validator? It must be a
    // 'VocabularyTermPE' and not just an id.
    public final void setUntypedValue(final String value, final Long vocabularyTerm)
    {
        assert value != null || vocabularyTerm == null : "Value from controlled vocabulary set cannot be null!";
        if (vocabularyTerm != null)
        {
            if (getVocabularyTerm() == null)
            {
                final VocabularyTermPE vt = new VocabularyTermPE();
                setVocabularyTerm(vt);
            }
            getVocabularyTerm().setId(vocabularyTerm);
            getVocabularyTerm().setCode(value);
        } else
        {
            setValue(value);
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
        builder.append("value", getValue());
        builder.append("vocabularyTerm", getVocabularyTerm());
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
}
