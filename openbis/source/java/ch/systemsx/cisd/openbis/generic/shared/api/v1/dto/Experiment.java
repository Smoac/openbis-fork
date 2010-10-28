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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Immutable value object representing an experiment.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public final class Experiment implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Class used to initialize a new experiment instance. Necessary since all the fields of a
     * sample are final.
     * <p>
     * All of the properties must be filled (non-null) before being used to initialize an
     * Experiment, otherwise the Experiment constructor will throw an exception.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static final class ExperimentInitializer
    {
        private Long id;

        private String permId;

        private String code;

        private String identifier;

        private String experimentTypeCode;

        private HashMap<String, String> properties = new HashMap<String, String>();

        public void setId(Long id)
        {
            this.id = id;
        }

        public Long getId()
        {
            return id;
        }

        public void setPermId(String permId)
        {
            this.permId = permId;
        }

        public String getPermId()
        {
            return permId;
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

        public void setExperimentTypeCode(String experimentTypeCode)
        {
            this.experimentTypeCode = experimentTypeCode;
        }

        public String getExperimentTypeCode()
        {
            return experimentTypeCode;
        }

        public HashMap<String, String> getProperties()
        {
            return properties;
        }

        public void putProperty(String propCode, String value)
        {
            properties.put(propCode, value);
        }
    }

    private final Long id;

    private final String permId;

    private final String code;

    private final String identifier;

    private final String experimentTypeCode;

    private final HashMap<String, String> properties;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public Experiment(ExperimentInitializer initializer)
    {
        checkValidLong(initializer.getId(), "Unspecified id.");
        this.id = initializer.getId();

        checkValidString(initializer.getPermId(), "Unspecified permanent id.");
        this.permId = initializer.getPermId();

        checkValidString(initializer.getCode(), "Unspecified code.");
        this.code = initializer.getCode();

        checkValidString(initializer.getIdentifier(), "Unspecified identifier.");
        this.identifier = initializer.getIdentifier();

        checkValidString(initializer.getExperimentTypeCode(), "Unspecified eperiment type code.");
        this.experimentTypeCode = initializer.getExperimentTypeCode();

        this.properties = initializer.getProperties();
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
     * Returns the experiment id.
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Returns the experiment permanent id.
     */
    public String getPermId()
    {
        return permId;
    }

    /**
     * Returns the experiment code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns the experiment identifier.
     */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * Returns the experiment type code.
     */
    public String getExperimentTypeCode()
    {
        return experimentTypeCode;
    }

    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Experiment == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        Experiment other = (Experiment) obj;
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
        builder.append(getExperimentTypeCode());
        builder.append(getProperties());
        return builder.toString();
    }
}
