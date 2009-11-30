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
import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * A <i>Persistence Entity</i> which represents a group.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.GROUPS_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.DATABASE_INSTANCE_COLUMN }) })
public final class GroupPE extends HibernateAbstractRegistrationHolder implements IIdAndCodeHolder,
        Comparable<GroupPE>, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final GroupPE[] EMPTY_ARRAY = new GroupPE[0];

    private transient Long id;

    private String code;

    private String description;

    private DatabaseInstancePE databaseInstance;

    // null if unknown
    private Boolean home;

    public final void setCode(final String code)
    {
        this.code = code;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_1000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.DATABASE_INSTANCE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATABASE_INSTANCE_COLUMN, updatable = false)
    public final DatabaseInstancePE getDatabaseInstance()
    {
        return databaseInstance;
    }

    public final void setDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    @Transient
    public final Boolean isHome()
    {
        return home;
    }

    public final void setHome(final Boolean home)
    {
        this.home = home;
    }

    //
    // IIdAndCodeHolder
    //

    @SequenceGenerator(name = SequenceNames.GROUP_SEQUENCE, sequenceName = SequenceNames.GROUP_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.GROUP_SEQUENCE)
    @Field(index = Index.NO, store = Store.YES)
    public final Long getId()
    {
        return id;
    }

    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Field(index = Index.TOKENIZED, store = Store.YES, name = SearchFieldConstants.CODE)
    public final String getCode()
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
        if (obj instanceof GroupPE == false)
        {
            return false;
        }
        final GroupPE that = (GroupPE) obj;
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
        builder.append("description", getDescription());
        builder.append("home", isHome());
        return builder.toString();
    }

    //
    // Compare
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    public final int compareTo(final GroupPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

    //
    // connected projects for use only in tests (no bidirectional support for connection)
    //

    private List<ProjectPE> projects = new ArrayList<ProjectPE>();

    @Private
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
    public List<ProjectPE> getProjects()
    {
        return projects;
    }

    @SuppressWarnings("unused")
    private void setProjects(List<ProjectPE> projects)
    {
        this.projects = projects;
    }
}
