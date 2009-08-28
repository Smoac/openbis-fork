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

package ch.systemsx.cisd.yeastx.etl;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.annotation.BeanProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * Represents one line in the file which describes one dataset: file name, sample to which dataset
 * should be connected, dataset properties and some additional processing information.
 * 
 * @author Tomasz Pylak
 */
public class DataSetMappingInformation
{
    private String fileName;

    private String sampleCodeOrLabel;

    private String experimentName;

    private String projectCode;

    private String groupCode;

    private String conversion;
    
    private String parentDataSetCode;

    private List<NewProperty> properties;

    public String getFileName()
    {
        return fileName;
    }

    @BeanProperty(label = "file_name")
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getSampleCodeOrLabel()
    {
        return sampleCodeOrLabel;
    }

    @BeanProperty(label = "sample")
    public void setSampleCodeOrLabel(String sampleCodeOrLabel)
    {
        this.sampleCodeOrLabel = StringUtils.trimToNull(sampleCodeOrLabel);
    }

    /**
     * Returns the code of the parent data set, if any.
     */
    public final String getParentDataSetCode()
    {
        return parentDataSetCode;
    }

    @BeanProperty(label="parent", optional = true)
    public final void setParentDataSetCode(String parentCode)
    {
        this.parentDataSetCode = parentCode;
    }

    public String getExperimentName()
    {
        return experimentName;
    }

    @BeanProperty(label = "experiment", optional = true)
    public void setExperimentName(String experimentName)
    {
        this.experimentName = StringUtils.trimToNull(experimentName);
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    @BeanProperty(label = "project", optional = true)
    public void setProjectCode(String projectCode)
    {
        this.projectCode = StringUtils.trimToNull(projectCode);
    }

    public String getGroupCode()
    {
        return groupCode;
    }

    @BeanProperty(label = "group")
    public void setGroupCode(String groupCode)
    {
        this.groupCode = groupCode;
    }

    public String getConversion()
    {
        return conversion;
    }

    @BeanProperty(label = "conversion", optional = true)
    public void setConversion(String conversion)
    {
        this.conversion = conversion;
    }

    public List<NewProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<NewProperty> dataSetProperties)
    {
        this.properties = dataSetProperties;
    }

}
