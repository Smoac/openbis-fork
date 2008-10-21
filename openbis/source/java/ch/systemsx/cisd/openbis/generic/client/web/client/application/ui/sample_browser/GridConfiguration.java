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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * @author Izabela Adamczyk
 */
public class GridConfiguration
{

    private SampleType sampleType;

    private String groupCode;

    private boolean includeGroup;

    private boolean includeInstance;

    public boolean majorChange(GridConfiguration newConfiguration)
    {
        return sampleTypeChanged(newConfiguration.getSampleType())
                || instanceRequested(newConfiguration.isIncludeInstance())
                || groupRequested(newConfiguration.getGroupCode(), newConfiguration
                        .isIncludeGroup());
    }

    public boolean minorChange(GridConfiguration newConfiguration)
    {
        return includeGroup == true && newConfiguration.isIncludeGroup() == false
                || includeInstance == true && newConfiguration.isIncludeInstance() == false;
    }

    public void update(SampleType sampleTypeNewValue, String groupCodeNewValue,
            boolean includeGroupNewValue, boolean includeInstanceNewValue)
    {
        setSampleType(sampleTypeNewValue);
        setGroupCode(groupCodeNewValue);
        setIncludeGroup(includeGroupNewValue);
        setIncludeInstance(includeInstanceNewValue);
    }

    public void update(GridConfiguration newConfiguration)
    {
        setSampleType(newConfiguration.getSampleType());
        setGroupCode(newConfiguration.getGroupCode());
        setIncludeGroup(newConfiguration.isIncludeGroup());
        setIncludeInstance(newConfiguration.isIncludeInstance());
    }

    private boolean groupRequested(String groupCodeNewValue, boolean includeGroupNewValue)
    {
        return includeGroupNewValue
                && (includeGroup == false || groupCode == null || groupCodeNewValue
                        .equals(groupCode) == false);
    }

    private boolean instanceRequested(boolean includeInstanceNewValue)
    {
        return includeInstanceNewValue && includeInstance == false;
    }

    private boolean sampleTypeChanged(SampleType sampleTypeNewValue)
    {
        return sampleType == null
                || sampleTypeNewValue.getCode().equals(sampleType.getCode()) == false;
    }

    private SampleType getSampleType()
    {
        return sampleType;
    }

    private void setSampleType(SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    private String getGroupCode()
    {
        return groupCode;
    }

    private void setGroupCode(String groupCode)
    {
        this.groupCode = groupCode;
    }

    public boolean isIncludeGroup()
    {
        return includeGroup;
    }

    private void setIncludeGroup(boolean includeGroup)
    {
        this.includeGroup = includeGroup;
    }

    public boolean isIncludeInstance()
    {
        return includeInstance;
    }

    private void setIncludeInstance(boolean includeInstance)
    {
        this.includeInstance = includeInstance;
    }

}
