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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.InternalNamespace;

/**
 * <i>Persistent Entity</i> object representing relationship type.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.RELATIONSHIP_TYPES_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.DATABASE_INSTANCE_COLUMN }) })
public class RelationshipTypePE extends HibernateAbstractRegistrationHolder implements
        IIdAndCodeHolder
{
    private static final long serialVersionUID = IServer.VERSION;

    private String simpleCode;

    private String description;

    private String label;

    private boolean internalNamespace;

    private boolean managedInternally;

    private transient Long id;

    private DatabaseInstancePE databaseInstance;

    private String parentLabel;

    private String childLabel;

    /**
     * Sets code in 'database format' - without 'user prefix'. To set full code (with user prefix
     * use {@link #setCode(String)}).
     */
    public void setSimpleCode(final String simpleCode)
    {
        this.simpleCode = simpleCode.toUpperCase();
    }

    @Column(name = ColumnNames.CODE_COLUMN)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getSimpleCode()
    {
        return simpleCode;
    }

    public void setCode(final String fullCode)
    {
        setInternalNamespace(CodeConverter.isInternalNamespace(fullCode));
        setSimpleCode(CodeConverter.tryToDatabase(fullCode));
    }

    @Transient
    public String getCode()
    {
        return CodeConverter.tryToBusinessLayer(getSimpleCode(), isInternalNamespace());
    }

    @NotNull
    @Column(name = ColumnNames.IS_INTERNAL_NAMESPACE)
    @InternalNamespace(message = ValidationMessages.CODE_IN_INTERNAL_NAMESPACE)
    public boolean isInternalNamespace()
    {
        return internalNamespace;
    }

    public void setInternalNamespace(final boolean internalNamespace)
    {
        this.internalNamespace = internalNamespace;
    }

    @NotNull(message = ValidationMessages.DESCRIPTION_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    @NotNull(message = ValidationMessages.LABEL_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.LABEL_COLUMN)
    @Length(max = GenericConstants.COLUMN_LABEL, message = ValidationMessages.LABEL_LENGTH_MESSAGE)
    public String getLabel()
    {
        return label;
    }

    public void setLabel(final String label)
    {
        this.label = label;
    }

    @NotNull
    @Column(name = ColumnNames.IS_MANAGED_INTERNALLY)
    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    public void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    @SequenceGenerator(name = SequenceNames.RELATIONSHIP_TYPE_SEQUENCE, sequenceName = SequenceNames.RELATIONSHIP_TYPE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.RELATIONSHIP_TYPE_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    @NotNull(message = ValidationMessages.DATABASE_INSTANCE_NOT_NULL_MESSAGE)
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

    @NotNull(message = ValidationMessages.LABEL_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.PARENT_LABEL_COLUMN)
    @Length(max = GenericConstants.COLUMN_LABEL, message = ValidationMessages.LABEL_LENGTH_MESSAGE)
    public String getParentLabel()
    {
        return parentLabel;
    }

    public void setParentLabel(final String parentLabel)
    {
        this.parentLabel = parentLabel;
    }

    @NotNull(message = ValidationMessages.LABEL_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.CHILD_LABEL_COLUMN)
    @Length(max = GenericConstants.COLUMN_LABEL, message = ValidationMessages.LABEL_LENGTH_MESSAGE)
    public String getChildLabel()
    {
        return childLabel;
    }

    public void setChildLabel(final String childLabel)
    {
        this.childLabel = childLabel;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof RelationshipTypePE == false)
        {
            return false;
        }
        final RelationshipTypePE that = (RelationshipTypePE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getSimpleCode(), that.getSimpleCode());
        builder.append(getDatabaseInstance(), that.getDatabaseInstance());
        builder.append(isInternalNamespace(), that.isInternalNamespace());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSimpleCode());
        builder.append(getDatabaseInstance());
        builder.append(isInternalNamespace());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return getCode();
    }

}
