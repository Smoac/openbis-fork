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

public class SampleToRegister implements IsSerializable
{
    String sampleIdentifier;

    String typeCode;

    String generatorParent;

    String containerParent;

    List<SampleProperty> properties;

    public SampleToRegister()
    {
    }

    public SampleToRegister(String sampleIdentifier, String type, String generatorParent,
            String containerParent)
    {
        this.sampleIdentifier = sampleIdentifier;
        this.typeCode = type;
        this.generatorParent = generatorParent;
        this.containerParent = containerParent;
        properties = new ArrayList<SampleProperty>();
    }

    public String getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public void setSampleIdentifier(String code)
    {
        this.sampleIdentifier = code;
    }

    public String getType()
    {
        return typeCode;
    }

    public void setType(String type)
    {
        this.typeCode = type;
    }

    public String getGeneratorParent()
    {
        return generatorParent;
    }

    public void setGeneratorParent(String generatorParent)
    {
        this.generatorParent = generatorParent;
    }

    public String getContainerParent()
    {
        return containerParent;
    }

    public void setContainerParent(String containerParent)
    {
        this.containerParent = containerParent;
    }

    public List<SampleProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<SampleProperty> properties)
    {
        this.properties = properties;
    }

    public void addProperty(SampleProperty property)
    {
        properties.add(property);
    }

}