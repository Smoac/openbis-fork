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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * <i>Persistent Entity</i> object of an entity 'procedure'.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.PROCEDURES_TABLE)
@Indexed
public class ProcedurePE implements IIdHolder, Serializable
{
    public final static ProcedurePE[] EMPTY_ARRAY = new ProcedurePE[0];

    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private ProcedureTypePE procedureType;

    private Date registrationDate;

    private transient Long id;

    private ExperimentPE experiment;

    private SamplePE[] resultSamples = SamplePE.EMPTY_ARRAY;

    // TODO 2008-09-10, Tomasz Pylak: this is a deprecated field for control layouts connected
    // directly to procedures. The workflow which used it has been deleted. We can get rid of this
    // field if we write a migration script which connection all the experiment samples to the
    // control layout directly.
    private SamplePE[] inputSamples = SamplePE.EMPTY_ARRAY;

    private Set<DataPE> data = new HashSet<DataPE>();

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.PROPERTY_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.PROCEDURE_TYPE_COLUMN, updatable = false)
    public ProcedureTypePE getProcedureType()
    {
        return procedureType;
    }

    public void setProcedureType(final ProcedureTypePE procedureType)
    {
        this.procedureType = procedureType;
    }

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    public Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    //
    // IIdHolder
    //

    @SequenceGenerator(name = SequenceNames.PROCEDURE_SEQUENCE, sequenceName = SequenceNames.PROCEDURE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.PROCEDURE_SEQUENCE)
    @DocumentId
    public final Long getId()
    {
        return id;
    }

    public void setExperiment(final ExperimentPE experiment)
    {
        if (experiment != null)
        {
            experiment.addProcedure(this);
        }
    }

    @Transient
    public ExperimentPE getExperiment()
    {
        return getExperimentInternal();
    }

    void setExperimentInternal(final ExperimentPE experiment)
    {
        this.experiment = experiment;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = ValidationMessages.EXPERIMENT_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN, updatable = false)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_EXPERIMENT)
    private ExperimentPE getExperimentInternal()
    {
        return experiment;
    }

    @Transient
    public SamplePE[] getInputSamples()
    {
        return inputSamples;
    }

    @Transient
    public SamplePE[] getResultSamples()
    {
        return resultSamples;
    }

    public void setResultSamples(final SamplePE[] resultSamples)
    {
        this.resultSamples = resultSamples;
    }

    public void setInputSamples(final SamplePE[] inputSamples)
    {
        this.inputSamples = inputSamples;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "procedure")
    @JoinColumn(name = ColumnNames.PROCEDURE_PRODUCED_BY_COLUMN, updatable = true)
    @ContainedIn
    public Set<DataPE> getData()
    {
        return data;
    }

    public void setData(final Set<DataPE> data)
    {
        this.data = data;
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
        builder.append("procedureType", getProcedureType());
        builder.append("experiment", getExperiment());
        return builder.toString();
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ProcedurePE == false)
        {
            return false;
        }
        final ProcedurePE that = (ProcedurePE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getProcedureType(), that.getProcedureType());
        builder.append(getExperiment(), that.getExperiment());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getProcedureType());
        builder.append(getExperiment());
        return builder.toHashCode();
    }
}
