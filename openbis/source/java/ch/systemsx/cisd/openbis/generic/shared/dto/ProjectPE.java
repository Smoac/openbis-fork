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
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.UnmodifiableListDecorator;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * A <i>Persistence Entity</i> which represents a project.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.PROJECTS_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.GROUP_COLUMN }) })
@Friend(toClasses = ExperimentPE.class)
public final class ProjectPE extends AttachmentHolderPE implements Comparable<ProjectPE>,
        IIdAndCodeHolder, Serializable
{
    public static final ProjectPE[] EMPTY_ARRAY = new ProjectPE[0];

    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private GroupPE group;

    private List<ExperimentPE> experiments = new ArrayList<ExperimentPE>();

    private String code;

    private String description;

    private PersonPE projectLeader;

    /** The number of experiments this project contains. */
    private int size;

    private PersonPE registrator;

    private Date registrationDate;

    private ProjectIdentifier projectIdentifier;

    private Date modificationDate;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_REGISTERER_COLUMN, updatable = false)
    public PersonPE getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
    }

    public final void setCode(final String code)
    {
        this.code = code;
    }

    /**
     * Sets the group which this <code>ProjectDTO</code> is related to.
     * 
     * @throws AssertionError if <code>groupId</code> is defined but unequal
     *             <code>group.getId()</code>.
     */
    public final void setGroup(final GroupPE group)
    {
        this.group = group;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.GROUP_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.GROUP_COLUMN, updatable = true)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_GROUP)
    public final GroupPE getGroup()
    {
        return group;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectInternal")
    @ContainedIn
    private List<ExperimentPE> getExperimentsInternal()
    {
        return experiments;
    }

    // hibernate only
    @SuppressWarnings("unused")
    private void setExperimentsInternal(List<ExperimentPE> experiments)
    {
        this.experiments = experiments;
    }

    @Transient
    /* Note: modifications of the returned collection will result in an exception. */
    public List<ExperimentPE> getExperiments()
    {
        return new UnmodifiableListDecorator<ExperimentPE>(getExperimentsInternal());
    }

    public void setExperiments(List<ExperimentPE> experiments)
    {
        getExperimentsInternal().clear();
        for (ExperimentPE experiment : experiments)
        {
            addExperiment(experiment);
        }
    }

    @Private
    void addExperiment(ExperimentPE experiment)
    {
        ProjectPE project = experiment.getProject();
        if (project != null)
        {
            project.getExperimentsInternal().remove(experiment);
        }
        experiment.setProjectInternal(this);
        getExperimentsInternal().add(experiment);
    }

    @Transient
    public final int getSize()
    {
        return size;
    }

    public final void setSize(final int size)
    {
        this.size = size;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_LEADER_COLUMN, updatable = false)
    public final PersonPE getProjectLeader()
    {
        return projectLeader;
    }

    public final void setProjectLeader(final PersonPE projectLeader)
    {
        this.projectLeader = projectLeader;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getCode(), "code");
        EqualsHashUtils.assertDefined(getGroup(), "group");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ProjectPE == false)
        {
            return false;
        }
        final ProjectPE that = (ProjectPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getGroup(), that.getGroup());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getGroup());
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
        return builder.toString();
    }

    //
    // Comparable
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    public final int compareTo(final ProjectPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

    //
    // IIdHolder
    //

    @SequenceGenerator(name = SequenceNames.PROJECT_SEQUENCE, sequenceName = SequenceNames.PROJECT_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.PROJECT_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Column(unique = true)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Field(index = Index.TOKENIZED, store = Store.YES, name = SearchFieldConstants.CODE)
    public final String getCode()
    {
        return code;
    }

    @Override
    @Transient
    public String getHolderName()
    {
        return "project";
    }

    @Override
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectParentInternal", cascade = CascadeType.ALL)
    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_ATTACHMENT)
    @Fetch(FetchMode.SUBSELECT)
    protected Set<AttachmentPE> getInternalAttachments()
    {
        return attachments;
    }

    @Transient
    public final String getIdentifier()
    {
        if (projectIdentifier == null)
        {
            projectIdentifier = IdentifierHelper.createProjectIdentifier(this);
        }
        return projectIdentifier.toString();
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

}
