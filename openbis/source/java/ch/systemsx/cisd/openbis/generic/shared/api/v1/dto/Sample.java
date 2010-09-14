/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Immutable value object representing a sample.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public final class Sample implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String identifier;

    /**
     * Class used to initialize a new sample instance. Necessary since all the fields of a sample
     * are final.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static final class SampleInitializer
    {
        private String identifier;

        public String getIdentifier()
        {
            return identifier;
        }

        public void setIdentifier(String identifier)
        {
            this.identifier = identifier;
        }
    }

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public Sample(SampleInitializer initializer)
    {
        String id = initializer.getIdentifier();
        if (id == null || id.length() == 0)
        {
            throw new IllegalArgumentException("Unspecified identifier.");
        }
        this.identifier = id;
    }

    /**
     * Returns the sample identifier;
     */
    public String getIdentifier()
    {
        return identifier;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Sample == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        Sample other = (Sample) obj;
        builder.append(getIdentifier(), other.getIdentifier());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getIdentifier());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getIdentifier());
        return builder.toString();
    }
}
