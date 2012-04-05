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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnore;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;

/**
 * Immutable value object representing a sample.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
public final class Sample implements Serializable, IIdentifierHolder
{
    private static final long serialVersionUID = 1L;

    public static enum Connections
    {
        PARENTS, CHILDREN, ASCENDANTS, DESCENDANTS
    }

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

        private String spaceCode;

        private String permId;

        private String code;

        private String identifier;

        private String experimentIdentifierOrNull;

        private Long sampleTypeId;

        private String sampleTypeCode;

        private HashMap<String, String> properties = new HashMap<String, String>();

        private EntityRegistrationDetails registrationDetails;

        private EnumSet<Connections> retrievedConnections = EnumSet.noneOf(Connections.class);

        private List<String> parentCodes = Collections.emptyList();

        private List<String> childrenCodes = Collections.emptyList();

        private List<String> ascendantsCodes = Collections.emptyList();

        private List<String> descendantsCodes = Collections.emptyList();

        public void setId(Long id)
        {
            this.id = id;
        }

        public Long getId()
        {
            return id;
        }

        public void setSpaceCode(String spaceCode)
        {
            this.spaceCode = spaceCode;
        }

        public String getSpaceCode()
        {
            return spaceCode;
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

        public String getExperimentIdentifierOrNull()
        {
            return experimentIdentifierOrNull;
        }

        public void setExperimentIdentifierOrNull(String experimentIdentifierOrNull)
        {
            this.experimentIdentifierOrNull = experimentIdentifierOrNull;
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

        public HashMap<String, String> getProperties()
        {
            return properties;
        }

        public void putProperty(String propCode, String value)
        {
            properties.put(propCode, value);
        }

        public void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
        {
            this.registrationDetails = registrationDetails;
        }

        public EntityRegistrationDetails getRegistrationDetails()
        {
            return registrationDetails;
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

        public void setParentCodes(List<String> parentCodes)
        {
            this.parentCodes = (null == parentCodes) ? new ArrayList<String>() : parentCodes;
        }

        public List<String> getParentCodes()
        {
            return parentCodes;
        }

        public List<String> getChildrenCodes()
        {
            return childrenCodes;
        }

        public void setChildrenCodes(List<String> childrenCodes)
        {
            this.childrenCodes = (null == childrenCodes) ? new ArrayList<String>() : childrenCodes;
        }

        public void setAscendantsCodes(List<String> ascendantsCodes)
        {
            this.ascendantsCodes =
                    (null == ascendantsCodes) ? new ArrayList<String>() : ascendantsCodes;
        }

        public List<String> getAscendantsCodes()
        {
            return ascendantsCodes;
        }

        public void setDescendantsCodes(List<String> descendantsCodes)
        {
            this.descendantsCodes =
                    (null == descendantsCodes) ? new ArrayList<String>() : descendantsCodes;
        }

        public List<String> getDescendantsCodes()
        {
            return descendantsCodes;
        }

    }

    private Long id;

    private String spaceCode;

    private String permId;

    private String code;

    private String identifier;

    private String experimentIdentifierOrNull;

    private Long sampleTypeId;

    private String sampleTypeCode;

    private HashMap<String, String> properties;

    private EntityRegistrationDetails registrationDetails;

    private EnumSet<Connections> retrievedConnections;

    private List<String> parentCodes = Collections.emptyList();

    private List<String> childrenCodes = Collections.emptyList();

    private List<String> ascendantsCodes = Collections.emptyList();

    private List<String> descendantsCodes = Collections.emptyList();

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public Sample(SampleInitializer initializer)
    {
        InitializingChecks.checkValidLong(initializer.getId(), "Unspecified id.");
        this.id = initializer.getId();

        this.spaceCode = initializer.getSpaceCode();

        InitializingChecks.checkValidString(initializer.getPermId(), "Unspecified permanent id.");
        this.permId = initializer.getPermId();

        InitializingChecks.checkValidString(initializer.getCode(), "Unspecified code.");
        this.code = initializer.getCode();

        InitializingChecks.checkValidString(initializer.getIdentifier(), "Unspecified identifier.");
        this.identifier = initializer.getIdentifier();

        this.experimentIdentifierOrNull = initializer.getExperimentIdentifierOrNull();

        InitializingChecks.checkValidLong(initializer.getSampleTypeId(),
                "Unspecified sample type id.");
        this.sampleTypeId = initializer.getSampleTypeId();

        InitializingChecks.checkValidString(initializer.getSampleTypeCode(),
                "Unspecified sample type code.");
        this.sampleTypeCode = initializer.getSampleTypeCode();

        this.properties = initializer.getProperties();

        InitializingChecks.checkValidRegistrationDetails(initializer.getRegistrationDetails(),
                "Unspecified entity registration details.");
        this.registrationDetails = initializer.getRegistrationDetails();

        this.retrievedConnections = initializer.getRetrievedConnections();
        this.parentCodes = initializer.getParentCodes();
        this.childrenCodes = initializer.getChildrenCodes();
        this.ascendantsCodes = initializer.getAscendantsCodes();
        this.descendantsCodes = initializer.getDescendantsCodes();
    }

    /**
     * Returns the sample id.
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Returns the space code.
     */
    public String getSpaceCode()
    {
        return spaceCode;
    }

    /**
     * Returns the sample permanent id.
     */
    public String getPermId()
    {
        return permId;
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

    public String getExperimentIdentifierOrNull()
    {
        return experimentIdentifierOrNull;
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

    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Return the sample registration details.
     * 
     * @since 1.11
     */
    public EntityRegistrationDetails getRegistrationDetails()
    {
        return registrationDetails;
    }

    /**
     * Returns types of retrieved connections.
     */
    public EnumSet<Connections> getRetrievedConnections()
    {
        return retrievedConnections;
    }

    /**
     * Return the parent codes. This throws an IllegalArgumentException if the parent codes were not
     * retrieved.
     * 
     * @return A list of parent data set codes or an empty list if there are no parents.
     * @throws IllegalArgumentException Thrown if the parent codes were not retrieved from the
     *             server.
     */
    @JsonIgnore
    public List<String> getParentCodes() throws IllegalArgumentException
    {
        if (getRetrievedConnections().contains(Connections.PARENTS))
        {
            return Collections.unmodifiableList(parentCodes);
        } else
        {
            throw new IllegalArgumentException("Parent codes were not retrieved for data set "
                    + getCode() + ".");
        }
    }

    /**
     * Return the children codes. This throws an IllegalArgumentException if the children codes were
     * not retrieved.
     * 
     * @return A list of chidlren data set codes or an empty list if there are no children.
     * @throws IllegalArgumentException Thrown if the children codes were not retrieved from the
     *             server.
     */
    @JsonIgnore
    public List<String> getChildrenCodes() throws IllegalArgumentException
    {
        if (getRetrievedConnections().contains(Connections.CHILDREN))
        {
            return Collections.unmodifiableList(childrenCodes);
        } else
        {
            throw new IllegalArgumentException("Children codes were not retrieved for data set "
                    + getCode() + ".");
        }
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
        builder.append(getProperties());
        return builder.toString();
    }

    //
    // JSON-RPC
    //

    private Sample()
    {
    }

    private void setId(Long id)
    {
        this.id = id;
    }

    private void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    private void setPermId(String permId)
    {
        this.permId = permId;
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    private void setExperimentIdentifierOrNull(String experimentIdentifierOrNull)
    {
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

    private void setSampleTypeId(Long sampleTypeId)
    {
        this.sampleTypeId = sampleTypeId;
    }

    private void setSampleTypeCode(String sampleTypeCode)
    {
        this.sampleTypeCode = sampleTypeCode;
    }

    private void setProperties(HashMap<String, String> properties)
    {
        this.properties = properties;
    }

    private void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
    {
        this.registrationDetails = registrationDetails;
    }

    private void setRetrievedConnections(EnumSet<Connections> retrievedConnections)
    {
        this.retrievedConnections = retrievedConnections;
    }

}
