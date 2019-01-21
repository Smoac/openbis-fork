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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IAttachmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDataSetsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityTypeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IIdentifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMaterialPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IProjectHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISamplesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ITagsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
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
@JsonObject("as.dto.experiment.Experiment")
public class Experiment implements Serializable, IAttachmentsHolder, ICodeHolder, IDataSetsHolder, IEntityTypeHolder, IIdentifierHolder,
        IMaterialPropertiesHolder, IModificationDateHolder, IModifierHolder, IPermIdHolder, IProjectHolder, IPropertiesHolder,
        IRegistrationDateHolder, IRegistratorHolder, ISamplesHolder, ITagsHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExperimentFetchOptions fetchOptions;

    @JsonProperty
    private ExperimentPermId permId;

    @JsonProperty
    private ExperimentIdentifier identifier;

    @JsonProperty
    private String code;

    @JsonProperty
    private boolean frozen;

    @JsonProperty
    private boolean frozenForDataSets;

    @JsonProperty
    private boolean frozenForSamples;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private ExperimentType type;

    @JsonProperty
    private Project project;

    @JsonProperty
    private List<DataSet> dataSets;

    @JsonProperty
    private List<Sample> samples;

    @JsonProperty
    private List<HistoryEntry> history;

    @JsonProperty
    private Map<String, String> properties;

    @JsonProperty
    private Map<String, Material> materialProperties;

    @JsonProperty
    private Set<Tag> tags;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Person modifier;

    @JsonProperty
    private List<Attachment> attachments;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public ExperimentFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(ExperimentFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public ExperimentPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(ExperimentPermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public ExperimentIdentifier getIdentifier()
    {
        return identifier;
    }

    // Method automatically generated with DtoGenerator
    public void setIdentifier(ExperimentIdentifier identifier)
    {
        this.identifier = identifier;
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

    public boolean isFrozen()
    {
        return frozen;
    }

    public void setFrozen(boolean frozen)
    {
        this.frozen = frozen;
    }

    public boolean isFrozenForDataSets()
    {
        return frozenForDataSets;
    }

    public void setFrozenForDataSets(boolean frozenForDataSets)
    {
        this.frozenForDataSets = frozenForDataSets;
    }

    public boolean isFrozenForSamples()
    {
        return frozenForSamples;
    }

    public void setFrozenForSamples(boolean frozenForSamples)
    {
        this.frozenForSamples = frozenForSamples;
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
    public ExperimentType getType()
    {
        if (getFetchOptions() != null && getFetchOptions().hasType())
        {
            return type;
        } else
        {
            throw new NotFetchedException("Experiment type has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setType(ExperimentType type)
    {
        this.type = type;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Project getProject()
    {
        if (getFetchOptions() != null && getFetchOptions().hasProject())
        {
            return project;
        } else
        {
            throw new NotFetchedException("Project has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setProject(Project project)
    {
        this.project = project;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<DataSet> getDataSets()
    {
        if (getFetchOptions() != null && getFetchOptions().hasDataSets())
        {
            return dataSets;
        } else
        {
            throw new NotFetchedException("Data sets have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setDataSets(List<DataSet> dataSets)
    {
        this.dataSets = dataSets;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<Sample> getSamples()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSamples())
        {
            return samples;
        } else
        {
            throw new NotFetchedException("Samples have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSamples(List<Sample> samples)
    {
        this.samples = samples;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasHistory())
        {
            return history;
        } else
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
    public Map<String, String> getProperties()
    {
        if (getFetchOptions() != null && getFetchOptions().hasProperties())
        {
            return properties;
        } else
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
        } else
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
    public Set<Tag> getTags()
    {
        if (getFetchOptions() != null && getFetchOptions().hasTags())
        {
            return tags;
        } else
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
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        } else
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
    @Override
    public Person getModifier()
    {
        if (getFetchOptions() != null && getFetchOptions().hasModifier())
        {
            return modifier;
        } else
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
    public List<Attachment> getAttachments()
    {
        if (getFetchOptions() != null && getFetchOptions().hasAttachments())
        {
            return attachments;
        } else
        {
            throw new NotFetchedException("Attachments have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setAttachments(List<Attachment> attachments)
    {
        this.attachments = attachments;
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
        return "Experiment " + permId;
    }

}
