/*
\ * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityWithDeletionInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIsStub;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.ITaggable;

/**
 * A DTO for external data sets.
 * 
 * @author Christian Ribeaud
 */
public abstract class ExternalData extends CodeWithRegistrationAndModificationDate<ExternalData>
        implements IEntityWithDeletionInformation, IEntityInformationHolderWithProperties,
        IIdAndCodeHolder, IPermIdHolder, IIsStub, ITaggable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /**
     * {@link Comparator} for data sets contained in a (virtual) container which uses ascending
     * order in container.
     */
    public static final Comparator<ExternalData> DATA_SET_COMPONENTS_COMPARATOR =
            new DataSetComponentsComparator();

    private boolean derived;

    private Long id;

    private Deletion deletion;

    private Experiment experiment;

    private DataSetType dataSetType;

    private Date productionDate;

    private String producerCode;

    private Collection<ExternalData> parents;

    private Long size;

    private Sample sample;

    private String sampleIdentifier;

    private String sampleCode;

    private SampleType sampleType;

    private Collection<ExternalData> children;

    private List<IEntityProperty> dataSetProperties;

    private DataStore dataStore;

    private String permlink;

    private Integer orderInContainer;

    private ContainerDataSet containerOrNull;

    private boolean storageConfirmation;

    private boolean isStub;

    private Collection<Metaproject> metaprojects;

    public ExternalData(boolean isStub)
    {
        this.isStub = isStub;
    }

    /**
     * @return true if the data set is available for viewing/editing.
     */
    public boolean isAvailable()
    {
        return true;
    }

    /**
     * @return true if this is a container data set.
     */
    public boolean isContainer()
    {
        return false; // overriden in subclasses
    }

    /**
     * @return true if this is a link data set.
     */
    public boolean isLinkData()
    {
        return false; // overriden in subclasses
    }

    /**
     * @return true if this is a place holder data set.
     */
    public boolean isPlaceHolderDataSet()
    {
        return false; // overriden in subclasses
    }

    /**
     * Will return non-null values for plain non-container data sets.
     */
    public DataSet tryGetAsDataSet()
    {
        return null; // overriden in subclasses
    }

    /**
     * Returns null if the data set is not a container data set, otherwise returns the container.
     */
    public ContainerDataSet tryGetAsContainerDataSet()
    {
        return null; // overriden in subclasses
    }

    public LinkDataSet tryGetAsLinkDataSet()
    {
        return null; // overriden in a subclasses
    }

    public String getPermlink()
    {
        return permlink;
    }

    public void setPermlink(String permlink)
    {
        this.permlink = permlink;
    }

    /** NOTE: may be NULL */
    public Sample getSample()
    {
        return sample;
    }

    public void setSample(Sample sample)
    {
        this.sample = sample;
        if (sample != null)
        {
            setSampleIdentifier(sample.getIdentifier());
            setSampleType(sample.getSampleType());
            setSampleCode(sample.getCode());
        }
    }

    /** NOTE: may be NULL */
    public final String getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    private final void setSampleIdentifier(String sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    /** NOTE: may be NULL */
    public final String getSampleCode()
    {
        return sampleCode;
    }

    private void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    /** NOTE: may be NULL */
    public final SampleType getSampleType()
    {
        return sampleType;
    }

    private final void setSampleType(SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public Collection<ExternalData> getChildren()
    {
        return children;
    }

    public void setChildren(Collection<ExternalData> children)
    {
        this.children = children;
    }

    public final boolean isDerived()
    {
        return derived;
    }

    public final void setDerived(boolean derived)
    {
        this.derived = derived;
    }

    public final DataSetType getDataSetType()
    {
        return dataSetType;
    }

    public final void setDataSetType(DataSetType dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    public final Date getProductionDate()
    {
        return productionDate;
    }

    public final void setProductionDate(Date productionDate)
    {
        this.productionDate = productionDate;
    }

    public final String getDataProducerCode()
    {
        return producerCode;
    }

    public final void setDataProducerCode(String producerCode)
    {
        this.producerCode = producerCode;
    }

    public Collection<ExternalData> getParents()
    {
        return parents;
    }

    public void setParents(Collection<ExternalData> parents)
    {
        this.parents = parents;
    }

    public Long getSize()
    {
        return size;
    }

    public void setSize(Long size)
    {
        this.size = size;
    }

    @Override
    public final Deletion getDeletion()
    {
        return deletion;
    }

    public final void setDeletion(Deletion deletion)
    {
        this.deletion = deletion;
    }

    public final Experiment getExperiment()
    {
        return experiment;
    }

    public final void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    public void setDataSetProperties(List<IEntityProperty> dataSetProperties)
    {
        this.dataSetProperties = dataSetProperties;
    }

    @Override
    public List<IEntityProperty> getProperties()
    {
        return dataSetProperties;
    }

    public final DataStore getDataStore()
    {
        return dataStore;
    }

    public final void setDataStore(DataStore dataStore)
    {
        this.dataStore = dataStore;
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    public EntityType getEntityType()
    {
        return dataSetType;
    }

    @Override
    public String getIdentifier()
    {
        return getCode();
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public String getPermId()
    {
        return getCode();
    }

    /**
     * @return the "virtual" container of this data set or null.
     */
    public ContainerDataSet tryGetContainer()
    {
        return containerOrNull;
    }

    public void setContainer(ContainerDataSet containerOrNull)
    {
        this.containerOrNull = containerOrNull;
    }

    public Integer getOrderInContainer()
    {
        return orderInContainer;
    }

    public void setOrderInContainer(Integer orderInContainer)
    {
        this.orderInContainer = orderInContainer;
    }

    public boolean isStorageConfirmation()
    {
        return storageConfirmation || isContainer();
    }

    public void setStorageConfirmation(boolean storageConfirmation)
    {
        this.storageConfirmation = storageConfirmation;
    }

    public void setMetaprojects(Collection<Metaproject> metaprojects)
    {
        this.metaprojects = metaprojects;
    }

    @Override
    public Collection<Metaproject> getMetaprojects()
    {
        return metaprojects;
    }

    public DataSetKind getDataSetKind()
    {
        return DataSetKind.PHYSICAL;
    }

    // 'transient'

    public String getSourceType()
    {
        return SourceType.create(isDerived()).name();
    }

    @Override
    public boolean isStub()
    {
        return this.isStub;
    }

    private static final class DataSetComponentsComparator implements Comparator<ExternalData>
    {
        @Override
        public int compare(ExternalData o1, ExternalData o2)
        {
            Integer order1 = o1.getOrderInContainer();
            Integer order2 = o2.getOrderInContainer();
            // sanity check
            if (order1 == null || order2 == null)
            {
                return Code.CODE_PROVIDER_COMPARATOR.compare(o1, o2);
            }
            return order1.compareTo(order2);
        }
    }
}
