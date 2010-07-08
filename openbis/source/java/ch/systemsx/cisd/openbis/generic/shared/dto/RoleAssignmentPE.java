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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Check;
import org.hibernate.validator.NotNull;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Persistent Entity containing informations about role assignment.
 * 
 * @author Izabela Adamczyk
 */

@Entity
@Check(constraints = "((DBIN_ID IS NOT NULL AND GROU_ID IS NULL) OR (DBIN_ID IS NULL AND GROU_ID IS NOT NULL))"
        + " AND "
        + "((AG_ID_GRANTEE IS NOT NULL AND PERS_ID_GRANTEE IS NULL) OR (AG_ID_GRANTEE IS NULL AND PERS_ID_GRANTEE IS NOT NULL))")
@Table(name = TableNames.ROLE_ASSIGNMENTS_TABLE)
public final class RoleAssignmentPE extends HibernateAbstractRegistrationHolder implements
        IIdHolder, Serializable
{

    private static final long serialVersionUID = IServer.VERSION;

    public static final RoleAssignmentPE[] EMPTY_ARRAY = new RoleAssignmentPE[0];

    private transient Long id;

    private DatabaseInstancePE databaseInstance;

    private GroupPE group;

    private PersonPE person;

    private AuthorizationGroupPE authorizationGroup;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_GRANTEE_COLUMN, updatable = false)
    @Private
    public final PersonPE getPersonInternal()
    {
        return person;
    }

    @Private
    public final void setPersonInternal(final PersonPE person)
    {
        this.person = person;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.AUTHORIZATION_GROUP_ID_GRANTEE_COLUMN, updatable = false)
    @Private
    public final AuthorizationGroupPE getAuthorizationGroupInternal()
    {
        return authorizationGroup;
    }

    @Private
    public final void setAuthorizationGroupInternal(final AuthorizationGroupPE authorizationGroup)
    {
        this.authorizationGroup = authorizationGroup;
    }

    @Transient
    public final PersonPE getPerson()
    {
        return getPersonInternal();
    }

    @Transient
    public final AuthorizationGroupPE getAuthorizationGroup()
    {
        return getAuthorizationGroupInternal();
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
        if (getPerson() == null)
        {
            EqualsHashUtils.assertDefined(getAuthorizationGroupInternal(), "authorization group");
        }
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
        builder.append(getAuthorizationGroup(), that.getAuthorizationGroup());
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
