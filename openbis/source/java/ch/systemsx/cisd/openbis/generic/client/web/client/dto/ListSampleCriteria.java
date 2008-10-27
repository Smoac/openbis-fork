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

/**
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 */
public class ListSampleCriteria implements IsSerializable
{
    private SampleType sampleType;

    private String groupCode;

    private boolean includeGroup;

    private boolean includeInstance;

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public String getGroupCode()
    {
        return groupCode;
    }

    public void setGroupCode(String groupCode)
    {
        this.groupCode = groupCode;
    }

    public boolean isIncludeGroup()
    {
        return includeGroup;
    }

    public void setIncludeGroup(boolean includeGroup)
    {
        this.includeGroup = includeGroup;
    }

    public boolean isIncludeInstance()
    {
        return includeInstance;
    }

    public void setIncludeInstance(boolean includeInstance)
    {
        this.includeInstance = includeInstance;
    }
}
