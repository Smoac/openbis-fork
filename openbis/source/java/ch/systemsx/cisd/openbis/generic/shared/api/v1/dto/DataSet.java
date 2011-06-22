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
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Immutable value object representing a data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public final class DataSet implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static enum Connections
    {
        PARENTS
    }

    /**
     * Class used to initialize a new data set instance. Necessary since all the fields of a DataSet
     * are final.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static final class DataSetInitializer
    {
        private String code;

        private String sampleIdentifierOrNull;

        private String experimentIdentifier;

        private String dataSetTypeCode;

        private Date registrationDate;

        private EnumSet<Connections> retrievedConnections = EnumSet.noneOf(Connections.class);

        private ArrayList<String> parentCodes = new ArrayList<String>();

        private HashMap<String, String> properties = new HashMap<String, String>();

        public String getCode()
        {
            return code;
        }

        public void setCode(String code)
        {
            this.code = code;
        }

        public String getSampleIdentifierOrNull()
        {
            return sampleIdentifierOrNull;
        }

        public void setSampleIdentifierOrNull(String sampleIdentifierOrNull)
        {
            this.sampleIdentifierOrNull = sampleIdentifierOrNull;
        }

        public String getExperimentIdentifier()
        {
            return experimentIdentifier;
        }

        public void setExperimentIdentifier(String experimentIdentifier)
        {
            this.experimentIdentifier = experimentIdentifier;
        }

        public void setDataSetTypeCode(String dataSetTypeCode)
        {
            this.dataSetTypeCode = dataSetTypeCode;
        }

        public String getDataSetTypeCode()
        {
            return dataSetTypeCode;
        }

        public void setRegistrationDate(Date registrationDate)
        {
            this.registrationDate = registrationDate;
        }

        public Date getRegistrationDate()
        {
            return registrationDate;
        }

        public HashMap<String, String> getProperties()
        {
            return properties;
        }

        public void putProperty(String propCode, String value)
        {
            properties.put(propCode, value);
        }

        public void setRetrievedConnections(EnumSet<Connections> retrievedConnections)
        {
            this.retrievedConnections =
                    (null == retrievedConnections) ? EnumSet.noneOf(Connections.class)
                            : retrievedConnections;
        }

        public EnumSet<Connections> getRetrievedConnections()
        {
            return retrievedConnections;
        }

        public void setParentCodes(ArrayList<String> parentCodes)
        {
            this.parentCodes = (null == parentCodes) ? new ArrayList<String>() : parentCodes;
        }

        public List<String> getParentCodes()
        {
            return parentCodes;
        }
    }

    private final String code;

    private final String experimentIdentifier;

    private final String sampleIdentifierOrNull;

    private final String dataSetTypeCode;

    private final Date registrationDate;

    private final HashMap<String, String> properties;

    // For handling connections to entities
    private final EnumSet<Connections> retrievedConnections;

    private final List<String> parentCodes;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public DataSet(DataSetInitializer initializer)
    {
        checkValidString(initializer.getCode(), "Unspecified code.");
        this.code = initializer.getCode();

        checkValidString(initializer.getExperimentIdentifier(), "Unspecified experiment.");
        this.experimentIdentifier = initializer.getExperimentIdentifier();

        this.sampleIdentifierOrNull = initializer.getSampleIdentifierOrNull();

        // Either the sample identifier or experiment identifier should be non-null
        assert sampleIdentifierOrNull != null || experimentIdentifier != null;

        checkValidString(initializer.getDataSetTypeCode(), "Unspecified data set type code.");
        this.dataSetTypeCode = initializer.getDataSetTypeCode();

        this.registrationDate = initializer.getRegistrationDate();

        this.properties = initializer.getProperties();

        this.retrievedConnections = initializer.getRetrievedConnections();
        this.parentCodes = initializer.getParentCodes();
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

    public String getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    public String getSampleIdentifierOrNull()
    {
        return sampleIdentifierOrNull;
    }

    public String getDataSetTypeCode()
    {
        return dataSetTypeCode;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public HashMap<String, String> getProperties()
    {
        return properties;
    }

    public EnumSet<Connections> getRetrievedConnections()
    {
        return retrievedConnections;
    }

    public List<String> getParentCodes()
    {
        return Collections.unmodifiableList(parentCodes);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof DataSet == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        DataSet other = (DataSet) obj;
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
        builder.append(getExperimentIdentifier());
        builder.append(getSampleIdentifierOrNull());
        builder.append(getDataSetTypeCode());

        // Append properties alphabetically for consistency
        TreeMap<String, String> sortedProps = new TreeMap<String, String>(getProperties());
        builder.append(sortedProps.toString());
        if (retrievedConnections.contains(Connections.PARENTS))
        {
            builder.append(getParentCodes());
        }
        return builder.toString();
    }
}
