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
 */
public class SampleTypePropertyType implements IsSerializable
{
    private boolean isDisplayed;

    private boolean mandatory;

    private boolean managedInternally;

    private SampleType sampleType;

    private PropertyType propertyType;

    public boolean isDisplayed()
    {
        return isDisplayed;
    }

    public void setDisplayed(boolean isDisplayed)
    {
        this.isDisplayed = isDisplayed;
    }

    public boolean isMandatory()
    {
        return mandatory;
    }

    public void setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    public void setManagedInternally(boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(SampleType entityType)
    {
        this.sampleType = entityType;
    }

    public PropertyType getPropertyType()
    {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }
}
