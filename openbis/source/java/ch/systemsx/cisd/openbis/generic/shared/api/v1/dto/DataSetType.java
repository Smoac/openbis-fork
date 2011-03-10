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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Immutable value object representing a data set type.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public final class DataSetType implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Class used to initialize a new data set instance. Necessary since all the fields of a DataSet
     * are final.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static final class DataSetTypeInitializer
    {
        private String code;

        private ArrayList<PropertyType> propertyTypes = new ArrayList<PropertyType>();

        public String getCode()
        {
            return code;
        }

        public void setCode(String code)
        {
            this.code = code;
        }

        public ArrayList<PropertyType> getPropertyTypes()
        {
            return propertyTypes;
        }

        public void addPropertyType(PropertyType propertyType)
        {
            propertyTypes.add(propertyType);
        }
    }

    private final String code;

    private final ArrayList<PropertyType> propertyTypes;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public DataSetType(DataSetTypeInitializer initializer)
    {
        checkValidString(initializer.getCode(), "Unspecified code.");
        this.code = initializer.getCode();

        this.propertyTypes = initializer.getPropertyTypes();
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

    public List<PropertyType> getPropertyTypes()
    {
        return propertyTypes;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof DataSetType == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        DataSetType other = (DataSetType) obj;
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
        builder.append(getPropertyTypes());
        return builder.toString();
    }
}
