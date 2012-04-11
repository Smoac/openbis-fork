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

        private EnumSet<SampleFetchOption> retrievedFetchOptions = EnumSet.noneOf(SampleFetchOption.class);

        private List<Sample> parents = Collections.emptyList();

        private List<Sample> children = Collections.emptyList();

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

        public void setRetrievedFetchOptions(EnumSet<SampleFetchOption> retrievedFetchOptions)
        {
            this.retrievedFetchOptions =
                    (null == retrievedFetchOptions) ? EnumSet.noneOf(SampleFetchOption.class)
                            : retrievedFetchOptions;
        }

        public EnumSet<SampleFetchOption> getRetrievedFetchOptions()
        {
            return retrievedFetchOptions;
        }

        public void setParents(List<Sample> parents)
        {
            this.parents = (null == parents) ? new ArrayList<Sample>() : parents;
        }

        public List<Sample> getParents()
        {
            return parents;
        }

        public List<Sample> getChildren()
        {
            return children;
        }

        public void setChildren(List<Sample> children)
        {
            this.children = (null == children) ? new ArrayList<Sample>() : children;
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

    private EnumSet<SampleFetchOption> retrievedFetchOptions;

    private List<Sample> parents = Collections.emptyList();

    private List<Sample> children = Collections.emptyList();

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

        this.retrievedFetchOptions = initializer.getRetrievedFetchOptions();
        this.parents = initializer.getParents();
        this.children = initializer.getChildren();
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
     * Returns fetch options used to retrieved this sample object.
     */
    public EnumSet<SampleFetchOption> getRetrievedFetchOptions()
    {
        return retrievedFetchOptions;
    }

    /**
     * Return the children.
     * 
     * @return Children of this sample or an empty list if there are no children.
     * @throws IllegalArgumentException Thrown if the children were not retrieved from the server.
     */
    @JsonIgnore
    public List<Sample> getChildren() throws IllegalArgumentException
    {
        if (getRetrievedFetchOptions().contains(SampleFetchOption.CHILDREN))
        {
            return Collections.unmodifiableList(children);
        } else
        {
            throw new IllegalArgumentException("Children were not retrieved for sample "
                    + getIdentifier() + ".");
        }
    }

    /**
     * Return the parents.
     * 
     * @return All parents of this sample or an empty list if there are no parents.
     * @throws IllegalArgumentException Thrown if the parents were not retrieved from the server.
     */
    @JsonIgnore
    public List<Sample> getParents() throws IllegalArgumentException
    {
        if (getRetrievedFetchOptions().contains(SampleFetchOption.PARENTS))
        {
            return Collections.unmodifiableList(parents);
        } else
        {
            throw new IllegalArgumentException("Parents were not retrieved for sample "
                    + getIdentifier() + ".");
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
        if (retrievedFetchOptions.contains(SampleFetchOption.PROPERTIES))
        {
            builder.append(getProperties());
        } else
        {
            builder.append("properties=?");
        }
        if (retrievedFetchOptions.contains(SampleFetchOption.PARENTS))
        {
            builder.append("parents", getParents());
        } else
        {
            builder.append("parents=?");
        }
        if (retrievedFetchOptions.contains(SampleFetchOption.CHILDREN))
        {
            builder.append("children", getChildren());
        }
        else
        {
            builder.append("children=?");
        }
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

    private void setRetrievedFetchOptions(EnumSet<SampleFetchOption> fetchOptions)
    {
        this.retrievedFetchOptions = fetchOptions;
    }

}
