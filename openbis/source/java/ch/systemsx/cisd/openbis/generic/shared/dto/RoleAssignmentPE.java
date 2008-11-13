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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Check;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Persistent Entity corresponding containing informations about role assignment.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Check(constraints = ColumnNames.DATABASE_INSTANCE_COLUMN + " IS NOT NULL AND "
        + ColumnNames.GROUP_COLUMN + " IS NULL OR " + ColumnNames.DATABASE_INSTANCE_COLUMN
        + " IS NULL AND " + ColumnNames.GROUP_COLUMN + " IS NOT NULL")
@Table(name = TableNames.ROLE_ASSIGNMENTS_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.PERSON_GRANTEE_COLUMN, ColumnNames.ROLE_COLUMN, ColumnNames.GROUP_COLUMN,
                ColumnNames.DATABASE_INSTANCE_COLUMN }) })
public final class RoleAssignmentPE extends HibernateAbstractRegistrationHolder implements
        IIdHolder, Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final RoleAssignmentPE[] EMPTY_ARRAY = new RoleAssignmentPE[0];

    private transient Long id;

    private DatabaseInstancePE databaseInstance;

    private GroupPE group;

    private PersonPE person;

    private RoleCode role;

    @NotNull(message = ValidationMessages.ROLE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.ROLE_COLUMN)
    @Enumerated(EnumType.STRING)
    public final RoleCode getRole()
    {
        return role;
    }

    public final void setRole(final RoleCode role)
    {
        this.role = role;
    }

    @NotNull(message = ValidationMessages.PERSON_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_GRANTEE_COLUMN, updatable = false)
    private final PersonPE getPersonInternal()
    {
        return person;
    }

    final void setPersonInternal(final PersonPE person)
    {
        this.person = person;
    }

    public final void setPerson(final PersonPE person)
    {
        person.addRoleAssignment(this);
    }

    @Transient
    public final PersonPE getPerson()
    {
        return getPersonInternal();
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.GROUP_COLUMN, updatable = false)
    public final GroupPE getGroup()
    {
        return group;
    }

    public final void setGroup(final GroupPE group)
    {
        this.group = group;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATABASE_INSTANCE_COLUMN, updatable = false)
    public final DatabaseInstancePE getDatabaseInstance()
    {
        return databaseInstance;
    }

    public final void setDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    //
    // IIdHolder
    //

    @SequenceGenerator(name = SequenceNames.ROLE_ASSIGNMENT_SEQUENCE, sequenceName = SequenceNames.ROLE_ASSIGNMENT_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.ROLE_ASSIGNMENT_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getRole(), "role");
        EqualsHashUtils.assertDefined(getPerson(), "person");
        if (getGroup() == null)
        {
            EqualsHashUtils.assertDefined(getDatabaseInstance(), "db");
        }
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof RoleAssignmentPE == false)
        {
            return false;
        }
        final RoleAssignmentPE that = (RoleAssignmentPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getRole(), that.getRole());
        builder.append(getPerson(), that.getPerson());
        builder.append(getDatabaseInstance(), that.getDatabaseInstance());
        builder.append(getGroup(), that.getGroup());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getRole());
        builder.append(getPerson());
        builder.append(getDatabaseInstance());
        builder.append(getGroup());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

}
