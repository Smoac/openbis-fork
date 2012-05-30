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
import org.codehaus.jackson.annotate.JsonIgnore;
import ch.systemsx.cisd.common.json.JsonObject;

/**
 * Immutable value object representing a data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonObject("DataSet")
public final class DataSet implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonObject("Connections")
    public static enum Connections
    {
        PARENTS, CHILDREN
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

        private boolean containerDataSet;

        private EnumSet<Connections> retrievedConnections = EnumSet.noneOf(Connections.class);

        private List<String> parentCodes = Collections.emptyList();

        private List<String> childrenCodes = Collections.emptyList();

        private List<DataSet> containedDataSets = Collections.emptyList();

        private HashMap<String, String> properties = new HashMap<String, String>();

        private EntityRegistrationDetails registrationDetails;

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

        public void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
        {
            this.registrationDetails = registrationDetails;
        }

        public EntityRegistrationDetails getRegistrationDetails()
        {
            return registrationDetails;
        }

        public boolean isContainerDataSet()
        {
            return containerDataSet;
        }

        public void setContainerDataSet(boolean containerDataSet)
        {
            this.containerDataSet = containerDataSet;
        }

        public List<DataSet> getContainedDataSets()
        {
            return containedDataSets;
        }

        public void setContainedDataSets(List<DataSet> containedDataSets)
        {
            this.containedDataSets =
                    (null == containedDataSets) ? new ArrayList<DataSet>() : containedDataSets;
        }

    }

    private String code;

    private String experimentIdentifier;

    private String sampleIdentifierOrNull;

    private String dataSetTypeCode;

    private boolean containerDataSet;

    private HashMap<String, String> properties;

    // For handling connections to entities
    private EnumSet<Connections> retrievedConnections;

    private List<String> parentCodes = Collections.emptyList();

    private List<String> childrenCodes = Collections.emptyList();

    private List<DataSet> containedDataSets = Collections.emptyList();

    private EntityRegistrationDetails registrationDetails;

    private DataSetFetchOptions fetchOptions;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public DataSet(DataSetInitializer initializer)
    {
        InitializingChecks.checkValidString(initializer.getCode(), "Unspecified code.");
        this.code = initializer.getCode();

        this.experimentIdentifier = initializer.getExperimentIdentifier();

        this.sampleIdentifierOrNull = initializer.getSampleIdentifierOrNull();

        InitializingChecks.checkValidString(initializer.getDataSetTypeCode(),
                "Unspecified data set type code.");
        this.dataSetTypeCode = initializer.getDataSetTypeCode();

        this.properties = initializer.getProperties();

        this.retrievedConnections = initializer.getRetrievedConnections();
        this.parentCodes = initializer.getParentCodes();
        this.childrenCodes = initializer.getChildrenCodes();

        InitializingChecks.checkValidRegistrationDetails(initializer.getRegistrationDetails(),
                "Unspecified entity registration details.");
        this.registrationDetails = initializer.getRegistrationDetails();
        this.containerDataSet = initializer.isContainerDataSet();
        this.containedDataSets = initializer.getContainedDataSets();

    }

    /**
     * Returns the sample code;
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns the identifier of the Experiment to which this data set belongs.
     * 
     * @return <code>null</code> if this data set is not completely filled with all information
     *         available. That is, <code>{@link #getExperimentIdentifier()} == null</code> indicates
     *         that {@link #getSampleIdentifierOrNull()}, {@link #getProperties()},
     *         {@link #getChildrenCodes()}, {@link #getParentCodes()}, and
     *         {@link #getContainedDataSets()} do not return correct results.
     */
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

    @JsonIgnore
    public Date getRegistrationDate()
    {
        return getRegistrationDetails().getRegistrationDate();
    }

    public HashMap<String, String> getProperties()
    {
        return properties;
    }

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

    /**
     * Return the data set registration details.
     * 
     * @since 1.11
     */
    public EntityRegistrationDetails getRegistrationDetails()
    {
        return registrationDetails;
    }

    public boolean isContainerDataSet()
    {
        return containerDataSet;
    }

    public List<DataSet> getContainedDataSets()
    {
        return containedDataSets;
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

    //
    // JSON-RPC
    //

    private DataSet()
    {
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setExperimentIdentifier(String experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

    private void setSampleIdentifierOrNull(String sampleIdentifierOrNull)
    {
        this.sampleIdentifierOrNull = sampleIdentifierOrNull;
    }

    private void setDataSetTypeCode(String dataSetTypeCode)
    {
        this.dataSetTypeCode = dataSetTypeCode;
    }

    private void setProperties(HashMap<String, String> properties)
    {
        this.properties = properties;
    }

    private void setRetrievedConnections(EnumSet<Connections> retrievedConnections)
    {
        this.retrievedConnections = retrievedConnections;
    }

    private void setParentCodes(List<String> parentCodes)
    {
        this.parentCodes = parentCodes;
    }

    private void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
    {
        this.registrationDetails = registrationDetails;
    }

    public void setContainerDataSet(boolean containerDataSet)
    {
        this.containerDataSet = containerDataSet;
    }

    public void setContainedDataSets(List<DataSet> containedDataSets)
    {
        this.containedDataSets = containedDataSets;
    }

    public DataSetFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(DataSetFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
        if (fetchOptions != null)
        {
            if (fetchOptions.isSupersetOf(DataSetFetchOption.PARENTS, DataSetFetchOption.CHILDREN))
            {
                setRetrievedConnections(EnumSet.of(Connections.PARENTS, Connections.CHILDREN));
            } else if (fetchOptions.isSupersetOf(DataSetFetchOption.PARENTS))
            {
                setRetrievedConnections(EnumSet.of(Connections.PARENTS));
            } else if (fetchOptions.isSupersetOf(DataSetFetchOption.CHILDREN))
            {
                setRetrievedConnections(EnumSet.of(Connections.CHILDREN));
            }
        }
    }
}
