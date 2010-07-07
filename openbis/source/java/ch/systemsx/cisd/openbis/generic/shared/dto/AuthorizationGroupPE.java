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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collections.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * Persistent Entity containing informations about authorization group.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.AUTHORIZATION_GROUPS_TABLE)
@Friend(toClasses = RoleAssignmentPE.class)
public class AuthorizationGroupPE extends HibernateAbstractRegistrationHolder implements
        Comparable<AuthorizationGroupPE>, IIdAndCodeHolder, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private DatabaseInstancePE databaseInstance;

    private String code;

    private String description;

    private Date modificationDate;

    private Set<RoleAssignmentPE> roleAssignments = new HashSet<RoleAssignmentPE>();

    private Set<PersonPE> persons = new HashSet<PersonPE>();

    public AuthorizationGroupPE()
    {
    }

    @SequenceGenerator(name = SequenceNames.AUTHORIZATION_GROUP_ID_SEQUENCE, sequenceName = SequenceNames.AUTHORIZATION_GROUP_ID_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.AUTHORIZATION_GROUP_ID_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public final void setId(final Long id)
    {
        this.id = id;
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

    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Field(index = Index.TOKENIZED, store = Store.YES, name = SearchFieldConstants.CODE)
    public String getCode()
    {
        return code;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "authorizationGroupInternal")
    private Set<RoleAssignmentPE> getRoleAssignmentsInternal()
    {
        return roleAssignments;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setRoleAssignmentsInternal(final Set<RoleAssignmentPE> rolesAssignments)
    {
        this.roleAssignments = rolesAssignments;
    }

    public final void setRoleAssignments(final Set<RoleAssignmentPE> rolesAssignments)
    {
        getRoleAssignmentsInternal().clear();
        for (final RoleAssignmentPE child : rolesAssignments)
        {
            addRoleAssignment(child);
        }
    }

    @Transient
    public final Set<RoleAssignmentPE> getRoleAssignments()
    {
        return new UnmodifiableSetDecorator<RoleAssignmentPE>(getRoleAssignmentsInternal());
    }

    public void addRoleAssignment(final RoleAssignmentPE roleAssignment)
    {
        final AuthorizationGroupPE authGroup = roleAssignment.getAuthorizationGroup();
        if (authGroup != null)
        {
            authGroup.removeRoleAssigment(roleAssignment);
        }
        roleAssignment.setAuthorizationGroupInternal(this);
        getRoleAssignmentsInternal().add(roleAssignment);
    }

    public void removeRoleAssigment(final RoleAssignmentPE roleAssignment)
    {
        assert roleAssignment != null : "Unspecified role assignment.";
        getRoleAssignmentsInternal().remove(roleAssignment);
        roleAssignment.setAuthorizationGroupInternal(null);
    }

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "authorizationGroupsInternal")
    private Set<PersonPE> getPersonsInternal()
    {
        return persons;
    }

    @Transient
    public final Set<PersonPE> getPersons()
    {
        return new UnmodifiableSetDecorator<PersonPE>(getPersonsInternal());
    }

    public void removePerson(final PersonPE person)
    {
        assert person != null : "Unspecified person.";
        getPersonsInternal().remove(person);
        person.getAuthorizationGroupsInternal().remove(this);
    }

    public void addPerson(final PersonPE person)
    {
        assert person != null : "Unspecified person.";
        person.getAuthorizationGroupsInternal().add(this);
        getPersonsInternal().add(person);
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setPersonsInternal(final Set<PersonPE> persons)
    {
        this.persons = persons;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof AuthorizationGroupPE == false)
        {
            return false;
        }
        final AuthorizationGroupPE that = (AuthorizationGroupPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getDatabaseInstance(), that.getDatabaseInstance());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getDatabaseInstance());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        builder.append("dbInstance", getDatabaseInstance());
        return builder.toString();
    }

    public int compareTo(AuthorizationGroupPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

}
