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
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.annotate.JsonTypeName;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * Immutable value object representing a property type.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonTypeName("PropertyType")
@JsonSubTypes(value = {@JsonSubTypes.Type(ControlledVocabularyPropertyType.class)})
public class PropertyType implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Class used to initialize a new data set instance. Necessary since all the fields of a DataSet
     * are final.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class PropertyTypeInitializer
    {
        private DataTypeCode dataType;

        private String code;

        private String label;

        private String description;

        private boolean mandatory;

        public DataTypeCode getDataType()
        {
            return dataType;
        }

        public void setDataType(DataTypeCode dataType)
        {
            this.dataType = dataType;
        }

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

        public boolean isMandatory()
        {
            return mandatory;
        }

        public void setMandatory(boolean mandatory)
        {
            this.mandatory = mandatory;
        }
    }

    private DataTypeCode dataType;

    private String code;

    private String label;

    private String description;

    private boolean mandatory;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public PropertyType(PropertyTypeInitializer initializer)
    {
        if (initializer.dataType == null)
        {
            throw new IllegalArgumentException("Unspecified data type.");
        }
        this.dataType = initializer.getDataType();

        InitializingChecks.checkValidString(initializer.getCode(), "Unspecified code.");
        this.code = initializer.getCode();

        InitializingChecks.checkValidString(initializer.getLabel(), "Unspecified label.");
        this.label = initializer.getLabel();

        this.description = initializer.getDescription();
        this.mandatory = initializer.isMandatory();
    }

    public DataTypeCode getDataType()
    {
        return dataType;
    }

    /**
     * Return the code of this property type.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Return the label shown in forms.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Return the extended description. May be null.
     */
    public String getDescription()
    {
        return description;
    }

    public boolean isMandatory()
    {
        return mandatory;
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
        builder.append(getDataType());
        builder.append(getCode());
        builder.append(getLabel());
        builder.append(getDescription());
        builder.append(isMandatory() ? "mandatory" : "optional");
        this.appendFieldsToStringBuilder(builder);
        return builder.toString();
    }

    /**
     * For subclasses to override
     */
    protected void appendFieldsToStringBuilder(ToStringBuilder builder)
    {

    }

    //
    // JSON-RPC
    //

    PropertyType()
    {
    }

    private void setDataType(DataTypeCode dataType)
    {
        this.dataType = dataType;
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setLabel(String label)
    {
        this.label = label;
    }

    private void setDescription(String description)
    {
        this.description = description;
    }

    private void setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
    }
}
