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
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetImmutable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentImmutable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * A data set that has already been stored in openBIS.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetImmutable implements IDataSetImmutable
{
    protected final ExternalData dataSet;

    public DataSetImmutable(ExternalData dataSet)
    {
        this.dataSet = dataSet;
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

    public String getPropertyValue(String propertyCode)
    {
        return EntityHelper.tryFindPropertyValue(dataSet, propertyCode);
    }

    public List<String> getParentDatasets()
    {
        return Code.extractCodes(dataSet.getParents());
    }

    public boolean isContainerDataSet()
    {
        return dataSet.isContainer();
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
                result.add(new DataSetImmutable(child));
            }
        }
        return result;
    }
}
