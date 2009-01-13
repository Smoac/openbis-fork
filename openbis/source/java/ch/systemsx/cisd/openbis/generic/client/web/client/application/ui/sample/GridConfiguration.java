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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;

/**
 * Stores the information about grid state.
 * 
 * @author Izabela Adamczyk
 */
public class GridConfiguration
{
    private ListSampleCriteria criteria = new ListSampleCriteria();

    public boolean majorChange(GridConfiguration newConfiguration)
    {
        return sampleTypeChanged(newConfiguration.getSampleType())
                || instanceRequested(newConfiguration.isIncludeInstance())
                || groupRequested(newConfiguration.getGroupCode(), newConfiguration
                        .isIncludeGroup());
    }

    public boolean minorChange(GridConfiguration newConfiguration)
    {
        return isIncludeGroup() == true && newConfiguration.isIncludeGroup() == false
                || isIncludeInstance() == true && newConfiguration.isIncludeInstance() == false;
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
                && (isIncludeGroup() == false || getGroupCode() == null || groupCodeNewValue
                        .equals(getGroupCode()) == false);
    }

    private boolean instanceRequested(boolean includeInstanceNewValue)
    {
        return includeInstanceNewValue && isIncludeInstance() == false;
    }

    private boolean sampleTypeChanged(SampleType sampleTypeNewValue)
    {
        return getSampleType() == null
                || sampleTypeNewValue.getCode().equals(getSampleType().getCode()) == false;
    }

    private SampleType getSampleType()
    {
        return this.criteria.getSampleType();
    }

    private void setSampleType(SampleType sampleType)
    {
        this.criteria.setSampleType(sampleType);
    }

    private String getGroupCode()
    {
        return this.criteria.getGroupCode();
    }

    private void setGroupCode(String groupCode)
    {
        this.criteria.setGroupCode(groupCode);
    }

    public boolean isIncludeGroup()
    {
        return this.criteria.isIncludeGroup();
    }

    private void setIncludeGroup(boolean includeGroup)
    {
        this.criteria.setIncludeGroup(includeGroup);
    }

    public boolean isIncludeInstance()
    {
        return this.criteria.isIncludeInstance();
    }

    private void setIncludeInstance(boolean includeInstance)
    {
        this.criteria.setIncludeInstance(includeInstance);
    }

    public ListSampleCriteria getCriterias()
    {
        return criteria;
    }

}
