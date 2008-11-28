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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Kind of <i>Java Bean</i> or <i>Value Object</i> which contains any information we would like to
 * know about one material batch.
 * <p>
 * This class is the <i>Java Object</i> representation of the corresponding data in the database.
 * </p>
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.MATERIAL_BATCHES_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.MATERIAL_COLUMN }) })
public final class MaterialBatchPE extends HibernateAbstractRegistrationHolder implements
        IIdAndCodeHolder, Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private transient Long id;

    private String code;

    private Double amount;

    private Set<SamplePE> samples = new HashSet<SamplePE>();

    private MaterialPE material;

    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    /** Returns <code>code</code>. */
    public final String getCode()
    {
        return code;
    }

    /** Sets <code>code</code>. */
    public final void setCode(final String code)
    {
        this.code = code;
    }

    @Column(name = ColumnNames.AMOUNT_COLUMN)
    public final Double getAmount()
    {
        return amount;
    }

    public final void setAmount(final Double ammount)
    {
        this.amount = ammount;
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = TableNames.SAMPLE_MATERIAL_BATCHES_TABLE, joinColumns = @JoinColumn(name = ColumnNames.MATERIAL_BATCH_COLUMN), inverseJoinColumns = @JoinColumn(name = ColumnNames.SAMPLE_COLUMN))
    public Set<SamplePE> getSamples()
    {
        return samples;
    }

    public void setSamples(final Set<SamplePE> samples)
    {
        this.samples = samples;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = ValidationMessages.MATERIAL_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.MATERIAL_COLUMN, updatable = false)
    public final MaterialPE getMaterial()
    {
        return material;
    }

    public final void setMaterial(final MaterialPE material)
    {
        this.material = material;
    }

    @SequenceGenerator(name = SequenceNames.MATERIAL_BATCH_SEQUENCE, sequenceName = SequenceNames.MATERIAL_BATCH_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.MATERIAL_BATCH_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public final void setId(final Long id)
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
        builder.append("code", getCode());
        builder.append("amount", getAmount());
        builder.append("material", getMaterial());
        return builder.toString();
    }
}