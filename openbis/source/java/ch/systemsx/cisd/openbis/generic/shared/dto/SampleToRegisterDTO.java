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

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * A sample that is ready to be registered.
 * 
 * @author Christian Ribeaud
 */
public final class SampleToRegisterDTO implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private SampleIdentifier sampleIdentifier;

    private String sampleTypeCode;

    private SampleIdentifier parent;

    private SampleIdentifier container;

    private SampleProperty[] properties;

    public SampleToRegisterDTO()
    {
    }

    public final SampleIdentifier getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public final void setSampleIdentifier(final SampleIdentifier sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    public final SampleIdentifier getParent()
    {
        return parent;
    }

    public final void setParent(final SampleIdentifier parent)
    {
        this.parent = parent;
    }

    public final SampleIdentifier getContainer()
    {
        return container;
    }

    public final void setContainer(final SampleIdentifier container)
    {
        this.container = container;
    }

    public final void setProperties(final SampleProperty[] properties)
    {
        this.properties = properties;
    }

    public final SampleProperty[] getProperties()
    {
        return properties;
    }

    public final void setSampleTypeCode(final String sampleTypeCode)
    {
        this.sampleTypeCode = sampleTypeCode;
    }

    public final String getSampleTypeCode()
    {
        return sampleTypeCode;
    }

}