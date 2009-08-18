/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;

/**
 * Criteria for listing <i>experiments</i>.
 * 
 * @author Izabela Adamczyk
 */
public final class ListExperimentsCriteria extends DefaultResultSetConfig<String, Experiment>
        implements IsSerializable
{
    private ExperimentType experimentType;

    private String groupCode;

    private String projectCode;

    private String baseIndexURL;

    public String getBaseIndexURL()
    {
        return baseIndexURL;
    }

    public void setBaseIndexURL(String baseIndexURL)
    {
        this.baseIndexURL = baseIndexURL;
    }

    public ExperimentType getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(final ExperimentType experimentType)
    {
        this.experimentType = experimentType;
    }

    public String getGroupCode()
    {
        return groupCode;
    }

    public void setGroupCode(final String groupCode)
    {
        this.groupCode = groupCode;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public void setProjectCode(final String projectCode)
    {
        this.projectCode = projectCode;
    }

}
