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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Persistence entity representing sample property.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.SAMPLE_PROPERTIES_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.SAMPLE_COLUMN, ColumnNames.SAMPLE_TYPE_PROPERTY_TYPE_COLUMN }) })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SamplePropertyPE extends EntityPropertyPE
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final SamplePropertyPE[] EMPTY_ARRAY = new SamplePropertyPE[0];

    private SamplePE sample;

    /**
     * Returns the sample that this property belongs to.
     */
    @Transient
    public final SamplePE getSample()
    {
        return sample;
    }

    //
    // EntityPropertyPE
    //

    @NotNull(message = ValidationMessages.SAMPLE_TYPE_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = SampleTypePropertyTypePE.class)
    @JoinColumn(name = ColumnNames.SAMPLE_TYPE_PROPERTY_TYPE_COLUMN, updatable = false)
    public EntityTypePropertyTypePE getEntityTypePropertyType()
    {
        return entityTypePropertyType;
    }

    @SequenceGenerator(name = SequenceNames.SAMPLE_PROPERTY_SEQUENCE, sequenceName = SequenceNames.SAMPLE_PROPERTY_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SAMPLE_PROPERTY_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = SamplePE.class)
    @JoinColumn(name = ColumnNames.SAMPLE_COLUMN, updatable = false)
    public IIdAndCodeHolder getEntity()
    {
        return getSample();
    }

    /**
     * Sets the <var>sample</var> of this property.
     * <p>
     * <i>Do not use directly, instead, call {@link SamplePE#addProperty(SamplePropertyPE)} with
     * <code>this</code> object!</i>
     */
    void setEntity(final IIdAndCodeHolder entity)
    {
        this.sample = (SamplePE) entity;
    }
}
