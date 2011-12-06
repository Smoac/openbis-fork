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
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetUpdatable;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PlaceholderDataSet;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * A data set already existing in the openBIS database, that is changed as part of the registration
 * process of another data set.
 * 
 * @author Kaloyan Enimanev
 */
public class DataSetUpdatable extends DataSetImmutable implements IDataSetUpdatable
{
    public DataSetUpdatable(ExternalData dataSet, IEncapsulatedOpenBISService service)
    {
        super(dataSet, service);
        if (dataSet.getProperties() == null)
        {
            dataSet.setDataSetProperties(new ArrayList<IEntityProperty>());
        }
    }

    public void setExperiment(IExperimentImmutable experiment)
    {
        if (experiment == null)
        {
            dataSet.setExperiment(null);
        } else
        {
            ExperimentImmutable exp = (ExperimentImmutable) experiment;
            dataSet.setExperiment(exp.getExperiment());
        }
    }

    public void setSample(ISampleImmutable sampleOrNull)
    {
        if (sampleOrNull == null)
        {
            dataSet.setSample(null);
        } else
        {
            SampleImmutable samp = (SampleImmutable) sampleOrNull;
            dataSet.setSample(samp.getSample());
            setExperiment(sampleOrNull.getExperiment());
        }
    }

    public void setFileFormatType(String fileFormatTypeCode)
    {
        if (isContainerDataSet())
        {
            // ignore
        } else
        {
            FileFormatType fileFormatType = new FileFormatType();
            fileFormatType.setCode(fileFormatTypeCode);
            dataSet.tryGetAsDataSet().setFileFormatType(fileFormatType);
        }
    }

    public void setPropertyValue(String propertyCode, String propertyValue)
    {
        EntityHelper.createOrUpdateProperty(dataSet, propertyCode, propertyValue);
    }

    public void setParentDatasets(List<String> parentDatasetCodes)
    {
        List<ExternalData> dummyParents = createDummyDataSetsFromCodes(parentDatasetCodes);
        dataSet.setParents(dummyParents);
    }

    public void setContainedDataSetCodes(List<String> containedDataSetCodes)
    {
        if (isContainerDataSet())
        {
            List<ExternalData> dummyDataSets = createDummyDataSetsFromCodes(containedDataSetCodes);
            dataSet.tryGetAsContainerDataSet().setContainedDataSets(dummyDataSets);
        } else
        {
            // ignored
        }
    }

    private List<ExternalData> createDummyDataSetsFromCodes(List<String> containedDataSetCodes)
    {
        List<ExternalData> dummies = new ArrayList<ExternalData>();
        if (containedDataSetCodes != null)
        {
            for (String code : containedDataSetCodes)
            {
                PlaceholderDataSet dummy = new PlaceholderDataSet();
                dummy.setCode(code);
                dummies.add(dummy);
            }
        }
        return dummies;
    }

    /**
     * Only visible to internal implementation classes. Not part of the public interface.
     */
    public ExternalData getExternalData()
    {
        return dataSet;
    }

}
