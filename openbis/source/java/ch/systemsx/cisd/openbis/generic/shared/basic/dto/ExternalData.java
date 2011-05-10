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
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IInvalidationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;

/**
 * A DTO for external data sets.
 * 
 * @author Christian Ribeaud
 */
public class ExternalData extends CodeWithRegistration<ExternalData> implements
        IInvalidationProvider, IEntityInformationHolderWithProperties, IIdAndCodeHolder,
        IPermIdHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private boolean derived;

    private Long id;

    private Invalidation invalidation;

    private Experiment experiment;

    private DataSetType dataSetType;

    private Date productionDate;

    private Date modificationDate;

    private String producerCode;

    private Collection<ExternalData> parents;

    // TODO KE: 2011-05-06 Implement a logic in ContainerDataSets to calculate the size
    private Long size;

    private Sample sample;

    private String sampleIdentifier;

    private String sampleCode;

    private SampleType sampleType;

    private List<ExternalData> children;

    private List<IEntityProperty> dataSetProperties;

    private DataStore dataStore;

    private String permlink;

    private Integer orderInContainer;

    private ContainerDataSet containerOrNull;

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
    public boolean isContainerDataSet()
    {
        return dataSetType != null && dataSetType.isContainerType();
    }

    /**
     * Tries to cast the current object to {@link DataSet}. Will return non-null values for plain
     * non-container data sets.
     */
    public DataSet tryGetAsDataSet()
    {
        if (this instanceof DataSet)
        {
            return (DataSet) this;
        }
        return null;
    }

    /**
     * Tries to cast the current object to {@link ContainerDataSet}. Returns null if the data set is
     * not a container data set.
     */
    public ContainerDataSet tryGetAsContainerDataSet()
    {
        if (this instanceof ContainerDataSet)
        {
            return (ContainerDataSet) this;
        }
        return null;
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

    public List<ExternalData> getChildren()
    {
        return children;
    }

    public void setChildren(List<ExternalData> children)
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

    @Deprecated
    public DataSetArchivingStatus getStatus()
    {
        return null;
    }

    @Deprecated
    public void setStatus(DataSetArchivingStatus status)
    {
    }

    @Deprecated
    public int getSpeedHint()
    {
        return 0;
    }

    @Deprecated
    public void setSpeedHint(int speedHint)
    {
    }

    @Deprecated
    public Boolean getComplete()
    {
        return null;
    }

    @Deprecated
    public void setComplete(Boolean complete)
    {
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

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
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

    @Deprecated
    public String getLocation()
    {
        return null;
    }

    @Deprecated
    public void setLocation(final String location)
    {
    }

    @Deprecated
    public FileFormatType getFileFormatType()
    {
        return null;
    }

    @Deprecated
    public void setFileFormatType(final FileFormatType fileFormatType)
    {
    }

    @Deprecated
    public LocatorType getLocatorType()
    {
        return null;
    }

    @Deprecated
    public void setLocatorType(final LocatorType locatorType)
    {
    }

    public final Invalidation getInvalidation()
    {
        return invalidation;
    }

    public final void setInvalidation(Invalidation invalidation)
    {
        this.invalidation = invalidation;
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

    public EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    public EntityType getEntityType()
    {
        return dataSetType;
    }

    public String getIdentifier()
    {
        return getCode();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getPermId()
    {
        return getCode();
    }

    /**
     * @return the "parent" container of this data set or null.
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

    // 'transient'

    public String getSourceType()
    {
        return SourceType.create(isDerived()).name();
    }

}
