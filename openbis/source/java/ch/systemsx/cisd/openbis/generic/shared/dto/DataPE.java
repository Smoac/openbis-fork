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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.OptimisticLock;
import org.hibernate.annotations.OrderBy;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.common.collection.UnmodifiableSetDecorator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Kind of <i>Java Bean</i> or <i>Value Object</i> which contains any information we would like to
 * know about one DATA.
 * <p>
 * This class is the <i>Java Object</i> representation of the corresponding data in the database.
 * </p>
 * 
 * @author Bernd Rinn
 */
@Entity
@Table(name = TableNames.DATA_VIEW, uniqueConstraints = @UniqueConstraint(columnNames = ColumnNames.CODE_COLUMN))
@Inheritance(strategy = InheritanceType.JOINED)
@Indexed(index = "DataPE")
public class DataPE extends AbstractIdAndCodeHolder<DataPE> implements
        IEntityInformationWithPropertiesHolder, IMatchingEntity, IIdentifierHolder, IDeletablePE,
        IEntityWithMetaprojects, IModifierAndModificationDateBean
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final DataPE[] EMPTY_ARRAY = new DataPE[0];

    private transient Long id;

    private String code;

    private boolean placeholder;

    private boolean isDerived;

    private PersonPE registrator;

    private PersonPE modifier;

    /** Registration date of the database instance. */
    private Date registrationDate;

    private DataSetTypePE dataSetType;

    private ExperimentPE experiment;

    private SamplePE sample;

    private Date productionDate;

    private Date modificationDate;

    private int version;

    private String dataProducerCode;

    private DataPE container = null;

    private List<DataPE> containedDataSets = new ArrayList<DataPE>();

    private Set<MetaprojectAssignmentPE> metaprojectAssignments =
            new HashSet<MetaprojectAssignmentPE>();

    /**
     * Deletion information.
     * <p>
     * If not <code>null</code>, then this data set is considered <i>deleted</i> (moved to trash).
     * </p>
     */
    private DeletionPE deletion;

    /**
     * If not null than this object has been originally trashed. (As oposed to the entities which
     * were trashed as being dependent on other trashed entity)
     */
    private Integer originalDeletion;

    /**
     * the index of this {@link DataPE} within its virtual parent; null if there is virtual parent
     */
    private Integer orderInContainer;

    private DataStorePE dataStore;

    private Set<DataSetRelationshipPE> parentRelationships = new HashSet<DataSetRelationshipPE>();

    private Set<DataSetRelationshipPE> childRelationships = new HashSet<DataSetRelationshipPE>();

    @OptimisticLock(excluded = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentDataSet")
    @Fetch(FetchMode.SUBSELECT)
    private Set<DataSetRelationshipPE> getDataSetChildRelationships()
    {
        return childRelationships;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setDataSetChildRelationships(final Set<DataSetRelationshipPE> childRelationships)
    {
        this.childRelationships = childRelationships;
    }

    @Transient
    public Set<DataSetRelationshipPE> getChildRelationships()
    {
        return new UnmodifiableSetDecorator<DataSetRelationshipPE>(getDataSetChildRelationships());
    }

    public void addChildRelationship(final DataSetRelationshipPE relationship)
    {
        relationship.setParentDataSet(this);
        getDataSetChildRelationships().add(relationship);
    }

    @OptimisticLock(excluded = true)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "childDataSet", orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private Set<DataSetRelationshipPE> getDataSetParentRelationships()
    {
        return parentRelationships;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setDataSetParentRelationships(final Set<DataSetRelationshipPE> parentRelationships)
    {
        this.parentRelationships = parentRelationships;
    }

    @Transient
    public Set<DataSetRelationshipPE> getParentRelationships()
    {
        return new UnmodifiableSetDecorator<DataSetRelationshipPE>(getDataSetParentRelationships());
    }

    /**
     * Returns <code>true</code>, if and only if the relationships have been initialized.
     */
    @Transient
    public boolean isParentRelationshipsInitialized()
    {
        return HibernateUtils.isInitialized(getDataSetParentRelationships());
    }

    public void setParentRelationships(final Set<DataSetRelationshipPE> parentRelationships)
    {
        getDataSetParentRelationships().clear();
        for (final DataSetRelationshipPE dataSetRelationship : parentRelationships)
        {
            final DataPE parent = dataSetRelationship.getChildDataSet();
            if (parent != null)
            {
                parent.getDataSetParentRelationships().remove(dataSetRelationship);
            }
            addParentRelationship(dataSetRelationship);
        }
    }

    public void addParentRelationship(final DataSetRelationshipPE relationship)
    {
        relationship.setChildDataSet(this);
        getDataSetParentRelationships().add(relationship);
    }

    public void removeParentRelationship(final DataSetRelationshipPE relationship)
    {
        getDataSetParentRelationships().remove(relationship);
        relationship.getParentDataSet().getDataSetChildRelationships().remove(relationship);
        relationship.setChildDataSet(null);
        relationship.setParentDataSet(null);
    }

    @Transient
    public List<DataPE> getParents()
    {
        final Set<DataSetRelationshipPE> relationships = getParentRelationships();
        final List<DataPE> parents = new ArrayList<DataPE>();
        for (DataSetRelationshipPE r : relationships)
        {
            assert r.getChildDataSet().equals(this);
            parents.add(r.getParentDataSet());
        }
        return parents;
    }

    @Transient
    public List<DataPE> getChildren()
    {
        final Set<DataSetRelationshipPE> relationships = getChildRelationships();
        final List<DataPE> children = new ArrayList<DataPE>();
        for (DataSetRelationshipPE r : relationships)
        {
            assert r.getParentDataSet().equals(this);
            children.add(r.getChildDataSet());
        }
        return children;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = ValidationMessages.DATA_STORE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATA_STORE_COLUMN, updatable = false)
    public DataStorePE getDataStore()
    {
        return dataStore;
    }

    public void setDataStore(final DataStorePE dataStorePE)
    {
        this.dataStore = dataStorePE;
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

    @Override
    @OptimisticLock(excluded = true)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_MODIFIER_COLUMN)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_MODIFIER)
    public PersonPE getModifier()
    {
        return modifier;
    }

    @Override
    public void setModifier(final PersonPE modifier)
    {
        this.modifier = modifier;
    }

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false)
    @Generated(GenerationTime.ALWAYS)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = ValidationMessages.DATA_SET_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATA_SET_TYPE_COLUMN)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_ENTITY_TYPE)
    public DataSetTypePE getDataSetType()
    {
        return dataSetType;
    }

    /** Sets <code>dataSetType</code>. */
    public void setDataSetType(final DataSetTypePE dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    /**
     * Returns <code>true</code> if this data set is data set is derived from a sample (otherwise it
     * is measured from a sample).
     */
    @Column(name = ColumnNames.IS_DERIVED)
    public boolean isDerived()
    {
        return isDerived;
    }

    /**
     * Set to <code>true</code> if this data set is data set is derived from a sample (otherwise it
     * is measured from a sample).
     */
    public void setDerived(boolean isDerived)
    {
        this.isDerived = isDerived;
    }

    @Column(name = ColumnNames.DATA_CONTAINER_ORDER_COLUMN)
    public Integer getOrderInContainer()
    {
        return orderInContainer;
    }

    public void setOrderInContainer(Integer orderInContainer)
    {
        this.orderInContainer = orderInContainer;
    }

    /**
     * Returns <code>true</code> if this data set is data set is measured from a sample (otherwise
     * it is derived from a sample).
     */
    @Transient
    public boolean isMeasured()
    {
        return isDerived == false;
    }

    /**
     * Returns <code>true</code> if this data set is a placeholder for a data set yet to arrive.
     */
    @Column(name = ColumnNames.IS_PLACEHOLDER_COLUMN)
    public boolean isPlaceholder()
    {
        return placeholder;
    }

    /**
     * Set to <code>true</code> if this data set is a placeholder for a data set yet to arrive.
     */
    public void setPlaceholder(final boolean placeholder)
    {
        this.placeholder = placeholder;
    }

    // bidirectional connection SamplePE-DataPE

    public void setSampleAcquiredFrom(final SamplePE sample)
    {
        setDerived(false);
        setSample(sample);
    }

    // @Private
    public void setSample(final SamplePE sample)
    {
        if (sample != null)
        {
            sample.addDataSet(this);
        } else
        {
            SamplePE previousSample = tryGetSample();
            if (previousSample != null) // this should always be true (otherwise there is no change)
            {
                previousSample.removeDataSet(this);
            }
        }
    }

    @Transient
    public SamplePE tryGetSample()
    {
        return getSampleInternal();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.SAMPLE_COLUMN)
    private SamplePE getSampleInternal()
    {
        return sample;
    }

    // TODO 2009-04-28, Tomasz Pylak: make @Private
    void setSampleInternal(final SamplePE sample)
    {
        this.sample = sample;
    }

    // used only by Hibernate Search
    @SuppressWarnings("unused")
    @Transient
    @Field(index = Index.UN_TOKENIZED, store = Store.YES, name = SearchFieldConstants.SAMPLE_ID)
    private Long getSampleId()
    {
        Long result = null;
        if (getSampleInternal() != null)
        {
            result = HibernateUtils.getId(getSampleInternal());
            assert result != null;
        }
        return result;
    }

    /**
     * Returns the date when the measurement / calculation that produced this external data set has
     * been performed.
     * <p>
     * This may not be known in which case this method will return <code>null</code>.
     */
    @Column(name = ColumnNames.PRODUCTION_TIMESTAMP_COLUMN)
    public Date getProductionDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(productionDate);
    }

    /**
     * Sets the date when the measurement / calculation that produced this external data set has
     * been performed.
     */
    public void setProductionDate(final Date productionDate)
    {
        this.productionDate = productionDate;
    }

    /**
     * Returns the code identifying the data source (i.e. measurement device or software pipeline)
     * that produced this external data set.
     * <p>
     * This may not be known in which case this method will return <code>null</code>.
     * </p>
     */
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Column(name = ColumnNames.DATA_PRODUCER_CODE_COLUMN)
    public String getDataProducerCode()
    {
        return dataProducerCode;
    }

    /**
     * Sets the code identifying the data source (i.e. measurement device or software pipeline) that
     * produced this external data set.
     */
    public void setDataProducerCode(final String dataProducerCode)
    {
        this.dataProducerCode = dataProducerCode;
    }

    @Version
    @Column(name = ColumnNames.VERSION_COLUMN, nullable = false)
    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    @Override
    @OptimisticLock(excluded = true)
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    @Field(name = SearchFieldConstants.MODIFICATION_DATE, index = Index.UN_TOKENIZED, store = Store.NO)
    @DateBridge(resolution = Resolution.SECOND)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    @Override
    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    /**
     * Sets the code (i.e. the externally used unique identifier) of this external data.
     */
    public void setCode(final String code)
    {
        this.code = code;
    }

    @Transient
    public DataPE getContainer()
    {
        return getContainerInternal();
    }

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = DataPE.class)
    @JoinColumn(name = ColumnNames.DATA_CONTAINER_COLUMN, updatable = true)
    private DataPE getContainerInternal()
    {
        return container;
    }

    private void setContainerInternal(final DataPE container)
    {
        this.container = container;
    }

    // used only by Hibernate Search
    @SuppressWarnings("unused")
    @Transient
    @Field(index = Index.UN_TOKENIZED, store = Store.YES, name = SearchFieldConstants.CONTAINER_ID)
    private Long getContainerId()
    {
        Long result = null;
        if (getContainer() != null)
        {
            result = HibernateUtils.getId(getContainerInternal());
            assert result != null;
        }
        return result;
    }

    public void addComponent(final DataPE component, final PersonPE modifierPerson)
    {
        assert component != null;
        this.containedDataSets.add(component);
        component.setContainerInternal(this);
        component.setModifier(modifierPerson);
        component.setOrderInContainer(containedDataSets.size());
    }

    public void removeComponent(final DataPE component)
    {
        assert component != null;
        this.containedDataSets.remove(component);
        component.setContainerInternal(null);
        component.setOrderInContainer(null);
    }

    @OptimisticLock(excluded = true)
    @OneToMany(mappedBy = "containerInternal", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @OrderBy(clause = ColumnNames.DATA_CONTAINER_ORDER_COLUMN)
    public List<DataPE> getContainedDataSets()
    {
        return containedDataSets;
    }

    // for Hibernate
    @SuppressWarnings("unused")
    private void setContainedDataSets(List<DataPE> containedDataSets)
    {
        this.containedDataSets = containedDataSets;
    }

    //
    // AbstractIdAndCodeHolder
    //

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.DATA_SEQUENCE, sequenceName = SequenceNames.DATA_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.DATA_SEQUENCE)
    @DocumentId(name = SearchFieldConstants.ID)
    public Long getId()
    {
        return id;
    }

    @Override
    @Column(unique = true)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Field(index = Index.TOKENIZED, store = Store.YES, name = SearchFieldConstants.CODE)
    public String getCode()
    {
        return code;
    }

    public void setExperiment(final ExperimentPE experiment)
    {
        if (experiment != null)
        {
            experiment.addDataSet(this);

            if (containedDataSets != null)
            {
                for (DataPE contained : containedDataSets)
                {
                    if (false == contained.equals(this))
                    {
                        contained.setExperiment(experiment);
                    }
                }
            }
        }
    }

    @Transient
    public ExperimentPE getExperiment()
    {
        return getExperimentInternal();
    }

    void setExperimentInternal(final ExperimentPE experiment)
    {
        this.experiment = experiment;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = ValidationMessages.EXPERIMENT_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN, updatable = true)
    private ExperimentPE getExperimentInternal()
    {
        return experiment;
    }

    // used only by Hibernate Search
    @SuppressWarnings("unused")
    @Transient
    @Field(index = Index.UN_TOKENIZED, store = Store.YES, name = SearchFieldConstants.EXPERIMENT_ID)
    private Long getExperimentId()
    {
        Long result = null;
        if (getExperimentInternal() != null)
        {
            result = HibernateUtils.getId(getExperimentInternal());
            assert result != null;
        }
        return result;
    }

    private Set<DataSetPropertyPE> properties = new HashSet<DataSetPropertyPE>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity", orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_PROPERTIES)
    @BatchSize(size = 100)
    private Set<DataSetPropertyPE> getDataSetProperties()
    {
        return properties;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setDataSetProperties(final Set<DataSetPropertyPE> properties)
    {
        this.properties = properties;
    }

    @Override
    public void setProperties(final Set<? extends EntityPropertyPE> properties)
    {
        getDataSetProperties().clear();
        for (final EntityPropertyPE untypedProperty : properties)
        {
            DataSetPropertyPE property = (DataSetPropertyPE) untypedProperty;
            final DataPE parent = property.getEntity();
            if (parent != null)
            {
                parent.getDataSetProperties().remove(property);
            }
            addProperty(property);
        }
    }

    @Override
    public void addProperty(final EntityPropertyPE property)
    {
        property.setEntity(this);
        getDataSetProperties().add((DataSetPropertyPE) property);
    }

    @Override
    public void removeProperty(final EntityPropertyPE property)
    {
        getDataSetProperties().remove(property);
        property.setEntity(null);
    }

    @Override
    @Transient
    public Set<DataSetPropertyPE> getProperties()
    {
        return new UnmodifiableSetDecorator<DataSetPropertyPE>(getDataSetProperties());
    }

    /**
     * Returns <code>true</code>, if and only if the properties have been initialized.
     */
    @Override
    @Transient
    public boolean isPropertiesInitialized()
    {
        return HibernateUtils.isInitialized(getDataSetProperties());
    }

    @Override
    @Transient
    public EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    @Transient
    public EntityTypePE getEntityType()
    {
        return getDataSetType();
    }

    @Override
    @Transient
    @Field(index = Index.NO, store = Store.YES, name = SearchFieldConstants.IDENTIFIER)
    public String getIdentifier()
    {
        return getCode();
    }

    @Override
    @Transient
    @Field(index = Index.NO, store = Store.YES, name = SearchFieldConstants.PERM_ID)
    public String getPermId()
    {
        return code;
    }

    @Transient
    /**
     * @return <code>true</code> if this is a container data set.
     */
    public boolean isContainer()
    {
        return tryAsExternalData() == null && isLinkData() == false;
    }

    @Transient
    /**
     * @return <code>true</code> if this is a data set with external data.
     */
    public boolean isExternalData()
    {
        return isPlaceholder() == false && isContainer() == false && isLinkData() == false;
    }

    @Transient
    public ExternalDataPE tryAsExternalData()
    {
        return (this instanceof ExternalDataPE) ? (ExternalDataPE) this : null;
    }

    @Transient
    public boolean isLinkData()
    {
        return false;
    }

    @Transient
    public LinkDataPE tryAsLinkData()
    {
        return null;
    }

    /**
     * return true if the data set if available in the data store.
     */
    @Transient
    public boolean isAvailable()
    {
        return false;
    }

    /**
     * return true if the data set can be deleted.
     */
    @Transient
    public boolean isDeletable()
    {
        return true;
    }

    // convenience method useful when checking authorization rules
    @Transient
    public SpacePE getSpace()
    {
        return getExperiment().getProject().getSpace();
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

    @Column(name = ColumnNames.ORIGINAL_DELETION_COLUMN, nullable = false)
    public Integer getOriginalDeletion()
    {
        return originalDeletion;
    }

    public void setOriginalDeletion(Integer originalDeletion)
    {
        this.originalDeletion = originalDeletion;
    }

    @Override
    public void addMetaproject(MetaprojectPE metaprojectPE)
    {
        if (metaprojectPE == null)
        {
            throw new IllegalArgumentException("Metaproject cannot be null");
        }
        MetaprojectAssignmentPE assignmentPE = new MetaprojectAssignmentPE();
        assignmentPE.setMetaproject(metaprojectPE);
        assignmentPE.setDataSet(this);

        getMetaprojectAssignmentsInternal().add(assignmentPE);
        metaprojectPE.getAssignmentsInternal().add(assignmentPE);
    }

    @Override
    public void removeMetaproject(MetaprojectPE metaprojectPE)
    {
        if (metaprojectPE == null)
        {
            throw new IllegalArgumentException("Metaproject cannot be null");
        }
        MetaprojectAssignmentPE assignmentPE = new MetaprojectAssignmentPE();
        assignmentPE.setMetaproject(metaprojectPE);
        assignmentPE.setDataSet(this);

        getMetaprojectAssignmentsInternal().remove(assignmentPE);
        metaprojectPE.getAssignmentsInternal().remove(assignmentPE);
    }

    @Override
    @Transient
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_METAPROJECT)
    public Set<MetaprojectPE> getMetaprojects()
    {
        Set<MetaprojectPE> metaprojects = new HashSet<MetaprojectPE>();
        for (MetaprojectAssignmentPE assignment : getMetaprojectAssignmentsInternal())
        {
            metaprojects.add(assignment.getMetaproject());
        }
        return new UnmodifiableSetDecorator<MetaprojectPE>(metaprojects);
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dataSet")
    @Fetch(FetchMode.SUBSELECT)
    private Set<MetaprojectAssignmentPE> getMetaprojectAssignmentsInternal()
    {
        return this.metaprojectAssignments;
    }

    @SuppressWarnings("unused")
    private void setMetaprojectAssignmentsInternal(
            Set<MetaprojectAssignmentPE> metaprojectAssignments)
    {
        this.metaprojectAssignments = metaprojectAssignments;
    }

}