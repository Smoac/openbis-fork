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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
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

    private final Set<String> dynamicPropertiesCodes;
    
    public DataSetImmutable(ExternalData dataSet, IEncapsulatedOpenBISService service)
    {
        super(service);
        this.dataSet = dataSet;
        
        dynamicPropertiesCodes = new HashSet<String>();
        for (DataSetTypePropertyType pt : dataSet.getDataSetType().getAssignedPropertyTypes())
        {
            if (pt.isDynamic())
            {
                dynamicPropertiesCodes.add(pt.getPropertyType().getCode());
            }
        }
    }

    protected boolean isDynamicProperty(String code)
    {
        return dynamicPropertiesCodes.contains(code);
    }

    public String getDataSetCode()
    {
        return dataSet.getCode();
    }

    public IExperimentImmutable getExperiment()
    {
        return new ExperimentImmutable(dataSet.getExperiment());
    }

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

    public boolean isMeasuredData()
    {
        return dataSet.isDerived() == false;
    }

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

    public String getDataSetType()
    {
        return dataSet.getDataSetType().getCode();
    }

    public DataSetType getDataSetTypeWithPropertyTypes()
    {
        return getDataSetTypeWithPropertyTypes(getDataSetType());
    }

    public String getPropertyValue(String propertyCode)
    {
        return EntityHelper.tryFindPropertyValue(dataSet, propertyCode);
    }

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

    public List<String> getParentDatasets()
    {
        return Code.extractCodes(dataSet.getParents());
    }

    public boolean isContainerDataSet()
    {
        return dataSet.isContainer();
    }

    public boolean isContainedDataSet()
    {
        return dataSet.tryGetContainer() != null;
    }

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

    public List<IDataSetImmutable> getChildrenDataSets()
    {
        List<IDataSetImmutable> result = new ArrayList<IDataSetImmutable>();
        List<ExternalData> children = dataSet.getChildren();
        if (children != null)
        {
            for (ExternalData child : children)
            {
                result.add(new DataSetImmutable(child, service));
            }
        }
        return result;
    }
}
