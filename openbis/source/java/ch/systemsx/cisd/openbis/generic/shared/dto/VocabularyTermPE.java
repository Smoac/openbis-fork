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
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Persistence entity representing vocabulary term.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.CONTROLLED_VOCABULARY_TERM_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.CONTROLLED_VOCABULARY_COLUMN }) })
public class VocabularyTermPE extends HibernateAbstractRegistrationHolder implements
        IIdAndCodeHolder, Comparable<VocabularyTermPE>, Serializable
{

    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final List<VocabularyTermPE> EMPTY_LIST = Collections.emptyList();

    public static final VocabularyTermPE[] EMPTY_ARRAY = new VocabularyTermPE[0];

    private transient Long id;

    private String code;

    private VocabularyPE vocabulary;

    public VocabularyTermPE()
    {
    }

    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getCode()
    {
        return code;
    }

    public void setCode(final String code)
    {
        this.code = CodeConverter.tryToDatabase(code);
    }

    public void setId(final long id)
    {
        this.id = id;

    }

    @SequenceGenerator(name = SequenceNames.CONTROLLED_VOCABULARY_TERM_SEQUENCE, sequenceName = SequenceNames.CONTROLLED_VOCABULARY_TERM_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.CONTROLLED_VOCABULARY_TERM_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    @NotNull(message = ValidationMessages.VOCABULARY_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.CONTROLLED_VOCABULARY_COLUMN, updatable = false)
    public VocabularyPE getVocabulary()
    {
        return vocabulary;
    }

    public void setVocabulary(final VocabularyPE vocabulary)
    {
        this.vocabulary = vocabulary;
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
        if (obj instanceof VocabularyTermPE == false)
        {
            return false;
        }
        final VocabularyTermPE that = (VocabularyTermPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        return builder.toString();
    }

    //
    // Comparable
    //

    public final int compareTo(final VocabularyTermPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

}
