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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
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
import ch.systemsx.cisd.openbis.generic.shared.IServer;
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
@Table(name = TableNames.DATA_TABLE, uniqueConstraints = @UniqueConstraint(columnNames = ColumnNames.CODE_COLUMN))
@Inheritance(strategy = InheritanceType.JOINED)
@Friend(toClasses = EventPE.class)
public class DataPE extends AbstractIdAndCodeHolder<DataPE> implements IEntityPropertiesHolder,
        IEntityInformationHolderDTO
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final DataPE[] EMPTY_ARRAY = new DataPE[0];

    private transient Long id;

    private String code;

    private boolean placeholder;

    private boolean isDerived;

    /** Registration date of the database instance. */
    private Date registrationDate;

    private DataSetTypePE dataSetType;

    private ExperimentPE experiment;

    private SamplePE sample;

    private Date productionDate;

    private Date modificationDate;

    private String dataProducerCode;

    private Set<DataPE> parents = new HashSet<DataPE>();

    private Set<DataPE> children = new HashSet<DataPE>();

    private DataStorePE dataStore;

    @ManyToOne(fetch = FetchType.EAGER)
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

    @ManyToOne(fetch = FetchType.EAGER)
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

    // bidirectional connection children-parents

    // we use cascade PERSIST, not ALL because we don't REMOVE parent when we delete a child
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = TableNames.DATA_SET_RELATIONSHIPS_TABLE, joinColumns = @JoinColumn(name = ColumnNames.DATA_CHILD_COLUMN), inverseJoinColumns = @JoinColumn(name = ColumnNames.DATA_PARENT_COLUMN))
    public Set<DataPE> getParents()
    {
        return parents;
    }

    @SuppressWarnings("unused")
    private void setParents(final Set<DataPE> parents)
    {
        this.parents = parents;
    }

    /** adds connection with specified parent */
    public void addParent(final DataPE parent)
    {
        assert parent != null;
        this.parents.add(parent);
        parent.addChild(this);
    }

    /** removes connection with specified parent */
    public void removeParent(final DataPE parent)
    {
        assert parent != null;
        this.parents.remove(parent);
        parent.removeChild(this);
    }

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "parents")
    public Set<DataPE> getChildren()
    {
        return children;
    }

    @SuppressWarnings("unused")
    private void setChildren(final Set<DataPE> children)
    {
        this.children = children;
    }

    private void addChild(final DataPE child)
    {
        this.children.add(child);
    }

    private void removeChild(final DataPE child)
    {
        this.children.remove(child);
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
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
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
    private ExperimentPE getExperimentInternal()
    {
        return experiment;
    }

    private Set<DataSetPropertyPE> properties = new HashSet<DataSetPropertyPE>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity")
    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_PROPERTIES)
    @Fetch(FetchMode.SUBSELECT)
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

    public void addProperty(final EntityPropertyPE property)
    {
        property.setEntity(this);
        getDataSetProperties().add((DataSetPropertyPE) property);
    }

    public void removeProperty(final EntityPropertyPE property)
    {
        getDataSetProperties().remove(property);
        property.setEntity(null);
    }

    @Transient
    public Set<DataSetPropertyPE> getProperties()
    {
        return new UnmodifiableSetDecorator<DataSetPropertyPE>(getDataSetProperties());
    }

    /**
     * Returns <code>true</code>, if and only if the properties have been initialized.
     */
    @Transient
    public boolean isPropertiesInitialized()
    {
        return HibernateUtils.isInitialized(getDataSetProperties());
    }

    @Transient
    public EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Transient
    public EntityTypePE getEntityType()
    {
        return getDataSetType();
    }

    @Transient
    public String getIdentifier()
    {
        return getCode();
    }
}
