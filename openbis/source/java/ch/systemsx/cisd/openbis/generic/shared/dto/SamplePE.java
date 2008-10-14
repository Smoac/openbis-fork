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

import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * <i>Persistent Entity</i> object of an entity 'sample'.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.SAMPLES_TABLE)
@Check(constraints = "(" + ColumnNames.DATABASE_INSTANCE_COLUMN + " IS NOT NULL AND "
        + ColumnNames.GROUP_COLUMN + " IS NULL) OR (" + ColumnNames.DATABASE_INSTANCE_COLUMN
        + " IS NULL AND " + ColumnNames.GROUP_COLUMN + " IS NOT NULL)")
public class SamplePE extends HibernateAbstractRegistrationHolder implements IIdAndCodeHolder,
        Comparable<SamplePE>, IEntityPropertiesHolder<SamplePropertyPE>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final SamplePE[] EMPTY_ARRAY = new SamplePE[0];

    public static final List<SamplePE> EMPTY_LIST = Collections.emptyList();

    private Long id;

    private String code;

    private SampleTypePE sampleType;

    private DatabaseInstancePE databaseInstance;

    private GroupPE group;

    private SampleIdentifier sampleIdentifier;

    private SamplePE controlLayout;

    private SamplePE container;

    private SamplePE top;

    private SamplePE generatedFrom;

    private List<ProcedurePE> procedures = ProcedurePE.EMPTY_LIST;

    private ProcedurePE validProcedure;

    private ExternalDataPE[] measurementData = ExternalDataPE.EMPTY_ARRAY;

    private ExternalDataPE[] derivedData = ExternalDataPE.EMPTY_ARRAY;

    /**
     * Invalidation information.
     * <p>
     * If not <code>null</code>, then this sample is considered as <i>invalid</i>.
     * </p>
     */
    private InvalidationPE invalidation;

    private List<SamplePropertyPE> properties = SamplePropertyPE.EMPTY_LIST;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.INVALIDATION_COLUMN)
    public InvalidationPE getInvalidation()
    {
        return invalidation;
    }

    public void setInvalidation(final InvalidationPE invalidation)
    {
        this.invalidation = invalidation;
    }

    @Transient
    public SampleIdentifier getSampleIdentifier()
    {
        if (sampleIdentifier == null)
        {
            sampleIdentifier = IdentifierHelper.createIdentifier(this);
        }
        return sampleIdentifier;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATABASE_INSTANCE_COLUMN, updatable = false)
    public DatabaseInstancePE getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.GROUP_COLUMN, updatable = false)
    public GroupPE getGroup()
    {
        return group;
    }

    public void setGroup(final GroupPE group)
    {
        this.group = group;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.SAMPLE_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.SAMPLE_TYPE_COLUMN, updatable = false)
    public SampleTypePE getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(final SampleTypePE sampleType)
    {
        this.sampleType = sampleType;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity")
    @Fetch(FetchMode.SUBSELECT)
    private List<SamplePropertyPE> getSampleProperties()
    {
        return properties;
    }

    private void setSampleProperties(final List<SamplePropertyPE> properties)
    {
        this.properties = properties;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.CONTROL_LAYOUT_SAMPLE_COLUMN, updatable = false)
    public SamplePE getControlLayout()
    {
        return controlLayout;
    }

    public void setControlLayout(final SamplePE controlLayout)
    {
        this.controlLayout = controlLayout;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PART_OF_SAMPLE_COLUMN, updatable = false)
    public SamplePE getContainer()
    {
        return container;
    }

    public void setContainer(final SamplePE container)
    {
        this.container = container;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.TOP_SAMPLE_COLUMN)
    public SamplePE getTop()
    {
        return top;
    }

    public void setTop(final SamplePE top)
    {
        this.top = top;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.GENERATED_FROM_SAMPLE_COLUMN)
    public SamplePE getGeneratedFrom()
    {
        return generatedFrom;
    }

    public void setGeneratedFrom(final SamplePE generatedFrom)
    {
        this.generatedFrom = generatedFrom;
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = TableNames.SAMPLE_INPUTS_TABLE, joinColumns = @JoinColumn(name = ColumnNames.SAMPLE_COLUMN), inverseJoinColumns = @JoinColumn(name = ColumnNames.PROCEDURE_COLUMN))
    public List<ProcedurePE> getProcedures()
    {
        return procedures;
    }

    public void setProcedures(final List<ProcedurePE> procedures)
    {
        this.procedures = procedures;
    }

    @Transient
    public ProcedurePE getValidProcedure()
    {
        return validProcedure;
    }

    public void setValidProcedure(final ProcedurePE validProcedure)
    {
        this.validProcedure = validProcedure;
    }

    @Transient
    public ExternalDataPE[] getMeasurementData()
    {
        return measurementData;
    }

    public void setMeasurementData(final ExternalDataPE[] measurementData)
    {
        this.measurementData = measurementData;
    }

    @Transient
    public ExternalDataPE[] getDerivedData()
    {
        return derivedData;
    }

    public void setDerivedData(final ExternalDataPE[] derivedData)
    {
        this.derivedData = derivedData;
    }

    //
    // IIdAndCodeHolder
    //

    @SequenceGenerator(name = SequenceNames.SAMPLE_SEQUENCE, sequenceName = SequenceNames.SAMPLE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SAMPLE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getCode()
    {
        return code;
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
        if (obj instanceof SamplePE == false)
        {
            return false;
        }
        final SamplePE that = (SamplePE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getSampleType(), that.getSampleType());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getSampleType());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        builder.append("sampleType", getSampleType());
        return builder.toString();
    }

    //
    // Compare
    //

    public final int compareTo(final SamplePE o)
    {
        return getSampleIdentifier().compareTo(o.getSampleIdentifier());
    }

    //
    // IEntityPropertiesHolder
    //

    @Transient
    public List<SamplePropertyPE> getProperties()
    {
        return getSampleProperties();
    }

    public void setProperties(final List<SamplePropertyPE> properties)
    {
        for (final SamplePropertyPE sampleProperty : properties)
        {
            final SamplePE parent = sampleProperty.getSample();
            if (parent != null)
            {
                parent.getProperties().remove(sampleProperty);
            }
            sampleProperty.setEntity(this);
        }
        setSampleProperties(properties);
    }
}
