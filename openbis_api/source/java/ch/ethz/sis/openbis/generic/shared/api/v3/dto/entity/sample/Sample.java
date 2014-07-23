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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
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
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("Sample")
public class Sample implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private SampleFetchOptions fetchOptions;

    @JsonProperty
    private SamplePermId permId;

    @JsonProperty
    private SampleIdentifier identifier;

    @JsonProperty
    private String code;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private SampleType sampleType;

    @JsonProperty
    private Space space;

    @JsonProperty
    private Experiment experiment;

    @JsonProperty
    private Map<String, String> properties;

    @JsonProperty
    private List<Sample> parents;

    @JsonProperty
    private List<Sample> children;

    @JsonProperty
    private Sample container;

    @JsonProperty
    private List<Sample> contained;

    @JsonProperty
    private Set<Tag> tags;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Person modifier;

    @JsonProperty
    private List<Attachment> attachments;

    @JsonIgnore
    public SampleFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(SampleFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    public SamplePermId getPermId()
    {
        return permId;
    }

    public void setPermId(SamplePermId permId)
    {
        this.permId = permId;
    }

    @JsonIgnore
    public SampleIdentifier getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(SampleIdentifier identifier)
    {
        this.identifier = identifier;
    }

    @JsonIgnore
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @JsonIgnore
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @JsonIgnore
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    @JsonIgnore
    public SampleType getSampleType()
    {
        if (getFetchOptions().hasSampleType())
        {
            return sampleType;
        }
        else
        {
            throw new NotFetchedException("Sample type has not been fetched.");
        }
    }

    public void setSampleType(SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    @JsonIgnore
    public Space getSpace()
    {
        if (getFetchOptions().hasSpace())
        {
            return space;
        }
        else
        {
            throw new NotFetchedException("Space has not been fetched.");
        }
    }

    public void setSpace(Space space)
    {
        this.space = space;
    }

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

    public void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    @JsonIgnore
    public Map<String, String> getProperties()
    {
        if (getFetchOptions().hasProperties())
        {
            return properties;
        }
        else
        {
            throw new NotFetchedException("Properties has not been fetched.");
        }
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    @JsonIgnore
    public List<Sample> getParents()
    {
        if (getFetchOptions().hasParents())
        {
            return parents;
        }
        else
        {
            throw new NotFetchedException("Parents has not been fetched.");
        }
    }

    public void setParents(List<Sample> parents)
    {
        this.parents = parents;
    }

    @JsonIgnore
    public List<Sample> getChildren()
    {
        if (getFetchOptions().hasChildren())
        {
            return children;
        }
        else
        {
            throw new NotFetchedException("Children has not been fetched.");
        }
    }

    public void setChildren(List<Sample> children)
    {
        this.children = children;
    }

    @JsonIgnore
    public Sample getContainer()
    {
        if (getFetchOptions().hasContainer())
        {
            return container;
        }
        else
        {
            throw new NotFetchedException("Container sample has not been fetched.");
        }
    }

    public void setContainer(Sample container)
    {
        this.container = container;
    }

    @JsonIgnore
    public List<Sample> getContained()
    {
        if (getFetchOptions().hasContained())
        {
            return contained;
        }
        else
        {
            throw new NotFetchedException("Contained samples has not been fetched.");
        }
    }

    public void setContained(List<Sample> contained)
    {
        this.contained = contained;
    }

    @JsonIgnore
    public Set<Tag> getTags()
    {
        if (getFetchOptions().hasTags())
        {
            return tags;
        }
        else
        {
            throw new NotFetchedException("Tags has not been fetched.");
        }
    }

    public void setTags(Set<Tag> tags)
    {
        this.tags = tags;
    }

    @JsonIgnore
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

    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    @JsonIgnore
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

    public void setModifier(Person modifier)
    {
        this.modifier = modifier;
    }

    @JsonIgnore
    public List<Attachment> getAttachments()
    {
        if (getFetchOptions().hasAttachments())
        {
            return attachments;
        }
        else
        {
            throw new NotFetchedException("Attachments has not been fetched.");
        }
    }

    public void setAttachments(List<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    @Override
    public String toString()
    {
        return "Sample " + permId;
    }

}
