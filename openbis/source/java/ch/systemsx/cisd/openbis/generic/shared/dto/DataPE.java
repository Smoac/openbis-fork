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

import java.util.Date;
import java.util.HashSet;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collections.UnmodifiableSetDecorator;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

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
@Table(name = TableNames.DATA_TABLE, uniqueConstraints = @UniqueConstraint(columnNames = ColumnNames.CODE_COLUMN))
@Check(constraints = "(" + ColumnNames.SAMPLE_ACQUIRED_FROM + " IS NOT NULL AND "
        + ColumnNames.SAMPLE_DERIVED_FROM + " IS NULL) OR (" + ColumnNames.SAMPLE_ACQUIRED_FROM
        + " IS NULL AND " + ColumnNames.SAMPLE_DERIVED_FROM + " IS NOT NULL)")
@Inheritance(strategy = InheritanceType.JOINED)
@Friend(toClasses = EventPE.class)
public class DataPE extends AbstractIdAndCodeHolder<DataPE> implements
        IEntityPropertiesHolder<DataSetPropertyPE>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final DataPE[] EMPTY_ARRAY = new DataPE[0];

    private transient Long id;

    private String code;

    private boolean placeholder;

    private boolean deleted;

    /** Registration date of the database instance. */
    private Date registrationDate;

    private DataSetTypePE dataSetType;

    private ExperimentPE experiment;

    private Date productionDate;

    private Date modificationDate;

    private String dataProducerCode;

    private SamplePE sampleAcquiredFrom;

    private SamplePE sampleDerivedFrom;

    private Set<DataPE> parents = new HashSet<DataPE>();

    private Set<EventPE> events = new HashSet<EventPE>();

    private DataStorePE dataStore;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.DATA_STORE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATA_STORE_COLUMN, updatable = false)
    public final DataStorePE getDataStore()
    {
        return dataStore;
    }

    public final void setDataStore(final DataStorePE dataStorePE)
    {
        this.dataStore = dataStorePE;
    }

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false)
    @Generated(GenerationTime.ALWAYS)
    public Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.DATA_SET_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATA_SET_TYPE_COLUMN)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_DATASET_TYPE)
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

    @Column(name = ColumnNames.IS_DELETED_COLUMN)
    @Field(index = Index.UN_TOKENIZED, store = Store.YES, name = SearchFieldConstants.DELETED)
    public boolean isDeleted()
    {
        return deleted;
    }

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

    // bidirectional connections SamplePE-DataPE

    public void setSampleDerivedFrom(final SamplePE sampleDerivedFrom)
    {
        if (sampleDerivedFrom != null)
        {
            sampleDerivedFrom.addDerivedDataSet(this);
        } else
        {
            SamplePE previousSample = getSampleDerivedFrom();
            if (previousSample != null)
            {
                previousSample.removeDerivedDataSet(this);
            }
        }
    }

    public void setSampleAcquiredFrom(final SamplePE sampleAcquiredFrom)
    {
        if (sampleAcquiredFrom != null)
        {
            sampleAcquiredFrom.addAcquiredDataSet(this);
        } else
        {
            SamplePE previousSample = getSampleAcquiredFrom();
            if (previousSample != null)
            {
                previousSample.removeAcquiredDataSet(this);
            }
        }
    }

    @Transient
    public SamplePE getSampleAcquiredFrom()
    {
        return getSampleAcquiredFromInternal();
    }

    @Transient
    public SamplePE getSampleDerivedFrom()
    {
        return getSampleDerivedFromInternal();
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.SAMPLE_ACQUIRED_FROM)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_SAMPLE)
    private SamplePE getSampleAcquiredFromInternal()
    {
        return sampleAcquiredFrom;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.SAMPLE_DERIVED_FROM)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_SAMPLE)
    private SamplePE getSampleDerivedFromInternal()
    {
        return sampleDerivedFrom;
    }

    // TODO 2009-04-28, Tomasz Pylak: make @Private
    void setSampleAcquiredFromInternal(final SamplePE sampleAcquiredFrom)
    {
        this.sampleAcquiredFrom = sampleAcquiredFrom;
    }

    // TODO 2009-04-28, Tomasz Pylak: make @Private
    void setSampleDerivedFromInternal(final SamplePE sampleDerivedFrom)
    {
        this.sampleDerivedFrom = sampleDerivedFrom;
    }

    @Transient
    public SamplePE getAssociatedSample()
    {
        return sampleAcquiredFrom == null ? sampleDerivedFrom : sampleAcquiredFrom;
    }

    @Transient
    public String getAssociatedSampleCode()
    {
        final SamplePE sample = getAssociatedSample();
        return sample != null ? sample.getCode() : null;
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
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
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
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

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

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = TableNames.DATA_SET_RELATIONSHIPS_TABLE, joinColumns = @JoinColumn(name = ColumnNames.DATA_CHILD_COLUMN), inverseJoinColumns = @JoinColumn(name = ColumnNames.DATA_PARENT_COLUMN))
    public Set<DataPE> getParents()
    {
        return parents;
    }

    public void setParents(final Set<DataPE> parents)
    {
        this.parents = parents;
    }

    //
    // AbstractIdAndCodeHolder
    //

    @Id
    @SequenceGenerator(name = SequenceNames.DATA_SEQUENCE, sequenceName = SequenceNames.DATA_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.DATA_SEQUENCE)
    @DocumentId
    public Long getId()
    {
        return id;
    }

    @Column(unique = true)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
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
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_EXPERIMENT)
    private ExperimentPE getExperimentInternal()
    {
        return experiment;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "dataInternal", cascade = CascadeType.ALL)
    private final Set<EventPE> getEventsInternal()
    {
        return events;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private final void setEventsInternal(final Set<EventPE> events)
    {
        this.events = events;
    }

    public final void setEvents(final Set<EventPE> events)
    {
        getEventsInternal().clear();
        for (final EventPE child : events)
        {
            addEvent(child);
        }
    }

    @Transient
    public final Set<EventPE> getEvents()
    {
        return new UnmodifiableSetDecorator<EventPE>(getEventsInternal());
    }

    public void addEvent(final EventPE event)
    {
        final DataPE data = event.getData();
        if (data != null)
        {
            data.removeEvent(event);
        }
        event.setDataInternal(this);
        getEventsInternal().add(event);
    }

    public void removeEvent(final EventPE event)
    {
        assert event != null : "Unspecified event.";
        getEventsInternal().remove(event);
        event.setDataInternal(null);
    }

    private Set<DataSetPropertyPE> properties = new HashSet<DataSetPropertyPE>();

    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_PROPERTIES)
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

    public void addProperty(final DataSetPropertyPE property)
    {
        property.setEntity(this);
        getDataSetProperties().add(property);
    }

    public void setProperties(final Set<DataSetPropertyPE> properties)
    {
        getDataSetProperties().clear();
        for (final DataSetPropertyPE property : properties)
        {
            final DataPE parent = property.getDataSet();
            if (parent != null)
            {
                parent.getDataSetProperties().remove(property);
            }
            addProperty(property);
        }
    }

    @Transient
    public Set<DataSetPropertyPE> getProperties()
    {
        return new UnmodifiableSetDecorator<DataSetPropertyPE>(getDataSetProperties());
    }

}
