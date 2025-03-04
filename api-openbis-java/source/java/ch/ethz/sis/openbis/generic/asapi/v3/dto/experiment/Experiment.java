/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.entity.AbstractEntity;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.property.PropertiesDeserializer;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.experiment.Experiment")
public class Experiment extends AbstractEntity<Experiment>
        implements Serializable, IAttachmentsHolder, ICodeHolder, IDataSetsHolder,
        IEntityTypeHolder, IIdentifierHolder, IMaterialPropertiesHolder, IModificationDateHolder,
        IModifierHolder, IPermIdHolder, IProjectHolder, IPropertiesHolder, IRegistrationDateHolder,
        IRegistratorHolder, ISamplesHolder, ITagsHolder
{
    private static final long serialVersionUID = 1L;

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
    private List<HistoryEntry> propertiesHistory;

    @JsonProperty
    private List<HistoryEntry> projectHistory;

    @JsonProperty
    private List<HistoryEntry> samplesHistory;

    @JsonProperty
    private List<HistoryEntry> dataSetsHistory;

    @JsonProperty
    private List<HistoryEntry> unknownHistory;

    @JsonProperty
    private Map<String, Material> materialProperties;

    @JsonProperty
    private Map<String, Sample[]> sampleProperties;

    @JsonProperty
    private Set<Tag> tags;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Person modifier;

    @JsonProperty
    private List<Attachment> attachments;

    @JsonProperty
    private Map<String, String> metaData;

    @JsonProperty
    private boolean immutableData;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public ExperimentFetchOptions getFetchOptions()
    {
        return (ExperimentFetchOptions) super.getFetchOptions();
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(ExperimentFetchOptions fetchOptions)
    {
        super.setFetchOptions(fetchOptions);
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

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public boolean isFrozen()
    {
        return frozen;
    }

    // Method automatically generated with DtoGenerator
    public void setFrozen(boolean frozen)
    {
        this.frozen = frozen;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public boolean isFrozenForDataSets()
    {
        return frozenForDataSets;
    }

    // Method automatically generated with DtoGenerator
    public void setFrozenForDataSets(boolean frozenForDataSets)
    {
        this.frozenForDataSets = frozenForDataSets;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public boolean isFrozenForSamples()
    {
        return frozenForSamples;
    }

    // Method automatically generated with DtoGenerator
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
    public List<HistoryEntry> getPropertiesHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasPropertiesHistory())
        {
            return propertiesHistory;
        } else
        {
            throw new NotFetchedException("Properties history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setPropertiesHistory(List<HistoryEntry> propertiesHistory)
    {
        this.propertiesHistory = propertiesHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getProjectHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasProjectHistory())
        {
            return projectHistory;
        } else
        {
            throw new NotFetchedException("Project history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setProjectHistory(List<HistoryEntry> projectHistory)
    {
        this.projectHistory = projectHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getSamplesHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSamplesHistory())
        {
            return samplesHistory;
        } else
        {
            throw new NotFetchedException("Samples history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSamplesHistory(List<HistoryEntry> samplesHistory)
    {
        this.samplesHistory = samplesHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getDataSetsHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasDataSetsHistory())
        {
            return dataSetsHistory;
        } else
        {
            throw new NotFetchedException("Data sets history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setDataSetsHistory(List<HistoryEntry> dataSetsHistory)
    {
        this.dataSetsHistory = dataSetsHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getUnknownHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasUnknownHistory())
        {
            return unknownHistory;
        } else
        {
            throw new NotFetchedException("Unknown history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setUnknownHistory(List<HistoryEntry> unknownHistory)
    {
        this.unknownHistory = unknownHistory;
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
    public Map<String, Sample[]> getSampleProperties()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSampleProperties())
        {
            return sampleProperties;
        } else
        {
            throw new NotFetchedException("Sample Properties have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSampleProperties(Map<String, Sample[]> sampleProperties)
    {
        this.sampleProperties = sampleProperties;
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

    @JsonIgnore
    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData)
    {
        this.metaData = metaData;
    }

    @JsonIgnore
    public boolean isImmutableData()
    {
        return immutableData;
    }

    public void setImmutableData(boolean immutableData)
    {
        this.immutableData = immutableData;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Experiment " + permId;
    }

}
