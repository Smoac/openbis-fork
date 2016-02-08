/*
 * Copyright 2010 ETH Zuerich, CISD
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Persistent entity representing operation execution.
 * 
 * @author pkupczyk
 */
@Entity
@Table(name = TableNames.OPERATION_EXECUTIONS_TABLE, uniqueConstraints = { @UniqueConstraint(columnNames = { ColumnNames.CODE_COLUMN }) })
public class OperationExecutionPE implements IIdHolder, Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    protected Long id;

    private String code;

    private OperationExecutionState state;

    private String description;

    private String error;

    private Date creationDate;

    private Date startDate;

    private Date finishDate;

    @Override
    @SequenceGenerator(name = SequenceNames.OPERATION_EXECUTIONS_SEQUENCE, sequenceName = SequenceNames.OPERATION_EXECUTIONS_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.OPERATION_EXECUTIONS_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @NotNull(message = ValidationMessages.STATE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.STATE)
    @Enumerated(EnumType.STRING)
    public OperationExecutionState getState()
    {
        return state;
    }

    public void setState(OperationExecutionState state)
    {
        this.state = state;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Column(name = ColumnNames.ERROR_COLUMN)
    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    @Column(name = ColumnNames.CREATION_DATE_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    public Date getCreationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(creationDate);
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    @Column(name = ColumnNames.START_DATE_COLUMN)
    public Date getStartDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(startDate);
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    @Column(name = ColumnNames.FINISH_DATE_COLUMN)
    public Date getFinishDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(finishDate);
    }

    public void setFinishDate(Date finishDate)
    {
        this.finishDate = finishDate;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof OperationExecutionPE == false)
        {
            return false;
        }

        final OperationExecutionPE that = (OperationExecutionPE) obj;
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
        return getCode();
    }

}
