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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A sample to register.
 * 
 * @author Christian Ribeaud
 */
public final class SampleToRegister implements IsSerializable
{
    private String sampleIdentifier;

    private String sampleTypeCode;

    private String parent;

    private String container;

    private List<SampleProperty> properties;

    public SampleToRegister()
    {
    }

    public SampleToRegister(final String sampleIdentifier, final String sampleTypeCode, final String parent,
            final String container)
    {
        this.sampleIdentifier = sampleIdentifier;
        this.sampleTypeCode = sampleTypeCode;
        this.parent = parent;
        this.container = container;
        properties = new ArrayList<SampleProperty>();
    }

    public final String getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public final void setSampleIdentifier(final String sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    public final String getSampleTypeCode()
    {
        return sampleTypeCode;
    }

    public final void setSampleTypeCode(final String typeCode)
    {
        this.sampleTypeCode = typeCode;
    }

    public final String getParent()
    {
        return parent;
    }

    public final void setParent(final String parent)
    {
        this.parent = parent;
    }

    public final String getContainer()
    {
        return container;
    }

    public final void setContainer(final String container)
    {
        this.container = container;
    }

    public final List<SampleProperty> getProperties()
    {
        return properties;
    }

    public final void setProperties(final List<SampleProperty> properties)
    {
        this.properties = properties;
    }

    public final void addProperty(final SampleProperty property)
    {
        properties.add(property);
    }
}