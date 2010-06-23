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
import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * DTO containing information about data set in a simple form.
 * 
 * @author Izabela Adamczyk
 */
public class SimpleDataSetInformationDTO implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String dataSetCode;

    private String dataSetLocation;

    private String dataSetType;

    private String sampleCode;

    private String groupCode;

    private String experimentCode;

    private String projectCode;

    private String databaseInstanceCode;

    private Collection<String> parentDataSetCodes;

    public String getDataSetType()
    {
        return dataSetType;
    }

    public void setDataSetType(String dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    public String getDataSetCode()
    {
        return dataSetCode;
    }

    public void setDataSetCode(String dataSetCode)
    {
        this.dataSetCode = dataSetCode;
    }

    public String getDataSetLocation()
    {
        return dataSetLocation;
    }

    public void setDataSetLocation(String dataSetLocation)
    {
        this.dataSetLocation = dataSetLocation;
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

    public String getGroupCode()
    {
        return groupCode;
    }

    public void setGroupCode(String groupCode)
    {
        this.groupCode = groupCode;
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

    public Collection<String> getParentDataSetCodes()
    {
        return parentDataSetCodes;
    }

    public void setParentDataSetCodes(Collection<String> parentDataSetCodes)
    {
        this.parentDataSetCodes = parentDataSetCodes;
    }

}
