/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedBasicOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * A data set that has already been stored in openBIS.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetImmutable extends AbstractDataSetImmutable
{
    protected final ExternalData dataSet;

    public DataSetImmutable(ExternalData dataSet, IEncapsulatedBasicOpenBISService service)
    {
        super(service);
        this.dataSet = dataSet;
    }

    @Override
    public String getDataSetCode()
    {
        return dataSet.getCode();
    }

    @Override
    public IObjectId getEntityId()
    {
        return new DataSetCodeId(getDataSetCode());
    }

    @Override
    public IExperimentImmutable getExperiment()
    {
        return new ExperimentImmutable(dataSet.getExperiment());
    }

    @Override
    public ISampleImmutable getSample()
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample = dataSet.getSample();
        if (sample == null)
        {
            return null;
        } else
        {
            return new SampleImmutable(sample);
        }
    }

    @Override
    public String getFileFormatType()
    {
        if (isContainerDataSet())
        {
            return null;
        } else
        {
            return dataSet.tryGetAsDataSet().getFileFormatType().getCode();
        }
    }

    @Override
    public boolean isMeasuredData()
    {
        return dataSet.isDerived() == false;
    }

    @Override
    public int getSpeedHint()
    {
        if (isContainerDataSet())
        {
            return Integer.MIN_VALUE;
        } else
        {
            return dataSet.tryGetAsDataSet().getSpeedHint();
        }
    }

    @Override
    public String getDataSetType()
    {
        return dataSet.getDataSetType().getCode();
    }

    @Override
    public DataSetType getDataSetTypeWithPropertyTypes()
    {
        return getDataSetTypeWithPropertyTypes(getDataSetType());
    }

    @Override
    public String getPropertyValue(String propertyCode)
    {
        return EntityHelper.tryFindPropertyValue(dataSet, propertyCode);
    }

    @Override
    public List<String> getAllPropertyCodes()
    {
        List<IEntityProperty> properties = dataSet.getProperties();
        List<String> codes = new ArrayList<String>();
        for (IEntityProperty property : properties)
        {
            codes.add(property.getPropertyType().getCode());
        }
        return codes;
    }

    @Override
    public List<String> getParentDatasets()
    {
        return Code.extractCodes(dataSet.getParents());
    }

    @Override
    public boolean isContainerDataSet()
    {
        return dataSet.isContainer();
    }

    @Override
    public boolean isContainedDataSet()
    {
        return dataSet.tryGetContainer() != null;
    }

    @Override
    public String getContainerDataSet()
    {
        ContainerDataSet container = dataSet.tryGetContainer();
        if (container != null)
        {
            return container.getCode();
        } else
        {
            return null;
        }
    }

    @Override
    public List<String> getContainedDataSetCodes()
    {
        if (isContainerDataSet())
        {
            return Code.extractCodes(dataSet.tryGetAsContainerDataSet().getContainedDataSets());
        } else
        {
            return Collections.emptyList();
        }
    }

    @Override
    public List<IDataSetImmutable> getChildrenDataSets()
    {
        List<IDataSetImmutable> result = new ArrayList<IDataSetImmutable>();
        Collection<ExternalData> children = dataSet.getChildren();
        if (children != null)
        {
            for (ExternalData child : children)
            {
                result.add(new DataSetImmutable(child, service));
            }
        }
        return result;
    }

    @Override
    public boolean isLinkDataSet()
    {
        return dataSet.tryGetAsLinkDataSet() != null;
    }

    @Override
    public IExternalDataManagementSystemImmutable getExternalDataManagementSystem()
    {
        ExternalDataManagementSystem externalDMS =
                dataSet.tryGetAsLinkDataSet().getExternalDataManagementSystem();
        if (externalDMS != null)
        {
            return new ExternalDataManagementSystemImmutable(externalDMS);
        } else
        {
            return null;
        }
    }

    @Override
    public String getExternalCode()
    {
        return dataSet.tryGetAsLinkDataSet().getExternalCode();
    }

    @Override
    public boolean isNoFileDataSet()
    {
        return isContainerDataSet() || isLinkDataSet();
    }
}
