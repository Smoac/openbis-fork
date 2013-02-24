/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * DTO containing information about data set in a simple form.
 * 
 * @author Izabela Adamczyk
 */
public class SimpleDataSetInformationDTO implements Serializable, IDatasetLocation
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String dataStoreCode;

    private String dataSetCode;

    private String dataSetShareId;

    private String dataSetLocation;

    private Long dataSetSize;

    private String dataSetType;

    private int speedHint;

    private String sampleCode;

    private String spaceCode;

    private String experimentCode;

    private String projectCode;

    private String databaseInstanceCode;

    private String dataStoreUrl;

    public void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode;
    }

    @Override
    public String getDataStoreCode()
    {
        return dataStoreCode;
    }

    public String getDataSetType()
    {
        return dataSetType;
    }

    public void setDataSetType(String dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    @Override
    public String getDataSetCode()
    {
        return dataSetCode;
    }

    public void setDataSetCode(String dataSetCode)
    {
        this.dataSetCode = dataSetCode;
    }

    public String getDataSetShareId()
    {
        return dataSetShareId;
    }

    public void setDataSetShareId(String dataSetShareId)
    {
        this.dataSetShareId = dataSetShareId;
    }

    public Long getDataSetSize()
    {
        return dataSetSize;
    }

    public void setDataSetSize(Long dataSetSize)
    {
        this.dataSetSize = dataSetSize;
    }

    @Override
    public String getDataSetLocation()
    {
        return dataSetLocation;
    }

    public void setDataSetLocation(String dataSetLocation)
    {
        this.dataSetLocation = dataSetLocation;
    }

    public int getSpeedHint()
    {
        return speedHint;
    }

    public void setSpeedHint(int speedHint)
    {
        this.speedHint = speedHint;
    }

    /** NOTE: may be NULL! */
    public String getSampleCode()
    {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public String getSpaceCode()
    {
        return spaceCode;
    }

    public void setSpaceCode(String groupCode)
    {
        this.spaceCode = groupCode;
    }

    public String getExperimentCode()
    {
        return experimentCode;
    }

    public void setExperimentCode(String experimentCode)
    {
        this.experimentCode = experimentCode;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    public String getDatabaseInstanceCode()
    {
        return databaseInstanceCode;
    }

    public void setDatabaseInstanceCode(String dbInstanceCode)
    {
        this.databaseInstanceCode = dbInstanceCode;
    }

    @Override
    public String getDataStoreUrl()
    {
        return dataStoreUrl;
    }

    public void setDataStoreUrl(String dataStoreUrl)
    {
        this.dataStoreUrl = dataStoreUrl;
    }

}
