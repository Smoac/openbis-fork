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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.UnmodifiableListDecorator;
import ch.systemsx.cisd.common.collection.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Persistence Entity representing experiment.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.EXPERIMENTS_VIEW, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.PROJECT_COLUMN }) })
@Indexed(index = "ExperimentPE")
@Friend(toClasses =
    { AttachmentPE.class, ProjectPE.class })
public class ExperimentPE extends AttachmentHolderPE implements
        IEntityInformationWithPropertiesHolder, IIdAndCodeHolder, Comparable<ExperimentPE>,
        IMatchingEntity, IDeletablePE, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final ExperimentPE[] EMPTY_ARRAY = new ExperimentPE[0];

    private transient Long id;

    private String code;

    private ProjectPE project;

    private ExperimentTypePE experimentType;

    private DeletionPE deletion;

    private Set<ExperimentPropertyPE> properties = new HashSet<ExperimentPropertyPE>();

    private List<SamplePE> samples = new ArrayList<SamplePE>();

    private List<DataPE> dataSets = new ArrayList<DataPE>();

    private Date lastDataSetDate;

    private ExperimentIdentifier experimentIdentifier;

    private Set<MetaprojectPE> metaprojects = new HashSet<MetaprojectPE>();

    /**
     * Person who registered this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private PersonPE registrator;

    /**
     * Person who last modified this entity.
     * <p>
     * This is specified at update time.
     * </p>
     */
    private PersonPE modifier;

    /**
     * Registration date of this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private Date registrationDate;

    private Date modificationDate;

    private String permId;

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    @Field(name = SearchFieldConstants.REGISTRATION_DATE, index = Index.UN_TOKENIZED, store = Store.NO)
    @DateBridge(resolution = Resolution.SECOND)
    public Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @Override
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_REGISTERER_COLUMN, updatable = false)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_REGISTRATOR)
    public PersonPE getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_MODIFIER_COLUMN)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_MODIFIER)
    public PersonPE getModifier()
    {
        return modifier;
    }

    public void setModifier(final PersonPE modifier)
    {
        this.modifier = modifier;
    }

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.EXPERIMENT_SEQUENCE, sequenceName = SequenceNames.EXPERIMENT_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.EXPERIMENT_SEQUENCE)
    @DocumentId(name = SearchFieldConstants.ID)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    @Override
    @Column(name = ColumnNames.CODE_COLUMN)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Field(index = Index.TOKENIZED, store = Store.YES, name = SearchFieldConstants.CODE)
    public String getCode()
    {
        return code;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.PROJECT_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.PROJECT_COLUMN, updatable = true)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_PROJECT)
    private ProjectPE getProjectInternal()
    {
        return project;
    }

    @Private
    void setProjectInternal(final ProjectPE project)
    {
        this.project = project;
    }

    @Transient
    public ProjectPE getProject()
    {
        return getProjectInternal();
    }

    public void setProject(final ProjectPE project)
    {
        project.addExperiment(this);
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.EXPERIMENT_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.EXPERIMENT_TYPE_COLUMN, updatable = false)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_ENTITY_TYPE)
    public ExperimentTypePE getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(final ExperimentTypePE experimentType)
    {

        this.experimentType = experimentType;
    }

    @Override
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DELETION_COLUMN)
    public DeletionPE getDeletion()
    {
        return deletion;
    }

    public void setDeletion(final DeletionPE deletion)
    {
        this.deletion = deletion;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity", orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_PROPERTIES)
    @Fetch(FetchMode.SUBSELECT)
    private Set<ExperimentPropertyPE> getExperimentProperties()
    {
        return properties;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setExperimentProperties(final Set<ExperimentPropertyPE> properties)
    {
        this.properties = properties;
    }

    @Override
    @Transient
    public Set<ExperimentPropertyPE> getProperties()
    {
        return new UnmodifiableSetDecorator<ExperimentPropertyPE>(getExperimentProperties());
    }

    /**
     * Returns <code>true</code>, if and only if the properties have been initialized.
     */
    @Override
    @Transient
    public boolean isPropertiesInitialized()
    {
        return HibernateUtils.isInitialized(getExperimentProperties());
    }

    @Override
    public void setProperties(final Set<? extends EntityPropertyPE> properties)
    {
        getExperimentProperties().clear();
        for (final EntityPropertyPE untypedProperty : properties)
        {
            ExperimentPropertyPE experimentProperty = (ExperimentPropertyPE) untypedProperty;
            final ExperimentPE parent = experimentProperty.getEntity();
            if (parent != null)
            {
                parent.getExperimentProperties().remove(experimentProperty);
            }
            addProperty(experimentProperty);
        }
    }

    @Override
    public void addProperty(final EntityPropertyPE property)
    {
        property.setEntity(this);
        getExperimentProperties().add((ExperimentPropertyPE) property);
    }

    @Override
    public void removeProperty(final EntityPropertyPE property)
    {
        getExperimentProperties().remove(property);
        property.setEntity(null);
    }

    @Override
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "experimentParentInternal", cascade = CascadeType.ALL, orphanRemoval = true)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_ATTACHMENT)
    @Fetch(FetchMode.SUBSELECT)
    protected Set<AttachmentPE> getInternalAttachments()
    {
        return attachments;
    }

    @Transient
    /* Note: modifications of the returned collection will result in an exception. */
    public List<SamplePE> getSamples()
    {
        return new UnmodifiableListDecorator<SamplePE>(getExperimentSamples());
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "experimentInternal")
    private List<SamplePE> getExperimentSamples()
    {
        return samples;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setExperimentSamples(final List<SamplePE> samples)
    {
        this.samples = samples;
    }

    public void setSamples(List<SamplePE> samples)
    {
        getExperimentSamples().clear();
        for (SamplePE sample : samples)
        {
            addSample(sample);
        }
    }

    public void removeSample(SamplePE sample)
    {
        getExperimentSamples().remove(sample);
        sample.setExperimentInternal(null);
    }

    public void addSample(SamplePE sample)
    {
        ExperimentPE experiment = sample.getExperiment();
        if (experiment != null)
        {
            experiment.getExperimentSamples().remove(sample);
        }
        sample.setExperimentInternal(this);
        getExperimentSamples().add(sample);
    }

    @Transient
    public List<DataPE> getDataSets()
    {
        return new UnmodifiableListDecorator<DataPE>(getExperimentDataSets());
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "experimentInternal")
    private List<DataPE> getExperimentDataSets()
    {
        return dataSets;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setExperimentDataSets(final List<DataPE> dataSets)
    {
        this.dataSets = dataSets;
    }

    public void addDataSet(final DataPE child)
    {
        final ExperimentPE parent = child.getExperiment();
        if (parent != null)
        {
            parent.getExperimentDataSets().remove(child);
        }
        child.setExperimentInternal(this);
        getExperimentDataSets().add(child);
    }

    @Transient
    public Date getLastDataSetDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(lastDataSetDate);
    }

    public void setLastDataSetDate(final Date lastDataSetDate)
    {
        this.lastDataSetDate = lastDataSetDate;
    }

    //
    // Comparable
    //

    @Override
    public int compareTo(final ExperimentPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getCode(), "code");
        EqualsHashUtils.assertDefined(getProject(), "project");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ExperimentPE == false)
        {
            return false;
        }
        final ExperimentPE that = (ExperimentPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getProject(), that.getProject());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getProject());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        builder.append("project", getProject());
        builder.append("experimentType", getExperimentType());
        builder.append("deletion", getDeletion());
        return builder.toString();
    }

    //
    // IMatchingEntity
    //

    @Override
    @Transient
    @Field(index = Index.NO, store = Store.YES, name = SearchFieldConstants.IDENTIFIER)
    public final String getIdentifier()
    {
        if (experimentIdentifier == null)
        {
            experimentIdentifier = IdentifierHelper.createExperimentIdentifier(this);
        }
        return experimentIdentifier.toString();
    }

    @Override
    @Transient
    public final EntityTypePE getEntityType()
    {
        return getExperimentType();
    }

    @Override
    @Transient
    public final EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Version
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    @Field(name = SearchFieldConstants.MODIFICATION_DATE, index = Index.UN_TOKENIZED, store = Store.NO)
    @DateBridge(resolution = Resolution.SECOND)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    @Override
    @Transient
    public AttachmentHolderKind getAttachmentHolderKind()
    {
        return AttachmentHolderKind.EXPERIMENT;
    }

    @Override
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Column(name = ColumnNames.PERM_ID_COLUMN, nullable = false)
    @Field(index = Index.TOKENIZED, store = Store.YES, name = SearchFieldConstants.PERM_ID)
    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    @SuppressWarnings("unused")
    @ManyToMany(fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(name = TableNames.METAPROJECT_ASSIGNMENTS_VIEW, joinColumns =
        { @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN) }, inverseJoinColumns =
        { @JoinColumn(name = ColumnNames.METAPROJECT_ID_COLUMN) })
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_METAPROJECT)
    private Set<MetaprojectPE> getMetaprojects()
    {
        return this.metaprojects;
    }

    @SuppressWarnings("unused")
    private void setMetaprojects(Set<MetaprojectPE> metaprojects)
    {
        this.metaprojects = metaprojects;
    }

}
