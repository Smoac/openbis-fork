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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

public class SampleToRegisterDTO implements Serializable, ISimpleEntityPropertiesHolder
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    SampleIdentifier sampleIdentifier;

    String typeCode;

    SampleIdentifier generatorParent;

    SampleIdentifier containerParent;

    SimpleEntityProperty[] properties;

    public SampleToRegisterDTO()
    {
    }

    public SampleIdentifier getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public void setSampleIdentifier(SampleIdentifier code)
    {
        this.sampleIdentifier = code;
    }

    public SampleIdentifier getGeneratorParent()
    {
        return generatorParent;
    }

    public void setGeneratorParent(SampleIdentifier generatorParent)
    {
        this.generatorParent = generatorParent;
    }

    public SampleIdentifier getContainerParent()
    {
        return containerParent;
    }

    public void setContainerParent(SampleIdentifier containerParent)
    {
        this.containerParent = containerParent;
    }

    public void setProperties(SimpleEntityProperty[] properties)
    {
        this.properties = properties;
    }

    public SimpleEntityProperty[] getProperties()
    {
        return properties;
    }

    public void setSampleTypeCode(String type)
    {
        typeCode = type;
    }

    public String getSampleTypeCode()
    {
        return typeCode;
    }

}