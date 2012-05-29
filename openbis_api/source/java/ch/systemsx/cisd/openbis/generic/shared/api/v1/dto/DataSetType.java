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
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Immutable value object representing a data set type.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonTypeName("DataSetType")
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

        private ArrayList<PropertyTypeGroup> propertyTypeGroups =
                new ArrayList<PropertyTypeGroup>();

        public String getCode()
        {
            return code;
        }

        public void setCode(String code)
        {
            this.code = code;
        }

        public ArrayList<PropertyTypeGroup> getPropertyTypeGroups()
        {
            return propertyTypeGroups;
        }

        public void addPropertyTypeGroup(PropertyTypeGroup propertyType)
        {
            propertyTypeGroups.add(propertyType);
        }
    }

    private String code;

    private ArrayList<PropertyTypeGroup> propertyTypeGroups;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public DataSetType(DataSetTypeInitializer initializer)
    {
        InitializingChecks.checkValidString(initializer.getCode(), "Unspecified code.");
        this.code = initializer.getCode();

        this.propertyTypeGroups = initializer.getPropertyTypeGroups();
    }

    /**
     * Returns the data set code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Return the grouped property types for this data set type. (Groups are referred to as sections
     * elsewhere).
     */
    public List<PropertyTypeGroup> getPropertyTypeGroups()
    {
        return propertyTypeGroups;
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
        builder.append(getPropertyTypeGroups());
        return builder.toString();
    }

    //
    // JSON-RPC
    //

    private DataSetType()
    {
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setPropertyTypeGroups(ArrayList<PropertyTypeGroup> propertyTypeGroups)
    {
        this.propertyTypeGroups = propertyTypeGroups;
    }
}
