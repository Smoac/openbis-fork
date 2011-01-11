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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes one dataset which should be processed by the plugin task.
 * 
 * @author Tomasz Pylak
 */
public class DatasetDescription implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String datasetTypeCode;
    
    private String datasetCode;

    private String dataSetLocation;

    private String sampleCode;
    
    private String sampleIdentifier;
    
    private String sampleTypeCode;

    private String spaceCode;

    private String projectCode;

    private String instanceCode;

    private String experimentCode;
    
    private String experimentIdentifier;
    
    private String experimentTypeCode;

    private String mainDataSetPattern;

    private String mainDataSetPath;

    public void setDatasetTypeCode(String datasetTypeCode)
    {
        this.datasetTypeCode = datasetTypeCode;
    }

    public String getDatasetTypeCode()
    {
        return datasetTypeCode;
    }

    public String getDataSetLocation()
    {
        return dataSetLocation;
    }

    public void setDataSetLocation(String dataSetLocation)
    {
        this.dataSetLocation = dataSetLocation;
    }
    
    public String getInstanceCode()
    {
        return instanceCode;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public void setDatasetCode(String datasetCode)
    {
        this.datasetCode = datasetCode;
    }
    
    /**
     * NOTE: may be NULL
     */
    public String getMainDataSetPattern()
    {
        return mainDataSetPattern;
    }

    public void setMainDataSetPattern(String mainDataSetPattern)
    {
        this.mainDataSetPattern = mainDataSetPattern;
    }
    
    /**
     * NOTE: may be NULL
     */
    public String getMainDataSetPath()
    {
        return mainDataSetPath;
    }

    public void setMainDataSetPath(String mainDataSetPath)
    {
        this.mainDataSetPath = mainDataSetPath;
    }
    
    /**
     * NOTE: may be NULL
     */
    public String getSampleCode()
    {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }
    
    public void setSampleIdentifier(String sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    public String getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public void setSampleTypeCode(String sampleTypeCode)
    {
        this.sampleTypeCode = sampleTypeCode;
    }

    public String getSampleTypeCode()
    {
        return sampleTypeCode;
    }

    public String getSpaceCode()
    {
        return spaceCode;
    }

    public void setSpaceCode(String groupCode)
    {
        this.spaceCode = groupCode;
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
        return instanceCode;
    }

    public void setDatabaseInstanceCode(String instanceCode)
    {
        this.instanceCode = instanceCode;
    }
    
    public String getExperimentCode()
    {
        return experimentCode;
    }

    public void setExperimentCode(String experimentCode)
    {
        this.experimentCode = experimentCode;
    }
    
    public void setExperimentIdentifier(String experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

    public String getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    public void setExperimentTypeCode(String experimentTypeCode)
    {
        this.experimentTypeCode = experimentTypeCode;
    }

    public String getExperimentTypeCode()
    {
        return experimentTypeCode;
    }

    @Override
    public String toString()
    {
        return String.format("Dataset '%s'", datasetCode);
    }
    
}


