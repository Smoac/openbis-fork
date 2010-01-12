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

    private final String datasetCode;

    private final String dataSetLocation;

    private final String sampleCode;

    private final String groupCode;

    private final String projectCode;

    private final String experimentCode;

    private final String mainDataSetPattern;

    private final String mainDataSetPath;

    public DatasetDescription(String datasetCode, String dataSetLocation, String sampleCode,
            String groupCode, String projectCode, String experimentCode,
            String mainDataSetPattern, String mainDataSetPath)
    {
        this.datasetCode = datasetCode;
        this.dataSetLocation = dataSetLocation;
        this.sampleCode = sampleCode;
        this.groupCode = groupCode;
        this.projectCode = projectCode;
        this.experimentCode = experimentCode;
        this.mainDataSetPattern = mainDataSetPattern;
        this.mainDataSetPath = mainDataSetPath;
    }

    public String getDataSetLocation()
    {
        return dataSetLocation;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    /**
     * NOTE: may be NULL
     */
    public String getMainDataSetPattern()
    {
        return mainDataSetPattern;
    }

    /**
     * NOTE: may be NULL
     */
    public String getMainDataSetPath()
    {
        return mainDataSetPath;
    }

    /**
     * NOTE: may be NULL
     */
    public String getSampleCode()
    {
        return sampleCode;
    }

    public String getGroupCode()
    {
        return groupCode;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public String getExperimentCode()
    {
        return experimentCode;
    }

    @Override
    public String toString()
    {
        return String.format("Dataset '%s'", datasetCode);
    }

}
