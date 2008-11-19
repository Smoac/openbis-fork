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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.common.collections.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Persistence entity representing controlled vocabulary.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.CONTROLLED_VOCABULARY_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.IS_INTERNAL_NAMESPACE,
                ColumnNames.DATABASE_INSTANCE_COLUMN }) })
public class VocabularyPE extends HibernateAbstractRegistrationHolder implements IIdAndCodeHolder,
        Comparable<VocabularyPE>, Serializable
{

    public final static VocabularyPE[] EMPTY_ARRAY = new VocabularyPE[0];

    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private transient Long id;

    private String simpleCode;

    private String description;

    private Set<VocabularyTermPE> terms = new HashSet<VocabularyTermPE>();

    private boolean managedInternally;

    private boolean internalNamespace;

    private DatabaseInstancePE databaseInstance;

    public VocabularyPE()
    {
    }

    @Column(name = ColumnNames.CODE_COLUMN)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    private String getSimpleCode()
    {
        return simpleCode;
    }

    /**
     * Sets code in 'database format' - without 'user prefix'. To set full code (with user prefix
     * use {@link #setCode(String)}).
     */
    private void setSimpleCode(final String simpleCode)
    {
        this.simpleCode = simpleCode;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = 80, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String descriptionOrNull)
    {
        this.description = descriptionOrNull;
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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "vocabularyInternal")
    private Set<VocabularyTermPE> getVocabularyTerms()
    {
        return terms;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setVocabularyTerms(final Set<VocabularyTermPE> terms)
    {
        this.terms = terms;
    }

    @Transient
    public Set<VocabularyTermPE> getTerms()
    {
        return new UnmodifiableSetDecorator<VocabularyTermPE>(getVocabularyTerms());
    }

    public final void setTerms(final Iterable<VocabularyTermPE> terms)
    {
        getVocabularyTerms().clear();
        for (final VocabularyTermPE child : terms)
        {
            addTerm(child);
        }
    }

    public void addTerm(final VocabularyTermPE child)
    {
        final VocabularyPE parent = child.getVocabulary();
        if (parent != null)
        {
            parent.getVocabularyTerms().remove(child);
        }
        child.setVocabularyInternal(this);
        getVocabularyTerms().add(child);
    }

    @SequenceGenerator(name = SequenceNames.CONTROLLED_VOCABULARY_SEQUENCE, sequenceName = SequenceNames.CONTROLLED_VOCABULARY_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.CONTROLLED_VOCABULARY_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Column(name = ColumnNames.IS_MANAGED_INTERNALLY, nullable = false)
    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    public void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    @Column(name = ColumnNames.IS_INTERNAL_NAMESPACE, nullable = false)
    public boolean isInternalNamespace()
    {
        return internalNamespace;
    }

    public void setInternalNamespace(final boolean internalNamespace)
    {
        this.internalNamespace = internalNamespace;
    }

    public void setId(final long id)
    {
        this.id = id;

    }

    public void setCode(final String fullCode)
    {
        setInternalNamespace(CodeConverter.isInternalNamespace(fullCode));
        setSimpleCode(CodeConverter.tryToDatabase(fullCode));
    }

    @Transient
    public String getCode()
    {
        return CodeConverter.tryToBusinessLayer(getSimpleCode(), isInternalNamespace() == false);
    }

    /**
     * Returns the {@link VocabularyTermPE} of this vocabulary having given code.
     * 
     * @return <code>null</code> if no term with given code could be found.
     */
    public final VocabularyTermPE tryGetVocabularyTerm(final String code)
    {
        assert code != null : "Unspecified code";
        for (final VocabularyTermPE vocabularyTermPE : getTerms())
        {
            if (vocabularyTermPE.getCode().equals(code))
            {
                return vocabularyTermPE;
            }
        }
        return null;
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
        if (obj instanceof VocabularyPE == false)
        {
            return false;
        }
        final VocabularyPE that = (VocabularyPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(isInternalNamespace(), that.isInternalNamespace());
        builder.append(getDatabaseInstance(), that.getDatabaseInstance());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(isInternalNamespace());
        builder.append(getDatabaseInstance());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("simpleCode", getSimpleCode());
        builder.append("internalNamespace", isInternalNamespace());
        builder.append("managedInternally", isManagedInternally());
        return builder.toString();
    }

    //
    // Comparable
    //

    public final int compareTo(final VocabularyPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

}
