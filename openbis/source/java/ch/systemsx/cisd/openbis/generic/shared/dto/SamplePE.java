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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
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

import ch.systemsx.cisd.common.collections.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * <i>Persistent Entity</i> object of an entity 'sample'.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.SAMPLES_TABLE)
@Check(constraints = "(" + ColumnNames.DATABASE_INSTANCE_COLUMN + " IS NOT NULL AND "
        + ColumnNames.SPACE_COLUMN + " IS NULL) OR (" + ColumnNames.DATABASE_INSTANCE_COLUMN
        + " IS NULL AND " + ColumnNames.SPACE_COLUMN + " IS NOT NULL)")
@Indexed
public class SamplePE extends AttachmentHolderPE implements IIdAndCodeHolder, Comparable<SamplePE>,
        IEntityInformationWithPropertiesHolder, IMatchingEntity, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final SamplePE[] EMPTY_ARRAY = new SamplePE[0];

    public static final List<SamplePE> EMPTY_LIST = Collections.emptyList();

    private Long id;

    private String code;

    private SampleTypePE sampleType;

    private DatabaseInstancePE databaseInstance;

    private SpacePE space;

    private SampleIdentifier sampleIdentifier;

    private SamplePE container;

    private ExperimentPE experiment;

    private String permId;

    private Set<SampleRelationshipPE> parentRelationships = new HashSet<SampleRelationshipPE>();

    private Set<SampleRelationshipPE> childRelationships = new HashSet<SampleRelationshipPE>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentSample")
    @Fetch(FetchMode.SUBSELECT)
    private Set<SampleRelationshipPE> getSampleChildRelationships()
    {
        return childRelationships;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setSampleChildRelationships(final Set<SampleRelationshipPE> childRelationships)
    {
        this.childRelationships = childRelationships;
    }

    @Transient
    public Set<SampleRelationshipPE> getChildRelationships()
    {

        return new UnmodifiableSetDecorator<SampleRelationshipPE>(getSampleChildRelationships());
    }

    /**
     * Returns <code>true</code>, if and only if the relationships have been initialized.
     */
    @Transient
    public boolean isChildRelationshipsInitialized()
    {
        return HibernateUtils.isInitialized(getSampleChildRelationships());
    }

    public void addChildRelationship(final SampleRelationshipPE relationship)
    {
        relationship.setParentSample(this);
        getSampleChildRelationships().add(relationship);
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "childSample")
    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @Fetch(FetchMode.SUBSELECT)
    private Set<SampleRelationshipPE> getSampleParentRelationships()
    {
        return parentRelationships;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setSampleParentRelationships(final Set<SampleRelationshipPE> parentRelationships)
    {
        this.parentRelationships = parentRelationships;
    }

    @Transient
    public Set<SampleRelationshipPE> getParentRelationships()
    {
        return new UnmodifiableSetDecorator<SampleRelationshipPE>(getSampleParentRelationships());
    }

    /**
     * Returns <code>true</code>, if and only if the relationships have been initialized.
     */
    @Transient
    public boolean isParentRelationshipsInitialized()
    {
        return HibernateUtils.isInitialized(getSampleParentRelationships());
    }

    public void setParentRelationships(final Set<SampleRelationshipPE> parentRelationships)
    {
        getSampleParentRelationships().clear();
        for (final SampleRelationshipPE sampleRelationship : parentRelationships)
        {
            final SamplePE parent = sampleRelationship.getChildSample();
            if (parent != null)
            {
                parent.getSampleParentRelationships().remove(sampleRelationship);
            }
            addParentRelationship(sampleRelationship);
        }
    }

    public void addParentRelationship(final SampleRelationshipPE relationship)
    {
        relationship.setChildSample(this);
        getSampleParentRelationships().add(relationship);
    }

    public void removeParentRelationship(final SampleRelationshipPE relationship)
    {
        getSampleParentRelationships().remove(relationship);
        relationship.getParentSample().getSampleChildRelationships().remove(relationship);
        relationship.setChildSample(null);
        relationship.setParentSample(null);
    }

    /**
     * Invalidation information.
     * <p>
     * If not <code>null</code>, then this sample is considered as <i>invalid</i>.
     * </p>
     */
    private InvalidationPE invalidation;

    private Set<SamplePropertyPE> properties = new HashSet<SamplePropertyPE>();

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

    private Set<DataPE> datasets = new HashSet<DataPE>();

    // bidirectional connection SamplePE-DataPE

    public void removeDataSet(DataPE dataset)
    {
        getDatasetsInternal().remove(dataset);
        dataset.setSampleInternal(null);
    }

    public void addDataSet(DataPE dataset)
    {
        SamplePE sample = dataset.tryGetSample();
        if (sample != null)
        {
            sample.getDatasetsInternal().remove(dataset);
        }
        dataset.setSampleInternal(this);
        getDatasetsInternal().add(dataset);
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sampleInternal")
    private Set<DataPE> getDatasetsInternal()
    {
        return datasets;
    }

    // hibernate only
    @SuppressWarnings("unused")
    private void setDatasetsInternal(Set<DataPE> datasets)
    {
        this.datasets = datasets;
    }

    @Transient
    public Set<DataPE> getDatasets()
    {
        return new UnmodifiableSetDecorator<DataPE>(getDatasetsInternal());
    }

    // --------------------

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

    @Transient
    public SampleIdentifier getSampleIdentifier()
    {
        if (sampleIdentifier == null)
        {
            sampleIdentifier = IdentifierHelper.createSampleIdentifier(this);
        }
        return sampleIdentifier;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATABASE_INSTANCE_COLUMN, updatable = true)
    public DatabaseInstancePE getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.SPACE_COLUMN, updatable = true)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_SPACE)
    public SpacePE getSpace()
    {
        return space;
    }

    public void setSpace(final SpacePE space)
    {
        this.space = space;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.SAMPLE_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.SAMPLE_TYPE_COLUMN, updatable = false)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_ENTITY_TYPE)
    public SampleTypePE getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(final SampleTypePE sampleType)
    {
        this.sampleType = sampleType;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "entity")
    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_PROPERTIES)
    @Fetch(FetchMode.SUBSELECT)
    private Set<SamplePropertyPE> getSampleProperties()
    {
        return properties;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setSampleProperties(final Set<SamplePropertyPE> properties)
    {
        this.properties = properties;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PART_OF_SAMPLE_COLUMN, updatable = true)
    public SamplePE getContainer()
    {
        return container;
    }

    public void setContainer(final SamplePE container)
    {
        this.container = container;
        // identifier should be reevaluated after change of container
        this.sampleIdentifier = null;
    }

    @Transient
    public SamplePE getTop()
    {
        // traverse through parent relationship graph and stops on first sample that doesn't
        // have parents or has more than one parent
        final List<SamplePE> parents = getParents();
        if (parents.size() == 1)
        {
            return parents.get(0).getTop();
        }
        return this;
    }

    @Transient
    public SamplePE getGeneratedFrom()
    {
        final List<SamplePE> parents = getParents();
        if (parents.size() == 0)
        {
            return null;
        }
        if (parents.size() > 1)
        {
            throw new IllegalStateException("Sample " + getIdentifier()
                    + " has more than one parent");
        }
        return parents.get(0);
    }

    @Transient
    public List<SamplePE> getParents()
    {
        final Set<SampleRelationshipPE> relationships = getParentRelationships();
        final List<SamplePE> parents = new ArrayList<SamplePE>();
        for (SampleRelationshipPE r : relationships)
        {
            assert r.getChildSample().equals(this);
            if (r.getRelationship().getCode()
                    .equals(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP))
            {
                parents.add(r.getParentSample());
            }
        }
        return parents;
    }

    public void setExperiment(final ExperimentPE experiment)
    {
        if (experiment != null)
        {
            experiment.addSample(this);
        } else
        {
            ExperimentPE previousExperiment = getExperiment();
            if (previousExperiment != null)
            {
                previousExperiment.removeSample(this);
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
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN, updatable = true)
    private ExperimentPE getExperimentInternal()
    {
        return experiment;
    }

    //
    // IIdAndCodeHolder
    //

    @SequenceGenerator(name = SequenceNames.SAMPLE_SEQUENCE, sequenceName = SequenceNames.SAMPLE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SAMPLE_SEQUENCE)
    @DocumentId
    public final Long getId()
    {
        return id;
    }

    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public String getCode()
    {
        return code;
    }

    // used only by Hibernate Search
    @SuppressWarnings("unused")
    @Transient
    @Field(index = Index.TOKENIZED, store = Store.YES, name = SearchFieldConstants.CODE)
    private String getFullCode()
    {
        // full code of contained sample consists of <container code>:<contained sample code>
        return (getContainer() != null ? getContainer().getCode() + ":" : "") + getCode();
    }

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

    //
    // IRegistratorHolder
    //

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

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getCode(), "code");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SamplePE == false)
        {
            return false;
        }
        final SamplePE that = (SamplePE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        builder.append(getDatabaseInstance(), that.getDatabaseInstance());
        builder.append(getSpace(), that.getSpace());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        builder.append(getDatabaseInstance());
        builder.append(getSpace());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        builder.append("sampleType", getSampleType());
        return builder.toString();
    }

    //
    // Compare
    //

    public final int compareTo(final SamplePE o)
    {
        return getSampleIdentifier().compareTo(o.getSampleIdentifier());
    }

    //
    // IEntityPropertiesHolder
    //

    @Transient
    public Set<SamplePropertyPE> getProperties()
    {
        return new UnmodifiableSetDecorator<SamplePropertyPE>(getSampleProperties());
    }

    /**
     * Returns <code>true</code>, if and only if the properties have been initialized.
     */
    @Transient
    public boolean isPropertiesInitialized()
    {
        return HibernateUtils.isInitialized(getSampleProperties());
    }

    public void setProperties(final Set<? extends EntityPropertyPE> properties)
    {
        getSampleProperties().clear();
        for (final EntityPropertyPE untypedProperty : properties)
        {
            SamplePropertyPE sampleProperty = (SamplePropertyPE) untypedProperty;
            final SamplePE parent = sampleProperty.getEntity();
            if (parent != null)
            {
                parent.getSampleProperties().remove(sampleProperty);
            }
            addProperty(sampleProperty);
        }
    }

    public void addProperty(final EntityPropertyPE property)
    {
        property.setEntity(this);
        getSampleProperties().add((SamplePropertyPE) property);
    }

    public void removeProperty(final EntityPropertyPE property)
    {
        getSampleProperties().remove(property);
        property.setEntity(null);
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

    //
    // IMatchingEntity
    //

    @Transient
    @Field(index = Index.NO, store = Store.YES, name = SearchFieldConstants.IDENTIFIER)
    public final String getIdentifier()
    {
        return getSampleIdentifier().toString();
    }

    @Transient
    public final EntityTypePE getEntityType()
    {
        return getSampleType();
    }

    @Transient
    public final EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    @Transient
    public AttachmentHolderKind getAttachmentHolderKind()
    {
        return AttachmentHolderKind.SAMPLE;
    }

    @Override
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sampleParentInternal")
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_ATTACHMENT)
    @Fetch(FetchMode.SUBSELECT)
    protected Set<AttachmentPE> getInternalAttachments()
    {
        return attachments;
    }

    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Column(name = ColumnNames.PERM_ID_COLUMN, nullable = false)
    @Field(index = Index.NO, store = Store.YES, name = SearchFieldConstants.PERM_ID)
    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    //
    // connected samples for use only in tests (no bidirectional support for connection)
    //

    /** children of container hierarchy - added only to simplify testing */
    private List<SamplePE> contained = new ArrayList<SamplePE>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "container")
    public List<SamplePE> getContained()
    {
        return contained;
    }

    @SuppressWarnings("unused")
    private void setContained(List<SamplePE> contained)
    {
        this.contained = contained;
    }

    @Transient
    public List<SamplePE> getGenerated()
    {
        Set<SampleRelationshipPE> relationships = getChildRelationships();
        List<SamplePE> samples = new ArrayList<SamplePE>();
        for (SampleRelationshipPE r : relationships)
        {
            assert r.getParentSample().equals(this);
            if (r.getRelationship().getCode()
                    .equals(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP))
            {
                samples.add(r.getChildSample());
            }
        }
        return samples;
    }

    @Transient
    public Map<RelationshipTypePE, Set<SamplePE>> getParentsMap()
    {
        Map<RelationshipTypePE, Set<SamplePE>> map =
                new HashMap<RelationshipTypePE, Set<SamplePE>>();
        for (SampleRelationshipPE r : getParentRelationships())
        {
            RelationshipTypePE type = r.getRelationship();
            if (map.get(type) == null)
            {
                map.put(type, new HashSet<SamplePE>());
            }
            map.get(type).add(r.getParentSample());
        }
        return map;
    }

}
