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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;

import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationDetails implements ITypeExtractor
{
    private FileFormatType fileFormatType;

    private boolean measuredData;

    private String processorType;

    private DataSetType dataSetType;

    private LocatorType locatorType;

    private DataSetInformation dataSetInformation;

    public DataSetRegistrationDetails()
    {
        fileFormatType = new FileFormatType(FileFormatType.DEFAULT_FILE_FORMAT_TYPE_CODE);
        measuredData = true;
        locatorType = new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE);
    }

    public FileFormatType getFileFormatType(File incomingDataSetPath)
    {
        return fileFormatType;
    }

    public boolean isMeasuredData(File incomingDataSetPath)
    {
        return measuredData;
    }

    public String getProcessorType(File incomingDataSetPath)
    {
        return processorType;
    }

    public DataSetType getDataSetType(File incomingDataSetPath)
    {
        return dataSetType;
    }

    public LocatorType getLocatorType(File incomingDataSetPath)
    {
        return locatorType;
    }

    public FileFormatType getFileFormatType()
    {
        return fileFormatType;
    }

    public void setFileFormatType(FileFormatType fileFormatType)
    {
        this.fileFormatType = fileFormatType;
    }

    public boolean isMeasuredData()
    {
        return measuredData;
    }

    public void setMeasuredData(boolean measuredData)
    {
        this.measuredData = measuredData;
    }

    public String getProcessorType()
    {
        return processorType;
    }

    public void setProcessorType(String processorType)
    {
        this.processorType = processorType;
    }

    public DataSetType getDataSetType()
    {
        return dataSetType;
    }

    public void setDataSetType(DataSetType dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    public LocatorType getLocatorType()
    {
        return locatorType;
    }

    public void setLocatorType(LocatorType locatorType)
    {
        this.locatorType = locatorType;
    }

    public DataSetInformation getDataSetInformation()
    {
        return dataSetInformation;
    }

    public void setDataSetInformation(DataSetInformation dataSetInformation)
    {
        this.dataSetInformation = dataSetInformation;
    }
}
