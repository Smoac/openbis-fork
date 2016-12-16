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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityTypeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IExperimentHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMaterialPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IParentChildrenHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISampleHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ITagsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.LinkedData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.dataset.DataSet")
public class DataSet implements Serializable, ICodeHolder, IEntityTypeHolder, IExperimentHolder, IMaterialPropertiesHolder, IModificationDateHolder, IModifierHolder, IParentChildrenHolder<DataSet>, IPermIdHolder, IPropertiesHolder, IRegistrationDateHolder, IRegistratorHolder, ISampleHolder, ITagsHolder
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
    private List<DataSet> components;

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
    private String dataProducer;

    @JsonProperty
    private Date dataProductionDate;

    @JsonProperty
    private Date accessDate;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public DataSetFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(DataSetFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public DataSetPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(DataSetPermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getCode()
    {
        return code;
    }

    // Method automatically generated with DtoGenerator
    public void setCode(String code)
    {
        this.code = code;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public DataSetType getType()
    {
        if (getFetchOptions() != null && getFetchOptions().hasType())
        {
            return type;
        }
        else
        {
            throw new NotFetchedException("Data Set type has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setType(DataSetType type)
    {
        this.type = type;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public DataStore getDataStore()
    {
        if (getFetchOptions() != null && getFetchOptions().hasDataStore())
        {
            return dataStore;
        }
        else
        {
            throw new NotFetchedException("Data store has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setDataStore(DataStore dataStore)
    {
        this.dataStore = dataStore;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isMeasured()
    {
        return measured;
    }

    // Method automatically generated with DtoGenerator
    public void setMeasured(Boolean measured)
    {
        this.measured = measured;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isPostRegistered()
    {
        return postRegistered;
    }

    // Method automatically generated with DtoGenerator
    public void setPostRegistered(Boolean postRegistered)
    {
        this.postRegistered = postRegistered;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public PhysicalData getPhysicalData()
    {
        if (getFetchOptions() != null && getFetchOptions().hasPhysicalData())
        {
            return physicalData;
        }
        else
        {
            throw new NotFetchedException("Physical data has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setPhysicalData(PhysicalData physicalData)
    {
        this.physicalData = physicalData;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public LinkedData getLinkedData()
    {
        if (getFetchOptions() != null && getFetchOptions().hasLinkedData())
        {
            return linkedData;
        }
        else
        {
            throw new NotFetchedException("Linked data has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setLinkedData(LinkedData linkedData)
    {
        this.linkedData = linkedData;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Experiment getExperiment()
    {
        if (getFetchOptions() != null && getFetchOptions().hasExperiment())
        {
            return experiment;
        }
        else
        {
            throw new NotFetchedException("Experiment has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Sample getSample()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSample())
        {
            return sample;
        }
        else
        {
            throw new NotFetchedException("Sample has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSample(Sample sample)
    {
        this.sample = sample;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Map<String, String> getProperties()
    {
        if (getFetchOptions() != null && getFetchOptions().hasProperties())
        {
            return properties;
        }
        else
        {
            throw new NotFetchedException("Properties have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    @Override
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Map<String, Material> getMaterialProperties()
    {
        if (getFetchOptions() != null && getFetchOptions().hasMaterialProperties())
        {
            return materialProperties;
        }
        else
        {
            throw new NotFetchedException("Material Properties have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    @Override
    public void setMaterialProperties(Map<String, Material> materialProperties)
    {
        this.materialProperties = materialProperties;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<DataSet> getParents()
    {
        if (getFetchOptions() != null && getFetchOptions().hasParents())
        {
            return parents;
        }
        else
        {
            throw new NotFetchedException("Parents have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setParents(List<DataSet> parents)
    {
        this.parents = parents;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<DataSet> getChildren()
    {
        if (getFetchOptions() != null && getFetchOptions().hasChildren())
        {
            return children;
        }
        else
        {
            throw new NotFetchedException("Children have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setChildren(List<DataSet> children)
    {
        this.children = children;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<DataSet> getContainers()
    {
        if (getFetchOptions() != null && getFetchOptions().hasContainers())
        {
            return containers;
        }
        else
        {
            throw new NotFetchedException("Container data sets have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setContainers(List<DataSet> containers)
    {
        this.containers = containers;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<DataSet> getComponents()
    {
        if (getFetchOptions() != null && getFetchOptions().hasComponents())
        {
            return components;
        }
        else
        {
            throw new NotFetchedException("Component data sets have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setComponents(List<DataSet> components)
    {
        this.components = components;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Set<Tag> getTags()
    {
        if (getFetchOptions() != null && getFetchOptions().hasTags())
        {
            return tags;
        }
        else
        {
            throw new NotFetchedException("Tags have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setTags(Set<Tag> tags)
    {
        this.tags = tags;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasHistory())
        {
            return history;
        }
        else
        {
            throw new NotFetchedException("History have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setHistory(List<HistoryEntry> history)
    {
        this.history = history;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getModifier()
    {
        if (getFetchOptions() != null && getFetchOptions().hasModifier())
        {
            return modifier;
        }
        else
        {
            throw new NotFetchedException("Modifier has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setModifier(Person modifier)
    {
        this.modifier = modifier;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        }
        else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getDataProducer()
    {
        return dataProducer;
    }

    // Method automatically generated with DtoGenerator
    public void setDataProducer(String dataProducer)
    {
        this.dataProducer = dataProducer;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getDataProductionDate()
    {
        return dataProductionDate;
    }

    // Method automatically generated with DtoGenerator
    public void setDataProductionDate(Date dataProductionDate)
    {
        this.dataProductionDate = dataProductionDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getAccessDate()
    {
        return accessDate;
    }

    // Method automatically generated with DtoGenerator
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
    public void setProperty(String propertyName, String propertyValue)
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
        }
        properties.put(propertyName, propertyValue);
    }

    @Override
    public Material getMaterialProperty(String propertyName)
    {
        return getMaterialProperties() != null ? getMaterialProperties().get(propertyName) : null;
    }

    @Override
    public void setMaterialProperty(String propertyName, Material propertyValue)
    {
        if (materialProperties == null)
        {
            materialProperties = new HashMap<String, Material>();
        }
        materialProperties.put(propertyName, propertyValue);
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "DataSet " + code;
    }

}
