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

import java.io.File;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentImmutable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * A generic class that represents a data set for the registration API. Can be subclassed.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSet<T extends DataSetInformation> implements IDataSet
{
    private final DataSetRegistrationDetails<T> registrationDetails;

    // A folder with either one file or one subfolder representing the data set.
    private final File dataSetFolder;

    private IExperimentImmutable experiment;

    private ISampleImmutable sampleOrNull;

    public DataSet(DataSetRegistrationDetails<T> registrationDetails, File dataSetFolder)
    {
        this.registrationDetails = registrationDetails;
        this.dataSetFolder = dataSetFolder;
    }

    public DataSetRegistrationDetails<T> getRegistrationDetails()
    {
        return registrationDetails;
    }

    public File getDataSetStagingFolder()
    {
        return dataSetFolder;
    }

    public File getDataSetContents()
    {
        File[] contents = dataSetFolder.listFiles();
        if (contents.length > 1)
        {
            throw new EnvironmentFailureException(
                    "Data set is ambiguous -- there are more than one potential candidates");
        }
        if (contents.length < 1)
        {
            throw new EnvironmentFailureException("Data set is empty");
        }

        return contents[0];
    }

    public String getDataSetCode()
    {
        return registrationDetails.getDataSetInformation().getDataSetCode();
    }

    public IExperimentImmutable getExperiment()
    {
        return experiment;
    }

    public void setExperiment(IExperimentImmutable experiment)
    {
        this.experiment = experiment;
        ExperimentImmutable exp = (ExperimentImmutable) experiment;
        setExperiment(exp.getExperiment());
    }

    public ISampleImmutable getSample()
    {
        return sampleOrNull;
    }

    public void setSample(ISampleImmutable sampleOrNull)
    {
        this.sampleOrNull = sampleOrNull;

        DataSetInformation dataSetInformation = registrationDetails.getDataSetInformation();
        if (sampleOrNull == null)
        {
            dataSetInformation.setSample(null);
            dataSetInformation.setSampleCode(null);
        } else
        {
            Sample sample = (Sample) sampleOrNull;

            dataSetInformation.setSample(sample.getSample());
            dataSetInformation.setSampleCode(sample.getSample().getCode());
            Space space = sample.getSample().getSpace();
            if (null != space)
            {
                dataSetInformation.setSpaceCode(space.getCode());
            }
            setExperiment(sample.getSample().getExperiment());
        }
    }

    public String getFileFormatType()
    {
        return registrationDetails.getFileFormatType().getCode();
    }

    public void setFileFormatType(String fileFormatTypeCode)
    {
        registrationDetails.setFileFormatType(new FileFormatType(fileFormatTypeCode));
    }

    public boolean isMeasuredData()
    {
        return registrationDetails.isMeasuredData();
    }

    public void setMeasuredData(boolean measuredData)
    {
        registrationDetails.setMeasuredData(measuredData);
    }

    public String getDataSetType()
    {
        return registrationDetails.getDataSetType().getCode();
    }

    public void setDataSetType(String dataSetTypeCode)
    {
        registrationDetails.setDataSetType(new DataSetType(dataSetTypeCode));
    }

    protected void setExperiment(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment exp)
    {
        registrationDetails.getDataSetInformation().setExperiment(exp);
        ExperimentIdentifier experimentId = ExperimentIdentifierFactory.parse(exp.getIdentifier());
        registrationDetails.getDataSetInformation().setExperimentIdentifier(experimentId);
    }
}
