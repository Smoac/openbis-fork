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
 * Immutable value object representing a property type.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public final class PropertyType implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Class used to initialize a new data set instance. Necessary since all the fields of a DataSet
     * are final.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static final class PropertyTypeInitializer
    {
        private String code;

        private String label;

        private String description;

        public String getCode()
        {
            return code;
        }

        public void setCode(String code)
        {
            this.code = code;
        }

        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

    }

    private final String code;

    private final String label;

    private final String description;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public PropertyType(PropertyTypeInitializer initializer)
    {
        checkValidString(initializer.getCode(), "Unspecified code.");
        this.code = initializer.getCode();

        checkValidString(initializer.getLabel(), "Unspecified label.");
        this.label = initializer.getLabel();

        this.description = initializer.getDescription();
    }

    private void checkValidString(String string, String message) throws IllegalArgumentException
    {
        if (string == null || string.length() == 0)
        {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Returns the sample code;
     */
    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof PropertyType == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        PropertyType other = (PropertyType) obj;
        builder.append(getCode(), other.getCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getCode());
        builder.append(getLabel());
        builder.append(getDescription());
        return builder.toString();
    }
}
