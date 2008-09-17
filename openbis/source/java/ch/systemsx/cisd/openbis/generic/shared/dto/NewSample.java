/*
 * Copyright 2007 ETH Zuerich, CISD
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;

/**
 * The full description of a new sample component.
 * <p>
 * Used when registering a sample component to the LIMS.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class NewSample implements Serializable, ISimpleEntityPropertiesHolder
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private SampleIdentifier sampleIdentifier;

    private SampleTypeCode sampleTypeCode;

    private SampleIdentifier parentSampleIdentifier;

    private SampleIdentifier controlLayoutIdentifierOrNull;

    private SimpleEntityProperty[] properties;

    public NewSample()
    {
    }

    public NewSample(final SampleIdentifier sampleIdentifier, final SampleTypeCode sampleTypeCode,
            final SampleIdentifier controlLayoutOrNull)
    {
        this.sampleIdentifier = sampleIdentifier;
        this.controlLayoutIdentifierOrNull = controlLayoutOrNull;
        this.sampleTypeCode = sampleTypeCode;
    }

    public SimpleEntityProperty[] getProperties()
    {
        return properties;
    }

    public void setProperties(final SimpleEntityProperty[] properties)
    {
        this.properties = properties;
    }

    public final SampleIdentifier getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public final SampleIdentifier getParentSampleIdentifier()
    {
        return parentSampleIdentifier;
    }

    public final void setParentSampleIdentifier(final SampleIdentifier parentSampleIdentifier)
    {
        this.parentSampleIdentifier = parentSampleIdentifier;
    }

    public final SampleTypeCode getSampleTypeCode()
    {
        return sampleTypeCode;
    }

    /**
     * Return the <i>Control Layout</i> {@link SampleIdentifier}.
     * 
     * @return <code>null</code> if not specified.
     */
    public final SampleIdentifier getControlLayoutIdentifier()
    {
        return controlLayoutIdentifierOrNull;
    }

    public final void setControlLayoutIdentifier(final SampleIdentifier controlLayoutIdentifier)
    {
        this.controlLayoutIdentifierOrNull = controlLayoutIdentifier;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof NewSample == false)
        {
            return false;
        }
        final NewSample that = (NewSample) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.getSampleIdentifier(), getSampleIdentifier());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSampleIdentifier());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

}
