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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * The <i>GWT</i> equivalent to {@link ExternalDataPE}.
 * 
 * @author Christian Ribeaud
 */
public class ExternalData extends CodeWithRegistration<ExternalData> implements
        IInvalidationProvider
{
    private boolean derived;

    private Boolean complete;

    private Invalidation invalidation;
    
    private Experiment experiment;

    private DataSetType dataSetType;

    private Date productionDate;

    private String producerCode;

    private String parentCode;

    private String location;

    private FileFormatType fileFormatType;

    private LocatorType locatorType;

    private String sampleIdentifier;

    private String sampleCode;

    private SampleType sampleType;

    private List<SampleProperty> sampleProperties;

    private List<DataSetProperty> dataSetProperties;
    
    private DataStore dataStore;

    public final String getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public final String getSampleCode()
    {
        return sampleCode;
    }

    public final void setSampleIdentifier(String sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    public final SampleType getSampleType()
    {
        return sampleType;
    }

    public final void setSampleType(SampleType sampleType)
    {
        this.sampleType = sampleType;
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

    public final String getDataProducerCode()
    {
        return producerCode;
    }

    public final void setDataProducerCode(String producerCode)
    {
        this.producerCode = producerCode;
    }

    public final String getParentCode()
    {
        return parentCode;
    }

    public final void setParentCode(String parentCode)
    {
        this.parentCode = parentCode;
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

    public void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public List<SampleProperty> getSampleProperties()
    {
        return sampleProperties;
    }

    public void setSampleProperties(List<SampleProperty> sampleProperties)
    {
        this.sampleProperties = sampleProperties;
    }

    public void setDataSetProperties(List<DataSetProperty> dataSetProperties)
    {
        this.dataSetProperties = dataSetProperties;
    }

    public List<DataSetProperty> getDataSetProperties()
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
}
