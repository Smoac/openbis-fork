/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.LinkedData;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.datastore.DataStore;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IParentChildrenHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.ITagsHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
 */
@JsonObject("dto.entity.dataset.DataSet")
public class DataSet implements Serializable, ICodeHolder, IModificationDateHolder, IModifierHolder, IParentChildrenHolder<DataSet>, IPermIdHolder, IPropertiesHolder, IRegistrationDateHolder, IRegistratorHolder, ITagsHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DataSetFetchOptions fetchOptions;

    @JsonProperty
    private DataSetPermId permId;

    @JsonProperty
    private String code;

    @JsonProperty
    private DataSetType type;

    @JsonProperty
    private DataStore dataStore;

    @JsonProperty
    private Boolean measured;

    @JsonProperty
    private Boolean postRegistered;

    @JsonProperty
    private PhysicalData physicalData;

    @JsonProperty
    private LinkedData linkedData;

    @JsonProperty
    private Experiment experiment;

    @JsonProperty
    private Sample sample;

    @JsonProperty
    private Map<String, String> properties;

    @JsonProperty
    private Map<String, Material> materialProperties;

    @JsonProperty
    private List<DataSet> parents;

    @JsonProperty
    private List<DataSet> children;

    @JsonProperty
    private List<DataSet> containers;

    @JsonProperty
    private List<DataSet> contained;

    @JsonProperty
    private Set<Tag> tags;

    @JsonProperty
    private List<HistoryEntry> history;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private Person modifier;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Date accessDate;

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public DataSetFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setFetchOptions(DataSetFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public DataSetPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setPermId(DataSetPermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public String getCode()
    {
        return code;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setCode(String code)
    {
        this.code = code;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public DataSetType getType()
    {
        if (getFetchOptions().hasType())
        {
            return type;
        }
        else
        {
            throw new NotFetchedException("Data Set type has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setType(DataSetType type)
    {
        this.type = type;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public DataStore getDataStore()
    {
        if (getFetchOptions().hasDataStore())
        {
            return dataStore;
        }
        else
        {
            throw new NotFetchedException("Data store has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setDataStore(DataStore dataStore)
    {
        this.dataStore = dataStore;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public Boolean isMeasured()
    {
        return measured;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setMeasured(Boolean measured)
    {
        this.measured = measured;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public Boolean isPostRegistered()
    {
        return postRegistered;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setPostRegistered(Boolean postRegistered)
    {
        this.postRegistered = postRegistered;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public PhysicalData getPhysicalData()
    {
        if (getFetchOptions().hasPhysicalData())
        {
            return physicalData;
        }
        else
        {
            throw new NotFetchedException("Physical data has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setPhysicalData(PhysicalData physicalData)
    {
        this.physicalData = physicalData;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public LinkedData getLinkedData()
    {
        if (getFetchOptions().hasLinkedData())
        {
            return linkedData;
        }
        else
        {
            throw new NotFetchedException("Linked data has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setLinkedData(LinkedData linkedData)
    {
        this.linkedData = linkedData;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public Experiment getExperiment()
    {
        if (getFetchOptions().hasExperiment())
        {
            return experiment;
        }
        else
        {
            throw new NotFetchedException("Experiment has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public Sample getSample()
    {
        if (getFetchOptions().hasSample())
        {
            return sample;
        }
        else
        {
            throw new NotFetchedException("Sample has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setSample(Sample sample)
    {
        this.sample = sample;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Map<String, String> getProperties()
    {
        if (getFetchOptions().hasProperties())
        {
            return properties;
        }
        else
        {
            throw new NotFetchedException("Properties have not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Map<String, Material> getMaterialProperties()
    {
        if (getFetchOptions().hasMaterialProperties())
        {
            return materialProperties;
        }
        else
        {
            throw new NotFetchedException("Material Properties have not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setMaterialProperties(Map<String, Material> materialProperties)
    {
        this.materialProperties = materialProperties;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public List<DataSet> getParents()
    {
        if (getFetchOptions().hasParents())
        {
            return parents;
        }
        else
        {
            throw new NotFetchedException("Parents have not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setParents(List<DataSet> parents)
    {
        this.parents = parents;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public List<DataSet> getChildren()
    {
        if (getFetchOptions().hasChildren())
        {
            return children;
        }
        else
        {
            throw new NotFetchedException("Children have not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setChildren(List<DataSet> children)
    {
        this.children = children;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public List<DataSet> getContainers()
    {
        if (getFetchOptions().hasContainers())
        {
            return containers;
        }
        else
        {
            throw new NotFetchedException("Container data sets have not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setContainers(List<DataSet> containers)
    {
        this.containers = containers;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public List<DataSet> getContained()
    {
        if (getFetchOptions().hasContained())
        {
            return contained;
        }
        else
        {
            throw new NotFetchedException("Contained data sets have not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setContained(List<DataSet> contained)
    {
        this.contained = contained;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Set<Tag> getTags()
    {
        if (getFetchOptions().hasTags())
        {
            return tags;
        }
        else
        {
            throw new NotFetchedException("Tags have not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setTags(Set<Tag> tags)
    {
        this.tags = tags;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public List<HistoryEntry> getHistory()
    {
        if (getFetchOptions().hasHistory())
        {
            return history;
        }
        else
        {
            throw new NotFetchedException("History have not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setHistory(List<HistoryEntry> history)
    {
        this.history = history;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Person getModifier()
    {
        if (getFetchOptions().hasModifier())
        {
            return modifier;
        }
        else
        {
            throw new NotFetchedException("Modifier has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setModifier(Person modifier)
    {
        this.modifier = modifier;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions().hasRegistrator())
        {
            return registrator;
        }
        else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public Date getAccessDate()
    {
        return accessDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setAccessDate(Date accessDate)
    {
        this.accessDate = accessDate;
    }

    @Override
    public String getProperty(String propertyName)
    {
        return getProperties() != null ? getProperties().get(propertyName) : null;
    }

    @Override
    public Material getMaterialProperty(String propertyName)
    {
        return getMaterialProperties() != null ? getMaterialProperties().get(propertyName) : null;
    }

    @Override
    public String toString()
    {
        return "DataSet " + code;
    }

}
