/*
 * Copyright 2009 ETH Zuerich, CISD
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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Persistence Entity describing the filter.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.FILTERS_TABLE)
public class FilterPE extends HibernateAbstractRegistrationHolder implements IIdHolder,
        Comparable<FilterPE>, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String gridId;

    private String name;

    private String expression;

    private boolean isPublic;

    private Date modificationDate;

    private String description;

    private DatabaseInstancePE databaseInstance;

    private Long id;

    @Column(name = ColumnNames.NAME_COLUMN)
    @NotNull(message = ValidationMessages.NAME_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 100, message = ValidationMessages.NAME_LENGTH_MESSAGE)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Column(name = ColumnNames.EXPRESSION_COLUMN)
    @NotNull(message = ValidationMessages.EXPRESSION_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 2000, message = ValidationMessages.EXPRESSION_LENGTH_MESSAGE)
    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    @Column(name = ColumnNames.IS_PUBLIC)
    public boolean isPublic()
    {
        return isPublic;
    }

    public void setPublic(boolean isPublic)
    {
        this.isPublic = isPublic;
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

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_1000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
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

    @SequenceGenerator(name = SequenceNames.FILTER_SEQUENCE, sequenceName = SequenceNames.FILTER_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.FILTER_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @Column(name = ColumnNames.GRID_ID_COLUMN)
    @NotNull(message = ValidationMessages.GRID_ID_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 200, message = ValidationMessages.GRID_ID_LENGTH_MESSAGE)
    public String getGridId()
    {
        return gridId;
    }

    public void setGridId(String gridId)
    {
        this.gridId = gridId;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof FilterPE == false)
        {
            return false;
        }
        final FilterPE that = (FilterPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getName(), that.getName());
        builder.append(getGridId(), that.getGridId());
        builder.append(getDatabaseInstance(), that.getDatabaseInstance());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getName());
        builder.append(getGridId());
        builder.append(getDatabaseInstance());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("name", getName());
        builder.append("grid", getGridId());
        builder.append("database", getDatabaseInstance());
        return builder.toString();
    }

    public int compareTo(FilterPE that)
    {
        final String thatName = that.getName();
        final String thisName = getName();
        if (thisName == null)
        {
            return thatName == null ? 0 : -1;
        }
        if (thatName == null)
        {
            return 1;
        }
        return thisName.compareTo(thatName);
    }
}
