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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.UnmodifiableListDecorator;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;

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
public class ExperimentPE implements IEntityPropertiesHolder<ExperimentPropertyPE>,
        IIdAndCodeHolder, Comparable<ExperimentPE>, IMatchingEntity, Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final Object EMPTY_ARRAY = new ExperimentPE[0];

    public static final char HIDDEN_EXPERIMENT_PROPERTY_PREFIX_CHARACTER = '$';

    public static final String HIDDEN_EXPERIMENT_PROPERTY_PREFIX =
            Character.toString(HIDDEN_EXPERIMENT_PROPERTY_PREFIX_CHARACTER);

    public static final String HIDDEN_EXPERIMENT_PROPERTY_PREFIX2 =
            HIDDEN_EXPERIMENT_PROPERTY_PREFIX + HIDDEN_EXPERIMENT_PROPERTY_PREFIX;

    private transient Long id;

    private String code;

    private ProjectPE project;

    private MaterialPE studyObject;

    private ExperimentTypePE experimentType;

    private InvalidationPE invalidation;

    private List<ExperimentPropertyPE> properties = new LinkedList<ExperimentPropertyPE>();

    private DataStorePE dataStore;

    private List<AttachmentPE> attachments = AttachmentPE.EMPTY_LIST;

    private List<ProcedurePE> procedures = new ArrayList<ProcedurePE>();

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
    @IndexedEmbedded
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
    @Field(index = Index.UN_TOKENIZED, store = Store.YES)
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
    @JoinColumn(name = ColumnNames.PROJECT_COLUMN, updatable = false)
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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<ExperimentPropertyPE> getExperimentProperties()
    {
        return properties;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setExperimentProperties(List<ExperimentPropertyPE> properties)
    {
        this.properties = properties;
    }

    @Transient
    public List<ExperimentPropertyPE> getProperties()
    {
        return new UnmodifiableListDecorator<ExperimentPropertyPE>(getExperimentProperties());
    }

    public void setProperties(final List<ExperimentPropertyPE> properties)
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

    public void addProperty(ExperimentPropertyPE property)
    {
        property.setEntity(this);
        getExperimentProperties().add(property);
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "parent")
    @Private
    public List<AttachmentPE> getExperimentAttachments()
    {
        return attachments;
    }

    @Private
    public void setExperimentAttachments(final List<AttachmentPE> attachments)
    {
        this.attachments = attachments;
    }

    @Transient
    public final List<AttachmentPE> getAttachments()
    {
        final List<AttachmentPE> list = getExperimentAttachments();
        for (final Iterator<AttachmentPE> iter = list.iterator(); iter.hasNext(); /**/)
        {
            final AttachmentPE property = iter.next();
            final boolean isHiddenFile = isHiddenFile(property.getFileName());
            if (isHiddenFile)
            {
                iter.remove();
            }
            unescapeFileName(property);
        }
        return list;
    }

    // Package visibility to avoid bean conversion which will call an uninitialized field.
    final void setAttachments(final List<AttachmentPE> attachments)
    {
        for (AttachmentPE experimentAttachment : attachments)
        {
            experimentAttachment.setParent(this);
        }
        setExperimentAttachments(attachments);
    }

    @Transient
    public List<ProcedurePE> getProcedures()
    {
        return new UnmodifiableListDecorator<ProcedurePE>(getExperimentProcedures());
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "experimentInternal")
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN, updatable = false)
    private List<ProcedurePE> getExperimentProcedures()
    {
        return procedures;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setExperimentProcedures(final List<ProcedurePE> procedures)
    {
        this.procedures = procedures;
    }

    public final void setProcedures(final List<ProcedurePE> procedures)
    {
        getExperimentProcedures().clear();
        for (final ProcedurePE child : procedures)
        {
            addProcedure(child);
        }
    }

    public void addProcedure(ProcedurePE child)
    {
        final ExperimentPE parent = child.getExperiment();
        if (parent != null)
        {
            parent.getExperimentProcedures().remove(child);
        }
        child.setExperimentInternal(this);
        getExperimentProcedures().add(child);
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATA_STORE_COLUMN)
    public DataStorePE getDataStore()
    {
        return dataStore;
    }

    public void setDataStore(final DataStorePE dataStore)
    {
        this.dataStore = dataStore;
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

    public final static void unescapeFileName(final AbstractAttachmentPE attachment)
    {
        if (attachment != null)
        {
            final String fileName = attachment.getFileName();
            if (fileName != null && fileName.startsWith(HIDDEN_EXPERIMENT_PROPERTY_PREFIX2))
            {
                attachment.setFileName(fileName.substring(1));
            }
        }
    }

    public final static boolean isHiddenFile(final String fileName)
    {
        return fileName.startsWith(HIDDEN_EXPERIMENT_PROPERTY_PREFIX)
                && (fileName.length() == 1 || fileName.charAt(1) != HIDDEN_EXPERIMENT_PROPERTY_PREFIX_CHARACTER);
    }

    public final static String escapeFileName(final String fileName)
    {
        if (fileName != null && fileName.startsWith(HIDDEN_EXPERIMENT_PROPERTY_PREFIX))
        {
            return HIDDEN_EXPERIMENT_PROPERTY_PREFIX + fileName;
        }
        return fileName;
    }

    public void ensurePropertiesAreLoaded()
    {
        Hibernate.initialize(getExperimentProperties());
    }

    //
    // IMatchingEntity
    //

    @Transient
    public final String getIdentifier()
    {
        if (experimentIdentifier == null)
        {
            experimentIdentifier = IdentifierHelper.createIdentifier(this);
        }
        return experimentIdentifier.toString();
    }
}
