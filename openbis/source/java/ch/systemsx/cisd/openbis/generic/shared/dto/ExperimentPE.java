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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collections.UnmodifiableListDecorator;
import ch.systemsx.cisd.common.collections.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Persistence Entity representing experiment.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.EXPERIMENTS_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.PROJECT_COLUMN }) })
@Indexed
@Friend(toClasses = AttachmentPE.class)
public class ExperimentPE extends AttachmentHolderPE implements
        IEntityPropertiesHolder<ExperimentPropertyPE>, IIdAndCodeHolder, Comparable<ExperimentPE>,
        IMatchingEntity, Serializable

{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final ExperimentPE[] EMPTY_ARRAY = new ExperimentPE[0];

    private transient Long id;

    private String code;

    private ProjectPE project;

    private MaterialPE studyObject;

    private ExperimentTypePE experimentType;

    private InvalidationPE invalidation;

    private Set<ExperimentPropertyPE> properties = new HashSet<ExperimentPropertyPE>();

    private List<SamplePE> samples = new ArrayList<SamplePE>();

    private List<DataPE> dataSets = new ArrayList<DataPE>();

    private ProcessingInstructionDTO[] processingInstructions =
            ProcessingInstructionDTO.EMPTY_ARRAY;

    private Date lastDataSetDate;

    private ExperimentIdentifier experimentIdentifier;

    /**
     * Person who registered this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private PersonPE registrator;

    /**
     * Registration date of this entity.
     * <p>
     * This is specified at insert time.
     * </p>
     */
    private Date registrationDate;

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
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_REGISTRATOR)
    public PersonPE getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
    }

    @Id
    @SequenceGenerator(name = SequenceNames.EXPERIMENT_SEQUENCE, sequenceName = SequenceNames.EXPERIMENT_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.EXPERIMENT_SEQUENCE)
    @DocumentId
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    @Column(name = ColumnNames.CODE_COLUMN)
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
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

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.PROJECT_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.PROJECT_COLUMN, updatable = true)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_PROJECT)
    public ProjectPE getProject()
    {
        return project;
    }

    public void setProject(final ProjectPE project)
    {
        this.project = project;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.STUDY_OBJECT_COLUMN, updatable = false)
    public MaterialPE getStudyObject()
    {
        return studyObject;
    }

    public void setStudyObject(final MaterialPE studyObject)
    {
        this.studyObject = studyObject;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.EXPERIMENT_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.EXPERIMENT_TYPE_COLUMN, updatable = false)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_EXPERIMENT_TYPE)
    public ExperimentTypePE getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(final ExperimentTypePE experimentType)
    {

        this.experimentType = experimentType;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.INVALIDATION_COLUMN)
    public InvalidationPE getInvalidation()
    {
        return invalidation;
    }

    public void setInvalidation(final InvalidationPE invalidation)
    {

        this.invalidation = invalidation;
    }

    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_PROPERTIES)
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

    @Transient
    public Set<ExperimentPropertyPE> getProperties()
    {
        return new UnmodifiableSetDecorator<ExperimentPropertyPE>(getExperimentProperties());
    }

    public void setProperties(final Set<ExperimentPropertyPE> properties)
    {
        getExperimentProperties().clear();
        for (final ExperimentPropertyPE experimentProperty : properties)
        {
            final ExperimentPE parent = experimentProperty.getExperiment();
            if (parent != null)
            {
                parent.getExperimentProperties().remove(experimentProperty);
            }
            addProperty(experimentProperty);
        }
    }

    public void addProperty(final ExperimentPropertyPE property)
    {
        property.setEntity(this);
        getExperimentProperties().add(property);
    }

    @Override
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "experimentParentInternal")
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_EXPERIMENT_ATTACHMENTS)
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
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN, updatable = true)
    @ContainedIn
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
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN, updatable = true)
    @ContainedIn
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

    public final void setDataSets(final List<DataPE> dataSets)
    {
        getExperimentDataSets().clear();
        for (final DataPE child : dataSets)
        {
            addDataSet(child);
        }
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
    public ProcessingInstructionDTO[] getProcessingInstructions()
    {
        return processingInstructions;
    }

    public void setProcessingInstructions(final ProcessingInstructionDTO[] processingInstructions)
    {
        this.processingInstructions = processingInstructions;
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
        builder.append("invalidation", getInvalidation());
        return builder.toString();
    }

    //
    // IMatchingEntity
    //

    @Transient
    public final String getIdentifier()
    {
        if (experimentIdentifier == null)
        {
            experimentIdentifier = IdentifierHelper.createExperimentIdentifier(this);
        }
        return experimentIdentifier.toString();
    }

    @Transient
    public final EntityTypePE getEntityType()
    {
        return getExperimentType();
    }

    @Transient
    public final EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
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

    @Override
    @Transient
    public String getHolderName()
    {
        return "experiment";
    }

}
