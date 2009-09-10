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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.IInvalidationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;

/**
 * A DTO for external data sets.
 * 
 * @author Christian Ribeaud
 */
public class ExternalData extends CodeWithRegistration<ExternalData> implements
        IInvalidationProvider, IEntityInformationHolder, IEntityPropertiesHolder, IIdentifiable,
        IPermIdHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private boolean derived;

    private Boolean complete;

    private Long id;

    private Invalidation invalidation;

    private Experiment experiment;

    private DataSetType dataSetType;

    private Date productionDate;

    private Date modificationDate;

    private String producerCode;

    private Collection<ExternalData> parents;

    private String location;

    private FileFormatType fileFormatType;

    private LocatorType locatorType;

    private Sample sample;

    private String sampleIdentifier;

    private String sampleCode;

    private SampleType sampleType;

    private List<ExternalData> children;

    private List<IEntityProperty> dataSetProperties;

    private DataStore dataStore;

    private String permlink;

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

    public final Boolean getComplete()
    {
        return complete;
    }

    public final void setComplete(Boolean complete)
    {
        this.complete = complete;
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

    public ExternalData getParent()
    {
        if (parents == null)
        {
            return null;
        }
        final Iterator<ExternalData> it = parents.iterator();
        if (it.hasNext())
        {
            return parents.iterator().next();
        } else
        {
            return null;
        }
    }

    public Collection<ExternalData> getParents()
    {
        return parents;
    }

    public void setParents(Collection<ExternalData> parents)
    {
        this.parents = parents;
    }

    public final String getParentCode()
    {
        final ExternalData parent = getParent();
        return (parent == null) ? null : parent.getCode();
    }

    public final String getLocation()
    {
        return location;
    }

    public final void setLocation(final String location)
    {
        this.location = location;
    }

    public final FileFormatType getFileFormatType()
    {
        return fileFormatType;
    }

    public final void setFileFormatType(final FileFormatType fileFormatType)
    {
        this.fileFormatType = fileFormatType;
    }

    public final LocatorType getLocatorType()
    {
        return locatorType;
    }

    public final void setLocatorType(final LocatorType locatorType)
    {
        this.locatorType = locatorType;
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

    // 'transient'

    public String getSourceType()
    {
        return SourceType.create(isDerived()).name();
    }

    public static List<String> extractCodes(List<ExternalData> datasets)
    {
        List<String> codes = new ArrayList<String>();
        for (ExternalData dataset : datasets)
        {
            codes.add(dataset.getCode());
        }
        return codes;
    }
}
