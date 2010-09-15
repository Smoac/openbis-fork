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

    /**
     * Class used to initialize a new sample instance. Necessary since all the fields of a sample
     * are final.
     * <p>
     * All of the properties must be filled (non-null) before being used to initialize a Sample,
     * otherwise the Sample constructor will throw an exception.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static final class SampleInitializer
    {
        private Long id;

        private String code;

        private String identifier;

        private Long sampleTypeId;

        private String sampleTypeCode;

        public void setId(Long id)
        {
            this.id = id;
        }

        public Long getId()
        {
            return id;
        }

        public void setCode(String code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }

        public String getIdentifier()
        {
            return identifier;
        }

        public void setIdentifier(String identifier)
        {
            this.identifier = identifier;
        }

        public void setSampleTypeId(Long sampleTypeId)
        {
            this.sampleTypeId = sampleTypeId;
        }

        public Long getSampleTypeId()
        {
            return sampleTypeId;
        }

        public void setSampleTypeCode(String sampleTypeCode)
        {
            this.sampleTypeCode = sampleTypeCode;
        }

        public String getSampleTypeCode()
        {
            return sampleTypeCode;
        }
    }

    private final Long id;

    private final String code;

    private final String identifier;

    private final Long sampleTypeId;

    private final String sampleTypeCode;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public Sample(SampleInitializer initializer)
    {
        checkValidLong(initializer.getId(), "Unspecified id.");
        this.id = initializer.getId();

        checkValidString(initializer.getCode(), "Unspecified code.");
        this.code = initializer.getCode();

        checkValidString(initializer.getIdentifier(), "Unspecified identifier.");
        this.identifier = initializer.getIdentifier();

        checkValidLong(initializer.getSampleTypeId(), "Unspecified sample type id.");
        this.sampleTypeId = initializer.getSampleTypeId();

        checkValidString(initializer.getSampleTypeCode(), "Unspecified sample type code.");
        this.sampleTypeCode = initializer.getSampleTypeCode();
    }

    private void checkValidString(String string, String message) throws IllegalArgumentException
    {
        if (string == null || string.length() == 0)
        {
            throw new IllegalArgumentException(message);
        }
    }

    private void checkValidLong(Long longValue, String message) throws IllegalArgumentException
    {
        if (longValue == null || longValue == 0)
        {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Returns the sample id.
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Returns the sample code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns the sample identifier;
     */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * Returns the sample type id.
     */
    public Long getSampleTypeId()
    {
        return sampleTypeId;
    }

    /**
     * Returns the sample type code.
     */
    public String getSampleTypeCode()
    {
        return sampleTypeCode;
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
        builder.append(getId(), other.getId());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getIdentifier());
        builder.append(getSampleTypeCode());
        return builder.toString();
    }
}
